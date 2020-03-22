package com.example.haolun.madisonbus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

/**
 * Created by Haolun on 2020-03-18.
 */
public class MapPlotter {
    private static final String TAG = "MapPlotter";
    private GoogleMap mMap;
    private List<Polyline> mPolyLine;
    private BitmapDescriptor stopIcon;

    public MapPlotter() {
        mPolyLine = new LinkedList<>();
    }

    public void setMap(GoogleMap m) {
        mMap = m;
    }

    public void initialize(Context context) {
        stopIcon = bitmapDescriptorFromVector(context, R.drawable.ic_directions_bus_black_24dp);
    }

    public void setUserLocation(LatLng location, boolean plotUserMarker) {
        if(plotUserMarker) // TODO: update marker icon
            mMap.addMarker(new MarkerOptions().position(location).title("Marker at home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void plotStops(List<LatLng> stops) {
        for(LatLng stop:stops) {
            //TODO: update stop icon
            mMap.addMarker(new MarkerOptions()
                    .position(stop)
                    .icon(stopIcon));
        }
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
