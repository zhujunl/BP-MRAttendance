package com.miaxis.attendance.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author Tank
 * @date 2021/8/23 11:53 上午
 * @des
 * @updateAuthor
 * @updateDes
 */
@Entity
public class Department {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String Name;//部门名称
    public String Number;//部门编号
    public boolean Enable;//是否启用
    public long create_time;//创建时间
    public long update_time;//修改时间

    public Department() {
        this.create_time=System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", Name='" + Name + '\'' +
                ", Number='" + Number + '\'' +
                ", Enable=" + Enable +
                ", create_time='" + create_time + '\'' +
                ", update_time='" + update_time + '\'' +
                '}';
    }
}
