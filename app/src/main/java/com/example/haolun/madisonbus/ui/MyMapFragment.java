package com.example.haolun.madisonbus.ui;

import android.os.Bundle;

import com.example.haolun.madisonbus.MainActivity;
import com.example.haolun.madisonbus.MapPlotter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Haolun on 2020-03-18.
 */
public class MyMapFragment extends SupportMapFragment implements OnMapReadyCallback {
    private MapPlotter mMap;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ((MainActivity)getActivity()).onMapReady(googleMap);
    }
}
