package com.miaxis.attendance.data.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import androidx.room.TypeConverter;

public class LongListConverter {

    private static final Gson GSON = new Gson();

    @TypeConverter
    public static List<Long> revertList(String json) {
        try {
            return GSON.fromJson(json, new TypeToken<List<Long>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String convertString(List<Long> list) {
        return GSON.toJson(list);
    }

}
