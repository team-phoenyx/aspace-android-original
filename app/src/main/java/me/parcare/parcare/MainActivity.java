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
import android.util.Pair;
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
import com.mapbox.mapboxsdk.constants.MyBearingTracking;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.RouteProgress;
import com.mapbox.services.android.navigation.v5.listeners.AlertLevelChangeListener;
import com.mapbox.services.android.navigation.v5.listeners.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.listeners.OffRouteListener;
import com.mapbox.services.android.navigation.v5.listeners.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.models.RouteStepProgress;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.HashMap;
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
import static com.mapbox.services.android.navigation.v5.NavigationConstants.ARRIVE_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.DEPART_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.HIGH_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.LOW_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.MEDIUM_ALERT_LEVEL;
import static com.mapbox.services.android.navigation.v5.NavigationConstants.NONE_ALERT_LEVEL;

public class MainActivity extends AppCompatActivity implements PermissionsListener {

    private MapView mMapView;
    private MapboxMap map;
    private FloatingSearchView searchView;
    private FloatingActionButton startNavigationFAB, cancelNavigationFAB, snapToLocationFAB, cancelRouteFAB;
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
    private List<ParkingSpot> previousParkingSpots;
    private HashMap<Integer, Integer> redrawSpotIDs;
    private PCRetrofitInterface parCareService, mapboxService;
    private boolean isUpdatingSpots;
    private boolean allowAlert;
    private MapboxNavigation navigation;
    private LatLng clickedSpotLatLng;
    private com.mapbox.services.api.directions.v5.models.DirectionsRoute route;
    private Position navDestination;
    private Polyline drivingRoutePolyline;
    private String searchedLatString, searchedLngString;
    //CONSTANTS
    private static final int DEFAULT_SNAP_ZOOM = 16;
    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://192.241.224.224:3000/api/";
    public static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    private static final int SPOT_UPDATE_RATE = 2000; // milliseconds
    private static final String SPOT_AVAILABLE = "F";
    private static final String SPOT_UNAVAILABLE = "T";
    private static final int REQUEST_LOCATION_PERMISSION = 3139;
    private static final int ROUTE_TYPE_WALKING = 1;
    private static final int ROUTE_TYPE_DRIVING = 0;

