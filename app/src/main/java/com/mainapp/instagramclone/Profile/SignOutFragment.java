package com.mainapp.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mainapp.instagramclone.Login.LoginActivity;
import com.mainapp.instagramclone.R;

public class SignOutFragment extends Fragment {
    private static final String TAG = "SignOutFragment";

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ProgressBar progressBar;
    private TextView tvSignOut, tvSigningOut;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_out, container, false);
        tvSignOut = view.findViewById(R.id.tvConfirmSignOut);
        progressBar = view.findViewById(R.id.progressbar);
        tvSigningOut = view.findViewById(R.id.tvSigningOut);
        Button buttonConfirmSignOut = view.findViewById(R.id.btnConfirmSignOut);
        progressBar.setVisibility(View.GONE);
        tvSigningOut.setVisibility(View.GONE);

        setupFirebaseAuth();

        buttonConfirmSignOut.setOnClickListener(view1 -> {
            Log.d(TAG, "onClick: attempting to sign out.");
            progressBar.setVisibility(View.VISIBLE);
            tvSigningOut.setVisibility(View.VISIBLE);
            auth.signOut();
            getActivity().finish();
        });
        return view;
    }

    // Firebase BEGINNING

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
            else {
                Log.d(TAG, "onAuthStateChanged: signed out.");
                Log.d(TAG, "onAuthStateChanged: navigating back to login screen.");
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        };
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
