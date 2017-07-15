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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth mAuth;

    private Button bCreateAccount;
    private TextInputLayout labelEmail;
    private TextInputLayout labelDisplayName;
    private TextInputLayout labelPassword;
    private ProgressDialog progressDialog;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(getResources().getDrawable(R.mipmap.ic_app_padded));

        findViews();
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    private void findViews() {
        bCreateAccount = (Button) findViewById(R.id.bCreateAccount);
        labelEmail = (TextInputLayout) findViewById(R.id.labelEmail);
        labelDisplayName = (TextInputLayout) findViewById(R.id.labelDisplayName);
        labelPassword = (TextInputLayout) findViewById(R.id.labelPassword);

        bCreateAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bCreateAccount:
                boolean dataValid = true;
                String displayName = labelDisplayName.getEditText().getText().toString().trim();
                String email = labelEmail.getEditText().getText().toString().trim();
                String password = labelPassword.getEditText().getText().toString().trim();

                if (TextUtils.isEmpty(displayName)) {
                    labelDisplayName.setError("Mandatory field !!!");
                    dataValid = false;
                }
                if (TextUtils.isEmpty(password)) {
                    labelPassword.setError("Mandatory field !!!");
                    dataValid = false;
                }
                if (TextUtils.isEmpty(email)) {
                    labelEmail.setError("Mandatory field !!!");
                    dataValid = false;
                }

                if (dataValid) {
                    progressDialog.setIcon(getResources().getDrawable(R.drawable.ic_register));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("Just a sec... while we welcome u to the family...");
                    progressDialog.setTitle("Registering...");
                    progressDialog.show();
                    registerUser(displayName, email, password);
                }
                break;
        }
    }

    private void registerUser(final String displayName, final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Registration Successfull !!!");

                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put("name", displayName);
                            userMap.put("status", "Hey there! Im using this cool application for chatting purposes");
                            userMap.put("email", email);
                            userMap.put("image", "default");
                            userMap.put("thumbnail", "default");

                            userDatabase.setValue(userMap);

                            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                            progressDialog.dismiss();
                            startActivity(mainIntent);
                            finish();
                        } else {
                            Log.e(TAG, "onComplete: Registration Failed", task.getException());
                            progressDialog.dismiss();
//                            Toast.makeText(getApplicationContext(), "Registration Failed : " + task.getException().getMessage(),
//                                    Toast.LENGTH_LONG).show();
                            Snackbar.make(getCurrentFocus(), "Failed to Register : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }

                    }
                });
    }
}