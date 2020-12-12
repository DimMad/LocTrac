package com.thefloow.techtest.trackmyfloow.journeysmap;


import com.google.android.gms.maps.OnMapReadyCallback;
import com.thefloow.techtest.trackmyfloow.BasePresenter;
import com.thefloow.techtest.trackmyfloow.BaseView;
import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;

import java.util.List;

/**
 * This is the contract between the View (JourneysMapFragment) and the presenter.
 */

public interface JourneysMapContract {
    interface View extends BaseView<Presenter>, OnMapReadyCallback {
        boolean hasPermissions();

        void requestPermissions();

        void startLocationService();

        void stopLocationService();

        void enableMapCurrentLocationLayer();

        void showJourneyHistory(List<Journey> journeys);

        void showNoJourneys();

        boolean isActive();

        void registerBroadcastReceiver();

        void unregisterBroadcastReceiver();

        void drawMapPolyline(List<Position> positions);

        void clearMapPolyline();

        void centerMapCamera(boolean isHistory, List<Position> positions);
    }

    interface Presenter extends BasePresenter {
        void getActiveJourney();

        void getActiveJourneyPositions();

        void checkPermissions();

        void startTracking();

        void stopTracking();

        void loadJourneys();

        void activateJourney(long journeyId);
    }
}
