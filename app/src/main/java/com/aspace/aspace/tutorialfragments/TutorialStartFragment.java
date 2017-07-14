package com.aspace.aspace.tutorialfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.aspace.aspace.R;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialStartFragment extends Fragment {

    TextView infoTextView;
    EditText vinEditText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_getstarted, container, false);

        infoTextView = (TextView) viewGroup.findViewById(R.id.tutorial_car_info_label);
        vinEditText = (EditText) viewGroup.findViewById(R.id.vin_edittext);

        vinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) infoTextView.setText("This can be found in front of your dashboard,\nor in the driver-side doorpost.");
                else if (s.length() < 17) infoTextView.setText("Keep typing... no car found yet");
                if (s.length() == 17) infoTextView.setText(getCarFromVin(vinEditText.getText().toString()));
                if (s.length() > 17) infoTextView.setText("Woah! Thats too many characters,\nVINs are 17 digits long.");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return viewGroup;
    }

    private String getCarFromVin(String vin) {
        return null;
    }
}
