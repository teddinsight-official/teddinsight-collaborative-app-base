package ng.com.teddinsight.teddinsightchat.services;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public static final String TAG = MyWorker.class.getSimpleName();

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (user == null)
            return Result.RETRY;
        String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(MyFirebaseMessagingService.PREF_REG_TOKEN, "");
        String uid = user.getUid();
        Log.e(TAG, uid);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users/" + uid);
        userRef.child("deviceToken").setValue(token).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MyFirebaseMessagingService.cancleWork(getApplicationContext());
                Log.e(TAG, "Work Successful");
            } else {
                MyFirebaseMessagingService.scheduleWork();
                Log.e(TAG, "Work Rescheduled");
            }
        });

        return Result.SUCCESS;
    }
}
