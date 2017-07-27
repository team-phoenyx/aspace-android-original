package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.securepreferences.SecurePreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zula on 7/24/17.
 */

public class AddVehicleDialogFragment extends DialogFragment {
    private EditText vinNumberEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_vehicle_dialog, null);
        dialogView.requestFocus();

        vinNumberEditText = (EditText) dialogView.findViewById(R.id.add_vehicle_dialog_vin_edit_text);
        builder.setView(dialogView).setCancelable(false);
        final Set<String> userVINList = ((SettingsActivity)getActivity()).getUserVINList();
        SharedPreferences securePreferences = new SecurePreferences(getActivity());
        if (securePreferences.contains(getString(R.string.user_vin_list_tag))) {

        } else {

        }


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do stuff in here to update list and server with new vehicle
                String vin = vinNumberEditText.getText().toString();
                userVINList.add(vin);
                SharedPreferences.Editor editor = new SecurePreferences(getActivity()).edit();
                editor.putStringSet(getString(R.string.user_vin_list_tag), userVINList);
                editor.apply();
                ((SettingsActivity)getActivity()).updateVehicleListAdapter();
                SharedPreferences securePreferences = new SecurePreferences(getActivity());
                for (String vinNum : securePreferences.getStringSet(getString(R.string.user_vin_list_tag), new HashSet<String>())) {
                    Log.i("SETTINGS", vinNum + "");
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
