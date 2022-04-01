package com.miaxis.attendance.ui.manager;

import java.util.List;

/**
 * @author Tank
 * @date 2021/9/27 7:26 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class MxUser {

    public String userId;
    public String name;
    public String number;
    public String face;
    public List<String> fingers;

    public MxUser(String userId, String name, String number, String face, List<String> fingers) {
        this.userId = userId;
        this.name = name;
        this.number = number;
        this.face = face;
        this.fingers = fingers;
    }

    @Override
    public String toString() {
        return "MxUser{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", face='" + face + '\'' +
                ", fingers=" + fingers +
                '}';
    }
}
