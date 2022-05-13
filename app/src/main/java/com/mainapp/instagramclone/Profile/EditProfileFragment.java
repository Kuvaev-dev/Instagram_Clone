// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Dialogs.ConfirmPasswordDialog;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Share.ShareActivity;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;
import com.microprogramer.library.CircularImageView;

import java.util.Objects;

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.onConfirmPasswordListener {
    private static final String TAG = "EditProfileFragment";

    private EditText editDisplayName, editUsername, editWebsite, editDescription, editEmail, editPhoneNumber;
    private CircularImageView profilePhoto;
    private TextView changeProfilePhoto;

    private UserSettings mUserSettings;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    @Override
    public void onConfirmPassword(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(
                Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail()), password);

        auth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: user re-authenticated.");
                        auth.fetchSignInMethodsForEmail(editEmail.getText().toString()).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                try {
                                    if (Objects.requireNonNull(task1.getResult().getSignInMethods()).size() == 1) {
                                        Log.d(TAG, "onConfirmPassword: that's email already in use.");
                                        Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "onConfirmPassword: that's email is available.");
                                        auth.getCurrentUser().updateEmail(editEmail.getText().toString()).addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                Log.d(TAG, "onConfirmPassword: user email address updated.");
                                                Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                                firebaseMethods.updateEmail(editEmail.getText().toString());
                                            }
                                        });
                                    }
                                } catch (NullPointerException exception) {
                                    Log.e(TAG, "onConfirmPassword: NullPointerException: " + exception.getMessage());
                                }
                            }
                        });
                    }
                    else
                        Log.d(TAG, "onConfirmPassword: user re-authentication failed.");
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) throws NullPointerException {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profilePhoto = view.findViewById(R.id.profile_photo);
        editDisplayName = view.findViewById(R.id.display_name);
        editUsername = view.findViewById(R.id.username);
        editWebsite = view.findViewById(R.id.website);
        editDescription = view.findViewById(R.id.description);
        editEmail = view.findViewById(R.id.email);
        editPhoneNumber = view.findViewById(R.id.phoneNumber);
        changeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        firebaseMethods = new FirebaseMethods(getActivity());

        // setProfileImage();
        setupFirebaseAuth();

        // Navigating arrow to ProfileActivity
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(view1 -> {
            Log.d(TAG, "onClick: navigating back to ProfileActivity");
            getActivity().finish();
        });

        ImageView checkmark = view.findViewById(R.id.saveChanges);
        checkmark.setOnClickListener(view12 -> {
            Log.d(TAG, "onCreateView: attempting to save changes.");
            saveProfileSettings();
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

        // If the user made a change to their username
        if (!mUserSettings.getUser().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }

        // If the user made a change to their email
        if (!mUserSettings.getUser().getEmail().equals(email)) {
            // 1. Re-authenticate
            //                  - confirm the password and email
            ConfirmPasswordDialog confirmPasswordDialog = new ConfirmPasswordDialog();
            assert getFragmentManager() != null;
            confirmPasswordDialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));
            confirmPasswordDialog.setTargetFragment(EditProfileFragment.this, 1);

            // 2. Check if the email already registered
            //                  - fetchProvidersForEmail(String email)
            // 3. Change the email
            //                  - submit the new email to the database and authentication
        }

        // Check if setting is not equals db data and update user_account_settings
        if (!mUserSettings.getUserAccountSettings().getDisplay_name().equals(displayName))
            firebaseMethods.updateUserAccountSettings(displayName, null, null, 0);

        if (!mUserSettings.getUserAccountSettings().getWebsite().equals(website))
            firebaseMethods.updateUserAccountSettings(null, website, null, 0);

        if (!mUserSettings.getUserAccountSettings().getDescription().equals(description))
            firebaseMethods.updateUserAccountSettings(displayName, null, description, 0);

        if (mUserSettings.getUserAccountSettings().getPhone_number() != phoneNumber)
            firebaseMethods.updateUserAccountSettings(displayName, null, null, phoneNumber);
    }

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists.");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Add the username
                    firebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved username.", Toast.LENGTH_SHORT).show();
                }

                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: FOUND A MATCH: " + Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                        Toast.makeText(getActivity(), "The username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from database: " + userSettings.toString());

        mUserSettings = userSettings;
        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profilePhoto, null, "");
        editDisplayName.setText(userAccountSettings.getDisplay_name());
        editUsername.setText(userAccountSettings.getUsername());
        editWebsite.setText(userAccountSettings.getWebsite());
        editDescription.setText(userAccountSettings.getDescription());
        editEmail.setText(userSettings.getUser().getEmail());
        editPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        changeProfilePhoto.setOnClickListener(view -> {
            Log.d(TAG, "onClick: changing profile photo.");
            Intent intent = new Intent(getActivity(), ShareActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getActivity().startActivity(intent);
            getActivity().finish();
        });
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
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
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
                setupProfileWidgets(firebaseMethods.getUserAccountSettings(snapshot));
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
