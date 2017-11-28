package com.chat.bridge;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView usersList;
    private DatabaseReference usersDatabase;
    private FrameLayout profileContainer;
    private ProfileFragment profileFragment;
    private DatabaseReference currentUserRef;
    private FirebaseAuth mAuth;

    private static final String TAG = "UsersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());

        profileContainer = (FrameLayout) findViewById(R.id.profileContainer);
        usersList = (RecyclerView) findViewById(R.id.usersList);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));

        String userIdFromIntent = null;
        userIdFromIntent = getIntent().getStringExtra("userId");
        Log.e(TAG, "onCreate: fromUserId = " + userIdFromIntent);
        if (userIdFromIntent != null) {
            profileFragment = ProfileFragment.newInstance(userIdFromIntent);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.profileFragment, profileFragment)
                    .commit();
            profileContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUserRef.child("online").setValue("true");
        FirebaseRecyclerAdapter<Users, UsersViewHolder> usersAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_item_layout,
                UsersViewHolder.class,
                usersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, final int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumbnail(UsersActivity.this, model.getThumbnail());

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileFragment = ProfileFragment.newInstance(getRef(position).getKey());

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.profileFragment, profileFragment)
                                .commit();
                        profileContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
        };
        usersList.setAdapter(usersAdapter);
    }

    @Override
    public void onBackPressed() {
        if (profileContainer.getVisibility() == View.VISIBLE) {
            profileContainer.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().remove(profileFragment);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        currentUserRef.child("online").setValue("false");
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

    public static UsersActivity defaultInstance() {
        return new UsersActivity();
    }
}
