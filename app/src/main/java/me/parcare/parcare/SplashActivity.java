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
    String realmEncryptionKey;
    Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isConnected = false;

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
                        byte[] key = new byte[64];

                        key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

                        //Initialize Realm
                        Realm.init(SplashActivity.this);

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
                            String userID = credentials.getUserID();
                            String userAccessToken = credentials.getUserAccessToken();
                            String userPhoneNumber = credentials.getUserPhoneNumber();

                            //If any data is empty, unAuth start, loginactivity
                            if (userPhoneNumber.equals("") || userID.equals("") || userAccessToken.equals("")) {
                                startLoginActivity();
                            } else {
                                //TODO reauthenticate call; check for success or fail response
                                //If success...
                                if (true) {
                                    Intent intent;
                                    //If success 101, intent = NameActivity
                                    intent = new Intent(getApplicationContext(), NameActivity.class);
                                    //If success 102, intent = MainActivity
                                    intent = new Intent(getApplicationContext(), MainActivity.class);

                                    intent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                                    intent.putExtra(getString(R.string.user_id_tag), userID);
                                    intent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                                    intent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);

                                    startActivity(intent);
                                    finish();
                                } else {
                                    //If fail (4 or 5), startLoginActivity()
                                    startLoginActivity();
                                }
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
        //Delete all credential objects
        if (!realmEncryptionKey.equals("") && realm != null) {
            final RealmResults<UserCredentials> credentialResults = realm.where(UserCredentials.class).findAll();

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    credentialResults.deleteAllFromRealm();
                }
            });
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
