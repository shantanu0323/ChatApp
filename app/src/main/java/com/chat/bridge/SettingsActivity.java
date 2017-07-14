package com.chat.bridge;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private CircularImageView profilepic;
    private EditText etDisplayName;
    private EditText etStatus;
    private TextView tvEmail;
    private FloatingActionButton bProfilepic;
    private Button bEdit;

    private void findViews() {
        profilepic = (CircularImageView) findViewById(R.id.profilepic);
        etDisplayName = (EditText) findViewById(R.id.etDisplayName);
        etStatus = (EditText) findViewById(R.id.etStatus);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        bProfilepic = (FloatingActionButton) findViewById(R.id.bProfilepic);
        bEdit = (Button) findViewById(R.id.bEdit);

        bProfilepic.setOnClickListener(this);
        bEdit.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        findViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bProfilepic:

                break;
            case R.id.bEdit:

                break;
        }
    }

}
