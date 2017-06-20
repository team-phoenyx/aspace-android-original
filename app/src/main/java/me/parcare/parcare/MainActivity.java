package me.parcare.parcare;

import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;
    private MarkerViewOptions destinationMarkerOptions;
    private MarkerView destinationMarker;
    private LatLng currentDisplayTopLeft;
    private LatLng currentDisplayBottomRight;
    private Icon openParkingSpotIcon;
    private Icon closedParkingSpotIcon;

    private static final int DEFAULT_SNAP_ZOOM = 16;
    private static final String TAG = "MainActivity";

    public static final String BASE_URL = "http://192.241.224.224:3000/api/";

    private ParkingSpot closestSpot;
    private List<ParkingSpot> parkingSpotsNearby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        openParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_available_48px);
        closedParkingSpotIcon = iconFactory.fromResource(R.drawable.circle_unavailable_48px);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                setCurrentScreenBounds();
            }
        });

        mMapView.addOnMapChangedListener(new MapView.OnMapChangedListener() {
            @Override
            public void onMapChanged(int change) {
                if (change == REGION_DID_CHANGE) {
                    setCurrentScreenBounds();
                }
            }
        });


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final PCRetrofitInterface parCareService = retrofit.create(PCRetrofitInterface.class);


        // Initialize autocomplete search bar onto view
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Get info about the selected place.
                if (place != null) {
                    com.google.android.gms.maps.model.LatLng searchedLocation = place.getLatLng();
                    double searchedLat = searchedLocation.latitude;
                    double searchedLng = searchedLocation.longitude;
                    LatLng searchedLatLng = new LatLng(searchedLat, searchedLng);
                    // Snaps camera to the location of whatever was searched
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, DEFAULT_SNAP_ZOOM));
                    Log.i(TAG, "Place: " + place.getName());

                    // Place a new marker at searched position if first search, or reposition
                    // previously searched destination marker to new search.
                    if (destinationMarkerOptions == null) {
                        destinationMarkerOptions = new MarkerViewOptions()
                                .position(searchedLatLng)
                                .title(place.getName().toString());
                    } else {
                        destinationMarkerOptions = destinationMarkerOptions
                                .position(searchedLatLng)
                                .title(place.getName().toString());
                    }

                    if (destinationMarker == null) {
                        map.addMarker(destinationMarkerOptions);
                    } else {
                        destinationMarker.setPosition(searchedLatLng);
                    }
                    destinationMarker = destinationMarkerOptions.getMarker();

                    setCurrentScreenBounds();
                    String lowerLat = Double.toString(Math.min(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
                    String upperLat = Double.toString(Math.max(currentDisplayTopLeft.getLatitude(), currentDisplayBottomRight.getLatitude()));
                    String lowerLon = Double.toString(Math.min(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));
                    String upperLon = Double.toString(Math.max(currentDisplayTopLeft.getLongitude(), currentDisplayBottomRight.getLongitude()));

                    Log.i(TAG + "3", lowerLat);
                    Log.i(TAG + "3", lowerLon);
                    Log.i(TAG + "3", upperLat);
                    Log.i(TAG + "3", upperLon);
                    parkingSpotsNearby = getParkingSpotsNearby(parCareService, lowerLat, lowerLon, upperLat, upperLon);
                    //parkingSpotsNearby = getParkingSpotsNearby(parCareService, "47.604327", "-122.2987024", "47.604327", "-122.2983136");
                    //drawSpots(parkingSpotsNearby);

                    /* *********************************************
                    String searchedLatString = searchedLat + "";
                    String searchedLngString = searchedLng + "";
                    Call<ParkingSpot> call = parCareService.getClosestSpot(searchedLatString, searchedLngString);
                    call.enqueue(new Callback<ParkingSpot>() {
                        @Override
                        public void onResponse(Call<ParkingSpot> call, Response<ParkingSpot> response) {
                            closestSpot = response.body();
                            double spotLat = Double.valueOf(closestSpot.getLatitude());
                            double spotLng = Double.valueOf(closestSpot.getLongitude());
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(spotLat, spotLng))
                                    .title("Closest Parking Spot Here"));
                        }

                        @Override
                        public void onFailure(Call<ParkingSpot> call, Throwable t) {
                            Log.e(TAG, "Unable to receive response from server", t);
                        }
                    });
                    ************************************************/

                    // Debugging purposes
//                    List<ParkingSpot> spotsList = getParkingSpotInfo(parCareService, "003");
//                    for (ParkingSpot spot : spotsList) {
//                        Log.i(TAG + "2", "LAT: " + spot.getLatitude() + " LNG: " + spot.getLongitude());
//                    }
                }
            }


            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
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

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    private List<ParkingSpot> getParkingSpotInfo(PCRetrofitInterface parCareService, String spotId) {
        final List<ParkingSpot> spotInfo = new ArrayList<ParkingSpot>();
        Call<List<ParkingSpot>> call = parCareService.getSpotInfo(spotId);
        call.enqueue(new Callback<List<ParkingSpot>>() {
            @Override
            public void onResponse(Call<List<ParkingSpot>> call, Response<List<ParkingSpot>> response) {
                if (response.isSuccessful()) {
                    List<ParkingSpot> spots = response.body();
                    for (ParkingSpot spot : spots) {
                        spotInfo.add(spot);
                    }
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

        return spotInfo;
    }

    // Retrieves all of the spots in a bound area given by upper and lower latitudes/longitudes,
    // returns a list of the spots in the area.
    private List<ParkingSpot> getParkingSpotsNearby(PCRetrofitInterface parCareService,
                                                    String lowerLat, String lowerLon,
                                                    String upperLat, String upperLon) {
        final List<ParkingSpot> nearbySpots = new ArrayList<ParkingSpot>();
        Call<List<ParkingSpot>> call = parCareService.getNearbySpots(lowerLat, lowerLon, upperLat, upperLon);
        call.enqueue(new Callback<List<ParkingSpot>>() {
            @Override
            public void onResponse(Call<List<ParkingSpot>> call, Response<List<ParkingSpot>> response) {
                if (response.isSuccessful()) {
                    List<ParkingSpot> spots = response.body();
                    drawSpots(spots);
                    for (ParkingSpot spot : spots) {
                        nearbySpots.add(spot);
                        Log.i(TAG + "3", spot.getLatitude() + "/"+ spot.getLongitude());
                    }
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

        return nearbySpots;
    }

    private void drawSpots(List<ParkingSpot> parkingSpots) {
        map.clear();
        for (ParkingSpot spot : parkingSpots) {
            Log.i(TAG + "10", "Spot Lat: " + spot.getLatitude() + " Spot Lng: " + spot.getLongitude());
            String status = spot.getStatus();
            LatLng spotLatLng = new LatLng(spot.getLatitude(), spot.getLongitude());
            if (status.equals("T")) {
                map.addMarker(new MarkerViewOptions()
                    .position(spotLatLng)
                    .icon(openParkingSpotIcon));
            } else {
                map.addMarker(new MarkerViewOptions()
                    .position(spotLatLng)
                    .icon(closedParkingSpotIcon));
            }
        }
    }

    // FAKE DATA
//    private List<ParkingSpot> getFakeParkingSpotData() {
//        List<ParkingSpot> fakeSpots = new ArrayList<ParkingSpot>();
//        fakeSpots.add(new ParkingSpot(47.6553351, -122.3035199));
//        fakeSpots.add(new ParkingSpot(47.6566424, -122.3063321));
//        fakeSpots.add(new ParkingSpot(47.6562283, -122.3073819));
//        return fakeSpots;
//    }
}
