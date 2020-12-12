package com.thefloow.techtest.trackmyfloow.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation.
 * Currently network is built as a placeholder and not used.
 * </p>
 */

public class AppExecutors
{
    private static final int THREAD_COUNT = 1;

    private final Executor diskIO;

    private final Executor networkIO;

    private final Executor mainThread;

    // TODO: Try to find another way to feed the service thread if possible so it can be made final.
    private Executor serviceThread;

    @VisibleForTesting
    AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread, Executor serviceThread)
    {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
        this.serviceThread = serviceThread;
    }

    public AppExecutors()
    {
        this(new DiskIOThreadExecutor(), Executors.newFixedThreadPool(THREAD_COUNT),
                new MainThreadExecutor(), null);
    }

    public void setServiceThread(Executor serviceExecutor)
    {
        serviceThread = serviceExecutor;
    }


    public Executor diskIO()
    {
        return diskIO;
    }

    /**
     * Not in use currently. Place holder for future implementation.
     *
     * @return the thread executor for networking tasks
     */
    public Executor networkIO()
    {
        return networkIO;
    }

    public Executor mainThread()
    {
        return mainThread;
    }

    public Executor serviceThread()
    {
        return serviceThread;
    }

    /**
     * Get and hold a reference to the UI thread's Looper so that we can communicate with the UI
     * from a thread if needed. Mainly used for callbacks that we want to send to the UI.
     */
    private static class MainThreadExecutor implements Executor
    {
        private Handler mainThreadHandler;

        public MainThreadExecutor()
        {
            mainThreadHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(@NonNull Runnable command)
        {
            mainThreadHandler.post(command);
        }
    }

    /**
     * Is called when the location service is initialized. Takes the looper of the service
     * assigns it to a handler which then is run by an executor.
     */
    public static class ServiceThreadExecutor implements Executor
    {
        private Handler serviceThreadHandler;

        public ServiceThreadExecutor(Looper serviceLooper)
        {
            serviceThreadHandler = new Handler(serviceLooper);
        }

        @Override
        public void execute(@NonNull Runnable command)
        {
            serviceThreadHandler.post(command);
        }
    }

    /**
     * Class to handle the creation of an executor for disk IO tasks.
     */
    private static class DiskIOThreadExecutor implements Executor
    {
        private final Executor mDiskIO;

        public DiskIOThreadExecutor()
        {
            mDiskIO = Executors.newSingleThreadExecutor();
        }

        @Override
        public void execute(@NonNull Runnable command)
        {
            mDiskIO.execute(command);
        }
    }
}
