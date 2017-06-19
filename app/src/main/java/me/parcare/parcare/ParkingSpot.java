package me.parcare.parcare;

/**
 * Created by Zula on 6/18/17.
 */

public class ParkingSpot {
    String latitude;
    String longitude;
    //boolean isAvailable;

    /*
    public ParkingSpot(String latitude, String longitude, boolean isAvailable) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.isAvailable = isAvailable;
    } */

    public ParkingSpot(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return this.latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    /*public boolean getAvailability() {
        return this.isAvailable;
    }*/
}
