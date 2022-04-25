package com.mainapp.instagramclone.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private ImageView profilePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        profilePhoto = view.findViewById(R.id.profile_photo);

        setProfileImage();

        // Navigating arrow to ProfileActivity
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigating back to ProfileActivity");
                getActivity().finish();
            }
        });

        return view;
    }

    private void setProfileImage() {
        Log.d(TAG, "setProfileImage: setting profile image.");
        String imgURL = "image.winudf.com/v2/image1/Y29tLmdvb2dsZS5zYW1wbGVzLmFwcHMuYWRzc2NoZWRfaWNvbl8xNTcwNzg3MzQ0XzA0Ng/icon.png?fakeurl=1&h=240&type=webp";
        UniversalImageLoader.setImage(imgURL, profilePhoto, null, "https://");
    }
}
