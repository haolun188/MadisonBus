package com.example.haolun.madisonbus;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Haolun on 2020-03-18.
 */
public class MapPlotter {
    private static final String TAG = "MapPlotter";
    private GoogleMap mMap;
    private List<Polyline> mPolyLine;

    public MapPlotter() {
        mPolyLine = new LinkedList<>();
    }

    public void setMap(GoogleMap m) {
        mMap = m;
    }

    public void initialize() {
        //TODO: user's location
        LatLng home = new LatLng(43.072538, -89.458626);
        mMap.addMarker(new MarkerOptions().position(home).title("Marker at home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(home));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(home, 15));
    }

    public LatLng getUserLocation() {
        //TODO
        return new LatLng(0,0);
    }

    public void removeRouteFromMap() {
        for(Polyline polyline:mPolyLine)
            polyline.remove();
        mPolyLine.clear();
    }

    public void plotRoute(List<LatLng> points, String rgb) {
        PolylineOptions options = new PolylineOptions()
                .addAll(points)
                .color(Color.parseColor(rgb))
                .visible(true);

        mPolyLine.add(mMap.addPolyline(options));
    }
}
