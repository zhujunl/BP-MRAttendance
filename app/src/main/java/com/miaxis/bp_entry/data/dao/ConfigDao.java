package com.miaxis.bp_entry.data.dao;

import com.miaxis.bp_entry.data.entity.Config;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * @author ZJL
 * @date 2022/3/23 15:19
 * @des
 * @updateAuthor
 * @updateDes
 */
@Dao
public interface ConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Config config);

    @Query("select * from config where id = 1")
    Config loadConfig();

    @Query("delete from config")
    void deleteAll();
}
