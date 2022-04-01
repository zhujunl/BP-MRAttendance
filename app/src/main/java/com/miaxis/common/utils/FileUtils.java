package com.miaxis.common.utils;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tank
 * @date 2021/5/11 13:29
 * @des
 * @updateAuthor
 * @updateDes
 */
public class FileUtils {


    public static boolean initFile(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                boolean mkdirs = parentFile.mkdirs();
            }
        }
        return true;
    }


    /**
     * 把数据流写入文件
     *
     * @param path
     * @param bytes
     */
    public static boolean writeFile(String path, byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        boolean b = initFile(path);
        if (!b) {
            return false;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);//指定写到哪个路径中
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static byte[] File2Bytes(File file) {
        int byte_size = 1024;
        byte[] b = new byte[byte_size];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(byte_size);
            for (int length; (length = fileInputStream.read(b)) != -1; ) {
                outputStream.write(b, 0, length);
            }
            fileInputStream.close();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void delete(String path) {
        try {
            boolean delete = new File(path).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete(List<String> paths) {
        for (String path : paths) {
            delete(path);
        }
    }

    public static void delete(HashMap<String, ?> paths) {
        if (paths != null && !paths.isEmpty()) {
            for (Map.Entry<String, ?> entry : paths.entrySet()) {
                delete(entry.getKey());
            }
        }
    }

    /**
     * 下载文件
     *
     * @param fileUrl 下载路径
     */
    public static boolean download(String fileUrl, String savePath) {
        boolean b = initFile(savePath);
        if (!b) {
            return false;
        }
        if (TextUtils.isEmpty(fileUrl)) {
            return false;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            File temp = new File(savePath + ".temp"); // 下载文件路径
            byte[] bs = new byte[1024];
            int len;
            URL url = new URL(fileUrl);
            inputStream = url.openStream();
            outputStream = new FileOutputStream(temp);
            while ((len = inputStream.read(bs)) != -1) {
                outputStream.write(bs, 0, len);
                outputStream.flush();
            }
            return temp.renameTo(new File(savePath));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
