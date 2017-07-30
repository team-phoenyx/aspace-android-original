package com.aspace.aspace;

import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.aspace.aspace.retrofitmodels.ParkingSpot;
import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.ReauthenticateResponse;
import com.aspace.aspace.retrofitmodels.RequestPINResponse;
import com.aspace.aspace.retrofitmodels.UpdateProfileResponse;
import com.aspace.aspace.retrofitmodels.VINDecodeResponse;
import com.aspace.aspace.retrofitmodels.VerifyPINResponse;

import java.util.List;

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

    //********SPOT ENDPOINTS********
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

    //********AUTH ENDPOINTS********
    @FormUrlEncoded
    @POST("users/auth/pin/")
    Call<RequestPINResponse> requestPIN(@Field("phone") String phone);

    @FormUrlEncoded
    @POST("users/auth/verify/")
    Call<VerifyPINResponse> verifyPIN(@Field("phone") String phone, @Field("pin") String pin);

    @FormUrlEncoded
    @POST("users/auth/reauth/")
    Call<ReauthenticateResponse> reauthenticate(@Field("access_token") String accessToken, @Field("phone") String phone, @Field("user_id") String userID);

    //********PROFILE ENDPOINTS********
    @FormUrlEncoded
    @POST("users/profile/update/")
    Call<UpdateProfileResponse> updateProfile(@Field("name") String name, @Field("work_address") String work_address,
                                              @Field("home_address") String home_address, @Field("home_loc_id") String home_loc_id,
                                              @Field("work_loc_id") String work_loc_id, @Field("user_id") String user_id,
                                              @Field("phone") String user_phone_number, @Field("access_token") String user_access_token);

    @FormUrlEncoded
    @POST("users/profile/get/")
    Call<Profile> getProfile(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    //********GEOCODING ENDPOINT********
    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<GeocodingResponse> getGeocodingSuggestions(@Path("query") String query, @Query("proximity") String proximityString, @Query("access_token") String accessToken);

    //********VIN DECODING ENDPOINT********
    @GET("api/vehicle/v2/vins/{vin}")
    Call<VINDecodeResponse> getCarSpecs(@Path("vin") String vin, @Query("fmt") String format, @Query("api_key") String apiKey);
}
