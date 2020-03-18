package com.example.haolun.madisonbus;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public Pair<Float, Float> getStopGPSPosition(String stopName) throws JSONException {
        String latitute;
        String longitute;
        latitute = stops.getJSONObject(stopName).getString("latitute");
        longitute = stops.getJSONObject(stopName).getString("longitute");

        Pair<Float, Float> ret = Pair.create(Float.parseFloat(latitute), Float.parseFloat(longitute));
        return ret;
    }

    public int getNumStops() {
        return routes.length();
    }

    public List<Pair<Float, Float>> getRouteByName(String routeName) throws JSONException {
        JSONArray routesDict = routes.getJSONArray(routeName);
        List<Pair<Float, Float>> ret = new LinkedList<>();
        JSONArray position;
        String latitute;
        String longitute;
        for(int i = 0; i < routesDict.length(); i++) {
            position = routesDict.getJSONArray(i);
            latitute = position.getString(0);
            longitute = position.getString(1);
            ret.add(Pair.create(Float.parseFloat(latitute), Float.parseFloat(longitute)));
        }
        return ret;
    }

    private final String TAG = "Info";
    private JSONObject stops;
    private JSONObject routes;
    private final String stopsJson;
    private final String routesJson;
}
