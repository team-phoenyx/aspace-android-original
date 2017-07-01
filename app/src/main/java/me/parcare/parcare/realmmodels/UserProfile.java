package me.parcare.parcare.realmmodels;

import io.realm.RealmObject;

/**
 * Created by terrance on 6/27/17.
 */

public class UserProfile extends RealmObject {

    private String name;
    private String homeAddress;
    private String workAddress;
    private String homeLocationID;
    private String workLocationID;
    private String profileImageDirectory;

    public UserProfile() {

    }

    public UserProfile(String name, String homeAddress, String workAddress, String homeLocationID, String workLocationID, String profileImageDirectory) {
        this.name = name;
        this.homeAddress = homeAddress;
        this.workAddress = workAddress;
        this.homeLocationID = homeLocationID;
        this.workLocationID = workLocationID;
        this.profileImageDirectory = profileImageDirectory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public String getHomeLocationID() {
        return homeLocationID;
    }

    public void setHomeLocationID(String homeLocationID) {
        this.homeLocationID = homeLocationID;
    }

    public String getWorkLocationID() {
        return workLocationID;
    }

    public void setWorkLocationID(String workLocationID) {
        this.workLocationID = workLocationID;
    }

    public String getProfileImageDirectory() {
        return profileImageDirectory;
    }

    public void setProfileImageDirectory(String profileImageDirectory) {
        this.profileImageDirectory = profileImageDirectory;
    }
}
