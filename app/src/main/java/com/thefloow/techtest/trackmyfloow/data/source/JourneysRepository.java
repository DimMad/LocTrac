package com.thefloow.techtest.trackmyfloow.data.source;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.source.local.JourneysLocalDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementations to load Journeys from data sources to a cache.
 * <p>
 * Currently there is no plan for the implementation of the remote repository but factories
 * have been created for future addition.
 * </p>
 */

public class JourneysRepository implements JourneysDataSource
{
    private static JourneysRepository INSTANCE = null;

    private final JourneysDataSource mJourneysRemoteDataSource;

    private final JourneysDataSource mJourneysLocalDataSource;

    /**
     * Package local visibility to access from tests.
     */
    Journey mActiveJourneyCache;

    /**
     * Package local visibility to access from tests.
     */
    List<Position> mJourneyPositionsCache;

    /**
     * Package local visibility to access from tests.
     */
    Map<Long, Journey> mJourneyHistoryCache;

    private boolean isHistoryActive = false;

    private JourneysRepository(@NonNull JourneysDataSource journeysRemoteDataSource,
                               @NonNull JourneysDataSource journeysLocalDataSource)
    {
        mJourneysRemoteDataSource = checkNotNull(journeysRemoteDataSource);
        mJourneysLocalDataSource = checkNotNull(journeysLocalDataSource);
    }

