package com.miaxis.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

/**
 * @author Tank
 * @date 2021/8/23 7:17 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StringUtils {

    public static boolean isNull(String string) {
        return string == null;
    }

    public static boolean isNullOrEmpty(String string) {
        return TextUtils.isEmpty(string) || TextUtils.isEmpty(string.trim());
    }
    public static String unicodetoString(String unicode) {
        if (unicode == null || "".equals(unicode)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;
        while ((i = unicode.indexOf("\\u", pos)) != -1) {
            sb.append(unicode.substring(pos, i));
            if (i + 5 < unicode.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16));
            }
        }
        return sb.toString();
    }
    //编码
    public static String stringtoUnicode(String string) {
        if (string == null || "".equals(string)) {
            return null;
        }
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
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
}
