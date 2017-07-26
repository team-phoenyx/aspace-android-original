package com.aspace.aspace.tutorialfragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.aspace.aspace.R;
import com.aspace.aspace.SwipeDirection;
import com.aspace.aspace.TutorialViewPager;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialNameFragment extends Fragment {

    EditText nameEditText;
    Button nextButton;
    TutorialViewPager parentViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_name, container, false);

        nameEditText = (EditText) viewGroup.findViewById(R.id.name_textview);
        nextButton = (Button) getActivity().findViewById(R.id.next_button);

        parentViewPager = (TutorialViewPager) getActivity().findViewById(R.id.pager);
        parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);

        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) {
//                    nextButton.setVisibility(View.VISIBLE);
//                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.all);
//                } else {
//                    nextButton.setVisibility(View.GONE);
//                    parentViewPager.setAllowedSwipeDirection(SwipeDirection.left);
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return viewGroup;
    }
}
