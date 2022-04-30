package com.mainapp.instagramclone.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.mainapp.instagramclone.R;

import java.util.Objects;

public class ConfirmPasswordDialog extends DialogFragment {
    private static final String TAG = "ConfirmPasswordDialog";

    public interface OnConfirmPasswordListener {
        public void onConfirmPassword(String password);
    }

    TextView mPassword;
    OnConfirmPasswordListener onConfirmPasswordListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        mPassword = (TextView) view.findViewById(R.id.confirm_password);

        Log.d(TAG, "onCreateView: started.");

        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: captured password and confirming.");
            String password = mPassword.getText().toString();
            if (!password.equals("")) {
                onConfirmPasswordListener.onConfirmPassword(password);
                getDialog().dismiss();
            }
            else
                Toast.makeText(getActivity(), "You must enter a password", Toast.LENGTH_SHORT).show();
        });

        TextView cancelDialog = (TextView) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: closing the dialog.");
            Objects.requireNonNull(getDialog()).dismiss();
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            onConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        } catch (ClassCastException exception) {
            Log.d(TAG, "onAttach: ClassCastException: " + exception.getMessage());
        }
    }
}
