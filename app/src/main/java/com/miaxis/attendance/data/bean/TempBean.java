package com.miaxis.attendance.data.bean;

public class TempBean {
    private String time;
    private Long cpu;
    private Long battery;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getCpu() {
        return cpu;
    }

    public void setCpu(Long cpu) {
        this.cpu = cpu;
    }

    public Long getBattery() {
        return battery;
    }

    public void setBattery(Long battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return
                "time='" + time + '\'' +
                ", cpu='" + cpu + '\'' +
                ", battery='" + battery + '\'' ;
    }
}
