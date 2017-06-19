package me.parcare.parcare;

/**
 * Created by Zula on 6/18/17.
 */

public class LatLngPC {
    String latitude;
    String longitude;

    public LatLngPC(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }
}
