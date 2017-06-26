package me.parcare.parcare;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Get Started");

        final EditText phoneNumberEditText = (EditText) findViewById(R.id.phone_number_edittext);
        final EditText CCEditText = (EditText) findViewById(R.id.country_code_edittext);
        final Button nextButton = (Button) findViewById(R.id.next_button);

        phoneNumberEditText.setText(getSharedPreferences("me.parcare.parcare", MODE_PRIVATE).getString(getString(R.string.sp_user_phone_number_tag), ""));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rawPhoneInput = phoneNumberEditText.getText().toString();
                String rawCCInput = CCEditText.getText().toString();

                if (!isInteger(rawCCInput) || !isInteger(rawPhoneInput) || rawCCInput.length() < 1 || rawPhoneInput.length() < 5) {
                    Snackbar.make(findViewById(android.R.id.content), "Invalid inputs", Snackbar.LENGTH_LONG).show();
                } else {
                    //TODO request to server; if request is successful, continue code below

                    nextButton.setText("Resend");
                    nextButton.setEnabled(false);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            nextButton.setEnabled(true); //TODO throws exception
                        }
                    }, 15000);

                    AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    verifyDialogBuilder.setView(inflater.inflate(R.layout.verify_dialog, null)).setTitle("Enter PIN number");
                    verifyDialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO prevent this from closing dialog, start a progresscircle and call API; if login successful, start createprofileactivity or mainactivity (if returning user)
                        }
                    });

                    verifyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    verifyDialogBuilder.create().show();
                }
            }
        });
    }

    public static boolean isInteger(String s) {
        try {
            if (Integer.parseInt(s) >= 0) return true;
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return false;
    }
}
