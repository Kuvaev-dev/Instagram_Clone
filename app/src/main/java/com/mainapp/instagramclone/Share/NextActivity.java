package com.mainapp.instagramclone.Share;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    private final String mAppend = "file:/";
    private int imageCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        firebaseMethods = new FirebaseMethods(NextActivity.this);

        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: closing the gallery fragment.");
            finish();
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(view2 -> {
            Log.d(TAG, "onCreateView: navigating to the final share screen.");

        });

        setImage();
    }

    private void uploadPhoto() {
        /*
            1. Step 1 - Create a data models for photos.
            2. Step 2 - Add properties to the photo objects: caption, date, imageURL, photo_id, tags, user_id.
            3. Step 3 - Count the number of photos that the usr already has.
            4. Step 4:
                a). Upload the photo to Firebase storage.
                b). Insert into 'photos' node.
                c). Insert into 'user_photos' node.
         */
    }

    private void setImage() {
        Intent intent = getIntent();
        ImageView imageView = findViewById(R.id.imageShare);
        UniversalImageLoader.setImage(intent.getStringExtra(getString(R.string.selected_image)), imageView, null, mAppend);
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
        Log.d(TAG, "onDataChange: image count: " + imageCount);
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
                imageCount = firebaseMethods.getImageCount(snapshot);
                Log.d(TAG, "onDataChange: image count: " + imageCount);
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
