package com.miaxis.attendance.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/9/6 11:04 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AutoViewPager extends ViewPager {

    private static final String TAG = "AutoViewPager";

    public AutoViewPager(@NonNull Context context) {
        super(context);
    }

    public AutoViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Timber.d( "dispatchTouchEvent: "+event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Timber.d("onInterceptTouchEvent: "+event);
        return super.onInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Timber.d("onTouchEvent: "+event);
        return super.onTouchEvent(event);
    }


}
