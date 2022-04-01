package com.miaxis.attendance.ui.bar.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Tank
 * @date 2021/9/27 4:25 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class ClickableLayout extends FrameLayout {


    public ClickableLayout(@NonNull Context context) {
        super(context);
    }

    public ClickableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClickableLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract static class OnComboClickListener implements OnClickListener {
        private final String TAG = "OnComboClickListener";
        private final ArrayList<Long> clickCounts = new ArrayList<>();

        @Override
        public void onClick(View v) {
            long timeMillis = System.currentTimeMillis();
            if (clickCounts.isEmpty()) {
                clickCounts.add(timeMillis);
            } else {
                long last = clickCounts.get(clickCounts.size() - 1);
                if (Math.abs(timeMillis - last) >= 1000) {
                    clickCounts.clear();
                } else {
                    clickCounts.add(timeMillis);
                }
            }
            if (clickCounts.size() >= 6) {
                clickCounts.clear();
                onComboClick(v);
            }
        }

        protected abstract void onComboClick(View v);
    }

}
