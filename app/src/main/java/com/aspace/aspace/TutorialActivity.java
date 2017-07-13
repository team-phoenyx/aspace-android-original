package com.aspace.aspace;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
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
    String userID, userPhoneNumber, userAccessToken;

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

                        if (((EditText) findViewById(R.id.name_edittext)).getText().toString().isEmpty()) {
                            nextButton.setVisibility(View.GONE);
                            viewPager.setAllowedSwipeDirection(SwipeDirection.left);
                        } else {
                            nextButton.setVisibility(View.VISIBLE);
                            viewPager.setAllowedSwipeDirection(SwipeDirection.all);
                        }

                        break;
                    case CAR_FRAGMENT_TAG:
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

                        EditText nameEditText = (EditText) findViewById(R.id.name_edittext);
                        EditText homeAddressEditText = (EditText) findViewById(R.id.home_address_edittext);
                        EditText workAddressEditText = (EditText) findViewById(R.id.work_address_edittext);

                        //TODO send car info up to server (if the user inputted one)

                        aspaceService.updateProfile(
                                nameEditText.getText().toString(),
                                workAddressEditText.getText().toString(),
                                homeAddressEditText.getText().toString(),
                                homeLocID, workLocID, userID, userPhoneNumber, userAccessToken)
                                .enqueue(new Callback<UpdateProfileResponse>() {
                                    @Override
                                    public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {

                                    }
                                });
                        //TODO send profile info up to server

                        nextButton.setVisibility(View.VISIBLE);
                        nextButton.setText("Start");
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
                    //TODO Start mainactivity, pass 3 identifiers
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
