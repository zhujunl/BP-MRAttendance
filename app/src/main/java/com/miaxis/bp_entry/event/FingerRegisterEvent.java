package com.miaxis.bp_entry.event;

public class FingerRegisterEvent {

    private String mark;
    private String feature;
    private byte[] template;

    public FingerRegisterEvent(String mark, String feature, byte[] template) {
        this.mark = mark;
        this.feature = feature;
        this.template = template;
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

    public byte[] getTemplate() {
        return template;
    }

    public void setTemplate(byte[] template) {
        this.template = template;
    }
}
