package ng.com.teddinsight.teddinsightchat.listeners;


import com.google.android.gms.tasks.Tasks;

import ng.com.teddinsight.teddinsightchat.models.User;

public class Listeners {


    public interface TaskItemClicked {
        void onTaskItemClicked(boolean isDesigner, Tasks tasks);
    }


    public interface StaffItemListener {
        void onStaffItemClicked(User user);
    }
}
