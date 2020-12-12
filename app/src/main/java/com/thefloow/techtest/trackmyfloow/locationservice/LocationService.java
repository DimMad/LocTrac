package com.thefloow.techtest.trackmyfloow.locationservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.thefloow.techtest.trackmyfloow.Injection;
import com.thefloow.techtest.trackmyfloow.R;
import com.thefloow.techtest.trackmyfloow.journeysmap.JourneysMapActivity;
import com.thefloow.techtest.trackmyfloow.util.AppExecutors;

/**
 * This is the locator service. It will handle all the communication between the location services
 * and the database. It will run as a foreground service to ensure that it has highest priority
 * and the system will never kill it.
 * <p>
 * NOTE: as we want to handle when to start and stop our service we are opting for a Service
 * instead of an IntentService. This also means that we need to write our background worker thread
 * as the normal Service runs in the UI Thread by default.
 * </p>
 */

// TODO: Polish the service MVP design later.
public class LocationService extends Service implements LocationContract.View,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    // TODO: create a separate config file for system wide constants
    private final int FOREGROUND_NOTIFICATION_ID = 101;
    public static final String SERVICE_START_ACTION = "START_FOREGROUND";
    public static final String SERVICE_STOP_ACTION = "STOP_FOREGROUND";
    public static final String NEW_POSITION_BROADCAST = "NEW_POSITION_BROADCAST";
    public static final String NEW_JOURNEY_BROADCAST = "NEW_JOURNEY_BROADCAST";
    public static final String END_JOURNEY_BROADCAST = "END_JOURNEY_BROADCAST";

    LocationPresenter mLocationPresenter;

    // Desired location request interval constants in milliseconds
    private final int LOCATION_INTERVAL = 10000;
    private final int LOCATION_FASTEST_INTERVAL = 5000;

    // Static field to be able to check if service is running
    public static boolean isServiceRunning;

    // Google API client to connect to Play Services
    private GoogleApiClient mGoogleApiClient;
    // LocationRequest used to specify our settings for getting location updates
    private LocationRequest mLocationRequest;

    // The services thread looper and it's executor
    private Looper mServiceLooper;

    // Service lifecycle callback methods
    @Override
    public void onCreate() {
        mLocationPresenter = new LocationPresenter(Injection.provideTasksRepository(getApplicationContext()), this);

        // Initialize and start our thread
        HandlerThread serviceThread = new HandlerThread("TrackingService", Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();

        // Get our thread looper for usage with FusedLocation and database communication
        mServiceLooper = serviceThread.getLooper();

        // Prepare an executor with service's thread looper
        AppExecutors.ServiceThreadExecutor serviceExecutor = new AppExecutors.ServiceThreadExecutor(mServiceLooper);

        mLocationPresenter.setThreadExecutor().setServiceThread(serviceExecutor);

        // Building the google API and setting the location settings
        setLocationRequest();
        buildGoogleApiClient();

        isServiceRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check if intent is null. Will be null if the service has been instantiated once, killed by the
        // system and now is restarted because it was declared as sticky;
        if (intent != null) {
            if (intent.getAction().equals(SERVICE_START_ACTION)) {
                return startLocationService();
            } else if (intent.getAction().equals(SERVICE_STOP_ACTION)) {
                mLocationPresenter.finishNewJourney();
            }
        } else {
            return startLocationService();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;

        // Destroy the current GoogleApiClient
        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            this.mGoogleApiClient = null;
        }
        super.onDestroy();
    }

    // Callbacks needed by the Google Services APIs
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isServiceRunning = true;
        final String journeyNamePrefix = getResources().getString(R.string.journey_default_name_prefix);
        mLocationPresenter.startNewJourney(journeyNamePrefix);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: handle connection suspension if enough time
        isServiceRunning = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: handle connection failure if enough time;
        isServiceRunning = false;
    }

    // Callbacks needed by the Fused Location APIs
    @Override
    public void requestLocationUpdates() {
        // TODO: proper centralized permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Request location updates providing the service thread looper so that Fused Location API
        // callbacks run in the background.
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this, mServiceLooper);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationPresenter.logLocation(location.getLatitude(), location.getLongitude(), location.getTime());
    }

    // Methods from the MVP view interface
    @Override
    public int startLocationService() {
        if (mGoogleApiClient.isConnected())
            return START_STICKY;

        buildGoogleApiClient();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
        showForegroundServiceNotification();

        return START_STICKY;
    }

    @Override
    public void stopLocationService() {
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void sendNewDataBroadcast(String action) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
    }

    @Override
    public void showForegroundServiceNotification() {
        Intent foregNotifIntent = new Intent(this, JourneysMapActivity.class);
        PendingIntent notificationClickIntent = PendingIntent.getActivity(this, 0, foregNotifIntent, 0);

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel("CHANNEL_ID", "name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);


            Notification foregroundNotification = new NotificationCompat.Builder(this)
                    .setChannelId(channel.getId())
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getResources().getString(R.string.foreground_notification_message))
                    .setSmallIcon(R.drawable.the_floow_logo)
                    .setContentIntent(notificationClickIntent)
                    .setTicker(getResources().getString(R.string.foreground_notification_message))
                    .setOngoing(true)
                    .build();

            startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification);
        }
    }

    @Override
    public void setLocationRequest() {
        // Creates a LocationRequest object
        mLocationRequest = new LocationRequest();
        // Use high accuracy for the locations retrieved
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the desired interval (system will not respect it)
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        // Set the desired fastest interval (system respects it and will never provide a location sooner)
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
    }

    @Override
    public synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    //.addApi(ActivityRecognition.API) TODO: check this for the optional goals
                    .build();
        }
    }
}
