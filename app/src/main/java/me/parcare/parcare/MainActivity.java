package me.parcare.parcare;

import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.parcare.parcare.models.Feature;
import me.parcare.parcare.models.GeocodingResponse;
import me.parcare.parcare.models.ParkingSpot;
import me.parcare.parcare.models.Suggestion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.mapbox.mapboxsdk.maps.MapView.REGION_DID_CHANGE;

public class MainActivity extends AppCompatActivity implements PermissionsListener {

    private MapView mMapView;
    private MapboxMap map;
    private FloatingSearchView searchView;
    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private Location currentLocation;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;
    private MarkerViewOptions closestSpotMarkerOptions;
    private MarkerViewOptions destinationMarkerOptions;
    private MarkerView destinationMarker;
    private LatLng currentDisplayTopLeft;
    private LatLng currentDisplayBottomRight;
    private Icon openParkingSpotIcon;
    private Icon closedParkingSpotIcon;
    private Icon closestParkingSpotIcon;
    private Timer timer;
    private TimerTask updateSpotTimerTask;
    private List<SearchSuggestion> newSuggestions;
    private List<Feature> rawSuggestions;
    private PCRetrofitInterface parCareService, mapboxService;

    private static final int DEFAULT_SNAP_ZOOM = 16;
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.241.224.224:3000/api/";
    public static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    private static final int SPOT_UPDATE_RATE = 1500; // milliseconds
    private static final String SPOT_AVAILABLE = "F";
    private static final String SPOT_UNAVAILABLE = "T";
    private static final int REQUEST_LOCATION_PERMISSION = 3139;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        parCareService = retrofit.create(PCRetrofitInterface.class);

