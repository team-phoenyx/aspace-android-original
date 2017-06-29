package me.parcare.parcare;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.securepreferences.SecurePreferences;

import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.parcare.parcare.realmmodels.UserCredentials;

public class LoginActivity extends AppCompatActivity {

    EditText phoneNumberEditText, CCEditText;
    Button nextButton;
    ProgressBar phoneProgressCircle;

    String realmEncryptionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        realmEncryptionKey = getIntent().getExtras().getString(getString(R.string.realm_encryption_key_tag), "");

        getSupportActionBar().setTitle("Get Started");

        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_edittext);
        CCEditText = (EditText) findViewById(R.id.country_code_edittext);
        nextButton = (Button) findViewById(R.id.next_button);
        phoneProgressCircle = (ProgressBar) findViewById(R.id.phone_progress_circle);

        phoneProgressCircle.setIndeterminate(true);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivity.this);

                phoneProgressCircle.setVisibility(View.VISIBLE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String rawPhoneInput = phoneNumberEditText.getText().toString();
                        String rawCCInput = CCEditText.getText().toString();

                        if (!isInteger(rawCCInput) || !isInteger(rawPhoneInput) || rawCCInput.length() < 1 || rawPhoneInput.length() < 5) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(findViewById(android.R.id.content), "Invalid inputs", Snackbar.LENGTH_LONG).show();
                                    phoneProgressCircle.setVisibility(View.INVISIBLE);
                                }
                            });

                        } else {
                            //TODO request to server; if request is successful, continue code below

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nextButton.setText("Wait to resend");
                                    nextButton.setEnabled(false);
                                    nextButton.setTextColor(Color.GRAY);
                                }
                            });

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            nextButton.setEnabled(true);
                                            nextButton.setText("Resend");
                                            nextButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                        }
                                    });
                                }
                            }, 15000);

                            final AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                            View verifyDialogView = getLayoutInflater().inflate(R.layout.verify_dialog, null);

                            final EditText pinEditText = (EditText) verifyDialogView.findViewById(R.id.pin_edittext);
                            final ProgressBar loginProgressCircle = (ProgressBar) verifyDialogView.findViewById(R.id.login_progress_circle);

                            loginProgressCircle.setIndeterminate(true);

                            verifyDialogBuilder.setView(verifyDialogView).setTitle("Enter PIN number");
                            verifyDialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //KEEP THIS METHOD (only to show button, behavior defined after verifyDialog.show();
                                }
                            });

                            verifyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });



                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog verifyDialog = verifyDialogBuilder.create();
                                    verifyDialog.show();
                                    phoneProgressCircle.setVisibility(View.INVISIBLE);

                                    verifyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            loginProgressCircle.setVisibility(View.VISIBLE);

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String userPhoneNumber = phoneNumberEditText.getText().toString();
                                                    String inputPIN = pinEditText.getText().toString();

                                                    //TODO use another thread to call the API; if login successful, start createprofileactivity (if profile has empty name) or mainactivity (if returning user);
                                                    // if login unsuccessful, exit dialog and show a snackbar, and hide progresscircle

                                                    //if login is successful, the user is technically signed in already, so save the UserCredentials in Realm

                                                    //TODO Set the id, phone, and accesstoken (these are placeholders)
                                                    String userID = "30";
                                                    String userAccessToken = "test_access_token";


                                                    byte[] key = new byte[64];

                                                    if (realmEncryptionKey.equals("")) {
                                                        //Realm encryption hasn't been set up yet, must generate and store a key
                                                        new SecureRandom().nextBytes(key);

                                                        //base64 encode string and store
                                                        realmEncryptionKey = Base64.encodeToString(key, Base64.DEFAULT);

                                                        SharedPreferences.Editor editor = new SecurePreferences(LoginActivity.this).edit();
                                                        editor.putString(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                                                        editor.apply();
                                                    } else {
                                                        //base64 decode string
                                                        key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);
                                                    }

                                                    Realm.init(LoginActivity.this);

                                                    RealmConfiguration config = new RealmConfiguration.Builder()
                                                            .encryptionKey(key)
                                                            .build();

                                                    Realm realm = Realm.getInstance(config);

                                                    realm.beginTransaction();

                                                    UserCredentials credentials = realm.createObject(UserCredentials.class);

                                                    credentials.setUserID(userID);
                                                    credentials.setUserAccessToken(userAccessToken);
                                                    credentials.setUserPhoneNumber(userPhoneNumber);

                                                    realm.commitTransaction();

                                                    Intent addNameIntent = new Intent(getApplicationContext(), NameActivity.class);
                                                    addNameIntent.putExtra(getString(R.string.user_id_tag), userID);
                                                    addNameIntent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                                                    addNameIntent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);
                                                    addNameIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);

                                                    startActivity(addNameIntent);
                                                }
                                            }).start();


                                        }
                                    });
                                }
                            });


                        }
                    }
                }).start();
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isInteger(String s) {
        long integer = -1;
        try {
            integer = Long.parseLong(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        if (integer < 0) return false;

        return true;
    }
}
