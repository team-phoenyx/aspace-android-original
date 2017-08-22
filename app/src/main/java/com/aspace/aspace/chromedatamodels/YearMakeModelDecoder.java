package com.aspace.aspace.chromedatamodels;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by Zula on 8/20/17.
 */

public class YearMakeModelDecoder extends AsyncTask<String, Void, Void> {
    private static String URL = "http://services.chromedata.com/Description/7b?wsdl";
    private static String TARGET_NAMESPACE ="urn:description7b.services.chrome.com";
    private static String ACCOUNT_NUMBER = "310699";
    private static String SECRET = "4277c6d3e66646b7";
    private static String COUNTRY = "US";
    private static String LANGUAGE ="en";
    private ProgressDialog progressDialog;
    private Context context;

    public YearMakeModelDecoder(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Adding your car...");
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        VehicleInfo vehicleInfo = new VehicleInfo();
        String generalEnvelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:description7b.services.chrome.com\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <urn:VehicleDescriptionRequest>\n" +
                "         <urn:accountInfo number=\"310699\" secret=\"4277c6d3e66646b7\" country=\"US\" language=\"en\" behalfOf=\"?\"/>\n" +
                "         <urn:modelYear>%1$d</urn:modelYear>\n" +
                "         <urn:makeName>%1$s</urn:makeName>\n" +
                "         <urn:modelName>%2$s</urn:modelName>\n" +
                "         <urn:switch>ShowExtendedTechnicalSpecifications</urn:switch>\n" +
                "      </urn:VehicleDescriptionRequest>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        String envelope = String.format(generalEnvelope, Integer.valueOf(params[0]), params[1], params[2]);

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

        } catch (Exception e) {
            Log.v("exception", e.toString());
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
                } else if (eventType == XmlPullParser.START_TAG) {
                    //System.out.println("Start tag " + xpp.getName());
                    if (xpp.getName().equalsIgnoreCase("VehicleDescription")) {
                        vehicleInfo.setModelYear(xpp.getAttributeValue(2));
                        vehicleInfo.setMakeName(xpp.getAttributeValue(3));
                        vehicleInfo.setModelName(xpp.getAttributeValue(4));
                        Log.i("Vehicle", xpp.getAttributeName(2) + ": " + xpp.getAttributeValue(2) + " " +
                                xpp.getAttributeName(3) + ": " + xpp.getAttributeValue(3) + " " +
                                xpp.getAttributeName(4) + ": " + xpp.getAttributeValue(4));
                    } else if (xpp.getName().equalsIgnoreCase("technicalSpecification")) {
                        String tagName = "";
                        while (!tagName.equalsIgnoreCase("titleId")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                tagName = xpp.getName();
                            }
                        }
                        String titleId = xpp.nextText();
                        tagName = "";
                        while (!tagName.equalsIgnoreCase("value")) {
                            eventType = xpp.next();
                            if (eventType == XmlPullParser.START_TAG) {
                                tagName = xpp.getName();
                            }
                        }
                        String titleIdValue = xpp.getAttributeValue(0);
                        if (titleId.equalsIgnoreCase("302")) {
                            Log.i("Vehicle", titleId + ": Length, Overall w/o rear bumper (inches): " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, titleIdValue);
                        } else if (titleId.equalsIgnoreCase("303")) {
                            Log.i("Vehicle", titleId + ": Length, Overall w/ rear bumper (inches):  " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, titleIdValue);
                        } else if (titleId.equalsIgnoreCase("304")) {
                            Log.i("Vehicle", titleId + ": Length, Overall (inches): " + titleIdValue);
                            vehicleInfo.addLengthSpecification(titleId, titleIdValue);
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
        } catch (XmlPullParserException | IOException e) {

        }
        //TODO: Update server w/ VIN here, call adapter update method
        Log.i("Vehicle", "VEHICLE INFO: " + vehicleInfo.toString());
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
