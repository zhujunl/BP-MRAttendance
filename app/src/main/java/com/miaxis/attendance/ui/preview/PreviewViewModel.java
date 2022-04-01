package com.miaxis.attendance.ui.preview;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.miaxis.attendance.App;
import com.miaxis.attendance.config.AppConfig;
import com.miaxis.attendance.data.bean.AttendanceBean;
import com.miaxis.attendance.data.entity.Attendance;
import com.miaxis.attendance.data.entity.Face;
import com.miaxis.attendance.data.entity.LocalImage;
import com.miaxis.attendance.data.entity.Person;
import com.miaxis.attendance.data.model.AttendanceModel;
import com.miaxis.attendance.data.model.FaceModel;
import com.miaxis.attendance.data.model.LocalImageModel;
import com.miaxis.attendance.data.model.PersonModel;
import com.miaxis.attendance.device.MR990Device;
import com.miaxis.common.camera.CameraConfig;
import com.miaxis.common.camera.CameraHelper;
import com.miaxis.common.camera.CameraPreviewCallback;
import com.miaxis.common.camera.MXCamera;
import com.miaxis.common.camera.MXFrame;
import com.miaxis.common.response.ZZResponse;
import com.miaxis.common.response.ZZResponseCode;
import com.miaxis.common.utils.ListUtils;
import com.miaxis.common.utils.MapUtils;

