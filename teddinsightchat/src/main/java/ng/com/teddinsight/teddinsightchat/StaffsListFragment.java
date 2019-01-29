package ng.com.teddinsight.teddinsightchat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.widgets.EmptyStateRecyclerView;

public class StaffsListFragment extends Fragment {

    @BindView(R2.id.activity_main_toolbar)
    Toolbar toolbar;
    @BindView(R2.id.activity_main_users_recycler)
    EmptyStateRecyclerView usersRecycler;
    @BindView(R2.id.activity_main_empty_view)
    TextView emptyView;
    FirebaseRecyclerAdapter adapter;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private Listeners.StaffItemListener listener;

    public static StaffsListFragment NewInstance() {
        return new StaffsListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearChatNotification();
        Query query = reference.child(User.getTableName());
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
        emptyView.setText(getString(R.string.no_staff));
        usersRecycler.setEmptyView(emptyView);


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
        private User currentUser;

        public StaffListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void setUser(User user) {
            currentUser = user;
            if (!user.role.equals(User.USER_ADMIN) && (user.id.equals(firebaseUser.getUid()) || user.role.equalsIgnoreCase(User.USER_CLIENT) || user.role.equalsIgnoreCase(User.USER_PARTNER)))
                hideUserLayout();
            else {
                if (user.id.equals(firebaseUser.getUid()))
                    hideUserLayout();
            }
            itemFriendNameTextView.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
            Picasso.get()
                    .load(user.getProfileImageUrl())
                    .into(itemUserImageView);
            reference.child("chat").child(firebaseUser.getUid().concat("_").concat(user.getId())).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Object newMessage = dataSnapshot.child(user.id.substring(0, 5).concat("_newMessage")).getValue();
                    boolean newMessageExists = newMessage != null && (boolean) newMessage;
                    Object lastMessage = dataSnapshot.child("lastMessage").getValue();
                    if (newMessageExists)
                        newMessageBadge.setVisibility(View.VISIBLE);
                    else
                        newMessageBadge.setVisibility(View.INVISIBLE);
                    if (lastMessage != null)
                        itemFriendEmailTextView.setText(lastMessage.toString());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
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
            listener.onStaffItemClicked(currentUser);
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
