package com.miaxis.bp_entry.event;

import org.zz.api.MXFaceInfoEx;

public class DrawRectEvent {

    private int faceNum;
    private MXFaceInfoEx[] faceInfos;

    public DrawRectEvent(int faceNum, MXFaceInfoEx[] faceInfos) {
        this.faceNum = faceNum;
        this.faceInfos = faceInfos;
    }

    public int getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(int faceNum) {
        this.faceNum = faceNum;
    }

    public MXFaceInfoEx[] getFaceInfos() {
        return faceInfos;
    }

    public void setFaceInfos(MXFaceInfoEx[] faceInfos) {
        this.faceInfos = faceInfos;
    }
}
