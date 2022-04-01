package com.miaxis.attendance.ui.manager;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.miaxis.attendance.R;
import com.miaxis.attendance.databinding.HolderUserBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Tank
 * @date 2021/9/27 7:25 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<MxUser> mMxUsers = new ArrayList<>();

    public UserAdapter() {
    }

    private PageNotifyInterface pageNotifyInterface;

    public UserAdapter bind(PageNotifyInterface pageNotifyInterface) {
        this.pageNotifyInterface = pageNotifyInterface;
        return this;
    }

    public void setMxUsers(List<MxUser> list) {
        mMxUsers = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HolderUserBinding inflate = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.holder_user, parent, false);
        return new UserViewHolder(inflate.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(pageNotifyInterface).bind(mMxUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mMxUsers == null ? 0 : mMxUsers.size();
    }
}
