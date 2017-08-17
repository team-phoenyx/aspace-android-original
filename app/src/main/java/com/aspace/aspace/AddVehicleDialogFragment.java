package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.securepreferences.SecurePreferences;

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

        vinNumberEditText = (EditText) dialogView.findViewById(R.id.add_vin_edittext);
        builder.setView(dialogView).setCancelable(false);
        //final Set<String> userVINList = ((SettingsActivity)getActivity()).getUserVINList();
        SharedPreferences securePreferences = new SecurePreferences(getActivity());
        /*
        if (securePreferences.contains(getString(R.string.user_vin_list_tag))) {

        } else {

        } */
        // description7b.services.chrome.com might be namespace instead
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                SoapObject request = new SoapObject(TARGET_NAMESPACE, "getVersionInfo");

                PropertyInfo numberProp = new PropertyInfo();
                numberProp.setName("number");
                numberProp.setValue(ACCOUNT_NUMBER);
                numberProp.setType(PropertyInfo.STRING_CLASS);
                request.addProperty(numberProp);

                PropertyInfo secretProp = new PropertyInfo();
                secretProp.setName("secret");
                secretProp.setValue(SECRET);
                secretProp.setType(PropertyInfo.STRING_CLASS);
                request.addProperty(secretProp);

                PropertyInfo countryProp = new PropertyInfo();
                countryProp.setName("country");
                countryProp.setValue(COUNTRY);
                countryProp.setType(PropertyInfo.STRING_CLASS);
                request.addProperty(countryProp);

                PropertyInfo languageProp = new PropertyInfo();
                languageProp.setName("language");
                languageProp.setValue(LANGUAGE);
                languageProp.setType(PropertyInfo.STRING_CLASS);
                request.addProperty(languageProp);

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.setOutputSoapObject(request);
                HttpTransportSE httpTransport = new HttpTransportSE(URL);
                try {
                    httpTransport.call(null, envelope);
                    SoapObject response = (SoapObject) envelope.getResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        */



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
}
