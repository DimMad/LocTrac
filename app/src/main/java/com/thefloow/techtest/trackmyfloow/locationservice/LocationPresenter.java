package com.thefloow.techtest.trackmyfloow.locationservice;

import android.support.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysDataSource;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysRepository;
import com.thefloow.techtest.trackmyfloow.util.AppExecutors;
import com.thefloow.techtest.trackmyfloow.util.DateFormatter;

import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Even though the view is passive (service) it listens to it's actions ({@link LocationService}),
 * retrieves, saves and updates the data as required.
 */

public class LocationPresenter implements LocationContract.Presenter
{
    private final LocationContract.View mLocationService;

    private final JourneysRepository mJourneysRepository;

    private Journey mActiveJourney;

    private boolean isStopCalled = false;

    public LocationPresenter(@NonNull JourneysRepository journeysRepository, @NonNull LocationContract.View locationServiceView)
    {
        mJourneysRepository = checkNotNull(journeysRepository, "journeysRepository cannot be null!");
        mLocationService = checkNotNull(locationServiceView, "journeysMapView cannot be null!");
    }

    public AppExecutors setThreadExecutor()
    {
        return mJourneysRepository.getJourneysLocalDataSource().getAppExecutors();
    }

    @Override
    public void startNewJourney(String namePrefix)
    {
        if (mActiveJourney == null)
        {
            Date startDate = new Date();
            final String tempName = namePrefix + " " + DateFormatter.getDateOnly(startDate);
            Journey newJourney = new Journey(tempName, startDate);
            mJourneysRepository.saveJourney(newJourney, new JourneysDataSource.SaveJourneyCallback()
            {
                @Override
                public void onJourneySaved(Journey journey)
                {
                    mActiveJourney = journey;
                    mLocationService.requestLocationUpdates();
                    mLocationService.sendNewDataBroadcast(LocationService.NEW_JOURNEY_BROADCAST);
                }
            });
        }
    }

    /**
     * Finishes the active journey by updating it with an end date and then cleans up and stops
     * the service.
     */
    @Override
    public void finishNewJourney()
    {
        isStopCalled = true;
        mActiveJourney.setEndDate(new Date());
        mJourneysRepository.completeJourney(mActiveJourney, new JourneysDataSource.JourneyCompletedCallback()
        {
            @Override
            public void onJourneyCompleted()
            {
                // TODO: weird bug. Broadcasts from here are not working...
                mLocationService.sendNewDataBroadcast(LocationService.NEW_JOURNEY_BROADCAST);
                mLocationService.stopLocationService();
            }
        });
    }

    /**
     * Takes the required parameters from a location object and pushes them to the repository for
     * handling.
     *
     * @param latitude   double, the latitude coordinate of the latest location position in decimal format
     * @param longitude  double, the longitude coordinate of the latest location position in decimal format
     * @param timeMillis long, the timestamp of the latest location position in milliseconds
     */
    @Override
    public void logLocation(double latitude, double longitude, long timeMillis)
    {
        if (!isStopCalled)
        {
            Position newPosition = new Position(mActiveJourney.getJourneyId(), latitude, longitude, new Date(timeMillis));
            mJourneysRepository.saveJourneyPosition(newPosition, new JourneysDataSource.SaveJourneyPositionCallback()
            {
                @Override
                public void onPositionSaved()
                {
                    // This call back returns no data but it is required for the service to
                    // broadcast that a new position was logged.
                    mLocationService.sendNewDataBroadcast(LocationService.NEW_POSITION_BROADCAST);
                }
            });
        }
    }
}
