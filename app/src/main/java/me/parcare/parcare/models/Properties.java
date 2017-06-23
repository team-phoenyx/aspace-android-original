
package me.parcare.parcare.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("wikidata")
    @Expose
    private String wikidata;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("tel")
    @Expose
    private String tel;
    @SerializedName("landmark")
    @Expose
    private Boolean landmark;
    @SerializedName("maki")
    @Expose
    private String maki;

    public String getWikidata() {
        return wikidata;
    }

    public void setWikidata(String wikidata) {
        this.wikidata = wikidata;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Boolean getLandmark() {
        return landmark;
    }

    public void setLandmark(Boolean landmark) {
        this.landmark = landmark;
    }

    public String getMaki() {
        return maki;
    }

    public void setMaki(String maki) {
        this.maki = maki;
    }

}
