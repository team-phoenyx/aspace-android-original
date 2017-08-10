package com.aspace.aspace.retrofitmodels;

/**
 * Created by Terrance on 8/10/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SavedLocation {

    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("location_id")
    @Expose
    private String locationId;
    @SerializedName("location_name")
    @Expose
    private String locationName;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

}