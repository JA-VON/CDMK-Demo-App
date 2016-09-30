package com.javon.cdmk.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by Javon-Personal on 16/02/2015.
 */
public class WordOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 6;
    private static final String DB_NAME = "WordManagement.db";

    private static final String TABLE_NAME = "words";
    private static final String KEY_ID = "_id";
    private static final String KEY_HINT = "hint";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_OPTIONS = "options";
    private Context myContext;
    private SQLiteDatabase myDataBase;


    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.chatois.app/databases/";

    public WordOpenHelper(Context context) {
        super(context, DB_NAME,null,DATABASE_VERSION);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        /*boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{*/

            //By calling this method an empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
//        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public HashMap<String,String> getWord(int id)
    {
        HashMap<String, String> word = new HashMap<>();
        //query
        String query = "select * from "+TABLE_NAME+" where "+KEY_ID+"="+id;


        //SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = myDataBase.rawQuery(query, null);

        if(cursor.moveToFirst())
        {
            word.put(KEY_HINT,cursor.getString(1));
            word.put(KEY_ANSWER,cursor.getString(2));
            word.put(KEY_OPTIONS,cursor.getString(3));

            cursor.close();
            myDataBase.close();
        }
        return word;
    }

    public void loadDb(LocalWordsOpenHelper helper,SQLiteDatabase db)
    {
        //query
        String query = "select * from "+TABLE_NAME+" order by random ()";


        Cursor cursor = myDataBase.rawQuery(query, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            helper.insert(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(0),db);
            cursor.moveToNext();

        }
        cursor.close();
        //myDataBase.close();
        //helper.close();
    }
}
