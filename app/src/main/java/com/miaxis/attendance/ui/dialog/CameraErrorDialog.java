package com.miaxis.attendance.ui.dialog;

import android.content.Context;

import com.miaxis.attendance.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

/**
 * @author Tank
 * @date 2021/8/27 7:43 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class CameraErrorDialog extends AlertDialog {


    protected CameraErrorDialog(@NonNull Context context) {
        super(context);


        setContentView(R.layout.dialog_camera_error);
    }
}



