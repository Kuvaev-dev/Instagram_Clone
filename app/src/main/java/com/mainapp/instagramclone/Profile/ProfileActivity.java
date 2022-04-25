package com.mainapp.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.GridImageAdapter;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private ImageView profilePhoto;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        setupBottomNavigationView();
        setupToolBar();
        setupActivityWidgets();
        setProfileImage();
        tempGridSetup();
    }

    private void tempGridSetup() {
        ArrayList<String> imgURLs = new ArrayList<>();

        // Test images
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");
        imgURLs.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1");

        setupGridImage(imgURLs);
    }

    private void setupGridImage(ArrayList<String> imgURLs) {
        GridView gridView = findViewById(R.id.gridView);
        GridImageAdapter adapter = new GridImageAdapter(ProfileActivity.this, R.layout.layout_grid_imageview, " ", imgURLs);
        gridView.setAdapter(adapter);
    }

    private void setProfileImage() {
        Log.d(TAG, "setProfileImage: setting profile photo.");
        String imgURL = "image.winudf.com/v2/image1/Y29tLmdvb2dsZS5zYW1wbGVzLmFwcHMuYWRzc2NoZWRfaWNvbl8xNTcwNzg3MzQ0XzA0Ng/icon.png?fakeurl=1&h=240&type=webp";
        UniversalImageLoader.setImage(imgURL, profilePhoto, mProgressBar, "https://");
    }

    private void setupActivityWidgets() {
        mProgressBar = findViewById(R.id.profileProgressBar);
        mProgressBar.setVisibility(View.GONE);
        profilePhoto = findViewById(R.id.profile_photo);
    }

    private void setupToolBar() {
        Toolbar toolbar = findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating to account settings");
                Intent intent = new Intent(ProfileActivity.this, AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(ProfileActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
