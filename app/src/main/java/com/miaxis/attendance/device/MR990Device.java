package com.miaxis.attendance.device;

import android.annotation.SuppressLint;
import android.content.Intent;

import com.miaxis.attendance.App;


/**
 * @author Tank
 * @date 2021/8/27 1:16 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class MR990Device {

    private MR990Device() {
    }

    protected static class Single {
        @SuppressLint("StaticFieldLeak")
        public static MR990Device mr990Device = new MR990Device();
    }

    public static MR990Device getInstance() {
        return Single.mr990Device;
    }

    /**
     * 摄像头上下电
     */
    public void CameraPower(boolean enable) {
        DebugPower(false);
        setPower(0x11, enable);
        //SystemClock.sleep(1000);
    }

    /**
     * 门禁继电器上下电
     */
    public void DoorPower(boolean enable) {
        setPower(0x14, enable);
    }

    /**
     * usb调试上下电
     */
    public void DebugPower(boolean enable) {
        setPower(0x01, enable);
    }


    /**
     * 指纹二代证上下电
     */
    public void FingerPower(boolean enable) {
        setPower(0x12, enable);
    }

    /**
     * 指纹二代证上下电
     */
    public void EthernetPower(boolean enable) {
        setPower(0x13, enable);
    }

    /**
     * LED灯上下电
     */
    public void LedPower(boolean enable) {
//        setPower(0x20, enable);// LED 绿⾊
//        setPower(0x21, enable);// LED 红⾊
//        setPower(0x22, enable);// LED 蓝⾊
//        mr990Driver.zzLightControl(enable?1:0,0);
    }

    private void setPower(int type, boolean enable) {
        Intent intent = new Intent("com.miaxis.power");
        intent.putExtra("type", type);
        intent.putExtra("value", enable);
        App.getInstance().sendBroadcast(intent);
    }

    /**
     * 开启USB调试
     */
    public void setUsbDebug(boolean enable) {
        setPower(0x01, enable);
    }

    /**
     * 开启WIFI调试
     */
    public void setWifiDebug(boolean enable) {
        Intent intent = new Intent("com.miaxis.debug.wifi");
        intent.putExtra("value", enable);
        App.getInstance().sendBroadcast(intent);
    }
}
