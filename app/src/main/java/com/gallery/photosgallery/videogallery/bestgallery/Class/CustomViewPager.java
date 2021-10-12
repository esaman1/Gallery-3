package com.gallery.photosgallery.videogallery.bestgallery.Class;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

/**
 * This Class is used to disable swiping motion as  per google guide lines there is not swiping motion
 * @author Waleed Sarwar
 * @since September 28, 2016 10:47 AM
 */
public class CustomViewPager extends ViewPager {
    private boolean enabled;

    public CustomViewPager(Context context,AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled)
            return super.onTouchEvent(event);
        else
            return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return enabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPagingEnabled() {
        return enabled;
    }


    public void setDurationScroll(int millis) {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new OwnScroller(getContext(), millis));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class OwnScroller extends Scroller{

        private int durationScrollMillis = 300;

        public OwnScroller(Context context,int durationScroll) {
            super(context, new DecelerateInterpolator());
            this.durationScrollMillis = durationScroll;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, durationScrollMillis);
        }

    }
}
