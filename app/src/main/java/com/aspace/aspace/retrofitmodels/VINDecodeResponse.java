
package com.aspace.aspace.retrofitmodels;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VINDecodeResponse {

    @SerializedName("make")
    @Expose
    private Make make;
    @SerializedName("model")
    @Expose
    private Model model;
    @SerializedName("drivenWheels")
    @Expose
    private String drivenWheels;
    @SerializedName("numOfDoors")
    @Expose
    private String numOfDoors;
    @SerializedName("manufacturerCode")
    @Expose
    private String manufacturerCode;
    @SerializedName("categories")
    @Expose
    private Categories categories;
    @SerializedName("vin")
    @Expose
    private String vin;
    @SerializedName("squishVin")
    @Expose
    private String squishVin;
    @SerializedName("years")
    @Expose
    private List<Year> years = null;
    @SerializedName("matchingType")
    @Expose
    private String matchingType;

    public Make getMake() {
        return make;
    }

    public void setMake(Make make) {
        this.make = make;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getDrivenWheels() {
        return drivenWheels;
    }

    public void setDrivenWheels(String drivenWheels) {
        this.drivenWheels = drivenWheels;
    }

    public String getNumOfDoors() {
        return numOfDoors;
    }

    public void setNumOfDoors(String numOfDoors) {
        this.numOfDoors = numOfDoors;
    }

    public String getManufacturerCode() {
        return manufacturerCode;
    }

    public void setManufacturerCode(String manufacturerCode) {
        this.manufacturerCode = manufacturerCode;
    }

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getSquishVin() {
        return squishVin;
    }

    public void setSquishVin(String squishVin) {
        this.squishVin = squishVin;
    }

    public List<Year> getYears() {
        return years;
    }

    public void setYears(List<Year> years) {
        this.years = years;
    }

    public String getMatchingType() {
        return matchingType;
    }

    public void setMatchingType(String matchingType) {
        this.matchingType = matchingType;
    }
}
