package com.miaxis.bp_entry.data.entity;

import com.miaxis.bp_entry.data.dao.AppDatabase;

/**
 * @author ZJL
 * @date 2022/3/23 16:17
 * @des
 * @updateAuthor
 * @updateDes
 */
public class ConfigModel {
    public static void saveConfig(Config config) {
        config.setId(1L);
        AppDatabase.getInstance().configDao().deleteAll();
        AppDatabase.getInstance().configDao().insert(config);
    }

    public static Config loadConfig() {
        Config config = AppDatabase.getInstance().configDao().loadConfig();
        return config;
    }
}
