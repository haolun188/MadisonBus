package com.example.haolun.madisonbus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.haolun.madisonbus.buslocation.RetrofitBusInstance;
import com.example.haolun.madisonbus.buslocation.RetrofitBusLocationClient;
import com.example.haolun.madisonbus.buslocation.RetrofitBusLocationInstance;
import com.example.haolun.madisonbus.buslocation.RetrofitBusLocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Haolun on 2020-03-18.
 */
public class MapPlotter {
    private static final String TAG = "MapPlotter";
    private GoogleMap mMap;
    private List<Polyline> mPolyLine;
    private BitmapDescriptor stopIcon;
    private BitmapDescriptor busIcon;
    private BitmapDescriptor userIcon;
    private String busSelected;
    private Info mInfo;
    private Timer mTimer;
    private Callback<RetrofitBusLocationInstance> mBusLocationCallback;
    private List<Marker> mRealTimeBuses;

    public MapPlotter(Info info) {
        mPolyLine = new LinkedList<>();
        mRealTimeBuses = new LinkedList<>();
        busSelected = "";
        mInfo = info;
    }

    public void setMap(GoogleMap m) {
        mMap = m;
    }

    public void setBusSelected(String busSelected) {
        this.busSelected = busSelected;
    }
    public void initialize(Context context) {
        stopIcon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_stop);
        busIcon = bitmapDescriptorFromVector(context, R.drawable.ic_directions_bus_black_16dp); //TODO: update icon
        userIcon = bitmapDescriptorFromVector(context, R.drawable.ic_dot);

        mBusLocationCallback = new Callback<RetrofitBusLocationInstance>() {
            @Override
            public void onResponse(Call<RetrofitBusLocationInstance> call, Response<RetrofitBusLocationInstance> response) {
                RetrofitBusLocationInstance retrofitBusLocationInstance = response.body();
                plotRealTimeBuses(retrofitBusLocationInstance.getBusList());
            }
            @Override
            public void onFailure(Call<RetrofitBusLocationInstance> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        };

        this.startPeriodicallyPlotBusesLocation();
    }

    public void plotUserLocation(LatLng location, boolean plotUserMarker, boolean moveCamera) {
        if(plotUserMarker)
            mMap.addMarker(new MarkerOptions().position(location).icon(userIcon));
        if(moveCamera) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void plotStops() {
        for(LatLng stop:mInfo.getStopsGPSPosition()) {
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

    public void plotRealTimeBuses(List<RetrofitBusInstance> buses) {
        removeBusesFromMap();
        for(RetrofitBusInstance bus:buses){
            String routeName = mInfo.getNameById(bus.getRouteId());
            if(routeName.equals(busSelected)) {
                // plot bus' real-time location on map
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(bus.getPosition())
                        .icon(busIcon));
                mRealTimeBuses.add(marker);
            }
        }
    }

    private void removeBusesFromMap() {
        for(Marker busMarker:mRealTimeBuses){
            busMarker.remove();
        }
        mRealTimeBuses.clear();
    }

    private TimerTask generateQueryBusLocationTask() {
        RetrofitBusLocationService service = RetrofitBusLocationClient
                .getRetrofitInstance()
                .create(RetrofitBusLocationService.class);
        TimerTask mQueryBusLocationTask = new TimerTask() {
            @Override
            public void run() {
                Call<RetrofitBusLocationInstance> call = service.getAllBusLocation();
                call.enqueue(mBusLocationCallback);
            }
        };
        return mQueryBusLocationTask;
    }

    public void plotBusesLocation() {
        if(mBusLocationCallback == null)
            return;
        mTimer = new Timer();
        mTimer.schedule(generateQueryBusLocationTask(), 0);
    }

    public void startPeriodicallyPlotBusesLocation() {
        if(mBusLocationCallback == null)
            return;
        mTimer = new Timer();
        mTimer.schedule(generateQueryBusLocationTask(), 0, 10000);
    }

    public void stopPlotBusesRealTimeLocation() {
        mTimer.cancel();
    }
}
