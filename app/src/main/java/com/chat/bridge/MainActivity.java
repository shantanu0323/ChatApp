package com.chat.bridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private TabLayout tabLayout;

    private DatabaseReference currentUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_app_padded));

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    currentUserRef.child("online").setValue("true");
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    redirectToStartActivity();
                }
                // ...
            }
        };

        try {
            currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
        } catch (Exception e) {
            Log.e(TAG, "onCreate: NO USER LOGGED IN : ", e);
        }

        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void redirectToStartActivity() {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: CALLED");
        try {
            currentUserRef.child("online").setValue("true");
        } catch (Exception e) {
            Log.e(TAG, "onStart: NO USER LOGGED IN : " + e.getMessage());
        }
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: CALLED");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                mAuth.signOut();
                redirectToStartActivity();
                break;
            case R.id.action_account_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), AccountSettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_all_users:
                Intent allUserIntent = new Intent(getApplicationContext(), UsersActivity.class);
                startActivity(allUserIntent);
                break;
        }

        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart: CALLED");
        try {
            currentUserRef.child("online").setValue("true");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: NO USER LOGGED IN : ", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: CALLED");
        try {
            currentUserRef.child("online").setValue("true");
        } catch (Exception e) {
            Log.e(TAG, "onCreate: NO USER LOGGED IN : ", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: CALLED");
        try {
            currentUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        } catch (Exception e) {
            Log.e(TAG, "onPause: NO USER HAS LOGGED IN : " + e.getMessage() );
        }
    }
}
