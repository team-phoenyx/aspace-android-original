package me.parcare.parcare;

/**
 * Created by Zula on 6/18/17.
 */
// Not regular LatLng class name to distinguish local LatLng object from Mapbox and Google API easier.
    // Might have to rename to match server response object? Probably not.
    // in onResponse: LatLngPC latLng = reponse.body()

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
