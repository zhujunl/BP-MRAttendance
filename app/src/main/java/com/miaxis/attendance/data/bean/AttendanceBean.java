package com.miaxis.attendance.data.bean;

/**
 * @author Tank
 * @date 2021/8/23 1:34 下午
 * @des
 * @updateAuthor
 * @updateDes
 */

public class AttendanceBean {

    public long AttendanceId;//考勤记录ID
    public int Mode;//考勤方式  0无  1人脸   2指纹
    public String UserId;//用户ID(Person)
    public String UserName;//用户姓名
    //public String BaseImage;//底图
    public String CaptureImage;//现场对比图
    public String CutImage;//现场人脸图
    public int Status;//考勤状态  0无  1成功   2失败
    public String create_time;//创建时间
    public String update_time;//修改时间
    public float tempFloat;//相似度
    public int tempType;//验证方式；0：人脸，1：指纹

    public AttendanceBean() {
    }

    @Override
    public String toString() {
        return "AttendanceBean{" +
                "AttendanceId=" + AttendanceId +
                ", Mode=" + Mode +
                ", UserId='" + UserId + '\'' +
                ", UserName='" + UserName + '\'' +
                ", CaptureImage='" + CaptureImage + '\'' +
                ", CutImage='" + CutImage + '\'' +
                ", Status=" + Status +
                ", create_time='" + create_time + '\'' +
                ", update_time='" + update_time + '\'' +
                ", tempFloat='" + tempFloat + '\'' +
                '}';
    }
}
