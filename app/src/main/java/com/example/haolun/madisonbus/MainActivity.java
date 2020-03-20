package com.example.haolun.madisonbus;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import androidx.annotation.NonNull;
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

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView mNavigationView;
    private Menu mMenu;
    private Info info;
    private DrawerLayout mDrawer;
    private MapPlotter mMapPlotter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = new Info(getResources().openRawResource(R.raw.stops), getResources().openRawResource(R.raw.routes));
        mMapPlotter = new MapPlotter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mMenu = mNavigationView.getMenu();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(mDrawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mNavigationView, navController);

        mNavigationView.setNavigationItemSelectedListener(this);//TODO: onclick
    }

    private void initDrawerMenu() {
        List<String> routesName = info.getRoutesName();
        //TODO: adjust icon color and shape

        for(int i = 0; i < routesName.size(); i++) {
            MenuItem newItem = mMenu.add(routesName.get(i));
            newItem.setIcon(R.drawable.ic_directions_bus_black_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        //TODO: plot routes
        String routeName = item.getTitle().toString();
        List<List<LatLng>> pointsList = info.getRoutesByName(routeName);
        String colorRgb = info.getColorByName(routeName);
        Log.d(TAG, colorRgb);

        mMapPlotter.removeRouteFromMap();
        for(List<LatLng> points:pointsList)
            mMapPlotter.plotRoute(points, colorRgb);

        mDrawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onMapReady(GoogleMap map) {
        mMapPlotter.setMap(map);
        mMapPlotter.initialize(this);
        mMapPlotter.plotStops(info.getStopsGPSPosition());
    }
}
