package com.artivatic.app.artivatic_assigment.Modules;

import android.util.Log;

import com.artivatic.app.artivatic_assigment.MapsActivity;
import com.artivatic.app.artivatic_assigment.Utils.Constants;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by SUNIL on 1/16/2018.
 */

public class DirectionFinder {

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDnwLF2-WfK8cVZt9OoDYJ9Y8kspXhEHfI";
    private DirectionFinderListener listener;
    private String origin;
    private String destination;


    public DirectionFinder(DirectionFinderListener listener, String origin, String destination) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
    }



    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionFinderStart();
        DownloadRawData(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String urlOrigin = URLEncoder.encode(origin, "utf-8");
        String urlDestination = URLEncoder.encode(destination, "utf-8");

        return urlOrigin+urlDestination+GOOGLE_API_KEY;
    }


    private void DownloadRawData(String executeurl){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final DirectionFinderListener map_data = retrofit.create(DirectionFinderListener.class);

        final Call<ResponseBody> response = map_data.getrawdata(origin,destination,GOOGLE_API_KEY);

        response.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){


                    try{
                        String res = response.body().string();
                        Log.e("res",res);

                        List<Route> routes = new ArrayList<Route>();
                        JSONObject jsonData = new JSONObject(res);
                        JSONArray jsonRoutes = jsonData.getJSONArray("routes");

                        for (int i = 0; i < jsonRoutes.length(); i++) {
                            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                            Route route = new Route();

                            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
                            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
                            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
                            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
                            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
                            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
                            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

                            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
                            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
                            route.endAddress = jsonLeg.getString("end_address");
                            route.startAddress = jsonLeg.getString("start_address");
                            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
                            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
                            route.points = decodePolyLine(overview_polylineJson.getString("points"));

                            routes.add(route);
                        }
                        listener.onDirectionFinderSuccess(routes);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
