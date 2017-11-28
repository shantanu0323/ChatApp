package com.chat.bridge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditActivity extends AppCompatActivity {

    private TextInputLayout labelDisplayName;
    private TextInputLayout labelStatus;
    private FloatingActionButton save;
    private ProgressDialog progressDialog;
    private DatabaseReference currentUserRef;
    private FirebaseAuth mAuth;

    private void findViews() {
        labelDisplayName = (TextInputLayout) findViewById(R.id.labelDisplayName);
        labelStatus = (TextInputLayout) findViewById(R.id.labelStatus);
        save = (FloatingActionButton) findViewById(R.id.save);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViews();
        labelDisplayName.getEditText().setText(getIntent().getStringExtra("name"));
        labelStatus.getEditText().setText(getIntent().getStringExtra("status"));

        mAuth = FirebaseAuth.getInstance();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = labelDisplayName.getEditText().getText().toString().trim();
                String status = labelStatus.getEditText().getText().toString().trim();
                boolean dataValid = true;
                if (TextUtils.isEmpty(displayName)) {
                    dataValid = false;
                    labelDisplayName.setError("Mandatory Field");
                }
                if (TextUtils.isEmpty(status)) {
                    dataValid = false;
                    labelStatus.setError("Mandatory Field");
                }

                if (dataValid) {
                    progressDialog.setTitle("Updating...");
                    progressDialog.setMessage("Just a sec while we update your profile...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    try {
                        progressDialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final boolean[] success = {true};
                    DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                    currentUser.child("name").setValue(displayName)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            try {
                                progressDialog.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            success[0] = false;
                            Toast.makeText(EditActivity.this, "Sorry!!! couldn't update your profile : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    currentUser.child("status").setValue(status)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (progressDialog.isShowing()) {
                                        try {
                                            progressDialog.dismiss();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    success[0] = false;
                                    Toast.makeText(EditActivity.this, "Sorry!!! couldn't update your profile : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    if (success[0]) {
                        finish();
                    }
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_cancel:
                finish();
                break;
        }

        return true;
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
    protected void onStop() {
        super.onStop();
        currentUserRef.child("online").setValue("false");
    }

}
