package com.dishtech.vgg.ui.gestures;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

public class SwipeRecognizer implements View.OnTouchListener {

    private static final String TAG = SwipeRecognizer.class.getName();

    private final GestureDetector gestureDetector;
    private final WeakReference<GestureDelegate> delegate;

    public SwipeRecognizer(Context ctx, WeakReference<GestureDelegate> delegate) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.delegate = delegate;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return gestureDetector.onTouchEvent(event);
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
                float[] params = {diffX, diffY, velocityX, velocityY};
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) >
                            SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(params);
                        } else {
                            onSwipeLeft(params);
                        }
                        result = true;
                    }
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) >
                        SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom(params);
                    } else {
                        onSwipeTop(params);
                    }
                    result = true;
                }
            } catch (Exception exception) {
                Log.d(TAG, "Unexpected problem handling swipes", exception);
                throw exception;
            }
            return result;
        }
    }

    public void onSwipeRight(float[] params) {
        delegate.get().onGesture(new Gesture(Gestures.SWIPE_RIGHT, params));
    }

    public void onSwipeLeft(float[] params) {
        delegate.get().onGesture(new Gesture(Gestures.SWIPE_LEFT, params));
    }
    public void onSwipeBottom(float[] params) {
        delegate.get().onGesture(new Gesture(Gestures.SWIPE_DOWN, params));
    }
    public void onSwipeTop(float[] params) {
        delegate.get().onGesture(new Gesture(Gestures.SWIPE_UP, params));
    }
}