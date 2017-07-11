package com.thefloow.techtest.trackmyfloow.journeysmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.thefloow.techtest.trackmyfloow.Injection;
import com.thefloow.techtest.trackmyfloow.R;

public class JourneysMapActivity extends AppCompatActivity
{
    private JourneysMapPresenter mJourneysMapPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Setting up a toolbar as the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check and load fragment if not already loaded
        JourneysMapFragment journeysMapFragment = (JourneysMapFragment) getSupportFragmentManager().findFragmentById(R.id.container_map_view);
        if (journeysMapFragment == null)
        {
            journeysMapFragment = JourneysMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.container_map_view, journeysMapFragment).commit();
        }

        mJourneysMapPresenter = new JourneysMapPresenter(Injection.provideTasksRepository(getApplicationContext()), journeysMapFragment);
    }
}
