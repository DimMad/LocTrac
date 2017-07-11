package com.thefloow.techtest.trackmyfloow.journeysmap;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.thefloow.techtest.trackmyfloow.R;
import com.thefloow.techtest.trackmyfloow.data.Journey;
import com.thefloow.techtest.trackmyfloow.data.Position;
import com.thefloow.techtest.trackmyfloow.locationservice.LocationService;
import com.thefloow.techtest.trackmyfloow.util.DateFormatter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple {@link Fragment} subclass.
 */
public class JourneysMapFragment extends Fragment implements JourneysMapContract.View
{
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    private RecyclerView mListView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mJourneyListAdapter;


    private LinearLayout llClearHistoryView;
    private ImageView imgClearButton;
    private TextView txtClearTitle;

    private LinearLayout llJourneyHistory;
    private LinearLayout llEmptyJourneyHistory;

    private GoogleMap mMap;
    private MapView mMapView;
    private BottomSheetBehavior mBottomSheetBehavior;

    private LocalBroadcastManager mLocalBroadcastManager;

    private JourneysMapContract.Presenter mJourneysMapPresenter;

    private boolean isShowingHistory = false;

    private boolean journeyStarted = false;

    private Polyline drawnPolyline;

    public JourneysMapFragment()
    {
        // Required empty public constructor
    }

