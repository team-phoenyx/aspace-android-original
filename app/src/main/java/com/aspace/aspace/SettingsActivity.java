package com.aspace.aspace;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.UpdateProfileResponse;
import com.securepreferences.SecurePreferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton toolbarExitButton;
    private EditText nameEditText;
    private ImageButton nameEditButton;
    private NonScrollListView myVehiclesList;
    private Button addVehicleButton;
    private Button deleteAccountButton;
    private PCRetrofitInterface parcareService;
    private String userName;
    private String workAddress;
    private String homeAddress;
    private String homeLocId;
    private String workLocId;
    private String userID;
    private String userPhoneNumber;
    private String userAccessToken;
    private VehicleListAdapter vehicleListAdapter;
    private int selectedVehicleButtonPosition;
    private Set<String> userVINList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
        parcareService = retrofit.create(PCRetrofitInterface.class);

        Bundle extras = getIntent().getExtras();

        String profileName = extras.getString("profileName");
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));

        SharedPreferences securePreferences = new SecurePreferences(this);
        if (securePreferences.contains(getString(R.string.user_vin_list_tag))) {
            userVINList = securePreferences.getStringSet(getString(R.string.user_vin_list_tag), new HashSet<String>());
            if (!userVINList.isEmpty()) {
                // do stuff here if there's stuff inside
            } else {
                // do stuff here if it's empty
            }
        } else {
            userVINList = new HashSet<String>();
            SharedPreferences.Editor editor = new SecurePreferences(this).edit();
            editor.putStringSet(getString(R.string.user_vin_list_tag), userVINList);
            editor.apply();
        }

        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbarExitButton = (ImageButton) findViewById(R.id.settings_toolbar_exit_button);
        nameEditText = (EditText) findViewById(R.id.settings_name_edit_text);
        nameEditButton = (ImageButton) findViewById(R.id.settings_name_edit_button);
        myVehiclesList = (NonScrollListView) findViewById(R.id.settings_my_vehicle_list);
        addVehicleButton = (Button) findViewById(R.id.settings_add_vehicle_button);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account_button);

        myVehiclesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // selected position of button set to the very first in the listview. This will need to change to the position
        // of the user's selected vehicle in the listview that will be saved
        // (server will keep track of it? add a vehicle retrofit w/ boolean selected?)
        selectedVehicleButtonPosition = 0;

        // Configure the vehicle list's adapter
        vehicleListAdapter = new VehicleListAdapter();
        myVehiclesList.setAdapter(vehicleListAdapter);

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
                    getAndUpdateProfile();
                    nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.greyed_out));
                    nameEditText.setFocusableInTouchMode(false);
                    nameEditText.clearFocus();
                    nameEditText.setInputType(InputType.TYPE_NULL);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                }
            }
        });

        // Action listener to handle saving upon done or enter key input.
        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE) && nameEditText.isFocusableInTouchMode()) {
                    getAndUpdateProfile();
                    nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.greyed_out));
                    nameEditText.setFocusableInTouchMode(false);
                    nameEditText.clearFocus();
                    nameEditText.setInputType(InputType.TYPE_NULL);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
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
    private void getAndUpdateProfile() {
        parcareService.getProfile(userPhoneNumber, userAccessToken, userID).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                //TODO check resp code 7, otherwise snackbar
                Profile userProfile = response.body();
                userName = nameEditText.getText().toString();
                workAddress = userProfile.getWorkAddress();
                homeAddress = userProfile.getHomeAddress();
                homeLocId = userProfile.getHomeLocId();
                workLocId = userProfile.getWorkLocId();
                // update the profile with parameters retrieved from getprofile and the user's new name
                parcareService.updateProfile(userName, workAddress, homeAddress, homeLocId,
                        workLocId, userID, userPhoneNumber, userAccessToken)
                        .enqueue(new Callback<UpdateProfileResponse>() {
                            @Override
                            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {

                            }

                            @Override
                            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {

                            }
                        });
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                //TODO as of July 10, a failed getProfile will go here :/
                Log.d("GET_PROFILE_FAIL", t.getMessage());
            }
        });
    }

    private class VehicleListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userVINList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            convertView = getLayoutInflater().inflate(R.layout.vehicle_list_row, parent, false);
            ImageButton removeVehicleButton = (ImageButton) convertView.findViewById(R.id.settings_my_vehicle_list_remove_button);
            final TextView vehicleNameLabel = (TextView) convertView.findViewById(R.id.settings_my_vehicle_list_vehicle_label);
            RadioButton selectVehicleButton = (RadioButton) convertView.findViewById(R.id.settings_my_vehicle_list_select_button);

            // currently iterating through vin list to populate vehicle name since we don't have VIN API yet
            Iterator<String> iterator = userVINList.iterator();
            String vin = "VIN Number";
            for (int i = 0; i <= position; i++) {
                vin = iterator.next();
            }

            vehicleNameLabel.setText(vin);

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
                    // Need to wait for endpoint
                    // do stuff here to delete the vehicle and update profile
                    // selectVehicleButtonPosition = -1; ?
                    Iterator<String> iterator = userVINList.iterator();
                    for (int i = 0; i <= pos; i++) {
                        iterator.next();
                    }
                    iterator.remove(); // removes the vehicle's VIN from the user's VIN list.
                    SharedPreferences.Editor editor = new SecurePreferences(SettingsActivity.this).edit();
                    editor.putStringSet(getString(R.string.user_vin_list_tag), userVINList); // update user's saved vin list.
                    editor.apply();
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }

    // Returns a list of the user's saved VINs
    protected Set<String> getUserVINList() {
        return userVINList;
    }

    // Updates the list of vehicles, non-private for use in add vehicle fragment
    protected void updateVehicleListAdapter() {
        vehicleListAdapter.notifyDataSetChanged();
    }
}
