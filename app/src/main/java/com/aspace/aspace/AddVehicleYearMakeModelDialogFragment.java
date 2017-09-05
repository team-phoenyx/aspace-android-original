package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aspace.aspace.chromedatamodels.YearMakeModelDecoder;

/**
 * Created by Zula on 9/1/17.
 */

public class AddVehicleYearMakeModelDialogFragment extends DialogFragment {
    private EditText carYearEditText;
    private EditText carMakeEditText;
    private EditText carModelEditText;
    private String userID;
    private String userAccessToken;
    private String userPhoneNumber;
    private String inputtedVIN;
    private String customCarName;
    private String carYear;
    private String carMake;
    private String carModel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle extras = getArguments();
        // retrieve user values to update server by passing into yearmakemodel decoder in onstart.

        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        inputtedVIN = extras.getString("inputtedVIN");
        customCarName = extras.getString("customCarName");
        carYear = extras.getString("carYear");
        carMake = extras.getString("carMake");
        carModel = extras.getString("carModel");

        // inputtedVIN and customCarName needed to pass into yearmakemodel decoder as mandatory params for updating server.
        inputtedVIN = extras.getString("inputtedVIN");
        customCarName = extras.getString("customCarName");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_vehicle_yearmakemodel_dialog, null);
        carYearEditText = (EditText) dialogView.findViewById(R.id.enter_year_edittext);
        carMakeEditText = (EditText) dialogView.findViewById(R.id.enter_make_edittext);
        carModelEditText = (EditText) dialogView.findViewById(R.id.enter_model_edittext);

        if (!carYear.isEmpty()) {
            carYearEditText.setText(carYear);
        }

        if (!carMake.isEmpty()) {
            carMakeEditText.setText(carMake);
        }

        if (!carModel.isEmpty()) {
            carModelEditText.setText(carModel);
        }

        dialogView.requestFocus();

        builder.setView(dialogView).setCancelable(false);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // positive button on click function is handled in onStart instead.
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
                    String carYear = carYearEditText.getText().toString();
                    String carMake = carMakeEditText.getText().toString();
                    String carModel = carModelEditText.getText().toString();
                    if (!carYear.isEmpty() && !carMake.isEmpty() && !carModel.isEmpty()) {
                        YearMakeModelDecoder yearMakeModelDecoder = new YearMakeModelDecoder(getActivity(), getActivity(), userPhoneNumber, userAccessToken, userID);
                        yearMakeModelDecoder.execute(carYear, carMake, carModel, inputtedVIN, customCarName);
                        dismiss();
                    } else {
                        // placeholder
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Some information is missing!", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

