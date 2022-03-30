package com.miaxis.bp_entry.manager.fingerPower;

import android.os.SystemClock;

import com.miaxis.bp_entry.manager.GpioManager;


/**
 * BP990_FingerPower
 *
 * @author zhangyw
 * Created on 2021/3/21.
 */
public class BP990_FingerPower implements IFingerPower {

    @Override
    public void powerOn() {
        openDevice();
    }

    @Override
    public void powerOff() {
        closeDevice();
    }

    private void openDevice() {
        {
            //            if (!GpioManager.getInstance().getCardDevicePowerStatus()) {
            GpioManager.getInstance().fingerDevicePowerControl(true);
            SystemClock.sleep(500);
            //            }
        }
    }

    private void closeDevice() {
       {
            //            if (GpioManager.getInstance().getCardDevicePowerStatus()) {
            GpioManager.getInstance().fingerDevicePowerControl(false);
            SystemClock.sleep(200);
            //            }
        }
    }

}
