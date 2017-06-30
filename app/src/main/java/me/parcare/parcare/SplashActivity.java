package me.parcare.parcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;

import com.securepreferences.SecurePreferences;

import java.net.HttpURLConnection;
import java.net.URL;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import me.parcare.parcare.realmmodels.UserCredentials;

/**
 * Created by Terrance on 6/24/2017.
 */

public class SplashActivity extends AppCompatActivity {

    boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FOR DEBUGGING PURPOSES ONLY, TO GET TO MAINACTIVITY
        /*
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
        */

        isConnected = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                isConnected = isConnectedToServer(getString(R.string.parcare_base_url), 5000);

                if (isConnected) {
                    SharedPreferences securePreferences = new SecurePreferences(SplashActivity.this);
                    String realmEncryptionKey = securePreferences.getString(getString(R.string.realm_encryption_key_tag), "");

                    if (!realmEncryptionKey.equals("")) {
                        byte[] key = new byte[64];

                        key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

                        Realm.init(SplashActivity.this);

                        RealmConfiguration config = new RealmConfiguration.Builder()
                                .encryptionKey(key)
                                .build();

                        Realm realm = Realm.getInstance(config);

                        RealmResults<UserCredentials> credentialsRealmResults = realm.where(UserCredentials.class).findAll();

                        if (credentialsRealmResults.size() == 0) {
                            startLoginActivity();
                        } else {
                            UserCredentials credentials = credentialsRealmResults.get(0);
                            String userID = credentials.getUserID();
                            String userAccessToken = credentials.getUserAccessToken();
                            String userPhoneNumber = credentials.getUserPhoneNumber();

                            if (userPhoneNumber.equals("") || userID.equals("") || userAccessToken.equals("")) {
                                startLoginActivity();
                            } else {
                                //TODO Pull profile from API, and check if there is name; go to NameActivity if no
                                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                                startIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                                startIntent.putExtra(getString(R.string.user_id_tag), userID);
                                startIntent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                                startActivity(startIntent);
                                finish();
                            }
                        }
                    } else {
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
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.putExtra(getString(R.string.realm_encryption_key_tag), "");
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
