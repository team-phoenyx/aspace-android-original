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

import com.aspace.aspace.chromedatamodels.AccountInfo;
import com.securepreferences.SecurePreferences;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zula on 7/24/17.
 */

public class AddVehicleDialogFragment extends DialogFragment {
    private EditText vinNumberEditText;

    private static String URL = "http://services.chromedata.com/Description/7b?wsdl";
    private static String TARGET_NAMESPACE ="urn:description7b.services.chrome.com";
    private static String ACCOUNT_NUMBER = "310699";
    private static String SECRET = "4277c6d3e66646b7";
    private static String COUNTRY = "US";
    private static String LANGUAGE ="en";

    private static String METHOD_NAME = "describeVehicle";

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
        /*
        if (securePreferences.contains(getString(R.string.user_vin_list_tag))) {

        } else {

        } */

        /* CHROMEDATA CONNECTION ATTEMPT FAILED, UNKNOWN HOST EXCEPTION ******
        new Thread(new Runnable() {
            @Override
            public void run() {
                SoapObject request = new SoapObject(TARGET_NAMESPACE, "accountInfo");
                AccountInfo accountInfo = new AccountInfo(ACCOUNT_NUMBER, SECRET, COUNTRY, LANGUAGE);
                PropertyInfo propertyInfo = new PropertyInfo();
                propertyInfo.setType(accountInfo.getClass());
                propertyInfo.setName("accountInfo");
                propertyInfo.setValue(accountInfo);
                propertyInfo.setType(accountInfo.getClass());
                request.addProperty(propertyInfo);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                envelope.addMapping(TARGET_NAMESPACE, "AccountInfo", new AccountInfo().getClass());
                HttpTransportSE httpTransport = new HttpTransportSE(URL);
                try {
                    httpTransport.call("", envelope);
                    //SoapObject response = (SoapObject) envelope.getResponse();
                    Log.i("SETTINGS", "ASDA" + envelope.getResponse().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start(); */



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
