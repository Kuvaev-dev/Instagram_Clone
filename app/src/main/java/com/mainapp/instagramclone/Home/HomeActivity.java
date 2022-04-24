package com.mainapp.instagramclone.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");

        setupBottomNavigationView();
        setupViewPager();
    }

    /*
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager() {
        SectionPagerAdapter sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        sectionPagerAdapter.addFragment(new CameraFragment());
        sectionPagerAdapter.addFragment(new HomeFragment());
        sectionPagerAdapter.addFragment(new MessagesFragment());

        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_camera);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_action_menu);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.ic_arrow);
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(HomeActivity.this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}