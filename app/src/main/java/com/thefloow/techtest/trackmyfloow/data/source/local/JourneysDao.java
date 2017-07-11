package com.thefloow.techtest.trackmyfloow.data.source.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.thefloow.techtest.trackmyfloow.data.Journey;

import java.util.List;

/**
 * Data Access Object for the journeys table.
 */

@Dao
public interface JourneysDao
{
    /**
     * Insert a journey in the database.
     *
     * @param journey the journey to be inserted.
     * @return SQLite rowId used to provide proper callback.
     */
    @Insert
    long insertJourney(Journey journey);

    /**
     * Delete a journey row from the database.
     *
     * @param journey the journey we want deleted.
     */
    @Delete
    void deleteJourney(Journey journey);

    /**
     * Update a journey row.
     *
     * @param journey the journey we want updated.
     */
    @Update
    void updateJourney(Journey journey);

    /**
     * Select all journeys from the database.
     *
     * @return list of all journeys
     */
    @Query("SELECT * FROM Journeys ORDER BY start_date ASC")
    List<Journey> getAllJourneys();

    /**
     * Select journey by id.
     *
     * @param journeyId the journey id.
     * @return the journey with journeyId
     */
    @Query("SELECT * FROM Journeys WHERE id = :journeyId")
    Journey getJourneyById(long journeyId);
}
