package com.example.haolun.madisonbus;

import android.app.ApplicationErrorReport;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Haolun on 2020-03-15.
 */
public class Info {
    public Info(InputStream stopsStream, InputStream routesStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(stopsStream));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            stopsJson = total.toString();

            r = new BufferedReader(new InputStreamReader(routesStream));
            total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            routesJson = total.toString();
        } catch (IOException e) {
            throw new Error(e.toString());
        }

        try {
            stops = new JSONObject(stopsJson);
            routes = new JSONObject(routesJson);
            Log.d(TAG, String.valueOf(routes.length()));
        } catch (JSONException e) {
            throw new Error(e.toString());
        }
    }

    public LatLng getStopGPSPosition(String stopName) throws JSONException {
        String latitude;
        String longitude;
        latitude = stops.getJSONObject(stopName).getString("latitute");
        longitude = stops.getJSONObject(stopName).getString("longitute");

        return new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude));
    }

    public int getNumStops() {
        return routes.length();
    }

    public List<String> getRoutesName() {
        List<String> ret = new LinkedList<>();
        String stopName;
        Iterator<String> iter = routes.keys(); //This should be the iterator you want.
        while(iter.hasNext()){
            stopName = iter.next();
            ret.add(stopName);
        }
        return ret;
    }

    public List<List<LatLng>> getRoutesByName(String routeName) {
        List<List<LatLng>> ret = new LinkedList<>();
        try{
            JSONArray routesByName = routes.getJSONObject(routeName).getJSONArray("route");
            List<LatLng> tmp;
            JSONArray position;
            String latitude;
            String longitude;
            Log.d(TAG, routeName + ":" + String.valueOf(routesByName.length()));
            for(int i = 0; i < routesByName.length(); i++) {
                tmp = new LinkedList<>();
                JSONArray oneOfRoutes = routesByName.getJSONArray(i);
                for(int j = 0; j < oneOfRoutes.length(); j++) {
                    position = oneOfRoutes.getJSONArray(j);
                    latitude = position.getString(0);
                    longitude = position.getString(1);
                    tmp.add(new LatLng(Float.parseFloat(latitude), Float.parseFloat(longitude)));
                }
                ret.add(tmp);
            }
        } catch (JSONException e) {
            throw new Error(e.toString());
        }

        return ret;
    }

    private final String TAG = "Info";
    private JSONObject stops;
    private JSONObject routes;
    private final String stopsJson;
    private final String routesJson;

    public String getColorByName(String routeName) {
        try {
            String color = routes.getJSONObject(routeName).getString("color");
            return "#" + color;
        } catch (JSONException e) {
            throw new Error(e.toString());
        }
    }
}
