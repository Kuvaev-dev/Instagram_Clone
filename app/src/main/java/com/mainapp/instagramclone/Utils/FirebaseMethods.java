// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mainapp.instagramclone.Home.HomeActivity;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.Profile.AccountSettingsActivity;
import com.mainapp.instagramclone.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    // Firebase
    private final FirebaseAuth auth;
    private final Context mContext;
    private final DatabaseReference databaseReference;
    private final StorageReference mStorageReference;
    private String userId;

    private double photoUploadProgress = 0;

    public FirebaseMethods(Context context) {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, String caption, int count, String imgURL, Bitmap bm) {
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");
        FilePaths filePaths = new FilePaths();
        // If new photo
        if (photoType.equals(mContext.getString(R.string.new_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new photo.");
            String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));

            // Convert image URL to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgURL);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> firebaseUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                firebaseUrl.addOnSuccessListener(uri -> {
                    Toast.makeText(mContext, "Photo upload success.", Toast.LENGTH_SHORT).show();
                    addPhotoToDatabase(caption, uri.toString());
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);
                });
            }).addOnFailureListener(exception -> {
                Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "uploadNewPhoto: photo upload failed.");
            }).addOnProgressListener(snapshot -> {
                double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                if (progress - 15 > photoUploadProgress) {
                    Toast.makeText(mContext, "Photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    photoUploadProgress = progress;
                }

                Log.d(TAG, "uploadNewPhoto: upload progress: " + progress + "% done.");
            });
        }
        // If new profile photo
        else if (photoType.equals(mContext.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadNewPhoto: uploading new profile photo.");

            String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            // Convert image URL to bitmap
            if (bm == null) {
                bm = ImageManager.getBitmap(imgURL);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);
            UploadTask uploadTask;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                Task<Uri> firebaseUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                firebaseUrl.addOnSuccessListener(uri -> {
                    Toast.makeText(mContext, "Photo upload success.", Toast.LENGTH_SHORT).show();
                    setProfilePhoto(uri.toString());

                    ((AccountSettingsActivity) mContext).setupViewPager(
                            ((AccountSettingsActivity) mContext).sectionStatePagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );
                });
            }).addOnFailureListener(exception -> {
                Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "uploadNewPhoto: photo upload failed.");
            }).addOnProgressListener(snapshot -> {
                double progress = (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();

                if (progress - 15 > photoUploadProgress) {
                    Toast.makeText(mContext, "Photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                    photoUploadProgress = progress;
                }

                Log.d(TAG, "uploadNewPhoto: upload progress: " + progress + "% done.");
            });
        }
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);
        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                         .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                         .child(mContext.getString(R.string.profile_photo))
                         .setValue(url);
    }

    private String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Uzhgorod"));
        return simpleDateFormat.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String imgURL) {
        Log.d(TAG, "addPhotoToDatabase: adding photo to database.");
        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = databaseReference.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(imgURL);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);

        assert newPhotoKey != null;
        databaseReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);
        databaseReference.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
    }

    public int getImageCount(DataSnapshot dataSnapshot) {
        int count = 0;
        for (DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()) {
            count++;
        }
        return count;
    }

    public void updateUserAccountSettings(String display_name, String website, String description, long phone_number) {
        Log.d(TAG, "updateUserAccountSettings: updating user account settings.");

        if (display_name != null)
            databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(display_name);

        if (website != null)
            databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);

        if (description != null)
            databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);

        if (phone_number != 0)
            databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phone_number);
    }

    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: updating username to: " + username);
        databaseReference.child(mContext.getString(R.string.dbname_users))
                         .child(userId)
                         .child(mContext.getString(R.string.field_username))
                         .setValue(username);

        databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                         .child(userId)
                         .child(mContext.getString(R.string.field_username))
                         .setValue(username);
    }

    public void updateEmail(String email) {
        Log.d(TAG, "updateUsername: updating email to: " + email);
        databaseReference.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);
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
                        Log.d(TAG, "onComplete: auth state changed " + userId);
                    }
                });
    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Nothing to do
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
                description, username, 0, 0, 0, profile_photo, StringManipulation.condenseUsername(username), website, userId
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
                Log.d(TAG, "getUserAccountSettings: data snapshot: " + ds);
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

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + userAccountSettings);
                } catch (NullPointerException exception) {
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + exception.getMessage());
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

                Log.d(TAG, "getUserAccountSettings: retrieved user information: " + user);
            }
        }
        return new UserSettings(user, userAccountSettings);
    }
}
