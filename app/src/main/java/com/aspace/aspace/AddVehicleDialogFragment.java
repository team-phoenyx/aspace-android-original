package com.aspace.aspace;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.aspace.aspace.chromedatamodels.AccountInfo;
import com.aspace.aspace.chromedatamodels.BaseRequest;
import com.aspace.aspace.chromedatamodels.VINDecoder;
import com.securepreferences.SecurePreferences;
import com.squareup.okhttp.OkHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zula on 7/24/17.
 */

public class AddVehicleDialogFragment extends DialogFragment {
    private EditText vinNumberEditText;
    private String userID;
    private String userAccessToken;
    private String userPhoneNumber;

    private static String URL = "http://services.chromedata.com/Description/7b?wsdl";
    private static String TARGET_NAMESPACE ="urn:description7b.services.chrome.com";
    private static String ACCOUNT_NUMBER = "310699";
    private static String SECRET = "4277c6d3e66646b7";
    private static String COUNTRY = "US";
    private static String LANGUAGE ="en";
    private static String METHOD_NAME = "describeVehicle";
    private static String getVersionInfoEnvelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:description7b.services.chrome.com\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <urn:VersionInfoRequest>\n" +
            "         <urn:accountInfo number=\"310699\" secret=\"4277c6d3e66646b7\" country=\"US\" language=\"en\" behalfOf=\"?\"/>\n" +
            "      </urn:VersionInfoRequest>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
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
        builder.setView(dialogView).setCancelable(false);
        //final Set<String> userVINList = ((SettingsActivity)getActivity()).getUserVINList();
        SharedPreferences securePreferences = new SecurePreferences(getActivity());
        /*
        if (securePreferences.contains(getString(R.string.user_vin_list_tag))) {

        } else {

        } */

        /* KSOAP2 ATTEMPT, BROKEN
        new Thread(new Runnable() {
            @Override
            public void run() {
                String URL = "http://services.chromedata.com/Description/7b?wsdl";
                String TARGET_NAMESPACE ="urn:description7b.services.chrome.com";
                String ACCOUNT_NUMBER = "310699";
                String SECRET = "4277c6d3e66646b7";
                String COUNTRY = "US";
                String LANGUAGE ="en";

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.dotNet = true;
                envelope.implicitTypes = true;

                SoapObject accountSO = new SoapObject();
                accountSO.addProperty("number", ACCOUNT_NUMBER);
                accountSO.addProperty("secret", SECRET);
                accountSO.addProperty("country", COUNTRY);
                accountSO.addProperty("language", LANGUAGE);

                BaseRequest versionInfoRequest = new BaseRequest(accountSO);

                SoapObject request = new SoapObject(TARGET_NAMESPACE, "getVersionInfo");
                envelope.addMapping(TARGET_NAMESPACE, "VersionInfoRequest", new BaseRequest().getClass());
                request.addProperty("VersionInfoRequest", versionInfoRequest);
                envelope.setOutputSoapObject(request);
                HttpTransportSE httpTransport = new HttpTransportSE(URL);
                try {
                    httpTransport.call(null, envelope);
                    SoapObject response = (SoapObject) envelope.getResponse();
                    Log.i("SETTINGS", "Response:" + response.toString());
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
        if(d != null) {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean suitableVIN = false;
                    String inputtedVIN = vinNumberEditText.getText().toString();
                    if (inputtedVIN.length() == STANDARD_VIN_LENGTH) {
                        suitableVIN = true;
                    }
                    if (suitableVIN) {
                        VINDecoder vinDecoder = new VINDecoder(getActivity(), getActivity(), userPhoneNumber, userAccessToken, userID);
                        vinDecoder.execute(inputtedVIN);
                        try {
                            //String vehicleName = vinDecoder.get(); // halts thread, maybe just update inside vindecoder, progress dialog does not show
                            // TODO: add vehicle name to car list or modify to return car instead of string
                            //Log.i("Vehicle", "VEHICLE NAME" + vehicleName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dismiss();
                    } else {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Please make sure your VIN is 17 characters long!", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
