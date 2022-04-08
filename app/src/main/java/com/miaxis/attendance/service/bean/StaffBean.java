package com.miaxis.attendance.service.bean;

/**
 * @author ZJL
 * @date 2022/3/31 9:12
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffBean {

    private long id;

    private String place;
    private String code;
    private byte[] faceFeature;
    private byte[] finger0;
    private byte[] finger1;


    public StaffBean() {
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public byte[] getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(byte[] faceFeature) {
        this.faceFeature = faceFeature;
    }

    public byte[] getFinger0() {
        return finger0;
    }

    public void setFinger0(byte[] finger0) {
        this.finger0 = finger0;
    }

    public byte[] getFinger1() {
        return finger1;
    }

    public void setFinger1(byte[] finger1) {
        this.finger1 = finger1;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", place='" + place + '\'' +
                ", code='" + code + '\'' +
                ", faceFeature='" + faceFeature+ '\'' +
                ", finger0='" + finger0 + '\'' +
                ", finger1='" + finger1 + '\'' +
                '}';
    }

}
