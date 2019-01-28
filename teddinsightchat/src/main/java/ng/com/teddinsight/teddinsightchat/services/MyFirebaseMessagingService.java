package ng.com.teddinsight.teddinsightchat.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String PREF_REG_TOKEN = "reg_token";
    public static final String PREF_SHOULD_SEND_TO_SERVER = "send_token_to_server";
    public static final String PREF_WORK_UUID = "work_uuid";
    public static final String SEND_REG_TOKEN_JOB_TAG = "sendRegTokenTag";

    @Override
    public void onNewToken(String s) {
        Log.e(LOG_TAG, "Refreshed Token: " + s);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        editor.putString(PREF_REG_TOKEN, s);
        editor.putBoolean(PREF_SHOULD_SEND_TO_SERVER, true);
        editor.apply();
        scheduleWork();
    }

    public static void scheduleWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .addTag(SEND_REG_TOKEN_JOB_TAG);
        WorkManager manager = WorkManager.getInstance();
        OneTimeWorkRequest oneTimeWorkRequest = builder.build();
        manager.enqueue(oneTimeWorkRequest);
        Log.e(LOG_TAG, "Work enqueued");

    }

    public static void cancleWork(Context context) {
        WorkManager manager = WorkManager.getInstance();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.contains(PREF_WORK_UUID))
            manager.cancelAllWorkByTag(SEND_REG_TOKEN_JOB_TAG);
    }
}
