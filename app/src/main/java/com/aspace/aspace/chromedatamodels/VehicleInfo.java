package com.aspace.aspace.chromedatamodels;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zula on 8/21/17.
 */

public class VehicleInfo {
    private String modelYear;
    private String makeName;
    private String modelName;
    private Map<String, String> lengthSpecifications;

    public VehicleInfo() {
        this.lengthSpecifications = new HashMap<String, String>();
    }

    public VehicleInfo(String modelYear, String makeName, String modelName) {
        this.modelYear = modelYear;
        this.makeName = makeName;
        this.modelName = modelName;
        this.lengthSpecifications = new HashMap<String, String>();
    }

    public void addLengthSpecification(String titleId, String titleIdValue) {
        this.lengthSpecifications.put(titleId, titleIdValue);
    }

    public void removeLengthSpecification(String titleId) {
        this.lengthSpecifications.remove(titleId);
    }

    public String getModelYear() {
        return this.modelYear;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public String getMakeName() {
        return this.makeName;
    }

    public void setMakeName(String makeName) {
        this.makeName = makeName;
    }

    public String getModelName() {
        return this.modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Map<String, String> getLengthSpecifications() {
        return this.lengthSpecifications;
    }

    @Override
    public String toString() {
        String result = "Model Year: " + this.modelYear + ", Make Name: " + this.makeName + ", Model Name: " + this.modelName;
        if (this.lengthSpecifications.containsKey("302")) {
            result += "| Length, Overall w/o rear bumper: " + this.lengthSpecifications.get("302");
        } else if (this.lengthSpecifications.containsKey("303")) {
            result += "| Length, Overall w/ rear bumper: " + this.lengthSpecifications.get("303");
        } else if (this.lengthSpecifications.containsKey("304")) {
            result += "| Length, Overall: " + this.lengthSpecifications.get("304");
        } else {
            result += "| NO LENGTH";
        }
        return result;
    }
}
