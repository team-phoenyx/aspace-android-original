package com.aspace.aspace.chromedatamodels;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.aspace.aspace.AspaceRetrofitService;
import com.aspace.aspace.R;
import com.aspace.aspace.SettingsActivity;
import com.aspace.aspace.retrofitmodels.ResponseCode;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Zula on 8/19/17.
 */

public class VINDecoder extends AsyncTask<String, Void, Void> {
    private static String URL = "http://services.chromedata.com/Description/7b?wsdl";
    private static String TARGET_NAMESPACE ="urn:description7b.services.chrome.com";
    private static String ACCOUNT_NUMBER = "310699";
    private static String SECRET = "4277c6d3e66646b7";
    private static String COUNTRY = "US";
    private static String LANGUAGE ="en";
    private static String STANDARD_CAR_LENGTH = "4.4";
    private ProgressDialog progressDialog;
    private Context context;
    private String userPhone;
    private String userAccessToken;
    private String userId;
    private Activity activity;
    private VehicleInfo vehicleInfo;
    private boolean isSuccessful;
    private boolean hasLengthSpecification;
    private String responseCode;

    public VINDecoder(Activity activity, Context context, String phone, String accessToken, String userId) {
        this.activity = activity;
        this.context = context;
        this.userPhone = phone;
        this.userAccessToken = accessToken;
        this.userId = userId;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Adding your car...");
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        vehicleInfo = new VehicleInfo();
        String generalEnvelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:description7b.services.chrome.com\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <urn:VehicleDescriptionRequest>\n" +
                "         <urn:accountInfo number=\"310699\" secret=\"4277c6d3e66646b7\" country=\"US\" language=\"en\" behalfOf=\"?\"/>\n" +
                "         <urn:vin>%s</urn:vin>\n" +
                "         <urn:switch>ShowExtendedTechnicalSpecifications</urn:switch>\n" +
                "      </urn:VehicleDescriptionRequest>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        String inputtedVIN = params[0];
        String customCarName = params[1]; // empty string if user did not enter custom name
        String envelope = String.format(generalEnvelope, inputtedVIN); // insert VIN into SOAP envelope

        HttpClient httpClient = new DefaultHttpClient();
        HttpParams parameters = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(parameters, 10000);
        HttpConnectionParams.setSoTimeout(parameters, 15000);
        HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), true);

        // POST the envelope
        HttpPost httppost = new HttpPost(URL);
        // add headers
        httppost.setHeader("soapaction", null);
        httppost.setHeader("Content-Type", "text/xml; charset=utf-8");

        String responseString = ""; // declare response string in outer scope to capture and reuse.
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

            responseString = httpClient.execute(httppost, rh).toString(); // capture response string
        } catch (Exception e) {
            e.printStackTrace();
        }

        // close the connection
        httpClient.getConnectionManager().shutdown();
        Log.i("SETTINGS", "RESPONSE: " + responseString);

        try { // parsing response for year/make/model/length
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(responseString));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) { // all parsing currently being done when we first hit a tag
                    //System.out.println("Start tag " + xpp.getName());
                    if (xpp.getName().equalsIgnoreCase("responseStatus")) {
                        responseCode = xpp.getAttributeValue(0); // retrieve the response status
                        // String descriptionCode = xpp.getAttributeValue(1); // not sure what's the difference between responseCode and description but maybe we'll need this
                        isSuccessful = responseCode.equalsIgnoreCase("Successful");
                        if (!isSuccessful) { // if the VIN is not found, break out of parsing immediately
                            return null;
                        }
                    } else if (xpp.getName().equalsIgnoreCase("VehicleDescription")) {
                        // ASSUMPTION: incomplete vehicle descriptions will still have the same amount of attributes
                        // as a complete description, just with empty values.
                        if (xpp.getAttributeCount() > 4) {
                            vehicleInfo.setModelYear(xpp.getAttributeValue(2));
                            vehicleInfo.setMakeName(xpp.getAttributeValue(3));
                            vehicleInfo.setModelName(xpp.getAttributeValue(4));
                            Log.i("Vehicle", xpp.getAttributeName(2) + ": " + xpp.getAttributeValue(2) + " " +
                                    xpp.getAttributeName(3) + ": " + xpp.getAttributeValue(3) + " " +
                                    xpp.getAttributeName(4) + ": " + xpp.getAttributeValue(4));
                        } // maybe move isSuccessful check to here? treat no year/make/model the same?
                    } else if (xpp.getName().equalsIgnoreCase("technicalSpecification")) {
                        String tagName = ""; // prime loop
                        while (!tagName.equalsIgnoreCase("titleId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                tagName = xpp.getName();
                            }
                        }
                        // get length specification ID and reset primer
                        String titleId = xpp.nextText();
                        tagName = "";
                        while (!tagName.equalsIgnoreCase("value")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                tagName = xpp.getName();
                            }
                        }
                        String titleIdValue = xpp.getAttributeValue(0);
                        // 302, 303, and 304 are the only length spec IDs applicable.
                        if (titleId.equalsIgnoreCase("302")) {
                            hasLengthSpecification = true;
                            Log.i("Vehicle", titleId + ": Length, Overall w/o rear bumper (inches): " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, inchesToMeters(titleIdValue));
                        } else if (titleId.equalsIgnoreCase("303")) {
                            hasLengthSpecification = true;
                            Log.i("Vehicle", titleId + ": Length, Overall w/ rear bumper (inches):  " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, inchesToMeters(titleIdValue));
                        } else if (titleId.equalsIgnoreCase("304")) {
                            hasLengthSpecification = true;
                            Log.i("Vehicle", titleId + ": Length, Overall (inches): " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, inchesToMeters(titleIdValue));
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    //System.out.println("End tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    //System.out.println("Text " + xpp.getText());
                }
                eventType = xpp.next();
            }
            System.out.println("End document");
        } catch (XmlPullParserException | IOException | IllegalArgumentException e) {
            e.printStackTrace();

        }
        Log.i("Vehicle", "VEHICLE INFO: " + vehicleInfo.toString());

        // remove this once conditional response is handled completely, placeholder.
        if (!hasLengthSpecification) { // if no length was found, use a standard car length
            vehicleInfo.addLengthSpecification("standard", STANDARD_CAR_LENGTH);
        }

        String vehicleLength = vehicleInfo.getLengthSpecifications().values().iterator().next();
        Retrofit retrofit = new Retrofit.Builder().baseUrl(this.context.getString(R.string.aspace_base_url_api)).addConverterFactory(GsonConverterFactory.create()).build();
        AspaceRetrofitService aspaceRetrofitService = retrofit.create(AspaceRetrofitService.class);

        if (customCarName == null || customCarName.isEmpty()) {
            // if user did not enter custom car name, set name as "{year} {make} {model}" (e.g 2005 Toyota Camry)
            aspaceRetrofitService.addCar(this.userPhone, this.userAccessToken, this.userId,
                    vehicleInfo.getModelYear() + " " + vehicleInfo.getMakeName() + " " + vehicleInfo.getModelName(),
                    inputtedVIN, vehicleInfo.getMakeName(), vehicleInfo.getModelName(), vehicleInfo.getModelYear(),
                    vehicleLength).enqueue(new Callback<ResponseCode>() {
                @Override
                public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                    ((SettingsActivity)activity).getVehicleListAdapter().updateList();
                }

                @Override
                public void onFailure(Call<ResponseCode> call, Throwable t) {
                    Log.i("Vehicle", t.toString());
                }
            });
        } else {
            aspaceRetrofitService.addCar(this.userPhone, this.userAccessToken, this.userId, customCarName,
                    inputtedVIN, vehicleInfo.getMakeName(), vehicleInfo.getModelName(), vehicleInfo.getModelYear(),
                    vehicleLength).enqueue(new Callback<ResponseCode>() {
                @Override
                public void onResponse(Call<ResponseCode> call, Response<ResponseCode> response) {
                    ((SettingsActivity)activity).getVehicleListAdapter().updateList();
                }

                @Override
                public void onFailure(Call<ResponseCode> call, Throwable t) {
                    Log.i("Vehicle", t.toString());
                }
            });
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // TODO: TRIGGER YEAR MAKE MODEL DECODER IF NEEDED
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (!isSuccessful) { // remove this branch once unsuccessful/conditional responses are handled, placeholder.
            // move this stuff to unsuccessful branch
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Invalid VIN: Vehicle Not Found");
            builder.setMessage("We're sorry, you're vehicle was not found. Please try again with a different VIN.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        } else if (responseCode.equalsIgnoreCase("Unsuccessful")) {
            // TODO: execute YearMakeModel decoder here via some form of dialog UI using activity reference.
        } else if (vehicleInfo.isIncomplete()) { // if it's missing vital information year/make/model/length
            // maybe fill in some of the fields in year make model with what is returned from vin decode
            if (vehicleInfo.getModelYear() == null) {
                // do stuff here if year is missing
            }

            if (vehicleInfo.getMakeName() == null) {
                // do stuff here if make is missing
            }

            if (vehicleInfo.getModelName() == null) {
                // do stuff here if model is missing
            }
        } else if (vehicleInfo.getLengthSpecifications().isEmpty()) {
            // TODO:
            // open up prompt here to let the user enter length in some form themselves.
            // pass in bundle containing all of the vehicle info to fragment
            // inside length dialog, add in the lengtht to update car.
        }
    }

    // converts the given inches into meters, returns meter conversion as a STRING.
    private String inchesToMeters(String inches) {
        double in = Double.valueOf(inches);
        return in * 0.0254 + "";
    }
}
