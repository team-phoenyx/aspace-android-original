package me.parcare.parcare;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Zula on 6/18/17.
 */

public interface PCRetrofitInterface {
    // Temp placeholders in place until server functionality is actually set up.
    @POST("URL Endpoint Here")
    Call<ParkingSpot> getClosestSpot(@Query("Variable name 1 Here") String var1, @Query("Variable name 2 Here") String var2);
}