    /**
     * Returns the single instance of this class.
     * Creates it if first instantiation.
     *
     * @param journeysRemoteDataSource the backend data source
     * @param journeysLocalDataSource  the device storage data source
     * @return the {@link JourneysRepository} instance
     */
    public static JourneysRepository getInstance(JourneysDataSource journeysRemoteDataSource,
                                                 JourneysDataSource journeysLocalDataSource)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new JourneysRepository(journeysRemoteDataSource, journeysLocalDataSource);
        }
        return INSTANCE;
    }

    // Chaining method to provide data source executors access to the location service.
    public JourneysLocalDataSource getJourneysLocalDataSource()
    {
        return (JourneysLocalDataSource) mJourneysLocalDataSource;
    }

    // Active journey getter for quick access from presenters that might need the information.
    public Journey getActiveJourney()
    {
        return mActiveJourneyCache;
    }

    @Override
    public void getJourneys(@NonNull final LoadJourneysCallback callback)
    {
        checkNotNull(callback);
        // Get from cache if available.
        if (mJourneyHistoryCache != null)
        {
            callback.onJourneysLoaded(getCacheReverse());
            return;
        }

        // Otherwise bring from persistent data source.
        mJourneysLocalDataSource.getJourneys(new LoadJourneysCallback()
        {
            @Override
            public void onJourneysLoaded(List<Journey> journeys)
            {
                refreshJourneyHistoryCache(journeys);
                callback.onJourneysLoaded(getCacheReverse());
            }

            @Override
            public void onDataNotAvailable()
            {
                callback.onDataNotAvailable();
            }
        });
    }

    @Override
    public void getJourney(@NonNull final Long journeyId, @NonNull final LoadJourneyCallback callback)
    {
        checkNotNull(journeyId);
        checkNotNull(callback);

        // Get from cache if available.
        Journey cachedJourney = getJourneyWithId(journeyId);
        if (cachedJourney != null)
        {
            callback.onJourneyLoaded(cachedJourney);
            return;
        }

        // Load from local/remote data source.
        // First ask for data in local data source.
        mJourneysLocalDataSource.getJourney(journeyId, new LoadJourneyCallback()
        {
            @Override
            public void onJourneyLoaded(Journey journey)
            {
                // In memory cache update
                if (mJourneyHistoryCache == null)
                    mJourneyHistoryCache = new LinkedHashMap<>();

                mJourneyHistoryCache.put(journey.getJourneyId(), journey);
                callback.onJourneyLoaded(journey);
            }

            @Override
            public void onDataNotAvailable()
            {
                // TODO: If the local data sources does not contain the data then fetch from remote.
            }
        });

    }

    @Override
    public void getJourneyPositions(@NonNull Long journeyId, @NonNull final LoadJourneyPositionsCallback callback)
    {
        checkNotNull(journeyId);
        checkNotNull(callback);

        // Get journey positions directly from cache if the cache has data and they belong to
        // the active task.
        if (!isHistoryActive)
        {
            if (mJourneyPositionsCache != null)
            {
                callback.onPositionsLoaded(mJourneyPositionsCache);
                return;
            }
        }

        isHistoryActive = false;

        mJourneysLocalDataSource.getJourneyPositions(journeyId, new LoadJourneyPositionsCallback()
        {
            @Override
            public void onPositionsLoaded(List<Position> positions)
            {
                refreshJourneyPositionsCache(positions);
                callback.onPositionsLoaded(mJourneyPositionsCache);
            }

            @Override
            public void onDataNotAvailable()
            {
                // TODO: Sync from remote.
            }
        });
    }

    @Override
    public void saveJourney(@NonNull Journey journey, @NonNull final SaveJourneyCallback callback)
    {
        checkNotNull(journey);
        checkNotNull(callback);
        // TODO: web call.
        mJourneysLocalDataSource.saveJourney(journey, new SaveJourneyCallback()
        {
            @Override
            public void onJourneySaved(Journey journey)
            {
                // Cache the saved Journey for immediate usage also contains the auto generated id.
                if (mJourneyHistoryCache == null)
                    mJourneyHistoryCache = new LinkedHashMap<>();

                mJourneyHistoryCache.put(journey.getJourneyId(), journey);
                mActiveJourneyCache = journey;

                callback.onJourneySaved(journey);
            }
        });
    }

    @Override
    public void saveJourneyPosition(@NonNull final Position position, @NonNull final SaveJourneyPositionCallback callback)
    {
        checkNotNull(position);
        checkNotNull(callback);
        // TODO: web call.
        mJourneysLocalDataSource.saveJourneyPosition(position, new SaveJourneyPositionCallback()
        {
            @Override
            public void onPositionSaved()
            {
                if (!isHistoryActive)
                {
                    if (mJourneyPositionsCache == null)
                        mJourneyPositionsCache = new ArrayList<>();

                    mJourneyPositionsCache.add(position);
                }

                callback.onPositionSaved();
            }
        });
    }

    @Override
    public void completeJourney(@NonNull Journey journey, @NonNull final JourneyCompletedCallback callback)
    {
        checkNotNull(journey);
        checkNotNull(callback);
        // TODO: web call.
        // Update the journey to finished (has end date).
        mJourneysLocalDataSource.completeJourney(journey, new JourneyCompletedCallback()
        {
            @Override
            public void onJourneyCompleted()
            {
                // Nothing to do here. Chaining to the service presenter to make sure that journey
                // was properly completed before closing the tracking or the app.
                mJourneyHistoryCache = null;
                callback.onJourneyCompleted();
            }
        });

        // Empty the unnecessary caches
        mActiveJourneyCache = null;
        mJourneyPositionsCache = null;
    }

    @Override
    public void activateJourney(@NonNull final Long journeyId, @NonNull final LoadJourneyPositionsCallback callback)
    {
        checkNotNull(journeyId);
        checkNotNull(callback);

        isHistoryActive = true;
        mJourneysLocalDataSource.activateJourney(journeyId, new LoadJourneyPositionsCallback()
        {
            @Override
            public void onPositionsLoaded(List<Position> positions)
            {
                refreshJourneyPositionsCache(positions);
                callback.onPositionsLoaded(mJourneyPositionsCache);
            }

            @Override
            public void onDataNotAvailable()
            {
                // TODO: Sync from remote.
            }
        });
    }

    @Override
    public void refreshJourneys()
    {
        // Place holder for synchronization between local and remote data sources.
    }

    @Override
    public void deleteJourney(@NonNull Long JourneyId)
    {
        // TODO: implement if enough time.
    }

    /**
     * Repository method. Refreshes the journey history cache when empty.
     *
     * @param journeys the journeys kept in the cache.
     */
    private void refreshJourneyHistoryCache(List<Journey> journeys)
    {
        if (mJourneyHistoryCache == null)
            mJourneyHistoryCache = new LinkedHashMap<>();

        mJourneyHistoryCache.clear();
        for (Journey journey : journeys)
        {
            mJourneyHistoryCache.put(journey.getJourneyId(), journey);
        }
    }

    /**
     * Repository method. Refreshes the positions cache when empty.
     *
     * @param positions the positions kept in the cache.
     */
    private void refreshJourneyPositionsCache(List<Position> positions)
    {
        if (mJourneyPositionsCache == null)
            mJourneyPositionsCache = new ArrayList<>();

        mJourneyPositionsCache.clear();
        for (Position position : positions)
        {
            mJourneyPositionsCache.add(position);
        }
    }

    private ArrayList<Journey> getCacheReverse()
    {
        ArrayList<Journey> journeys = new ArrayList<>(mJourneyHistoryCache.values());
        Collections.reverse(journeys);
        return journeys;
    }

    @Nullable
    private Journey getJourneyWithId(@NonNull Long id)
    {
        checkNotNull(id);
        if (mJourneyHistoryCache == null || mJourneyHistoryCache.isEmpty())
        {
            return null;
        }
        else
        {
            return mJourneyHistoryCache.get(id);
        }
    }
}
