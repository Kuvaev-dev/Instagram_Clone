package com.mainapp.instagramclone.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private String email, username, password;
    private EditText editEmail, editUsername, editPassword;
    private Button regBtn;
    private TextView authInfo;
    private Context context;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference databaseReference;

    private String append = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(context);
        Log.d(TAG, "onCreate: started.");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init() {
        regBtn.setOnClickListener(view -> {
            email = editEmail.getText().toString();
            username = editUsername.getText().toString();
            password = editPassword.getText().toString();

            if (checkInputs(email, username, password)) {
                progressBar.setVisibility(View.VISIBLE);
                authInfo.setVisibility(View.VISIBLE);

                firebaseMethods.registerNewEmail(email, password, username);
            }
        });
    }

    private boolean checkInputs(String email, String username, String password) {
        Log.d(TAG, "checkInputs: checking inputs for null values.");
        if (email.equals("") || username.equals("") || password.equals("")) {
            Toast.makeText(context, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets: initializing widgets.");
        editEmail = findViewById(R.id.input_email);
        editUsername = findViewById(R.id.input_username);
        progressBar = findViewById(R.id.progressbar);
        authInfo = findViewById(R.id.pleaseWait);
        editPassword = findViewById(R.id.input_password);
        regBtn = findViewById(R.id.btn_register);
        context = RegisterActivity.this;
        progressBar.setVisibility(View.GONE);
        authInfo.setVisibility(View.GONE);
    }

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Make sure the username is not already in use
                        if (firebaseMethods.checkIfUsernameExists(username, snapshot)) {
                            append = Objects.requireNonNull(databaseReference.push().getKey()).substring(3, 10);
                            Log.d(TAG, "onDataChange: username already exists. Appending random string to name " + append);
                        }

                        username = username + append;
                        // Add new user to the database
                        firebaseMethods.addNewUser(email, username, "", "", "");
                        Toast.makeText(context, "Signup successful. Sending verification email.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                finish();
            }
            else
                Log.d(TAG, "onAuthStateChanged: signed out.");
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null)
            auth.removeAuthStateListener(authStateListener);
    }

    // Firebase END
}
