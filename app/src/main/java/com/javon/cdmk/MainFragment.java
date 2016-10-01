package com.javon.cdmk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.javon.cdmk.helpers.WordInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 30/09/2016.
 */

public class MainFragment extends Fragment{

    private static final String KEY_LEVEL = "level";

    private TextView hint;
    private Button showAnswer;
    private LinearLayout answerLayout,topLayout,bottomLayout;
    private Map<TextView,TextView> viewMap = new HashMap<>();
    private Map<Integer,TextView> optionsMap = new HashMap<>();
    private onAnswerVerifiedListener mListener;
    private OnPattyCountListener mPattyListener;
    private OnShareListener mShareListener;
    private Map<Integer,Character> answerMap = new HashMap<>();
    private ArrayList<TextView> mValidViews = new ArrayList<>();
    public static final int SHOW_LETTER = 1;
    public static final int SHOW_ANSWER = 2;
    private TextView plus;
    private int level;
    private PersistenceOpenHelper dbHelper;
    private ImageView refresh;
    private TextView sound;
    private MediaPlayer mp;
    private ImageView whatsappView,facebookView;
    private int soundId;


    public static MainFragment newInstance(int level) {
        MainFragment fragment = new MainFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_LEVEL,level);

        fragment.setArguments(args);

        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLevel(getArguments().getInt(KEY_LEVEL));
        dbHelper = new PersistenceOpenHelper(getActivity());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onAnswerVerifiedListener) activity;
            mPattyListener = (OnPattyCountListener) activity;
            mShareListener = (OnShareListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAnswerVerifiedListener and OnPattyCountListener adn OnShareListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        facebookView = (ImageView) rootView.findViewById(R.id.facebook);

        if(!appInstalled("com.facebook.katana"))
        {
            facebookView.setVisibility(View.GONE);
        }
        else
        {
            facebookView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareListener.share("facebook");
                }
            });
        }

        whatsappView = (ImageView) rootView.findViewById(R.id.whatsapp);

        if(!appInstalled("com.whatsapp"))
        {
            whatsappView.setVisibility(View.GONE);
        }
        else {
            whatsappView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mShareListener.share("whatsapp");
                }
            });
        }

        hint = (TextView) rootView.findViewById(R.id.hintView);
        refresh = (ImageView) rootView.findViewById(R.id.refreshImageView);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyFields();
            }
        });

        setupHint();

        showAnswer = (Button) rootView.findViewById(R.id.show_answer);

        showAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowAnswerDialogFragment dialog = new ShowAnswerDialogFragment();
                dialog.setTargetFragment(MainFragment.this, SHOW_ANSWER);
                dialog.show(getFragmentManager().beginTransaction(), "dialog");
            }
        });

        answerLayout = ((LinearLayout) rootView.findViewById(R.id.answerLayout));
        topLayout = ((LinearLayout) rootView.findViewById(R.id.topLayout));
        bottomLayout = ((LinearLayout) rootView.findViewById(R.id.bottomLayout));

        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "LuckiestGuy.ttf");

        hint.setTypeface(font);

        plus = (TextView) rootView.findViewById(R.id.plusButton);
        plus.setTypeface(font);

        plus.setOnClickListener(new onAddLetterListener());

        for(int i=0; i<answerLayout.getChildCount();i++)
        {
            ((TextView) answerLayout.getChildAt(i)).setTypeface(font);
            (answerLayout.getChildAt(i)).setOnClickListener(new OnAnswerViewListener());
        }

        for(int i=0; i<topLayout.getChildCount();i++)
        {
            (topLayout.getChildAt(i)).setOnClickListener(new OnOptionsViewListener());
            (bottomLayout.getChildAt(i)).setOnClickListener(new OnOptionsViewListener());


            optionsMap.put(i, (TextView) topLayout.getChildAt(i));
            optionsMap.put(i+7, (TextView) bottomLayout.getChildAt(i));

            ((TextView) topLayout.getChildAt(i)).setTypeface(font);
            ((TextView) bottomLayout.getChildAt(i)).setTypeface(font);
        }

        setup();

        HashMap<Integer,Integer> persistenceMapping = dbHelper.getPurchases();
        HashMap<TextView,TextView> views = new HashMap<>();

        for(Integer i: persistenceMapping.keySet())
        {
            TextView ans = (TextView) rootView.findViewById(i);
            TextView option = (TextView) rootView.findViewById(persistenceMapping.get(i));

            views.put(ans,option);
        }

        fillAlreadyPurchasedLetters(views);

        sound = (TextView) rootView.findViewById(R.id.playSound);

        String word = new String(WordInterface.getAnswer());
        if(soundExists(word.toLowerCase())) {
            sound.setVisibility(View.VISIBLE);
            sound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder confirmWippe = new AlertDialog.Builder( getActivity());
                    confirmWippe.setMessage("Hear the word pronounced and used in a sentence for 20 patties?");
                    confirmWippe.setPositiveButton(getActivity().getResources().getString(R.string.ahh),new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(mPattyListener.hasEnoughPatties(R.integer.hear_wippe_cost)) {
                                if (mPattyListener.removePatty(getActivity().getResources().getInteger(R.integer.hear_wippe_cost)))
                                    playSound();
                            }
                            else
                                mPattyListener.notEnoughPatties();
                        }
                    }).setNegativeButton(getActivity().getResources().getString(R.string.gweh), null);

                    confirmWippe.show();

                }
            });
        }



        return rootView;
    }

    private void showAnswer() {

        //disableAllButtons();
        Handler mHandler = new Handler() ;

        for(int i =0;i<WordInterface.getAnswerLength();i++)
        {
            if(enterRandomLetter())
                break;
        }

    }

    private void emptyFields() {
        for(int i=0; i<answerLayout.getChildCount();i++)
        {
            (answerLayout.getChildAt(i)).callOnClick();
        }
    }

    private void setupHint()
    {
        hint.setText(WordInterface.getHint(getActivity()));
    }

    private boolean allSlotsFilled()
    {
        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            if (((TextView) answerLayout.getChildAt(i)).getText().length() == 0)
                return false;
        }
        return true;
    }

    private void setup() {

        answerSetup();
        performMapping();//
        optionsSetup();
    }

    private void performMapping() {

        char[] letterMaps = WordInterface.getAnswer();

        for(int i =0;i<letterMaps.length;i++)
        {
            answerMap.put(i, letterMaps[i]);
        }

    }

    private void optionsSetup() {

        char[] options = WordInterface.getOptions();

        for(int i =0; i<topLayout.getChildCount();i++)
        {
            ((TextView) topLayout.getChildAt(i)).setText(Character.toString(options[i]));
            ((TextView) bottomLayout.getChildAt(i)).setText(Character.toString(options[i + 7]));
        }
    }

    private void answerSetup() {
        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<answerLayout.getChildCount();i++)
        {
            TextView mView = (TextView) answerLayout.getChildAt(i);
            mView.setVisibility(numViews > i ? View.VISIBLE : View.GONE);
        }
    }

    private int getFirstEmptySlot() {
        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            if (((TextView) answerLayout.getChildAt(i)).getText().length() == 0)
                return i;
        }
        return 0;
    }

    private boolean hasLetter(TextView v)
    {
        return v.getText().length() != 0;
    }

    private boolean checkAnswer() {
        String proposed ="";
        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            proposed += ((TextView) answerLayout.getChildAt(i)).getText().toString();
        }

        if (WordInterface.isCorrectAnswer(proposed)) {
            makeAnswerGreen();
            disableAllButtons();
            dbHelper.deleteAll();
            mListener.onAnswerVerified(proposed);
            return true;
        } else {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

            makeAnswerRed();
            v.vibrate(500);
            return false;
        }
    }

    private void disableAllButtons() {
        plus.setEnabled(false);
        refresh.setEnabled(false);
        sound.setEnabled(false);
        showAnswer.setEnabled(false);

        for(int i =0; i<topLayout.getChildCount();i++)
        {
            (topLayout.getChildAt(i)).setEnabled(false);
            (bottomLayout.getChildAt(i)).setEnabled(false);
        }

        for(int i=0; i<answerLayout.getChildCount();i++)
        {
            (answerLayout.getChildAt(i)).setEnabled(false);
        }
        whatsappView.setEnabled(false);
        facebookView.setEnabled(false);
    }

    private void makeAnswerGreen() {

        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            TextView view = ((TextView) answerLayout.getChildAt(i));
            if(view.isEnabled())
                view.setTextColor(getResources().getColor(R.color.letterColor));
        }

    }


    private void makeAnswerRed() {

        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            TextView view = ((TextView) answerLayout.getChildAt(i));
            if(view.isEnabled())
                view.setTextColor(getResources().getColor(R.color.red));
        }

    }

    private void makeAnswerBlack()
    {
        int numViews = WordInterface.getAnswerLength();

        for(int i=0; i<numViews;i++)
        {
            TextView view = ((TextView) answerLayout.getChildAt(i));
            if(view.isEnabled())
                view.setTextColor(getResources().getColor(R.color.answerColor));
        }
    }

    public boolean soundExists(String name)
    {
        int resID = getResources().getIdentifier(name, "raw", getActivity().getPackageName());
        soundId = resID;
        return resID != 0;
    }

    public void playSound() {
        if (mp != null) {
            mp.release();
        }

        if (soundId != 0) {
            mp = MediaPlayer.create(getActivity(), soundId);
            mp.start();
        }
    }

    private boolean enterRandomLetter() {
        int count = 0;
        int numViews = WordInterface.getAnswerLength();
        char[] verified = new char[numViews];

        for(int i=0; i<numViews;i++)
        {
            TextView aView = (TextView) answerLayout.getChildAt(i);
            if(!aView.isEnabled())
            {
                verified[count] = aView.getText().toString().charAt(0);
                count++;
            }
        }

        //get a random letter for the answer
        char randomLetter = WordInterface.getRandomAnswerLetter(verified);

        //get a random view with that random letter
        TextView view = getRandomViewWithRandomLetter(randomLetter);
        mValidViews.add(view);

        ArrayList<Integer> setOfPossibleValues = new ArrayList<>();

        // find all possible children of the answer views that match that letter
        for(Integer i: answerMap.keySet())
        {

            if(answerMap.get(i).equals(randomLetter))
            {
                if((answerLayout.getChildAt(i)).isEnabled())
                    setOfPossibleValues.add(i);
            }
        }

        //pick one
        int child =setOfPossibleValues.get(new Random().nextInt(setOfPossibleValues.size()));


        TextView viewFill = ((TextView) answerLayout.getChildAt(child));

        //put letter in view and disable it
        //checks performed
        if(viewFill.getText().toString().isEmpty()) {
            viewFill.setText(Character.toString(randomLetter));
            viewFill.setTextColor(getResources().getColor(R.color.letterColor));
            viewFill.setEnabled(false);
            viewFill.setBackgroundResource(R.drawable.letter_border_disabled);


            //TODO - evaluate
            view.setVisibility(View.INVISIBLE);
        }
        else {
            if(viewFill.getText().toString().equals(Character.toString(randomLetter)))
            {

                viewFill.setEnabled(false);
                viewFill.setTextColor(getResources().getColor(R.color.letterColor));
                viewFill.setBackgroundResource(R.drawable.letter_border_disabled);

            }
            else {
                viewFill.setText(Character.toString(randomLetter));
                viewFill.setEnabled(false);
                view.setVisibility(View.INVISIBLE);
                viewFill.setTextColor(getResources().getColor(R.color.letterColor));
                viewFill.setBackgroundResource(R.drawable.letter_border_disabled);

                for (TextView v : viewMap.keySet()) {
                    if (v.getId() == viewFill.getId()) {
                        viewMap.get(v).setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        }

        dbHelper.insertPurchase(viewFill.getId(),view.getId());

        for (TextView v : viewMap.keySet()) {
            if (viewMap.get(v).getId() == view.getId()) {
                if(v.isEnabled())
                    v.setText("");
                break;
            }
        }

        if(allSlotsFilled())
        {
            if(checkAnswer())
                return true;
            return false;
        }
        return false;
    }

    public void fillAlreadyPurchasedLetters(HashMap<TextView,TextView> map)
    {
        for(TextView view:map.keySet())
        {
            TextView mView = map.get(view);

            view.setText(Character.toString(mView.getText().toString().charAt(0)));
            view.setTextColor(getResources().getColor(R.color.letterColor));
            view.setEnabled(false);
            view.setBackgroundResource(R.drawable.letter_border_disabled);


            //TODO - evaluate
            mView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SHOW_LETTER:

                if (resultCode == Activity.RESULT_OK) {
                    int resId = R.integer.show_letter_amount;
                    if (mPattyListener.hasEnoughPatties(resId)) {
                        if(mPattyListener.removePatty(getActivity().getResources().getInteger(resId)))
                            enterRandomLetter();
                    }
                    else
                        mPattyListener.notEnoughPatties();

                } else if (resultCode == Activity.RESULT_CANCELED){
                    // After Cancel code.
                }
                break;
            case SHOW_ANSWER:
                if(resultCode == Activity.RESULT_OK)
                {
                    boolean notEnough = true;
                    if(mPattyListener.hasEnoughPatties(R.integer.show_answer_amount))
                    {
                        notEnough = false;
                        if(mPattyListener.removePatty(getActivity().getResources().getInteger(R.integer.show_answer_amount)))
                            showAnswer();
                        else
                            notEnough = true;
                    }

                    if(notEnough)
                        mPattyListener.notEnoughPatties();

                }
                else if(resultCode == Activity.RESULT_FIRST_USER)
                {
                    showAnswer();
                }

        }

    }

    private boolean appInstalled(String uri)
    {
        PackageManager pm = getActivity().getPackageManager();
        boolean app_installed = false;
        try
        {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed ;
    }

    private TextView getRandomViewWithRandomLetter(char randomLetter) {

        //TODO - derive more efficient solution
        ArrayList<TextView> views = new ArrayList<>();
        ArrayList<TextView> filteredViews = new ArrayList<>();
        int i =0,k=0,j=0;


        for(TextView view : optionsMap.values())
        {
            if(view.getText().toString().equals(Character.toString(randomLetter))) {
                views.add(view);
            }
        }

        filteredViews.addAll(views);

        for(TextView view :mValidViews)
        {
            for(TextView mView : views)
                if(view.getId()==mView.getId()) {
                    filteredViews.remove(mView);

                }

        }

        int pick =  new Random().nextInt(filteredViews.size());

        return filteredViews.get(pick);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    //---------------------------------------- Listeners -------------------------------------------------
    private class OnOptionsViewListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getVisibility() == View.VISIBLE)
                if(mp!=null)
                {
                    mp.release();
                }
            mp = MediaPlayer.create(getActivity(), R.raw.click);
            mp.start();
            if(!allSlotsFilled())
            {
                v.setVisibility(View.INVISIBLE);

                int x = getFirstEmptySlot();

                TextView aView = ((TextView) answerLayout.getChildAt(x));
                aView.setText(((TextView) v).getText());

                viewMap.put(aView, (TextView) v);
                if(allSlotsFilled())
                    checkAnswer();
            }
        }
    }

    private class OnAnswerViewListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            TextView view = (TextView) v;
            if(v.isEnabled())

                if(hasLetter(view))
                {
                    if(mp!=null)
                    {
                        mp.release();
                    }
                    mp = MediaPlayer.create(getActivity(), R.raw.click);
                    mp.start();
                    makeAnswerBlack();
                    view.setText("");
                    for (TextView mView : viewMap.keySet()) {
                        if (v.getId() == mView.getId()) {
                            viewMap.get(mView).setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
        }
    }

    private class onAddLetterListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            AddLetterConfirmationDialogFragment dialog = new AddLetterConfirmationDialogFragment();
            dialog.setTargetFragment(MainFragment.this, SHOW_LETTER);
            dialog.show(getFragmentManager().beginTransaction(),"dialog");

        }
    }

    //-------------------------------- interface callbacks ----------------------------------
    public interface onAnswerVerifiedListener
    {
        void onAnswerVerified(String answer);
    }

    public interface OnPattyCountListener
    {
        boolean hasEnoughPatties(int resId);
        boolean removePatty(int cost);

        void notEnoughPatties();
    }

    public interface OnShareListener
    {
        void share(String tag);
    }
}