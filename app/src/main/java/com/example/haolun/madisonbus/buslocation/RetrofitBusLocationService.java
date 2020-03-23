package com.example.haolun.madisonbus.buslocation;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Haolun on 2020-03-22.
 */
public interface RetrofitBusLocationService {
    @GET("Vehicle/VehiclePositions.json")
    Call<RetrofitBusLocationInstance> getAllBusLocation();
}
