package com.miaxis.attendance.ui.advertising;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * @author Tank
 * @date 2021/9/6 11:14 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AdvertisingPagerAdapter extends PagerAdapter {

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return false;
    }

}
