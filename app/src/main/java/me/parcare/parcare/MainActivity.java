package me.parcare.parcare;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.instabug.library.Instabug;
import com.instabug.library.invocation.InstabugInvocationEvent;
import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.parcare.parcare.retrofitmodels.Feature;
import me.parcare.parcare.retrofitmodels.GeocodingResponse;
import me.parcare.parcare.retrofitmodels.ParkingSpot;
import me.parcare.parcare.retrofitmodels.Suggestion;
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
    private FloatingActionButton gpsFAB;
    private LocationEngine locationEngine;
    private Location currentLocation;
    private LocationEngineListener locationEngineListener;
    private MarkerViewOptions closestSpotMarkerOptions, destinationMarkerOptions;
    private MarkerView destinationMarker;
    private LatLng currentDisplayTopLeft, currentDisplayBottomRight;
    private Icon openParkingSpotIcon, closedParkingSpotIcon, closestParkingSpotIcon;
    private Timer timer;
    private TimerTask updateSpotTimerTask;
    private List<SearchSuggestion> newSuggestions;
    private List<Feature> rawSuggestions;
    private PCRetrofitInterface parCareService, mapboxService;
    private boolean isUpdatingSpots;
    private FloatingActionButton navigationFAB;
    private MapboxNavigation navigation;

    //CONSTANTS
    private static final int DEFAULT_SNAP_ZOOM = 16;
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.241.224.224:3000/api/";
    public static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    private static final int SPOT_UPDATE_RATE = 1500; // milliseconds
    private static final String SPOT_AVAILABLE = "F";
    private static final String SPOT_UNAVAILABLE = "T";
    private static final int REQUEST_LOCATION_PERMISSION = 3139;
    private static final int ROUTE_TYPE_WALKING = 1;
    private static final int ROUTE_TYPE_DRIVING = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUpdatingSpots = true;

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);

        new Instabug.Builder(getApplication(), "6b6c0881b17446216f69f587e1b48021")
                .setInvocationEvent(InstabugInvocationEvent.SHAKE)
                .build();

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();
        navigation = new MapboxNavigation(this, Mapbox.getAccessToken());
        navigationFAB = (FloatingActionButton) findViewById(R.id.navigate_route_fab);
        navigationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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

                toggleGps(true, true);

                setCurrentScreenBounds();

                MarkerViewManager markerViewManager = map.getMarkerViewManager();
                markerViewManager.setOnMarkerViewClickListener(new MapboxMap.OnMarkerViewClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
                        isUpdatingSpots = false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        Log.i("CLICK", "Dialog  Pop");
                        final Marker markerF = marker;

                        builder.setTitle("Directions to Spot")
                                .setMessage("Would you like to see the route to this spot?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        List<Polyline> polylines = map.getPolylines();
                                        for (Polyline polyline : polylines) {
                                            map.removePolyline(polyline);
                                        }
                                        drawRouteToSpot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), markerF.getPosition(), ROUTE_TYPE_DRIVING);
                                        drawRouteToSpot(markerF.getPosition(), destinationMarker.getPosition(), ROUTE_TYPE_WALKING);
                                        gpsFAB.setVisibility(View.GONE);
                                        navigationFAB.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isUpdatingSpots = true;
                                        navigationFAB.setVisibility(View.GONE);
                                        gpsFAB.setVisibility(View.VISIBLE);
                                        dialog.dismiss();
                                    }
                                })
                                .create().show();
                        return true;
                    }
                });

                // ******************** SKETCHY TIMER TASK HERE ******************** \\
                updateSpotTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (isUpdatingSpots) {
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
                    }
                };
                timer.schedule(updateSpotTimerTask, 0, SPOT_UPDATE_RATE);
                // ******************** SKETCHY TIMER TASK HERE ******************** //
                Log.i(TAG + "Call", "ON MAP READY");
                mMapView.addOnMapChangedListener(new MapView.OnMapChangedListener() {
                    @Override
                    public void onMapChanged(int change) {
                        if (change == REGION_DID_CHANGE) {
                            // placeholder
                        }
                    }
                });
            }
        });

        searchView = (FloatingSearchView) findViewById(R.id.search_view);

        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                toggleGps(true, false);

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) searchView.clearFocus();

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
                                newSuggestions.add(new Suggestion(feature.getPlaceName()));
                            }

                            searchView.swapSuggestions(newSuggestions);
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                            Log.e("MAPBOX_GEOCODER_API", "Geocoder request failed");
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
                //Check if there is any query at all
                if (currentQuery != null && !currentQuery.isEmpty() && !currentQuery.equals("") && currentQuery.length() > 0 && rawSuggestions.size() > 0) {
                    onSearch(0); //automatically search the first suggestion, move the map camera
                    searchView.clearSearchFocus(); //collapses suggestions and search bar
                    searchView.setSearchText(newSuggestions.get(newSuggestions.size() - 1).getBody()); //sets the search text to the first suggestion
                }
            }
        });

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile:
                        toggleGps(true, false);

                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return;

                        try {
                            currentLocation = locationEngine.getLastLocation();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                            return;
                        }

                        Bundle extras = new Bundle();
                        extras.putDouble("lat", currentLocation.getLatitude());
                        extras.putDouble("lng", currentLocation.getLongitude());
                        ProfileDialogFragment profileDialogFragment = new ProfileDialogFragment();
                        profileDialogFragment.setArguments(extras);
                        profileDialogFragment.show(getFragmentManager(), "profiledialog");
                        break;
                }
            }
        });

        gpsFAB = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        gpsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled(), true);
                }
            }
        });
    }

    //When user selects a search suggestion
    private void onSearch(int searchedIndex) {

        isUpdatingSpots = true;

        navigationFAB.setVisibility(View.GONE);
        gpsFAB.setVisibility(View.VISIBLE);

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

//        if (closestSpotMarkerOptions != null) {
//            MarkerView closestSpotMarker = closestSpotMarkerOptions.getMarker();
//            if (closestSpotMarker != null) {
//                drawDrivingRouteToSpot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), closestSpotMarker.getPosition());
//            }
//        }

        //* Gives me a null pointer for the marker here for some reason?
        //drawDrivingRouteToSpot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), closestSpotMarkerOptions.getMarker().getPosition());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        isUpdatingSpots = true;
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        isUpdatingSpots = false;
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
        // make sure to remove all navigation listeners being used
        navigation.endNavigation();
    }

    private void toggleGps(boolean enableGps, boolean moveCamera) {
        if (enableGps) {

            //Check if location services are turned on
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder enableGPSBuilder = new AlertDialog.Builder(this).setTitle("Enable GPS").setMessage("Please enable GPS for app functionality").setCancelable(false).setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                enableGPSBuilder.create().show();
            }

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return;

            //Check if access to fine location is granted
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                enableLocation(true, moveCamera);
            }
        } else {
            enableLocation(false, moveCamera);
        }
    }

    private void enableLocation(boolean enabled, final boolean moveCamera) {
        if (enabled) {

            if (moveCamera) {
                // If we have the last location of the user, we can move the camera to that position.
                Location lastLocation = new Location(LocationManager.GPS_PROVIDER);
                try {
                    lastLocation = locationEngine.getLastLocation();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                if (lastLocation != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 14));
                }

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
                        if (moveCamera) map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), DEFAULT_SNAP_ZOOM));

                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
            gpsFAB.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            gpsFAB.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    enableLocation(true, true);
                } else {
                    //permission not granted
                    searchView.clearFocus();
                }
                break;
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }


    // Required by PermissionsListener interface. Not actually being called anywhere right now.
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation(true, true);
        } else {
            searchView.clearFocus();
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
        if (isUpdatingSpots) {
            Call<List<ParkingSpot>> call = parCareService.getNearbySpots(lowerLat, lowerLon, upperLat, upperLon);
            call.enqueue(new Callback<List<ParkingSpot>>() {
                @Override
                public void onResponse(Call<List<ParkingSpot>> call, Response<List<ParkingSpot>> response) {

                    if (response.isSuccessful()) {
                        List<ParkingSpot> spots = response.body();
                        drawSpots(spots);
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
    }

    // Gets the closest parking spot to the given lat lon input, draws a marker at that spot
    private void getClosestParkingSpot(PCRetrofitInterface parCareService, String lat, String lon) {
        Call<ParkingSpot> call = parCareService.getClosestSpot(lat, lon);
        final String latF = lat;
        final String lonF = lon;
        call.enqueue(new Callback<ParkingSpot>() {
            @Override
            public void onResponse(Call<ParkingSpot> call, Response<ParkingSpot> response) {
                ParkingSpot closestSpot = response.body();
                LatLng closestSpotLatLng = new LatLng(closestSpot.getLatitude(), closestSpot.getLongitude());
                closestSpotMarkerOptions = new MarkerViewOptions()
                        .position(closestSpotLatLng)
                        .icon(closestParkingSpotIcon);
                map.addMarker(closestSpotMarkerOptions);

                // Draws polyline route from current location to the closest spot. Ideally remove this call and move it to inside
                // onSearch instead, but right now doing so gives a null pointer for the first search and needs
                // 2 searches to work.

//                drawRouteToSpot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), closestSpotLatLng, ROUTE_TYPE_DRIVING);
//                drawRouteToSpot(new LatLng(Double.valueOf(latF), Double.valueOf(lonF)), closestSpotLatLng, ROUTE_TYPE_WALKING);
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

    // Draws polyline route from the origin to the spot specified by the given destination. Route determined
    // by the given routeType.
    private void drawRouteToSpot(LatLng origin, LatLng destination, int routeType) {
        // Note that waypoint takes in longitude first instead of latitude
        Waypoint originWaypoint = new Waypoint(origin.getLongitude(), origin.getLatitude());

        Waypoint destinationWaypoint = new Waypoint(destination.getLongitude(), destination.getLatitude());

        MapboxDirections client = null;


        switch (routeType) {
            case ROUTE_TYPE_WALKING:
                client = new MapboxDirections.Builder()
                        .setAccessToken(getString(R.string.access_token))
                        .setOrigin(originWaypoint)
                        .setDestination(destinationWaypoint)
                        .setProfile(DirectionsCriteria.PROFILE_WALKING)
                        .build();
                break;
            case ROUTE_TYPE_DRIVING:
                client = new MapboxDirections.Builder()
                        .setAccessToken(getString(R.string.access_token))
                        .setOrigin(originWaypoint)
                        .setDestination(destinationWaypoint)
                        .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                        .build();
                break;
        }

        final int routeTypeF = routeType;

        client.enqueue(new retrofit.Callback<DirectionsResponse>() {
            @Override
            public void onResponse(retrofit.Response<DirectionsResponse> response, retrofit.Retrofit retrofit) {
                if (response.isSuccess()) {
                    DirectionsRoute route = response.body().getRoutes().get(0);
                    //int distance = route.getDistance(); // meters
                    List<Waypoint> waypoints = route.getGeometry().getWaypoints();
                    LatLng[] points = new LatLng[waypoints.size()];
                    for (int i = 0; i < waypoints.size(); i++) {
                        points[i] = new LatLng(
                                waypoints.get(i).getLatitude(),
                                waypoints.get(i).getLongitude());
                    }
                    switch (routeTypeF) {
                        case ROUTE_TYPE_WALKING:
                            map.addPolyline(new PolylineOptions()
                                    .add(points)
                                    .color(Color.parseColor("#d84315"))
                                    .width(2));
                            break;
                        case ROUTE_TYPE_DRIVING:
                            map.addPolyline(new PolylineOptions()
                                    .add(points)
                                    .color(Color.parseColor("#3887be"))
                                    .alpha((float) 0.5)
                                    .width(5));
                            break;
                    }
                } else {
                    Log.i(TAG +"2", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.i(TAG + "2", t.toString());
            }
        });
    }
}