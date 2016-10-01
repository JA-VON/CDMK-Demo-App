package com.javon.cdmk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 30/09/2016.
 */

public class ShowAnswerDialogFragment extends DialogFragment {

    private static final String PREFS = "userPrefs1";
    private OnGetSkipsListener mListener;

    public ShowAnswerDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnGetSkipsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnGetSkipsListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.add_letter_dialog, null);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "LuckiestGuy.ttf");

        TextView prompt = ((TextView) rootView.findViewById(R.id.prompt));
        TextView free = (TextView) rootView.findViewById(R.id.freeView);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prompt.setTypeface(font);
        free.setTypeface(font);

        int cost = getActivity().getResources().getInteger(R.integer.show_answer_amount);
        if(cost == 1)
            prompt.setText("Show the answer for "+ cost + " patty?");
        else if(cost>1)
            prompt.setText("Show the answer for "+ cost + " patties?");

        int skips = sharedPreferences.getInt("skips", 0);

        final int newSkips = skips-1;

        free.setText(skips + " free answers left");



        builder.setView(rootView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
            }
        });

        if(skips>0)
        {
            builder.setNeutralButton("Show for free", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("skips", newSkips);
                    editor.apply();
                    getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_FIRST_USER,getActivity().getIntent());
                }
            });
        }
        else
        {
            builder.setNeutralButton("Get more free answers?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(getActivity()).setMessage("Get 10 skips for $US12.99")
                            .setPositiveButton("Yeah Man!", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mListener.getSkips();
                                }
                            })
                            .setNegativeButton("Gweh",null).show();
                }
            });
        }

        AlertDialog dialog = builder.create();

        return dialog;
    }

    public interface OnGetSkipsListener
    {
        void getSkips();
    }
}
