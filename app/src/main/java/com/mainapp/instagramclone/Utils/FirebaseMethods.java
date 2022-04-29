package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.R;

import java.util.Objects;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    private final FirebaseAuth auth;
    private final Context mContext;
    private final DatabaseReference databaseReference;
    private String userId;

    public FirebaseMethods(Context context) {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mContext = context;

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
        Log.d(TAG, "checkIfUsernameExists: check if " + username + " already exists.");
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.child(userId).getChildren()) {
            Log.d(TAG, "checkIfUsernameExists: data snapshot: " + ds);
            user.setUsername(Objects.requireNonNull(ds.getValue(User.class)).getUsername());
            Log.d(TAG, "checkIfUsernameExists: username: " + user.getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
                Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + user.getUsername());
                return true;
            }
        }
        return false;
    }

    public void registerNewEmail(final String email, String password, final String username) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "onComplete: " + task.isSuccessful());
                    if (!task.isSuccessful())
                        Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                    else if (task.isSuccessful()) {
                        sendVerificationEmail();
                        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        Log.d(TAG, "onComplete: authstate changed " + userId);
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(mContext, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addNewUser(String email, String username, String description, String website, String profile_photo) {
        User user = new User(userId, 1, email, StringManipulation.condenseUsername(username));
        databaseReference.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .setValue(user);

        UserAccountSettings userAccountSettings = new UserAccountSettings(
                description, username, 0, 0, 0, profile_photo, StringManipulation.condenseUsername(username), website
        );

        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .setValue(userAccountSettings);
    }

    public UserSettings getUserAccountSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from the firebase.");
        UserAccountSettings userAccountSettings = new UserAccountSettings();
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {
            // user_account_settings node
            if (Objects.equals(ds.getKey(), mContext.getString(R.string.dbname_user_account_settings))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);
                try {
                    userAccountSettings.setDisplay_name(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getDisplay_name()
                    );
                    userAccountSettings.setUsername(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getUsername()
                    );
                    userAccountSettings.setWebsite(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getWebsite()
                    );
                    userAccountSettings.setDescription(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getDescription()
                    );
                    userAccountSettings.setProfile_photo(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getProfile_photo()
                    );
                    userAccountSettings.setPosts(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getPosts()
                    );
                    userAccountSettings.setFollowers(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getFollowers()
                    );
                    userAccountSettings.setFollowing(
                            Objects.requireNonNull(ds.child(userId)
                                    .getValue(UserAccountSettings.class))
                                    .getFollowing()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + userAccountSettings.toString());
                } catch (NullPointerException exception) {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException: " + exception.getMessage());
                }
            }

            // users node
            if (Objects.equals(ds.getKey(), mContext.getString(R.string.dbname_users))) {
                Log.d(TAG, "getUserAccountSettings: data snapshot: " + ds);
                user.setUsername(
                        Objects.requireNonNull(ds.child(userId)
                                .getValue(User.class))
                                .getUsername()
                );
                user.setEmail(
                        Objects.requireNonNull(ds.child(userId)
                                .getValue(User.class))
                                .getEmail()
                );
                user.setPhone_number(
                        Objects.requireNonNull(ds.child(userId)
                                .getValue(User.class))
                                .getPhone_number()
                );
                user.setUser_id(
                        Objects.requireNonNull(ds.child(userId)
                                .getValue(User.class))
                                .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved user information: " + user.toString());
            }
        }
        return new UserSettings(user, userAccountSettings);
    }
}
