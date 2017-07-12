package com.aspace.aspace;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspace.aspace.realmmodels.UserCredentials;
import com.aspace.aspace.retrofitmodels.RequestPINResponse;
import com.aspace.aspace.retrofitmodels.VerifyPINResponse;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    EditText phoneNumberEditText, CCEditText;
    Button nextButton;
    ProgressBar phoneProgressCircle;
    FloatingActionButton helpFAB;

    PCRetrofitInterface parcareService;

    String realmEncryptionKey;

    public static final String BASE_URL = "http://192.241.224.224:3000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        realmEncryptionKey = getIntent().getExtras().getString(getString(R.string.realm_encryption_key_tag), "");

        // Sets the color of action bar text to white.
        SpannableString loginString = new SpannableString("Login");
        loginString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, loginString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(loginString);

        phoneNumberEditText = (EditText) findViewById(R.id.phone_number_edittext);
        CCEditText = (EditText) findViewById(R.id.country_code_edittext);
        nextButton = (Button) findViewById(R.id.next_button);
        helpFAB = (FloatingActionButton) findViewById(R.id.help_fab);
        phoneProgressCircle = (ProgressBar) findViewById(R.id.phone_progress_circle);

        phoneProgressCircle.setIndeterminate(true);

        helpFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("What's up with the \"1\"?")
                        .setMessage(getString(R.string.country_code_explanation))
                        .setPositiveButton("GOT IT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // No action needed here.
                            }
                        }).create().show();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivity.this);
                helpFAB.setVisibility(View.GONE);

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
                            //Disable the resend button
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nextButton.setText("Wait to resend");
                                    nextButton.setEnabled(false);
                                    nextButton.setTextColor(Color.GRAY);
                                }
                            });

                            //Enable the resend button in 15 seconds
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

                            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                                    .addConverterFactory(GsonConverterFactory.create()).build();

                            parcareService = retrofit.create(PCRetrofitInterface.class);

                            parcareService.requestPIN(rawCCInput + rawPhoneInput).enqueue(new Callback<RequestPINResponse>() {
                                @Override
                                public void onResponse(Call<RequestPINResponse> call, Response<RequestPINResponse> response) {

                                    //TODO put this dialog creation in a method and call in onCreate, and just call .create and .show
                                    //Create the dialog for PIN input
                                    final AlertDialog.Builder verifyDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                                    View verifyDialogView = getLayoutInflater().inflate(R.layout.verify_dialog, null);

                                    final EditText pinEditText = (EditText) verifyDialogView.findViewById(R.id.pin_edittext);
                                    final ProgressBar loginProgressCircle = (ProgressBar) verifyDialogView.findViewById(R.id.login_progress_circle);
                                    final TextView noticeLabel = (TextView) verifyDialogView.findViewById(R.id.notice_label);

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
                                            helpFAB.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final AlertDialog verifyDialog = verifyDialogBuilder.create();
                                            verifyDialog.getWindow().getAttributes().windowAnimations = R.style.PinDialogAnimation;
                                            verifyDialog.show();
                                            phoneProgressCircle.setVisibility(View.INVISIBLE);

                                            verifyDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    if (pinEditText.getText().toString().isEmpty() ||pinEditText.getText().toString().equals("")) {
                                                        noticeLabel.setText("Please enter your PIN");
                                                        return;
                                                    }

                                                    loginProgressCircle.setVisibility(View.VISIBLE);

                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            final String userPhoneNumber = phoneNumberEditText.getText().toString();
                                                            final String userCC = CCEditText.getText().toString();
                                                            String inputPIN = pinEditText.getText().toString();


                                                            parcareService.verifyPIN(userCC + userPhoneNumber, inputPIN).enqueue(new Callback<VerifyPINResponse>() {
                                                                @Override
                                                                public void onResponse(Call<VerifyPINResponse> call, Response<VerifyPINResponse> response) {
                                                                    if (response.body().getRespCode().equals("101") || response.body().getRespCode().equals("102")) {
                                                                        String userID = response.body().getUserId();
                                                                        String userAccessToken = response.body().getAccessToken();

                                                                        byte[] key = new byte[64];

                                                                        key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

                                                                        //Realm initialization
                                                                        Realm.init(LoginActivity.this);

                                                                        RealmConfiguration config = new RealmConfiguration.Builder()
                                                                                .encryptionKey(key)
                                                                                .build();

                                                                        Realm realm = Realm.getInstance(config);

                                                                        //Save usercredentials in encrypted realm
                                                                        realm.beginTransaction();

                                                                        UserCredentials credentials = realm.createObject(UserCredentials.class);

                                                                        credentials.setUserID(userID);
                                                                        credentials.setUserAccessToken(userAccessToken);
                                                                        credentials.setUserPhoneNumber(userCC + userPhoneNumber);

                                                                        realm.commitTransaction();

                                                                        realm.close();

                                                                        Intent intent;

                                                                        String respCode = response.body().getRespCode();

                                                                        //Check success new user (101) or returning user (102)
                                                                        Log.d("AUTH_RESPONSE", respCode);
                                                                        if (respCode.equals("102")) {
                                                                            intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                        } else {
                                                                            intent = new Intent(getApplicationContext(), NameActivity.class);
                                                                        }

                                                                        intent.putExtra(getString(R.string.user_id_tag), userID);
                                                                        intent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                                                                        intent.putExtra(getString(R.string.user_phone_number_tag), userCC + userPhoneNumber);
                                                                        intent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);

                                                                        // Animation for introducing the map. Map MainActivity slides in from the right to the left
                                                                        // LoginActivity fades out.
                                                                        ActivityOptions options =
                                                                                ActivityOptions.makeCustomAnimation(LoginActivity.this, R.anim.slide_to_left, R.anim.fade_out);
                                                                        startActivity(intent, options.toBundle());
                                                                        finish();
                                                                    } else if (response.body().getRespCode().equals("2")) {
                                                                        noticeLabel.setText("PIN Incorrect, try again");
                                                                        loginProgressCircle.setVisibility(View.INVISIBLE);
                                                                    } else if (response.body().getRespCode().equals("3")) {
                                                                        loginProgressCircle.setVisibility(View.INVISIBLE);
                                                                        verifyDialog.dismiss();
                                                                        Snackbar.make(findViewById(android.R.id.content), "PIN expired, try again", Snackbar.LENGTH_LONG).show();
                                                                    }

                                                                }

                                                                @Override
                                                                public void onFailure(Call<VerifyPINResponse> call, Throwable t) {
                                                                    Log.d("VERIFYPINRESPONSE_FAIL", t.getMessage());
                                                                }
                                                            });
                                                        }
                                                    }).start();


                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<RequestPINResponse> call, Throwable t) {
                                    nextButton.setEnabled(true);
                                    nextButton.setText("Resend");
                                    nextButton.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                    Snackbar.make(findViewById(android.R.id.content), "Request failed", Snackbar.LENGTH_SHORT).show();
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
