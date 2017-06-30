package me.parcare.parcare.retrofitmodels;

/**
 * Created by Zula on 6/18/17.
 */
import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.annotations.Marker;

public class ParkingSpot {
    @SerializedName("id_type")
    String id_type;
    @SerializedName("id_num")
    String id_num;
    @SerializedName("lat")
    double lat;
    @SerializedName("lon")
    double lon;
    @SerializedName("status")
    String status;

    transient Marker marker;

    public double getLatitude() {
        return this.lat;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void setLatitude(double lat) {
        this.lat = lat;
    }

    public double getLongitude() {
        return this.lon;
    }

    public void setLongitude(double lon) {
        this.lon = lon;
    }
    public String getId_type() {
        return this.id_type;
    }

    public void setId_type(String id_type) {
        this.id_type = id_type;
    }

    public void setId_num(String id_num) {
        this.id_num = id_num;
    }

    public String getId_num() {
        return this.getId_num();
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public String getStatus() {
        return this.status;
    }

    /*public boolean getAvailability() {
        return this.isAvailable;
    }*/

    /*public LatLng getLatLng() {
        return this.spotLatLng;
     }*/
}