import org.zz.api.MXFace;
import org.zz.api.MXFaceIdAPI;
import org.zz.api.MXImageToolsAPI;
import org.zz.api.MXResult;
import org.zz.api.MxImage;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class PreviewViewModel extends ViewModel implements CameraPreviewCallback {

    private static final String TAG = "PreviewViewModel";
    private Handler mHandler = new Handler();

    /**
     * 人脸框
     */
    MutableLiveData<RectF> faceRect = new MutableLiveData<>();
    /**
     * 摄像头是否可用
     */
    MutableLiveData<ZZResponse<?>> IsCameraEnable_Rgb = new MutableLiveData<>();
    MutableLiveData<ZZResponse<?>> IsCameraEnable_Nir = new MutableLiveData<>();

    /**
     * 近红外预览区域
     */
    AtomicReference<SurfaceHolder> SurfaceHolder_Nir = new AtomicReference<>();
    /**
     * Nir视频帧是否正在处理
     */
    AtomicBoolean IsNirFrameProcessing = new AtomicBoolean(false);
    /**
     * 是否启用近红外帧
     */
    AtomicBoolean IsNirEnable = new AtomicBoolean(true);


    /**
     * 人脸帧数据缓存
     */
    AtomicReference<Map.Entry<MxImage, MXFace>> CurrentMxImage_Rgb = new AtomicReference<>();
    AtomicReference<Map.Entry<MxImage, MXFace>> CurrentMxImage_Nir = new AtomicReference<>();

    //MutableLiveData<Boolean> HaveFace = new MutableLiveData<>(false);

    MutableLiveData<Boolean> StartCountdown = new MutableLiveData<>(true);

    MutableLiveData<ZZResponse<AttendanceBean>> AttendanceBean = new MutableLiveData<>();

    public PreviewViewModel() {
    }

    /*补光灯控制*/
    public void Fill_light(boolean enable){
        MR990Device.getInstance().LedPower(enable);
    }
    /**
     * 处理可见光视频帧数据
     */
    private synchronized void Process_Rgb(MXFrame frame) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<RectF>>) emitter -> {
            if (!MXFrame.isBufferEmpty(frame) && MXFrame.isSizeLegal(frame)) {
                MXResult<byte[]> mxResult = MXImageToolsAPI.getInstance().YUV2RGB(frame.buffer, frame.width, frame.height);//MR90 10ms
                if (MXResult.isSuccess(mxResult)) {
                    MXResult<MxImage> imageRotate = MXImageToolsAPI.getInstance().ImageRotate(
                            new MxImage(frame.width, frame.height, mxResult.getData()),
                            CameraConfig.Camera_RGB.bufferOrientation);//MR90 15ms
                    if (MXResult.isSuccess(imageRotate)) {
                        MxImage mxImage = imageRotate.getData();
                        MXResult<List<MXFace>> detectFace = MXFaceIdAPI.getInstance().mxDetectFace(
                                mxImage.buffer, mxImage.width, mxImage.height);//MR90 40--100ms
                        if (MXResult.isSuccess(detectFace)) {
                            List<RectF> list = new ArrayList<>();
                            List<MXFace> data = detectFace.getData();
                            for (MXFace mxFace : data) {
                                list.add(mxFace.getFaceRectF());
                            }
                            if (!ListUtils.isNullOrEmpty(data)) {
                                this.StartCountdown.postValue(true);
                            }
                            boolean b = this.CurrentMxImage_Rgb.compareAndSet(null, new AbstractMap.SimpleEntry<>(mxImage, MXFaceIdAPI.getInstance().getMaxFace(data)));
                            emitter.onNext(list);
                            return;
                        }
                    }
                }
            }
            this.CurrentMxImage_Rgb.set(null);
            emitter.onNext(new ArrayList<>());
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (ListUtils.isNullOrEmpty(list)) {
                        this.faceRect.postValue(null);
                    } else {
                        boolean isNirEnable = this.IsNirEnable.get();
                        if (isNirEnable) {
                            this.IsNirEnable.set(false);
                            startNirFrame();
                        }
                        this.faceRect.postValue(list.get(0));
                    }
                    startRgbFrame();
                }, throwable -> {
                   this.CurrentMxImage_Rgb.set(null);
                    this.faceRect.postValue(null);
                    startRgbFrame();
                });
    }

    /**
     * 开启可见光视频帧
     */
    private void startRgbFrame() {
        ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_RGB);
        if (ZZResponse.isSuccess(mxCamera)) {
            int enable = mxCamera.getData().setNextFrameEnable();
            this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateSuccess());
        } else {
            this.StartCountdown.postValue(false);
            this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateFail(mxCamera.getCode(), mxCamera.getMsg()));
        }
    }

    /**
     * 开启近红外视频帧
     * 主线程
     */
    private void startNirFrame() {
        if (!this.IsNirFrameProcessing.get()) {
            ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_NIR);
            if (ZZResponse.isSuccess(mxCamera)) {
                int enable = mxCamera.getData().setNextFrameEnable();
                this.IsNirFrameProcessing.set(true);
                this.IsCameraEnable_Nir.setValue(ZZResponse.CreateSuccess());
            } else {
                this.IsNirEnable.set(true);
                this.StartCountdown.postValue(false);
                this.IsCameraEnable_Nir.setValue(ZZResponse.CreateFail(mxCamera.getCode(), mxCamera.getMsg()));
            }
        }
    }

    /**
     * 处理近红外视频帧数据
     */
    private void Process_Nir(MXFrame frame) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<RectF>>) emitter -> {
            if (!MXFrame.isBufferEmpty(frame) && MXFrame.isSizeLegal(frame)) {
                //SystemClock.sleep(250);
                MXResult<byte[]> mxResult = MXImageToolsAPI.getInstance().YUV2RGB(frame.buffer, frame.width, frame.height);//MR90 10ms
                if (MXResult.isSuccess(mxResult)) {
                    MXResult<MxImage> imageRotate = MXImageToolsAPI.getInstance().ImageRotate(
                            new MxImage(frame.width, frame.height, mxResult.getData()), CameraConfig.Camera_NIR.bufferOrientation);//MR90 15ms
                    if (MXResult.isSuccess(imageRotate)) {
                        MxImage mxImage = imageRotate.getData();
                        MXResult<List<MXFace>> detectFace = MXFaceIdAPI.getInstance().mxDetectFaceNir(
                                mxImage.buffer, mxImage.width, mxImage.height);//MR90 40--100ms
                        if (MXResult.isSuccess(detectFace)) {
                            //String path = "/sdcard/1/" + System.currentTimeMillis() + ".jpeg";
                            //MXResult<?> imageSave = MXImageToolsAPI.getInstance().ImageSave(path, mxImage.buffer, mxImage.width, mxImage.height, 3);
                            //Timber.e(TAG, "imageSave: " + imageSave);
                            List<RectF> list = new ArrayList<>();
                            List<MXFace> data = detectFace.getData();
                            for (MXFace mxFace : data) {
                                list.add(mxFace.getFaceRectF());
                            }
                            boolean b = this.CurrentMxImage_Nir.compareAndSet(null, new AbstractMap.SimpleEntry<>(mxImage, MXFaceIdAPI.getInstance().getMaxFace(data)));
                            emitter.onNext(list);
                            return;
                        }
                    }
                }
            }
            emitter.onNext(new ArrayList<>());
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (ListUtils.isNullOrEmpty(list)) {
                        this.IsNirFrameProcessing.set(false);
                        this.IsNirEnable.set(true);
                    } else {
                        this.StartCountdown.postValue(true);
                        processLiveAndMatch();
                    }
                }, throwable -> {
                    this.CurrentMxImage_Rgb.set(null);
                    this.IsNirEnable.set(true);
                    this.CurrentMxImage_Nir.set(null);
                    this.IsNirFrameProcessing.set(false);
                });
    }

    /**
     * 开启可见光预览
     */
    public void showRgbCameraPreview(SurfaceTexture surface) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<MXCamera>>) emitter -> {
            //            ZZResponse<?> init = CameraHelper.getInstance().init(2);
            //            Timber.e("init:" + init);
            //            if (ZZResponse.isSuccess(init)) {
            ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_RGB);
            Timber.e("createMXCamera:" + mxCamera);
            if (ZZResponse.isSuccess(mxCamera)) {
                MXCamera camera = mxCamera.getData();
                int startTexture = camera.startTexture(surface);
                Timber.e("startTexture:" + startTexture);
                if (startTexture == 0) {
                    emitter.onNext(mxCamera);
                } else {
                    emitter.onNext(ZZResponse.CreateFail(startTexture, "Preview failed"));
                }
            } else {
                emitter.onNext(mxCamera);
            }
            //            } else {
            //                emitter.onNext(ZZResponse.CreateFail(init));
            //            }
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(camera -> {
                    if (ZZResponse.isSuccess(camera)) {
                        camera.getData().setPreviewCallback(PreviewViewModel.this);
                        startNirPreview();
                    }
                    startRgbFrame();
                }, throwable -> {
                    this.StartCountdown.postValue(false);
                    this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
                });
    }

    /**
     * 开启近红外预览
     */
    private void startNirPreview() {
        if (!this.IsNirEnable.get()) {
            return;
        }
        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<MXCamera>>) emitter -> {
            ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_NIR);
            Timber.e("createMXCamera:" + mxCamera);
            if (ZZResponse.isSuccess(mxCamera)) {
                MXCamera camera = mxCamera.getData();
                int start = camera.start(this.SurfaceHolder_Nir.get());
                Timber.e("start:" + start);
                if (start == 0) {
                    emitter.onNext(mxCamera);
                } else {
                    emitter.onNext(ZZResponse.CreateFail(start, "Preview(Nir) failed"));
                }
            } else {
                emitter.onNext(mxCamera);
            }
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(camera -> {
                    if (ZZResponse.isSuccess(camera)) {
                        camera.getData().setPreviewCallback(PreviewViewModel.this);
                    }
                    this.IsCameraEnable_Nir.setValue(camera);
                }, throwable -> {
                    this.IsCameraEnable_Nir.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
                });
    }

    private String lastUserID;
    private long lastTime;

    /**
     * 处理活体和比对
     */
    private void processLiveAndMatch() {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<AttendanceBean>>) emitter -> {
            Map.Entry<MxImage, MXFace> rgbEntry = this.CurrentMxImage_Rgb.get();
            Map.Entry<MxImage, MXFace> nirEntry = this.CurrentMxImage_Nir.get();
            if (rgbEntry == null || nirEntry == null
                    || rgbEntry.getKey() == null || nirEntry.getKey() == null
                    || rgbEntry.getKey().isBufferEmpty() || !rgbEntry.getKey().isSizeLegal()
                    || rgbEntry.getValue() == null || nirEntry.getValue() == null
                    || rgbEntry.getKey().isBufferEmpty() || !rgbEntry.getKey().isSizeLegal()) {
                emitter.onNext(ZZResponse.CreateFail(ZZResponseCode.CODE_ILLEGAL_PARAMETER, "正在检测"));
            } else {
                MxImage rgbImage = rgbEntry.getKey();
                MXFace rgbFace = rgbEntry.getValue();
                //可见光活体判断
                MXResult<?> rgbResult = MXFaceIdAPI.getInstance().mxRGBLiveDetect(rgbImage.buffer, rgbImage.width, rgbImage.height, rgbFace);
                if (!MXResult.isSuccess(rgbResult)) {
                    emitter.onNext(ZZResponse.CreateFail(rgbResult.getCode(), rgbResult.getMsg()));
                    return;
                }
                //3. 近红外人脸检测
                //4. 近红外活体检测
                MxImage nirImage = nirEntry.getKey();
                MXFace nirFace = nirEntry.getValue();
                MXResult<Integer> nirResult = MXFaceIdAPI.getInstance().mxNIRLiveDetect(nirImage.buffer, nirImage.width, nirImage.height, nirFace);
                if (!MXResult.isSuccess(nirResult)) {
                    emitter.onNext(ZZResponse.CreateFail(nirResult.getCode(), nirResult.getMsg()));
                    return;
                }
                if (nirResult.getData() < MXFaceIdAPI.getInstance().FaceLive) {
                    emitter.onNext(ZZResponse.CreateFail(-76, "非活体"));
                    //emitter.onNext(ZZResponse.CreateFail(-76, "非活体，value:" + nirResult.getData()));
                    return;
                }

                //5.比对
                //5.1提取特征
                MXResult<byte[]> featureExtract = MXFaceIdAPI.getInstance().mxFeatureExtract(rgbImage.buffer, rgbImage.width, rgbImage.height, rgbFace);
                if (!MXResult.isSuccess(featureExtract)) {
                    emitter.onNext(ZZResponse.CreateFail(featureExtract.getCode(), featureExtract.getMsg()));
                    return;
                }
                HashMap<String, Face> all = FaceModel.findAll();
                if (MapUtils.isNullOrEmpty(all)) {
                    saveFailedAttendance(rgbImage, rgbFace);
                    emitter.onNext(ZZResponse.CreateFail(-80, "人脸数据库为空"));
                    return;
                }
                Face tempFace = null;
                float tempFloat = 0F;
                for (Map.Entry<String, Face> entry : all.entrySet()) {
                    Face value = entry.getValue();
                    if (value != null) {
                        MXResult<Float> result = MXFaceIdAPI.getInstance().mxFeatureMatch(featureExtract.getData(), value.FaceFeature);
                        if (MXResult.isSuccess(result)) {
                            if (result.getData() >= tempFloat) {
                                tempFace = value;
                                tempFloat = result.getData();
                            }
                        }
                    }
                }
                if (tempFloat < MXFaceIdAPI.getInstance().FaceMatch) {
                    saveFailedAttendance(rgbImage, rgbFace);
                    emitter.onNext(ZZResponse.CreateFail(-81, "正在识别中，最大匹配值：" + tempFloat));
                    return;
                }
                if (lastUserID != null && lastUserID.equals(tempFace.UserId) && (System.currentTimeMillis() - lastTime) <= AppConfig.verifyTimeOut) {
                    emitter.onNext(ZZResponse.CreateFail(-83, "重复识别"));
                    return;
                }
                lastUserID=tempFace.UserId;
                lastTime = System.currentTimeMillis();
                Person person = PersonModel.findByUserID(tempFace.UserId);
                if (person == null) {
                    saveFailedAttendance(rgbImage, rgbFace);
                    emitter.onNext(ZZResponse.CreateFail(-82, "该人员不存在，人员ID：" + tempFace.UserId));
                    return;
                }
                //识别通过

                String capturePath = AppConfig.Path_CaptureImage + "face" + "_" + person.UserId + "_" + System.currentTimeMillis() + ".jpeg";
                MXResult<?> save = MXImageToolsAPI.getInstance().ImageSave(capturePath, rgbImage.buffer, rgbImage.width, rgbImage.height, 3);
                if (!MXResult.isSuccess(save)) {
                    emitter.onNext(ZZResponse.CreateFail(save.getCode(), save.getMsg()));
                    return;
                }
                LocalImage captureLocalImage = new LocalImage();
                captureLocalImage.LocalPath = capturePath;
                captureLocalImage.id = LocalImageModel.insert(captureLocalImage);
                if (captureLocalImage.id <= 0) {
                    emitter.onNext(ZZResponse.CreateFail(-70, "保存图片记录失败"));
                    return;
                }
                Rect faceRect = rgbFace.getFaceRect();
                faceRect.left = Math.max((int) (faceRect.left * 0.8F), 0);
                faceRect.top = Math.max((int) (faceRect.top * 0.7F), 0);
                faceRect.right = Math.min((int) (faceRect.right * 1.2F), 480);
                faceRect.bottom = Math.min((int) (faceRect.bottom * 1.1F), 640);
                MXResult<MxImage> cutRect = MXImageToolsAPI.getInstance().ImageCutRect(rgbImage, faceRect);
                if (!MXResult.isSuccess(cutRect)) {
                    emitter.onNext(ZZResponse.CreateFail(cutRect.getCode(), cutRect.getMsg()));
                    return;
                }
                MxImage cutImage = cutRect.getData();
                String cutPath = AppConfig.Path_CutImage + person.UserId + "_" + System.currentTimeMillis() + ".jpeg";
                MXResult<?> cutSave = MXImageToolsAPI.getInstance().ImageSave(cutPath, cutImage.buffer, cutImage.width, cutImage.height, 3);
                if (!MXResult.isSuccess(cutSave)) {
                    emitter.onNext(ZZResponse.CreateFail(cutSave.getCode(), cutSave.getMsg()));
                    return;
                }
                LocalImage cutLocalImage = new LocalImage();
                cutLocalImage.LocalPath = cutPath;
                cutLocalImage.id = LocalImageModel.insert(cutLocalImage);
                if (cutLocalImage.id <= 0) {
                    emitter.onNext(ZZResponse.CreateFail(-71, "保存人脸截图记录失败"));
                    return;
                }
                Attendance attendance = new Attendance();
                attendance.UserId = person.UserId;
                //attendance.BaseImage = person.FaceImage;
                attendance.CaptureImage = captureLocalImage.id;
                attendance.CutImage = cutLocalImage.id;
                attendance.Mode = 1;
                attendance.Status = 1;
                attendance.id = AttendanceModel.insert(attendance);
                if (attendance.id <= 0) {
                    emitter.onNext(ZZResponse.CreateFail(-60, "保存考勤记录失败"));
                    return;
                }
                //List<LocalImage> byID = LocalImageModel.findByID(person.FaceImage);
                //if (ListUtils.isNullOrEmpty(byID)) {
                //    emitter.onNext(ZZResponse.CreateFail(-61, "该人员不存在，UserId：" + tempFace.UserId));
                //    return;
                //}
                AttendanceBean attendanceBean = new AttendanceBean();
                attendanceBean.AttendanceId = attendance.id;
                attendanceBean.Status = 1;
                attendanceBean.Mode = 1;
                attendanceBean.UserId = person.UserId;
                attendanceBean.CaptureImage = capturePath;
                attendanceBean.CutImage = cutPath;
                attendanceBean.UserName = person.Name;
//                attendanceBean.tempFloat=new DecimalFormat("##0.00").format(tempFloat);
                attendanceBean.tempFloat=tempFloat;
                attendanceBean.tempType=0;
                //attendanceBean.BaseImage = byID.get(0).LocalPath;
                //开启门禁
                emitter.onNext(ZZResponse.CreateSuccess(attendanceBean));
            }
        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Timber.e("processLiveAndMatch: " + response);
                    this.IsNirEnable.set(true);
                    this.AttendanceBean.setValue(response);
                    this.IsNirFrameProcessing.set(false);
                    this.CurrentMxImage_Rgb.set(null);
                    this.CurrentMxImage_Nir.set(null);
                }, throwable -> {
                    this.IsNirEnable.set(true);
                    this.AttendanceBean.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
                    this.IsNirFrameProcessing.set(false);
                    this.CurrentMxImage_Rgb.set(null);
                    this.CurrentMxImage_Nir.set(null);
                });
    }

    @Override
    public void onPreview(MXCamera camera, MXFrame frame) {
        if (camera.getCameraId() == CameraConfig.Camera_RGB.CameraId) {
            this.Process_Rgb(frame);
        } else {
            this.Process_Nir(frame);
        }
    }

    private void saveFailedAttendance(MxImage rgbImage, MXFace rgbFace) {
        long currentTimeMillis = System.currentTimeMillis();
        String capturePath = AppConfig.Path_CaptureImage + "face" + "_" + "temp_" + currentTimeMillis + ".jpeg";
        MXResult<?> save = MXImageToolsAPI.getInstance().ImageSave(capturePath, rgbImage.buffer, rgbImage.width, rgbImage.height, 3);
        if (!MXResult.isSuccess(save)) {
            return;
        }
        LocalImage captureLocalImage = new LocalImage();
        captureLocalImage.LocalPath = capturePath;
        captureLocalImage.id = LocalImageModel.insert(captureLocalImage);
        if (captureLocalImage.id <= 0) {
            return;
        }

        MXResult<MxImage> cutRect = MXImageToolsAPI.getInstance().ImageCutRect(rgbImage, rgbFace.getFaceRect());
        if (!MXResult.isSuccess(cutRect)) {
            return;
        }
        MxImage cutImage = cutRect.getData();
        String cutPath = AppConfig.Path_CutImage + "temp_" + currentTimeMillis + ".jpeg";
        MXResult<?> cutSave = MXImageToolsAPI.getInstance().ImageSave(cutPath, cutImage.buffer, cutImage.width, cutImage.height, 3);
        if (!MXResult.isSuccess(cutSave)) {
            return;
        }
        LocalImage cutLocalImage = new LocalImage();
        cutLocalImage.LocalPath = cutPath;
        cutLocalImage.id = LocalImageModel.insert(cutLocalImage);
        if (cutLocalImage.id <= 0) {
            return;
        }

        Attendance attendance = new Attendance();
        attendance.CaptureImage = captureLocalImage.id;
        attendance.CutImage = cutLocalImage.id;
        attendance.Mode = 1;
        attendance.Status = 2;
        attendance.id = AttendanceModel.insert(attendance);
    }

    public void resume() {
        this.CurrentMxImage_Rgb.set(null);
        this.CurrentMxImage_Nir.set(null);
        CameraHelper.getInstance().resume();
    }

    public void pause() {
        CameraHelper.getInstance().pause();
    }

    public void destroy() {
        this.StartCountdown.postValue(false);
        CameraHelper.getInstance().stop();
        CameraHelper.getInstance().free();
    }

}