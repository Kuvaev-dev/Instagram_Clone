package com.mainapp.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;
import com.microprogramer.library.CircularImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int ACTIVITY_NUM = 4;

    private Toolbar toolbar;
    private ImageView profileMenu;
    private Context context;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView tDisplayName;
    private TextView tUsername;
    private TextView tWebsite;
    private TextView tDescription;
    private TextView tPosts;
    private TextView tFollowers;
    private TextView tFollowing;
    private CircularImageView profilePhoto;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tDisplayName = (TextView) view.findViewById(R.id.display_name);
        tUsername = (TextView) view.findViewById(R.id.username);
        tWebsite = (TextView) view.findViewById(R.id.website);
        tDescription = (TextView) view.findViewById(R.id.description);
        tPosts = (TextView) view.findViewById(R.id.tvPosts);
        tFollowers = (TextView) view.findViewById(R.id.tvFollowers);
        tFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        profilePhoto = (CircularImageView) view.findViewById(R.id.profile_photo);
        progressBar = (ProgressBar) view.findViewById(R.id.profileProgressBar);
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        toolbar = (Toolbar) view.findViewById(R.id.profileToolBar);
        profileMenu = (ImageView) view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottomNavViewBar);
        context = getActivity();
        firebaseMethods = new FirebaseMethods(getActivity());

        setupBottomNavigationView();
        setupToolBar();
        setupFirebaseAuth();

        TextView editProfile = (TextView) view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: navigating to " + context.getString(R.string.edit_profile_fragment));
            Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
            startActivity(intent);
        });

        return view;
    }

    private void setProfileWidgets(UserSettings userSettings) {
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from database: " + userSettings.toString());
        //User user = userSettings.getUser();
        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profilePhoto, null, "");
        tDisplayName.setText(userAccountSettings.getDisplay_name());
        tUsername.setText(userAccountSettings.getUsername());
        tWebsite.setText(userAccountSettings.getWebsite());
        tDescription.setText(userAccountSettings.getDescription());
        tPosts.setText(String.valueOf(userAccountSettings.getPosts()));
        tFollowers.setText(String.valueOf(userAccountSettings.getFollowers()));
        tFollowing.setText(String.valueOf(userAccountSettings.getFollowing()));
        progressBar.setVisibility(View.GONE);

    }

    private void setupToolBar() {
        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(view -> {
            Log.d(TAG, "onClick: navigating to account settings");
            Intent intent = new Intent(context, AccountSettingsActivity.class);
            startActivity(intent);
        });
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    // Firebase BEGINNING

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null)
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
            else
                Log.d(TAG, "onAuthStateChanged: signed out.");
        };

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve user information from the database
                setProfileWidgets(firebaseMethods.getUserAccountSettings(snapshot));
                // Retrieve images for the user in question
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    // Firebase END
}
