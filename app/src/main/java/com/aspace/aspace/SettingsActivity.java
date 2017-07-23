package com.aspace.aspace;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageButton toolbarExitButton;
    private TextView myNameLabel;
    private EditText nameEditText;
    private ImageButton nameEditButton;
    private ListView myVehiclesList;
    private Button addVehicleButton;
    private Button deleteAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbarExitButton = (ImageButton) findViewById(R.id.settings_toolbar_exit_button);
        nameEditText = (EditText) findViewById(R.id.settings_name_edit_text);
        nameEditButton = (ImageButton) findViewById(R.id.settings_name_edit_button);
        myVehiclesList = (ListView) findViewById(R.id.settings_my_vehicle_list);
        addVehicleButton = (Button) findViewById(R.id.settings_add_vehicle_button);
        deleteAccountButton = (Button) findViewById(R.id.settings_delete_account_button);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Settings");

        toolbarExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        nameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SETTINGS", "CLICKED");
                if (!nameEditText.isFocusableInTouchMode()) {
                    nameEditText.setCursorVisible(true);
                    nameEditText.setFocusableInTouchMode(true);
                    nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    nameEditText.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    nameEditText.setFocusableInTouchMode(false);
                    nameEditText.clearFocus();
                    nameEditText.setInputType(InputType.TYPE_NULL);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
                }
            }
        });
    }
}
