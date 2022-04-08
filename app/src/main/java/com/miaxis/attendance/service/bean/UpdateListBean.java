package com.miaxis.attendance.service.bean;

import java.util.List;

/**
 * @author ZJL
 * @date 2022/4/7 9:54
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UpdateListBean {
    private List<StaffBean> list;

    public UpdateListBean() {
    }

    @Override
    public String toString() {
        return "UpdateListBean{" +
                "list=" + list +
                '}';
    }
}
