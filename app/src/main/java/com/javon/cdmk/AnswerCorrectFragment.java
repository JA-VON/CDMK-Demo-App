package com.javon.cdmk;



import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 30/09/2016.
 */

public class AnswerCorrectFragment extends Fragment {

    public static AnswerCorrectFragment newInstance()
    {
        return new AnswerCorrectFragment();
    }

    public AnswerCorrectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_answer_correct, container, false);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "LuckiestGuy.ttf");
        ((TextView) rootView.findViewById(R.id.correctHeader)).setTypeface(font);

        ((Button) rootView.findViewById(R.id.nextButton)).setTypeface(font);

        return rootView;
    }

}
