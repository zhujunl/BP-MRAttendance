package com.miaxis.attendance.api.bean;

import java.util.List;

/**
 * @author Tank
 * @date 2021/8/23 4:23 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UserBean {

    public long id;
    public String name;
    public String jobNo;
    public String idNumber;
    public String basePic;
    public long departmentId;
    public List<FingerBean> fingerList;

    public UserBean() {
    }

    public static class FingerBean {

        public String url;//指纹图片地址
        public String value;//指纹特征值
        public String deviceVersion;//设备版本
        public String algorithm;//算法
        public String location;//指纹位置

        public FingerBean() {
        }

        @Override
        public String toString() {
            return "FingerBean{" +
                    "url='" + url + '\'' +
                    ", value='" + value + '\'' +
                    ", deviceVersion='" + deviceVersion + '\'' +
                    ", algorithm='" + algorithm + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }

    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jobNo='" + jobNo + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", basePic='" + basePic + '\'' +
                ", departmentId=" + departmentId +
                ", fingerList=" + fingerList +
                '}';
    }

}

