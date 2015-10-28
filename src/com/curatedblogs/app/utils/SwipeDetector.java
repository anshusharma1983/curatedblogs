package com.curatedblogs.app.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Anshu on 28/10/15.
 */

public class SwipeDetector extends GestureDetector.SimpleOnGestureListener implements
        View.OnTouchListener {

    private GestureDetector gDetector;
    private OnSwipeListener listener;

    public SwipeDetector(Context context, OnSwipeListener listener) {
        gDetector = new GestureDetector(context, this);
        this.listener = listener;
    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish,
                           float velocityX, float velocityY) {

        float gapX = start.getRawX() - finish.getRawX();
        float gapY = start.getRawY() - finish.getRawY();
        float distanceX = Math.abs(gapX);
        float distanceY = Math.abs(gapY);

        if (distanceY > distanceX) { // up downs
            if (gapY > 0) {
                // up
                listener.onSwipeUp(distanceY, velocityY);
            } else {
                // down
                listener.onSwipeDown(distanceY, velocityY);
            }
        } else { // left right
            if (gapX > 0) {
                // left
                listener.onSwipeLeft(distanceX, velocityX);
            } else {
                // rights
                listener.onSwipeRight(distanceX, velocityX);
            }
        }

        return super.onFling(start, finish, velocityX, velocityY);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gDetector.onTouchEvent(event);
    }

    public static interface OnSwipeListener {

        public void onSwipeUp(float distance, float velocity);

        public void onSwipeDown(float distance, float velocity);

        public void onSwipeLeft(float distance, float velocity);

        public void onSwipeRight(float distance, float velocity);

    }

}