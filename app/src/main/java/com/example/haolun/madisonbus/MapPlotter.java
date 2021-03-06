package com.example.haolun.madisonbus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.haolun.madisonbus.retrofit.RetrofitBusLocationClient;
import com.example.haolun.madisonbus.retrofit.RetrofitBusLocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.transit.realtime.GtfsRealtimeProtos;

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
    private BitmapDescriptor bus0Icon;
    private BitmapDescriptor bus45Icon;
    private BitmapDescriptor bus90Icon;
    private BitmapDescriptor bus135Icon;
    private BitmapDescriptor bus180Icon;
    private BitmapDescriptor bus225Icon;
    private BitmapDescriptor bus270Icon;
    private BitmapDescriptor bus315Icon;
    private BitmapDescriptor userIcon;
    private String busSelected;
    private Info mInfo;
    private Timer mTimer;
    private Callback<GtfsRealtimeProtos.FeedMessage> mBusLocationCallback;
    private List<Marker> mRealTimeBuses;
    private Marker mUserMarker;

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
        busIcon = bitmapDescriptorFromVector(context, R.drawable.ic_bus);
        bus0Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_0);
        bus45Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_45);
        bus90Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_90);
        bus135Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_135);
        bus180Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_180);
        bus225Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_225);
        bus270Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_270);
        bus315Icon = bitmapDescriptorFromVector(context, R.drawable.ic_bus_315);
        userIcon = bitmapDescriptorFromVector(context, R.drawable.ic_dot);

        mBusLocationCallback = new Callback<GtfsRealtimeProtos.FeedMessage>() {
            @Override
            public void onResponse(Call<GtfsRealtimeProtos.FeedMessage> call, Response<GtfsRealtimeProtos.FeedMessage> response) {
                GtfsRealtimeProtos.FeedMessage feedMessage = response.body();
                plotRealTimeBuses(feedMessage);
            }
            @Override
            public void onFailure(Call<GtfsRealtimeProtos.FeedMessage> call, Throwable t) {
                Log.d(TAG, t.toString());
            }
        };

        this.startPeriodicallyPlotBusesLocation();
    }

    public void plotUserLocation(LatLng location, boolean plotUserMarker, boolean moveCamera) {
        if(plotUserMarker) {
            if(mUserMarker != null)
                mUserMarker.remove();
            mUserMarker = mMap.addMarker(new MarkerOptions().position(location).icon(userIcon));
        }
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

    public void plotRealTimeBuses(GtfsRealtimeProtos.FeedMessage feedMessage) {
        removeBusesFromMap();
        for(GtfsRealtimeProtos.FeedEntity feedEntity:feedMessage.getEntityList()){
            String routeName = mInfo.getNameById(feedEntity.getVehicle().getTrip().getRouteId());
            if(routeName.equals(busSelected)) {
                // plot bus' real-time location on map
                float lat = feedEntity.getVehicle().getPosition().getLatitude();
                float lng = feedEntity.getVehicle().getPosition().getLongitude();
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng)));
                switch ((int)feedEntity.getVehicle().getPosition().getBearing()) {
                    case 0:
                        marker.setIcon(bus0Icon);
                        break;
                    case 45:
                        marker.setIcon(bus45Icon);
                        break;
                    case 90:
                        marker.setIcon(bus90Icon);
                        break;
                    case 135:
                        marker.setIcon(bus135Icon);
                        break;
                    case 180:
                        marker.setIcon(bus180Icon);
                        break;
                    case 225:
                        marker.setIcon(bus225Icon);
                        break;
                    case 270:
                        marker.setIcon(bus270Icon);
                        break;
                    case 315:
                        marker.setIcon(bus315Icon);
                        break;
                    default:
                        marker.setIcon(busIcon);
                        Log.d(TAG, "angle:" + feedEntity.toString());
                }
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
//                Call<RetrofitBusLocationInstance> call = service.getAllBusLocation();
                Call<GtfsRealtimeProtos.FeedMessage> call = service.getAllBusLocationPb();
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
        mTimer.schedule(generateQueryBusLocationTask(), 0, 5000);
    }

    public void stopPlotBusesRealTimeLocation() {
        if(mTimer != null)
            mTimer.cancel();
    }
}
