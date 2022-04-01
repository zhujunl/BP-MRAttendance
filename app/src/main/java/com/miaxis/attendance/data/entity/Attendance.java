package com.miaxis.attendance.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Tank
 * @date 2021/8/23 1:34 下午
 * @des
 * @updateAuthor
 * @updateDes
 */

@Entity
public class Attendance {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public int Mode;//考勤方式  0无  1人脸   2指纹
    /**
     * 用户ID
     */
    public String UserId;//用户ID
    //public long BaseImage;//底图ID
    public long CaptureImage;//现场对比图ID
    public long CutImage;//现场人脸图ID(仅人脸)
    public int Status;//考勤状态  0无  1成功     2失败
    public int Upload;//上传状态  0无  1已上传   2未上传
    public long create_time;//创建时间
    public long update_time;//修改时间

    public Attendance() {
        this.create_time=System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", Mode=" + Mode +
                ", UserId='" + UserId + '\'' +
                //", BaseImage=" + BaseImage +
                ", CaptureImage=" + CaptureImage +
                ", CutImage=" + CutImage +
                ", Status=" + Status +
                ", Upload=" + Upload +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                '}';
    }
}
