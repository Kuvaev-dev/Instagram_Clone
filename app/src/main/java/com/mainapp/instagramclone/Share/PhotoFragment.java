package com.mainapp.instagramclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mainapp.instagramclone.Profile.AccountSettingsActivity;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.Permissions;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started.");
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Button btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: launching camera.");
            if (((ShareActivity) getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {
                if (((ShareActivity) getActivity()).checkPermissions(Permissions.PERMISSIONS[2])) {
                    Log.d(TAG, "onCreateView: starting camera.");
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(getActivity(), ShareActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    private boolean isRootTask() {
        return ((ShareActivity) getActivity()).getTask() == 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");
        }

        assert data != null;
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

        if (isRootTask()) {
            try {
                Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                Intent intent = new Intent(getActivity(), NextActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                startActivity(intent);
            } catch (NullPointerException exception) {
                Log.e(TAG, "onActivityResult: NullPointerException: " + exception.getMessage());
            }
        } else {
            try {
                Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                startActivity(intent);
                getActivity().finish();
            } catch (NullPointerException exception) {
                Log.e(TAG, "onActivityResult: NullPointerException: " + exception.getMessage());
            }
        }
    }
}
