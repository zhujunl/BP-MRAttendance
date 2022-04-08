package com.miaxis.bp_entry.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.TextureView;

import com.miaxis.bp_entry.app.App;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

@SuppressWarnings("deprecation")
public class CameraManager {

    private CameraManager() {
    }

    public static CameraManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final CameraManager instance = new CameraManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    public static final int PRE_WIDTH = 640;
    public static final int PRE_HEIGHT = 480;
    public static final int PIC_WIDTH = 640;
    public static final int PIC_HEIGHT = 480;
    private static final int RETRY_TIMES = 3;

    private Camera mCamera;
    private Camera frontCamera;
    private Camera backCamera;

    private int retryTime = 0;

    public interface OnCameraOpenListener {
        void onCameraOpen(Camera.Size previewSize);
    }

    public int getPreviewOrientation() {
        if (frontCamera != null) {
            return 90;
        }
        return 270;
    }

    public int getPictureOrientation() {
        if (frontCamera != null) {
            return 90;
        }
        return 90;
    }

    public void openFrontCamera(@NonNull TextureView textureView, OnCameraOpenListener listener) {
        if (frontCamera != null) return;
        if (Camera.getNumberOfCameras() == 2) {
            openBackCamera(textureView, listener);
            return;
        }
        try {
            frontCamera = Camera.open(1);
            Camera.Parameters parameters = frontCamera.getParameters();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            //对焦模式设置
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            frontCamera.setParameters(parameters);
            frontCamera.setDisplayOrientation(270);
            textureView.setSurfaceTextureListener(frontTextureListener);
            frontCamera.setPreviewCallback(previewCallback);
            frontCamera.startPreview();
            if (listener != null) {
                listener.onCameraOpen(parameters.getPreviewSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openFrontCamera(textureView, listener);
                }
            });
        }
    }

    public void closeFrontCamera() {
        if (Camera.getNumberOfCameras() == 2) {
            closeBackCamera();
            return;
        }
        try {
            if (frontCamera != null) {
                frontCamera.setPreviewCallback(null);
                frontCamera.stopPreview();
                frontCamera.release();
                frontCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Camera getFrontCamera() {
        if (Camera.getNumberOfCameras() == 2) {
            return backCamera;
        }
        return frontCamera;
    }

    private TextureView.SurfaceTextureListener frontTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (frontCamera != null) {
                try {
                    frontCamera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            closeFrontCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    public void openBackCamera(@NonNull TextureView textureView, OnCameraOpenListener listener) {
        if (backCamera != null) return;
        try {
            backCamera = Camera.open();
            Camera.Parameters parameters = backCamera.getParameters();
            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            //对焦模式设置
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
            backCamera.setParameters(parameters);
            backCamera.setDisplayOrientation(90);
            textureView.setSurfaceTextureListener(backTextureListener);
            backCamera.setPreviewCallback(previewCallback);
            backCamera.startPreview();
            if (listener != null) {
                listener.onCameraOpen(parameters.getPreviewSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openBackCamera(textureView, listener);
                }
            });
        }
    }

    public void closeBackCamera() {
        try {
            if (backCamera != null) {
                backCamera.setPreviewCallback(null);
                backCamera.stopPreview();
                backCamera.release();
                backCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Camera getBackCamera() {
        return backCamera;
    }

    private TextureView.SurfaceTextureListener backTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            if (backCamera != null) {
                try {
                    backCamera.setPreviewTexture(surfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            closeBackCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    public void resetRetryTime() {
        this.retryTime = 0;
    }

    public void openCamera(SurfaceHolder holder, OnCameraOpenListener listener) {
        try {
            mCamera = Camera.open(0);
            Camera.Parameters parameters = mCamera.getParameters();
            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            if (supportedFocusModes != null && supportedFocusModes.size() > 0) {
                if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }
//            parameters.setPreviewSize(PRE_WIDTH, PRE_HEIGHT);
//            parameters.setPictureSize(PIC_WIDTH, PIC_HEIGHT);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            if (listener != null) {
                listener.onCameraOpen(parameters.getPreviewSize());
            }
        } catch (Exception e) {
            e.printStackTrace();
            App.getInstance().getThreadExecutor().execute(() -> {
                if (retryTime <= RETRY_TIMES) {
                    retryTime++;
                    openCamera(holder, listener);
                }
            });
        }
    }

    public void closeCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Camera getmCamera() {
        return mCamera;
    }

    private Camera.PreviewCallback previewCallback = (data, camera) -> {
        FaceManager.getInstance().setLastPreviewData(data);
    };

    public static Uri getOutputMediaFileUri(Context context) {
        File mediaFile = null;
        String cameraPath;
        try {
            File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            mediaFile = new File(mediaStorageDir.getPath()
                    + File.separator
                    + "/miaxis/postal/temp.jpg");//注意这里需要和filepaths.xml中配置的一样
            cameraPath = mediaFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// sdk >= 24  android7.0以上
            Uri contentUri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider",//与清单文件中android:authorities的值保持一致
                    mediaFile);//FileProvider方式或者ContentProvider。也可使用VmPolicy方式
            return contentUri;
        } else {
            return Uri.fromFile(mediaFile);//或者 Uri.isPaise("file://"+file.toString()
        }
    }

    public static Bitmap getBitmapFormUri(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);

            //这一段代码是不加载文件到内存中也得到bitmap的真是宽高，主要是设置inJustDecodeBounds为true
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;//不加载到内存
            onlyBoundsOptions.inDither = true;//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.RGB_565;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            int originalWidth = onlyBoundsOptions.outWidth;
            int originalHeight = onlyBoundsOptions.outHeight;
            if ((originalWidth == -1) || (originalHeight == -1))
                return null;
            float hh = 640f;
            float ww = 360f;
            //缩放比，由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            int be = 1;//be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) (originalWidth / ww);
            } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (int) (originalHeight / hh);
            }
            if (be <= 0)
                be = 1;
            //比例压缩
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = be;//设置缩放比例
            bitmapOptions.inDither = true;
            bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
            return compressImage(bitmap);//再进行质量压缩
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
            if (options <= 0)
                break;
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public void takeBackPicture(Camera.PictureCallback jpeg) {
        backCamera.takePicture(null, null, jpeg);
    }


    public void startBackPreview() {
        if (backCamera!=null){
            backCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        if (backCamera!=null){
            backCamera.stopPreview();
        }
        if (frontCamera!=null){
            frontCamera.stopPreview();
        }
    }

}
