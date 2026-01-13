package com.eveningoutpost.dexdrip.services.broadcastservice.models;

import android.os.Parcel;
import android.os.Parcelable;

public class GraphPoint implements Parcelable {

    private float x;
    private float y;

    public GraphPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    protected GraphPoint(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GraphPoint> CREATOR = new Creator<GraphPoint>() {
        @Override
        public GraphPoint createFromParcel(Parcel in) {
            return new GraphPoint(in);
        }

        @Override
        public GraphPoint[] newArray(int size) {
            return new GraphPoint[size];
        }
    };

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}