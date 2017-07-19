package com.aspace.aspace;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Terrance on 7/12/2017.
 */

public class DirectionsFragment extends Fragment {

    ListView directionsListView;
    TextView navDurationTextView, navDistanceTextView, navSpotsTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_directions, container, false);

        directionsListView = (ListView) viewGroup.findViewById(R.id.directions_listview);
        navDurationTextView = (TextView) viewGroup.findViewById(R.id.nav_info_duration_label);
        navDistanceTextView = (TextView) viewGroup.findViewById(R.id.nav_info_distance_label);
        navSpotsTextView = (TextView) viewGroup.findViewById(R.id.nav_info_spots_label);

        return viewGroup;
    }
}
