package com.thefloow.techtest.trackmyfloow.data.dbutils;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * This is a utility class for the Room ORM required to change complex objects into primitives
 * that can be saved in a database. In this case we are converting dates.
 */

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : (new Date(timestamp));
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
