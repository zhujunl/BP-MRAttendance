package com.miaxis.bp_entry.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;


public class ValueUtil {

    public final static Gson GSON = new Gson();

    //    public static final String DEFAULT_BASE_HOST = "http://192.168.5.125:8088/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://192.168.5.94:8080/policebus/";
//    public static final String DEFAULT_BASE_HOST = "http://14.205.75.23:8089/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://192.168.11.185:8080/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://192.168.5.164:8080/policebus/";

        public static final String DEFAULT_BASE_HOST = "/14.205.75.23:8089/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://14.205.75.23:8089/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://192.168.11.185:8080/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://192.168.5.164:8080/policebus/";
    //    public static final String DEFAULT_BASE_HOST = "http://bnrzhysj.postaldata.top:8800/policebus/";
    public static final float DEFAULT_VERIFY_SCORE = 0.73f;
    public static final float DEFAULT_MASK_VERIFY_SCORE = 0.73f;
    public static final int DEFAULT_QUALITY_SCORE = 80;
    public static final int DEFAULT_REGISTER_QUALITY_SCORE = 75;
    public static final int DEFAULT_MASK_SCORE = 40;
    public static final int DEFAULT_HEART_BEAT_INTERVAL = 10 * 60;
    public static final int LOGIN_MODE_FINGER = 1;//1:仅指纹，2:仅人脸
    public static final int LOGIN_MODE_FACE = 2;//1:仅指纹，2:仅人脸
    public static final int DEFAULT_LOGIN_MODE = LOGIN_MODE_FINGER;

    public static final int ERROR_COUNT=5;//核对失败的次数

    public static final String SUCCESS = "200";
    public static final String DEVICE_ENABLE = "00601";
    public static final String DEVICE_UNABLE = "00602";
    public static final int PAGE_SIZE = 8;

    public static String GlobalPhone = null;

    public static boolean isNetException(Throwable throwable) {
        if (throwable instanceof SocketTimeoutException
                || throwable instanceof ConnectException
                || throwable instanceof HttpException
                || throwable instanceof com.jakewharton.retrofit2.adapter.rxjava2.HttpException) {
            return true;
        }
        return false;
    }

    public static String getCurVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String fingerPositionCovert(byte finger) {
        switch ((int) finger) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "其他不确定指位";
        }
    }

    public static String nameDesensitization(String name) {
        if (TextUtils.isEmpty(name))
            return "";
        String myName = null;
        char[] chars = name.toCharArray();
        if (chars.length == 1) {
            myName = name;
        }
        if (chars.length == 2) {
            myName = name.replaceFirst(name.substring(1), " *");
        }
        if (chars.length > 2) {
            //            myName = name.replaceAll(name.substring(1, chars.length - 1), "*");
            StringBuilder cache = new StringBuilder(name.substring(0, 1));
            for (int i = 0; i < chars.length - 1; i++) {
                cache.append(" *");
            }
            myName = cache.toString();
        }
        return myName;
    }

    //身份证前三后四脱敏
    public static String cardNumberDesensitization(String id) {
        if (TextUtils.isEmpty(id) || (id.length() < 8)) {
            return id;
        }
        return id.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
    }

}
