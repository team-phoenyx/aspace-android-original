package com.aspace.aspace;

import com.aspace.aspace.retrofitmodels.Car;
import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.aspace.aspace.retrofitmodels.ParkingSpot;
import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.ResponseCode;
import com.aspace.aspace.retrofitmodels.SavedLocation;
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

public interface AspaceRetrofitService {

    //********SPOT ENDPOINTS********
    @FormUrlEncoded
    @POST("spots/single/")
    Call<ParkingSpot> getSpotInfo(@Field("spot_id") String spotID);

    @FormUrlEncoded
    @POST("spots/onscreen/")
    Call<List<ParkingSpot>> getNearbySpots(@Field("lower_lat") String lower_lat, @Field("lower_lon") String lower_lon,
                                           @Field("upper_lat") String upper_lat, @Field("upper_lon") String upper_long);

    //TODO: DEPRECATED, REMOVE AFTER NAV ALGORITHM IMPLEMENTED
    /*
    @FormUrlEncoded
    @POST("spots/closest/")
    Call<ParkingSpot> getClosestSpot(@Field("lat") String lat, @Field("lon") String lon);
    */

    //********AUTH ENDPOINTS********
    @FormUrlEncoded
    @POST("users/auth/pin/")
    Call<ResponseCode> requestPIN(@Field("phone") String phone);

    @FormUrlEncoded
    @POST("users/auth/verify/")
    Call<VerifyPINResponse> verifyPIN(@Field("phone") String phone, @Field("pin") String pin);

    @FormUrlEncoded
    @POST("users/auth/reauth/")
    Call<ResponseCode> reauthenticate(@Field("access_token") String accessToken, @Field("phone") String phone, @Field("user_id") String userID);

    //********PROFILE ENDPOINTS********
    @FormUrlEncoded
    @POST("users/profile/update/")
    Call<ResponseCode> updateProfile(@Field("name") String name, @Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    @FormUrlEncoded
    @POST("users/profile/get/")
    Call<Profile> getProfile(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    //Cars
    @FormUrlEncoded
    @POST("users/profile/cars/get")
    Call<List<Car>> getCars(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    @FormUrlEncoded
    @POST("users/profile/cars/add")
    Call<ResponseCode> addCar(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID,
                              @Field("car_name") String carName, @Field("car_name") String carVIN, @Field("car_name") String carMake, @Field("car_name") String carModel, @Field("car_name") String carYear, @Field("car_name") String carLength);

    @FormUrlEncoded
    @POST("users/profile/cars/remove")
    Call<ResponseCode> removeCar(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID, @Field("car_vin") String carVIN);

    @FormUrlEncoded
    @POST("users/profile/cars/update")
    Call<ResponseCode> updateCar(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID,
                              @Field("car_name") String carName, @Field("car_name") String carVIN, @Field("car_name") String carMake, @Field("car_name") String carModel, @Field("car_name") String carYear, @Field("car_name") String carLength);

    //Locations
    @FormUrlEncoded
    @POST("users/profile/locs/get")
    Call<List<SavedLocation>> getSavedLocations(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    @FormUrlEncoded
    @POST("users/profile/locs/add")
    Call<ResponseCode> addSavedLocation(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID, @Field("loc_address") String locationAddress, @Field("loc_name") String locationName, @Field("loc_id") String locationID);

    @FormUrlEncoded
    @POST("users/profile/locs/remove")
    Call<ResponseCode> removeSavedLocation(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID, @Field("loc_id") String locationID);

    @FormUrlEncoded
    @POST("users/profile/locs/update")
    Call<ResponseCode> updateSavedLocation(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID,
                                        @Field("address") String locationAddress, @Field("location_name") String locationName, @Field("location_id") String locationID);

    //ACCOUNT TERMINATION
    @FormUrlEncoded
    @POST("users/delete")
    Call<ResponseCode> deleteAccount(@Field("phone") String phone, @Field("access_token") String accessToken, @Field("user_id") String userID);

    //********GEOCODING ENDPOINT********
    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<GeocodingResponse> getGeocodingSuggestions(@Path("query") String query, @Query("proximity") String proximityString, @Query("access_token") String accessToken);

    //********VIN DECODING ENDPOINT********
}
