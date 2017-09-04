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
import com.aspace.aspace.retrofitmodels.ResponseCode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Zula on 9/3/17.
 */

public class AddVehicleLengthDialogFragment extends DialogFragment {
    private EditText carLengthEditText;
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
        // retrieve args from bundle needed for add car to profile callback
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        inputtedVIN = extras.getString("inputtedVIN");
        customCarName = extras.getString("customCarName");
        carYear = extras.getString("carYear");
        carMake = extras.getString("carMake");
        carModel = extras.getString("carModel");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_vehicle_length_dialog, null);
        carLengthEditText = (EditText) dialogView.findViewById(R.id.enter_length_edittext);
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
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // the dialog will only close if the user either cancels or enters a valid VIN.
                    String carLength = carLengthEditText.getText().toString();
                    if (!carLength.isEmpty()) {
                        Retrofit retrofit = new Retrofit.Builder().baseUrl(getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
                        AspaceRetrofitService aspaceRetrofitService = retrofit.create(AspaceRetrofitService.class);

                        aspaceRetrofitService.addCar(userPhoneNumber, userAccessToken, userID, customCarName, inputtedVIN, carMake, carModel, carYear, carLength).enqueue(new Callback<ResponseCode>() {
                            @Override
                            public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                                // handle response
                            }

                            @Override
                            public void onFailure(Call<ResponseCode> call, Throwable t) {
                                // handle failure
                            }
                        });
                        dismiss();
                    } else {
                        // placeholder
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Please enter car length before continuing!", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

