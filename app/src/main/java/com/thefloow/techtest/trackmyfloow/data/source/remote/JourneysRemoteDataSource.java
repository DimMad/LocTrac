package com.thefloow.techtest.trackmyfloow.data.source.remote;

import android.support.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysDataSource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Concrete implementation of a data source for the remote db.
 *
 * Currently a stub. Will be implemented later.
 */

public class JourneysRemoteDataSource implements JourneysDataSource
{
    private static JourneysRemoteDataSource INSTANCE;

    // Prevent direct instantiation.
    private JourneysRemoteDataSource()
    {
    }

    public static JourneysRemoteDataSource getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new JourneysRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public void getJourneys(@NonNull LoadJourneysCallback callback)
    {

    }

    @Override
    public void getJourney(@NonNull Long journeyId, @NonNull LoadJourneyCallback callback)
    {

    }

    @Override
    public void getJourneyPositions(@NonNull Long journeyId, @NonNull LoadJourneyPositionsCallback callback)
    {

    }

    @Override
    public void saveJourney(@NonNull Journey journey, @NonNull SaveJourneyCallback callback)
    {

    }

    @Override
    public void saveJourneyPosition(@NonNull Position position, @NonNull SaveJourneyPositionCallback callback)
    {

    }

    @Override
    public void completeJourney(@NonNull Journey journey, @NonNull JourneyCompletedCallback callback)
    {

    }

    @Override
    public void activateJourney(@NonNull Long journeyId, @NonNull LoadJourneyPositionsCallback callback)
    {

    }

    @Override
    public void refreshJourneys()
    {

    }

    @Override
    public void deleteJourney(@NonNull Long JourneyId)
    {

    }
}
