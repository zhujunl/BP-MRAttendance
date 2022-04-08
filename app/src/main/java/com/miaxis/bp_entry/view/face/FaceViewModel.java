package com.miaxis.bp_entry.view.face;

import android.util.Log;

import com.miaxis.bp_entry.bridge.SingleLiveEvent;
import com.miaxis.bp_entry.manager.CameraManager;
import com.miaxis.bp_entry.manager.FaceManager;
import com.miaxis.bp_entry.util.ValueUtil;
import com.miaxis.bp_entry.viewModel.BaseViewModel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

/**
 * @author ZJL
 * @date 2022/3/24 9:30
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FaceViewModel extends BaseViewModel {
    public ObservableField<String> hint = new ObservableField<>("");
    public MutableLiveData<Boolean> verifyFailedFlag = new SingleLiveEvent<>();


    private byte[] cardFeature;
    public FaceViewModel() {
    }

    public void startFaceVerify(byte[] featureCache) {
        cardFeature=null;
        cardFeature=featureCache;
        FaceManager.getInstance().setFeatureListener(faceListener);
        FaceManager.getInstance().setNeedNextFeature(true);
        FaceManager.getInstance().setOrientation(CameraManager.getInstance().getPreviewOrientation());
        FaceManager.getInstance().startLoop();
        hint.set("请将镜头朝向查询的人员");

    }

    public void stopFaceVerify() {
        FaceManager.getInstance().stopLoop();
        FaceManager.getInstance().setFeatureListener(null);
        CameraManager.getInstance().closeFrontCamera();
    }

    private FaceManager.OnFeatureExtractListener faceListener = (mxRGBImage, mxFaceInfoEx, feature, mask) -> {
        try {
            float score;
            if (mask) {
                score = FaceManager.getInstance().matchMaskFeature(feature, cardFeature);
            } else {
                score = FaceManager.getInstance().matchFeature(feature, cardFeature);
            }
            Log.e("faceListener:","mask="+mask+"-----------------score="+score);
            int verify;
            if (mask ? score >= ValueUtil.DEFAULT_MASK_VERIFY_SCORE : score >= ValueUtil.DEFAULT_VERIFY_SCORE) {
                verify = 1;
                verifyFailedFlag.postValue(Boolean.TRUE);
                hint.set("人脸核验成功");
                stopFaceVerify();
            } else {
                verify = 2;
                Log.e("比对", "比对失败: "+score);
                verifyFailedFlag.postValue(Boolean.FALSE);
                hint.set("识别不通过");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FaceManager.getInstance().setNeedNextFeature(true);

    };


}
