package com.miaxis.attendance.ui.manager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.miaxis.attendance.R;
import com.miaxis.common.utils.ListUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Tank
 * @date 2021/9/27 7:24 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "UserViewHolder";

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    private PageNotifyInterface pageNotifyInterface;

    public UserViewHolder bind(PageNotifyInterface pageNotifyInterface) {
        this.pageNotifyInterface = pageNotifyInterface;
        return this;
    }

    public void bind(MxUser mxUser) {
        Log.e(TAG, "bind: " + mxUser);
        if (mxUser == null) {
            return;
        }
        TextView tv_name = itemView.findViewById(R.id.tv_name);
        tv_name.setText(String.valueOf(mxUser.name));
        TextView tv_work = itemView.findViewById(R.id.tv_work);
        tv_work.setText(String.valueOf(mxUser.number));

        ImageView iv_face = itemView.findViewById(R.id.iv_face);
        Glide.with(iv_face).load(mxUser.face).error(R.drawable.logo).into(iv_face);

        List<String> fingers = mxUser.fingers;
        LinearLayout ll_fingers = itemView.findViewById(R.id.ll_fingers);
        ll_fingers.removeAllViews();
        for (int i = 0; i < 2; i++) {
            View view = LayoutInflater.from(ll_fingers.getContext())
                    .inflate(R.layout.view_finger, ll_fingers, false);
            TextView tv_finger = view.findViewById(R.id.tv_finger);
            tv_finger.setText(tv_finger.getResources().getString(R.string.text_finger) + "" + (i + 1));
            view.findViewById(R.id.iv_finger).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FingerCaptureDialog(v.getContext(), mxUser).bind(pageNotifyInterface).show();
                }
            });
            ll_fingers.addView(view);
        }


        if (!ListUtils.isNullOrEmpty(fingers)) {
            for (int i = 0; i < fingers.size() && i < ll_fingers.getChildCount(); i++) {
                View childAt = ll_fingers.getChildAt(i);
                String path = fingers.get(i);
                ImageView iv_finger = childAt.findViewById(R.id.iv_finger);
                Glide.with(iv_finger).load(TextUtils.isEmpty(path) ? R.drawable.ic_baseline_fingerprint_24 : path)
                        .error(R.drawable.ic_baseline_fingerprint_24).into(iv_finger);
            }
        }
        //        ImageView finger = (ImageView) LayoutInflater.from(ll_fingers.getContext())
        //                .inflate(R.layout.view_finger, ll_fingers, false);
        //        ll_fingers.addView(finger);
        //        finger.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                new FingerCaptureDialog(v.getContext(), mxUser).bind(pageNotifyInterface).show();
        //                //RecyclerView.Adapter<? extends RecyclerView.ViewHolder> bindingAdapter = getBindingAdapter();
        //            }
        //        });
    }


}
