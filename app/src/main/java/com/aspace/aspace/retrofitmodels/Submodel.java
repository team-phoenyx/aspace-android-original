
package com.aspace.aspace.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Submodel {

    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("modelName")
    @Expose
    private String modelName;
    @SerializedName("niceName")
    @Expose
    private String niceName;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

}
