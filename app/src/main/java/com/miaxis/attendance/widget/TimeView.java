package com.miaxis.attendance.widget;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.miaxis.attendance.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author Tank
 * @date 2021/7/26 10:53 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class TimeView extends AppCompatTextView {

    //private String strDateFormat = "yyyy-MM-dd HH:mm:ss";
    private int format;//0 time    1 date   3 week
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat simpleWeekFormat = new SimpleDateFormat("EEEE");

    private BroadcastReceiver broadcastReceiver;

    public TimeView(@NonNull Context context) {
        super(context);
    }

    public TimeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("CustomViewStyleable")
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.time_view);
        this.format = array.getInt(R.styleable.time_view_time_format, 0);
        array.recycle();
    }

    public void setFormat(int format) {
        this.format = format;
        updateTime();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        }, filter);
        updateTime();
    }

    private void updateTime() {
        Date date = new Date();
        if (this.format == 0) {
            setText(this.simpleTimeFormat.format(date));
        } else if (this.format == 1) {
            setText(this.simpleDateFormat.format(date));
        } else {
            setText(this.simpleWeekFormat.format(date));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.broadcastReceiver != null) {
            getContext().unregisterReceiver(this.broadcastReceiver);
        }
    }

}
