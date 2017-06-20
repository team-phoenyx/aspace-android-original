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
    @POST("spots/single/")
    Call<List<ParkingSpot>> getSpotInfo(@Header("Content-Type") String content, @Body String spot_id);
}
