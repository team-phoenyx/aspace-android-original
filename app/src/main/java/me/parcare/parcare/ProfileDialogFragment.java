package me.parcare.parcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.parcare.parcare.models.Feature;
import me.parcare.parcare.models.GeocodingResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static me.parcare.parcare.MainActivity.BASE_URL;
import static me.parcare.parcare.MainActivity.MAPBOX_BASE_URL;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

    SharedPreferences sharedPreferences;
    ImageView profilePictureImageView;
    EditText nameEditText;
    AutoCompleteTextView homeAddressEditText, workAddressEditText;
    TextView errorTextView;
    ArrayAdapter<String> autocompleteAdapter;
    PCRetrofitInterface mapboxService;
    List<Feature> rawSuggestions;

    private String homeLocationID = "", workLocationID = "";
    private double lat, lng;

    private static final String SP_USER_NAME_TAG = "user_name";
    private static final String SP_USER_HOME_ADDRESS_TAG = "user_home_address";
    private static final String SP_USER_WORK_ADDRESS_TAG = "user_work_address";
    private static final String SP_USER_HOME_LOC_ID_TAG = "user_home_loc_id";
    private static final String SP_USER_WORK_LOC_ID_TAG = "user_work_loc+id";
    private static final String USER_PROFILE_PICTURE_FILENAME_TAG = "user_profile_picture.png";
    private static final String USER_PROFILE_PICTURE_DIRECTORY_TAG = "profile_picture_directory_path";
    private static final int PICK_IMAGE_REQUEST_CALLBACK = 1;

    private PCRetrofitInterface parCareService;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle extras = getArguments();
        lat = extras.getDouble("lat");
        lng = extras.getDouble("lng");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.profile_dialog, null);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        parCareService = retrofit.create(PCRetrofitInterface.class);

        retrofit = new Retrofit.Builder().baseUrl(MAPBOX_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

        mapboxService = retrofit.create(PCRetrofitInterface.class);

        builder.setView(dialogView).setCancelable(false);

        sharedPreferences = getActivity().getSharedPreferences("me.parcare.parcare", Context.MODE_PRIVATE);

        profilePictureImageView = (ImageView) dialogView.findViewById(R.id.profile_pic_imageview);
        nameEditText = (EditText) dialogView.findViewById(R.id.name_edittext);
        homeAddressEditText = (AutoCompleteTextView) dialogView.findViewById(R.id.home_address_edittext);
        workAddressEditText = (AutoCompleteTextView) dialogView.findViewById(R.id.work_address_edittext);
        errorTextView = (TextView) dialogView.findViewById(R.id.enter_name_label);

        nameEditText.setText(sharedPreferences.getString(SP_USER_NAME_TAG, "Your Name"));
        homeAddressEditText.setText(sharedPreferences.getString(SP_USER_HOME_ADDRESS_TAG, ""));
        workAddressEditText.setText(sharedPreferences.getString(SP_USER_WORK_ADDRESS_TAG, ""));
        homeLocationID = sharedPreferences.getString(SP_USER_HOME_LOC_ID_TAG, "");
        workLocationID = sharedPreferences.getString(SP_USER_WORK_LOC_ID_TAG, "");

        String directoryPath = sharedPreferences.getString(USER_PROFILE_PICTURE_DIRECTORY_TAG, "no_picture");
        if (directoryPath.equals("no_picture")) {
            profilePictureImageView.setImageResource(R.drawable.sample_profile_pic);
        } else {
            profilePictureImageView.setImageBitmap(openImage(directoryPath));
        }

        homeAddressEditText.setLines(1);
        workAddressEditText.setLines(1);

        homeAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                homeLocationID = "";
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //refresh the adapter with new string s

                final List<String> autocompleteSuggestions = new ArrayList<String>();

                if (s.equals("")) {
                    autocompleteSuggestions.clear();

                    autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                    homeAddressEditText.setAdapter(autocompleteAdapter);
                } else {
                    String proximityString = Double.toString(lng) + "," + Double.toString(lat);
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

                if (s.equals("")) {
                    autocompleteSuggestions.clear();

                    autocompleteAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, autocompleteSuggestions);
                    workAddressEditText.setAdapter(autocompleteAdapter);
                } else {
                    String proximityString = Double.toString(lng) + "," + Double.toString(lat);
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
            }
        });

        workAddressEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Feature result = rawSuggestions.get(position);
                workAddressEditText.setText(result.getPlaceName());
                workLocationID = result.getId();
            }
        });

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseImageIntent = new Intent();
                chooseImageIntent.setType("image/*");
                chooseImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(chooseImageIntent, "Select a picture"), PICK_IMAGE_REQUEST_CALLBACK);
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /* KEEP THIS METHOD
                    It is only used to show the Save button, the actual behavior of the save button
                    is defined below in onResume()
                 */
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) return;

        switch (requestCode) {
            case PICK_IMAGE_REQUEST_CALLBACK:
                Uri imageURI = data.getData();

                if (imageURI == null) break;

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                    Log.d("IMAGEPICKER", String.valueOf(bitmap));


                    //Check if image is ridiculously large (over 512 on largest dimension)
                    if (Math.max(bitmap.getHeight(), bitmap.getWidth()) > 512) {
                        //Scale the bitmap
                        int scaledHeight = 0, scaledWidth = 0;
                        double scaleFactor = Math.max(bitmap.getHeight(), bitmap.getWidth()) / (float) 512;

                        scaledHeight = (int) Math.floor(bitmap.getHeight() / scaleFactor);
                        scaledWidth = (int) Math.floor(bitmap.getWidth() / scaleFactor);

                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);

                        profilePictureImageView.setImageBitmap(scaledBitmap);
                    } else {
                        profilePictureImageView.setImageBitmap(bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (nameEditText.getText().toString().equals("")) {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("Please enter your name");
                    } else if (homeLocationID.equals("") && !homeAddressEditText.getText().toString().equals("")) {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("Select a home address, or leave empty");
                    } else if (workLocationID.equals("") && !workAddressEditText.getText().toString().equals("")) {
                        errorTextView.setVisibility(View.VISIBLE);
                        errorTextView.setText("Select a work address, or leave empty");
                    } else {
                            String directory = saveImage(((BitmapDrawable) profilePictureImageView.getDrawable()).getBitmap());

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SP_USER_NAME_TAG, nameEditText.getText().toString());
                            editor.putString(SP_USER_HOME_ADDRESS_TAG, homeAddressEditText.getText().toString());
                            editor.putString(SP_USER_WORK_ADDRESS_TAG, workAddressEditText.getText().toString());
                            editor.putString(SP_USER_HOME_LOC_ID_TAG, homeLocationID);
                            editor.putString(SP_USER_WORK_LOC_ID_TAG, workLocationID);

                            editor.putString(USER_PROFILE_PICTURE_DIRECTORY_TAG, directory);
                            editor.commit();

                            d.dismiss();

                            String profileName = nameEditText.getText().toString();
                            String workAddress = workAddressEditText.getText().toString();
                            String homeAddress = homeAddressEditText.getText().toString();
                            String userId = "30"; //TODO placeholder, passed from mainactivity using Bundle, or sharedPrefs
                            updateProfile(parCareService, profileName, workAddress, homeAddress, homeLocationID,
                                    workLocationID, userId);

                    }
                }
            });
        }
    }

    private String saveImage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,USER_PROFILE_PICTURE_FILENAME_TAG);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    private Bitmap openImage(String path) {
        try {
            File f = new File(path, USER_PROFILE_PICTURE_FILENAME_TAG);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Updates the user's profile based on the given info
    private void updateProfile(PCRetrofitInterface parCareService, String name, String workAddress, String homeAddress, String homeLocID, String workLocID, String userId) {
        Call<String> call = parCareService.updateProfile(name, workAddress, homeAddress, homeLocID, workLocID, userId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String resp = response.body();
                Log.i("PROFILE", "Response Received: " + resp);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("PROFILE", "Profile update response failure: " + t.toString());
            }
        });
    }
}
