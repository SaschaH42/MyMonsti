package com.eveningoutpost.dexdrip.services.broadcastservice.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class GraphLine implements Parcelable {

    private List<GraphPoint> values;
    private int color;

    public GraphLine() {
        this.values = new ArrayList<>();
        this.color = 0;
    }

    protected GraphLine(Parcel in) {
        values = in.createTypedArrayList(GraphPoint.CREATOR);
        color = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(values);
        dest.writeInt(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GraphLine> CREATOR = new Creator<GraphLine>() {
        @Override
        public GraphLine createFromParcel(Parcel in) {
            return new GraphLine(in);
        }

        @Override
        public GraphLine[] newArray(int size) {
            return new GraphLine[size];
        }
    };

    public List<GraphPoint> getValues() {
        return values;
    }

    public void setValues(List<GraphPoint> values) {
        this.values = values;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}