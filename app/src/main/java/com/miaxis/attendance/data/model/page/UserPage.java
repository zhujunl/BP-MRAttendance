package com.miaxis.attendance.data.model.page;

import com.miaxis.attendance.data.entity.Person;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author Tank
 * @date 2021/9/30 2:27 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UserPage implements BasePage<Person> {

    int total;
    int index;
    List<Person> pageData;

    public UserPage(int total, int index, @Nullable List<Person> pageData) {
        this.total = total;
        this.index = index;
        this.pageData = pageData;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public long position() {
        return index;
    }

    @Override
    public List<Person> pageData() {
        return pageData;
    }
}
