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

import java.util.List;

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
    private LatLng currentDisplayTopRight;
    private LatLng currentDisplayBottomLeft;
    private LatLng currentDisplayBottomRight;

    private static final int DEFAULT_SNAP_ZOOM = 16;
    private static final String TAG = "MainActivity";

    public static final String BASE_URL = "http://placeholder.com/";

    private ParkingSpot closestSpot;
    private List<ParkingSpot> parkingSpotsNearby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_main);

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

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



        /*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final PCRetrofitInterface parCareService = retrofit.create(PCRetrofitInterface.class);
        */

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

                    /*
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
                    */

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
            currentDisplayTopRight = map.getProjection().fromScreenLocation(new PointF(viewportWidth, 0));
            currentDisplayBottomLeft = map.getProjection().fromScreenLocation(new PointF(viewportWidth, viewportHeight));
            currentDisplayBottomRight = map.getProjection().fromScreenLocation(new PointF(0, viewportHeight));

            Log.i(TAG, "Top Left Lat//Lng: " + currentDisplayTopLeft.getLatitude() + "//" + currentDisplayTopLeft.getLongitude());
            Log.i(TAG, "Bottom Right Lat//Lng: " + currentDisplayBottomRight.getLatitude() + "//" + currentDisplayBottomRight.getLongitude());
        }
    }
}
