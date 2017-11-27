package com.chat.bridge;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Shaan on 28-11-2017.
 */

public class Chatofy extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Picasso offline capabilities
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        // Firebase offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
