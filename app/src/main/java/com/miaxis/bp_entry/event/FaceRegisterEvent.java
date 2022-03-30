package com.miaxis.bp_entry.event;

import android.graphics.Bitmap;

public class FaceRegisterEvent {

    private String feature;
    private String maskFeature;
    private Bitmap bitmap;

    public FaceRegisterEvent(String feature, String maskFeature, Bitmap bitmap) {
        this.feature = feature;
        this.maskFeature = maskFeature;
        this.bitmap = bitmap;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getMaskFeature() {
        return maskFeature;
    }

    public void setMaskFeature(String maskFeature) {
        this.maskFeature = maskFeature;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
