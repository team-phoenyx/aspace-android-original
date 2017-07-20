package com.aspace.aspace;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terrance on 7/12/2017.
 */

public class DirectionsFragment extends Fragment implements GestureDetector.OnGestureListener {

    ListView directionsListView;
    TextView navDurationTextView, navDistanceTextView, navSpotsTextView;
    ArrayList<String> instructionsList, distancesList, iconNamesList;
    List<NavigationInstruction> instructions;
    String navTotalTimeLeft, navTotalDistanceLeft, navTotalSpots;
    ConstraintLayout infoFooter;
    int currentStep;
    GestureDetector gestureDetector;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_directions, container, false);

        gestureDetector = new GestureDetector(getActivity(), this);

        directionsListView = (ListView) viewGroup.findViewById(R.id.directions_listview);
        navDurationTextView = (TextView) viewGroup.findViewById(R.id.nav_info_duration_label);
        navDistanceTextView = (TextView) viewGroup.findViewById(R.id.nav_info_distance_label);
        navSpotsTextView = (TextView) viewGroup.findViewById(R.id.nav_info_spots_label);
        infoFooter = (ConstraintLayout) viewGroup.findViewById(R.id.nav_info_footer);

        Bundle extras = getArguments();

        instructionsList = extras.getStringArrayList("instructions");
        distancesList = extras.getStringArrayList("distances");
        iconNamesList = extras.getStringArrayList("icon_names");

        navTotalTimeLeft = extras.getString("total_time_left");
        navTotalDistanceLeft = extras.getString("total_distance_left");
        navTotalSpots = extras.getString("total_spots");

        currentStep = extras.getInt("current_step");

        navDurationTextView.setText(navTotalTimeLeft);
        navDistanceTextView.setText(navTotalDistanceLeft);
        navSpotsTextView.setText(navTotalSpots);

        instructions = new ArrayList<>();

        for (int i = 0; i < instructionsList.size(); i++) {
            instructions.add(i, new NavigationInstruction(instructionsList.get(i), distancesList.get(i), iconNamesList.get(i)));
        }

        DirectionsAdapter adapter = new DirectionsAdapter(instructions, getActivity(), currentStep);

        directionsListView.setAdapter(adapter);

        infoFooter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        return viewGroup;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.i("GESTUREDETECTION", "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.i("GESTUREDETECTION", "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i("GESTUREDETECTION", "onSingleTap");
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.i("GESTUREDETECTION", "onScroll");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.i("GESTUREDETECTION", "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
        if (finish.getY() < (start.getY() - 400) && velocityY < -2000) {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.swipe_down, R.anim.swipe_up).remove(this).commit();
        }
        return true;
    }
}
