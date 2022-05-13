// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.microprogramer.library.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;
import java.util.Objects;

public class UserListAdapter extends ArrayAdapter<User> {
    private static final String TAG = "UserListAdapter";

    private final LayoutInflater mInflater;
    private final int layoutResource;
    private final Context mContext;

    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<User> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
    }

    private static class ViewHolder {
        TextView username, email;
        CircularImageView profileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.username = convertView.findViewById(R.id.username);
            viewHolder.email = convertView.findViewById(R.id.email);
            viewHolder.profileImage = convertView.findViewById(R.id.profile_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.username.setText(getItem(position).getUsername());
        viewHolder.email.setText(getItem(position).getEmail());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " + Objects.requireNonNull(singleSnapshot
                            .getValue(UserAccountSettings.class)));

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(Objects.requireNonNull(singleSnapshot
                                    .getValue(UserAccountSettings.class)).getProfile_photo(),
                            viewHolder.profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return convertView;
    }
}
