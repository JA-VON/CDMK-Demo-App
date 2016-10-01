package com.javon.cdmk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 30/09/2016.
 */

public class AddLetterConfirmationDialogFragment extends DialogFragment {


    public AddLetterConfirmationDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.add_letter_dialog, null);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "LuckiestGuy.ttf");

        TextView prompt = ((TextView) rootView.findViewById(R.id.prompt));
        prompt.setTypeface(font);

        int cost = getActivity().getResources().getInteger(R.integer.show_letter_amount);
        if(cost == 1)
            prompt.setText("Show a letter in the answer for "+ cost + " patty?");
        else if(cost>1)
            prompt.setText("Show a letter in the answer for "+ cost + " patties?");



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


        AlertDialog dialog = builder.create();

        return dialog;
    }
}
