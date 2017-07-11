package com.thefloow.techtest.trackmyfloow.data.source.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.thefloow.techtest.trackmyfloow.data.Position;

import java.util.List;

/**
 * Data Access Object for the positions table.
 * <p>
 * No delete query was created as the on delete relationship of journey and position is cascade.
 */

@Dao
public interface PositionsDao
{
    /**
     * A position of the user during a journey.
     *
     * @param position the position to be inserted.
     * @return SQLite rowId used to provide proper callback.
     */
    @Insert
    long insertPosition(Position position);

    /**
     * Select all positions of given journey id.
     *
     * @param journeyId the journey of journeyId for which we want the positions.
     * @return list of all the positions for given journey.
     */
    @Query("SELECT * FROM Positions WHERE journey_id = :journeyId ORDER BY date_time ASC")
    List<Position> getJourneyPositions(Long journeyId);
}
