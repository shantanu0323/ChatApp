package com.chat.bridge;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by SHAAN on 14-07-17.
 */

public class FadeView extends FrameLayout {
    private long mFadeDelay = 1500;
    private ImageView mFirst;
    private ImageView mSecond;
    private boolean mFirstShowing;

    public FadeView(Context context) {
        super(context);
        init(context);
    }

    public FadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FadeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context c){
        mFirst = new ImageView(c);
        mSecond = new ImageView(c);

        mFirst.setAlpha(1.0f);
        mSecond.setAlpha(0.0f);

        mFirstShowing = true;

        addView(mFirst);
        addView(mSecond);
    }

    public void setFadeDelay(long fadeDelay) {
        mFadeDelay = fadeDelay;
    }

    public void ShowImage(Drawable d){
        if(mFirstShowing){
            mSecond.setImageDrawable(d);
            mSecond.animate().alpha(1.0f).setDuration(mFadeDelay);
            mFirst.animate().alpha(0.0f).setDuration(mFadeDelay);
        }else {
            mFirst.setImageDrawable(d);
            mSecond.animate().alpha(0.0f).setDuration(mFadeDelay);
            mFirst.animate().alpha(1.0f).setDuration(mFadeDelay);
        }

        mFirstShowing = !mFirstShowing;
    }
}