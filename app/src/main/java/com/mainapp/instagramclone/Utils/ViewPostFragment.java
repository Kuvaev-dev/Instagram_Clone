package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.Like;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.User;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.SquareImageView;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {
    private static final String TAG = "ViewPostFragment";

    public interface onCommentThreadSelectedListener {
        void onCommentThreadSelected(Photo photo);
    }

    onCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView mBackLabel, mCaption, mUsername, mTimestamp, mLikes, mComments;
    private ImageView mBackArrow, mEllipses, mHeartRed, mHeartWhite, mProfileImage, mComment;

    private int mActivityNum = 0;
    private String photoUrl;
    private String photoUsername;
    private UserAccountSettings userAccountSettings;
    private GestureDetector gestureDetector;
    private Boolean likedByCurrentUser;
    private StringBuilder mUsers;
    private String likesString;
    private Photo mPhoto;
    private Heart heart;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference mDatabaseReference;

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        SquareImageView mPostImage = view.findViewById(R.id.post_image);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        mBackArrow = view.findViewById(R.id.backArrow);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mCaption = view.findViewById(R.id.image_caption);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.image_time_posted);
        mEllipses = view.findViewById(R.id.ivEllipses);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mLikes = view.findViewById(R.id.image_likes);
        mHeartWhite = view.findViewById(R.id.image_heart);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mComment = view.findViewById(R.id.speech_bubble);
        mComments = view.findViewById(R.id.image_comments_link);

        heart = new Heart(mHeartWhite, mHeartRed);

        gestureDetector = new GestureDetector(getActivity(), new GestureListener());
        try {
            //mPhoto = getPhotoFromBundle();
            //assert mPhoto != null;
            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(), mPostImage, null, "");
            mActivityNum = getActivityNumFromBundle();
            String photo_id = getPhotoFromBundle().getPhoto_id();
            Query query = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_user_photos))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        Photo newPhoto = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        assert objectMap != null;
                        newPhoto.setCaption(Objects.requireNonNull(objectMap.get(getString(R.string.field_caption))).toString());
                        newPhoto.setTags(Objects.requireNonNull(objectMap.get(getString(R.string.field_tags))).toString());
                        newPhoto.setPhoto_id(Objects.requireNonNull(objectMap.get(getString(R.string.field_photo_id))).toString());
                        newPhoto.setDate_created(Objects.requireNonNull(objectMap.get(getString(R.string.field_date_created))).toString());
                        newPhoto.setImage_path(Objects.requireNonNull(objectMap.get(getString(R.string.field_image_path))).toString());

                        List<Comment> mComments = new ArrayList<>();
                        for (DataSnapshot ds: singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(Objects.requireNonNull(ds.getValue(Comment.class)).getUser_id());
                            comment.setComment(Objects.requireNonNull(ds.getValue(Comment.class)).getComment());
                            comment.setDate_created(Objects.requireNonNull(ds.getValue(Comment.class)).getDate_created());
                            mComments.add(comment);
                        }
                        newPhoto.setComments(mComments);

                        mPhoto = newPhoto;
                        getPhotoDetails();
                        getLikesString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: query cancelled.");
                }
            });
        } catch (NullPointerException exception) {
            Log.e(TAG, "onCreateView: NullPointerException: " + exception.getMessage());
        }

        setupFirebaseAuth();
        setupBottomNavigationView();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnCommentThreadSelectedListener = (onCommentThreadSelectedListener) getActivity();
        } catch (ClassCastException exception) {
            Log.e(TAG, "onAttach: ClassCastException: " + exception.getMessage());
        }
    }

    private void getLikesString() {
        Log.d(TAG, "getLikesString: getting likes string.");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers = new StringBuilder();
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    Query query = databaseReference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo(Objects.requireNonNull(singleSnapshot.getValue(Like.class)).getUser_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            mUsers = new StringBuilder();
                            for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: found like: " + Objects.requireNonNull(
                                        singleSnapshot.getValue(User.class)).getUsername());
                                mUsers.append(Objects.requireNonNull(singleSnapshot.getValue(User.class)).getUsername());
                                mUsers.append(",");
                            }
                            String[] splitUsers = mUsers.toString().split(",");
                            likedByCurrentUser = mUsers.toString().contains(userAccountSettings.getUsername() + ",");
                            int length = splitUsers.length;
                            if (length == 1) {
                                likesString = "Liked by " + splitUsers[0];
                            } else if (length == 2) {
                                likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
                            } else if (length == 3) {
                                likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                        " and " + splitUsers[2];
                            } else if (length == 4) {
                                likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                        ", " + splitUsers[2] + " and " + splitUsers[3];
                            } else if (length > 4) {
                                likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] +
                                        ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
                            }
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if (!snapshot.exists()) {
                    likesString = "";
                    likedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                        String keyId = singleSnapshot.getKey();
                        // If the user already liked a photo
                        if (likedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            assert keyId != null;

                            mDatabaseReference.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId).removeValue();

                            mDatabaseReference.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyId).removeValue();

                            heart.toggleLike();
                            getLikesString();
                        }
                        // If the user has not liked a photo
                        else if (!likedByCurrentUser) {
                            // Add new like
                            addNewLike();
                            break;
                        }
                    }
                    if (!snapshot.exists()) {
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return true;
        }
    }

    private void addNewLike() {
        Log.d(TAG, "addNewLike: adding new like.");
        String newLikeKey = mDatabaseReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        assert newLikeKey != null;
        mDatabaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeKey).setValue(like);

        mDatabaseReference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeKey).setValue(like);

        heart.toggleLike();
        getLikesString();
    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(mPhoto.getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                }
                //setupWidgets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void setupWidgets() {
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + " DAYS AGO");
        } else {
            mTimestamp.setText("TODAY");
        }
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(userAccountSettings.getUsername());
        mLikes.setText(likesString);
        mCaption.setText(mPhoto.getCaption());

        if (mPhoto.getComments().size() > 0) {
            mComments.setText("View all " + mPhoto.getComments().size() + " comments");
        } else {
            mComments.setText("");
        }

        mComments.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: navigating to comments thread.");
            mOnCommentThreadSelectedListener.onCommentThreadSelected(mPhoto);
        });

        mBackArrow.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: navigating back.");
            getActivity().getSupportFragmentManager().popBackStack();
        });

        mComment.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: navigating back.");
            mOnCommentThreadSelectedListener.onCommentThreadSelected(mPhoto);
        });

        if (likedByCurrentUser) {
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener((view, motionEvent) -> {
                Log.d(TAG, "onTouch: red heart touch detected.");
                return gestureDetector.onTouchEvent(motionEvent);
            });
        } else {
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener((view, motionEvent) -> {
                Log.d(TAG, "onTouch: white heart touch detected.");
                return gestureDetector.onTouchEvent(motionEvent);
            });
        }
    }

    private String getTimestampDifference() {
        Log.d(TAG, "getTimestampDifference: getting timestamp difference,");
        String difference;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Uzhgorod"));
        Date timestamp;
        Date today = calendar.getTime();
        simpleDateFormat.format(today);
        final String photoTimestamp = mPhoto.getDate_created();

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

    private int getActivityNumFromBundle() {
        Log.d(TAG, "getActivityNumFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null)
            return bundle.getInt(getString(R.string.activity_number));
        else
            return 0;
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null)
            return bundle.getParcelable(getString(R.string.photo));
        else
            return null;
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNum);
        menuItem.setChecked(true);
    }

    // Firebase BEGINNING

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null)
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
            else
                Log.d(TAG, "onAuthStateChanged: signed out.");
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
