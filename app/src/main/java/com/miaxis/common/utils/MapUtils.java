package com.miaxis.common.utils;

import java.util.Map;

/**
 * @author Tank
 * @date 2021/8/6 7:26 下午
 * @des
 * @updateAuthor
 * @updateDes
 */
public class MapUtils {


    public static boolean isNull(Map<?,?> map) {
        return map == null;
    }

    public static boolean isNullOrEmpty(Map<?,?> map) {
        return map == null || map.isEmpty();
    }


}
