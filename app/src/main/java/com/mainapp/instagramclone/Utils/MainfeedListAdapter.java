package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.mainapp.instagramclone.Home.HomeActivity;
import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.Like;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.Profile.ProfileActivity;
import com.mainapp.instagramclone.R;
import com.microprogramer.library.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {
    private static final String TAG = "MainfeedListAdapter";

    public interface onLoadMoreItemsListener {
        void onLoadMoreItems();
    }
    onLoadMoreItemsListener loadMoreItemsListener;

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
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
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
            holder.mProfileImage = convertView.findViewById(R.id.profile_photo);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.gestureDetector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Set the current username and likes string
        getCurrentUsername();
        getLikesString(holder);

        // Set the caption
        holder.caption.setText(getItem(position).getCaption());

        // Ser the comments
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all " + comments.size() + " comments");
        holder.comment.setOnClickListener(view -> {
            Log.d(TAG, "getView: loading comment thread for " + getItem(position).getPhoto_id());
            ((HomeActivity) mContext).onCommentThreadSelected(getItem(position),
                    mContext.getString(R.string.home_activity));
            ((HomeActivity) mContext).hideLayout();
        });

        // Set the time it was posted
        String timestampDiff = getTimestampDifference(getItem(position));
        if (!timestampDiff.equals("0")) {
            holder.timeDelta.setText(timestampDiff + " DAYS AGO");
        } else {
            holder.timeDelta.setText("TODAY");
        }

        // Set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.image);

        // Get the profile image and username
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    //currentUsername = Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getUsername();
                    Log.d(TAG, "onDataChange: found user: " +
                            Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getUsername());

                    holder.username.setText(Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getUsername());
                    holder.username.setOnClickListener(view -> {
                        Log.d(TAG, "onDataChange: navigating to profile: " + holder.user.getUsername());
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra(mContext.getString(R.string.calling_activity),
                                mContext.getString(R.string.home_activity));
                        intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                        mContext.startActivity(intent);
                    });

                    imageLoader.displayImage(Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getProfile_photo(), holder.mProfileImage);
                    holder.mProfileImage.setOnClickListener(view -> {
                        Log.d(TAG, "onDataChange: navigating to profile: " + holder.user.getUsername());
                        Intent intent = new Intent(mContext, ProfileActivity.class);
                        intent.putExtra(mContext.getString(R.string.calling_activity),
                                mContext.getString(R.string.home_activity));
                        intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                        mContext.startActivity(intent);
                    });

                    holder.settings = Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class));
                    holder.comment.setOnClickListener(view -> {
                        ((HomeActivity) mContext).onCommentThreadSelected(getItem(position),
                                mContext.getString(R.string.home_activity));
                        ((HomeActivity) mContext).hideLayout();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Query userQuery = databaseReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                    holder.user = Objects.requireNonNull(singleSnapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (reachedEndOfList(position)) {
            loadMoreData();
        }

        return convertView;
    }

    private boolean reachedEndOfList(int position) {
        return position == getCount() - 1;
    }

    private void loadMoreData() {
        try {
            loadMoreItemsListener = (onLoadMoreItemsListener) getContext();
        } catch (ClassCastException exception) {
            Log.e(TAG, "loadMoreData: ClassCastException: " + exception.getMessage());
        }

        try {
            loadMoreItemsListener.onLoadMoreItems();
        } catch (NullPointerException exception) {
            Log.e(TAG, "loadMoreData: NullPointerException: " + exception.getMessage());
        }
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
                            getLikesString(mHolder);
                        }
                        // If the user has not liked a photo
                        else if (!mHolder.likedByCurrentUser) {
                            // Add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if (!snapshot.exists()) {
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return true;
        }
    }

    private void addNewLike(final ViewHolder holder) {
        Log.d(TAG, "addNewLike: adding new like.");
        String newLikeKey = mDatabaseReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        assert newLikeKey != null;
        mDatabaseReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeKey).setValue(like);

        mDatabaseReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeKey).setValue(like);

        holder.heart.toggleLike();
        getLikesString(holder);
    }

    private void getCurrentUsername() {
        Log.d(TAG, "getCurrentUsername: retrieving user account settings.");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(mContext.getString(R.string.dbname_users))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    currentUsername = Objects.requireNonNull(singleSnapshot.getValue(UserAccountSettings.class)).getUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikesString(final ViewHolder holder) {
        Log.d(TAG, "getLikesString: getting likes string.");
        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(holder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.users = new StringBuilder();
                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                        Query query = databaseReference
                                .child(mContext.getString(R.string.dbname_users))
                                .orderByChild(mContext.getString(R.string.field_user_id))
                                .equalTo(Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.users = new StringBuilder();
                                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: found like: " + Objects.requireNonNull(
                                            singleSnapshot.getValue(User.class)).getUsername());
                                    holder.users.append(Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                                    holder.users.append(",");
                                }
                                String[] splitUsers = holder.users.toString().split(",");
                                holder.likedByCurrentUser = holder.users.toString().contains(currentUsername + ",");
                                int length = splitUsers.length;
                                if (length == 1) {
                                    holder.likesString = "Liked by " + splitUsers[0];
                                } else if (length == 2) {
                                    holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                                } else if (length == 3) {
                                    holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                            " and " + splitUsers[2];
                                } else if (length == 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                            ", " + splitUsers[2] + " and " + splitUsers[3];
                                } else if (length > 4) {
                                    holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                            ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
                                }
                                setupLikesString(holder, holder.likesString);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    if (!snapshot.exists()) {
                        holder.likesString = "";
                        holder.likedByCurrentUser = false;
                        setupLikesString(holder, holder.likesString);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (NullPointerException exception) {
            Log.e(TAG, "getLikesString: NullPointerException: " + exception.getMessage());
            holder.likesString = "";
            holder.likedByCurrentUser = false;
            setupLikesString(holder, holder.likesString);
        }
    }

    private void setupLikesString(final ViewHolder holder, String likesString) {
        Log.d(TAG, "setupLikesString: likes string: " + holder.likesString);
        if (holder.likedByCurrentUser) {
            Log.d(TAG, "setupLikesString: photo is liked by current user.");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener((view, motionEvent) ->
                    holder.gestureDetector.onTouchEvent(motionEvent));
        } else {
            Log.d(TAG, "setupLikesString: photo is not liked by current user.");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener((view, motionEvent) ->
                    holder.gestureDetector.onTouchEvent(motionEvent));
        }
        holder.likes.setText(likesString);
    }

    private String getTimestampDifference(Photo photo) {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference,");
        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Uzhgorod"));
        Date timestamp;
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        final String photoTimestamp = photo.getDate_created();

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
