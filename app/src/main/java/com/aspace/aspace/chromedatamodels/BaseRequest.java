package com.aspace.aspace.chromedatamodels;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;

import java.util.Hashtable;

import pt.joaocruz04.lib.misc.SOAPSerializable;

/**
 * Created by Zula on 8/17/17.
 */
public class BaseRequest implements KvmSerializable {
    public AccountInfo accountInfo;

    public BaseRequest() {

    }

    public BaseRequest(SoapObject soapObject) {
        if (soapObject == null) {
            return;
        }
        if (soapObject.hasProperty("accountInfo")) {
            SoapObject so = (SoapObject) soapObject.getProperty("accountInfo");
            accountInfo = new AccountInfo(so);
        }
    }

    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return accountInfo;
        }
        return null;
    }

    @Override
    public int getPropertyCount() {
        return 1;
    }

    @Override
    public void setProperty(int index, Object value) {

    }

    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
            case 0:
                info.type = AccountInfo.class;
                info.name = "accountInfo";
                break;
        }
    }

}
