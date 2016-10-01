package com.javon.cdmk.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 30/09/2016.
 */

public class WordInterface {

    private static final String KEY_HINT = "hint";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTIONS = "options";
    private static final String KEY_REMOTE_ID = "remote_id";
    private static final String PREFS = "userPrefs1";

    private static String hint;
    private static String answer;
    private static String kId;
    private static int level = 0;
    private static LocalWordsOpenHelper dbHelper;

    private static ArrayList<String> answerChars = new ArrayList<>();
    private static String options;
    private SharedPreferences sharedPreferences;

    public static char getRandomAnswerLetter(char[] lettersVerified)
    {
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(answerChars);

        for(char c : lettersVerified)
        {
            temp.remove(Character.toString(c));
        }

        char randomLetter = temp.get(new Random().nextInt(temp.size())).charAt(0);

        return randomLetter;
    }

    public static void setHint(String s, String s1)
    {
        hint = s;
        setAnswer(s1);
    }

    public static char[] getAnswer()
    {
        return answer.toCharArray();
    }

    public static int getAnswerLength()
    {
        if(answer!= null)
            return answer.length();

        return 0;
    }

    public static boolean isCorrectAnswer(String proposed)
    {
        return answer.equals(proposed);
    }

    public static void setAnswer(String s1) {

        answer = s1;
        answerChars = new ArrayList<>();
        for(int i = 0;i<getAnswerLength();i++)
        {
            answerChars.add(Character.toString(answer.charAt(i)));
        }
    }

    public static String getkId() {
        return kId;
    }

    public static void setkId(String kId) {
        WordInterface.kId = kId;
    }

    public int getLastLevel(Context context)
    {
        int last = 0;
        sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if(sharedPreferences.contains("last_level")) {
            last = sharedPreferences.getInt("last_level", 1);
        }
        return last;
    }

    public static String getHint(Context context) {
        dbHelper = new LocalWordsOpenHelper(context);



        HashMap<String, String> values = dbHelper.getWord();

        setkId(values.get(KEY_REMOTE_ID));
        setHint(values.get(KEY_HINT), values.get(KEY_ANSWER));
        setOptions(values.get(KEY_OPTIONS));
        return hint;
    }

    public static void setLevel(int mLevel) {
        WordInterface.level = mLevel;
    }

    public static void incrementLevel()
    {
        level++;
        dbHelper.setDone(LocalWordsOpenHelper.getLast());
    }

    public static void setOptions(String options) {
        WordInterface.options = options;
    }

    public static char [] getOptions() {
        return options.toCharArray();
    }
}
