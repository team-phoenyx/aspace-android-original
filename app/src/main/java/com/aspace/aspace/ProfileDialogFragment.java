package com.aspace.aspace;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspace.aspace.realmmodels.UserCredentials;
import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.SavedLocation;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

    TextView nameTextView, noLocationsLabel;
    ListView locationsListView;
    ImageButton settingsButton;
    AspaceRetrofitService aspaceService;
    Realm realm;
    ProfileDialogListAdapter profileDialogListAdapter;
    List<SavedLocation> locations = new ArrayList<>();
    SavedLocation clickedItem;
    ProgressBar nameProgressCircle, locationsProgressCircle;

    private double lat, lng;
    private String userID, userAccessToken, userPhoneNumber, realmEncryptionKey;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle extras = getArguments();
        lat = extras.getDouble("lat");
        lng = extras.getDouble("lng");
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));

        Realm.init(getActivity());

        byte[] key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();

        realm = Realm.getInstance(config);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
        aspaceService = retrofit.create(AspaceRetrofitService.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.profile_dialog, null);

        builder.setView(dialogView).setCancelable(false);

        profileDialogListAdapter = new ProfileDialogListAdapter(new ArrayList<SavedLocation>());
        locationsListView = (ListView) dialogView.findViewById(R.id.locations_listview);
        locationsProgressCircle = (ProgressBar) dialogView.findViewById(R.id.saved_locations_progresscircle);
        nameProgressCircle = (ProgressBar) dialogView.findViewById(R.id.name_progress_circle);
        locationsListView.setAdapter(profileDialogListAdapter);

        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickedItem = locations.get(position);
                ((MainActivity) getActivity()).setClickedLocation(clickedItem);
                getDialog().dismiss();
            }
        });

        nameTextView = (TextView) dialogView.findViewById(R.id.name_textview);
        noLocationsLabel = (TextView) dialogView.findViewById(R.id.no_locations_label);

        aspaceService.getProfile(userPhoneNumber, userAccessToken, userID).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Profile userProfile = response.body();
                if (userProfile.getResponseCode() == null) {
                    nameProgressCircle.setVisibility(View.GONE);
                    locationsProgressCircle.setVisibility(View.GONE);

                    nameTextView.setText(userProfile.getName().isEmpty() ? "Your Profile" : userProfile.getName());
                    locations = userProfile.getLocations();

                    profileDialogListAdapter = new ProfileDialogListAdapter(locations);
                    locationsListView.setAdapter(profileDialogListAdapter);

                    if (locations.size() == 0) {
                        noLocationsLabel.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                Log.d("GET_PROFILE_FAIL", t.getMessage());
            }
        });

        settingsButton = (ImageButton) dialogView.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Intent startSettingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startSettingsIntent.putExtra(getString(R.string.user_id_tag), userID);
                startSettingsIntent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                startSettingsIntent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);
                startSettingsIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                getActivity().startActivity(startSettingsIntent);
                /* Old Fragment Stuff
                SettingsFragment settingsFragment= new SettingsFragment();
                FragmentManager fragmentManager = getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.settings_fragment_framelayout, settingsFragment);
                fragmentTransaction.commit();
                */
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Log Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Clear all UserCredential objects from Realm
                final RealmResults<UserCredentials> credentialResults = realm.where(UserCredentials.class).findAll();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        credentialResults.deleteAllFromRealm();
                    }
                });

                realm.close();

                //Start loginactivity
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                loginIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                startActivity(loginIntent);
                getActivity().finish();
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private class ProfileDialogListAdapter extends BaseAdapter {

        List<SavedLocation> locations;

        public ProfileDialogListAdapter(List<SavedLocation> locations) {
            this.locations = locations;
        }

        @Override
        public int getCount() {
            return locations.size();
        }

        @Override
        public Object getItem(int position) {
            return locations.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.profile_dialog_saved_locations_row, parent, false);
            TextView addresssTextView = (TextView) convertView.findViewById(R.id.saved_location_address);
            TextView nameTextView = (TextView) convertView.findViewById(R.id.saved_location_label);

            addresssTextView.setText(locations.get(position).getAddress());
            nameTextView.setText(locations.get(position).getName());

            return convertView;
        }
    }

}
