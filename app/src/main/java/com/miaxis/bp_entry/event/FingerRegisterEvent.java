package com.miaxis.bp_entry.event;

public class FingerRegisterEvent {

    private String mark;
    private String feature;

    public FingerRegisterEvent(String mark, String feature) {
        this.mark = mark;
        this.feature = feature;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }
}
