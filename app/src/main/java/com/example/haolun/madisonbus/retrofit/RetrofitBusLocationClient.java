package com.example.haolun.madisonbus.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.protobuf.ProtoConverterFactory;

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
                    .addConverterFactory(ProtoConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
