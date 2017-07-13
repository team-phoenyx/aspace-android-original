package com.aspace.aspace.tutorialfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.aspace.aspace.R;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialCarFragment extends Fragment {

    Button helpButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_car, container, false);

        helpButton = (Button) viewGroup.findViewById(R.id.help_button);

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(getActivity());
                helpDialogBuilder.setTitle("Find your VIN")
                        .setMessage("There are a couple ways to find your VIN. It can be found in front of the driver-side dash, inside the driver-side doorpost (where the door closes), or inside the hood in front of the engine. It can also be found in your car's user manual.")
                        .setCancelable(true)
                        .create()
                        .show();
            }
        });

        return viewGroup;
    }
}
