package com.thefloow.techtest.trackmyfloow.data.source;


import androidx.annotation.NonNull;

import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;

import java.util.List;

/**
 * Main entry point interface for accessing journey data.
 * <p>
 * For simplicity and due to time constraints the callbacks for data failures are not handled.
 * Set as future work.
 * </p>
 */

public interface JourneysDataSource
{
    interface LoadJourneysCallback
    {
        void onJourneysLoaded(List<Journey> journeys);

        void onDataNotAvailable();
    }

    interface LoadJourneyCallback
    {
        void onJourneyLoaded(Journey journey);

        void onDataNotAvailable();
    }

    interface SaveJourneyCallback
    {
        void onJourneySaved(Journey journey);
    }

    interface LoadJourneyPositionsCallback
    {
        void onPositionsLoaded(List<Position> positions);

        void onDataNotAvailable();
    }

    interface SaveJourneyPositionCallback
    {
        void onPositionSaved();
    }

    interface JourneyCompletedCallback
    {
        void onJourneyCompleted();
    }

    void getJourneys(@NonNull LoadJourneysCallback callback);

    void getJourney(@NonNull Long journeyId, @NonNull LoadJourneyCallback callback);

    void getJourneyPositions(@NonNull Long journeyId, @NonNull LoadJourneyPositionsCallback callback);

    void saveJourney(@NonNull Journey journey, @NonNull SaveJourneyCallback callback);

    void saveJourneyPosition(@NonNull Position position, @NonNull SaveJourneyPositionCallback callback);

    void completeJourney(@NonNull Journey journey, @NonNull JourneyCompletedCallback callback);

    void activateJourney(@NonNull Long journeyId, @NonNull LoadJourneyPositionsCallback callback);

    void refreshJourneys();

    void deleteJourney(@NonNull Long JourneyId);
}
