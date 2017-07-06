package me.parcare.parcare.retrofitmodels;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Terrance on 6/22/2017.
 */

public class Suggestion implements SearchSuggestion {
    String body;

    public Suggestion(String body) {
        this.body = body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
