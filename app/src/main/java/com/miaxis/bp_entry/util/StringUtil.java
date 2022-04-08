package com.miaxis.bp_entry.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * @author ZJL
 * @date 2022/3/30 16:53
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StringUtil {
    public static String byte2Str(byte[] data){
        StringBuilder builder = new StringBuilder();
        if (data!=null){
            for (int i = 0; i < data.length; i++) {
                builder.append(String.format("%02x ",data[i]));
            }
        }
        return builder.toString();
    }

    public static String getPlace(byte[] data){
        int cut=0;
        for (int i = 0; i < data.length; i++) {
            if (data[i]==0x2d){
                cut=i;
                break;
            }
        }
        Log.e("StringUtil", "cut:" + cut);
        return new String( data,7,cut-7);
    }

    public static String getCode(byte[] data){
        int cut=0;
        for (int i = 0; i < data.length; i++) {
            if (data[i]==0x2d){
                cut=i;
                break;
            }
        }
        return new String( data,cut+1,data.length-cut-2);
    }

    public static byte[] HighToLow(byte[] data){
        byte[] bytes=new byte[data.length];
        for (int i = 0; i < data.length-1; i=i+2) {
            byte b=bytes[i];
            bytes[i]=bytes[i+1];
            bytes[i+1]=b;
        }
        return bytes;
    }

    public static Bitmap stringToBitmap(String string){
        //数据库中的String类型转换成Bitmap
        Bitmap bitmap=null;
        if(string!=null){
            byte[] bytes= Base64.decode(string,Base64.DEFAULT);
            bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            return bitmap;
        }
        else {
            return null;
        }
    }
    public static String bitmapToString(Bitmap bitmap){
        //用户在活动中上传的图片转换成String进行存储
        String string=null;
        if(bitmap!=null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bytes = stream.toByteArray();// 转为byte数组
            string=Base64.encodeToString(bytes,Base64.DEFAULT);
            return string;
        }
        else{
            return "";
        }
    }
}
