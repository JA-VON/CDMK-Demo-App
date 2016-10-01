package com.javon.cdmk;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.javon.cdmk.helpers.WordInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements MainFragment.onAnswerVerifiedListener,
        MainFragment.OnShareListener,ShowAnswerDialogFragment.OnGetSkipsListener {

    private static final String PREFS = "userPrefs1";
    private SharedPreferences sharedPreferences;
    private Fragment correctFrag;
    private Toolbar mainBar;
    private TextView levelView;
    private Bundle ownedItems;
    private int level;

    private ServiceConnection mServiceConn;
    private MediaPlayer mp;
    private boolean isSkip = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        mainBar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(mainBar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int mLevel = 1;

        sharedPreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);



        if(!sharedPreferences.contains(getString(R.string.string_first)))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.string_first), 1);
            editor.apply();

            InstructionDialogFragment dialog = new InstructionDialogFragment();
            dialog.show(getSupportFragmentManager(),"Instructions");
        }

        if(!sharedPreferences.contains(getString(R.string.string_skips)))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.string_skips), 5);
            editor.apply();
        }

        if(sharedPreferences.contains(getString(R.string.string_level))) {
            mLevel = sharedPreferences.getInt(getString(R.string.string_level), 1);
            WordInterface.setLevel(mLevel);
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.string_level), mLevel);
            editor.apply();
            WordInterface.setLevel(mLevel);
        }

        levelView = (TextView) mainBar.findViewById(R.id.level);
        levelView.setText(Integer.toString(mLevel));

        //TODO - Evaluate this further
        if(sharedPreferences.contains(getString(R.string.string_patties))) {
            setPattyValue(sharedPreferences.getInt(getString(R.string.string_patties), 0));
        }
        else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.string_patties), 10);
            editor.apply();
            setPattyValue(10);
        }



        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, MainFragment.newInstance(level))
                    .commit();
        }

    }

    private void setPattyValue(int patties) {
        ((TextView) mainBar.findViewById(R.id.pattyView)).setText(Integer.toString(patties)+" patties");

    }

    public void increasePattyCount(int amount)
    {
        int val = sharedPreferences.getInt("patties", 0);
        val+=amount;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("patties", val);
        editor.apply();

        setPattyValue(val);
    }

    public void increaseSkipCount(int amount)
    {
        int val = sharedPreferences.getInt("skips", 0);
        val+=amount;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("skips", val);
        editor.apply();

        setPattyValue(val);
    }

    public int getPattyCount()
    {
        return sharedPreferences.getInt("patties", 10);
    }


    public Bitmap screenShot() {

        View view = findViewById(R.id.root);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public Uri getImageUri( Bitmap inImage) {
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut;
        File file = new File(path, "chatois.png"); // the File to save to
        try {
            fOut = new FileOutputStream(file);
            inImage.compress(Bitmap.CompressFormat.PNG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close(); // do not forget to close the stream

            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    @Override
    public void share(String tag)
    {
        switch(tag) {
            case "whatsapp":
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Can you help me guess this word?");
                sendIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(screenShot()));
                sendIntent.setType("image/*");
                sendIntent.setPackage("com.whatsapp");
                startActivity(sendIntent);
                break;
        }
    }

    public void next(View view)
    {

        getSupportFragmentManager().beginTransaction()
                .remove(correctFrag)
                .commit();

        if(mp!=null)
        {
            mp.release();
        }
        mp = MediaPlayer.create(this, R.raw.awoh);
        mp.start();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MainFragment.newInstance(level))
                .commit();

        /*if(level%5 == 0)
        {
            displayInterstitial();
        }*/
    }

    @Override
    public void onAnswerVerified(String answer) {
        level = levelUp();

        int newLevel = level+1;
        if(newLevel<=getResources().getInteger(R.integer.max_level)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("last_state");

            editor.apply();
        }

        correctFrag = AnswerCorrectFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, correctFrag)
                .commit();



    }

    private int levelUp() {
        int level = sharedPreferences.getInt("level",1);
        int newLevel = level+1;

        if(newLevel<=getResources().getInteger(R.integer.max_level) ) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("level", newLevel);
            editor.apply();

            levelView.setText(Integer.toString(newLevel));
            WordInterface.incrementLevel();

            increasePattyCount(getResources().getInteger(R.integer.increase_amount));

            return newLevel;
        }
        else
        {
            showCompletionScreen();
            return newLevel -1;
        }
    }


    private void showCompletionScreen() {
        CompleteDialogFragment dialog = new CompleteDialogFragment();
        dialog.show(getSupportFragmentManager(),"dialog");
    }

    @Override
    public void getSkips() {

    }
}
