
package com.aspace.aspace.retrofitmodels;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Year {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("styles")
    @Expose
    private List<Style> styles = null;
    @SerializedName("states")
    @Expose
    private List<String> states = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }

}
