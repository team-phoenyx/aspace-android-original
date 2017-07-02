package me.parcare.parcare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import me.parcare.parcare.realmmodels.UserCredentials;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NameActivity extends AppCompatActivity {

    public static final String BASE_URL = "http://192.241.224.224:3000/api/";
    String name, userID, userAccessToken, userPhoneNumber, realmEncryptionKey;
    PCRetrofitInterface parcareService;
    EditText nameEditText;
    TextView instructionsLabel;
    Button nextButton;
    ProgressBar updateNameProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        userID = extras.getString(getString(R.string.user_id_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));

        setContentView(R.layout.activity_name);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        parcareService = retrofit.create(PCRetrofitInterface.class);

        nameEditText = (EditText) findViewById(R.id.name_edittext);
        instructionsLabel = (TextView) findViewById(R.id.enter_name_label);
        nextButton = (Button) findViewById(R.id.name_next_button);
        updateNameProgressCircle = (ProgressBar) findViewById(R.id.update_name_progresscircle);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            updateNameProgressCircle.setVisibility(View.VISIBLE);

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(instructionsLabel.getWindowToken(), 0);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    name = nameEditText.getText().toString();

                    if (name.isEmpty() || name.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(findViewById(android.R.id.content), "Please enter your name", Snackbar.LENGTH_SHORT).show();
                                updateNameProgressCircle.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        //TODO get response from this, success 100 or fail 6
                        parcareService.updateProfile(name, "", "", "", "", userID, userPhoneNumber, userAccessToken);

                        //If success, run this code
                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                        mainIntent.putExtra(getString(R.string.user_id_tag), userID);
                        mainIntent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                        mainIntent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);
                        mainIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                        startActivity(mainIntent);
                        finish();

                        //If fail, hide keyboard and show snackbar
                    }
                }
            }).start();


            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Realm.init(this);

        byte[] key = Base64.decode(realmEncryptionKey, Base64.DEFAULT);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .encryptionKey(key)
                .build();

        Realm realm = Realm.getInstance(config);

        //Clear all UserCredential and UserProfile objects from Realm
        final RealmResults<UserCredentials> credentialResults = realm.where(UserCredentials.class).findAll();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                credentialResults.deleteAllFromRealm();
            }
        });

        //Start loginactivity
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        loginIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
        startActivity(loginIntent);
        finish();
    }
}
