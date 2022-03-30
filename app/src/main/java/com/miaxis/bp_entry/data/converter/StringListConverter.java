package com.miaxis.bp_entry.data.converter;

import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.miaxis.bp_entry.util.ValueUtil;

import java.util.List;

import androidx.room.TypeConverter;

public class StringListConverter {

    @TypeConverter
    public static List<String> revert(String s) {
        try {
            return ValueUtil.GSON.fromJson(s, new TypeToken<List<String>>() {}.getType());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String converter(List<String> list) {
        Log.e("converter","list:"+ValueUtil.GSON.toJson(list));
        return ValueUtil.GSON.toJson(list);
    }

}
