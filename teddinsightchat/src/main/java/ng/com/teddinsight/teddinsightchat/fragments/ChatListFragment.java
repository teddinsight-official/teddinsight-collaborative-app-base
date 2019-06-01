package ng.com.teddinsight.teddinsightchat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import ng.com.teddinsight.teddinsightchat.R;
import ng.com.teddinsight.teddinsightchat.R2;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.Notifications;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.widgets.EmptyStateRecyclerView;

public class ChatListFragment extends Fragment {

    @BindView(R2.id.activity_main_toolbar)
    Toolbar toolbar;
    @BindView(R2.id.activity_main_users_recycler)
    EmptyStateRecyclerView usersRecycler;
    @BindView(R2.id.activity_main_empty_view)
    TextView emptyView;
    @BindView(R2.id.newChat)
    FloatingActionButton floatingActionButton;
    @BindView(R2.id.title)
    TextView title;
    FirebaseRecyclerAdapter adapter;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private Listeners.StaffItemListener listener;
    public User currentUser;

    public static ChatListFragment NewInstance() {
        return new ChatListFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, v);
        getCurrentUser();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearChatNotification();
        title.setText(getString(R.string.chats));
        floatingActionButton.setVisibility(View.VISIBLE);
        floatingActionButton.setOnClickListener(v -> {
            if (currentUser == null) {
                getCurrentUser();
                Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                return;
            }
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            Fragment prev = getChildFragmentManager().findFragmentByTag("staffDialog");
            if (prev != null)
                fragmentTransaction.remove(prev);
            StaffDialogFragment.NewInstance(currentUser).show(fragmentTransaction, "staffDialog");
        });
        Query query = reference.child("chat").child(firebaseUser.getUid()).orderByChild("timeStamp");
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<User, StaffListViewHolder>(options) {
            @Override
            public StaffListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user, parent, false);

                return new StaffListViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(StaffListViewHolder holder, int position, User user) {
                holder.setUser(user);
            }

        };
        usersRecycler.setAdapter(adapter);
        usersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyView.setText(getString(R.string.no_conversation));
        usersRecycler.setEmptyView(emptyView);


    }

    public void getCurrentUser() {
        reference.child(User.getTableName()).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (Listeners.StaffItemListener) context;

    }

    private void clearChatNotification() {
        FirebaseDatabase.getInstance().getReference().child(Notifications.getTableName()).child(firebaseUser.getUid()).child("count").setValue(0);
    }

    class StaffListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R2.id.item_user_image_view)
        ImageView itemUserImageView;
        @BindView(R2.id.item_friend_name_text_view)
        TextView itemFriendNameTextView;
        @BindView(R2.id.item_friend_email_text_view)
        TextView itemFriendEmailTextView;
        @BindView(R2.id.item_user_parent)
        CardView itemUserParent;
        @BindView(R2.id.new_message_badge)
        TextView newMessageBadge;
        private User chatuser;

        public StaffListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void setUser(User user) {
            chatuser = user;
            itemFriendNameTextView.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
            String profileUrl;
            if (user.getProfileImageUrl() == null || TextUtils.isEmpty(user.getProfileImageUrl()))
                profileUrl = "https://png.pngtree.com/svg/20161021/de74bae88b.png";
            else profileUrl = user.getProfileImageUrl();

            Picasso.get()
                    .load(profileUrl)
                    .into(itemUserImageView);
            if (user.getUnreadCount() > 0) {
                newMessageBadge.setText(user.getUnreadCount() > 99 ? "9+" : String.valueOf(user.getUnreadCount()));
                newMessageBadge.setVisibility(View.VISIBLE);
            } else
                newMessageBadge.setVisibility(View.INVISIBLE);
            if (user.getLastMessage() != null)
                itemFriendEmailTextView.setText(user.getLastMessage());
        }

        void hideUserLayout() {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.height = 0;
            params.setMargins(0, 0, 0, 0);
            itemView.setPadding(0, 0, 0, 0);
            itemView.setLayoutParams(params);
            itemView.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            listener.onStaffItemClicked(currentUser, chatuser);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
