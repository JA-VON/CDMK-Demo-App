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

public class CompleteDialogFragment extends DialogFragment {


    public CompleteDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.fragment_complete_dialog, null);

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "LuckiestGuy.ttf");

        TextView prompt = ((TextView) rootView.findViewById(R.id.text));
        prompt.setTypeface(font);

        builder.setView(rootView)
                .setPositiveButton(R.string.ahh, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });


        AlertDialog dialog = builder.create();

        return dialog;
    }
}
