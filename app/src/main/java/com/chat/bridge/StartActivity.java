package com.chat.bridge;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class StartActivity extends AppCompatActivity {

    private Button bRegister;
    private Button bLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        bRegister = (Button) findViewById(R.id.bRegister);
        bLogin = (Button) findViewById(R.id.bLogin);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        FadeView fadeView = (FadeView) findViewById(R.id.fadeView);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_start_original);

        Drawable d1 = getResources().getDrawable(R.drawable.bg_start); //new BitmapDrawable(getResources(), blurredBitmap);
        Drawable d2 = getResources().getDrawable(R.drawable.bg_start_original);

//        fadeView.mFirst.setImageDrawable(d2);
        fadeView.ShowImage(d1);
        Animation containerAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.container_animation);
        ConstraintLayout container = (ConstraintLayout) findViewById(R.id.container);
        container.startAnimation(containerAnimation);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}

