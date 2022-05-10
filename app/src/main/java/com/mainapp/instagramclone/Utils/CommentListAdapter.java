package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.microprogramer.library.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentListAdapter extends ArrayAdapter<Comment> {
    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    
    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }

    private static class ViewHolder {
        TextView comment, username, timestamp, reply, likes;
        CircularImageView profilePhoto;
        ImageView like;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.comment = convertView.findViewById(R.id.comment);
            viewHolder.username = convertView.findViewById(R.id.comment_username);
            viewHolder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            viewHolder.reply = convertView.findViewById(R.id.comment_reply);
            viewHolder.profilePhoto = convertView.findViewById(R.id.comment_profile_image);
            viewHolder.like = convertView.findViewById(R.id.comment_like);
            viewHolder.likes = convertView.findViewById(R.id.comment_likes);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Set the comment
        viewHolder.comment.setText(getItem(position).getComment());

        // Set timestamp difference
        String timestampDiff = getTimestampDifference(getItem(position));
        if (!timestampDiff.equals("0"))
            viewHolder.timestamp.setText(timestampDiff + " d");
        else
            viewHolder.timestamp.setText("Today");

        // Set the username and profile image
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    viewHolder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            viewHolder.profilePhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

        try {
            if (position == 0) {
                viewHolder.like.setVisibility(View.GONE);
                viewHolder.likes.setVisibility(View.GONE);
                viewHolder.reply.setVisibility(View.GONE);
            }
        } catch (NullPointerException exception) {
            Log.e(TAG, "getView: NullPointerException: " + exception.getMessage());
        }

        return convertView;
    }

    private String getTimestampDifference(Comment comment) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference,");
        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Uzhgorod"));
        Date timestamp;
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        final String photoTimestamp = comment.getDate_created();

        try {
            timestamp = simpleDateFormat.parse(photoTimestamp);
            assert timestamp != null;
            difference = String.valueOf(Math.round(today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24);
        } catch (ParseException exception) {
            Log.e(TAG, "getTimestampDifference: ParseException: " + exception.getMessage());
            difference = "0";
        }
        return difference;
    }
}
