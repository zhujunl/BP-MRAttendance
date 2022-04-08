package com.miaxis.attendance.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Tank
 * @date 2021/8/23 1:36 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
@Entity
public class LocalImage {

    @PrimaryKey(autoGenerate = true)
    public long id;
    //    /**
    //     * 图片类型  0无  1人脸图片(注册图片)   2人脸抓拍图片  3指纹图片(注册)  4指纹图片(采集)  5人脸截图
    //     */
    //    public int Type;
    /**
     * 用户ID
     */
    public String LocalPath;
//    public String RemotePath;
    public long create_time;//创建时间
    public long update_time;//修改时间

    public LocalImage() {
        this.create_time = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "LocalImage{" +
                "id=" + id +
                //", Type=" + Type +
                ", LocalPath='" + LocalPath + '\'' +
//                ", RemotePath='" + RemotePath + '\'' +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                '}';
    }
}
