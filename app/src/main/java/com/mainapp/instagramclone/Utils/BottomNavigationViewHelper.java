// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.Home.HomeActivity;
import com.mainapp.instagramclone.Likes.LikesActivity;
import com.mainapp.instagramclone.Profile.ProfileActivity;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Search.SearchActivity;
import com.mainapp.instagramclone.Share.ShareActivity;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    @SuppressLint("NonConstantResourceId")
    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.ic_house:
                    Intent intent1 = new Intent(context, HomeActivity.class);
                    context.startActivity(intent1);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
                case R.id.ic_search:
                    Intent intent2 = new Intent(context, SearchActivity.class);
                    context.startActivity(intent2);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
                case R.id.ic_circle:
                    Intent intent3 = new Intent(context, ShareActivity.class);
                    context.startActivity(intent3);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
                case R.id.ic_alert:
                    Intent intent4 = new Intent(context, LikesActivity.class);
                    context.startActivity(intent4);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
                case R.id.ic_android:
                    Intent intent5 = new Intent(context, ProfileActivity.class);
                    context.startActivity(intent5);
                    callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    break;
            }
            return false;
        });
    }
}
