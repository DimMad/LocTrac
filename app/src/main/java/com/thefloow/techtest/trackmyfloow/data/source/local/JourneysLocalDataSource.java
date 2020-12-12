package com.thefloow.techtest.trackmyfloow.data.source.local;


import androidx.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.data.source.JourneysDataSource;
import com.thefloow.techtest.trackmyfloow.util.AppExecutors;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source for the local db.
 * <p>
 * The data source holds an executors thread pool ({@link AppExecutors}) to utilize for all background
 * work. In each method a runnable is passed to a disk IO executor. If the method has a callback to post
 * that happens in a second runnable that uses whichever thread needed.
 * </p>
 */

public class JourneysLocalDataSource implements JourneysDataSource {
    // Static instance of the data source to ensure only one is instantiated and volatile
    // to ensure tha all threads always access the same data from the system memory.
    private static volatile JourneysLocalDataSource INSTANCE;

    private JourneysDao mJourneysDao;

    private PositionsDao mPositionsDao;

    private AppExecutors mAppExecutors;

    // Prevent direct instantiation.
    private JourneysLocalDataSource(@NonNull AppExecutors appExecutors, @NonNull JourneysDao journeysDao,
                                    @NonNull PositionsDao positionsDao) {
        mAppExecutors = appExecutors;
        mJourneysDao = journeysDao;
        mPositionsDao = positionsDao;
    }

    // Get static instance.
    public static JourneysLocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                                      @NonNull JourneysDao journeysDao,
                                                      @NonNull PositionsDao positionsDao) {
        if (INSTANCE == null) {
            synchronized (JourneysLocalDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JourneysLocalDataSource(appExecutors, journeysDao, positionsDao);
                }
            }
        }
        return INSTANCE;
    }

    // Chaining method to provide data source executors access to the location service.
    public AppExecutors getAppExecutors() {
        return mAppExecutors;
    }

    @Override
    public void getJourneys(@NonNull final LoadJourneysCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Journey> journeys = mJourneysDao.getAllJourneys();
                // TODO: should handle which thread each callback is run onto better.
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
//                        if (journeys.isEmpty())
//                            callback.onDataNotAvailable(); TODO: handle later
//                        else
                        callback.onJourneysLoaded(journeys);
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getJourney(@NonNull final Long journeyId, @NonNull final LoadJourneyCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final Journey journey = mJourneysDao.getJourneyById(journeyId);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
//                        if (journey == null)
//                            callback.onDataNotAvailable(); TODO: handle later
//                        else
                        callback.onJourneyLoaded(journey);
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getJourneyPositions(@NonNull final Long journeyId, @NonNull final LoadJourneyPositionsCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Position> positions = mPositionsDao.getJourneyPositions(journeyId);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
//                        if (positions.isEmpty())
//                            callback.onDataNotAvailable();
//                        else
                        callback.onPositionsLoaded(positions);
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveJourney(@NonNull final Journey journey, @NonNull final SaveJourneyCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final long rowId = mJourneysDao.insertJourney(journey);
                journey.setJourneyId(rowId);
                mAppExecutors.serviceThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (rowId > 0)
                            callback.onJourneySaved(journey);
                        //TODO: handle insertion failure.
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveJourneyPosition(@NonNull final Position position, @NonNull final SaveJourneyPositionCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final long rowId = mPositionsDao.insertPosition(position);
                position.setPositionId(rowId);
                mAppExecutors.serviceThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (rowId > 0)
                            callback.onPositionSaved();
                        //TODO: handle insertion failure.
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void completeJourney(@NonNull final Journey journey, @NonNull final JourneyCompletedCallback callback) {
        checkNotNull(journey);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mJourneysDao.updateJourney(journey);
                mAppExecutors.serviceThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onJourneyCompleted();
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void activateJourney(@NonNull final Long journeyId, @NonNull final LoadJourneyPositionsCallback callback) {
        checkNotNull(journeyId);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<Position> positions = mPositionsDao.getJourneyPositions(journeyId);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.onPositionsLoaded(positions);
                    }
                });
            }
        };
        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void refreshJourneys() {
        // Implementation not required here as the repository handles the synchronizations of data
        // from all available data sources.
    }

    @Override
    public void deleteJourney(@NonNull Long JourneyId) {
        // TODO: implement if enough time.
    }
}
