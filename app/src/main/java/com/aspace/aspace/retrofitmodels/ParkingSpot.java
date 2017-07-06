package me.parcare.parcare.retrofitmodels;

/**
 * Created by Zula on 6/18/17.
 */
import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.google.gson.annotations.Expose;

public class ParkingSpot {

    @SerializedName("block_id")
    @Expose
    private String blockId;
    @SerializedName("spot_id")
    @Expose
    private int spotId;
    @SerializedName("lat")
    @Expose
    private double lat;
    @SerializedName("lon")
    @Expose
    private double lon;
    @SerializedName("status")
    @Expose
    private String status;

    transient Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}