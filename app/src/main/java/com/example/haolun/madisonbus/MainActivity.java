package com.example.haolun.madisonbus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView mNavigationView;
    private Menu mMenu;
    private Info info;
    private DrawerLayout mDrawer;
    private MapPlotter mMapPlotter;
    private FusedLocationProviderClient fusedLocationClient;

    private Map<String, Integer> starRouteToItemIdMap;
    private Map<String, Integer> routeToItemIdMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = new Info(getResources().openRawResource(R.raw.stops), getResources().openRawResource(R.raw.routes));
        mMapPlotter = new MapPlotter(info);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mMenu = mNavigationView.getMenu();

        starRouteToItemIdMap = new HashMap<>();
        routeToItemIdMap = new HashMap<>();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(mDrawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);
        mNavigationView.setNavigationItemSelectedListener(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_NETWORK_STATE},
                    101);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationUpdates();
        mMapPlotter.startPeriodicallyPlotBusesLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(new LocationCallback());
        mMapPlotter.stopPlotBusesRealTimeLocation();
    }

    private void initDrawerMenu() {
        List<String> routesName = info.getRoutesName();
        //TODO: adjust icon color and shape

        for(int i = 0; i < routesName.size(); i++) {
            String routeName = routesName.get(i);
            MenuItem newItem = mMenu.add(R.id.all_group, Menu.NONE, Menu.NONE, routeName);
            routeToItemIdMap.put(routeName, newItem.getItemId());
            newItem.setActionView(R.layout.btn_star);
            LinearLayout linearLayout = (LinearLayout) newItem.getActionView();
            ImageButton starButton = (ImageButton) linearLayout.getChildAt(0);
            starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Click " + routeName);
                    ImageButton imageButton = (ImageButton)v;
                    if(!starRouteToItemIdMap.containsKey(routeName)) {
                        // Mark a route as starred route
                        starRoute(imageButton, routeName);
                    } else {
                        // Cancel a starred route
                        // change the icon
                        imageButton.setImageResource(R.drawable.ic_star_border_black_24dp);
                        // remove the route from star group
                    }
                }
            });
        }
    }

    private void starRoute(ImageButton imageButton, String routeName) {
        // change the icon
        imageButton.setImageResource(R.drawable.ic_star_black_24dp);
        // add the route to star group
        MenuItem newItem = mMenu.add(R.id.star_group, Menu.NONE, Menu.NONE, routeName);
        starRouteToItemIdMap.put(routeName, newItem.getItemId());
        newItem.setActionView(R.layout.btn_star);
        LinearLayout linearLayout = (LinearLayout) newItem.getActionView();
        ImageButton starButton = (ImageButton) linearLayout.getChildAt(0);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeStarredRoute(routeName);
            }
        });
        // Re-render the menu
    }

    private void removeStarredRoute(String routeName) {
        if(!starRouteToItemIdMap.containsKey(routeName))
            throw new Error("Route " + routeName + " is not starred.");

        int starId = starRouteToItemIdMap.get(routeName);
        mMenu.removeItem(starId);

        int routeId = routeToItemIdMap.get(routeName);
        for(int i = 0; i < mMenu.size(); i++) {
            Log.d(TAG, i + ":" + mMenu.getItem(i).getTitle().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        initDrawerMenu();
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String routeName = item.getTitle().toString();
        List<List<LatLng>> pointsList = info.getRoutesByName(routeName);
        String colorRgb = info.getColorByName(routeName);

        mToolbar.setTitle(routeName);
        mToolbar.setBackgroundColor(Color.parseColor(colorRgb));

        mMapPlotter.setBusSelected(routeName);
        mMapPlotter.removeRouteFromMap();
        for(List<LatLng> points:pointsList)
            mMapPlotter.plotRoute(points, colorRgb);
        mMapPlotter.plotBusesLocation();

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onMapReady(GoogleMap map) {
        mMapPlotter.setMap(map);
        mMapPlotter.initialize(this);

        // Check location permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        } else {
            setUserLocationOnMap();
        }

//        mMapPlotter.plotStops();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for(int code:grantResults) {
            if(code == 100)
                setUserLocationOnMap();
        }
    }

    private void setUserLocationOnMap() {
        Log.d(TAG, "setUserLocationOnMap");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mMapPlotter.plotUserLocation(new LatLng(location.getLatitude(), location.getLongitude()), true, true);
                        }
                        startUserLocationUpdates();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LatLng madison = new LatLng(43.073190, -89.404951);
                        mMapPlotter.plotUserLocation(madison, false, true);
                    }
                });
    }

    private void startUserLocationUpdates() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        Log.d(TAG, "startLocationUpdates");
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(3000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    mMapPlotter.plotUserLocation(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), true, false);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }
}
