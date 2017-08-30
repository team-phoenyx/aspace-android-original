package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aspace.aspace.chromedatamodels.VINDecoder;
import com.securepreferences.SecurePreferences;

/**
 * Created by Zula on 7/24/17.
 */

public class AddVehicleDialogFragment extends DialogFragment {
    private EditText vinNumberEditText;
    private EditText addCarNameEditText;
    private String userID;
    private String userAccessToken;
    private String userPhoneNumber;
    private static int STANDARD_VIN_LENGTH = 17;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle extras = getArguments();
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_vehicle_dialog, null);
        dialogView.requestFocus();

        vinNumberEditText = (EditText) dialogView.findViewById(R.id.add_vin_edittext);
        addCarNameEditText = (EditText) dialogView.findViewById(R.id.add_car_name_edittext);
        builder.setView(dialogView).setCancelable(false);
        //final Set<String> userVINList = ((SettingsActivity)getActivity()).getUserVINList();
        SharedPreferences securePreferences = new SecurePreferences(getActivity());

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // positive button on click function is handled in onStart instead.
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do stuff in here to update list and server with new vehicle
                /*
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
                */
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // the dialog will only close if the user either cancels or enters a valid VIN.
                    String inputtedVIN = vinNumberEditText.getText().toString();
                    if (inputtedVIN.length() == STANDARD_VIN_LENGTH) {
                        String customCarName = addCarNameEditText.getText().toString();
                        VINDecoder vinDecoder = new VINDecoder(getActivity(), getActivity(), userPhoneNumber, userAccessToken, userID);
                        vinDecoder.execute(inputtedVIN, customCarName);
                        dismiss();
                    } else {
                        // placeholder
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Please make sure your VIN is 17 characters long!", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
