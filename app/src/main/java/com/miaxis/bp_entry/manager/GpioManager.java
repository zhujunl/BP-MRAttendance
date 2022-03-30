package com.miaxis.bp_entry.manager;

import android.app.Application;

import com.miaxis.bp_entry.manager.bp990.BP900GpioStrategy;


public class GpioManager {

    private GpioManager() {
    }

    public static GpioManager getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static final GpioManager instance = new GpioManager();
    }

    /**
     * ================================ 静态内部类单例 ================================
     **/

    private GpioStrategy gpioStrategy = new BP900GpioStrategy();

    public void init() {
        //gpioStrategy = new BP900GpioStrategy();
//        if (ValueUtil.SIGN == Sign.DEFAULT) {
//
//        }
    }

    public void initGpio(Application application) {
        if (gpioStrategy != null) {
            gpioStrategy.initGpio(application);
        }
    }

    public void resetGpio() {
        if (gpioStrategy != null) {
            gpioStrategy.resetGpio();
        }
    }

    public void setGpio(int gpio, boolean status) {
        if (gpioStrategy != null) {
            gpioStrategy.setGpio(gpio, status);
        }
    }

    public void cardDevicePowerControl(boolean status) {
        if (gpioStrategy != null) {
            gpioStrategy.cardDevicePowerControl(status);
        }
    }

    public void fingerDevicePowerControl(boolean status) {
        if (gpioStrategy != null) {
            gpioStrategy.fingerDevicePowerControl(status);
        }
    }

    @Deprecated
    public void scanDevicePowerControl(boolean status) {
        if (gpioStrategy != null) {
            gpioStrategy.scanDevicePowerControl(status);
        }
    }

    public boolean getCardDevicePowerStatus() {
        if (gpioStrategy != null) {
            return gpioStrategy.getCardDevicePowerStatus();
        }
        return false;
    }

    public boolean getFingerDevicePowerStatus() {
        if (gpioStrategy != null) {
            return gpioStrategy.getFingerDevicePowerStatus();
        }
        return false;
    }

    @Deprecated
    public boolean getScanDevicePowerStatus() {
        if (gpioStrategy != null) {
            return gpioStrategy.getScanDevicePowerStatus();
        }
        return false;
    }

    public interface GpioStrategy {
        void initGpio(Application application);
        void resetGpio();
        void setGpio(int gpio, boolean status);
        void cardDevicePowerControl(boolean status);
        boolean getCardDevicePowerStatus();
        void fingerDevicePowerControl(boolean status);
        boolean getFingerDevicePowerStatus();
        void scanDevicePowerControl(boolean status);
        boolean getScanDevicePowerStatus();
    }

}
