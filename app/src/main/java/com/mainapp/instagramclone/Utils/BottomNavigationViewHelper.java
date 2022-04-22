package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.HomeActivity;
import com.mainapp.instagramclone.LikesActivity;
import com.mainapp.instagramclone.ProfileActivity;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.SearchActivity;
import com.mainapp.instagramclone.ShareActivity;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_house:
                    Intent intent1 = new Intent(context, HomeActivity.class);
                    context.startActivity(intent1);
                    break;
                case R.id.ic_search:
                    Intent intent2 = new Intent(context, SearchActivity.class);
                    context.startActivity(intent2);
                    break;
                case R.id.ic_circle:
                    Intent intent3 = new Intent(context, ShareActivity.class);
                    context.startActivity(intent3);
                    break;
                case R.id.ic_alert:
                    Intent intent4 = new Intent(context, LikesActivity.class);
                    context.startActivity(intent4);
                    break;
                case R.id.ic_android:
                    Intent intent5 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent5);
                    break;
            }

            return false;
        });
    }
}
