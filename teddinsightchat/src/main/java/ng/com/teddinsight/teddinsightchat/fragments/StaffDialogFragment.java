package ng.com.teddinsight.teddinsightchat.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ng.com.teddinsight.teddinsightchat.R;
import ng.com.teddinsight.teddinsightchat.R2;
import ng.com.teddinsight.teddinsightchat.listeners.Listeners;
import ng.com.teddinsight.teddinsightchat.models.Notifications;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.widgets.EmptyStateRecyclerView;

public class StaffDialogFragment extends DialogFragment {

    @BindView(R2.id.activity_main_toolbar)
    Toolbar toolbar;
    @BindView(R2.id.activity_main_users_recycler)
    EmptyStateRecyclerView usersRecycler;
    @BindView(R2.id.activity_main_empty_view)
    TextView emptyView;
    User currentUser;

    FirebaseRecyclerAdapter adapter;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private Listeners.StaffItemListener listener;

    public static StaffDialogFragment NewInstance(User currentUser) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ThreadFragment.BUNDLE_CURRENT_USER, currentUser);
        StaffDialogFragment staffDialogFragment = new StaffDialogFragment();
        staffDialogFragment.setArguments(bundle);
        return staffDialogFragment;
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
        currentUser = getArguments().getParcelable(ThreadFragment.BUNDLE_CURRENT_USER);
        if (currentUser == null)
            Objects.requireNonNull(getDialog()).cancel();
        Query query = reference.child(User.getTableName());
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<User, StaffDialogFragment.StaffListViewHolder>(options) {
            @Override
            public StaffDialogFragment.StaffListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_user, parent, false);

                return new StaffDialogFragment.StaffListViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(StaffDialogFragment.StaffListViewHolder holder, int position, User user) {
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
        private User chatUser;

        public StaffListViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void setUser(User user) {
            chatUser = user;
            if (!user.role.equals(User.USER_ADMIN) && (user.id.equals(firebaseUser.getUid()) || user.role.equalsIgnoreCase(User.USER_CLIENT) || user.role.equalsIgnoreCase(User.USER_PARTNER)))
                hideUserLayout();
            else {
                if (user.id.equals(firebaseUser.getUid()))
                    hideUserLayout();
            }
            itemFriendNameTextView.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
            String profileUrl;
            if (user.getProfileImageUrl() == null || TextUtils.isEmpty(user.getProfileImageUrl()))
                profileUrl = "https://png.pngtree.com/svg/20161021/de74bae88b.png";
            else profileUrl = user.getProfileImageUrl();

            Picasso.get()
                    .load(profileUrl)
                    .into(itemUserImageView);
            newMessageBadge.setVisibility(View.GONE);
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
            Objects.requireNonNull(getDialog()).cancel();
            listener.onStaffItemClicked(currentUser, chatUser);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
        adapter.startListening();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }


    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
