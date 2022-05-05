package com.mainapp.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.SectionStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    private static final int ACTIVITY_NUM = 4;

    private Context context;
    private SectionStatePagerAdapter sectionStatePagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        context = AccountSettingsActivity.this;
        Log.d(TAG, "onCreate: started.");
        mViewPager = findViewById(R.id.container);
        mRelativeLayout = findViewById(R.id.relLayout1);

        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view -> {
            Log.d(TAG, "onClick: navigating to 'ProfileActivity'");
            finish();
        });
    }

    private void getIncomingIntent() {
        Intent intent = getIntent();

        // If there is an imageURL attached as an extra, than it was chosen from the gallery/photo fragment
        if (intent.hasExtra(getString(R.string.selected_image))) {
            Log.d(TAG, "getIncomingIntent: new incoming image URL.");
            if (intent.getStringExtra(getString(R.string.return_to_fragment))
                      .equals(getString(R.string.edit_profile_fragment))) {
                // Set the new profile image
                FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0,
                        intent.getStringExtra(getString(R.string.selected_image)));
            }
        }

        if (intent.hasExtra(getString(R.string.calling_activity))) {
            Log.d(TAG, "getIncomingIntent: received incoming intent " + getString(R.string.profile_activity));
            setupViewPager(sectionStatePagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragments() {
        sectionStatePagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        sectionStatePagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        sectionStatePagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));
    }

    private void setupViewPager(int fragmentNumber) {
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setupViewPager: navigating to fragment # " + fragmentNumber);
        mViewPager.setAdapter(sectionStatePagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupSettingsList() {
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' list.");
        ListView listView = findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out));

        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            Log.d(TAG, "onItemClick: navigating to fragment " + position);
            setupViewPager(position);
        });
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
