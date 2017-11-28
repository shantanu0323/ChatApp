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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private TextInputLayout labelPassword;
    private TextInputLayout labelEmail;
    private Button bLogin;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference mUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_app_padded));

        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        findViews();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                boolean dataValid = true;
                String email = labelEmail.getEditText().getText().toString().trim();
                String password = labelPassword.getEditText().getText().toString().trim();

                if (TextUtils.isEmpty(password)) {
                    labelPassword.setError("Mandatory field !!!");
                    dataValid = false;
                }
                if (TextUtils.isEmpty(email)) {
                    labelEmail.setError("Mandatory field !!!");
                    dataValid = false;
                }

                if (dataValid) {
                    progressDialog.setIcon(getResources().getDrawable(R.drawable.ic_login));
                    progressDialog.setMessage("Just a sec... while we prepare your account...");
                    progressDialog.setTitle("Logging you in...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    loginUser(email, password);
                }
                break;
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Login Successfull !!!");

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            mUsers.child(currentUserId).child("deviceToken").setValue(deviceToken)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    });

                        } else {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
//                            Toast.makeText(getApplicationContext(), "Failed to Sign In : " + task.getException().getMessage(),
//                                    Toast.LENGTH_LONG).show();
                            Snackbar.make(getCurrentFocus(), "Failed to Login : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void findViews() {
        labelPassword = (TextInputLayout) findViewById(R.id.labelPassword);
        labelEmail = (TextInputLayout) findViewById(R.id.labelEmail);
        bLogin = (Button) findViewById(R.id.bLogin);

        bLogin.setOnClickListener(this);
    }


}
