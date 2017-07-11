package com.thefloow.techtest.trackmyfloow.locationservice;

import com.thefloow.techtest.trackmyfloow.util.AppExecutors;

/**
 * Declaration of the interface between the {@link LocationService} and its {@link LocationPresenter}.
 */

public interface LocationContract
{
    /**
     * This View is referring to the service. It is going to be implemented by the service and so
     * all the Google API interfaces required are extended by it.
     */
    interface View
    {
        int startLocationService();

        void stopLocationService();

        void requestLocationUpdates();

        void sendNewDataBroadcast(String action);

        void showForegroundServiceNotification();

        void setLocationRequest();

        void buildGoogleApiClient();
    }

    /**
     * The Presenter interface exposes the actions that can be done on the data to the view.
     */
    interface Presenter
    {
        AppExecutors setThreadExecutor();

        void startNewJourney(String namePrefix);

        void finishNewJourney();

        void logLocation(double latitude, double longitude, long time);
    }
}
