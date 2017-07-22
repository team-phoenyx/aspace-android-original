package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.aspace.aspace.realmmodels.UserCredentials;
import com.aspace.aspace.retrofitmodels.Feature;
import com.aspace.aspace.retrofitmodels.GeocodingResponse;
import com.aspace.aspace.retrofitmodels.Profile;
import com.aspace.aspace.retrofitmodels.UpdateProfileResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.aspace.aspace.MainActivity.BASE_URL;
import static com.aspace.aspace.MainActivity.MAPBOX_BASE_URL;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

    TextView nameTextView;
    ListView locationsListView;
    ImageButton settingsButton;
    PCRetrofitInterface parcareService;
    Realm realm;

    private static final String BASE_URL = "http://192.241.224.224:3000/api/";
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

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        parcareService = retrofit.create(PCRetrofitInterface.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.profile_dialog, null);

        builder.setView(dialogView).setCancelable(false);

        nameTextView = (TextView) dialogView.findViewById(R.id.name_textview);
        settingsButton = (ImageButton) dialogView.findViewById(R.id.settings_button);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        parcareService.getProfile(userPhoneNumber, userAccessToken, userID).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                Profile userProfile = response.body();
                //TODO check resp code 7, otherwise snackbar
                nameTextView.setText(userProfile.getName());
                //TODO when profile endpoint is updated, populate listview and if is empty, do something
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable t) {
                //TODO as of July 10, a failed getProfile will go here :/
                Log.d("GET_PROFILE_FAIL", t.getMessage());
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

}
