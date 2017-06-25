package me.parcare.parcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

    SharedPreferences sharedPreferences;
    boolean editImage = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.profile_layout, null);

        builder.setView(dialogView).setCancelable(false);

        ImageView profilePictureImageView = (ImageView) dialogView.findViewById(R.id.profile_pic_imageview);
        final EditText nameEditText = (EditText) dialogView.findViewById(R.id.name_edittext);
        final EditText homeAddressEditText = (EditText) dialogView.findViewById(R.id.home_address_edittext);
        final EditText workAddressEditText = (EditText) dialogView.findViewById(R.id.work_address_edittext);
        final TextView enterNameTextView = (TextView) dialogView.findViewById(R.id.enter_name_label);
        final FloatingActionButton editFAB = (FloatingActionButton) dialogView.findViewById(R.id.edit_fab);

        nameEditText.setText(sharedPreferences.getString("user_name", "Your Name"));
        nameEditText.setText(sharedPreferences.getString("home_address", ""));
        nameEditText.setText(sharedPreferences.getString("work_address", ""));
        //TODO GET request to get profile image URL, retrieve image from image server

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editImage) {
                    //TODO open up activity to choose an image
                } else {
                    //TODO open up image full res full screen
                }
            }
        });

        editFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameEditText.setEnabled(true);
                homeAddressEditText.setEnabled(true);
                workAddressEditText.setEnabled(true);
                editImage = true;
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nameEditText.setEnabled(false);
                homeAddressEditText.setEnabled(false);
                workAddressEditText.setEnabled(false);

                if (nameEditText.getText().toString().equals("")) {
                    enterNameTextView.setVisibility(View.VISIBLE);
                } else {
                    sharedPreferences = getActivity().getSharedPreferences("me.parcare.parcare", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user_name", nameEditText.getText().toString());
                    editor.putString("user_home_address", homeAddressEditText.getText().toString());
                    editor.putString("user_work_address", workAddressEditText.getText().toString());
                    editor.apply();

                    //TODO POST request to server, update profile and image
                }


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }
}
