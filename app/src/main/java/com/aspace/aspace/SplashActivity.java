package com.aspace.aspace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.aspace.aspace.realmmodels.UserCredentials;
import com.aspace.aspace.retrofitmodels.ReauthenticateResponse;
import com.securepreferences.SecurePreferences;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Terrance on 6/24/2017.
 */

public class SplashActivity extends AppCompatActivity {

    boolean isConnected;
    String realmEncryptionKey;
    Realm realm;
    PCRetrofitInterface parcareService;

    byte[] key;

    public static final String BASE_URL = "http://138.68.241.101:3000/api/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isConnected = false;

        key = new byte[64];

        //Initialize Realm
        Realm.init(SplashActivity.this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                isConnected = isConnectedToServer(getString(R.string.parcare_base_url), 5000);

                //Check if client is connected to server
                if (isConnected) {
                    SharedPreferences securePreferences = new SecurePreferences(SplashActivity.this);
                    realmEncryptionKey = securePreferences.getString(getString(R.string.realm_encryption_key_tag), "");

                    //Check if realmEncryptionKey exists; if it doesn't, user is new, go to LoginActivity for unAuth start
                    if (!realmEncryptionKey.equals("")) {

                        key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

                        RealmConfiguration config = new RealmConfiguration.Builder()
                                .encryptionKey(key)
                                .build();

                        realm = Realm.getInstance(config);

                        RealmResults<UserCredentials> credentialsRealmResults = realm.where(UserCredentials.class).findAll();

                        //Check for existing credential objects; if there are none, unAuth start, loginActivity
                        if (credentialsRealmResults.size() == 0) {
                            startLoginActivity();
                        } else {
                            //Retrieve credential data from object
                            UserCredentials credentials = credentialsRealmResults.get(0);
                            final String userID = credentials.getUserID();
                            final String userAccessToken = credentials.getUserAccessToken();
                            final String userPhoneNumber = credentials.getUserPhoneNumber();

                            //If any data is empty, unAuth start, loginactivity
                            if (userPhoneNumber.equals("") || userID.equals("") || userAccessToken.equals("")) {
                                startLoginActivity();
                            } else {
                                Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

                                parcareService = retrofit.create(PCRetrofitInterface.class);

                                parcareService.reauthenticate(userAccessToken, userPhoneNumber, userID).enqueue(new Callback<ReauthenticateResponse>() {
                                    @Override
                                    public void onResponse(Call<ReauthenticateResponse> call, Response<ReauthenticateResponse> response) {
                                        //If success...
                                        if (response.body().getRespCode().equals("101") || response.body().getRespCode().equals("102")) {
                                            Intent intent;
                                            Log.d("REAUTH_RESPONSE", response.body().getRespCode());
                                            //if (response.body().getRespCode().equals("102")) intent = new Intent(getApplicationContext(), MainActivity.class);
                                            //else intent = new Intent(getApplicationContext(), TutorialActivity.class);
                                            intent = new Intent(getApplicationContext(), MainActivity.class); // since everything is optional, just go to the map
                                            intent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                                            intent.putExtra(getString(R.string.user_id_tag), userID);
                                            intent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                                            intent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);

                                            startActivity(intent);
                                            finish();
                                        } else {
                                            startLoginActivity();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ReauthenticateResponse> call, Throwable t) {
                                        Log.d("REAUTHENTICATE_FAIL", t.getMessage());
                                    }
                                });

                            }
                        }
                    } else {
                        //Realm encryption hasn't been set up yet, must generate and store a key
                        new SecureRandom().nextBytes(key);

                        //base64 encode string and store
                        realmEncryptionKey = Base64.encodeToString(key, Base64.DEFAULT);

                        SharedPreferences.Editor editor = new SecurePreferences(SplashActivity.this).edit();
                        editor.putString(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                        editor.apply();

                        startLoginActivity();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(findViewById(android.R.id.content), "Can't connect to server", Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Restarts activity for retry
                                    Intent intent = getIntent();
                                    finish();
                                    startActivity(intent);
                                }
                            }).show();
                        }
                    });
                }

            }
        }).start();
    }

    private void startLoginActivity() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();

        realm = Realm.getInstance(config);

        //Delete all credential objects
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

        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
        startActivity(loginIntent);
        finish();
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
