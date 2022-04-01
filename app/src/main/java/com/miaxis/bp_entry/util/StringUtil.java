package com.miaxis.bp_entry.util;

import android.util.Log;

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
}
