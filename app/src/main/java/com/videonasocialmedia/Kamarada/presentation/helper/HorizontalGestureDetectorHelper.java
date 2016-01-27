/*
 * Copyright (c) 2016. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.Kamarada.presentation.helper;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.videonasocialmedia.Kamarada.presentation.listener.OnSwipeListener;

public class HorizontalGestureDetectorHelper implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private OnSwipeListener swipeListener;

    public HorizontalGestureDetectorHelper(Context ctx, OnSwipeListener swipeListener) {

        this.swipeListener = swipeListener;
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    public GestureDetector getGestureDetector(){
        return  gestureDetector;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void onSwipeRight() {
        Log.d("SwipeListener", "Right");
        swipeListener.onSwipeRight();
    }

    public void onSwipeLeft() {
        Log.d("SwipeListener", "Left");
        swipeListener.onSwipeLeft();
    }

    public void onSwipeTop() {
        Log.d("SwipeListener", "Top");
    }

    public void onSwipeBottom() {
        Log.d("SwipeListener", "Bottom");
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeLeft();
                        } else {
                            onSwipeRight();
                        }
                    }
                    result = true;
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

    }
}