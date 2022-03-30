package com.miaxis.bp_entry.manager.bp990;

import android.app.Application;
import android.os.SystemClock;
import android.util.Log;

import com.miaxis.bp_entry.manager.GpioManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BP900GpioStrategy implements GpioManager.GpioStrategy {

    private static final String TAG = "bp990 gpio";

    private boolean usbStatus = false;

    @Override
    public void initGpio(Application application) {
        usbStatus = getFileValue("/sys/nexgo/usb/enable_micro_usb");
    }

    @Override
    public void resetGpio() {
        cardDevicePowerControl(false);
        fingerDevicePowerControl(false);
        scanDevicePowerControl(false);
    }

    @Override
    public void setGpio(int gpio, boolean status) {

    }

    @Override
    public void cardDevicePowerControl(boolean status) {
        writeFile("/sys/nexgo/power_ctl/idcard_pwr_en", status ? "1" : "0");
    }

    @Override
    public void fingerDevicePowerControl(boolean status) {
        if (status) {
            usbStatus = getFileValue("/sys/nexgo/usb/enable_micro_usb");
            setFileValue("/sys/nexgo/usb/enable_micro_usb", "0");
        } else {
            setFileValue("/sys/nexgo/usb/enable_micro_usb", usbStatus ? "1" : "0");
        }
        SystemClock.sleep(200);
        writeFile("/sys/nexgo/power_ctl/finger_pwr_en", status ? "1" : "0");
    }

    @Override
    public void scanDevicePowerControl(boolean status) {
        writeFile("/sys/nexgo/power_ctl/scan_power_en", status ? "1" : "0");
        writeFile("/sys/nexgo/power_ctl/scan_trip_en", status ? "1" : "0");
    }

    @Override
    public boolean getCardDevicePowerStatus() {
        return getDeviceStatus("/sys/nexgo/power_ctl/idcard_pwr_en");
    }

    @Override
    public boolean getFingerDevicePowerStatus() {
        return getDeviceStatus("/sys/nexgo/power_ctl/finger_pwr_en");
    }

    @Override
    public boolean getScanDevicePowerStatus() {
        return getDeviceStatus("/sys/nexgo/power_ctl/scan_power_en");
        //&& getDeviceStatus("/sys/nexgo/power_ctl/scan_trip_en");
    }

    private void writeFile(String filePath, String writeData) {
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(writeData);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "writeFile: ", e);
        }
    }

    private boolean getDeviceStatus(String filePath) {
        String status = "";
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader localBufferedReader = new BufferedReader(fr, 2);
            status = localBufferedReader.readLine();
            localBufferedReader.close();
            fr.close();
        } catch (IOException e) {
            Log.e(TAG, "get" + filePath + "DeviceStatus: ", e);
        }
        return status.toUpperCase().contains("ON");
    }

    public static boolean getFileValue(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        byte[] read_buf = new byte[1];
        try {
            FileInputStream fis = new FileInputStream(path);
            fis.read(read_buf);
            fis.close();
            // 发现 驱动里读取到的是字符 '0' 或者 '1',
            // 因此 这里读取到的是 字符的 ascii;
            return (read_buf[0] - 48) != 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * 写数据;
     *
     *  写失败, 返回 false;
     *  写成功, 返回 true;
     */
    public static boolean setFileValue(String path, String value) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(value.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}
