package com.aspace.aspace.chromedatamodels;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;

/**
 * Created by Zula on 8/11/17.
 */

public class AccountInfo implements KvmSerializable {
    public String number;
    public String secret;
    public String country;
    public String language;

    public AccountInfo() {

    }

    public AccountInfo(String number, String secret, String country, String language) {
        this.number = number;
        this.secret = secret;
        this.country = country;
        this.language = language;
    }

    @Override
    public Object getProperty(int index) {
        if (index == 0) {
            return this.number;
        } else if (index == 1) {
            return this.secret;
        } else if (index == 2) {
            return this.country;
        } else if (index == 3) {
            return this.language;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 4;
    }

    @Override
    public void setProperty(int index, Object value) {
        switch (index) {
            case 0:
                this.number = value.toString();
                break;
            case 1:
                this.secret = value.toString();
                break;
            case 2:
                this.country = value.toString();
                break;
            case 3:
                this.language = value.toString();
                break;
            default:break;
        }
    }

    @Override
    public void getPropertyInfo(int index, Hashtable properties, PropertyInfo info) {
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
            default:break;
        }
    }
}
