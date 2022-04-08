package com.miaxis.bp_entry.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author ZJL
 * @date 2022/3/31 9:12
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


    public Staff() {
    }

    private Staff(Builder builder){
        setId(builder.id);
        setPlace(builder.place);
        setCode(builder.code);
        setFaceFeature(builder.faceFeature);
        setFinger0(builder.finger0);
        setFinger1(builder.finger1);
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
                ", faceFeature='" + faceFeature + '\'' +
                ", finger0='" + finger0 + '\'' +
                ", finger1='" + finger1 + '\'' +
                '}';
    }

    public static final class Builder{
        private long id;
        private String place;
        private String code;
        private byte[] faceFeature;
        private byte[] finger0;
        private byte[] finger1;

        public Builder() {
        }

        public Builder id(long id) {
            this.id=id;
            return this;
        }

        public Builder place(String place) {
            this.place=place;
            return this;
        }

        public Builder code(String code) {
            this.code=code;
            return this;
        }

        public Builder faceFeature(byte[] faceFeature) {
            this.faceFeature=faceFeature;
            return this;
        }

        public Builder finger0(byte[] finger0) {
            this.finger0=finger0;
            return this;
        }

        public Builder finger1(byte[] finger1) {
            this.finger1=finger1;
            return this;
        }

        public Staff builder(){return new Staff(this);}
    }

}
