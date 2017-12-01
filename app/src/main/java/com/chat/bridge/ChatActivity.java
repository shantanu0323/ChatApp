package com.chat.bridge;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

        ibChatAdd = (ImageButton) findViewById(R.id.ibChatAdd);
        ibChatSend = (ImageButton) findViewById(R.id.ibChatSend);
        etMessage = (EditText) findViewById(R.id.etMessage);

        ibChatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle(userName);
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

        rootRef = FirebaseDatabase.getInstance().getReference();

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
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        etMessage.setText("");
        if (!TextUtils.isEmpty(message)) {

            DatabaseReference userMessageRef = rootRef.child("Messages")
                    .child(currentUserId).child(chatUserId).push();
            String pushId = userMessageRef.getKey();
            String currentUserRef = "Messages/" + currentUserId + "/" + chatUserId;
            String chatUserRef = "Messages/" + chatUserId + "/" + currentUserId;

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", "false");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("type", "text");

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

}
