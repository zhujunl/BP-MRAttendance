package com.miaxis.attendance.task;

import android.os.SystemClock;

import com.miaxis.attendance.api.HttpApi;
import com.miaxis.attendance.api.HttpResponse;
import com.miaxis.attendance.data.entity.Attendance;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.model.AttendanceModel;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.common.utils.DateUtil;
import com.miaxis.common.utils.ListUtils;
import com.miaxis.common.utils.StringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

/**
 * @author Tank
 * @date 2021/8/26 9:44 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UploadAttendance implements Runnable {

    private static final String TAG = "UploadAttendance";
    private boolean isRunning;

    public UploadAttendance() {
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void run() {
        this.isRunning = true;
        while (true) {
            SystemClock.sleep(3000);
            if (!this.isRunning) {
                return;
            }
            try {
                List<Attendance> noUpload = AttendanceModel.findNoUpload();
                if (!ListUtils.isNullOrEmpty(noUpload)) {
                    Attendance attendance = noUpload.get(0);
                    String uploadImagePath = null;
                    long captureImage = attendance.CaptureImage;
                    List<LocalImage> localImages = LocalImageModel.findByID(captureImage);
                    if (!ListUtils.isNullOrEmpty(localImages)) {
                        LocalImage localImage = localImages.get(0);
                        if (StringUtils.isNullOrEmpty(localImage.RemotePath)) {
                            File file = new File(localImage.LocalPath);
                            Response<HttpResponse<String>> execute = HttpApi.uploadImage(file).execute();
                            //Timber.d(TAG, "uploadImage:" + execute);
                            HttpResponse<String> body = execute.body();
                            //Timber.d(TAG, "uploadImage   body:" + body);
                            if (body.isSuccess()) {
                                uploadImagePath = body.result;
                                localImage.RemotePath = uploadImagePath;
                                LocalImageModel.update(localImage);
                                boolean delete = file.delete();
                            }
                        } else {
                            uploadImagePath = localImage.RemotePath;
                        }
                    }
                    if (StringUtils.isNullOrEmpty(uploadImagePath)) {
                        throw new IllegalArgumentException("上传图片路径不能为空");
                    }
                    Call<HttpResponse<Object>> uploadAttendance = HttpApi.uploadAttendance(
                            attendance.UserId == null ? 0 : Integer.parseInt(attendance.UserId),
                            attendance.Status == 1 ? 0 : 1,
                            0,
                            //HardWareUtils.getDeviceId(App.getInstance()),
                            DateUtil.DATE_FORMAT.format(new Date(attendance.create_time)),
                            "入口",
                            attendance.Mode == 1 ? 0 : 1,
                            uploadImagePath);
                    Response<HttpResponse<Object>> response = uploadAttendance.execute();
                    Timber.e("uploadAttendance:" + response);
                    HttpResponse<Object> httpResponse = response.body();
                    Timber.e("uploadAttendance   body:" + httpResponse);
                    if (httpResponse.code.equals("200")) {
                        attendance.Upload = 1;
                        AttendanceModel.update(attendance);
                    }
                } else {
                    Timber.d("no attendance");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e("UploadAttendance Exception:" + e);
            }
        }
    }
}
