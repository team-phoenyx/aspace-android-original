package me.parcare.parcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

    SharedPreferences sharedPreferences;
    boolean editImage = false;
    ImageView profilePictureImageView;
    EditText nameEditText, homeAddressEditText, workAddressEditText;
    TextView enterNameTextView;
    FloatingActionButton editFAB;

    private static final String SP_USER_NAME_TAG = "user_name";
    private static final String SP_USER_HOME_ADDRESS_TAG = "user_home_address";
    private static final String SP_USER_WORK_ADDRESS_TAG = "user_work_address";
    private static final int PICK_IMAGE_REQUEST_CALLBACK = 1;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.profile_layout, null);

        builder.setView(dialogView).setCancelable(false);

        sharedPreferences = getActivity().getSharedPreferences("me.parcare.parcare", Context.MODE_PRIVATE);

        profilePictureImageView = (ImageView) dialogView.findViewById(R.id.profile_pic_imageview);
        nameEditText = (EditText) dialogView.findViewById(R.id.name_edittext);
        homeAddressEditText = (EditText) dialogView.findViewById(R.id.home_address_edittext);
        workAddressEditText = (EditText) dialogView.findViewById(R.id.work_address_edittext);
        enterNameTextView = (TextView) dialogView.findViewById(R.id.enter_name_label);
        editFAB = (FloatingActionButton) dialogView.findViewById(R.id.edit_fab);

        nameEditText.setText(sharedPreferences.getString(SP_USER_NAME_TAG, "Your Name"));
        homeAddressEditText.setText(sharedPreferences.getString(SP_USER_HOME_ADDRESS_TAG, ""));
        workAddressEditText.setText(sharedPreferences.getString(SP_USER_WORK_ADDRESS_TAG, ""));
        //TODO Retrieve image from internal storage

        profilePictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editImage) {
                    Intent chooseImageIntent = new Intent();
                    chooseImageIntent.setType("image/*");
                    chooseImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(chooseImageIntent, "Select a picture"), PICK_IMAGE_REQUEST_CALLBACK);
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
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(SP_USER_NAME_TAG, nameEditText.getText().toString());
                    editor.putString(SP_USER_HOME_ADDRESS_TAG, homeAddressEditText.getText().toString());
                    editor.putString(SP_USER_WORK_ADDRESS_TAG, workAddressEditText.getText().toString());
                    editor.commit();

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_IMAGE_REQUEST_CALLBACK:
                Uri imageURI = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                    Log.d("IMAGEPICKER", String.valueOf(bitmap));

                    profilePictureImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    nameEditText.setEnabled(false);
                    homeAddressEditText.setEnabled(false);
                    workAddressEditText.setEnabled(false);

                    if (nameEditText.getText().toString().equals("")) {
                        enterNameTextView.setVisibility(View.VISIBLE);
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_name", nameEditText.getText().toString());
                        editor.putString("user_home_address", homeAddressEditText.getText().toString());
                        editor.putString("user_work_address", workAddressEditText.getText().toString());
                        editor.apply();

                        //TODO POST request to server, update profile and image

                        d.dismiss();
                    }
                }
            });
        }
    }
}
