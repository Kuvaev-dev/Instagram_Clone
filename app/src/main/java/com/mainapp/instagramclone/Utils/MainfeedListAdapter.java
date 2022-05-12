package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Models.Like;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.microprogramer.library.CircularImageView;

import java.util.List;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private String currentUsername;

    public MainfeedListAdapter(@NonNull Context context, int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
    }

    static class ViewHolder {
        CircularImageView mProfileImage;
        String likesString;
        TextView username, timeDelta, caption, likes, comments;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        StringBuilder users;
        String mLikesString;
        boolean likedByCurrentUser;
        Heart heart;
        GestureDetector gestureDetector;
        Photo photo;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.image = convertView.findViewById(R.id.post_image);
            holder.heartRed = convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.image_heart);
            holder.comment = convertView.findViewById(R.id.speech_bubble);
            holder.likes = convertView.findViewById(R.id.image_likes);
            holder.comments = convertView.findViewById(R.id.image_comments_link);
            holder.caption = convertView.findViewById(R.id.image_caption);
            holder.timeDelta = convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImage = convertView.findViewById(R.id.profile_image);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.gestureDetector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();
        }

        return convertView;
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ViewHolder mHolder;

        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        String keyId = singleSnapshot.getKey();
                        // If the user already liked a photo
                        if (mHolder.likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            assert keyId != null;

                            mDatabaseReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyId).removeValue();

                            mDatabaseReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyId).removeValue();

                            mHolder.heart.toggleLike();
                            //getLikesString();
                        }
                        // If the user has not liked a photo
                        else if (!mHolder.likedByCurrentUser) {
                            // Add new like
                            //addNewLike();
                            break;
                        }
                    }
                    if (!snapshot.exists()) {
                        //addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return true;
        }
    }
}
