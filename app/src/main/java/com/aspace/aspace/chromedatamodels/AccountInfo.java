package com.aspace.aspace.chromedatamodels;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.Hashtable;

/**
 * Created by Zula on 8/11/17.
 */

public class AccountInfo implements KvmSerializable {
    public String number;
    public String secret;
    public String country;
    public String language;
    public String behalfOf;

    public AccountInfo(){}

    public AccountInfo(SoapObject soapObject) {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("number")) {
            Object obj = soapObject.getProperty("number");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j =(SoapPrimitive) obj;
                number = j.toString();
            } else if (obj!= null && obj instanceof String){
                number = (String) obj;
            }
        }
        if (soapObject.hasProperty("secret")) {
            Object obj = soapObject.getProperty("secret");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j =(SoapPrimitive) obj;
                secret = j.toString();
            } else if (obj!= null && obj instanceof String){
                secret = (String) obj;
            }
        }
        if (soapObject.hasProperty("country")) {
            Object obj = soapObject.getProperty("country");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j =(SoapPrimitive) obj;
                country = j.toString();
            } else if (obj!= null && obj instanceof String){
                country = (String) obj;
            }
        }
        if (soapObject.hasProperty("language")) {
            Object obj = soapObject.getProperty("language");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j =(SoapPrimitive) obj;
                language = j.toString();
            } else if (obj != null && obj instanceof String){
                language = (String) obj;
            }
        }
        if (soapObject.hasProperty("behalfOf")) {
            Object obj = soapObject.getProperty("behalfOf");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)) {
                SoapPrimitive j =(SoapPrimitive) obj;
                behalfOf = j.toString();
            } else if (obj != null && obj instanceof String){
                behalfOf = (String) obj;
            }
        }
    }

    @Override
    public Object getProperty(int arg0) {
        switch (arg0) {
            case 0:
                return number;
            case 1:
                return secret;
            case 2:
                return country;
            case 3:
                return language;
            case 4:
                return behalfOf;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 5;
    }

    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch (index) {
            case 0:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "number";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "secret";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "country";
                break;
            case 3:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "language";
                break;
            case 4:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "behalfOf";
                break;
        }
    }

    @Override
    public void setProperty(int arg0, Object arg1) {
    }

}
