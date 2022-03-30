package com.miaxis.bp_entry.view.face;

import android.graphics.Bitmap;

import com.miaxis.bp_entry.viewModel.BaseViewModel;

import androidx.lifecycle.MutableLiveData;

/**
 * @author ZJL
 * @date 2022/3/24 9:30
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FaceViewModel extends BaseViewModel {

    public MutableLiveData<Bitmap> imgres=new MutableLiveData<>();


    public void CheckFace(byte[] data){

    }
}
