package com.example.haolun.madisonbus;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private SharedPreferences mSharedPref;
    private Set<String> mStarredRoutesSet;

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

        mStarredRoutesSet = new HashSet<>();

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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> setUserLocationOnMap());
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

        mSharedPref = getPreferences(Context.MODE_PRIVATE);
        mStarredRoutesSet = mSharedPref.getStringSet(getString(R.string.starredRouteKey), mStarredRoutesSet);

        // Set up title
        MenuItem starRoutesTitle = mMenu.add(R.id.star_group, Menu.NONE, Menu.NONE, getString(R.string.starred_route_title));
        starRoutesTitle.setEnabled(false);
        // Add routes
        for(int i = 0; i < routesName.size(); i++) {
            String routeName = routesName.get(i);
            MenuItem newItem = mMenu.add(R.id.star_group, Menu.NONE, Menu.NONE, routeName);
            newItem.setActionView(R.layout.btn_star);
            LinearLayout linearLayout = (LinearLayout) newItem.getActionView();
            ImageButton starButton = (ImageButton) linearLayout.getChildAt(0);
            starButton.setImageResource(R.drawable.ic_star_orange_light_24dp);
            starButton.setOnClickListener(v -> {
                // Cancel a starred route
                removeStarredRoute(routeName);
            });
            if(mStarredRoutesSet.contains(routeName))
                newItem.setVisible(true);
            else
                newItem.setVisible(false);
        }

        // Set up title
        MenuItem allRoutesTitle = mMenu.add(R.id.star_group, Menu.NONE, Menu.NONE, getString(R.string.all_routes_title));
        allRoutesTitle.setEnabled(false);
        // Add routes
        for(int i = 0; i < routesName.size(); i++) {
            String routeName = routesName.get(i);
            MenuItem newItem = mMenu.add(R.id.all_group, Menu.NONE, Menu.NONE, routeName);
            newItem.setActionView(R.layout.btn_star);
            LinearLayout linearLayout = (LinearLayout) newItem.getActionView();
            ImageButton starButton = (ImageButton) linearLayout.getChildAt(0);
            starButton.setOnClickListener(v -> {
                ImageButton imageButton = (ImageButton)v;
                if(!mStarredRoutesSet.contains(routeName)) {
                    // Mark a route as starred route
                    starRoute(routeName);
                } else {
                    // Cancel a starred route
                    // remove the route from star group
                    removeStarredRoute(routeName);
                }
            });

            // Set user-saved starred route
            if(mStarredRoutesSet.contains(routeName)) {
                starButton.setImageResource(R.drawable.ic_star_orange_light_24dp);
            }
        }
    }

    private void starRoute(String routeName) {
        // Show the menu item in starred group
        for(int i = 0; i < mMenu.size(); i++) {
            if(mMenu.getItem(i).getTitle() == routeName) {
                mMenu.getItem(i).setVisible(true);
                break;
            }
        }
        // change the icon
        for(int i = 0; i < mMenu.size(); i++) {
            if(mMenu.getItem(i).getTitle() == routeName) {
                ImageButton imageButton = (ImageButton) ((LinearLayout)mMenu.getItem(i).getActionView()).getChildAt(0);
                imageButton.setImageResource(R.drawable.ic_star_orange_light_24dp);
            }
        }

        mStarredRoutesSet.add(routeName);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putStringSet(getString(R.string.starredRouteKey), mStarredRoutesSet);
        editor.apply();
    }

    private void removeStarredRoute(String routeName) {
        if(!mStarredRoutesSet.contains(routeName))
            throw new Error("Route " + routeName + " is not starred.");

        // Hide the menu item in starred group
        for(int i = 0; i < mMenu.size(); i++) {
            if(mMenu.getItem(i).getTitle() == routeName) {
                mMenu.getItem(i).setVisible(false);
                break;
            }
        }
        // change the icon
        for(int i = 0; i < mMenu.size(); i++) {
            if(mMenu.getItem(i).getTitle() == routeName) {
                ImageButton imageButton = (ImageButton) ((LinearLayout)mMenu.getItem(i).getActionView()).getChildAt(0);
                imageButton.setImageResource(R.drawable.ic_star_border_orange_light_24dp);
            }
        }

        // Cannot just update because the return set should be treated as immutable. https://developer.android.com/reference/android/content/SharedPreferences#developer-guides
        // Create a new set instead
        mStarredRoutesSet = new HashSet<>(mStarredRoutesSet);
        mStarredRoutesSet.remove(routeName);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putStringSet(getString(R.string.starredRouteKey), mStarredRoutesSet);
        editor.apply();
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
            startUserLocationUpdates();
        }

        mMapPlotter.plotStops();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for(int code:grantResults) {
            if(code == 100) {
                setUserLocationOnMap();
                startUserLocationUpdates();
            }
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
