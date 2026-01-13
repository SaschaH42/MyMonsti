package com.eveningoutpost.dexdrip.services.broadcastservice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Settings implements Parcelable {

    private String apkName;
    private boolean displayGraph;
    private long graphStart;
    private long graphEnd;

    public Settings() {
        // Default constructor
    }

    protected Settings(Parcel in) {
        // The order of reading must match the order of writing in writeToParcel
        apkName = in.readString();
        graphStart = in.readLong();
        graphEnd = in.readLong();
        displayGraph = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // The order of writing is critical and must be matched when reading
        dest.writeString(apkName);
        dest.writeLong(graphStart);
        dest.writeLong(graphEnd);
        dest.writeInt(displayGraph ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Settings> CREATOR = new Creator<Settings>() {
        @Override
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };

    // Getters and Setters
    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public boolean isDisplayGraph() {
        return displayGraph;
    }

    public void setDisplayGraph(boolean displayGraph) {
        this.displayGraph = displayGraph;
    }

    public long getGraphStart() {
        return graphStart;
    }

    public void setGraphStart(long graphStart) {
        this.graphStart = graphStart;
    }

    public long getGraphEnd() {
        return graphEnd;
    }

    public void setGraphEnd(long graphEnd) {
        this.graphEnd = graphEnd;
    }
}