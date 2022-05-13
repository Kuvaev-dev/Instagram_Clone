// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText mCaption;
    private Intent intent;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    private int imageCount = 0;
    private String imgURL;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        firebaseMethods = new FirebaseMethods(NextActivity.this);
        mCaption = findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: closing the gallery fragment.");
            finish();
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(view2 -> {
            Log.d(TAG, "onCreateView: navigating to the final share screen.");
            Toast.makeText(NextActivity.this, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
            String caption = mCaption.getText().toString();

            if (intent.hasExtra(getString(R.string.selected_image))) {
                imgURL = intent.getStringExtra(getString(R.string.selected_image));
                firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, imgURL, null);
            } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
                bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                firebaseMethods.uploadNewPhoto(getString(R.string.new_photo), caption, imageCount, null, bitmap);
            }
        });

        setImage();
    }

    private void setImage() {
        intent = getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if (intent.hasExtra(getString(R.string.selected_image))) {
            String mAppend = "file:/";
            imgURL = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new image URL: " + imgURL);
            UniversalImageLoader.setImage(imgURL, image, null, mAppend);
        } else if (intent.hasExtra(getString(R.string.selected_bitmap))) {
            bitmap = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap.");
            image.setImageBitmap(bitmap);
        }
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
