package com.miaxis.attendance.data.bean;

/**
 * @author Tank
 * @date 2021/9/27 8:44 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class DataObject<A,B> {

    public A data0;
    public B data1;

    public DataObject() {
    }

    public DataObject(A data0, B data1) {
        this.data0 = data0;
        this.data1 = data1;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "data0=" + data0 +
                ", data1=" + data1 +
                '}';
    }
}
