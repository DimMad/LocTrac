package com.thefloow.techtest.trackmyfloow.data;



import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

/**
 * This is the immutable Model class for a location Position.
 * It is annotated as an Entity for usage with the Room ORM.
 */

@Entity(tableName = "Positions", foreignKeys = @ForeignKey(entity = Journey.class, parentColumns = "id", childColumns = "journey_id", onDelete = CASCADE),
        indices = {@Index("journey_id")})
public class Position
{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long positionId;

    @ColumnInfo(name = "journey_id")
    private long journeyId;

    private double latitude;

    private double longitude;

    @ColumnInfo(name = "date_time")
    private Date dateTime;

    public Position(long journeyId, double latitude, double longitude, Date dateTime)
    {
        this.journeyId = journeyId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
    }

    public long getPositionId()
    {
        return positionId;
    }

    public void setPositionId(long positionId)
    {
        this.positionId = positionId;
    }

    public long getJourneyId()
    {
        return journeyId;
    }

    public void setJourneyId(long journeyId)
    {
        this.journeyId = journeyId;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public Date getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(Date dateTime)
    {
        this.dateTime = dateTime;
    }
}
