package com.mainapp.instagramclone.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mainapp.instagramclone.Home.HomeActivity;
import com.mainapp.instagramclone.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ProgressBar progressBar;
    private EditText editEmail, editPassword;
    private TextView authInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        authInfo = (TextView) findViewById(R.id.pleaseWait);
        editEmail = (EditText) findViewById(R.id.input_email);
        editPassword = (EditText) findViewById(R.id.input_password);
        Log.d(TAG, "onCreate: started.");

        authInfo.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }

    private boolean isStringNull(String string) {
        Log.d(TAG, "isStringNull: checking string if null.");
        return string.equals("");
    }

    // Firebase BEGINNING

    private void init() {
        // Init the button for login
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(view -> {
            Log.d(TAG, "onClick: attempting to login");
            String email = editEmail.getText().toString();
            String password = editPassword.getText().toString();

            if (isStringNull(email) || isStringNull(password))
                Toast.makeText(LoginActivity.this, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
            else {
                progressBar.setVisibility(View.VISIBLE);
                authInfo.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        Log.d(TAG, "onComplete: " + task.isSuccessful());
                        FirebaseUser user = auth.getCurrentUser();

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "onComplete: failed", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to Authenticate", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                assert user != null;
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "onClick: success. Email is verified.");
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Email is not verified.\nCheck your email inbox.", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    authInfo.setVisibility(View.GONE);
                                    auth.signOut();
                                }
                            } catch (NullPointerException exception) {
                                Log.d(TAG, "onClick: NullPointerException: " + exception.getMessage());
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        authInfo.setVisibility(View.GONE);
                    });
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(view -> {
            Log.d(TAG, "onClick: navigating to register string");
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        /*
         *  If user logged in then navigate to Home and call 'finish()'
         */
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null)
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
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
