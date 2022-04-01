package com.miaxis.attendance.data.model.page;

import java.util.List;

/**
 * @author Tank
 * @date 2021/9/30 2:25 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public interface BasePage<T> {

    long getTotal();

    long position();

    List<T> pageData();

}
