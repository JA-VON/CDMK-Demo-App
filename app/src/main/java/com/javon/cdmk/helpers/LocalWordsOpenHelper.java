package com.javon.cdmk.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class to control main database
 * Created by Javon-Personal on 07/03/2015.
 */
public class LocalWordsOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 18;
    private static final String DB_NAME = "WordManagementLocal.db";

    private static final String PREFS = "userPrefs1";
    private SharedPreferences sharedPreferences;

    private static final String TABLE_NAME = "words";
    private static final String KEY_ID = "_id";
    private static final String KEY_REMOTE_ID = "remote_id";
    private static final String KEY_HINT = "hint";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTIONS = "options";
    private static final String KEY_DONE = "done";
    private static int last = 0;
    private Context myContext;
    private WordOpenHelper helper;
    private ArrayList<Integer> doneAlready = new ArrayList<>();

    public LocalWordsOpenHelper(Context context)
    {
        super(context, DB_NAME,null,DATABASE_VERSION);
        this.myContext = context;

        sharedPreferences = myContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int getLast()
    {
        return  last;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table "+TABLE_NAME+" " +
                        "("+KEY_ID+" integer primary key,"+KEY_HINT+" text, "+KEY_ANSWER+" text,"+KEY_OPTIONS+" text," +
                        ""+KEY_DONE+" integer,"+KEY_REMOTE_ID+" integer)"
        );

        helper = new WordOpenHelper(myContext);

        try {

            helper.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

        try {

            helper.openDataBase();

        } catch (SQLException sqle) {

            Toast.makeText(myContext, "DB Error", Toast.LENGTH_LONG).show();

        }

        helper.loadDb(this, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        doneAlready = getAllDone(db);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("last_state_refresh", sharedPreferences.getInt("last_state",1));
        editor.apply();

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public HashMap<String,String> getWord() {

        SQLiteDatabase db = this.getWritableDatabase();

        HashMap<String, String> word = new HashMap<>();

        String query;

        if(sharedPreferences.contains("last_state_refresh")) {
            last  = sharedPreferences.getInt("last_state_refresh", 1);
            query = "select * from "+TABLE_NAME+" where "+KEY_REMOTE_ID+"="+last;
        }
        else if(sharedPreferences.contains("last_state"))
        {
            last  = sharedPreferences.getInt("last_state", 1);
            query = "select * from "+TABLE_NAME+" where "+KEY_REMOTE_ID+"="+last;
        }
        else {
            query = "select * from " + TABLE_NAME + " where " + KEY_DONE + "=0 order by random() limit 1";
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("last_state_refresh");
        
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
        {
            word.put(KEY_REMOTE_ID,Integer.toString(cursor.getInt(0)));
            word.put(KEY_HINT,cursor.getString(1));
            word.put(KEY_ANSWER,cursor.getString(2));
            word.put(KEY_OPTIONS,cursor.getString(3));
            last = cursor.getInt(5);


            editor.putInt("last_state", last);
            editor.apply();

            cursor.close();
            db.close();
        }
        return word;
    }

    public void insert(String hint,String answer,String options,int id,SQLiteDatabase db)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(KEY_HINT,hint);
        contentValues.put(KEY_ANSWER,answer);
        contentValues.put(KEY_OPTIONS,options);
        contentValues.put(KEY_REMOTE_ID,id);
        contentValues.put(KEY_DONE,doneAlready.contains(id) ? 1:0);

        db.insert(TABLE_NAME,null,contentValues);
        //db.close();
    }

    public void setDone(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DONE,1);

        db.update(TABLE_NAME,values,KEY_REMOTE_ID+"= ?",new String[]{Integer.toString(id)});
    }

    public ArrayList<Integer> getAllDone(SQLiteDatabase db)
    {
        ArrayList<Integer> values = new ArrayList<>();

        String query = "select * from "+TABLE_NAME+" where "+KEY_DONE+" = 1";

        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            values.add(cursor.getInt(5));

            cursor.moveToNext();
        }
        cursor.close();
        //db.close();

        return values;
    }


}
