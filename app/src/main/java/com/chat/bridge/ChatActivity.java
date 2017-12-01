package com.chat.bridge;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private String chatUserId, userName;
    private DatabaseReference rootRef;
    private DatabaseReference currentUserRef;

    private TextView tvDisplayName;
    private TextView tvLastSeen;
    private CircularImageView profileImage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUserId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");

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
