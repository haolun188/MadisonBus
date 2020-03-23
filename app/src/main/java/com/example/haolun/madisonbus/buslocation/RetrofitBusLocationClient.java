package com.example.haolun.madisonbus.buslocation;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Haolun on 2020-03-22.
 */
public class RetrofitBusLocationClient{
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://transitdata.cityofmadison.com/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
