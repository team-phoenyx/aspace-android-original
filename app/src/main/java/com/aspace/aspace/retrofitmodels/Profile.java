
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Profile {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("cars")
    @Expose
    private List<Car> cars;
    @SerializedName("locs")
    @Expose
    private List<SavedLocation> locations;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public List<SavedLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<SavedLocation> locations) {
        this.locations = locations;
    }
}
