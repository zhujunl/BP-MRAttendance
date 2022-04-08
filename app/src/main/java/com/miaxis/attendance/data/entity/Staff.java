package com.miaxis.attendance.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author ZJL
 * @date 2022/4/1 17:11
 * @des
 * @updateAuthor
 * @updateDes
 */
@Entity
public class Staff {
    @PrimaryKey(autoGenerate = true)

    private long id;

    private String place;
    private String code;
    private byte[] faceFeature;
    private byte[] finger0;
    private byte[] finger1;


    public long create_time;//创建时间
    public long update_time;//修改时间

    public Staff() {
        this.create_time = System.currentTimeMillis();
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

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "id=" + id +
                ", place='" + place + '\'' +
                ", code='" + code + '\'' +
//                ", faceFeature='" + faceFeature+ '\'' +
                ", finger0='" + finger0.toString() + '\'' +
                ", finger1='" + finger1.toString() + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                '}';
    }
}
