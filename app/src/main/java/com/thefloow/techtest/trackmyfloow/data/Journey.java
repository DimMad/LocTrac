package com.thefloow.techtest.trackmyfloow.data;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * This is the immutable Model class for a Journey.
 * It is annotated as an Entity for usage with the Room ORM.
 */

@Entity(tableName = "Journeys")
public final class Journey {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long journeyId;

    @ColumnInfo(name = "name")
    private String journeyName;

    @ColumnInfo(name = "start_date")
    private Date startDate;

    @ColumnInfo(name = "end_date")
    private Date endDate;

    public Journey(String journeyName, Date startDate) {
        this.journeyName = journeyName;
        this.startDate = startDate;
        this.endDate = null;
    }

    public long getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(long journeyId) {
        this.journeyId = journeyId;
    }

    public String getJourneyName() {
        return journeyName;
    }

    public void setJourneyName(String journeyName) {
        this.journeyName = journeyName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