    private String userID, userAccessToken, userPhoneNumber, realmEncryptionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));
        isUpdatingSpots = true;
        allowAlert = true;

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
        navigation.addNavigationEventListener(new NavigationEventListener() {
            @Override
            public void onRunning(boolean running) {

            }
        });

        navigation.addAlertLevelChangeListener(new AlertLevelChangeListener() {
            @Override
            public void onAlertLevelChange(int alertLevel, RouteProgress routeProgress) {
                // we can do stuff here using the routeProgress object.
                switch (alertLevel) {
                    case HIGH_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "HIGH", Toast.LENGTH_LONG).show();
                        break;
                    case MEDIUM_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "MEDIUM", Toast.LENGTH_LONG).show();
                        break;
                    case LOW_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "LOW", Toast.LENGTH_LONG).show();
                        break;
                    case ARRIVE_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "ARRIVE", Toast.LENGTH_LONG).show();
                        break;
                    case NONE_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "NONE", Toast.LENGTH_LONG).show();
                        break;
                    case DEPART_ALERT_LEVEL:
                        Toast.makeText(MainActivity.this, "DEPART", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });

        navigation.addProgressChangeListener(new ProgressChangeListener() {
            @Override
            public void onProgressChange(Location location, RouteProgress routeProgress) {
                // we can do stuff here to update UI using routeProgress object
                List<LegStep> steps = routeProgress.getCurrentLeg().getSteps();
                RouteStepProgress routeStepProgress = routeProgress.getCurrentLegProgress().getCurrentStepProgress();
                LegStep currentStep = routeStepProgress.step();
                // Current step log info
                Log.i(TAG + "Directions", "Current Step: " + currentStep.getName()
                        + ", Current Maneuver: " + currentStep.getManeuver().getInstruction()
                        + ", Distance In: " + routeStepProgress.getDistanceTraveled()
                        + ", Distance Left: " + routeStepProgress.getDistanceRemaining()
                        + ", Duration: " + currentStep.getDuration());
                Toast.makeText(MainActivity.this, "" + currentStep.getManeuver().getInstruction(), Toast.LENGTH_LONG).show();
                /* Complete directions log
                for (LegStep step : steps) {
                    Log.i(TAG + "Directions", "LEGSTEP: " + step.getName() + ", Maneuver: " + step.getManeuver().getInstruction() + ", Step distance: " + step.getDistance());
                }
                */
            }
        });

        navigation.addOffRouteListener(new OffRouteListener() {
            @Override
            public void userOffRoute(Location location) {
                final LatLng newOriginLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                double destinationLat = destinationMarker.getPosition().getLatitude();
                double destinationLng = destinationMarker.getPosition().getLongitude();
                final LatLng destinationLatLng = new LatLng (destinationLat, destinationLng);
                Position newOrigin = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
                Position destination = Position.fromCoordinates(destinationLng, destinationLat);
                navigation.getRoute(newOrigin, destination, new Callback<com.mapbox.services.api.directions.v5.models.DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Response<com.mapbox.services.api.directions.v5.models.DirectionsResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "YOU ARE OFF-ROUTE! REROUTING NOW", Toast.LENGTH_LONG).show();
                            com.mapbox.services.api.directions.v5.models.DirectionsRoute newRoute = response.body().getRoutes().get(0);
                            // update new route to navigation
                            navigation.updateRoute(newRoute);
                            // remove old driving route
                            if (map != null && !map.getPolylines().isEmpty()) {
                                map.removePolyline(drivingRoutePolyline);
                            }
                            // draw new driving route
                            drawRouteToSpot(newOriginLatLng, clickedSpotLatLng, ROUTE_TYPE_DRIVING);
                            Log.i(TAG + "Directions", "Route Update Successful");
                        } else {
                            Log.i(TAG + "Directions", "Route Update Unsuccessful");
                        }
                    }

                    @Override
                    public void onFailure(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Throwable t) {
                        Log.i(TAG + "Directions", "Reroute FAILED", t);
                    }
                });
            }
        });

        MapboxNavigationOptions mapboxNavigationOptions = navigation.getMapboxNavigationOptions();
        // sets off route distance threshhold to 10 meters for testing.
        // (default threshhold is 50 meters).
        mapboxNavigationOptions.setMaximumDistanceOffRoute(50);

        startNavigationFAB = (FloatingActionButton) findViewById(R.id.navigate_route_fab);
        startNavigationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Position origin = Position.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
                navDestination = Position.fromLngLat(clickedSpotLatLng.getLongitude(), clickedSpotLatLng.getLatitude());
                navigation.getRoute(origin, navDestination, new Callback<com.mapbox.services.api.directions.v5.models.DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Response<com.mapbox.services.api.directions.v5.models.DirectionsResponse> response) {
                        if (response.isSuccessful()) {
                            com.mapbox.services.api.directions.v5.models.DirectionsRoute route = response.body().getRoutes().get(0);
                            MainActivity.this.route = route;
                            navigation.startNavigation(route);
                            // not sure if it makes a difference if I use map.getMyLocation vs the currentLocation field here
                            Location myCurrentLocation = map.getMyLocation();
                            LatLng myCurrentLocationLatLng = new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocationLatLng, DEFAULT_SNAP_ZOOM));

                            map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                            map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);

                            startNavigationFAB.setVisibility(View.GONE);
                            cancelRouteFAB.setVisibility(View.GONE);
                            cancelNavigationFAB.setVisibility(View.VISIBLE);
                            snapToLocationFAB.setVisibility(View.VISIBLE);
                            Log.i(TAG + "nav", "Response success: " + response.raw().toString());
                        } else {
                            Log.i(TAG + "nav", "Response unsuccessful: " + response.raw().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Throwable t) {
                        Log.i(TAG + "nav", "Response failed: ", t);
                    }
                });
            }
        });

        cancelNavigationFAB = (FloatingActionButton) findViewById(R.id.cancel_navigation_fab);
        cancelNavigationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.endNavigation();
                map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_NONE);
                cancelRouteFAB.setVisibility(View.VISIBLE);
                cancelNavigationFAB.setVisibility(View.GONE);
                snapToLocationFAB.setVisibility(View.GONE);
                startNavigationFAB.setVisibility(View.VISIBLE);
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

                if (map.getTrackingSettings().getMyBearingTrackingMode() != MyBearingTracking.COMPASS) {
                    map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);
                }

                MarkerViewManager markerViewManager = map.getMarkerViewManager();
                markerViewManager.setOnMarkerViewClickListener(new MapboxMap.OnMarkerViewClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
                        clickedSpotLatLng = marker.getPosition();
                        final LatLng clickedSpotLatLngF = new LatLng(marker.getPosition().getLatitude(), marker.getPosition().getLongitude());
                        if (allowAlert) {
                            allowAlert = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Directions to Spot")
                                    .setMessage("Would you like to see the route to this spot?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            List<Polyline> polylines = map.getPolylines();
                                            for (Polyline polyline : polylines) {
                                                map.removePolyline(polyline);
                                            }
                                            drawRouteToSpot(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), clickedSpotLatLngF, ROUTE_TYPE_DRIVING);
                                            drawRouteToSpot(clickedSpotLatLngF, destinationMarker.getPosition(), ROUTE_TYPE_WALKING);
                                            startNavigationFAB.setVisibility(View.VISIBLE);
                                            cancelRouteFAB.setVisibility(View.VISIBLE);
                                            snapToLocationFAB.setVisibility(View.GONE);
                                            allowAlert = true;
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startNavigationFAB.setVisibility(View.GONE);
                                            allowAlert = true;
                                        }
                                    })
                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            allowAlert = true;
                                        }
                                    })
                                    .create().show();
                        }
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
                            /*
                            Log.i(TAG + "3", lowerLat);
                            Log.i(TAG + "3", lowerLon);
                            Log.i(TAG + "3", upperLat);
                            Log.i(TAG + "3", upperLon);
                            */
                            getParkingSpotsNearby(parCareService, lowerLat, lowerLon, upperLat, upperLon);
                        }
                    }
                };
                timer.scheduleAtFixedRate(updateSpotTimerTask, 1000, SPOT_UPDATE_RATE);
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

                /*
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    searchView.clearSearchFocus();
                }
                */


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
                        extras.putString(getString(R.string.user_id_tag), userID);
                        extras.putString(getString(R.string.user_access_token_tag), userAccessToken);
                        extras.putString(getString(R.string.user_phone_number_tag), userPhoneNumber);
                        extras.putString(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                        ProfileDialogFragment profileDialogFragment = new ProfileDialogFragment();
                        profileDialogFragment.setArguments(extras);
                        profileDialogFragment.show(getFragmentManager(), "profiledialog");
                        break;
                }
            }
        });

        snapToLocationFAB = (FloatingActionButton) findViewById(R.id.snap_to_location_fab);
        snapToLocationFAB.setVisibility(View.VISIBLE);
        snapToLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && map.isMyLocationEnabled()) {
                    Location currentLocation = map.getMyLocation();
                    LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, DEFAULT_SNAP_ZOOM));
                }
            }
        });

        cancelRouteFAB = (FloatingActionButton) findViewById(R.id.cancel_route_fab);
        cancelRouteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && !map.getPolylines().isEmpty()) {
                    List<Polyline> routes = map.getPolylines();
                    for (Polyline route : routes) {
                        map.removePolyline(route);
                    }
                    cancelRouteFAB.setVisibility(View.GONE);
                    snapToLocationFAB.setVisibility(View.VISIBLE);
                    if (startNavigationFAB.getVisibility() == View.VISIBLE) {
                        startNavigationFAB.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    //When user selects a search suggestion
    private void onSearch(int searchedIndex) {

        isUpdatingSpots = true;

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
        map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);
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

        searchedLatString = lat + "";
        searchedLngString = lng + "";
        // Removal of this
        //getClosestParkingSpot(parCareService, searchedLatString, searchedLngString);

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
        navigation.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearFocus();
        if (map != null) {
            List<Polyline> polylines = map.getPolylines();
            if (polylines.isEmpty()) {
                isUpdatingSpots = true;
            }
            mMapView.onResume();
        }
        toggleGps(true, false);
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
        navigation.onStop();
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
                        if (searchView.isSearchBarFocused()) {
                            searchView.clearSearchFocus();
                        }
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

            //Log.i(TAG, "Top Left Lat//Lng: " + currentDisplayTopLeft.getLatitude() + "//" + currentDisplayTopLeft.getLongitude());
            //Log.i(TAG, "Bottom Right Lat//Lng: " + currentDisplayBottomRight.getLatitude() + "//" + currentDisplayBottomRight.getLongitude());
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
        //if (isUpdatingSpots) {
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
        //}
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
                LatLng closestSpotLatLng = new LatLng(closestSpot.getLat(), closestSpot.getLon());
                if (closestSpotMarkerOptions != null && closestSpotMarkerOptions.getMarker().getPosition() != closestSpotLatLng) {
                    closestSpotMarkerOptions.getMarker().setPosition(closestSpotLatLng);
                } else {
                    closestSpotMarkerOptions = new MarkerViewOptions()
                            .position(closestSpotLatLng)
                            .icon(closestParkingSpotIcon);
                    map.addMarker(closestSpotMarkerOptions);
                }
            }

            @Override
            public void onFailure(Call<ParkingSpot> call, Throwable t) {
                Log.i(TAG + "2", "Failed to connect: " + t.toString());
            }
        });
    }

    private Pair<List<ParkingSpot>, List<ParkingSpot>> getDeltaParkingSpots(List<ParkingSpot> newParkingSpots, List<ParkingSpot> previousParkingSpots) {
        List<ParkingSpot> deltas = new ArrayList<>();
        List<ParkingSpot> nonDeltas = new ArrayList<>();
        boolean spotExists = false;
        for (ParkingSpot checkSpot : newParkingSpots) {
            spotExists = false;

            for (int i = 0; i < previousParkingSpots.size(); i++) {
                ParkingSpot previousCheckSpot = previousParkingSpots.get(i);
                if (checkSpot.getSpotId() == previousCheckSpot.getSpotId()) {
                    spotExists = true;
                    if (checkSpot.getLat() != previousCheckSpot.getLat() ||
                            checkSpot.getLon() != previousCheckSpot.getLon() ||
                            !checkSpot.getStatus().equals(previousCheckSpot.getStatus())) {
                        deltas.add(checkSpot);
                        redrawSpotIDs.put(checkSpot.getSpotId(), i);
                    } else {
                        nonDeltas.add(previousCheckSpot);
                    }
                    break;
                }
            }

            if (!spotExists) deltas.add(checkSpot);
        }
        Pair<List<ParkingSpot>, List<ParkingSpot>> pair = new Pair<List<ParkingSpot>, List<ParkingSpot>>(deltas, nonDeltas);
        return pair;
    }

    private void drawSpots(List<ParkingSpot> parkingSpots) {
        List<ParkingSpot> deltaParkingSpots = new ArrayList<>();
        List<ParkingSpot> nonDeltaParkingSpots = new ArrayList<>();
        redrawSpotIDs = new HashMap<>();

        if (previousParkingSpots != null && previousParkingSpots.size() > 0) {
            Pair<List<ParkingSpot>, List<ParkingSpot>> pair = getDeltaParkingSpots(parkingSpots, previousParkingSpots);

            deltaParkingSpots = pair.first;
            nonDeltaParkingSpots = pair.second;

        } else {
            deltaParkingSpots = parkingSpots;
        }

        /*
        // redraw destination spot marker
        if (destinationMarkerOptions != null) {
            map.addMarker(destinationMarkerOptions);
        }
        // redraw closest spot marker
        if (closestSpotMarkerOptions != null) {
            map.addMarker(closestSpotMarkerOptions);
        }
        */
        Log.d("DRAWSPOT", deltaParkingSpots.size() + " deltas");

        //Draw changed spots
        for (int i = 0; i < deltaParkingSpots.size(); i++) {
            //Log.i(TAG + "10", "Spot Lat: " + spot.getLatitude() + " Spot Lng: " + spot.getLongitude());
            ParkingSpot spot = deltaParkingSpots.get(i);

            //remove the previous marker, if there is one

            if (redrawSpotIDs.containsKey(spot.getSpotId())) {
                map.removeMarker(previousParkingSpots.get(redrawSpotIDs.get(spot.getSpotId())).getMarker());
            }

            LatLng spotLatLng = new LatLng(spot.getLat(), spot.getLon());
            if (spot.getStatus().equals(SPOT_AVAILABLE)) {
                //Add the marker to the map and store it to the ParkingSpot
                Marker marker = map.addMarker(new MarkerViewOptions().position(spotLatLng).icon(openParkingSpotIcon));
                marker.setSnippet("Status: Available" + "\nLocation: " + spot.getLat() + ", " + spot.getLon());
                spot.setMarker(marker);
            } else {
                Marker marker = map.addMarker(new MarkerViewOptions().position(spotLatLng).icon(closedParkingSpotIcon));
                marker.setSnippet("Status: Unavailable" + "\nLocation: " + spot.getLat() + ", " + spot.getLon());
                spot.setMarker(marker);
            }
            //Replace the old ParkingSpot with the new one
            deltaParkingSpots.remove(i);
            deltaParkingSpots.add(i, spot);
        }

        //Store the updated parkingspots into previousParkingSpots for the next update round (must make sure all spots have markers)
        if (previousParkingSpots == null) {
            previousParkingSpots = new ArrayList<>();
        }
        previousParkingSpots.clear();
        previousParkingSpots.addAll(deltaParkingSpots);
        previousParkingSpots.addAll(nonDeltaParkingSpots);
        getClosestParkingSpot(parCareService, searchedLatString, searchedLngString);
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
                            PolylineOptions polylineOptionsWalk = new PolylineOptions()
                                    .add(points)
                                    .color(Color.parseColor("#d84315"))
                                    .width(2);
                            map.addPolyline(polylineOptionsWalk);
                            break;
                        case ROUTE_TYPE_DRIVING:
                            PolylineOptions polylineOptionsDrive = new PolylineOptions()
                                    .add(points)
                                    .color(Color.parseColor("#3887be"))
                                    .alpha((float) 0.5)
                                    .width(5);
                            map.addPolyline(polylineOptionsDrive);
                            drivingRoutePolyline = polylineOptionsDrive.getPolyline();
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