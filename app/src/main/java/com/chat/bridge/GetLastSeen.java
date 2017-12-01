package com.chat.bridge;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shaan on 01-12-2017.
 */

public class GetLastSeen {
    private static final String TAG = "GetLastSeen";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        final long diff = now - time;
        Log.i(TAG, "getTimeAgo: LAST SEEN  : " + time);
        Log.i(TAG, "getTimeAgo: CURRENT    : " + now);
        Log.i(TAG, "getTimeAgo: DIFFERENCE : " + diff);
        if (time > now || time <= 0) {
            return "just now";
        }

        // TODO: localize
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 59 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else if (diff < 7 * DAY_MILLIS) {
            return diff / DAY_MILLIS + " days ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, H:mm a");
            String localTime = sdf.format(new Date(time));
            return localTime;
        }
    }
}
