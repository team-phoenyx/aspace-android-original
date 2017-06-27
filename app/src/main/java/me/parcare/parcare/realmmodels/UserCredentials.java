package me.parcare.parcare.realmmodels;

import io.realm.RealmObject;

/**
 * Created by Terrance on 6/26/2017.
 */

public class UserCredentials extends RealmObject {

    public UserCredentials() {

    }

    public UserCredentials(String userID, String userAccessToken, String userPhoneNumber) {
        this.userID = userID;
        this.userAccessToken = userAccessToken;
        this.userPhoneNumber = userPhoneNumber;
    }

    private String userID, userAccessToken, userPhoneNumber;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }
}
