package com.javon.cdmk;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //loadAd();

        Typeface font = Typeface.createFromAsset(getAssets(), "LuckiestGuy.ttf");
        ((TextView) findViewById(R.id.splashView)).setTypeface(font);

    }


    public void play(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void showInstructions(View view)
    {
        InstructionDialogFragment dialog = new InstructionDialogFragment();
        dialog.show(getSupportFragmentManager(),"Instructions");
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (mAdView != null) {
            mAdView.resume();
        }*/
    }

    @Override
    protected void onPause() {
        //mAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        //mAdView.destroy();
        super.onDestroy();
    }
}
