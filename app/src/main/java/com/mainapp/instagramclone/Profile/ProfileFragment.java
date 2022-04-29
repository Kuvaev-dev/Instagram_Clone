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
import com.mainapp.instagramclone.Login.LoginActivity;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.microprogramer.library.CircularImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int ACTIVITY_NUM = 4;

    private Toolbar toolbar;
    private ImageView profileMenu;
    private Context context;
    private BottomNavigationViewEx bottomNavigationViewEx;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView tDisplayName = view.findViewById(R.id.display_name);
        TextView tUsername = view.findViewById(R.id.username);
        TextView tWebsite = view.findViewById(R.id.website);
        TextView tDescription = view.findViewById(R.id.description);
        TextView tPosts = view.findViewById(R.id.tvPosts);
        TextView tFollowers = view.findViewById(R.id.tvFollowers);
        TextView tFollowing = view.findViewById(R.id.tvFollowing);
        CircularImageView profilePhoto = view.findViewById(R.id.profile_photo);
        ProgressBar progressBar = view.findViewById(R.id.profileProgressBar);
        GridView gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolBar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        context = getActivity();

        setupBottomNavigationView();
        setupToolBar();

        return view;
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
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
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
