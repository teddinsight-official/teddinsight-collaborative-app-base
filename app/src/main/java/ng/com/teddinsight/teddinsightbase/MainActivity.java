package ng.com.teddinsight.teddinsightbase;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;

import ng.com.teddinsight.teddinsightchat.fragments.ChatListFragment;
import ng.com.teddinsight.teddinsightchat.fragments.ThreadFragment;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.User;


public class MainActivity extends AppCompatActivity implements Listeners.StaffItemListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().signInWithEmailAndPassword("oluwatayoadegboye@gmail.com", "itkalasado13").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.e("jfs", "succ");
                getSupportFragmentManager().beginTransaction().replace(R.id.content, ChatListFragment.NewInstance()).commit();
            } else {
                Log.e("error", task.getException().getLocalizedMessage());
                Toast.makeText(MainActivity.this, "login failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //getSupportFragmentManager().beginTransaction().replace(R.id.content, ChatListFragment.NewInstance()).commit();


    }

    @Override
    public void onStaffItemClicked(User currentUser, User chatUser) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, ThreadFragment.NewInstance(currentUser, chatUser)).addToBackStack(null).commit();
    }

}