    public static JourneysMapFragment newInstance()
    {
        return new JourneysMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mJourneyListAdapter = new JourneysAdapter(new ArrayList<Journey>(0), mHistoryItemListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        // Setup Google Maps.
        mMapView = (MapView) root.findViewById(R.id.google_maps);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        // Setup bottom sheet.
        View mBottomSheet = root.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setHideable(false);
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (60 * scale + 0.5f);
        mBottomSheetBehavior.setPeekHeight(pixels);

        // Setup journey history views.
        llJourneyHistory = (LinearLayout) root.findViewById(R.id.ll_journeys);
        mListView = (RecyclerView) root.findViewById(R.id.list_journeys_History);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setAdapter(mJourneyListAdapter);

        // Setup no journey history view.
        llEmptyJourneyHistory = (LinearLayout) root.findViewById(R.id.ll_no_journeys);

        // Setup history item UI tools.
        llClearHistoryView = (LinearLayout) root.findViewById(R.id.ll_clear_journey);
        imgClearButton = (ImageView) root.findViewById(R.id.img_button_clear_journey);
        txtClearTitle = (TextView) root.findViewById(R.id.txt_clear_journey);

        imgClearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                llClearHistoryView.setVisibility(View.INVISIBLE);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mBottomSheetBehavior.setHideable(false);
                isShowingHistory = false;
                mJourneysMapPresenter.getActiveJourneyPositions();
            }
        });

        return root;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if (mMapView != null)
            mMapView.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mJourneysMapPresenter.start();

        if (mMapView != null)
            mMapView.onResume();

        if (LocationService.isServiceRunning)
            registerBroadcastReceiver();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (mMapView != null)
            mMapView.onPause();

        if (LocationService.isServiceRunning)
            unregisterBroadcastReceiver();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mMapView != null)
            mMapView.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mMapView != null)
            mMapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        if (mMapView != null)
            mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (mMapView != null)
            mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mJourneysMapPresenter.checkPermissions();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_exit_app:
                if (LocationService.isServiceRunning)
                    mJourneysMapPresenter.stopTracking();
                getActivity().finish();
                break;
            case R.id.menu_tracking_toggle:
                if (item.isChecked())
                {
                    item.setChecked(false);
                    item.setIcon(R.drawable.ic_visibility_off_white_24dp);
                    mJourneysMapPresenter.stopTracking();
                }
                else
                {
                    item.setChecked(true);
                    item.setIcon(R.drawable.ic_visibility_white_24dp);
                    mJourneysMapPresenter.startTracking();
                }
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        mJourneysMapPresenter.checkPermissions();
    }

    // View interface implementations
    @Override
    public void setPresenter(@NonNull JourneysMapContract.Presenter presenter)
    {
        mJourneysMapPresenter = checkNotNull(presenter);
    }

    @Override
    public void requestPermissions()
    {
        requestPermissions(PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
    }

    /**
     * This forces the user to accept all possible required permissions a the start of the application
     * Would be better to create a request util and be able to handle each permission where required.
     *
     * @return boolean indicating whether all permission have been accepted.
     */
    @Override
    public boolean hasPermissions()
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            boolean hasPermission = true;
            for (String permission : PERMISSIONS)
            {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
                {
                    hasPermission = false;
                    break;
                }
            }
            return hasPermission;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void startLocationService()
    {
        Intent locationServiceIntent = new Intent(getActivity(), LocationService.class);
        if (!LocationService.isServiceRunning)
        {
            locationServiceIntent.setAction(LocationService.SERVICE_START_ACTION);
            getActivity().startService(locationServiceIntent);
        }
    }

    @Override
    public void stopLocationService()
    {
        Intent locationServiceIntent = new Intent(getActivity(), LocationService.class);
        if (LocationService.isServiceRunning)
        {
            locationServiceIntent.setAction(LocationService.SERVICE_STOP_ACTION);
            getActivity().startService(locationServiceIntent);
        }
    }

    @Override
    public void enableMapCurrentLocationLayer()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Handle this properly later.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void showJourneyHistory(List<Journey> journeys)
    {
        llEmptyJourneyHistory.setVisibility(View.GONE);
        llJourneyHistory.setVisibility(View.VISIBLE);
        ((JourneysAdapter) mJourneyListAdapter).refreshList(journeys);
    }

    @Override
    public void showNoJourneys()
    {
        llEmptyJourneyHistory.setVisibility(View.VISIBLE);
        llJourneyHistory.setVisibility(View.GONE);
    }

    @Override
    public boolean isActive()
    {
        return isAdded();
    }

    @Override
    public void registerBroadcastReceiver()
    {
        if (mLocalBroadcastManager == null)
        {
            IntentFilter broadcastFilter = new IntentFilter();
            broadcastFilter.addAction(LocationService.NEW_POSITION_BROADCAST);
            broadcastFilter.addAction(LocationService.NEW_JOURNEY_BROADCAST);
            broadcastFilter.addAction(LocationService.END_JOURNEY_BROADCAST);
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
            mLocalBroadcastManager.registerReceiver(mMessageReceiver, broadcastFilter);
        }
    }

    @Override
    public void unregisterBroadcastReceiver()
    {
        if (mLocalBroadcastManager != null)
        {
            mLocalBroadcastManager.unregisterReceiver(mMessageReceiver);
            mLocalBroadcastManager = null;
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            switch (intent.getAction())
            {
                case LocationService.NEW_JOURNEY_BROADCAST:
                    mJourneysMapPresenter.getActiveJourney();
                    mJourneysMapPresenter.loadJourneys();
                    break;
                case LocationService.NEW_POSITION_BROADCAST:
                    if (!isShowingHistory)
                        mJourneysMapPresenter.getActiveJourneyPositions();
                    break;
                case LocationService.END_JOURNEY_BROADCAST:
                    mJourneysMapPresenter.loadJourneys();
                    clearMapPolyline();
                    unregisterBroadcastReceiver();
                    break;
            }
        }
    };

    @Override
    public void drawMapPolyline(List<Position> positions)
    {
        PolylineOptions options = new PolylineOptions().color(Color.BLUE).geodesic(true).width(10);
        for (Position position : positions)
            options.add(new LatLng(position.getLatitude(), position.getLongitude()));

        drawnPolyline = mMap.addPolyline(options);
    }

    @Override
    public void clearMapPolyline()
    {
        if (drawnPolyline != null)
            drawnPolyline.remove();

        drawnPolyline = null;
    }

    @Override
    public void centerMapCamera(boolean isHistory, List<Position> positions)
    {
        if (!isHistory)
        {
            int lastIndex = positions.size() - 1;
            LatLng pointToCenter = new LatLng(positions.get(lastIndex).getLatitude(), positions.get(lastIndex).getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLng(pointToCenter);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);
        }
        // TODO: camera to fit route of history journey
    }

    /**
     * Listener for journey history item clicks.
     */
    JourneyHistoryItemListener mHistoryItemListener = new JourneyHistoryItemListener()
    {
        @Override
        public void onJourneyClick(Journey clickedJourney)
        {
            isShowingHistory = true;
            llClearHistoryView.setVisibility(View.VISIBLE);
            txtClearTitle.setText(clickedJourney.getJourneyName());
            mBottomSheetBehavior.setHideable(true);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            mJourneysMapPresenter.activateJourney(clickedJourney.getJourneyId());
        }

        @Override
        public void onEditJourneyTitleClick(Journey clickedJourney)
        {
            // Stub connected to adapter on click.
        }
    };

    /**
     * Adapter class for the journeys list.
     * Recycler view is used due to being inherently easier to handle and more efficient.
     */
    private static class JourneysAdapter extends RecyclerView.Adapter<JourneysAdapter.ViewHolder>
    {
        private List<Journey> mJourneys;
        private JourneyHistoryItemListener mItemListener;

        // Create a holder that contains the structure of the layout used for each line of the list.
        public static class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView txtJourneyTitle;
            TextView txtStartDate;
            TextView txtEndDate;
            Button btnEditTitle;

            public ViewHolder(View view)
            {
                super(view);
                // Init all views.
                txtJourneyTitle = (TextView) view.findViewById(R.id.edt_journey_title);
                txtStartDate = (TextView) view.findViewById(R.id.txt_start_date);
                txtEndDate = (TextView) view.findViewById(R.id.txt_end_date);
                btnEditTitle = (Button) view.findViewById(R.id.btn_edit_journey_title);
            }

            // Method used to bind the views of each row with their data.
            public void bind(final Journey journey, final JourneyHistoryItemListener clickListener)
            {
                txtJourneyTitle.setText(journey.getJourneyName());
                txtStartDate.setText(DateFormatter.getDateTime(journey.getStartDate()));
                if (journey.getEndDate() == null)
                    txtEndDate.setText(R.string.ongoing_journey_message);
                else
                    txtEndDate.setText(DateFormatter.getDateTime(journey.getEndDate()));

                itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (journey.getEndDate() != null)
                        {
                            clickListener.onJourneyClick(journey);
                        }
                    }
                });

                btnEditTitle.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // TODO: implement at some point
                    }
                });
            }
        }

        // Adapter methods.
        public JourneysAdapter(List<Journey> journeys, JourneyHistoryItemListener itemListener)
        {
            setList(journeys);
            mItemListener = itemListener;
        }

        private void setList(List<Journey> journeys)
        {
            mJourneys = checkNotNull(journeys);
        }

        public void refreshList(List<Journey> journeys)
        {
            setList(journeys);
            this.notifyDataSetChanged();
        }

        // Inflate the row layout and create reference for the recycler view to use.
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.journey_history_item, parent, false);
            return new ViewHolder(rowView);
        }

        // Bind each row view to a view in the recycler view.
        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {
            holder.bind(mJourneys.get(position), mItemListener);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public int getItemCount()
        {
            return mJourneys.size();
        }
    }

    /**
     * Click listener to communicate clicks from the history list to the view.
     */
    public interface JourneyHistoryItemListener
    {
        void onJourneyClick(Journey clickedJourney);

        void onEditJourneyTitleClick(Journey clickedJourney);
    }
}
