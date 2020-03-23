package com.example.haolun.madisonbus.buslocation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Haolun on 2020-03-22.
 */
public class RetrofitBusLocationInstance {
    @SerializedName("entity")
    private List<RetrofitBusInstance> busList;

    @SerializedName("header")
    private BusHeader hearder;

    public RetrofitBusLocationInstance(List<RetrofitBusInstance> busList, BusHeader hearder) {
        this.busList = busList;
        this.hearder = hearder;
    }

    public List<RetrofitBusInstance> getBusList() {
        return busList;
    }

    public void setBusList(List<RetrofitBusInstance> busList) {
        this.busList = busList;
    }

    public BusHeader getHearder() {
        return hearder;
    }

    public void setHearder(BusHeader hearder) {
        this.hearder = hearder;
    }

    private class BusHeader {
        @SerializedName("incrementality")
        private int incrementality;
        @SerializedName("timestamp")
        private long timestamp;
    }
}
