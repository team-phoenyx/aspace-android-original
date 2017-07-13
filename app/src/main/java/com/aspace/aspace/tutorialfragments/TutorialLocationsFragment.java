package com.aspace.aspace.tutorialfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.aspace.aspace.PCRetrofitInterface;
import com.aspace.aspace.R;
import com.aspace.aspace.retrofitmodels.Feature;

import java.util.List;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialLocationsFragment extends Fragment {

    String homeLocationID, workLocationID;
    AutoCompleteTextView homeAddressEditText, workAddressEditText;
    TextView errorTextView;
    ArrayAdapter<String> autocompleteAdapter;
    PCRetrofitInterface mapboxService;
    List<Feature> rawSuggestions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_locations, container, false);

        getActivity().set

        return viewGroup;
    }
}
