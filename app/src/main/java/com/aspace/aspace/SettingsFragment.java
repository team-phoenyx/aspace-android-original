package com.aspace.aspace;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * Created by Zula on 7/23/17.
 */

public class SettingsFragment extends Fragment {
    private Toolbar toolbar;
    private ImageButton toolbarExitButton;
    private EditText nameEditText;
    private ImageButton nameEditButton;
    private ListView myVehiclesList;
    private Button addVehicleButton;
    private Button deleteAccountButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_settings, container, false);

        toolbar = (Toolbar) viewGroup.findViewById(R.id.settings_toolbar);
        toolbarExitButton = (ImageButton) viewGroup.findViewById(R.id.settings_toolbar_exit_button);
        nameEditText = (EditText) viewGroup.findViewById(R.id.settings_name_edit_text);
        nameEditButton = (ImageButton) viewGroup.findViewById(R.id.settings_name_edit_button);
        myVehiclesList = (ListView) viewGroup.findViewById(R.id.settings_my_vehicle_list);
        addVehicleButton = (Button) viewGroup.findViewById(R.id.settings_add_vehicle_button);
        deleteAccountButton = (Button) viewGroup.findViewById(R.id.settings_delete_account_button);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