        retrofit = new Retrofit.Builder().baseUrl(MAPBOX_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

        mapboxService = retrofit.create(PCRetrofitInterface.class);

        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        openParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_available_48px);
        closedParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_unavailable_48px);
        closestParkingSpotIcon = iconFactory.fromResource(R.drawable.blue_marker_view);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                setCurrentScreenBounds();

                // ******************** SKETCHY TIMER TASK HERE ******************** \\
                updateSpotTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        setCurrentScreenBounds();
                        Log.i(TAG + "Call", "ON MAP CHANGE");

                        String lowerLat = Double.toString(Math.min(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
                        String upperLat = Double.toString(Math.max(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
                        String lowerLon = Double.toString(Math.min(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));
                        String upperLon = Double.toString(Math.max(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));

                        Log.i(TAG + "3", lowerLat);
                        Log.i(TAG + "3", lowerLon);
                        Log.i(TAG + "3", upperLat);
                        Log.i(TAG + "3", upperLon);
                        getParkingSpotsNearby(parCareService, lowerLat, lowerLon, upperLat, upperLon);
                        //*****Implement later, this is designed to fix edge case of closest parking spot switching to unavailable*****
//                        if (destinationMarker != null) {
//                            LatLng destinationLatLng = destinationMarker.getPosition();
//                            String destinationLatString = String.valueOf(destinationLatLng.getLatitude());
//                            String destinationLonString = String.valueOf(destinationLatLng.getLongitude());
//                            getClosestParkingSpot(parCareService, destinationLatString, destinationLonString);
//                        }
                        //******
                    }
                };
                timer.schedule(updateSpotTimerTask, 0, SPOT_UPDATE_RATE);
                // ******************** SKETCHY TIMER TASK HERE ******************** //
                Log.i(TAG + "Call", "ON MAP READY");
                mMapView.addOnMapChangedListener(new MapView.OnMapChangedListener() {
                    @Override
                    public void onMapChanged(int change) {
                        if (change == REGION_DID_CHANGE) {

//                            setCurrentScreenBounds();
//                            Log.i(TAG + "Call", "ON MAP CHANGE");
//
//                            String lowerLat = Double.toString(Math.min(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
//                            String upperLat = Double.toString(Math.max(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
//                            String lowerLon = Double.toString(Math.min(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));
//                            String upperLon = Double.toString(Math.max(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));
//
//                            Log.i(TAG + "3", lowerLat);
//                            Log.i(TAG + "3", lowerLon);
//                            Log.i(TAG + "3", upperLat);
//                            Log.i(TAG + "3", upperLon);
//                            getParkingSpotsNearby(parCareService, lowerLat, lowerLon, upperLat, upperLon);
                        }
                    }
                });
            }
        });

        searchView = (FloatingSearchView) findViewById(R.id.search_view);

        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                try {
                    currentLocation = locationEngine.getLastLocation();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFocusCleared() {

            }
        });

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {

                if (newQuery.equals("")) {
                    searchView.swapSuggestions(new ArrayList<SearchSuggestion>());
                } else {
                    String proximityString = Double.toString(currentLocation.getLongitude()) + "," + Double.toString(currentLocation.getLatitude());
                    mapboxService.getGeocodingSuggestions(newQuery, proximityString, getString(R.string.access_token)).enqueue(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                            GeocodingResponse geocodingResponse = response.body();

                            if (geocodingResponse == null) return;

                            rawSuggestions = geocodingResponse.getFeatures();
                            newSuggestions = new ArrayList<>();

                            for (Feature feature : rawSuggestions) {
                                newSuggestions.add(new Suggestion(feature.getText()));
                            }
                            
                            searchView.swapSuggestions(newSuggestions);
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                            //TODO Handle failure with snackbar
                        }
                    });
                }


            }
        });

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                int rawSuggestionIndex = rawSuggestions.size() - 1 - newSuggestions.indexOf(searchSuggestion);
                onSearch(rawSuggestionIndex); //moves the map camera
                searchView.clearSearchFocus(); //collapses suggestions and search bar
                searchView.setSearchText(searchSuggestion.getBody()); //sets the search text to the selected suggestion
            }

            @Override
            public void onSearchAction(String currentQuery) {
                onSearch(0); //automatically search the first suggestion, move the map camera
                searchView.clearSearchFocus(); //collapses suggestions and search bar
                searchView.setSearchText(newSuggestions.get(newSuggestions.size() - 1).getBody()); //sets the search text to the first suggestion
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });
    }

    private void onSearch(int searchedIndex) {
        Feature selectedFeature = rawSuggestions.get(searchedIndex);

        int zoomScale = 16;

        switch (rawSuggestions.get(searchedIndex).getPlaceType().get(0)) {
            case "country":
                zoomScale = 2;
                break;
            case "region":
                zoomScale = 4;
                break;
            case "postcode":
                zoomScale = 12;
                break;
            case "district":
                zoomScale = 12;
                break;
            case "place":
                zoomScale = 9;
                break;
            case "locality":
                zoomScale = 8;
                break;
            case "neighborhood":
                zoomScale = 14;
                break;
            case "address":
                zoomScale = 15;
                break;

            case "poi":
                zoomScale = 14;
                break;

            case "poi.landmark":
                zoomScale = 14;
                break;

        }

        double lng = selectedFeature.getCenter().get(0);
        double lat = selectedFeature.getCenter().get(1);
        LatLng searchedLatLng = new LatLng(lat, lng);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, zoomScale));
        Log.i(TAG, "Place: " + selectedFeature.getPlaceName());

        if (destinationMarkerOptions == null) {
            destinationMarkerOptions = new MarkerViewOptions()
                    .position(searchedLatLng)
                    .title(selectedFeature.getPlaceName());
        } else {
            destinationMarkerOptions = destinationMarkerOptions
                    .position(searchedLatLng)
                    .title(selectedFeature.getPlaceName());
        }

        if (destinationMarker == null) {
            map.addMarker(destinationMarkerOptions);
        } else {
            destinationMarker.setPosition(searchedLatLng);
        }
        destinationMarker = destinationMarkerOptions.getMarker();

        String searchedLatString = lat + "";
        String searchedLngString = lng + "";

        getClosestParkingSpot(parCareService, searchedLatString, searchedLngString);
        //parkingSpotsNearby = getParkingSpotsNearby(parCareService, "47.604327", "-122.2987024", "47.604327", "-122.2983136");
        //drawSpots(parkingSpotsNearby);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            currentLocation = locationEngine.getLastLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
        }
    }

    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            permissionsManager = new PermissionsManager(this);
            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager.requestLocationPermissions(this);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                return;
            }
            Location lastLocation = locationEngine.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), DEFAULT_SNAP_ZOOM));
            }

            locationEngineListener = new LocationEngineListener() {
                @Override
                public void onConnected() {
                    // No action needed here.
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), DEFAULT_SNAP_ZOOM));
                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                enableLocation(true);
                break;
            default:
                permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation(true);
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Retrieves the LatLng objects corresponding to each corner of the screen that is being
    // currently displayed to the user.
    private void setCurrentScreenBounds() {
        if (map != null) {
            int viewportWidth = mMapView.getWidth();
            int viewportHeight = mMapView.getHeight();
            currentDisplayTopLeft = map.getProjection().fromScreenLocation(new PointF(0, 0));
            currentDisplayBottomRight = map.getProjection().fromScreenLocation(new PointF(viewportWidth, viewportHeight));

            Log.i(TAG, "Top Left Lat//Lng: " + currentDisplayTopLeft.getLatitude() + "//" + currentDisplayTopLeft.getLongitude());
            Log.i(TAG, "Bottom Right Lat//Lng: " + currentDisplayBottomRight.getLatitude() + "//" + currentDisplayBottomRight.getLongitude());
        }
    }

    // Retrieves the info for a specific parking spot given by the spotId, returns
    // a List of spots with the given id and their respective information.
    private void getParkingSpotInfo(PCRetrofitInterface parCareService, String spotId) {
        Call<List<ParkingSpot>> call = parCareService.getSpotInfo(spotId);
        call.enqueue(new Callback<List<ParkingSpot>>() {
            @Override
            public void onResponse(Call<List<ParkingSpot>> call, Response<List<ParkingSpot>> response) {
                if (response.isSuccessful()) {
                    List<ParkingSpot> spots = response.body();
                    Log.i(TAG + "2", "Response Successful");
                } else {
                    Log.i(TAG + "2", "Response Unsuccessful: " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<List<ParkingSpot>> call, Throwable t) {
                Log.i(TAG + "2", "Failed to connect: " + t.toString());
            }
        });
    }

    // Retrieves all of the spots in a bound area given by upper and lower latitudes/longitudes,
    // returns a list of the spots in the area.
    private void getParkingSpotsNearby(PCRetrofitInterface parCareService,
                                                    String lowerLat, String lowerLon,
                                                    String upperLat, String upperLon) {
        Call<List<ParkingSpot>> call = parCareService.getNearbySpots(lowerLat, lowerLon, upperLat, upperLon);
        call.enqueue(new Callback<List<ParkingSpot>>() {
            @Override
            public void onResponse(Call<List<ParkingSpot>> call, Response<List<ParkingSpot>> response) {

                if (response.isSuccessful()) {
                    List<ParkingSpot> spots = response.body();
                    drawSpots(spots);
//                    for (ParkingSpot spot : spots) {
//                        Log.i(TAG + "3", spot.getLatitude() + "/"+ spot.getLongitude());
//                    }
                    Log.i(TAG + "2", "Response Successful");
                } else {
                    Log.i(TAG + "2", "Response Unsuccessful: " + response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<List<ParkingSpot>> call, Throwable t) {
                Log.i(TAG + "2", "Failed to connect: " + t.toString());
            }
        });
    }

    // Gets the closest parking spot to the given lat lon input, draws a marker at that spot
    private void getClosestParkingSpot(PCRetrofitInterface parCareService, String lat, String lon) {
        Call<ParkingSpot> call = parCareService.getClosestSpot(lat, lon);
        call.enqueue(new Callback<ParkingSpot>() {
            @Override
            public void onResponse(Call<ParkingSpot> call, Response<ParkingSpot> response) {
                ParkingSpot closestSpot = response.body();
                LatLng closestSpotLatLng = new LatLng(closestSpot.getLatitude(), closestSpot.getLongitude());
                closestSpotMarkerOptions = new MarkerViewOptions()
                        .position(closestSpotLatLng)
                        .icon(closestParkingSpotIcon);
                map.addMarker(closestSpotMarkerOptions);
            }

            @Override
            public void onFailure(Call<ParkingSpot> call, Throwable t) {
                Log.i(TAG + "2", "Failed to connect: " + t.toString());
            }
        });
    }

    private void drawSpots(List<ParkingSpot> parkingSpots) {
        map.clear();

        // redraw destination spot marker
        if (destinationMarkerOptions != null) {
            map.addMarker(destinationMarkerOptions);
        }
        // redraw closest spot marker
        if (closestSpotMarkerOptions != null) {
            map.addMarker(closestSpotMarkerOptions);
        }

        // draw spots
        for (ParkingSpot spot : parkingSpots) {
            //Log.i(TAG + "10", "Spot Lat: " + spot.getLatitude() + " Spot Lng: " + spot.getLongitude());
            String status = spot.getStatus();
            LatLng spotLatLng = new LatLng(spot.getLatitude(), spot.getLongitude());
            // might be able to keep track of a list of the markers on screen and iterate through the list to
            // pick the ones we want to remove instead of wiping the overlay entirely
            if (status.equals(SPOT_AVAILABLE)) {
                map.addMarker(new MarkerViewOptions()
                        .position(spotLatLng)
                        .icon(openParkingSpotIcon))
                        .setSnippet("Status: Available" + "\nLocation: " + spot.getLatitude() + ", " + spot.getLongitude());
            } else {
                map.addMarker(new MarkerViewOptions()
                        .position(spotLatLng)
                        .icon(closedParkingSpotIcon))
                        .setSnippet("Status: Unavailable" + "\nLocation: " + spot.getLatitude() + ", " + spot.getLongitude());
            }
        }
    }
}