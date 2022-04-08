package com.miaxis.attendance.ui.finger;

import android.graphics.Bitmap;

import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.bean.AttendanceBean;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Staff;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.data.model.StaffModel;
import com.miaxis.common.response.ZZResponse;
import com.miaxis.common.utils.FileUtils;
import com.mx.finger.common.MxImage;
import com.mx.finger.utils.RawBitmapUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import timber.log.Timber;

public class FingerViewModel extends ViewModel implements MR990FingerStrategy.ReadFingerCallBack {

    MutableLiveData<Boolean> StartCountdown = new MutableLiveData<>(true);
    MutableLiveData<ZZResponse<AttendanceBean>> mAttendance = new MutableLiveData<>();

    public FingerViewModel() {
    }

    public void readFinger() {
        MR990FingerStrategy.getInstance().readFinger(this);
    }

    public void stopRead() {
        MR990FingerStrategy.getInstance().stopRead();
    }

    public void resume() {
        MR990FingerStrategy.getInstance().resume();
        this.StartCountdown.setValue(true);
    }

    public void pause() {
        MR990FingerStrategy.getInstance().pause();
        this.StartCountdown.setValue(false);
    }

    @Override
    public void onReadFinger(MxImage finger) {
        this.StartCountdown.postValue(true);
    }

    @Override
    public void onExtractFeature(MxImage image, byte[] feature) {
    }

    private String lastUserID;
    private String lastPlaceID;
    private long lastTime;

    @Override
    public void onFeatureMatch(MxImage image, byte[] feature, Staff st, Bitmap bitmap) {
        String capturePath = null;
        String UserId = null;
        Staff staff = null;
        if (st != null) {
            if (lastUserID != null && lastUserID.equals(st.getCode()) &&lastPlaceID.equals(st.getPlace())&& (System.currentTimeMillis() - lastTime) <= AppConfig.verifyTimeOut) {
                this.mAttendance.postValue(ZZResponse.CreateFail(-204, "重复识别"));
                return;
            }
            lastUserID=st.getCode();
            lastPlaceID=st.getPlace();
            staff= StaffModel.findStaffByCode(st.getCode(),st.getPlace());
            if (staff != null) {
                capturePath = AppConfig.Path_CaptureImage + "finger" + "_" + staff.getCode() +"place"+staff.getPlace()+ "_" + System.currentTimeMillis() + ".bmp";
            }
        }
        lastTime = System.currentTimeMillis();
        if (capturePath == null) {
            capturePath = AppConfig.Path_CaptureImage + "finger" + "_temp_" + System.currentTimeMillis() + ".bmp";
        }
        FileUtils.initFile(capturePath);
        int saveBMP = RawBitmapUtils.saveBMP(capturePath, image.data, image.width, image.height);
        Timber.e("saveBMP:%s", saveBMP);
        if (saveBMP != 0) {
            return;
        }

        LocalImage captureLocalImage = new LocalImage();
        captureLocalImage.LocalPath = capturePath;
        captureLocalImage.id = LocalImageModel.insert(captureLocalImage);
        if (captureLocalImage.id <= 0) {
            return;
        }


        AttendanceBean attendanceBean = new AttendanceBean();
        attendanceBean.Status = staff == null ? 2 : 1;
        attendanceBean.Mode = 2;
        attendanceBean.Code = staff == null ? null : staff.getCode();
        attendanceBean.Place=staff==null?null:staff.getPlace();
        attendanceBean.CaptureImage = capturePath;
        attendanceBean.CutImage = capturePath;
        attendanceBean.UserName = staff == null ? null : staff.getCode();
        attendanceBean.tempType=1;
        if (staff == null) {
            this.mAttendance.postValue(ZZResponse.CreateFail(-203, "人员未找到"));
        } else {
            this.mAttendance.postValue(ZZResponse.CreateSuccess(attendanceBean));
        }
    }

}