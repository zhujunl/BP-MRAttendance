//package com.miaxis.attendance.ui.preview;
//
//import android.graphics.RectF;
//import android.graphics.SurfaceTexture;
//import android.os.Handler;
//import android.util.Log;
//import android.view.SurfaceHolder;
//
//import com.miaxis.attendance.App;
//import com.miaxis.attendance.data.bean.AttendanceBean;
//import com.miaxis.attendance.data.entity.Attendance;
//import com.miaxis.attendance.data.entity.Face;
//import com.miaxis.attendance.data.entity.LocalImage;
//import com.miaxis.attendance.data.model.AttendanceModel;
//import com.miaxis.attendance.data.model.FaceModel;
//import com.miaxis.attendance.data.model.LocalImageModel;
//import com.miaxis.common.camera.CameraConfig;
//import com.miaxis.common.camera.CameraHelper;
//import com.miaxis.common.camera.CameraPreviewCallback;
//import com.miaxis.common.camera.MXCamera;
//import com.miaxis.common.camera.MXFrame;
//import com.miaxis.common.response.ZZResponse;
//import com.miaxis.common.response.ZZResponseCode;
//
//import org.zz.api.MXFace;
//import org.zz.api.MXFaceIdAPI;
//import org.zz.api.MXImageToolsAPI;
//import org.zz.api.MXResult;
//import org.zz.api.MxImage;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import androidx.lifecycle.MutableLiveData;
//import androidx.lifecycle.ViewModel;
//import io.reactivex.Observable;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//
//public class Preview2ViewModel extends ViewModel implements CameraPreviewCallback {
//
//    private static final String TAG = "PreviewViewModel";
//    private Handler mHandler = new Handler();
//    private long timeOut = 1000 * 4L;
//
//    /**
//     * 人脸框
//     */
//    MutableLiveData<RectF> faceRect = new MutableLiveData<>();
//    /**
//     * 摄像头是否可用
//     */
//    MutableLiveData<ZZResponse<?>> IsCameraEnable_Rgb = new MutableLiveData<>();
//    MutableLiveData<ZZResponse<?>> IsCameraEnable_Nir = new MutableLiveData<>();
//
//    /**
//     * 近红外预览区域
//     */
//    AtomicReference<SurfaceHolder> SurfaceHolder_Nir = new AtomicReference<>();
//    /**
//     * Nir视频帧是否正在处理
//     */
//    AtomicBoolean IsNirFrameProcessing = new AtomicBoolean(false);
//    /**
//     * 是否启用近红外帧
//     */
//    AtomicBoolean IsNirEnable = new AtomicBoolean(true);
//
//
//    /**
//     * 人脸帧数据缓存
//     */
//    AtomicReference<MxImage> CurrentImageCache_Rgb = new AtomicReference<>();
//    AtomicReference<MXFace> CurrentFaceCache_Rgb = new AtomicReference<>();
//    //AtomicReference<MxImage> CurrentImageCache_Nir = new AtomicReference<>();
//
//    //MutableLiveData<Boolean> HaveFace = new MutableLiveData<>(false);
//
//    MutableLiveData<Boolean> StartCountdown = new MutableLiveData<>(true);
//
//    MutableLiveData<ZZResponse<AttendanceBean>> AttendanceBean = new MutableLiveData<>();
//
//    public Preview2ViewModel() {
//    }
//
//    /**
//     * 处理可见光视频帧数据
//     */
//    private synchronized void Process_Rgb(MXFrame frame) {
//        this.CurrentFaceCache_Rgb.set(null);
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<MxImage>) emitter -> {
//            if (!MXFrame.isBufferEmpty(frame) && MXFrame.isSizeLegal(frame)) {
//                MXResult<byte[]> mxResult = MXImageToolsAPI.getInstance().YUV2RGB(frame.buffer, frame.width, frame.height);//MR90 10ms
//                if (MXResult.isSuccess(mxResult)) {
//                    MXResult<MxImage> imageRotate = MXImageToolsAPI.getInstance().ImageRotate(
//                            new MxImage(frame.width, frame.height, mxResult.getData()), CameraConfig.Camera_RGB.bufferOrientation);//MR90 15ms
//                        Log.d(TAG, "imageRotate: "+imageRotate);
//                    if (MXResult.isSuccess(imageRotate)) {
//                        emitter.onNext(imageRotate.getData());
//                        return;
//                    }
//                }
//            }
//            throw new RuntimeException("Process Rgb failed");
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(mxImage -> {
//                    this.CurrentImageCache_Rgb.set(mxImage);
//                    startNirFrame();
//                }, throwable -> {
//                    Log.d(TAG, "Process_Rgb: "+throwable);
//                    this.CurrentImageCache_Rgb.set(null);
//                    startRgbFrame();
//                });
//    }
//
//    /**
//     * 开启可见光视频帧
//     */
//    private void startRgbFrame() {
//        ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_RGB);
//        if (ZZResponse.isSuccess(mxCamera)) {
//            int enable = mxCamera.getData().setNextFrameEnable();
//            timeOutReset();
//            if (this.StartCountdown.getValue() != null && !this.StartCountdown.getValue()) {
//                this.StartCountdown.setValue(true);
//            }
//            this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateSuccess());
//        } else {
//            this.StartCountdown.setValue(false);
//            this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateFail(mxCamera.getCode(), mxCamera.getMsg()));
//        }
//    }
//
//    /**
//     * 倒计时重置
//     */
//    private void timeOutReset() {
//        timeOutCancel();
//        if (this.mHandler != null) {
//            this.mHandler.postDelayed(() -> {
//                StartCountdown.setValue(false);
//                IsCameraEnable_Rgb.setValue(ZZResponse.CreateFail(-98, "Camera preview is error"));
//            }, this.timeOut);
//        }
//    }
//
//    /**
//     * 取消倒计时
//     */
//    private void timeOutCancel() {
//        if (this.mHandler != null) {
//            this.mHandler.removeCallbacksAndMessages(null);
//        }
//    }
//
//    /**
//     * 开启近红外视频帧
//     * 主线程
//     */
//    private void startNirFrame() {
//        if (!this.IsNirFrameProcessing.get()) {
//            ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_NIR);
//            if (ZZResponse.isSuccess(mxCamera)) {
//                int enable = mxCamera.getData().setNextFrameEnable();
//                this.IsNirFrameProcessing.set(true);
//                this.StartCountdown.setValue(false);
//                this.IsCameraEnable_Nir.setValue(ZZResponse.CreateSuccess());
//            } else {
//                this.StartCountdown.setValue(false);
//                this.IsCameraEnable_Nir.setValue(ZZResponse.CreateFail(mxCamera.getCode(), mxCamera.getMsg()));
//            }
//        }
//    }
//
//    /**
//     * 处理近红外视频帧数据
//     */
//    private void Process_Nir(MXFrame frame) {
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<RectF>) emitter -> {
//            this.CurrentFaceCache_Rgb.set(null);
//            if (!MXFrame.isBufferEmpty(frame) && MXFrame.isSizeLegal(frame)) {
//                MXResult<byte[]> mxResult = MXImageToolsAPI.getInstance().YUV2RGB(frame.buffer, frame.width, frame.height);//MR90 10ms
//                if (MXResult.isSuccess(mxResult)) {
//                    MXResult<MxImage> imageRotate = MXImageToolsAPI.getInstance().ImageRotate(
//                            new MxImage(frame.width, frame.height, mxResult.getData()),
//                            CameraConfig.Camera_NIR.bufferOrientation);//MR90 15ms
//                    if (MXResult.isSuccess(imageRotate)) {
//                        MxImage mxImage_Nir = imageRotate.getData();
//                        MxImage mxImage_Rgb = this.CurrentImageCache_Rgb.get();
//                        MXResult<List<MXFace>> detectLive = MXFaceIdAPI.getInstance().detectLive(mxImage_Rgb.buffer, mxImage_Rgb.width, mxImage_Rgb.height, mxImage_Nir.buffer);
//                        if (MXResult.isSuccess(detectLive)) {
//                            //String path = "/sdcard/1/" + System.currentTimeMillis() + ".jpeg";
//                            //MXResult<?> imageSave = MXImageToolsAPI.getInstance().ImageSave(path, mxImage.buffer, mxImage.width, mxImage.height, 3);
//                            //Log.e(TAG, "imageSave: " + imageSave);
//                            List<MXFace> data = detectLive.getData();
//                            MXFace maxFace = MXFaceIdAPI.getInstance().getMaxFace(data);
//                            this.CurrentFaceCache_Rgb.set(maxFace);
//                            if (maxFace != null) {
//                                emitter.onNext(maxFace.getFaceRectF());
//                                return;
//                            }
//                        } else {
//                            throw new RuntimeException(detectLive.getMsg());
//                        }
//                    }
//                }
//            }
//            throw new RuntimeException("检测失败");
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(face -> {
//                    this.faceRect.postValue(face);
//                    if (face == null) {
//                        this.IsNirFrameProcessing.set(false);
//                    } else {
//                        processMatch();
//                    }
//                }, throwable -> {
//                    Log.d(TAG, "Process_Nir: "+throwable);
//                    this.faceRect.postValue(null);
//                    this.IsNirFrameProcessing.set(false);
//                    startRgbFrame();
//                });
//    }
//
//    /**
//     * 开启可见光预览
//     */
//    public void showRgbCameraPreview(SurfaceTexture surface) {
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<MXCamera>>) emitter -> {
//            ZZResponse<?> init = CameraHelper.getInstance().init(2);
//            Log.e(TAG, "init:" + init);
//            if (ZZResponse.isSuccess(init)) {
//                ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_RGB);
//                Log.e(TAG, "createMXCamera:" + mxCamera);
//                if (ZZResponse.isSuccess(mxCamera)) {
//                    MXCamera camera = mxCamera.getData();
//                    int startTexture = camera.startTexture(surface);
//                    Log.e(TAG, "startTexture:" + startTexture);
//                    if (startTexture == 0) {
//                        emitter.onNext(mxCamera);
//                    } else {
//                        emitter.onNext(ZZResponse.CreateFail(startTexture, "Preview failed"));
//                    }
//                } else {
//                    emitter.onNext(mxCamera);
//                }
//            } else {
//                emitter.onNext(ZZResponse.CreateFail(init));
//            }
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(camera -> {
//                    if (ZZResponse.isSuccess(camera)) {
//                        camera.getData().setPreviewCallback(Preview2ViewModel.this);
//                        startNirPreview();
//                    }
//                    startRgbFrame();
//                }, throwable -> {
//                    this.IsCameraEnable_Rgb.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
//                });
//    }
//
//    /**
//     * 开启近红外预览
//     */
//    private void startNirPreview() {
//        if (!this.IsNirEnable.get()) {
//            return;
//        }
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<MXCamera>>) emitter -> {
//            ZZResponse<MXCamera> mxCamera = CameraHelper.getInstance().createOrFindMXCamera(CameraConfig.Camera_NIR);
//            Log.e(TAG, "createMXCamera:" + mxCamera);
//            if (ZZResponse.isSuccess(mxCamera)) {
//                MXCamera camera = mxCamera.getData();
//                int start = camera.start(this.SurfaceHolder_Nir.get());
//                Log.e(TAG, "start:" + start);
//                if (start == 0) {
//                    emitter.onNext(mxCamera);
//                } else {
//                    emitter.onNext(ZZResponse.CreateFail(start, "Preview(Nir) failed"));
//                }
//            } else {
//                emitter.onNext(mxCamera);
//            }
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(camera -> {
//                    if (ZZResponse.isSuccess(camera)) {
//                        camera.getData().setPreviewCallback(Preview2ViewModel.this);
//                    }
//                    this.IsCameraEnable_Nir.setValue(camera);
//                }, throwable -> {
//                    this.IsCameraEnable_Nir.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
//                });
//    }
//
//    /**
//     * 处理人脸比对
//     */
//    private void processMatch() {
//        Disposable subscribe = Observable.create((ObservableOnSubscribe<ZZResponse<AttendanceBean>>) emitter -> {
//            MXFace rgbFace = this.CurrentFaceCache_Rgb.get();
//            MxImage rgbImage = this.CurrentImageCache_Rgb.get();
//            if (rgbFace == null || rgbImage == null || rgbImage.isBufferEmpty() || !rgbImage.isSizeLegal()) {
//                emitter.onNext(ZZResponse.CreateFail(ZZResponseCode.CODE_ILLEGAL_PARAMETER, ZZResponseCode.MSG_ILLEGAL_PARAMETER));
//            } else {
//                //5.比对
//                //5.1提取特征
//                MXResult<byte[]> featureExtract = MXFaceIdAPI.getInstance().mxFeatureExtract(rgbImage.buffer, rgbImage.width, rgbImage.height, rgbFace);
//                if (!MXResult.isSuccess(featureExtract)) {
//                    emitter.onNext(ZZResponse.CreateFail(featureExtract.getCode(), featureExtract.getMsg()));
//                    return;
//                }
//                List<Face> all = FaceModel.findAll();
//                if (all.size()==0) {
//                    emitter.onNext(ZZResponse.CreateFail(-80, "人脸数据库为空"));
//                    return;
//                }
//                Face tempFace = null;
//                float tempFloat = 0F;
//                for (Face value:all) {
//                    if (value!=null){
//                        MXResult<Float> result = MXFaceIdAPI.getInstance().mxFeatureMatch(featureExtract.getData(), value.FaceFeature);
//                        if (MXResult.isSuccess(result)) {
//                            if (result.getData() >= tempFloat) {
//                                tempFace = value;
//                                tempFloat = result.getData();
//                            }
//                        }
//                    }
//                }
//                if (tempFloat < MXFaceIdAPI.getInstance().FaceMatch) {
//                    emitter.onNext(ZZResponse.CreateFail(-81, "未找到，最大匹配值：" + tempFloat));
//                    return;
//                }
////                Person person = PersonModel.findByUserID(tempFace.UserId);
////                if (person==null) {
////                    emitter.onNext(ZZResponse.CreateFail(-82, "该人员不存在，UserId：" + tempFace.UserId));
////                    return;
////                }
//                //识别通过
//
////                String capturePath = AppConfig.Path_CaptureImage + person.UserId + "_" + System.currentTimeMillis() + ".jpeg";
////                MXResult<?> save = MXImageToolsAPI.getInstance().ImageSave(capturePath, rgbImage.buffer, rgbImage.width, rgbImage.height, 3);
////                if (!MXResult.isSuccess(save)) {
////                    emitter.onNext(ZZResponse.CreateFail(save.getCode(), save.getMsg()));
////                    return;
////                }
//                LocalImage captureLocalImage = new LocalImage();
////                captureLocalImage.LocalPath = capturePath;
//                captureLocalImage.id = LocalImageModel.insert(captureLocalImage);
//                if (captureLocalImage.id <= 0) {
//                    emitter.onNext(ZZResponse.CreateFail(-70, "保存图片记录失败"));
//                    return;
//                }
//                MXResult<MxImage> cutRect = MXImageToolsAPI.getInstance().ImageCutRect(rgbImage, rgbFace.getFaceRect());
//                if (!MXResult.isSuccess(cutRect)) {
//                    emitter.onNext(ZZResponse.CreateFail(cutRect.getCode(), cutRect.getMsg()));
//                    return;
//                }
//                MxImage cutImage = cutRect.getData();
////                String cutPath = AppConfig.Path_CutImage + person.UserId + "_" + System.currentTimeMillis() + ".jpeg";
////                MXResult<?> cutSave = MXImageToolsAPI.getInstance().ImageSave(cutPath, cutImage.buffer, cutImage.width, cutImage.height, 3);
////                if (!MXResult.isSuccess(cutSave)) {
////                    emitter.onNext(ZZResponse.CreateFail(cutSave.getCode(), cutSave.getMsg()));
////                    return;
////                }
//                LocalImage cutLocalImage = new LocalImage();
////                cutLocalImage.LocalPath = cutPath;
//                cutLocalImage.id = LocalImageModel.insert(cutLocalImage);
//                if (cutLocalImage.id <= 0) {
//                    emitter.onNext(ZZResponse.CreateFail(-71, "保存人脸截图记录失败"));
//                    return;
//                }
//
//                Attendance attendance = new Attendance();
////                attendance.UserId = person.UserId;
//                //attendance.BaseImage = person.FaceImage;
//                attendance.CaptureImage = captureLocalImage.id;
//                attendance.CutImage = cutLocalImage.id;
//                attendance.Mode = 1;
//                attendance.Status = 1;
//                attendance.id = AttendanceModel.insert(attendance);
//                if (attendance.id <= 0) {
//                    emitter.onNext(ZZResponse.CreateFail(-60, "保存考勤记录失败"));
//                    return;
//                }
//                //List<LocalImage> byID = LocalImageModel.findByID(person.FaceImage);
//                //if (ListUtils.isNullOrEmpty(byID)) {
//                //    emitter.onNext(ZZResponse.CreateFail(-61, "该人员不存在，UserId：" + tempFace.UserId));
//                //    return;
//                //}
//                AttendanceBean attendanceBean = new AttendanceBean();
//                attendanceBean.AttendanceId = attendance.id;
//                attendanceBean.Status = 1;
//                attendanceBean.Mode = 1;
////                attendanceBean.UserId = person.UserId;
////                attendanceBean.CaptureImage = capturePath;
////                attendanceBean.CutImage = cutPath;
////                attendanceBean.UserName = person.Name;
//                //attendanceBean.BaseImage = byID.get(0).LocalPath;
//                //开启门禁
//                emitter.onNext(ZZResponse.CreateSuccess(attendanceBean));
//            }
//        }).subscribeOn(Schedulers.from(App.getInstance().threadExecutor))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(response -> {
//                    Log.e(TAG, "processLiveAndMatch: " + response);
//                    this.AttendanceBean.setValue(response);
//                    this.IsNirFrameProcessing.set(false);
//                    this.CurrentFaceCache_Rgb.set(null);
//                    this.CurrentImageCache_Rgb.set(null);
//                    startRgbFrame();
//                }, throwable -> {
//                    this.AttendanceBean.setValue(ZZResponse.CreateFail(-99, throwable.getMessage()));
//                    this.IsNirFrameProcessing.set(false);
//                    this.CurrentFaceCache_Rgb.set(null);
//                    this.CurrentImageCache_Rgb.set(null);
//                    startRgbFrame();
//                });
//    }
//
//    @Override
//    public void onPreview(MXCamera camera, MXFrame frame) {
//        if (camera.getCameraId() == CameraConfig.Camera_RGB.CameraId) {
//            timeOutCancel();
//            this.Process_Rgb(frame);
//        } else {
//            this.Process_Nir(frame);
//        }
//    }
//
//    public void resume() {
//        this.CurrentFaceCache_Rgb.set(null);
//        this.CurrentImageCache_Rgb.set(null);
//        CameraHelper.getInstance().resume();
//    }
//
//    public void pause() {
//        timeOutCancel();
//        CameraHelper.getInstance().pause();
//    }
//
//    public void destroy() {
//        CameraHelper.getInstance().stop();
//        CameraHelper.getInstance().free();
//    }
//
//}