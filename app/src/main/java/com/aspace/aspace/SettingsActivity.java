package com.aspace.aspace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aspace.aspace.realmmodels.UserCredentials;
import com.aspace.aspace.retrofitmodels.Car;
import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.ResponseCode;
import com.aspace.aspace.retrofitmodels.SavedLocation;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity implements DialogInterface.OnDismissListener{
    Toolbar toolbar;
    ImageButton toolbarExitButton;
    EditText nameEditText;
    ImageButton nameEditButton;
    NonScrollListView myVehiclesList;
    Button addVehicleButton;
    Button deleteAccountButton;
    NonScrollListView myLocationsList;
    Button addLocationButton;
    AspaceRetrofitService aspaceService;
    String userName;
    List<Car> userCars;
    List<SavedLocation> userLocations;
    String userID;
    String userPhoneNumber;
    String userAccessToken;
    String realmEncryptionKey;
    VehicleListAdapter vehicleListAdapter;
    LocationListAdapter locationListAdapter;
    int selectedVehicleButtonPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
        aspaceService = retrofit.create(AspaceRetrofitService.class);

        Bundle extras = getIntent().getExtras();

        String profileName = extras.getString("profileName");
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));


        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbarExitButton = (ImageButton) findViewById(R.id.settings_toolbar_exit_button);
        nameEditText = (EditText) findViewById(R.id.settings_name_edit_text);
        nameEditButton = (ImageButton) findViewById(R.id.settings_name_edit_button);
        myVehiclesList = (NonScrollListView) findViewById(R.id.settings_my_vehicle_list);
        addVehicleButton = (Button) findViewById(R.id.settings_add_vehicle_button);
        myLocationsList = (NonScrollListView) findViewById(R.id.settings_my_locations_list);
        addLocationButton = (Button) findViewById(R.id.settings_add_location_button);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account_button);

        myVehiclesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        myLocationsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // selected position of button set to the very first in the listview. This will need to change to the position
        // of the user's selected vehicle in the listview that will be saved
        // (server will keep track of it? add a vehicle retrofit w/ boolean selected?)
        selectedVehicleButtonPosition = 0;

        getProfile();

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.settings_toolbar_title));

        nameEditText.setText(profileName);

        toolbarExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameEditText.isFocusableInTouchMode()) { // user taps to start edit
                    nameEditText.setCursorVisible(true);
                    nameEditText.setFocusableInTouchMode(true);
                    nameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    // place the cursor at the end.
                    nameEditText.setSelection(nameEditText.getText().length());
                    nameEditText.requestFocus();
                    nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT);
                } else { // user taps to end edit
                    aspaceService.updateProfile(nameEditText.getText().toString(), userPhoneNumber, userAccessToken, userID).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.greyed_out));
                            nameEditText.setFocusableInTouchMode(false);
                            nameEditText.clearFocus();
                            nameEditText.setInputType(InputType.TYPE_NULL);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });
                }
            }
        });

        // Action listener to handle saving upon done or enter key input.
        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) && nameEditText.isFocusableInTouchMode()) {
                    aspaceService.updateProfile(nameEditText.getText().toString(), userPhoneNumber, userAccessToken, userID).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.greyed_out));
                            nameEditText.setFocusableInTouchMode(false);
                            nameEditText.clearFocus();
                            nameEditText.setInputType(InputType.TYPE_NULL);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });
                    return true;
                }
                return false;
            }
        });

        addVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddVehicleDialogFragment addVehicleDialogFragment = new AddVehicleDialogFragment();
                addVehicleDialogFragment.show(getFragmentManager(), "addVehicleDialogFragment");
            }
        });

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddLocationDialogFragment locationDialogFragment = new AddLocationDialogFragment();
                Bundle args = new Bundle();
                args.putString(getString(R.string.user_id_tag), userID);
                args.putString(getString(R.string.user_access_token_tag), userAccessToken);
                args.putString(getString(R.string.user_phone_number_tag), userPhoneNumber);
                locationDialogFragment.setArguments(args);
                locationDialogFragment.show(getFragmentManager(), "addLocationDialogFragment");
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this, R.style.DeleteAccountDialogTheme);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.settings_delete_account_dialog_title))
                        .setMessage(getString(R.string.settings_delete_account_dialog_message))
                        .setPositiveButton(getString(R.string.settings_delete_account_dialog_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                aspaceService.deleteAccount(userPhoneNumber, userAccessToken, userID).enqueue(new Callback<ResponseCode>() {
                                    @Override
                                    public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                                        if ("100".equals(response.body().getRespCode())) {
                                            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                            loginIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);

                                            //Clear usercredential objects from realm
                                            byte[] key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);
                                            RealmConfiguration config = new RealmConfiguration.Builder()
                                                    .encryptionKey(key)
                                                    .build();

                                            Realm realm = Realm.getInstance(config);

                                            if (!realmEncryptionKey.equals("") && realm != null) {
                                                final RealmResults<UserCredentials> credentialResults = realm.where(UserCredentials.class).findAll();

                                                realm.executeTransaction(new Realm.Transaction() {
                                                    @Override
                                                    public void execute(Realm realm) {
                                                        credentialResults.deleteAllFromRealm();
                                                    }
                                                });

                                                realm.close();
                                            }

                                            //start the intent
                                            startActivity(loginIntent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseCode> call, Throwable t) {
                                        Snackbar.make(findViewById(android.R.id.content), "Something happened, please try again", Snackbar.LENGTH_LONG).show();
                                    }
                                });
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.settings_delete_account_dialog_negative), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

    // Retrieves the user's profile and uses the information retrieved to update with the new NAME
    private void getProfile() {
        aspaceService.getProfile(userPhoneNumber, userAccessToken, userID).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Profile userProfile = response.body();
                if (userProfile.getResponseCode() == null) {
                    nameEditText.setText(userProfile.getName());
                    userCars = userProfile.getCars();
                    userLocations = userProfile.getLocations();
                    locationListAdapter = new LocationListAdapter(userLocations);
                    vehicleListAdapter = new VehicleListAdapter(userCars);

                    myLocationsList.setAdapter(locationListAdapter);
                    myVehiclesList.setAdapter(vehicleListAdapter);
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.d("GET_PROFILE_FAIL", t.getMessage());
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        getProfile();
    }

    private class VehicleListAdapter extends BaseAdapter {

        List<Car> carList;

        public VehicleListAdapter(List<Car> carList) {
            this.carList = carList;
        }

        @Override
        public int getCount() {
            return carList.size();
        }

        @Override
        public Object getItem(int position) {
            return carList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.vehicle_list_row, parent, false);
            ImageButton removeVehicleButton = (ImageButton) convertView.findViewById(R.id.settings_my_vehicle_list_remove_button);
            final TextView vehicleNameLabel = (TextView) convertView.findViewById(R.id.settings_my_vehicle_list_vehicle_label);
            RadioButton selectVehicleButton = (RadioButton) convertView.findViewById(R.id.settings_my_vehicle_list_select_button);

            vehicleNameLabel.setText(carList.get(position).getName());

            //TODO: when car is selected, save the car length in SharedPrefs for other parts of app to use
            // if the row is selected, check the radio button.
            selectVehicleButton.setChecked(position == selectedVehicleButtonPosition);
            if (position == selectedVehicleButtonPosition) { // if this row is the selected one
                selectVehicleButton.setButtonTintList(ColorStateList.valueOf(Color.BLACK));
                vehicleNameLabel.setTextColor(Color.BLACK);
                vehicleNameLabel.setTypeface(vehicleNameLabel.getTypeface(), Typeface.BOLD);
            } else {
                selectVehicleButton.setButtonTintList(ColorStateList.valueOf(getColor(R.color.greyed_out)));
                vehicleNameLabel.setTextColor(getColor(R.color.greyed_out));
                vehicleNameLabel.setTypeface(vehicleNameLabel.getTypeface(), Typeface.NORMAL);
            }
            selectVehicleButton.setTag(position);
            selectVehicleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedVehicleButtonPosition = (Integer)view.getTag();
                    notifyDataSetChanged();
                }
            });

            removeVehicleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    aspaceService.removeCar(userPhoneNumber, userAccessToken, userID, carList.get(position).getVin()).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            if (response.body().getRespCode().equals("100")) {
                                userCars.remove(position);
                                carList.remove(position);
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });


                }
            });

            return convertView;
        }
    }

    private class LocationListAdapter extends BaseAdapter {

        List<SavedLocation> locationList;

        public LocationListAdapter(List<SavedLocation> locationList) {
            this.locationList = locationList;
        }

        @Override
        public int getCount() {
            return locationList.size();
        }

        @Override
        public Object getItem(int position) {
            return locationList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.settings_saved_locations_row, parent, false);

            ImageButton removeLocationButton = (ImageButton) convertView.findViewById(R.id.settings_my_locations_list_remove_button);
            ImageView locationIcon = (ImageView) convertView.findViewById(R.id.settings_location_icon);
            TextView locationLabel = (TextView) convertView.findViewById(R.id.settings_saved_location_label);
            TextView locationAddress = (TextView) convertView.findViewById(R.id.settings_saved_location_address);

            removeLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    aspaceService.removeSavedLocation(userPhoneNumber, userAccessToken, userID, locationList.get(position).getLocId()).enqueue(new Callback<ResponseCode>() {
                        @Override
                        public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                            if (response.body().getRespCode().equals("100")) {
                                if (position < userLocations.size()) userLocations.remove(position);
                                //if (position < locationList.size()) locationList.remove(position);
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseCode> call, Throwable t) {

                        }
                    });
                }
            });

            // set the icon

            // set the label
            locationLabel.setText(locationList.get(position).getName());
            // set the address
            locationAddress.setText(locationList.get(position).getAddress());
            return convertView;
        }
    }
}
