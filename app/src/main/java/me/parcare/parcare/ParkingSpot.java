package me.parcare.parcare;

/**
 * Created by Zula on 6/18/17.
 */
import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class ParkingSpot {
    String id_type;

    String id_num;

    @SerializedName("lat")
    double latitude;

    @SerializedName("lon")
    double longitude;

    String status;



    LatLng spotLatLng;
    boolean isAvailable;


    public ParkingSpot(double latitude, double longitude, boolean isAvailable) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
    }

    public ParkingSpot(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.spotLatLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getId_type() {
        return this.id_type;
    }

    public String getId_num() {
        return this.getId_num();
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
