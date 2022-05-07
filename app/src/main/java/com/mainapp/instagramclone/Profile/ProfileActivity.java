package com.mainapp.instagramclone.Profile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Post.ViewPostFragment;
import com.mainapp.instagramclone.R;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.onGridImageSelectedListener {
    private static final String TAG = "ProfileActivity";
    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private ImageView profilePhoto;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: started.");

        init();
    }

    private void init() {
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));
        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, profileFragment);
        transaction.addToBackStack(getString(R.string.profile_fragment));
        transaction.commit();
    }

    @Override
    public void onGridImageSelected(Photo photo, int activity_number) {
        Log.d(TAG, "onGridImageSelected: selected an image grid view: " + photo.toString());
        ViewPostFragment fragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putInt(getString(R.string.activity_number), activity_number);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }
}
