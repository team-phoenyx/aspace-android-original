package com.aspace.aspace;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.ViewGroup;
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
    private ListView myVehiclesList;
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

    private static final String BASE_URL = "http://192.241.224.224:3000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        parcareService = retrofit.create(PCRetrofitInterface.class);

        Bundle extras = getIntent().getExtras();

        String profileName = extras.getString("profileName");
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));

        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbarExitButton = (ImageButton) findViewById(R.id.settings_toolbar_exit_button);
        nameEditText = (EditText) findViewById(R.id.settings_name_edit_text);
        nameEditButton = (ImageButton) findViewById(R.id.settings_name_edit_button);
        myVehiclesList = (ListView) findViewById(R.id.settings_my_vehicle_list);
        addVehicleButton = (Button) findViewById(R.id.settings_add_vehicle_button);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account_button);

        myVehiclesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // selected position of button set to the very first in the listview. This will need to change to the position
        // of the user's selected vehicle in the listview that will be saved
        // (server will keep track of it? add a vehicle retrofit w/ boolean selected?)
        selectedVehicleButtonPosition = 0;
        vehicleListAdapter = new VehicleListAdapter();
        myVehiclesList.setAdapter(vehicleListAdapter);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Settings");

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
                if (!nameEditText.isFocusableInTouchMode()) {
                    nameEditText.setCursorVisible(true);
                    nameEditText.setFocusableInTouchMode(true);
                    nameEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    // place the cursor at the end.
                    nameEditText.setSelection(nameEditText.getText().length());
                    nameEditText.requestFocus();
                    nameEditButton.setColorFilter(ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT);
                } else {
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
                builder.setTitle("Delete Account?")
                        .setMessage("Deleting your account is a permanent action and cannot be reversed. Continue?")
                        .setPositiveButton("Yep", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
    }

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
                // update the profile with paramaters retrieved from getprofile
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
            return 3;
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
            convertView = getLayoutInflater().inflate(R.layout.vehicle_list_row, parent, false);
            ImageButton removeVehicleButton = (ImageButton) convertView.findViewById(R.id.settings_my_vehicle_list_remove_button);
            TextView vehicleNameLabel = (TextView) convertView.findViewById(R.id.settings_my_vehicle_list_vehicle_label);
            RadioButton selectVehicleButton = (RadioButton) convertView.findViewById(R.id.settings_my_vehicle_list_select_button);

            selectVehicleButton.setChecked(position == selectedVehicleButtonPosition);
            if (position == selectedVehicleButtonPosition) {
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
                    // do stuff here to delete the vehicle and update profile
                    // selectVehicleButtonPosition = -1;
                }
            });

            return convertView;
        }
    }
}
