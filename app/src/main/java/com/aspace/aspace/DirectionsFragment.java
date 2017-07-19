package com.aspace.aspace;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.api.directions.v5.models.StepManeuver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terrance on 7/12/2017.
 */

public class DirectionsFragment extends Fragment {

    ListView directionsListView;
    TextView navDurationTextView, navDistanceTextView, navSpotsTextView;
    ArrayList<String> instructionsList, distancesList, iconNamesList;
    List<NavigationInstruction> instructions;
    String navTotalTimeLeft, navTotalDistanceLeft, navTotalSpots;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_directions, container, false);

        directionsListView = (ListView) viewGroup.findViewById(R.id.directions_listview);
        navDurationTextView = (TextView) viewGroup.findViewById(R.id.nav_info_duration_label);
        navDistanceTextView = (TextView) viewGroup.findViewById(R.id.nav_info_distance_label);
        navSpotsTextView = (TextView) viewGroup.findViewById(R.id.nav_info_spots_label);

        Bundle extras = getArguments();

        instructionsList = extras.getStringArrayList("instructions");
        distancesList = extras.getStringArrayList("distances");
        iconNamesList = extras.getStringArrayList("icon_names");

        navTotalTimeLeft = extras.getString("total_time_left");
        navTotalDistanceLeft = extras.getString("total_distance_left");
        navTotalSpots = extras.getString("total_spots");

        navDurationTextView.setText(navTotalTimeLeft);
        navDistanceTextView.setText(navTotalDistanceLeft);
        navSpotsTextView.setText(navTotalSpots);

        instructions = new ArrayList<>();

        for (int i = 0; i < instructionsList.size(); i++) {
            instructions.add(i, new NavigationInstruction(instructionsList.get(i), distancesList.get(i), iconNamesList.get(i)));
        }

        DirectionsAdapter adapter = new DirectionsAdapter(instructions, getActivity());

        directionsListView.setAdapter(adapter);

        return viewGroup;
    }
}
