package com.miaxis.bp_entry.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author ZJL
 * @date 2022/3/23 15:16
 * @des
 * @updateAuthor
 * @updateDes
 */

@Entity
public class  Config {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String place;
    public String tittle;
    public String Ip;

    public Config() {
    }

    public Config(String place, String tittle, String ip) {
        this.place = place;
        this.tittle = tittle;
        Ip = ip;
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

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", place='" + place + '\'' +
                ", tittle='" + tittle + '\'' +
                ", Ip='" + Ip + '\'' +
                '}';
    }
}
