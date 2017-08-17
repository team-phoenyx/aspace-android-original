package com.aspace.aspace.tutorialfragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.aspace.aspace.AspaceRetrofitService;
import com.aspace.aspace.R;
import com.aspace.aspace.SwipeDirection;
import com.aspace.aspace.TutorialViewPager;
import com.aspace.aspace.retrofitmodels.Feature;
import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialLocationsFragment extends Fragment {

    String homeLocationID = "", workLocationID = "";
    AutoCompleteTextView homeAddressEditText, workAddressEditText;
    TextView errorTextView;
    ArrayAdapter<String> autocompleteAdapter;
    AspaceRetrofitService mapboxService;
    List<Feature> rawSuggestions;
    LocationEngineListener locationEngineListener;
    LocationEngine locationEngine;
    Location currentLocation;
    TutorialViewPager parentViewPager;
    Button nextButton, backButton;

    private static final int REQUEST_LOCATION_PERMISSION = 3139;
    private static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_locations, container, false);

        parentViewPager = (TutorialViewPager) getActivity().findViewById(R.id.pager);

        locationEngine = LocationSource.getLocationEngine(getActivity());
        locationEngine.activate();

        homeAddressEditText = (AutoCompleteTextView) viewGroup.findViewById(R.id.home_address_edittext);
        workAddressEditText = (AutoCompleteTextView) viewGroup.findViewById(R.id.work_address_edittext);

        nextButton = (Button) getActivity().findViewById(R.id.next_button);
        backButton = (Button) getActivity().findViewById(R.id.back_button);

        enableGps();

        try {
            currentLocation = locationEngine.getLastLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        homeAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    enableGps();
                    try {
                        currentLocation = locationEngine.getLastLocation();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        workAddressEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    enableGps();
                    try {
                        currentLocation = locationEngine.getLastLocation();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        homeAddressEditText.setLines(1);
        workAddressEditText.setLines(1);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MAPBOX_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        mapboxService = retrofit.create(AspaceRetrofitService.class);

        homeAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                homeLocationID = "";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //refresh the adapter with new string s

                final List<String> autocompleteSuggestions = new ArrayList<String>();

                if (s.length() == 0) {
                    autocompleteSuggestions.clear();

                    autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                    homeAddressEditText.setAdapter(autocompleteAdapter);

                    if (workAddressEditText.getText().toString().equals("") || !workLocationID.isEmpty()) {
                        parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        nextButton.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    String proximityString = Double.toString(currentLocation.getLongitude()) + "," + Double.toString(currentLocation.getLatitude());
                    mapboxService.getGeocodingSuggestions(s.toString(), proximityString, getString(R.string.access_token)).enqueue(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                            GeocodingResponse geocodingResponse = response.body();

                            if (geocodingResponse == null) return;

                            rawSuggestions = geocodingResponse.getFeatures();

                            for (Feature feature : rawSuggestions) {
                                autocompleteSuggestions.add(feature.getPlaceName());
                            }

                            autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                            homeAddressEditText.setAdapter(autocompleteAdapter);
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                            Log.e("MAPBOX_GEO_AUTO", "Fetch failed");
                        }
                    });

                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.none);
                    nextButton.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        workAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                workLocationID = "";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final List<String> autocompleteSuggestions = new ArrayList<String>();

                if (s.length() == 0) {
                    autocompleteSuggestions.clear();

                    autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                    workAddressEditText.setAdapter(autocompleteAdapter);

                    if (homeAddressEditText.getText().toString().equals("") || !homeLocationID.isEmpty()) {
                        parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        nextButton.setVisibility(View.VISIBLE);
                        backButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    String proximityString = Double.toString(currentLocation.getLongitude()) + "," + Double.toString(currentLocation.getLatitude());
                    mapboxService.getGeocodingSuggestions(s.toString(), proximityString, getString(R.string.access_token)).enqueue(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                            GeocodingResponse geocodingResponse = response.body();

                            if (geocodingResponse == null) return;

                            rawSuggestions = geocodingResponse.getFeatures();

                            for (Feature feature : rawSuggestions) {
                                autocompleteSuggestions.add(feature.getPlaceName());
                            }

                            autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                            workAddressEditText.setAdapter(autocompleteAdapter);
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                            Log.e("MAPBOX_GEO_AUTO", "Fetch failed");
                        }
                    });

                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.none);
                    nextButton.setVisibility(View.GONE);
                    backButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        homeAddressEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feature result = rawSuggestions.get(position);
                homeAddressEditText.setText(result.getPlaceName());
                homeLocationID = result.getId();

                if (workAddressEditText.getText().toString().isEmpty() || !workLocationID.isEmpty()) {
                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);
                    nextButton.setVisibility(View.VISIBLE);
                    backButton.setVisibility(View.VISIBLE);
                }
            }
        });

        workAddressEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feature result = rawSuggestions.get(position);
                workAddressEditText.setText(result.getPlaceName());
                workLocationID = result.getId();

                if (homeAddressEditText.getText().toString().isEmpty() || !homeLocationID.isEmpty()) {
                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);
                    nextButton.setVisibility(View.VISIBLE);
                    backButton.setVisibility(View.VISIBLE);
                }
            }
        });

        return viewGroup;
    }

    public String[] getLocationIDs() {
        return new String[]{homeLocationID, workLocationID};
    }

    private void enableGps() {
        //Check if location services are turned on
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder enableGPSBuilder = new AlertDialog.Builder(getActivity()).setTitle("Enable GPS").setMessage("Please enable GPS for app functionality").setCancelable(false).setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (homeAddressEditText.hasFocus()) homeAddressEditText.clearFocus();
                    if (workAddressEditText.hasFocus()) workAddressEditText.clearFocus();

                    dialog.dismiss();
                }
            });

            enableGPSBuilder.create().show();
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return;

        //Check if access to fine location is granted
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            enableLocation();
        }
    }

    private void enableLocation() {

        locationEngineListener = new LocationEngineListener() {
            @Override
            public void onConnected() {
                // No action needed here.
            }

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {


                    locationEngine.removeLocationEngineListener(this);
                }
            }
        };
        locationEngine.addLocationEngineListener(locationEngineListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    enableLocation();
                } else {
                    //permission not granted
                    homeAddressEditText.clearFocus();
                    workAddressEditText.clearFocus();
                }
                break;
        }
    }
}
