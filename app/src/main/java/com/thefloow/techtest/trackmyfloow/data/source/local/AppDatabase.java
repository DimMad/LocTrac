package com.thefloow.techtest.trackmyfloow.data.source.local;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.dbutils.DateConverter;

/**
 * This is the main Database class which extends the RoomDatabase class and is responsible for
 * creating our database for the entities provided (Entity == DB Table). A singleton is used
 * to persist the database reference and make accessing faster and thread safe.
 */

@Database(entities = {Journey.class, Position.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase
{
    private static AppDatabase DATABASE_INSTANCE;

    public abstract JourneysDao journeysDao();

    public abstract PositionsDao positionsDao();

    public static synchronized AppDatabase openDatabase(Context context)
    {
        if (DATABASE_INSTANCE == null)
            DATABASE_INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "trackfloow.db").build();

        return DATABASE_INSTANCE;
    }

    public static void destroyInstance()
    {
        DATABASE_INSTANCE = null;
    }
}
