package me.parcare.parcare;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Created by Terrance on 6/24/2017.
 */

public class ProfileDialogFragment extends DialogFragment {

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

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nameEditText.setEnabled(false);
                homeAddressEditText.setEnabled(false);
                workAddressEditText.setEnabled(false);
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
