package com.chat.bridge;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private String chatUserId, userName;
    private String currentUserId;
    private DatabaseReference rootRef;
    private DatabaseReference currentUserRef;

    private TextView tvDisplayName;
    private TextView tvLastSeen;
    private CircularImageView profileImage;
    private FirebaseAuth mAuth;

    private ImageButton ibChatAdd;
    private ImageButton ibChatSend;
    private EditText etMessage;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerView messagesListLayout;
    private SwipeRefreshLayout swipeMessageLayout;
    private RecyclerView.SmoothScroller smoothScroller;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private MessagesAdapter messagesAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private int currentPageNo = 1;

    // NEW SOLUTION
    private int itemPos = 0;
    private String lastMessageKey = "";
    private String redundantKey = "";
    private String firstMessageKey = "";
    private boolean refreshEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        findViews();

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);

        tvDisplayName = (TextView) actionBarView.findViewById(R.id.tvDisplayName);
        tvLastSeen = (TextView) actionBarView.findViewById(R.id.tvLastSeen);
        profileImage = (CircularImageView) actionBarView.findViewById(R.id.profileImage);
        tvDisplayName.setText(userName);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());
        currentUserRef.keepSynced(true);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.keepSynced(true);

        // LAST SEEN AND ONLINE FEATURE
        rootRef.child("Users").child(chatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if (online.equals("true")) {
                    tvLastSeen.setText("online");
                } else {
                    String time = GetLastSeen.getTimeAgo(Long.parseLong(online));
                    tvLastSeen.setText("last seen " + time);
                }
                String image = dataSnapshot.child("thumbnail").getValue().toString();
                Picasso.with(getBaseContext()).load(image).placeholder(R.drawable.default_image).into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        actionBar.setCustomView(actionBarView);

        // CREATING THE CONVERSATION IF NOT PRESENT
        rootRef.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUserId)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", "false");
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + currentUserId + "/" + chatUserId, chatAddMap);
                    chatUserMap.put("Chat/" + chatUserId + "/" + currentUserId, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e(TAG, "onComplete: " + databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ibChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // RETRIEVING THE DATA
        loadMessages();

        // ADDING THE SWIPE REFRESH FEATURE
        swipeMessageLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                currentPageNo++;
                itemPos = 0;
                loadMoreMessages();
                if (!refreshEnabled) {
                    swipeMessageLayout.setEnabled(false);
                }
            }
        });

    }

    private void loadMoreMessages() {
        final DatabaseReference messageRef = rootRef.child("Messages").child(currentUserId).child(chatUserId);
        messageRef.keepSynced(true);
        final DatabaseReference senderMessageRef = rootRef.child("Messages").child(chatUserId).child(currentUserId);
        senderMessageRef.keepSynced(true);
        Query messageQuery = messageRef.orderByKey().endAt(lastMessageKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
        final Map seenMap = new HashMap();

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                if (message != null && message.getFrom() != null) {
                    if (message.getFrom().equals(chatUserId)){
                        message.setSeen("true");
                        messageRef.child(dataSnapshot.getKey()).child("seen").setValue("true");
                        senderMessageRef.child(dataSnapshot.getKey()).child("seen").setValue("true");
                    }
                }
                if (!dataSnapshot.getKey().equals(redundantKey)) {
                    messagesList.add(itemPos++, message);
                }
                if (itemPos == 1) {
                    redundantKey = lastMessageKey;
                    lastMessageKey = dataSnapshot.getKey();
                }
                messagesAdapter.notifyDataSetChanged();
                swipeMessageLayout.setRefreshing(false);
                smoothScroller.setTargetPosition(TOTAL_ITEMS_TO_LOAD - 2);
                layoutManager.startSmoothScroll(smoothScroller);
                if (!dataSnapshot.getKey().equals(firstMessageKey)) {
                    refreshEnabled = false;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged: TRIGGERED");
                Messages newMessage = dataSnapshot.getValue(Messages.class);
                if (newMessage != null && newMessage.getSeen().equals("true")) {
                    Log.i(TAG, "onChildChanged: CONDITION SATISFIED");
                    messagesList.clear();
                    loadMessages();
//                    Messages oldMessage = newMessage;
//                    newMessage.setSeen("false");
//                    int index = messagesList.indexOf(newMessage);
//                    if (index != -1) {
//                        messagesList.set(index, newMessage);
////                    messagesAdapter.notifyItemChanged(index);
//                    } else {
//                        Log.i(TAG, "onChildChanged: INDEX = -1 : " + s);
//                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        final DatabaseReference messageRef = rootRef.child("Messages").child(currentUserId).child(chatUserId);
        messageRef.keepSynced(true);
        final DatabaseReference senderMessageRef = rootRef.child("Messages").child(chatUserId).child(currentUserId);
        senderMessageRef.keepSynced(true);
        Query messageQuery = messageRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
        Query firstMessageQuery = messageRef.orderByKey().limitToFirst(1);

        firstMessageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firstMessageKey = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    lastMessageKey = dataSnapshot.getKey();
                }
                if (message != null && message.getFrom().equals(chatUserId)) {
                    message.setSeen("true");
                    messageRef.child(dataSnapshot.getKey()).child("seen").setValue("true");
                    senderMessageRef.child(dataSnapshot.getKey()).child("seen").setValue("true");
                }
                messagesList.add(message);
                messagesAdapter.notifyDataSetChanged();
                messagesListLayout.scrollToPosition(messagesList.size() - 1);
                swipeMessageLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged: TRIGGERED");
                Messages newMessage = dataSnapshot.getValue(Messages.class);
                if (newMessage != null && newMessage.getSeen().equals("true")) {
                    Log.i(TAG, "onChildChanged: CONDITION SATISFIED");
                    messagesList.clear();
                    loadMessages();
//                    Messages oldMessage = newMessage;
//                    newMessage.setSeen("false");
//                    int index = messagesList.indexOf(oldMessage);
//                    if (index != -1) {
//                        messagesList.set(index, newMessage);
////                    messagesAdapter.notifyItemChanged(index);
//                    } else {
//                        Log.i(TAG, "onChildChanged: INDEX = -1 : " + s);
//                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        etMessage.setText("");
        if (!TextUtils.isEmpty(message)) {

            DatabaseReference userMessageRef = rootRef.child("Messages")
                    .child(currentUserId).child(chatUserId).push();
            userMessageRef.keepSynced(true);
            String pushId = userMessageRef.getKey();
            String currentUserRef = "Messages/" + currentUserId + "/" + chatUserId;
            String chatUserRef = "Messages/" + chatUserId + "/" + currentUserId;

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", "false");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("type", "text");
            messageMap.put("from", currentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushId, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushId, messageMap);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.e(TAG, "onComplete: " + databaseError.getMessage());
                    }
                }
            });
        }
    }

    private void findViews() {
        ibChatAdd = (ImageButton) findViewById(R.id.ibChatAdd);
        ibChatSend = (ImageButton) findViewById(R.id.ibChatSend);
        etMessage = (EditText) findViewById(R.id.etMessage);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        messagesListLayout = (RecyclerView) findViewById(R.id.messagesList);
        layoutManager = new LinearLayoutManager(this);
        messagesAdapter = new MessagesAdapter(messagesList, getApplicationContext());
        messagesListLayout.setHasFixedSize(true);
        messagesListLayout.setLayoutManager(layoutManager);
        messagesListLayout.setAdapter(messagesAdapter);
        swipeMessageLayout = (SwipeRefreshLayout) findViewById(R.id.swipeMessageLayout);
        smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            private static final float MILLISECONDS_PER_INCH = 70f;

            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel
                    (DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUserRef.child("online").setValue("true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUserRef.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ACTIVITY FINISHED");
        chatUserId = null;
        finish();
    }
}