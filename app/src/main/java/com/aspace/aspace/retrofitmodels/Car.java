
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Car {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("model")
    @Expose
    private String model;
    @SerializedName("make")
    @Expose
    private String make;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("vin")
    @Expose
    private String vin;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("length")
    @Expose
    private Double length;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }
}
