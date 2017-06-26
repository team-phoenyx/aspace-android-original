package me.parcare.parcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Terrance on 6/24/2017.
 */

public class SplashActivity extends AppCompatActivity {

    boolean isConnected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            SharedPreferences sharedPreferences = getSharedPreferences("me.parcare.parcare", MODE_PRIVATE);

            String userID = sharedPreferences.getString(getString(R.string.sp_user_id_tag), "");
            String userAccessToken = sharedPreferences.getString(getString(R.string.sp_user_access_token_tag), "");
            String userPhoneNumber = sharedPreferences.getString(getString(R.string.sp_user_phone_number_tag), "");

            if (userID.isEmpty() || userAccessToken.isEmpty() || userPhoneNumber.isEmpty()) {
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginIntent);
                finish();
            } else {
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
