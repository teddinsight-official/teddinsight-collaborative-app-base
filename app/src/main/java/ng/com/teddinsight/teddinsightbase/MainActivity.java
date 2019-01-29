package ng.com.teddinsight.teddinsightbase;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            FirebaseAuth.getInstance().signInWithEmailAndPassword("creative.designer@teddinsight.com", "donmickey").addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    getSupportFragmentManager().beginTransaction().replace(R.id.content, StaffsListFragment.NewInstance()).commit();
//                } else {
//                    Toast.makeText(MainActivity.this, "login failed " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            getSupportFragmentManager().beginTransaction().replace(R.id.content, StaffsListFragment.NewInstance()).commit();
//        }
//
//    }
//
//    @Override
//    public void onStaffItemClicked(User user) {
//        getSupportFragmentManager().beginTransaction().replace(R.id.content, ThreadFragment.NewInstance(user)).addToBackStack(null).commit();
//    }
}
