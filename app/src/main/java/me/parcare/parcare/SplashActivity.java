package me.parcare.parcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Terrance on 6/24/2017.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Check connection

        Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(startIntent);
        finish();
    }
}
