//package com.miaxis.bp_entry.manager.fingerPower;
//
//import android.serialport.DeviceControlSpd;
//
//import java.io.IOException;
//
///**
// * BP990_FingerPower
// *
// * @author zhangyw
// * Created on 2021/3/21.
// */
//public class BP990s_FingerPower implements IFingerPower {
//
//
//    private DeviceControlSpd deviceControl;
//
//    public BP990s_FingerPower() {
//        try {
//            deviceControl = new DeviceControlSpd(DeviceControlSpd.PowerType.MAIN, 93, 63);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void powerOn() {
//        try {
//            if (deviceControl != null)
//                deviceControl.PowerOnDevice();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void powerOff() {
//        try {
//            if (deviceControl != null)
//                deviceControl.PowerOffDevice();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
