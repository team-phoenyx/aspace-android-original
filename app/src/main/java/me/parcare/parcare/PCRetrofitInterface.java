package me.parcare.parcare;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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
}
