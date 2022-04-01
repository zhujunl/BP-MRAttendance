package com.miaxis.attendance.service.bean;

import com.miaxis.attendance.service.HttpServer;
import com.miaxis.common.utils.StringUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tank
 * @date 2021/8/23 11:53 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class User {

    public long id;
    //public boolean ia_admin;//是否是管理员 0-否 1-是
    public String name;//姓名（唯一，汉字）
    public String id_number;//身份证号
    public String job_no;//工号（唯一，大小写字母和数字）
    public String department_id;//部门id
    public String url_face;//人脸底图(图片URL)
    public String url_fingers;//指纹底图(图片URL)
    //public String is_delete;//是否删除 0-否 1-是
    //public String create_time;//创建时间
    //public String update_time;//修改时间


    //{
    //    "id_number": "412723199505012332",
    //    "department_id": "1",
    //    "url_fingers": [{
    //        "url": "http://192.168.5.164:8085/upload/2021/9/1/1630466471199.jpg",
    //        "location": 6
    //    }],
    //    "name": "管理员",
    //    "job_no": "system",
    //    "url_face": "http://192.168.5.164:8085/upload/2021/9/10/1631254516790.jpg",
    //    "id": "1"
    //}


    public User() {
    }

    public boolean isIllegal() {
        return this.id <= 0 ||
                StringUtils.isNullOrEmpty(this.name) ||
                StringUtils.isNullOrEmpty(this.job_no);
    }

    public List<Finger> getUrl_fingers() {
        List<Finger> fingers = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(this.url_fingers);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                String string = jsonArray.getString(i);
                Finger finger = HttpServer.Gson.fromJson(string, Finger.class);
                fingers.add(finger);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fingers;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                //", ia_admin=" + ia_admin +
                ", name='" + name + '\'' +
                ", id_number='" + id_number + '\'' +
                ", job_no='" + job_no + '\'' +
                ", department_id='" + department_id + '\'' +
                ", url_face=" + url_face +
                ", url_fingers=" + url_fingers +
                //", is_delete='" + is_delete + '\'' +
                //", create_time='" + create_time + '\'' +
                //", update_time='" + update_time + '\'' +
                '}';
    }

    public static class Finger {

        public int location;
        public String url;

        public Finger() {
        }

        public boolean isIllegal() {
            return this.location < 0 ||
                    this.location > 9 ||
                    StringUtils.isNullOrEmpty(this.url);
        }

        @Override
        public String toString() {
            return "Finger{" +
                    "position=" + location +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

}


