
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Profile {

    @SerializedName("home_loc_id")
    @Expose
    private String homeLocId;
    @SerializedName("home_address")
    @Expose
    private String homeAddress;
    @SerializedName("work_loc_id")
    @Expose
    private String workLocId;
    @SerializedName("work_address")
    @Expose
    private String workAddress;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("access_token")
    @Expose
    private String accessToken;

    public String getHomeLocId() {
        return homeLocId;
    }

    public void setHomeLocId(String homeLocId) {
        this.homeLocId = homeLocId;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getWorkLocId() {
        return workLocId;
    }

    public void setWorkLocId(String workLocId) {
        this.workLocId = workLocId;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
