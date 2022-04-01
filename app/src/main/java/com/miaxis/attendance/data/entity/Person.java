package com.miaxis.attendance.data.entity;

import java.util.List;

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
public class Person {

    @PrimaryKey(autoGenerate = true)
    public long id;
    /**
     * 用户ID
     */
    public String UserId;//用户ID
    /**
     * 姓名
     */
    public String Name;//姓名
    /**
     * 工号
     */
    public String Number;//工号
    /**
     * 性别
     */
    public String Gender;//性别
    /**
     * 身份证号码
     */
    public String IdCardNumber;//身份证号码
    /**
     * 是否启用
     */
    public boolean Enable;//是否启用
    /**
     * 人脸ID
     */
    public List<Long> faceIds;
    /**
     * 指纹ID
     */
    public List<Long> fingerIds;

    public long create_time;//创建时间
    public long update_time;//修改时间

    public Person() {
        this.create_time = System.currentTimeMillis();
    }


    //    @Entity(foreignKeys = @ForeignKey(entity = Company.class,parentColumns = "id",childColumns = "emp_id",onDelete = CASCADE),
    //            indices = @Index(value={"emp_id"},unique = true))
    //    public class Department {
    //        @PrimaryKey(autoGenerate = true)
    //        private int id;
    //        private String dept;
    //        @ColumnInfo(name = "emp_id")
    //        private int empId;
    //
    //        public Department(String dept, int empId) {
    //            this.dept = dept;
    //            this.empId = empId;
    //        }
    //        //省略了getter/setter方法
    //    }


    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", UserId='" + UserId + '\'' +
                ", Name='" + Name + '\'' +
                ", Number='" + Number + '\'' +
                ", Gender='" + Gender + '\'' +
                ", IdCardNumber='" + IdCardNumber + '\'' +
                ", Enable=" + Enable +
                ", faceId=" + faceIds +
                ", fingerIds=" + fingerIds +
                ", create_time=" + create_time +
                ", update_time=" + update_time +
                '}';
    }

}
