package com.javon.cdmk.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by Javon-Personal on 22/02/2015.
 */
public class PersistenceOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "persistentLetters";

    private static final String TABLE_NAME = "purchases";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_OPTION = "option_id";
    private static final String COLUMN_ANSWER = "answer_id";

    public PersistenceOpenHelper(Context mContext)
    {
        super(mContext,DB_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table "+TABLE_NAME+" " +
                        "("+COLUMN_ID+" integer primary key, "+COLUMN_ANSWER+" integer,"+COLUMN_OPTION+" integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertPurchase(int answer,int option)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ANSWER, answer);
        contentValues.put(COLUMN_OPTION, option);

        db.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public HashMap<Integer,Integer> getPurchases()
    {
        HashMap<Integer,Integer> mapping = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "select * from "+TABLE_NAME,null );

        if(cursor.moveToFirst())
        {
            do
                mapping.put(cursor.getInt(1),cursor.getInt(2));
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return mapping;
    }

    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null,null);
        db.close();
    }
}
