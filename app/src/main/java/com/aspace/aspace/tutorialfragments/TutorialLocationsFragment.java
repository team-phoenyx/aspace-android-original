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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.aspace.aspace.PCRetrofitInterface;
import com.aspace.aspace.R;
import com.aspace.aspace.retrofitmodels.Feature;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import java.util.List;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialLocationsFragment extends Fragment {

    String homeLocationID = "", workLocationID = "";
    AutoCompleteTextView homeAddressEditText, workAddressEditText;
    TextView errorTextView;
    ArrayAdapter<String> autocompleteAdapter;
    PCRetrofitInterface mapboxService;
    List<Feature> rawSuggestions;
    LocationEngineListener locationEngineListener;
    LocationEngine locationEngine;
    Location currentLocation;

    private static final int REQUEST_LOCATION_PERMISSION = 3139;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_locations, container, false);

        locationEngine = LocationSource.getLocationEngine(getActivity());
        locationEngine.activate();

        homeAddressEditText = (AutoCompleteTextView) viewGroup.findViewById(R.id.home_address_edittext);
        workAddressEditText = (AutoCompleteTextView) viewGroup.findViewById(R.id.work_address_edittext);

        enableGps();

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
