package com.miaxis.attendance.ui.advertising;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.miaxis.attendance.R;
import com.miaxis.common.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Tank
 * @date 2021/8/25 2:31 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class AdvertisingAdapter extends RecyclerView.Adapter<AdvertisingHolder> {

    private final List<Advertising> list = new ArrayList<>();
    private final AdvertisingClickListener mClickListener;

    public AdvertisingAdapter(AdvertisingClickListener advertisingClickListener) {
        this.mClickListener = advertisingClickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addAll(List<Advertising> list) {
        this.list.clear();
        if (!ListUtils.isNullOrEmpty(list)) {
            this.list.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdvertisingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdvertisingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advertising, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdvertisingHolder holder, int position) {
        holder.bind(this.mClickListener);
        holder.bind(list.get(position % list.size()));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

}



