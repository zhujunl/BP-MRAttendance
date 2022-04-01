package com.miaxis.attendance.ui.advertising;

import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.R;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Tank
 * @date 2021/8/25 2:45 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AdvertisingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private AdvertisingClickListener mClickListener;
    private Advertising mAdvertising;

    public AdvertisingHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    public void bind(AdvertisingClickListener advertisingClickListener) {
        this.mClickListener = advertisingClickListener;
    }

    public void bind(Advertising advertising) {
        this.mAdvertising = advertising;
        AppCompatImageView aiv_image = itemView.findViewById(R.id.aiv_image);
        Glide.with(aiv_image).load(advertising.imageId).error(R.mipmap.ic_launcher).into(aiv_image);

        Advertising.MxRect rect = this.mAdvertising.rect;

        AppCompatImageView aiv_logo = itemView.findViewById(R.id.aiv_logo);
        aiv_logo.setX(rect.left);
        aiv_logo.setY(rect.top);
        ViewGroup.LayoutParams layoutParams = aiv_logo.getLayoutParams();
        layoutParams.width=rect.width();
        layoutParams.height=rect.height();
        aiv_logo.setLayoutParams(layoutParams);
        Glide.with(aiv_logo).load(advertising.logoId).error(R.mipmap.ic_launcher).into(aiv_logo);
    }

    @Override
    public void onClick(View v) {
        if (this.mClickListener != null) {
            this.mClickListener.onAdvertisingClick(this.mAdvertising);
        }
    }
}
