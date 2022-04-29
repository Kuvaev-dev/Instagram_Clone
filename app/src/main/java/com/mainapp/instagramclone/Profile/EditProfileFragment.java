package com.mainapp.instagramclone.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;
import com.microprogramer.library.CircularImageView;

import java.util.Objects;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";

    private EditText editDisplayName, editUsername, editWebsite, editDescription, editEmail, editPhoneNumber;
    private TextView changeProfilePhoto;
    private CircularImageView profilePhoto;
    private String userId;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) throws NullPointerException {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profilePhoto = (CircularImageView) view.findViewById(R.id.profile_photo);
        editDisplayName = (EditText) view.findViewById(R.id.display_name);
        editUsername = (EditText) view.findViewById(R.id.username);
        editWebsite = (EditText) view.findViewById(R.id.website);
        editDescription = (EditText) view.findViewById(R.id.description);
        editEmail = (EditText) view.findViewById(R.id.email);
        editPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        changeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        firebaseMethods = new FirebaseMethods(getActivity());

        // setProfileImage();
        setupFirebaseAuth();

        // Navigating arrow to ProfileActivity
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view1 -> {
            Log.d(TAG, "onClick: navigating back to ProfileActivity");
            getActivity().finish();
        });
        return view;
    }

    private void saveProfileSettings() {
        final String displayName = editDisplayName.getText().toString();
        final String username = editUsername.getText().toString();
        final String website = editWebsite.getText().toString();
        final String description = editDescription.getText().toString();
        final String email = editEmail.getText().toString();
        final long phoneNumber = Long.parseLong(editPhoneNumber.getText().toString());

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User();
                for (DataSnapshot ds: snapshot.child(getString(R.string.dbname_users)).getChildren()) {
                    if (ds.getKey().equals(userId)) {
                        user.setUsername(Objects.requireNonNull(ds.getValue(User.class)).getUsername());
                    }
                }
                Log.d(TAG, "onDataChange: CURRENT USERNAME: " + user.getUsername());

                if (user.getUsername().equals(username)) {

                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from database: " + userSettings.toString());
        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profilePhoto, null, "");
        editDisplayName.setText(userAccountSettings.getDisplay_name());
        editUsername.setText(userAccountSettings.getUsername());
        editWebsite.setText(userAccountSettings.getWebsite());
        editDescription.setText(userAccountSettings.getDescription());
        editEmail.setText(userSettings.getUser().getEmail());
        editPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));
    }

    // Firebase BEGINNING

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
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
