package com.miaxis.attendance.data.dao;


import com.miaxis.attendance.data.entity.Person;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * @author Admin
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
@Dao
public interface PersonDao {

    @Query("SELECT * FROM Person ORDER BY Person.id ASC")
    List<Person> findAll();

    @Query("SELECT * FROM Person WHERE Person.userID=:userID")
    List<Person> findByUserID(String userID);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Person face);

    @Update
    int update(Person face);

    @Delete
    int delete(Person face);

    @Query("DELETE FROM Person WHERE Person.userID=:userId")
    int delete(String userId);

    @Query("DELETE FROM Person WHERE Person.id=:id")
    int delete(long id);

    @Query("DELETE FROM Person")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM Person")
    int allCounts();

    @Query("SELECT * FROM Person ORDER BY Person.id ASC LIMIT :pageSize OFFSET :offset")
    List<Person> findPage(int pageSize, int offset);

}
