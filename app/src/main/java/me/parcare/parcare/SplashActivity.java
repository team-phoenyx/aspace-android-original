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

import java.net.URL;
import java.net.URLConnection;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
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

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                isConnected = isConnectedToServer(getString(R.string.parcare_base_url), 5000);
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isConnected) {
            SharedPreferences securePreferences = new SecurePreferences(SplashActivity.this);
            String realmEncryptionKey = securePreferences.getString(getString(R.string.realm_encryption_key_tag), "");

            if (!realmEncryptionKey.equals("")) {
                byte[] key = new byte[64];

                key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

                Realm.init(this);

                RealmConfiguration config = new RealmConfiguration.Builder()
                        .encryptionKey(key)
                        .build();

                Realm realm = Realm.getInstance(config);

                RealmResults<UserCredentials> credentialsRealmResults = realm.where(UserCredentials.class).findAll();

                if (credentialsRealmResults.size() == 0) {
                    startLoginActivity();
                }

                UserCredentials credentials = credentialsRealmResults.get(0);

                //TODO work on here, check if credentials is all populated


            } else {
                startLoginActivity();
            }




            //TODO use realm to check for credentails
            SharedPreferences sharedPreferences = getSharedPreferences("me.parcare.parcare", MODE_PRIVATE);

            String userID = sharedPreferences.getString(getString(R.string.sp_user_id_tag), "");
            String userAccessToken = sharedPreferences.getString(getString(R.string.sp_user_access_token_tag), "");
            String userPhoneNumber = sharedPreferences.getString(getString(R.string.sp_user_phone_number_tag), "");

            if (userID.isEmpty() || userAccessToken.isEmpty() || userPhoneNumber.isEmpty()) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
            } else {
                //Todo check if profile has name; go to nameactivity if no
                Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                startIntent.putExtra(getString(R.string.sp_user_id_tag), userID);
                startIntent.putExtra(getString(R.string.sp_user_access_token_tag), userAccessToken);
                startIntent.putExtra(getString(R.string.sp_user_phone_number_tag), userPhoneNumber);
                startActivity(startIntent);
                finish();
            }


        } else {
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
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
