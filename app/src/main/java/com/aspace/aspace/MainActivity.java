package com.aspace.aspace;

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
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.aspace.aspace.retrofitmodels.Feature;
import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.aspace.aspace.retrofitmodels.ParkingSpot;
import com.aspace.aspace.retrofitmodels.Suggestion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.mapbox.services.Constants;
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
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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

public class MainActivity extends AppCompatActivity implements PermissionsListener, TextToSpeech.OnInitListener {

    private MapView mMapView;
    private MapboxMap map;
    private FloatingSearchView searchView;
    private DisplayMetrics displayMetrics;
    private FloatingActionButton startNavigationFAB, cancelNavigationFAB, snapToLocationFAB, cancelRouteFAB;
    private LocationEngine locationEngine;
    private Location currentLocation;
    private LocationEngineListener locationEngineListener;
    private MarkerViewOptions closestSpotMarkerOptions, destinationMarkerOptions;
    private MarkerView destinationMarker;
    private Marker closestMarker;
    private LatLng currentDisplayTopLeft, currentDisplayBottomRight;
    private Icon openParkingSpotIcon, closedParkingSpotIcon, closestParkingSpotIcon;
    private Timer timer;
    private TimerTask updateSpotTimerTask;
    private List<SearchSuggestion> newSuggestions;
    private List<Feature> rawSuggestions;
    private List<ParkingSpot> previousParkingSpots;
    private HashMap<Integer, Integer> redrawSpotIDs;
    private PCRetrofitInterface parCareService, mapboxService;
    private MapboxNavigation navigation;
    private LatLng clickedSpotLatLng;
    private com.mapbox.services.api.directions.v5.models.DirectionsRoute route;
    private LegStep lastUpcomingStep;
    private TextToSpeech textToSpeech;
    private Position navDestination;
    private Polyline drivingRoutePolyline;
    private String searchedLatString, searchedLngString;
    private Toolbar navToolbar;
    private ConstraintLayout navLowerBar;
    private ImageView navManeuverImageView, navInfoDurationImageView, navInfoDistanceImageView, navInfoSpotsImageView;
    private ImageButton navMuteButton;
    private TextView navManeuverDistanceLabel, navInfoDurationLabel, navInfoDistanceLabel, navInfoSpotsLabel;
    private AutoResizeTextView navManeuverTargetLabel;
    private List<String> routeManeuverInstructions;
    private List<String> offRouteManeuverInstructions;

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

    //BOOLEAN FLAGS
    private boolean isUpdatingSpots;
    private boolean allowAlert;
    private boolean isNavMuted;
    private boolean alreadyNotifiedOneMile;
    private boolean alreadyNotifiedHalfMile;
    private boolean alreadyNotifiedClose;
    private boolean alreadyNotifiedManeuver;

    private String userID, userAccessToken, userPhoneNumber, realmEncryptionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LOCATION INIT
        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

        //MAPBOX INIT
        Mapbox.getInstance(this, getString(R.string.access_token));
        navigation = new MapboxNavigation(this, Mapbox.getAccessToken());
        navigation.setSnapToRoute(true);
        MapboxNavigationOptions mapboxNavigationOptions = navigation.getMapboxNavigationOptions();
        mapboxNavigationOptions.setMaximumDistanceOffRoute(50); //off-route threshold to 50 meters (default)

        setContentView(R.layout.activity_main);

        //VIEWS INIT
        startNavigationFAB = (FloatingActionButton) findViewById(R.id.navigate_route_fab);
        cancelNavigationFAB = (FloatingActionButton) findViewById(R.id.cancel_navigation_fab);
        mMapView = (MapView) findViewById(R.id.mapview);
        searchView = (FloatingSearchView) findViewById(R.id.search_view);
        snapToLocationFAB = (FloatingActionButton) findViewById(R.id.snap_to_location_fab);
        cancelRouteFAB = (FloatingActionButton) findViewById(R.id.cancel_route_fab);
        // toolbar views
        navManeuverImageView = (ImageView) findViewById(R.id.nav_ic_maneuver);
        navMuteButton = (ImageButton) findViewById(R.id.nav_mute_button);
        navManeuverDistanceLabel = (TextView) findViewById(R.id.nav_maneuver_distance);
        navManeuverTargetLabel = (AutoResizeTextView) findViewById(R.id.nav_maneuver_target);
        // lower white bar views
        navInfoDurationImageView = (ImageView) findViewById(R.id.nav_info_duration_icon);
        navInfoDistanceImageView = (ImageView) findViewById(R.id.nav_info_distance_icon);
        navInfoSpotsImageView = (ImageView) findViewById(R.id.nav_info_spots_icon);
        navInfoDurationLabel = (TextView) findViewById(R.id.nav_info_duration_label);
        navInfoDistanceLabel = (TextView) findViewById(R.id.nav_info_distance_label);
        navInfoSpotsLabel = (TextView) findViewById(R.id.nav_info_spots_label);

