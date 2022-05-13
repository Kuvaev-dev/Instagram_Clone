package com.mainapp.instagramclone.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.Login.LoginActivity;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.MainfeedListAdapter;
import com.mainapp.instagramclone.Utils.SectionPagerAdapter;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;
import com.mainapp.instagramclone.Utils.ViewCommentsFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements
        MainfeedListAdapter.onLoadMoreItemsListener {
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    private final Context context = HomeActivity.this;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");
        mViewPager = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.container);
        mRelLayout = findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
    }

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos.");
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container
                        + ":" + mViewPager.getCurrentItem());
        if (fragment != null) {
            fragment.displayMorePhotos();
        }
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity) {
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread.");
        ViewCommentsFragment viewCommentsFragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        viewCommentsFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, viewCommentsFragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(context);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /*
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager() {
        SectionPagerAdapter sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        sectionPagerAdapter.addFragment(new CameraFragment());
        sectionPagerAdapter.addFragment(new HomeFragment());
        sectionPagerAdapter.addFragment(new MessagesFragment());

        mViewPager.setAdapter(sectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setIcon(R.drawable.ic_camera);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setIcon(R.drawable.ic_action_menu);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setIcon(R.drawable.ic_arrow);
    }

    public void hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout.");
        mRelLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout() {
        Log.d(TAG, "hideLayout: showing layout.");
        mRelLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mFrameLayout.getVisibility() == View.VISIBLE) {
            showLayout();
        }
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView.");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    // Firebase BEGINNING

    /*
     *  Checks if user is logged in
     */
    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: check if user is logged in.");
        if (user == null) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
        }
    }

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            // Check if user is logged in
            checkCurrentUser(user);

            if (user != null)
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
            else
                Log.d(TAG, "onAuthStateChanged: signed out.");
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrentUser(auth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    // Firebase END
}