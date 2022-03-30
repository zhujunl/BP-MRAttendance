package com.miaxis.bp_entry.view.base;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected Context context;
    protected List<T> dataList = new ArrayList<>();

    public BaseAdapter(@NonNull Context context) {
        this.context = context;
    }

    public T getData(int position) {
        return dataList.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDataList(List<T> dataList) {
        this.dataList.clear();
        if (dataList!=null&&!dataList.isEmpty()){
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void appendDataList(List<T> appendDataList) {
        this.dataList.addAll(appendDataList);
    }

}
