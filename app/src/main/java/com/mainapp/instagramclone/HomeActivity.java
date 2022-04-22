package com.mainapp.instagramclone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        setupBottomNavigationView();
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
    }
}