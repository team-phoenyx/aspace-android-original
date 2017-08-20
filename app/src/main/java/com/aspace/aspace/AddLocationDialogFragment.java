package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.aspace.aspace.retrofitmodels.Feature;
import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.aspace.aspace.retrofitmodels.ResponseCode;
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
 * Created by Zula on 7/24/17.
 */

public class AddLocationDialogFragment extends DialogFragment {

    private AlertDialog dialog;
    private AutoCompleteTextView locationEditText;
    private TextView addLocationTitleTextView;
    private EditText locationNameEditText;
    private ArrayAdapter<String> autocompleteAdapter;
    private AspaceRetrofitService mapboxService;
    private AspaceRetrofitService aspaceService;
    private List<Feature> rawSuggestions;
    private Context activityContext;

    private LocationEngineListener locationEngineListener;
    private LocationEngine locationEngine;
    private Location currentLocation;

    private Feature selectedLocation;

    private String editLocID, editLocName, editLocOriginalName, editLocAddress;

    private String userID, userAccessToken, userPhoneNumber;
    private static final String MAPBOX_BASE_URL = "https://api.mapbox.com/";
    private static final int REQUEST_LOCATION_PERMISSION = 3139;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        locationEngine = LocationSource.getLocationEngine(getActivity());
        locationEngine.activate();

        activityContext = getActivity().getApplicationContext();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_location_dialog, null);
        dialogView.requestFocus();

        locationEditText = (AutoCompleteTextView) dialogView.findViewById(R.id.add_location_edittext);
        locationNameEditText = (EditText) dialogView.findViewById(R.id.add_location_name_edittext);
        addLocationTitleTextView = (TextView) dialogView.findViewById(R.id.add_location_title);
        builder.setView(dialogView).setCancelable(false);

        Bundle args = getArguments();
        userID = args.getString(getString(R.string.user_id_tag));
        userAccessToken = args.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = args.getString(getString(R.string.user_phone_number_tag));

        editLocID = args.getString("loc_id");
        editLocName = args.getString("loc_name");
        editLocOriginalName = args.getString("loc_original_name");
        editLocAddress = args.getString("loc_address");

        if (editLocID != null) {
            locationNameEditText.setText(editLocName);
            locationEditText.setText(editLocOriginalName + ", " + editLocAddress);
            addLocationTitleTextView.setText("Edit Location");
        }

        Retrofit retrofit = new Retrofit.Builder().baseUrl(MAPBOX_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        mapboxService = retrofit.create(AspaceRetrofitService.class);

        retrofit = new Retrofit.Builder().baseUrl(getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
        aspaceService = retrofit.create(AspaceRetrofitService.class);

        enableGps();

        try {
            currentLocation = locationEngine.getLastLocation();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        locationNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                else dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        locationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                selectedLocation = null;
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (locationEditText.isPerformingCompletion()) return;

                final List<String> autocompleteSuggestions = new ArrayList<String>();

                if (s.length() == 0) {
                    autocompleteSuggestions.clear();

                    autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line);
                    locationEditText.setAdapter(autocompleteAdapter);
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

                            autocompleteAdapter = new ArrayAdapter<>(activityContext, android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                            locationEditText.setAdapter(autocompleteAdapter);
                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        locationEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = rawSuggestions.get(position);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);

                String name = selectedLocation.getText();
                if (selectedLocation.getAddress() != null) name = selectedLocation.getAddress() + " " + name;

                locationNameEditText.setText(name);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                String lon, lat, name, address, originalName;
                name = locationNameEditText.getText().toString();
                if (selectedLocation != null) {
                    lon = Double.toString(selectedLocation.getCenter().get(0));
                    lat = Double.toString(selectedLocation.getCenter().get(1));

                    originalName = selectedLocation.getText();
                    if (selectedLocation.getAddress() != null) originalName = selectedLocation.getAddress() + " " + originalName;
                    address = selectedLocation.getPlaceName().substring(originalName.length() + 2);

                    if (!originalName.equals(name)) address = originalName + ", " + address;
                } else {
                    lon = "";
                    lat = "";
                    originalName = editLocOriginalName;
                    address = editLocAddress;
                    if (!name.equals(originalName) && editLocName.equals(editLocOriginalName)) address = editLocOriginalName + ", " + address;
                    if (!editLocName.equals(editLocOriginalName) && name.equals(editLocOriginalName)) address = address.substring(name.length() + 2);
                }


                final DialogInterface.OnDismissListener listener = (DialogInterface.OnDismissListener) getActivity();

                if (editLocID == null) {
                    aspaceService.addSavedLocation(userPhoneNumber, userAccessToken, userID, address, name, originalName, lat, lon).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            if ("100".equals(response.body().getRespCode())) {
                                dialog.dismiss();
                                listener.onDismiss(dialog);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });
                } else {
                    aspaceService.updateSavedLocation(userPhoneNumber, userAccessToken, userID, address, name, originalName, editLocID, lat, lon).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            if ("100".equals(response.body().getRespCode())) {
                                dialog.dismiss();
                                listener.onDismiss(dialog);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });
                }

            }
        });

        dialog = builder.create();
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
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
                    if (locationEditText.hasFocus()) locationEditText.clearFocus();

                    dialog.dismiss();
                }
            });

            enableGPSBuilder.create().show();
        }

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return;

        //Check if access to fine location is granted
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                    locationEditText.clearFocus();
                }
                break;
        }
    }
}
