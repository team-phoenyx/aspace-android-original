package com.aspace.aspace;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.aspace.aspace.retrofitmodels.UpdateProfileResponse;
import com.aspace.aspace.tutorialfragments.TutorialCarFragment;
import com.aspace.aspace.tutorialfragments.TutorialLocationsFragment;
import com.aspace.aspace.tutorialfragments.TutorialNameFragment;
import com.aspace.aspace.tutorialfragments.TutorialStartFragment;
import com.aspace.aspace.tutorialfragments.TutorialWelcomeFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TutorialActivity extends FragmentActivity {

    TutorialViewPager viewPager;
    PagerAdapter pagerAdapter;
    Button backButton, nextButton;
    String userID, userPhoneNumber, userAccessToken, realmEncryptionKey;
    String name;

    PCRetrofitInterface aspaceService;

    private static final int NUM_PAGES = 5;

    private static final int START_FRAGMENT_TAG = 0;
    private static final int NAME_FRAGMENT_TAG = 1;
    private static final int CAR_FRAGMENT_TAG = 2;
    private static final int LOCATIONS_FRAGMENT_TAG = 3;
    private static final int WELCOME_FRAGMENT_TAG = 4;

    public static final String BASE_URL = "http://192.241.224.224:3000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        Bundle extras = getIntent().getExtras();
        userID = extras.getString(getString(R.string.user_id_tag));
        userPhoneNumber = extras.getString(getString(R.string.user_phone_number_tag));
        userAccessToken = extras.getString(getString(R.string.user_access_token_tag));
        realmEncryptionKey = extras.getString(getString(R.string.realm_encryption_key_tag));

        backButton = (Button) findViewById(R.id.back_button);
        nextButton = (Button) findViewById(R.id.next_button);

        viewPager = (TutorialViewPager) findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();

        aspaceService = retrofit.create(PCRetrofitInterface.class);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case START_FRAGMENT_TAG:
                        backButton.setVisibility(View.GONE);
                        nextButton.setVisibility(View.VISIBLE);
                        viewPager.setAllowedSwipeDirection(SwipeDirection.right);
                        break;
                    case NAME_FRAGMENT_TAG:
                        backButton.setVisibility(View.VISIBLE);

                        if (((EditText) findViewById(R.id.name_textview)).getText().toString().isEmpty()) {
                            nextButton.setVisibility(View.GONE);
                            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                        } else {
                            nextButton.setVisibility(View.VISIBLE);
                            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        }

                        break;
                    case CAR_FRAGMENT_TAG:

                        View nameFragmentView = ((TutorialNameFragment) pagerAdapter.instantiateItem(viewPager, NAME_FRAGMENT_TAG)).getView();
                        if (nameFragmentView != null) {
                            EditText nameEditText = (EditText) nameFragmentView.findViewById(R.id.name_textview);
                            name = nameEditText.getText().toString();
                        }
                        backButton.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(View.VISIBLE);
                        viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        break;
                    case LOCATIONS_FRAGMENT_TAG:
                        backButton.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(View.VISIBLE);
                        viewPager.setAllowedSwipeDirection(SwipeDirection.all);


                        break;
                    case WELCOME_FRAGMENT_TAG:
                        backButton.setVisibility(View.GONE);

                        TutorialLocationsFragment locationsFragment = (TutorialLocationsFragment) pagerAdapter.instantiateItem(viewPager, LOCATIONS_FRAGMENT_TAG);

                        View locationsFragmentView = locationsFragment.getView();

                        EditText homeAddressEditText = (EditText) locationsFragmentView.findViewById(R.id.home_address_edittext);
                        EditText workAddressEditText = (EditText) locationsFragmentView.findViewById(R.id.work_address_edittext);

                        //TODO send car info up to server (if the user inputted one)

                        String[] locationIDs = locationsFragment.getLocationIDs();
                        aspaceService.updateProfile(
                                name,
                                workAddressEditText.getText().toString(),
                                homeAddressEditText.getText().toString(),
                                locationIDs[0], locationIDs[1], userID, userPhoneNumber, userAccessToken)
                                .enqueue(new Callback<UpdateProfileResponse>() {
                                    @Override
                                    public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                                        if (response.body().getRespCode().equals("100")) {
                                            nextButton.setVisibility(View.VISIBLE);
                                            nextButton.setText("Start");
                                        } else {
                                            View view = TutorialActivity.this.getCurrentFocus();
                                            if (view != null) {
                                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                            }
                                            Snackbar.make(findViewById(android.R.id.content), "Something went wrong, please try again", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                                        View view = TutorialActivity.this.getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                        Snackbar.make(findViewById(android.R.id.content), "Something went wrong, please try again", Snackbar.LENGTH_LONG).show();
                                    }
                                });


                        viewPager.setAllowedSwipeDirection(SwipeDirection.none);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == WELCOME_FRAGMENT_TAG) {
                    Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startIntent.putExtra(getString(R.string.user_id_tag), userID);
                    startIntent.putExtra(getString(R.string.user_access_token_tag), userAccessToken);
                    startIntent.putExtra(getString(R.string.user_phone_number_tag), userPhoneNumber);
                    startIntent.putExtra(getString(R.string.realm_encryption_key_tag), realmEncryptionKey);
                    startActivity(startIntent);
                    finish();
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
            }
        });

        backButton.setVisibility(View.GONE);
        viewPager.setAllowedSwipeDirection(SwipeDirection.right);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0 || viewPager.getCurrentItem() == 4) super.onBackPressed(); //default action if at the very first fragment
        else viewPager.setCurrentItem(viewPager.getCurrentItem() - 1); //go to the last fragment if not at the very first fragment
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case START_FRAGMENT_TAG:
                    return new TutorialStartFragment();
                case NAME_FRAGMENT_TAG:
                    return new TutorialNameFragment();
                case CAR_FRAGMENT_TAG:
                    return new TutorialCarFragment();
                case LOCATIONS_FRAGMENT_TAG:
                    return new TutorialLocationsFragment();
                case WELCOME_FRAGMENT_TAG:
                    return new TutorialWelcomeFragment();
                default:
                    return new TutorialStartFragment();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
