package com.thefloow.techtest.trackmyfloow.journeysmap;

import android.support.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysDataSource;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysRepository;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * UI presenter. Listens to UI ({@link JourneysMapFragment}) interactions, retrieves data and updates
 * is as required.
 */

public class JourneysMapPresenter implements JourneysMapContract.Presenter
{
    private final JourneysMapContract.View mJourneysMapView;

    private final JourneysRepository mJourneysRepository;

    private Journey mActiveJourney;

    public JourneysMapPresenter(@NonNull JourneysRepository journeysRepository, @NonNull JourneysMapContract.View journeysMapView)
    {
        mJourneysRepository = checkNotNull(journeysRepository, "journeysRepository cannot be null!");
        mJourneysMapView = checkNotNull(journeysMapView, "journeysMapView cannot be null!");

        mJourneysMapView.setPresenter(this);
    }

    @Override
    public void start()
    {
        loadJourneys();
        getActiveJourney();
    }

    @Override
    public void getActiveJourney()
    {
        mActiveJourney = mJourneysRepository.getActiveJourney();
    }

    @Override
    public void getActiveJourneyPositions()
    {
        // In case presenter was just initialized get the active journey from the repository.
        if(mActiveJourney == null)
            getActiveJourney();

        if (mActiveJourney != null)
        {
            mJourneysRepository.getJourneyPositions(mActiveJourney.getJourneyId(), new JourneysDataSource.LoadJourneyPositionsCallback()
            {
                @Override
                public void onPositionsLoaded(List<Position> positions)
                {
                    mJourneysMapView.clearMapPolyline();
                    mJourneysMapView.drawMapPolyline(positions);
                    mJourneysMapView.centerMapCamera(false, positions);
                }

                @Override
                public void onDataNotAvailable()
                {

                }
            });
        }
        else
        {
            mJourneysMapView.clearMapPolyline();
        }
    }

    @Override
    public void checkPermissions()
    {
        if (!mJourneysMapView.hasPermissions())
        {
            mJourneysMapView.requestPermissions();
        }
        else
        {
            mJourneysMapView.enableMapCurrentLocationLayer();
        }
    }

    @Override
    public void startTracking()
    {
        mJourneysMapView.startLocationService();
        mJourneysMapView.registerBroadcastReceiver();
    }

    @Override
    public void stopTracking()
    {
        mJourneysMapView.stopLocationService();
    }

    @Override
    public void loadJourneys()
    {
        mJourneysRepository.getJourneys(new JourneysDataSource.LoadJourneysCallback()
        {
            @Override
            public void onJourneysLoaded(List<Journey> journeys)
            {
                // TODO: add Espresso idling resources at this point.

                // Make sure the view is available to handle requests
                if (!mJourneysMapView.isActive())
                    return;

                if (journeys.isEmpty())
                    mJourneysMapView.showNoJourneys();
                else
                    mJourneysMapView.showJourneyHistory(journeys);
            }

            @Override
            public void onDataNotAvailable()
            {
                mJourneysMapView.showNoJourneys();
            }
        });
    }

    @Override
    public void activateJourney(@NonNull long journeyId)
    {
        checkNotNull(journeyId, "journeyId cannot be null");
        mJourneysRepository.activateJourney(journeyId, new JourneysDataSource.LoadJourneyPositionsCallback()
        {
            @Override
            public void onPositionsLoaded(List<Position> positions)
            {
                mJourneysMapView.clearMapPolyline();
                mJourneysMapView.drawMapPolyline(positions);
                mJourneysMapView.centerMapCamera(true, positions);
            }

            @Override
            public void onDataNotAvailable()
            {

            }
        });
    }
}
