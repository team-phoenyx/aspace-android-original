package com.aspace.aspace.tutorialfragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aspace.aspace.R;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

/**
 * Created by Terrance on 7/12/2017.
 */

public class TutorialWelcomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_tutorial_welcome, container, false);

        KonfettiView konfettiView = (KonfettiView) viewGroup.findViewById(R.id.konfetti);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        konfettiView.build()
                .addColors(Color.RED, Color.BLUE)
                .setDirection(0.0, 359.0)
                .setSpeed(0.1f, 0.5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(0f, (float) width, -100f, -100f)
                .stream(300, 5000L);

        return viewGroup;
    }
}
