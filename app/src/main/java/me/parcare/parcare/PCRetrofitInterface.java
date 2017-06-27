package me.parcare.parcare;

import java.util.List;

import me.parcare.parcare.retrofitmodels.GeocodingResponse;
import me.parcare.parcare.retrofitmodels.ParkingSpot;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Zula on 6/18/17.
 */

public interface PCRetrofitInterface {
    @FormUrlEncoded
    @POST("spots/single/")
    Call<List<ParkingSpot>> getSpotInfo(@Field("spot_id") String spot_id);

    @FormUrlEncoded
    @POST("spots/onscreen/")
    Call<List<ParkingSpot>> getNearbySpots(@Field("lower_lat") String lower_lat, @Field("lower_lon") String lower_lon,
                                           @Field("upper_lat") String upper_lat, @Field("upper_lon") String upper_long);

    @FormUrlEncoded
    @POST("spots/closest/")
    Call<ParkingSpot> getClosestSpot(@Field("lat") String lat, @Field("lon") String lon);

    @FormUrlEncoded
    @POST("users/profile/update/")
    Call<String> updateProfile(@Field("name") String name, @Field("work_address") String work_address,
                               @Field("home_address") String home_address, @Field("home_loc_id") String home_loc_id,
                               @Field("work_loc_id") String work_loc_id, @Field("user_id") String user_id);

    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<GeocodingResponse> getGeocodingSuggestions(@Path("query") String query, @Query("proximity") String proximityString, @Query("access_token") String accessToken);
}
