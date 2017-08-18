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
import com.aspace.aspace.chromedatamodels.BaseRequest;
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


        new Thread(new Runnable() {
            @Override
            public void run() {
                String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:description7b.services.chrome.com\">\n" +
                        "   <soapenv:Header/>\n" +
                        "   <soapenv:Body>\n" +
                        "      <urn:VehicleDescriptionRequest>\n" +
                        "         <urn:accountInfo number=\"310699\" secret=\"4277c6d3e66646b7\" country=\"US\" language=\"en\" behalfOf=\"?\"/>\n" +
                        "         <urn:vin>JTEHT05J542053195</urn:vin>\n" +
                        "         <urn:includeTechnicalSpecificationTitleId>304</urn:includeTechnicalSpecificationTitleId>\n" +
                        "         <!-- Everything below is optional\n" +
                        "         <urn:reducingStyleId>?</urn:reducingStyleId>\n" +
                        "         <urn:reducingAcode>?</urn:reducingAcode>\n" +
                        "         <urn:styleId>?</urn:styleId>\n" +
                        "         <urn:acode>?</urn:acode>\n" +
                        "         <urn:styleName>?</urn:styleName>\n" +
                        "         <urn:trimName>?</urn:trimName>\n" +
                        "         -->\n" +
                        "      </urn:VehicleDescriptionRequest>\n" +
                        "   </soapenv:Body>\n" +
                        "</soapenv:Envelope>";
                HttpClient httpClient = new DefaultHttpClient();
                HttpParams params = httpClient.getParams();
                HttpConnectionParams.setConnectionTimeout(params, 10000);
                HttpConnectionParams.setSoTimeout(params, 15000);
                HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), true);

                // POST the envelope
                HttpPost httppost = new HttpPost(URL);
                // add headers
                httppost.setHeader("soapaction", null);
                httppost.setHeader("Content-Type", "text/xml; charset=utf-8");

                String responseString= "";
                try {

                    // the entity holds the request
                    HttpEntity entity = new StringEntity(envelope);
                    httppost.setEntity(entity);

                    // Response handler
                    ResponseHandler rh=new ResponseHandler() {
                        // invoked when client receives response
                        public String handleResponse(HttpResponse response)
                                throws ClientProtocolException, IOException {

                            // get response entity
                            HttpEntity entity = response.getEntity();

                            // read the response as byte array
                            StringBuffer out = new StringBuffer();
                            byte[] b = EntityUtils.toByteArray(entity);

                            // write the response byte array to a string buffer
                            out.append(new String(b, 0, b.length));
                            return out.toString();
                        }
                    };

                    responseString = httpClient.execute(httppost, rh).toString();

                }
                catch (Exception e) {
                    Log.v("exception", e.toString());
                }

                // close the connection
                httpClient.getConnectionManager().shutdown();
                Log.i("SETTINGS", "RESPONSE: " + responseString);

                try {
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();

                    xpp.setInput(new StringReader(responseString));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_DOCUMENT) {
                            System.out.println("Start document");
                        } else if (eventType == XmlPullParser.START_TAG) {
                            //System.out.println("Start tag " + xpp.getName());
                            if (xpp.getName().equalsIgnoreCase("VehicleDescription")) {
                                Log.i("Vehicle", xpp.getAttributeName(2) + ": " + xpp.getAttributeValue(2) + " " +
                                        xpp.getAttributeName(3) + ": " + xpp.getAttributeValue(3) + " " +
                                        xpp.getAttributeName(4) + ": " + xpp.getAttributeValue(4));
                            } else if (xpp.getName().equalsIgnoreCase("technicalSpecification")) {
                                String tagName = "";
                                while (!tagName.equalsIgnoreCase("value")) { // keep going until we get to length value within tech specs
                                    eventType = xpp.next();
                                    if (eventType == XmlPullParser.START_TAG) {
                                        tagName = xpp.getName();
                                    }
                                }
                                Log.i("Vehicle", "Overall Length (inches): " + xpp.getAttributeValue(0)); // very first attribute is the length
                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            //System.out.println("End tag " + xpp.getName());
                        } else if (eventType == XmlPullParser.TEXT) {
                            //System.out.println("Text " + xpp.getText());
                        }
                        eventType = xpp.next();
                    }
                    System.out.println("End document");
                } catch (XmlPullParserException | IOException e) {

                }
            }
        }).start();

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
}
