package com.miaxis.common.utils;

import com.miaxis.common.response.ZZResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.NonNull;

/**
 * @author Tank
 * @date 2020/9/10 14:15
 * @des
 * @updateAuthor
 * @updateDes
 */
public class DownloadClient {

    private int mBufferSize = 1024 * 4;
    private String mUrlPath;
    private String mSavePath;
    private InputStream mInputStream;
    private FileOutputStream mFileOutputStream;

    public DownloadClient() {
    }

    public DownloadClient bindDownloadInfo(@NonNull String urlPath, @NonNull String savePath) {
        this.mUrlPath = urlPath;
        this.mSavePath = savePath;
        return this;
    }

    public DownloadClient bindBufferSize(int bufferSize) {
        this.mBufferSize = bufferSize;
        return this;
    }

    private int mReadTimeOut = 0;
    private int mConnectTimeOut = 5 * 1000;

    private boolean cancel = false;

    public void stopDownload() {
        this.cancel = true;
    }

    public DownloadClient bindDownloadTimeOut(int connectTimeOut, int readTimeOut) {
        if (connectTimeOut < 0 || readTimeOut < 0) {
            return this;
        }
        this.mConnectTimeOut = connectTimeOut;
        this.mReadTimeOut = readTimeOut;
        return this;
    }

    private static final HostnameVerifier NOT_VERIYIER = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    };

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};

    public ZZResponse<?> download() {
        this.cancel = false;
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(NOT_VERIYIER);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            //获取文件名
            URL url = new URL(this.mUrlPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(this.mConnectTimeOut);
            urlConnection.setReadTimeout(this.mReadTimeOut);
            urlConnection.connect();
            if (this.cancel) {
                return ZZResponse.CreateFail(-1, "Download cancel");
            }
            this.mInputStream = urlConnection.getInputStream();
            int contentLength = urlConnection.getContentLength();//根据响应获取文件大小
            if (contentLength <= 0) {
                return ZZResponse.CreateFail(-2, "Content Length Illegal," + contentLength);
            }
            if (this.mInputStream == null) {
                return ZZResponse.CreateFail(-3, "No InputStream");
            }
            File file = new File(this.mSavePath);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    boolean mkdirs = parentFile.mkdirs();
                }
            } else {
                boolean delete = file.delete();
            }
            if (this.cancel) {
                return ZZResponse.CreateFail(-1, "Download cancel");
            }
            String tempPath = this.mSavePath + ".temp";
            //把数据存入路径+文件名
            this.mFileOutputStream = new FileOutputStream(tempPath);
            byte[] buffer = new byte[this.mBufferSize];
            int downLoadFileSize = 0;
            do {
                if (this.cancel) {
                    return ZZResponse.CreateFail(-1, "Download cancel");
                }
                //循环读取
                int num = this.mInputStream.read(buffer);
                if (num == -1) {
                    break;
                }
                this.mFileOutputStream.write(buffer, 0, num);
                //更新进度条
                downLoadFileSize += num;
            } while (true);
            if (this.cancel) {
                return ZZResponse.CreateFail(-1, "Download cancel");
            }
            boolean renameTo = new File(tempPath).renameTo(new File(this.mSavePath));
            if (renameTo) {
                return ZZResponse.CreateSuccess();
            } else {
                return ZZResponse.CreateFail(-3, "rename failed");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return ZZResponse.CreateFail(-99, "download failed," + ex.getMessage());
        } finally {
            close();
        }
    }

    private void close() {
        try {
            if (this.mInputStream != null) {
                this.mInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (this.mFileOutputStream != null) {
                this.mFileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



