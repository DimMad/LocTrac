package com.thefloow.techtest.trackmyfloow;

import android.content.Context;

import androidx.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.source.JourneysDataSource;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysRepository;
import com.thefloow.techtest.trackmyfloow.data.source.local.AppDatabase;
import com.thefloow.techtest.trackmyfloow.data.source.local.JourneysLocalDataSource;
import com.thefloow.techtest.trackmyfloow.data.source.remote.JourneysRemoteDataSource;
import com.thefloow.techtest.trackmyfloow.util.AppExecutors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for {@link JourneysDataSource} at compile time.
 * Helps with testing and enables us switching build variants easily.
 */

public class Injection
{
    public static JourneysRepository provideTasksRepository(@NonNull Context context)
    {
        checkNotNull(context);
        AppDatabase database = AppDatabase.openDatabase(context);
        return JourneysRepository.getInstance(JourneysRemoteDataSource.getInstance(),
                JourneysLocalDataSource.getInstance(new AppExecutors(), database.journeysDao(), database.positionsDao()));
    }
}
