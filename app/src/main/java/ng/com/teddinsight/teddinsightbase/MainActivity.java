package ng.com.teddinsight.teddinsightbase;

import androidx.appcompat.app.AppCompatActivity;
import ng.com.teddinsight.teddinsightchat.StaffsListFragment;
import ng.com.teddinsight.teddinsightchat.ThreadFragment;
import ng.com.teddinsight.teddinsightchat.User;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements Listeners.StaffItemListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword("creative.designer@teddinsight.com", "donmickey").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.content, StaffsListFragment.NewInstance()).commit();
                } else {
                    Toast.makeText(MainActivity.this, "login failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content, StaffsListFragment.NewInstance()).commit();
        }

    }

    @Override
    public void onStaffItemClicked(User user) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, ThreadFragment.NewInstance(user)).addToBackStack(null).commit();
    }
}