        mMapView.onCreate(savedInstanceState);

        navToolbar = (Toolbar) findViewById(R.id.nav_toolbar);
        setSupportActionBar(navToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        navLowerBar = (ConstraintLayout) findViewById(R.id.nav_subview);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        navManeuverTargetLabel.setWidth(displayMetrics.widthPixels - dpToPx(32) - navManeuverImageView.getWidth() - navMuteButton.getWidth());

        //RETROFIT INIT
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build();
        parCareService = retrofit.create(PCRetrofitInterface.class);
        retrofit = new Retrofit.Builder().baseUrl(MAPBOX_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        mapboxService = retrofit.create(PCRetrofitInterface.class);

        //ICONS INIT
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        openParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_available_48px);
        closedParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_unavailable_48px);
        closestParkingSpotIcon = iconFactory.fromResource(R.drawable.blue_marker_view);

        //IDENTIFIERS AND ENCRYPTIONKEY INIT
        Bundle extras = getIntent().getExtras();
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));

        //TTS INIT
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        //BOOLEAN FLAGS INIT
        isUpdatingSpots = true;
        allowAlert = true;

        //TIMER INIT
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        //********NAVIGATION EVENT HANDLERS********
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
                        textToSpeech.speak(routeProgress.getCurrentLeg().getSteps().get(0).getManeuver().getInstruction(), TextToSpeech.QUEUE_ADD, null, null);
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
                LegStep nextStep = routeProgress.getCurrentLegProgress().getUpComingStep();

                /*
                if (nextStep == null) {
                    navigation.endNavigation();
                    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_NONE);
                    cancelRouteFAB.setVisibility(View.VISIBLE);
                    cancelNavigationFAB.setVisibility(View.GONE);
                    snapToLocationFAB.setVisibility(View.GONE);
                    startNavigationFAB.setVisibility(View.VISIBLE);
                    navLowerBar.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) navToolbar.getLayoutParams();
                    layoutParams.height = dpToPx(64);
                    navToolbar.setLayoutParams(layoutParams);

                    navManeuverImageView.setVisibility(View.INVISIBLE);
                    navManeuverDistanceLabel.setVisibility(View.INVISIBLE);
                    navManeuverTargetLabel.setVisibility(View.INVISIBLE);
                    navMuteButton.setVisibility(View.INVISIBLE);
                    return;
                }
                */

                if (routeProgress.getCurrentLegProgress().getCurrentStep().getManeuver().getType().equals("arrive")) {
                    navigation.endNavigation();
                    map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_NONE);
                    cancelRouteFAB.setVisibility(View.VISIBLE);
                    cancelNavigationFAB.setVisibility(View.GONE);
                    snapToLocationFAB.setVisibility(View.GONE);
                    startNavigationFAB.setVisibility(View.VISIBLE);
                    navLowerBar.setVisibility(View.GONE);
                    searchView.setVisibility(View.VISIBLE);

                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) navToolbar.getLayoutParams();
                    layoutParams.height = dpToPx(64);
                    navToolbar.setLayoutParams(layoutParams);

                    navManeuverImageView.setVisibility(View.INVISIBLE);
                    navManeuverDistanceLabel.setVisibility(View.INVISIBLE);
                    navManeuverTargetLabel.setVisibility(View.INVISIBLE);
                    navMuteButton.setVisibility(View.INVISIBLE);
                    textToSpeech.speak("You have arrived at your destination", TextToSpeech.QUEUE_ADD, null, null);
                    return;
                }

                //NEW MANEUVER
                if (lastUpcomingStep == null || !nextStep.getManeuver().getInstruction().equals(lastUpcomingStep.getManeuver().getInstruction())) {
                    double distance = routeStepProgress.getDistanceRemaining();

                    if (distance <= 30.5 || (distance >= 150 && distance < 182.9) || (distance >= 800 && distance < 965.6) || (distance >= 1600 && distance < 1649.8)) {

                    } else {
                        String distanceString = translateDistance(distance);
                        String instruction = nextStep.getManeuver().getInstruction();
                        if (nextStep.getManeuver().getType().equals("arrive")) instruction = "Your destination is on the " + nextStep.getManeuver().getModifier();
                        textToSpeech.speak("In " + distanceString.replace("mi", "miles").replace("ft", "feet") + ", " + instruction, TextToSpeech.QUEUE_ADD, null, null);
                    }
                    alreadyNotifiedManeuver = false;
                    alreadyNotifiedOneMile = false;
                    alreadyNotifiedHalfMile = false;
                    alreadyNotifiedClose = false;
                }

                // Updating main navigation tool bar
                if (routeStepProgress.getDistanceRemaining() <= 30.5) {
                    if (!alreadyNotifiedManeuver && !nextStep.getManeuver().equals("arrive")) {
                        String directionString;
                        String maneuverType = nextStep.getManeuver().getType();
                        String maneuverMod = nextStep.getManeuver().getModifier();
                        if (maneuverType.equalsIgnoreCase("new name")) {
                            directionString = "continue" + " " + maneuverMod;
                        } else if (maneuverType.equalsIgnoreCase("on ramp")) {
                            if (maneuverMod == null || maneuverMod.isEmpty()) directionString = "take the ramp";
                            else directionString = "take the ramp on the " + maneuverMod;
                        } else {
                            directionString = maneuverType + " " + maneuverMod;
                        }
                        navManeuverDistanceLabel.setText(Character.toUpperCase(directionString.charAt(0)) + directionString.substring(1));
                        textToSpeech.speak(nextStep.getManeuver().getInstruction(), TextToSpeech.QUEUE_ADD, null, null);
                        alreadyNotifiedManeuver = true;
                    }
                }
                else {
                    String distanceString = translateDistance(routeStepProgress.getDistanceRemaining());
                    navManeuverDistanceLabel.setText("In " + distanceString);

                    String instruction = nextStep.getManeuver().getInstruction();

                    if (nextStep.getManeuver().getType().equals("arrive")) instruction = "Your destination is on the " + nextStep.getManeuver().getModifier();

                    if (distanceString.equals("1.0 mi") || distanceString.equals("1 mi")) {
                        if (!alreadyNotifiedOneMile) {
                            textToSpeech.speak("In one mile, " + instruction, TextToSpeech.QUEUE_ADD, null, null);
                            alreadyNotifiedOneMile = true;
                        }

                    }
                    if (distanceString.equals("0.5 mi")) {
                        if (!alreadyNotifiedHalfMile) {
                            textToSpeech.speak("In half a mile, " + instruction, TextToSpeech.QUEUE_ADD, null, null);
                            alreadyNotifiedHalfMile = true;
                        }
                    }
                    if (distanceString.equals("500 ft")) {
                        if (!alreadyNotifiedClose) {
                            textToSpeech.speak("In 500 feet, " + instruction, TextToSpeech.QUEUE_ADD, null, null);
                            alreadyNotifiedClose = true;
                        }

                    }
                }

                if (nextStep.getManeuver().getType().equals("arrive")) {
                    navManeuverTargetLabel.setText("Destination is on the " + nextStep.getManeuver().getModifier());
                } else if (nextStep.getManeuver().getType().equals("on ramp")) {
                    navManeuverTargetLabel.setText("Take the ramp");
                } else {
                    if (nextStep.getName().isEmpty()) navManeuverTargetLabel.setText(nextStep.getManeuver().getInstruction());
                    else navManeuverTargetLabel.setText(nextStep.getName());
                }

                String maneuverType = nextStep.getManeuver().getType();
                if (maneuverType.equalsIgnoreCase("continue")) {
                    maneuverType += "e";
                }
                String maneuverModifier = "" + nextStep.getManeuver().getModifier();

                String imageName = maneuverType;
                if (!maneuverModifier.isEmpty() && !maneuverModifier.equals("null")) {
                    imageName += " " + maneuverModifier;
                    imageName = imageName.replace(' ', '_');
                }
                int id = getResources().getIdentifier(imageName, "drawable", getPackageName());
                navManeuverImageView.setImageResource(id);

                // Updating lower navigation white bar
                navInfoDurationLabel.setText((int)routeProgress.getDurationRemaining() / 60 + " min");
                navInfoDistanceLabel.setText(translateDistance(routeProgress.getDistanceRemaining()));
                navInfoSpotsLabel.setText("10+ spots");

                // Complete directions log
                for (LegStep step : steps) {
                    Log.i(TAG + "Directions", "LEGSTEP: " + step.getName() + ", Maneuver: " + step.getManeuver().getInstruction() + ", Step distance: " + step.getDistance() + " Type: "+ step.getManeuver().getType() + " Modifier: " + step.getManeuver().getModifier());
                }

                lastUpcomingStep = nextStep;
            }
        });

        offRouteManeuverInstructions = new ArrayList<String>();
        navigation.addOffRouteListener(new OffRouteListener() {
            @Override
            public void userOffRoute(Location location) {
                //final LatLng newOriginLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Position newOrigin = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
                Position destination = Position.fromCoordinates(clickedSpotLatLng.getLongitude(), clickedSpotLatLng.getLatitude());
                navigation.getRoute(newOrigin, destination, new Callback<com.mapbox.services.api.directions.v5.models.DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Response<com.mapbox.services.api.directions.v5.models.DirectionsResponse> response) {
                        if (response.isSuccessful()) {
                            com.mapbox.services.api.directions.v5.models.DirectionsRoute newRoute = response.body().getRoutes().get(0);
                            if (!offRouteManeuverInstructions.isEmpty()) {
                                offRouteManeuverInstructions.clear();
                            }

                            for (LegStep ls : newRoute.getLegs().get(0).getSteps()) {
                                offRouteManeuverInstructions.add(ls.getManeuver().getInstruction());
                            }

                            // if this offroute's list of instructions are different than the one before this call
                            // routeManeuverInstructions initialized when first nav session begins.
                            if (!offRouteManeuverInstructions.equals(routeManeuverInstructions)) { // might also have to add && !routeManeuverInstructions.isEmpty()
                                // ends old starts new navigation session with the new route
                                navigation.endNavigation();
                                navigation.startNavigation(newRoute);
                                // draw new driving route
                                drawNavRoute(newRoute);
                                Log.i(TAG + "Directions", "Route Update Successful");
                                // update the route's instructions list to the new route instructions list
                                routeManeuverInstructions.clear();
                                routeManeuverInstructions.addAll(offRouteManeuverInstructions);
                                Snackbar.make(findViewById(android.R.id.content), "Rerouted", Snackbar.LENGTH_SHORT).show();
                            }

                            map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                            map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);
                            map.getTrackingSettings().setDismissAllTrackingOnGesture(false);
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

        //MUTE BUTTON HANDLER
        navMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNavMuted) {
                    isNavMuted = false;
                    navMuteButton.setImageDrawable(getDrawable(R.drawable.ic_volume_on_24dp));
                } else {
                    isNavMuted = true;
                    navMuteButton.setImageDrawable(getDrawable(R.drawable.ic_volume_off_24dp));
                }
            }
        });

        //MAPVIEW INIT HANDLER
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
                        if (destinationMarkerOptions != null && marker == destinationMarkerOptions.getMarker()) {
                            return false;
                        } else {
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
                                                if (navigation != null) {
                                                    navigation.endNavigation();
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
                        }
                        return true;
                    }
                });

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

                            getParkingSpotsNearby(parCareService, lowerLat, lowerLon, upperLat, upperLon);
                        }
                    }
                };
                timer.scheduleAtFixedRate(updateSpotTimerTask, 1000, SPOT_UPDATE_RATE);
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

        //********SEARCHVIEW EVENT HANDLERS********
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                toggleGps(true, false);

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
                if (currentQuery != null && !currentQuery.isEmpty() && !currentQuery.equals("")) {
                    if (rawSuggestions != null && rawSuggestions.size() > 0) {
                        onSearch(0); //automatically search the first suggestion, move the map camera
                        searchView.clearSearchFocus(); //collapses suggestions and search bar
                        searchView.setSearchText(newSuggestions.get(newSuggestions.size() - 1).getBody()); //sets the search text to the first suggestion
                    } else {
                        AlertDialog.Builder searchFailedDB = new AlertDialog.Builder(MainActivity.this);
                        searchFailedDB.setTitle("Unable to search").setMessage("Either we cannot find anything related to '" + currentQuery + "', or you are disconnected.");
                        searchFailedDB.create().show();
                    }
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

        //********FAB ONCLICKLISTENERS********
        snapToLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGps(true, false);

                try {
                    currentLocation = locationEngine.getLastLocation();
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return;
                }

                if (map != null && map.isMyLocationEnabled() && currentLocation != null) {
                    Location currentLocation = map.getMyLocation();
                    LatLng currentLocationLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, DEFAULT_SNAP_ZOOM));
                    if (cancelNavigationFAB.getVisibility() == View.VISIBLE) {
                        map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                        map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);
                        map.getTrackingSettings().setDismissAllTrackingOnGesture(false);
                    }
                }
            }
        });

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
                            drawNavRoute(route);

                            // if this is the first navigation session, then create the instructions list used to compare
                            // with the offroute's instructions list.
                            // isEmpty check also for when the user cancels the first nav session and restarts immediately
                            if (routeManeuverInstructions == null) {
                                routeManeuverInstructions = new ArrayList<String>();
                            } else if (!routeManeuverInstructions.isEmpty()) {
                                routeManeuverInstructions.clear();
                            }

                            // fill in the list with the route's instructions.
                            for (LegStep ls : route.getLegs().get(0).getSteps()) {
                                routeManeuverInstructions.add(ls.getManeuver().getInstruction());
                            }

                            navigation.startNavigation(route);
                            // not sure if it makes a difference if I use map.getMyLocation vs the currentLocation field here
                            Location myCurrentLocation = map.getMyLocation();
                            LatLng myCurrentLocationLatLng = new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocationLatLng, DEFAULT_SNAP_ZOOM));

                            map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
                            map.getTrackingSettings().setMyBearingTrackingMode(MyBearingTracking.COMPASS);
                            map.getTrackingSettings().setDismissAllTrackingOnGesture(false);

                            startNavigationFAB.setVisibility(View.GONE);
                            cancelRouteFAB.setVisibility(View.GONE);
                            cancelNavigationFAB.setVisibility(View.VISIBLE);
                            snapToLocationFAB.setVisibility(View.VISIBLE);

                            //TODO maybe animate the search bar out and the nav directions toolbar in?
                            searchView.setVisibility(View.GONE);

                            navLowerBar.setVisibility(View.VISIBLE);
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) navToolbar.getLayoutParams();
                            layoutParams.height = dpToPx(80);
                            navToolbar.setLayoutParams(layoutParams);

                            navManeuverImageView.setVisibility(View.VISIBLE);
                            navManeuverDistanceLabel.setVisibility(View.VISIBLE);
                            navManeuverTargetLabel.setVisibility(View.VISIBLE);
                            navMuteButton.setVisibility(View.VISIBLE);

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

        cancelNavigationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigation.endNavigation();
                map.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_NONE);
                cancelRouteFAB.setVisibility(View.VISIBLE);
                cancelNavigationFAB.setVisibility(View.GONE);
                snapToLocationFAB.setVisibility(View.GONE);
                startNavigationFAB.setVisibility(View.VISIBLE);
                navLowerBar.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);

                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) navToolbar.getLayoutParams();
                layoutParams.height = dpToPx(64);
                navToolbar.setLayoutParams(layoutParams);

                navManeuverImageView.setVisibility(View.INVISIBLE);
                navManeuverDistanceLabel.setVisibility(View.INVISIBLE);
                navManeuverTargetLabel.setVisibility(View.INVISIBLE);
                navMuteButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    public String shortenStreetName (String streetName) {
        return streetName.replace(" Street ", " St. ").replace(" Drive ", " Dr. ").replace(" Road ", " Rd. ").replace(" Boulevard ", " Blvd.").replace("Place", "Pl.").replace("Court", "Ct.").replace("Highway", "Hwy.").replace("Avenue", "Ave.").replace("Lane", "Ln.")
                .replace("South", "S.").replace("south", "S.").replace("North", "N.").replace("north", "N.").replace("West", "W.").replace("West", "S.").replace("South", "S.").replace("south", "S.");
    }

    public int dpToPx(int dp) {
        return Math.round(dp * ((float) displayMetrics.densityDpi / (float) DisplayMetrics.DENSITY_DEFAULT));
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
            toggleGps(true, false);
        }

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

        Gson gson = new Gson();

        Type newListType = new TypeToken<List<SearchSuggestion>>() {}.getType();
        Type rawListType = new TypeToken<List<Feature>>() {}.getType();

        outState.putString("newSuggestions", gson.toJson(newSuggestions, newListType));
        outState.putString("rawSuggestions", gson.toJson(rawSuggestions, rawListType));
    }

    @Override
    public void onLowMemory() {
        Log.i("ACTIVITY_LIFECYCLE", "LOW MEMORY");
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        Log.i("ACTIVITY_LIFECYCLE", "ACTIVITY DESTROYED");
        super.onDestroy();
        mMapView.onDestroy();
        if (locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
        }
        // make sure to remove all navigation listeners being used
        navigation.onDestroy(); // *
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
    private void getParkingSpotsNearby(PCRetrofitInterface parCareService, String lowerLat, String lowerLon, String upperLat, String upperLon) {
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

        call.enqueue(new Callback<ParkingSpot>() {
            @Override
            public void onResponse(Call<ParkingSpot> call, Response<ParkingSpot> response) {
                ParkingSpot closestSpot = response.body();
                LatLng closestSpotLatLng = new LatLng(closestSpot.getLat(), closestSpot.getLon());

                if (closestSpotMarkerOptions != null) {
                    // if the current marker's position is different than the "closest spot"
                    if (!closestSpotMarkerOptions.getMarker().getPosition().equals(closestSpotLatLng)) {
                        // removing and readding here makes the marker blink. is this necessary? *******
                        map.removeMarker(closestMarker);
                        closestMarker = map.addMarker(closestSpotMarkerOptions);
                        // ^^^^^
                        closestSpotMarkerOptions.getMarker().setPosition(closestSpotLatLng);
                    }
                } else {
                    closestSpotMarkerOptions = new MarkerViewOptions().position(closestSpotLatLng).icon(closestParkingSpotIcon);
                    if (closestMarker != null) map.removeMarker(closestMarker);
                    closestMarker = map.addMarker(closestSpotMarkerOptions);
                }
                map.selectMarker(closestMarker);
                mMapView.bringToFront();
                Log.d("MARKER", "CLOSEST MARKER SELECTED");
            }

            @Override
            public void onFailure(Call<ParkingSpot> call, Throwable t) {
                Log.i(TAG + "2", "Failed to connect: " + t.toString());
            }
        });
    }

    private List<List<ParkingSpot>> getDeltaParkingSpots(List<ParkingSpot> newParkingSpots, List<ParkingSpot> previousParkingSpots) {
        List<ParkingSpot> deltas = new ArrayList<>();
        List<ParkingSpot> nonDeltas = new ArrayList<>();
        List<ParkingSpot> removes = new ArrayList<>();
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

        for (int i = 0; i < previousParkingSpots.size(); i++) {
            ParkingSpot checkStillExistsSpot = previousParkingSpots.get(i);

            int oldSpotID = checkStillExistsSpot.getSpotId();
            boolean spotStillExists = false;
            for (ParkingSpot newSpot : newParkingSpots) {
                if (newSpot.getSpotId() == oldSpotID) {
                    spotStillExists = true;
                    break;
                }
            }

            if (!spotStillExists) {
                redrawSpotIDs.put(checkStillExistsSpot.getSpotId(), i);
                removes.add(checkStillExistsSpot);
            }
        }

        List<List<ParkingSpot>> redrawData = new ArrayList<>();
        redrawData.add(deltas);
        redrawData.add(nonDeltas);
        redrawData.add(removes);
        return redrawData;
    }

    private void drawSpots(List<ParkingSpot> parkingSpots) {
        List<ParkingSpot> deltaParkingSpots = new ArrayList<>();
        List<ParkingSpot> removedParkingSpots = new ArrayList<>();
        List<ParkingSpot> nonDeltaParkingSpots = new ArrayList<>();
        redrawSpotIDs = new HashMap<>();

        if (previousParkingSpots != null && previousParkingSpots.size() > 0) {
            List<List<ParkingSpot>> redrawData = getDeltaParkingSpots(parkingSpots, previousParkingSpots);

            deltaParkingSpots = redrawData.get(0);
            nonDeltaParkingSpots = redrawData.get(1);
            removedParkingSpots = redrawData.get(2);

        } else {
            deltaParkingSpots = parkingSpots;
        }

        Log.d("DRAWSPOT", deltaParkingSpots.size() + " deltas/adds; " + removedParkingSpots.size() + " removed;" + nonDeltaParkingSpots.size() + " unchanged.");

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

        for (ParkingSpot removeSpot : removedParkingSpots) {
            map.removeMarker(previousParkingSpots.get(redrawSpotIDs.get(removeSpot.getSpotId())).getMarker());
        }

        //Store the updated parkingspots into previousParkingSpots for the next update round (must make sure all spots have markers)
        if (previousParkingSpots == null) {
            previousParkingSpots = new ArrayList<>();
        }
        previousParkingSpots.clear();
        previousParkingSpots.addAll(deltaParkingSpots);
        previousParkingSpots.addAll(nonDeltaParkingSpots);

        //draw the closest spot marker
        if (searchedLatString != null && searchedLngString != null && !searchedLatString.equals("") && !searchedLngString.equals("")) {
            getClosestParkingSpot(parCareService, searchedLatString, searchedLngString);
        }
        Log.d("MARKERS", map.getMarkers().size() + " markers total");
    }

    // Draws polyline route from the given origin to the spot specified by the given destination. Route determined
    // by the given routeType.
    private void drawRouteToSpot(LatLng origin, LatLng destination, int routeType) {

        if (routeType == ROUTE_TYPE_DRIVING) {
            if (navigation != null) {
                Position originPos = Position.fromLngLat(origin.getLongitude(), origin.getLatitude());
                navDestination = Position.fromLngLat(destination.getLongitude(), destination.getLatitude());
                navigation.getRoute(originPos, navDestination, new Callback<com.mapbox.services.api.directions.v5.models.DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Response<com.mapbox.services.api.directions.v5.models.DirectionsResponse> response) {
                        com.mapbox.services.api.directions.v5.models.DirectionsRoute route = response.body().getRoutes().get(0);
                        drawNavRoute(route);
                    }

                    @Override
                    public void onFailure(Call<com.mapbox.services.api.directions.v5.models.DirectionsResponse> call, Throwable t) {

                    }
                });
            }
        } else if (routeType == ROUTE_TYPE_WALKING) {
            // Note that waypoint takes in longitude first instead of latitude
            Waypoint originWaypoint = new Waypoint(origin.getLongitude(), origin.getLatitude());
            Waypoint destinationWaypoint = new Waypoint(destination.getLongitude(), destination.getLatitude());
            MapboxDirections client = new MapboxDirections.Builder()
                    .setAccessToken(getString(R.string.access_token))
                    .setOrigin(originWaypoint)
                    .setDestination(destinationWaypoint)
                    .setProfile(DirectionsCriteria.PROFILE_WALKING)
                    .build();
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
                        PolylineOptions polylineOptionsWalk = new PolylineOptions()
                                .add(points)
                                .color(Color.parseColor("#d84315"))
                                .width(2);
                        map.addPolyline(polylineOptionsWalk);
                    } else {
                        Log.i(TAG + "2", response.raw().toString());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.i(TAG + "2", t.toString());
                }
            });
        }
    }

    // Draws the given route on the map -- shows driving route that nav is or will likely follow.
    public void drawNavRoute(com.mapbox.services.api.directions.v5.models.DirectionsRoute route) {
        LineString lineString = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
            //Log.i(TAG + "COORDS", "" + coordinates.get(i).toString());
        }

        // Draw Points on MapView

        if (map != null && !map.getPolylines().isEmpty()) {
            if (drivingRoutePolyline != null) {
                map.removePolyline(drivingRoutePolyline);
            }
        }

        PolylineOptions polylineOptionsDrive = new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#3887be"))
                .alpha((float) 0.5)
                .width(5);
        map.addPolyline(polylineOptionsDrive);
        drivingRoutePolyline = polylineOptionsDrive.getPolyline();
    }

    // Converts the given meters to the appropriate feet or miles measure.
    // Returns the translated distance concatenated with its respective measure.
    private static String translateDistance(double meters) {
        if (meters < 305) { // 305 meters is approximately 1000 feet
            String feet = metersToFeet(meters) + " ft";
            return feet;
        } else {
            double miles = metersToMiles(meters);
            return miles + " mi";
        }
    }

    // Converts the given meters into feet. Returns feet rounded to the
    // nearest 50th.
    private static int metersToFeet(double meters) {
        int feet = (int) Math.round(3.279 * meters);
        return (feet + 49)/50 * 50;
    }

    // Converts the given meters into miles. Returns miles rounded
    // to one decimal place.
    private static double metersToMiles(double meters) {
        double miles = Math.round(0.000621371192 * meters * 10);
        miles = miles / 10;
        return miles;
    }

    @Override
    public void onInit(int status) {
        
    }
}