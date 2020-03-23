package com.example.haolun.madisonbus.buslocation;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Haolun on 2020-03-22.
 */
public class RetrofitBusInstance {
    @SerializedName("alert")
    private String alert;

    @SerializedName("id")
    private String id;

    @SerializedName("trip_update")
    private String trip_update;

    @SerializedName("vehicle")
    private Vehicle vehicle;

    public RetrofitBusInstance(String alert, String id, String trip_update, Vehicle vehicle) {
        this.alert = alert;
        this.id = id;
        this.trip_update = trip_update;
        this.vehicle = vehicle;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrip_update() {
        return trip_update;
    }

    public void setTrip_update(String trip_update) {
        this.trip_update = trip_update;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public String toString() {
        return "RetrofitBusInstance{" +
                "alert='" + alert + '\'' +
                ", id='" + id + '\'' +
                ", trip_update='" + trip_update + '\'' +
                ", vehicle=" + vehicle.toString() +
                '}';
    }

    public LatLng getPosition() {
        return this.vehicle.getPosition();
    }
    public String getRouteId() {
        Log.d("BUSINSTANCE", this.toString());
        return this.vehicle.getRouteId();
    }

    private class Vehicle {
        @SerializedName("position")
        private Position position;

        @SerializedName("trip")
        private Trip trip;

        @Override
        public String toString() {
            return "Vehicle{" +
                    "position=" + position.toString() +
                    ", trip=" + trip.toString() +
                    '}';
        }

        public LatLng getPosition() {
            float lat, lng;
            lat = this.position.getLatitude();
            lng = this.position.getLongitude();
            return new LatLng(lat, lng);
        }

        public String getRouteId() {
            return this.trip.getRouteId();
        }
    }

    private class Position{
        @SerializedName("latitude")
        private float latitude;

        @SerializedName("longitude")
        private float longitude;

        public Position(float latitude, float longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public float getLatitude() {
            return latitude;
        }

        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }

        public float getLongitude() {
            return longitude;
        }

        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "Position{" +
                    "latitude=" + latitude +
                    ", longitude=" + longitude +
                    '}';
        }
    }

    private class Trip{
        @SerializedName("route_id")
        private String routeId;

        public Trip(String routeId) {
            this.routeId = routeId;
        }

        public String getRouteId() {
            return routeId;
        }

        public void setRouteId(String routeId) {
            this.routeId = routeId;
        }

        @Override
        public String toString() {
            return "Trip{" +
                    "routeId='" + routeId + '\'' +
                    '}';
        }
    }
}
