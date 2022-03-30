package com.miaxis.bp_entry.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.miaxis.bp_entry.been.Intermediary;
import com.miaxis.bp_entry.been.MxRGBImage;
import com.miaxis.bp_entry.been.PhotoFaceFeature;
import com.miaxis.bp_entry.event.DrawRectEvent;
import com.miaxis.bp_entry.util.ValueUtil;

import org.greenrobot.eventbus.EventBus;
import org.zz.api.MXFaceAPI;
import org.zz.api.MXFaceInfoEx;
import org.zz.jni.mxImageTool;

import java.io.ByteArrayOutputStream;

public class FaceManager {

    private FaceManager() {
        mxFaceAPI = new MXFaceAPI();
        dtTool = new mxImageTool();
    }

    public static FaceManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final FaceManager instance = new FaceManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    public static final int ERR_LICENCE         = -2009;
    public static final int ERR_FILE_COMPARE    = -101;
    public static final int INIT_SUCCESS        = 0;
    public static final int ZOOM_WIDTH = 480;
    public static final int ZOOM_HEIGHT = 640;

    private static final int MAX_FACE_NUM = 50;
    private static final Byte lock1 = 1;
    private static final Byte lock2 = 2;

    private MXFaceAPI mxFaceAPI;
    private mxImageTool dtTool;

    private HandlerThread asyncDetectThread;
    private Handler asyncDetectHandler;
    private volatile boolean detectLoop = true;

    private HandlerThread asyncExtractThread;
    private Handler asyncExtractHandler;
    private volatile boolean extractLoop = true;
    private volatile boolean needNextFeature = false;
    private volatile boolean nova = false;
    private volatile Intermediary intermediaryData;

    private byte[] lastPreviewData;
    private int orientation = 270;

    private OnFeatureExtractListener featureListener;

