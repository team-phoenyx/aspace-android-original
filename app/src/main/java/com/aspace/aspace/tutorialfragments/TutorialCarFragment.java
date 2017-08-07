package com.aspace.aspace.tutorialfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aspace.aspace.AspaceRetrofitService;
import com.aspace.aspace.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialCarFragment extends Fragment {

    Button helpButton;
    TextView infoTextView;
    EditText vinEditText;
    AspaceRetrofitService vinDecodeService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_car, container, false);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.edmunds.com/").addConverterFactory(GsonConverterFactory.create()).build();
        vinDecodeService = retrofit.create(AspaceRetrofitService.class);

        helpButton = (Button) viewGroup.findViewById(R.id.help_button);
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

    private String getCarFromVin(String vin) {
        //vinDecodeService.getCarSpecs(vin, "json,", )
        return null;
    }
}
