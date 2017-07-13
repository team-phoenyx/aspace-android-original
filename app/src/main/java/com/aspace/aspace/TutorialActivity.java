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

import com.aspace.aspace.tutorialfragments.TutorialCarFragment;
import com.aspace.aspace.tutorialfragments.TutorialLocationsFragment;
import com.aspace.aspace.tutorialfragments.TutorialNameFragment;
import com.aspace.aspace.tutorialfragments.TutorialStartFragment;
import com.aspace.aspace.tutorialfragments.TutorialWelcomeFragment;

public class TutorialActivity extends FragmentActivity {

    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    Button backButton, nextButton;

    private static final int NUM_PAGES = 5;

    private static final int START_FRAGMENT_TAG = 0;
    private static final int NAME_FRAGMENT_TAG = 1;
    private static final int CAR_FRAGMENT_TAG = 2;
    private static final int LOCATIONS_FRAGMENT_TAG = 3;
    private static final int WELCOME_FRAGMENT_TAG = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        //TODO receive 3 identifiers from splash

        backButton = (Button) findViewById(R.id.back_button);
        nextButton = (Button) findViewById(R.id.next_button);

        viewPager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

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
                        break;
                    case NAME_FRAGMENT_TAG:
                        backButton.setVisibility(View.VISIBLE);

                        if (((EditText) findViewById(R.id.name_edittext)).getText().toString().isEmpty()) nextButton.setVisibility(View.GONE);
                        else nextButton.setVisibility(View.VISIBLE);

                        break;
                    case CAR_FRAGMENT_TAG:
                        backButton.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(View.VISIBLE);
                        break;
                    case LOCATIONS_FRAGMENT_TAG:
                        backButton.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(View.VISIBLE);
                        break;
                    case WELCOME_FRAGMENT_TAG:
                        backButton.setVisibility(View.GONE);
                        nextButton.setVisibility(View.VISIBLE);
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
