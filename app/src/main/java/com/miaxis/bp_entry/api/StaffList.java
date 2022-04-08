package com.miaxis.bp_entry.api;

import com.miaxis.bp_entry.data.entity.Staff;

import java.util.List;

/**
 * @author ZJL
 * @date 2022/4/7 10:46
 * @des
 * @updateAuthor
 * @updateDes
 */
public class StaffList {
    private List<Staff> list;

    public StaffList(List<Staff> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "StaffList{" +
                "list=" + list +
                '}';
    }
}