    /**
     * 初始化人脸算法
     *
     * @param context     设备上下文
     * @param licencePath 授权文件路径
     * @return 状态码
     */
    public int initFaceST(Context context, String licencePath) {
        //        final String sLicence = FileUtil.readLicence(licencePath);
        //BP-990 不用授权文件自动授权
        //        if (TextUtils.isEmpty(sLicence)) {
        //            return ERR_LICENCE;
        //        }
        int re = mxFaceAPI.mxInitAlg(context, "", "");
        asyncDetectThread = new HandlerThread("detect_thread");
        asyncDetectThread.setPriority(3);
        asyncDetectThread.start();
        asyncDetectHandler = new Handler(asyncDetectThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (detectLoop) {
                    previewDataLoop();
                }
            }
        };
        asyncExtractThread = new HandlerThread("extract_thread");
        asyncExtractThread.setPriority(4);
        asyncExtractThread.start();
        asyncExtractHandler = new Handler(asyncExtractThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (extractLoop) {
                    intermediaryDataLoop();
                }
            }
        };
        return re;
    }


    public static String getFaceInitResultDetail(int result) {
        switch (result) {
            case ERR_LICENCE:
                return "读取授权文件失败";
            case ERR_FILE_COMPARE:
                return "文件校验失败";
            case INIT_SUCCESS:
                return "初始化人脸算法成功";
            default:
                return "初始化算法失败";
        }
    }

    public void startLoop() {
        detectLoop = true;
        extractLoop = true;
        lastPreviewData = null;
        intermediaryData = null;
        needNextFeature = true;
        asyncDetectHandler.sendEmptyMessage(0);
        asyncExtractHandler.sendEmptyMessage(0);
    }

    public void stopLoop() {
        detectLoop = false;
        extractLoop = false;
        asyncDetectHandler.removeMessages(0);
        asyncExtractHandler.removeMessages(0);
    }

    public void setNeedNextFeature(boolean needNextFeature) {
        this.needNextFeature = needNextFeature;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    private void sendEvent(Object object) {
        if (detectLoop) {
            EventBus.getDefault().post(object);
        }
    }

    public void setLastPreviewData(byte[] lastPreviewData) {
        this.lastPreviewData = lastPreviewData;
    }

    private void previewDataLoop() {
        try {
            if (this.lastPreviewData == null) {
                Thread.sleep(100);
                asyncDetectHandler.sendEmptyMessage(0);
            } else {
//                verify(lastPreviewData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncDetectHandler.sendEmptyMessage(0);
        }
    }

//    private void verify(byte[] detectData) {
//        try {
//            String message = "";
//            long time = System.currentTimeMillis();
//            byte[] zoomedRgbData = cameraPreviewConvert(detectData, CameraManager.PRE_WIDTH, CameraManager.PRE_HEIGHT, orientation, ZOOM_WIDTH, ZOOM_HEIGHT);
//            if (zoomedRgbData == null) {
//                sendEvent(new DrawRectEvent(0, null));
//                return;
//            }
//            int[] faceNum = new int[]{MAX_FACE_NUM};
//            MXFaceInfoEx[] faceBuffer = makeFaceContainer(faceNum[0]);
//            boolean result = faceDetect(zoomedRgbData, ZOOM_WIDTH, ZOOM_HEIGHT, faceNum, faceBuffer);
//            if (result && faceNum[0] > 0) {
//                sendEvent(new DrawRectEvent(faceNum[0], faceBuffer));
//                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(faceBuffer);
//                result = faceQuality(zoomedRgbData, ZOOM_WIDTH, ZOOM_HEIGHT, 1, new MXFaceInfoEx[]{mxFaceInfoEx});
//                if (!result) {
//                    message = "提取特征失败";
//                    sendEvent(new VerifyEvent(message));
//                } else {
//                    //                    if (Math.abs(mxFaceInfoEx.pitch) > 20 || Math.abs(mxFaceInfoEx.yaw) > 20 || Math.abs(mxFaceInfoEx.roll) > 20) {
//                    //                        message = "请正对摄像头";
//                    //                    } else if (mxFaceInfoEx.illumination < 50) {
//                    //                        message = "脸部过暗";
//                    //                    } else if (mxFaceInfoEx.illumination > 200) {
//                    //                        message = "脸部过亮";
//                    //                    } else if (mxFaceInfoEx.blur > 30) {
//                    //                        message = "图像模糊";
//                    //                    } else if (mxFaceInfoEx.eyeDistance < 30) {
//                    //                        message = "请靠近摄像头";
//                    //                    } else if (mxFaceInfoEx.quality < ValueUtil.DEFAULT_QUALITY_SCORE) {
//                    //                        message = "人脸质量过低";
//                    //                    }
//                    //                    if (!TextUtils.isEmpty(message)) {
//                    //                        sendEvent(new VerifyEvent(message));
//                    //                    }
//                    Intermediary intermediary = new Intermediary();
//                    intermediary.width = ZOOM_WIDTH;
//                    intermediary.height = ZOOM_HEIGHT;
//                    intermediary.mxFaceInfoEx = new MXFaceInfoEx(mxFaceInfoEx);
//                    intermediary.data = zoomedRgbData;
//                    intermediaryData = intermediary;
//                    nova = true;
//                    Log.e("asd", "检测耗时" + (System.currentTimeMillis() - time) + "-----" + mxFaceInfoEx.quality);
//                }
//            } else {
//                sendEvent(new VerifyEvent("未检测到人脸"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public Bitmap adjustPhotoRotation(Bitmap bitmap, int orientationDegree) {
        Matrix matrix = new Matrix();
        matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2,
                (float) bitmap.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bitmap.getHeight();
            targetY = 0;
        } else {
            targetX = bitmap.getHeight();
            targetY = bitmap.getWidth();
        }

        final float[] values = new float[9];
        matrix.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        matrix.postTranslate(targetX - x1, targetY - y1);

        Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(),
                Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(canvasBitmap);
        canvas.drawBitmap(bitmap, matrix, paint);
        return canvasBitmap;
    }

    private void intermediaryDataLoop() {
        try {
            if (nova && intermediaryData != null) {
                nova = false;
                extract(intermediaryData);
                intermediaryData = null;
            } else {
                Thread.sleep(50);
                asyncExtractHandler.sendEmptyMessage(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            asyncExtractHandler.sendEmptyMessage(0);
        }
    }

    private void extract(Intermediary intermediary) {
        try {
            if (needNextFeature) {
                //                if (intermediary.mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getQualityScore()) {
                if (intermediary.mxFaceInfoEx.quality > ValueUtil.DEFAULT_QUALITY_SCORE) {
                    boolean result = detectMask(intermediary.data, ZOOM_WIDTH, ZOOM_HEIGHT, intermediary.mxFaceInfoEx);
                    if (result) {
                        //                        boolean mask = intermediary.mxFaceInfoEx.mask > ConfigManager.getInstance().getConfig().getMaskScore();
                        boolean mask = intermediary.mxFaceInfoEx.mask > ValueUtil.DEFAULT_MASK_SCORE;
                        byte[] feature;
                        if (mask) {
                            feature = extractMaskFeature(intermediary.data, ZOOM_WIDTH, ZOOM_HEIGHT, intermediary.mxFaceInfoEx);
                        } else {
                            feature = extractFeature(intermediary.data, ZOOM_WIDTH, ZOOM_HEIGHT, intermediary.mxFaceInfoEx);
                        }
                        if (feature != null) {
                            needNextFeature = false;
                            if (featureListener != null) {
                                featureListener.onFeatureExtract(new MxRGBImage(intermediary.data, ZOOM_WIDTH, ZOOM_HEIGHT),
                                        intermediary.mxFaceInfoEx,
                                        feature,
                                        mask);
                            }
                        }
                    }
                } else {
                    sendEvent(new DrawRectEvent(-1, null));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnFeatureExtractListener {
        void onFeatureExtract(MxRGBImage mxRGBImage, MXFaceInfoEx mxFaceInfoEx, byte[] feature, boolean mask);
    }

    public void setFeatureListener(OnFeatureExtractListener featureListener) {
        this.featureListener = featureListener;
    }

    /**
     * 比对特征，人证比对0.7，人像比对0.8
     *
     * @param alpha
     * @param beta
     * @return
     */
    public float matchFeature(byte[] alpha, byte[] beta) {
        if (alpha != null && beta != null) {
            float[] score = new float[1];
            int re = mxFaceAPI.mxFeatureMatch(alpha, beta, score);
            if (re == 0) {
                return score[0];
            }
            return -1;
        }
        return 0;
    }

    /**
     * 比对口罩人脸特征
     *
     * @param alpha
     * @param beta
     * @return
     */
    public float matchMaskFeature(byte[] alpha, byte[] beta) {
        if (alpha != null && beta != null) {
            float[] score = new float[1];
            int re = mxFaceAPI.mxMaskFeatureMatch(alpha, beta, score);
            if (re == 0) {
                return score[0];
            }
            return -1;
        }
        return 0;
    }

    public PhotoFaceFeature getCardFaceFeatureByBitmapPosting(Bitmap bitmap) {
        String message = "";
        if (bitmap==null){
            message = "图片为空";
            return new PhotoFaceFeature(message);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            message = "图片转码失败";
            return new PhotoFaceFeature(message);
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result && pFaceNum[0] > 0) {
            if (pFaceNum[0] == 1) {
                //                result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
                //                if (result && mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getRegisterQualityScore()) {
                byte[] faceFeature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                if (faceFeature != null) {
                    byte[] maskFaceFeature = extractMaskFeatureForRegister(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                    if (maskFaceFeature != null) {
                        return new PhotoFaceFeature(faceFeature, maskFaceFeature, "提取成功");
                    }
                } else {
                    message = "提取特征失败";
                }
                //                } else {
                //                    message = "人脸质量过低";
                //                }
            } else {
                message = "检测到多张人脸";
            }
        } else {
            message = "未检测到人脸";
        }
        return new PhotoFaceFeature(message);
    }

    public PhotoFaceFeature getPhotoFaceFeatureByBitmapForRegisterPosting(Bitmap bitmap) {
        String message = "";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] rgbData = imageFileDecode(outputStream.toByteArray(), bitmap.getWidth(), bitmap.getHeight());
        if (rgbData == null) {
            message = "图片转码失败";
            return new PhotoFaceFeature(message);
        }
        int[] pFaceNum = new int[]{0};
        MXFaceInfoEx[] pFaceBuffer = makeFaceContainer(MAX_FACE_NUM);
        boolean result = faceDetect(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum, pFaceBuffer);
        if (result && pFaceNum[0] > 0) {
            if (pFaceNum[0] == 1) {
                result = faceQuality(rgbData, bitmap.getWidth(), bitmap.getHeight(), pFaceNum[0], pFaceBuffer);
                MXFaceInfoEx mxFaceInfoEx = sortMXFaceInfoEx(pFaceBuffer);
                //                if (result && mxFaceInfoEx.quality > ConfigManager.getInstance().getConfig().getRegisterQualityScore()) {
                //                if (result && mxFaceInfoEx.quality > ValueUtil.DEFAULT_REGISTER_QUALITY_SCORE) {
                byte[] faceFeature = extractFeature(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                if (faceFeature != null) {
                    byte[] maskFaceFeature = extractMaskFeatureForRegister(rgbData, bitmap.getWidth(), bitmap.getHeight(), mxFaceInfoEx);
                    if (maskFaceFeature != null) {
                        return new PhotoFaceFeature(faceFeature, maskFaceFeature, "提取成功");
                    }
                } else {
                    message = "提取特征失败";
                }
                //                } else {
                //                    message = "人脸质量过低";
                //                }
            } else {
                message = "检测到多张人脸";
            }
        } else {
            message = "未检测到人脸";
        }
        return new PhotoFaceFeature(message);
    }

    public byte[] imageEncode(byte[] rgbBuf, int width, int height) {
        byte[] fileBuf = new byte[width * height * 4];
        int[] fileLength = new int[]{0};
        int re = dtTool.ImageEncode(rgbBuf, width, height, ".png", fileBuf, fileLength);
        if (re == 1 && fileLength[0] != 0) {
            byte[] fileImage = new byte[fileLength[0]];
            System.arraycopy(fileBuf, 0, fileImage, 0, fileImage.length);
            return fileImage;
        } else {
            return null;
        }
    }

    /**
     * 图像文件解码成RGB裸数据
     *
     * @param data
     * @param width
     * @param height
     * @return
     */
    public byte[] imageFileDecode(byte[] data, int width, int height) {
        byte[] rgbData = new byte[width * height * 3];
        int[] oX = new int[1];
        int[] oY = new int[1];
        if (data==null||data.length<=0){
            return null;
        }
        int result = dtTool.ImageDecode(data, data.length, rgbData, oX, oY);
        if (result > 0) {
            return rgbData;
        }
        return null;
    }

    //Todo:BUG HERE
    public Bitmap tailoringFace(Bitmap bitmap, int radioX, int radioY, MXFaceInfoEx mxFaceInfoEx) throws IllegalArgumentException {
        int tailoringWidth = CameraManager.PIC_HEIGHT * radioX / radioY;
        int startX = mxFaceInfoEx.x - ((tailoringWidth - mxFaceInfoEx.width) / 2);
        if (startX < 0) {
            startX = 0;
        } else if (startX + tailoringWidth > CameraManager.PIC_WIDTH) {
            startX = CameraManager.PIC_WIDTH - tailoringWidth;
        }
        return Bitmap.createBitmap(bitmap, startX, 0, tailoringWidth, CameraManager.PIC_HEIGHT);//截取
    }

    /**
     * 摄像头预览数据转换
     *
     * @param data        摄像头onPreviewFrame-data
     * @param width       摄像头实际分辨率-宽
     * @param height      摄像头实际分辨率-高
     * @param orientation 旋转角度
     * @param zoomWidth   实际分辨率旋转压缩后的宽度
     * @param zoomHeight  实际分辨率旋转压缩后的高度
     * @return
     */
    private byte[] cameraPreviewConvert(byte[] data, int width, int height, int orientation, int zoomWidth, int zoomHeight) {
        // 原始YUV数据转换RGB裸数据
        byte[] rgbData = new byte[width * height * 3];
        dtTool.YUV2RGB(data, width, height, rgbData);
        int[] rotateWidth = new int[1];
        int[] rotateHeight = new int[1];
        // 旋转相应角度
        int re = dtTool.ImageRotate(rgbData, width, height, orientation, rgbData, rotateWidth, rotateHeight);
        if (re != 1) {
            Log.e("asd", "旋转失败");
            return null;
        }
        //镜像后画框位置按照正常坐标系，不镜像的话按照反坐标系也可画框
        re = dtTool.ImageFlip(rgbData, rotateWidth[0], rotateHeight[0], 1, rgbData);
        if (re != 1) {
            Log.e("asd", "镜像失败");
            return null;
        }
        // RGB数据压缩到指定宽高
        byte[] zoomedRgbData = new byte[zoomWidth * zoomHeight * 3];
        re = dtTool.Zoom(rgbData, rotateWidth[0], rotateHeight[0], 3, zoomWidth, zoomHeight, zoomedRgbData);
        if (re != 1) {
            Log.e("asd", "压缩失败");
            return null;
        }
        return zoomedRgbData;
    }

    /**
     * 摄像头预览数据转换
     *
     * @param data        摄像头onPreviewFrame-data
     * @param width       摄像头实际分辨率-宽
     * @param height      摄像头实际分辨率-高
     * @param orientation 旋转角度
     * @param zoomWidth   实际分辨率旋转压缩后的宽度
     * @param zoomHeight  实际分辨率旋转压缩后的高度
     * @return
     */
    private byte[] cameraPreviewConvertWithFlip(byte[] data, int width, int height, int orientation, int zoomWidth, int zoomHeight) {
        // 原始YUV数据转换RGB裸数据
        byte[] rgbData = new byte[width * height * 3];
        dtTool.YUV2RGB(data, width, height, rgbData);
        int[] rotateWidth = new int[1];
        int[] rotateHeight = new int[1];
        // 旋转相应角度
        int re = dtTool.ImageRotate(rgbData, width, height, orientation, rgbData, rotateWidth, rotateHeight);
        if (re != 1) {
            Log.e("asd", "旋转失败");
            return null;
        }
        //镜像后画框位置按照正常坐标系，不镜像的话按照反坐标系也可画框
        re = dtTool.ImageFlip(rgbData, rotateWidth[0], rotateHeight[0], 1, rgbData);
        if (re != 1) {
            Log.e("asd", "镜像失败");
            return null;
        }
        // RGB数据压缩到指定宽高
        byte[] zoomedRgbData = new byte[zoomWidth * zoomHeight * 3];
        re = dtTool.Zoom(rgbData, rotateWidth[0], rotateHeight[0], 3, zoomWidth, zoomHeight, zoomedRgbData);
        if (re != 1) {
            Log.e("asd", "压缩失败");
            return null;
        }
        return zoomedRgbData;
    }

    /**
     * 组装人脸信息存储容器数组
     *
     * @param size
     * @return
     */
    private MXFaceInfoEx[] makeFaceContainer(int size) {
        MXFaceInfoEx[] pFaceBuffer = new MXFaceInfoEx[size];
        for (int i = 0; i < size; i++) {
            pFaceBuffer[i] = new MXFaceInfoEx();
        }
        return pFaceBuffer;
    }

    /**
     * 检测人脸信息
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    native输出，检测到的人脸数量
     * @param faceBuffer native输出，人脸信息
     * @return true - 算法执行成功，并且检测到人脸，false - 算法执行失败，或者执行成功但是未检测到人脸
     */
    private boolean faceDetect(byte[] rgbData, int width, int height, int[] faceNum, MXFaceInfoEx[] faceBuffer) {
        synchronized (lock2) {
            int result = mxFaceAPI.mxDetectFace(rgbData, width, height, faceNum, faceBuffer);
            return result == 0 && faceNum[0] > 0;
        }
    }

    /**
     * 口罩检测
     *
     * @param rgbData  RGB裸图像数据
     * @param width    图像数据宽度
     * @param height   图像数据高度
     * @param faceInfo 输入，人脸检测结果
     * @return
     */
    private boolean detectMask(byte[] rgbData, int width, int height, MXFaceInfoEx faceInfo) {
        int result = mxFaceAPI.mxMaskDetect(rgbData, width, height, 1, new MXFaceInfoEx[]{faceInfo});
        return result == 0;
    }

    /**
     * 人脸质量检测
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    检测到人脸数量
     * @param faceBuffer 输入，人脸检测结果，native输出，根据瞳距进行从大到小排序
     * @return
     */
    private boolean faceQuality(byte[] rgbData, int width, int height, int faceNum, MXFaceInfoEx[] faceBuffer) {
        int result = mxFaceAPI.mxFaceQuality(rgbData, width, height, faceNum, faceBuffer);
        return result == 0;
    }

    /**
     * 红外活体检测
     *
     * @param rgbData    RGB裸图像数据
     * @param width      图像数据宽度
     * @param height     图像数据高度
     * @param faceNum    检测到人脸数量
     * @param faceBuffer 输入，人脸检测结果，native输出，根据瞳距进行从大到小排序
     * @return
     */
    private boolean infraredLivenessDetect(byte[] rgbData, int width, int height, int faceNum, MXFaceInfoEx faceBuffer) {
        int result = mxFaceAPI.mxNIRLivenessDetect(rgbData, width, height, faceNum, new MXFaceInfoEx[]{faceBuffer});
        return result == 0;
    }

    /**
     * RGB裸图像数据提取人脸特征
     *
     * @param pImage
     * @param width
     * @param height
     * @param faceInfo
     * @return
     */
    private byte[] extractFeature(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxFeatureExtract(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    /**
     * @param pImage   - 输入，RGB图像数据
     * @param width    - 输入，图像宽度
     * @param height   - 输入，图像高度
     * @param faceInfo - 输入，人脸信息
     * @return 0-成功，其他-失败
     * @category 人脸特征提取, 用于比对（戴口罩算法）
     */
    public byte[] extractMaskFeature(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxMaskFeatureExtract(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    /**
     * @param pImage   - 输入，RGB图像数据
     * @param width    - 输入，图像宽度
     * @param height   - 输入，图像高度
     * @param faceInfo - 输入，人脸信息
     * @return 0-成功，其他-失败
     * @category 人脸特征提取, 用于注册（戴口罩算法）
     */
    public byte[] extractMaskFeatureForRegister(byte[] pImage, int width, int height, MXFaceInfoEx faceInfo) {
        synchronized (lock1) {
            byte[] feature = new byte[mxFaceAPI.mxGetFeatureSize()];
            int result = mxFaceAPI.mxMaskFeatureExtract4Reg(pImage, width, height, 1, new MXFaceInfoEx[]{faceInfo}, feature);
            return result == 0 ? feature : null;
        }
    }

    private MXFaceInfoEx sortMXFaceInfoEx(MXFaceInfoEx[] mxFaceInfoExList) {
        MXFaceInfoEx maxMXFaceInfoEx = mxFaceInfoExList[0];
        for (MXFaceInfoEx mxFaceInfoEx : mxFaceInfoExList) {
            if (mxFaceInfoEx.width > maxMXFaceInfoEx.width) {
                maxMXFaceInfoEx = mxFaceInfoEx;
            }
        }
        return maxMXFaceInfoEx;
    }

    private static double calculationPupilDistance(MXFaceInfoEx mxFaceInfoEx) {
        int a = mxFaceInfoEx.keypt_x[1] - mxFaceInfoEx.keypt_x[0];
        int b = mxFaceInfoEx.keypt_y[1] - mxFaceInfoEx.keypt_y[0];
        double pow = Math.pow(a, 2) + Math.pow(b, 2);
        return Math.sqrt(pow);
    }

}
