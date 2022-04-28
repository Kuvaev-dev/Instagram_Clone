package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.mainapp.instagramclone.Models.User;

import java.util.Objects;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    private final FirebaseAuth auth;
    private final Context mContext;
    private String userId;

    public FirebaseMethods(Context context) {
        auth = FirebaseAuth.getInstance();
        mContext = context;

        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot) {
        Log.d(TAG, "checkIfUsernameExists: check if " + username + " already exists.");
        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()) {
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
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: " + task.isSuccessful());
                        if (!task.isSuccessful())
                            Toast.makeText(mContext, "", Toast.LENGTH_SHORT).show();
                        else if (task.isSuccessful()) {
                            userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                            Log.d(TAG, "onComplete: authstate changed " + userId);
                        }
                    }
                });
    }
}
