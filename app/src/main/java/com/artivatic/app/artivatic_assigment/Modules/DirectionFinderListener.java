package com.artivatic.app.artivatic_assigment.Modules;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);


    @POST("/maps/api/directions/json?")
    Call<ResponseBody> getrawdata(@Query("origin") String Origin, @Query("destination") String Destination, @Query("key") String Key, @Query("alternatives") boolean alternatives);
}
