
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Categories {

    @SerializedName("EPAClass")
    @Expose
    private String ePAClass;
    @SerializedName("primaryBodyType")
    @Expose
    private String primaryBodyType;
    @SerializedName("vehicleStyle")
    @Expose
    private String vehicleStyle;
    @SerializedName("vehicleType")
    @Expose
    private String vehicleType;

    public String getEPAClass() {
        return ePAClass;
    }

    public void setEPAClass(String ePAClass) {
        this.ePAClass = ePAClass;
    }

    public String getPrimaryBodyType() {
        return primaryBodyType;
    }

    public void setPrimaryBodyType(String primaryBodyType) {
        this.primaryBodyType = primaryBodyType;
    }

    public String getVehicleStyle() {
        return vehicleStyle;
    }

    public void setVehicleStyle(String vehicleStyle) {
        this.vehicleStyle = vehicleStyle;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

}
