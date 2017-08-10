
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Car {

    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("car_name")
    @Expose
    private String carName;
    @SerializedName("car_vin")
    @Expose
    private String carVin;
    @SerializedName("car_make")
    @Expose
    private String carMake;
    @SerializedName("car_model")
    @Expose
    private String carModel;
    @SerializedName("car_year")
    @Expose
    private Integer carYear;
    @SerializedName("car_length")
    @Expose
    private Integer carLength;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarVin() {
        return carVin;
    }

    public void setCarVin(String carVin) {
        this.carVin = carVin;
    }

    public String getCarMake() {
        return carMake;
    }

    public void setCarMake(String carMake) {
        this.carMake = carMake;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public Integer getCarYear() {
        return carYear;
    }

    public void setCarYear(Integer carYear) {
        this.carYear = carYear;
    }

    public Integer getCarLength() {
        return carLength;
    }

    public void setCarLength(Integer carLength) {
        this.carLength = carLength;
    }

}
