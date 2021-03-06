package ng.com.teddinsight.teddinsightchat.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import ng.com.teddinsight.teddinsightchat.utils.ExtraUtils;
import ng.com.teddinsight.teddinsightchat.adapters.MessagesAdapter;
import ng.com.teddinsight.teddinsightchat.R;
import ng.com.teddinsight.teddinsightchat.R2;
import ng.com.teddinsight.teddinsightchat.models.Message;
import ng.com.teddinsight.teddinsightchat.models.Notifications;
import ng.com.teddinsight.teddinsightchat.models.User;
import ng.com.teddinsight.teddinsightchat.widgets.EmptyStateRecyclerView;


import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class ThreadFragment extends Fragment implements TextWatcher {
    public static final String BUNDLE_CURRENT_USER = "current_user";
    public static final String BUNDLE_CHAT_USER = "chat_user";
    private static final String TAG = ThreadFragment.class.getSimpleName();
    @BindView(R2.id.user_avatar)
    CircleImageView userAvatar;
    @BindView(R2.id.user_name)
    TextView userName;
    @BindView(R2.id.last_seen)
    TextView lastSeenView;
    @BindView(R2.id.back_button)
    ImageButton backButton;
    @BindView(R2.id.activity_thread_messages_recycler)
    EmptyStateRecyclerView messagesRecycler;
    @BindView(R2.id.activity_thread_send_fab)
    FloatingActionButton sendFab;
    @BindView(R2.id.activity_thread_input_edit_text)
    TextInputEditText inputEditText;
    @BindView(R2.id.activity_thread_empty_view)
    TextView emptyView;
    @BindView(R2.id.activity_thread_editor_parent)
    RelativeLayout editorParent;
    @BindView(R2.id.activity_thread_progress)
    ProgressBar progress;
    User chatingWithUser;
    User currentUser;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mDatabase;
    private MessagesAdapter adapter;

    protected boolean isLoading;
    protected boolean userInteracted;
    boolean emptyInput;
    String chatRef = "";


    public static ThreadFragment NewInstance(User currentUser, User chatingWithUser) {
        Bundle b = new Bundle();
        b.putParcelable(BUNDLE_CURRENT_USER, currentUser);
        b.putParcelable(BUNDLE_CHAT_USER, chatingWithUser);
        ThreadFragment threadFragment = new ThreadFragment();
        threadFragment.setArguments(b);
        return threadFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_thread, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        chatingWithUser = bundle.getParcelable(BUNDLE_CHAT_USER);
        currentUser = bundle.getParcelable(BUNDLE_CURRENT_USER);
        initializeInteractionListeners();
        if (chatingWithUser == null || currentUser == null) {
            Toast.makeText(getContext(), "Cannot start chat", Toast.LENGTH_LONG).show();
            backButton.performClick();
            return;
        }
        mDatabase = rootRef;
        sendFab.requestFocus();
        String defaultUrl = "https://png.pngtree.com/svg/20161021/de74bae88b.png";
        Picasso.get().load(chatingWithUser.getProfileImageUrl() == null || TextUtils.isEmpty(chatingWithUser.getProfileImageUrl()) ? defaultUrl : chatingWithUser.getProfileImageUrl()).placeholder(R.drawable.avatar).into(userAvatar);
        userName.setText(chatingWithUser.getFirstName().concat(" ").concat(chatingWithUser.getLastName()));
        int res = chatingWithUser.getId().compareTo(firebaseUser.getUid());
        if (res > 0)
            chatRef = chatingWithUser.getId().concat("_").concat(firebaseUser.getUid());
        else
            chatRef = firebaseUser.getUid().concat("_").concat(chatingWithUser.getId());
        initializeMessagesRecycler();
        rootRef.child("presence").child(ExtraUtils.getWorkspaceId()).child(chatingWithUser.id).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Object onlineObj = dataSnapshot.getValue();
                if (onlineObj != null) {
                    String online = onlineObj.toString();
                    try {
                        long timeAgo = Long.parseLong(online);
                        online = "last seen: ".concat(ExtraUtils.getTimeAgo(timeAgo));
                        lastSeenView.setText(online);
                    } catch (Exception e) {
                        lastSeenView.setText("Online");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeInteractionListeners() {
        inputEditText.addTextChangedListener(this);
    }

    private void initializeMessagesRecycler() {
        if (chatingWithUser == null || firebaseUser == null) {
            Log.d("@@@@", "initializeMessagesRecycler: User:" + firebaseUser + " Owner:" + userName);
            return;
        }
        Query messagesQuery = mDatabase
                .child("messages")
                .child(ExtraUtils.getWorkspaceId())
                .child(chatRef)
                .orderByChild("negatedTimestamp");
        FirebaseRecyclerOptions<Message> options =
                new FirebaseRecyclerOptions.Builder<Message>()
                        .setQuery(messagesQuery, Message.class)
                        .build();
        adapter = new MessagesAdapter(options, getContext());
        messagesRecycler.setAdapter(null);
        messagesRecycler.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        messagesRecycler.setLayoutManager(mLayoutManager);
        /*messagesRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                    Toast.makeText(getContext(), "end of list", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        messagesRecycler.setEmptyView(emptyView);
        messagesRecycler.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messagesRecycler.smoothScrollToPosition(0);
            }
        });
    }

    @OnClick(R2.id.activity_thread_send_fab)
    public void onClick() {
        if (chatingWithUser == null || firebaseUser == null) {
            Log.d("@@@@", "onSendClick: User:" + firebaseUser + " Owner:" + chatingWithUser);
            return;
        }
        long timestamp = new Date().getTime();
        long dayTimestamp = getDayTimestamp(timestamp);
        String body = inputEditText.getText().toString().trim();
        if (TextUtils.isEmpty(body)) {
            Toast.makeText(getContext(), "We can't send an empty message, can we?", Toast.LENGTH_SHORT).show();
            return;
        }
        String ownerUid = firebaseUser.getUid();
        String userUid = chatingWithUser.getId();
        Message message =
                new Message(timestamp, -timestamp, dayTimestamp, body, ownerUid, userUid);

        mDatabase
                .child("messages")
                .child(ExtraUtils.getWorkspaceId())
                .child(chatRef)
                .push()
                .setValue(message);
        mDatabase.child("chat")
                .child(ExtraUtils.getWorkspaceId())
                .child(firebaseUser.getUid())
                .child(chatingWithUser.getId())
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        User chatUser = mutableData.getValue(User.class);
                        if (chatUser == null) {
                            chatUser = new User(chatingWithUser.getId(), chatingWithUser.getProfileImageUrl(), chatingWithUser.getUsername(), chatingWithUser.getFirstName(), chatingWithUser.getLastName(), 0, body);
                        } else {
//                            long currentMessageCount = chatUser.getUnreadCount();
//                            currentMessageCount = currentMessageCount + 1;
//                            chatUser.setUnreadCount(currentMessageCount);
                            chatUser.setLastMessage(body);
                            chatUser.setTimeStamp(System.currentTimeMillis() * (-1));
                        }
                        mutableData.setValue(chatUser.toChatMap());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.e(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
        mDatabase.child("chat")
                .child(ExtraUtils.getWorkspaceId())
                .child(chatingWithUser.getId())
                .child(firebaseUser.getUid())
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        User chatUser = mutableData.getValue(User.class);
                        if (chatUser == null) {
                            chatUser = new User(currentUser.getId(), currentUser.getProfileImageUrl(), currentUser.getUsername(), currentUser.getFirstName(), currentUser.getLastName(), 1, body);
                        } else {
                            long currentMessageCount = chatUser.getUnreadCount();
                            currentMessageCount = currentMessageCount + 1;
                            chatUser.setUnreadCount(currentMessageCount);
                            chatUser.setLastMessage(body);
                            chatUser.setTimeStamp(System.currentTimeMillis() * (-1));
                        }
                        mutableData.setValue(chatUser.toChatMap());
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.e(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
        mDatabase
                .child("notifications")
                .child(ExtraUtils.getWorkspaceId())
                .child(chatingWithUser.getId())
                .runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        Notifications notifications = mutableData.getValue(Notifications.class);
                        if (notifications == null) {
                            return Transaction.success(mutableData);
                        }

                        notifications.count = notifications.count + 1;
                        notifications.type = "Chat";
                        notifications.message = body;
                        mutableData.setValue(notifications);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        Log.e(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });

        inputEditText.setText("");
    }


    protected void displayLoadingState() {
        //was considering a progress bar but firebase offline database makes it unnecessary

        //TransitionManager.beginDelayedTransition(editorParent);
        progress.setVisibility(isLoading ? VISIBLE : INVISIBLE);
        //displayInputState();
    }

    private void displayInputState() {
        //inputEditText.setEnabled(!isLoading);
        sendFab.setEnabled(!emptyInput && !isLoading);
        //sendFab.setImageResource(isLoading ? R.color.colorTransparent : R.drawable.ic_send);
    }

    private long getDayTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        emptyInput = s.toString().trim().isEmpty();
        displayInputState();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayLoadingState();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        mDatabase.child("notifications").child(ExtraUtils.getWorkspaceId()).child(firebaseUser.getUid()).child("count").setValue(0);
        mDatabase.child("chat").child(ExtraUtils.getWorkspaceId()).child(firebaseUser.getUid()).child(chatingWithUser.getId()).child("unreadCount").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                if (mutableData.getValue() != null)
                    mutableData.setValue(0);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }
}
