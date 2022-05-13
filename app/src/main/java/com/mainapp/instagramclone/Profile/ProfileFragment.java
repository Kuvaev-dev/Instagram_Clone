package com.mainapp.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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
import com.mainapp.instagramclone.Models.UserSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.BottomNavigationViewHelper;
import com.mainapp.instagramclone.Utils.FirebaseMethods;
import com.mainapp.instagramclone.Utils.GridImageAdapter;
import com.mainapp.instagramclone.Utils.UniversalImageLoader;
import com.microprogramer.library.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    public interface onGridImageSelectedListener {
        void onGridImageSelected(Photo photo, int activity_number);
    }

    onGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM = 4;
    private static final int NUM_GRID_COLUMNS = 3;

    private Toolbar toolbar;
    private ImageView profileMenu;
    private Context context;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private TextView tDisplayName, tUsername, tWebsite, tDescription, tPosts, tFollowers, tFollowing;
    private CircularImageView profilePhoto;
    private ProgressBar progressBar;
    private GridView gridView;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;

    private int mFollowersCount = 0;
    private int mFollowingCount = 0;
    private int mPostsCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tDisplayName = view.findViewById(R.id.display_name);
        tUsername = view.findViewById(R.id.username);
        tWebsite = view.findViewById(R.id.website);
        tDescription = view.findViewById(R.id.description);
        tPosts = view.findViewById(R.id.tvPosts);
        tFollowers = view.findViewById(R.id.tvFollowers);
        tFollowing = view.findViewById(R.id.tvFollowing);
        profilePhoto = view.findViewById(R.id.profile_photo);
        progressBar = view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolBar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationViewEx = view.findViewById(R.id.bottomNavViewBar);
        context = getActivity();
        firebaseMethods = new FirebaseMethods(getActivity());

        setupBottomNavigationView();
        setupToolBar();
        setupFirebaseAuth();
        setupGridView();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(view1 -> {
            Log.d(TAG, "onCreateView: navigating to " + context.getString(R.string.edit_profile_fragment));
            Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
            intent.putExtra(getString(R.string.calling_activity), getString(R.string.profile_activity));
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mOnGridImageSelectedListener = (onGridImageSelectedListener) getActivity();
        } catch (ClassCastException exception) {
            Log.e(TAG, "onAttach: ClassCastException: " + exception.getMessage());
        }

        super.onAttach(context);
    }

    private void setupGridView() {
        Log.d(TAG, "setupGridView: setting up image grid.");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    try {
                        assert objectMap != null;
                        photo.setCaption(Objects.requireNonNull(objectMap.get(getString(R.string.field_caption))).toString());
                        photo.setTags(Objects.requireNonNull(objectMap.get(getString(R.string.field_tags))).toString());
                        photo.setPhoto_id(Objects.requireNonNull(objectMap.get(getString(R.string.field_photo_id))).toString());
                        photo.setDate_created(Objects.requireNonNull(objectMap.get(getString(R.string.field_date_created))).toString());
                        photo.setImage_path(Objects.requireNonNull(objectMap.get(getString(R.string.field_image_path))).toString());

                        ArrayList<Comment> mComments = new ArrayList<>();
                        for (DataSnapshot ds : singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(Objects.requireNonNull(ds.getValue(Comment.class)).getUser_id());
                            comment.setComment(Objects.requireNonNull(ds.getValue(Comment.class)).getComment());
                            comment.setDate_created(Objects.requireNonNull(ds.getValue(Comment.class)).getDate_created());
                            mComments.add(comment);
                        }
                        photo.setComments(mComments);

                        List<Like> likeList = new ArrayList<>();
                        for (DataSnapshot ds : singleSnapshot.child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(Objects.requireNonNull(ds.getValue(Like.class)).getUser_id());
                            likeList.add(like);
                        }
                        photo.setLikes(likeList);
                        photos.add(photo);
                    } catch (NullPointerException exception) {
                        Log.e(TAG, "onDataChange: NullPointerException: " + exception.getMessage());
                    }
                }

                // Setup image grid
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgURLs = new ArrayList<>();
                for (int i = 0; i < photos.size(); i++) {
                    imgURLs.add(photos.get(i).getImage_path());
                }

                GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                        "", imgURLs);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener((adapterView, view, position, id) -> mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });
    }

    private void getFollowersCount() {
        mFollowersCount = 0;
        DatabaseReference gfDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query gfQuery = gfDatabaseReference
                .child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        gfQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found follower: " + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                tFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowingCount() {
        mFollowingCount = 0;
        DatabaseReference gfDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query gfQuery = gfDatabaseReference
                .child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        gfQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found following user: " + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                tFollowing.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostsCount() {
        mPostsCount = 0;
        DatabaseReference gfDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query gfQuery = gfDatabaseReference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        gfQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found post: " + singleSnapshot.getValue());
                    mPostsCount++;
                }
                tPosts.setText(String.valueOf(mPostsCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from database: " + userSettings.toString());
        UserAccountSettings userAccountSettings = userSettings.getUserAccountSettings();
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), profilePhoto, null, "");
        tDisplayName.setText(userAccountSettings.getDisplay_name());
        tUsername.setText(userAccountSettings.getUsername());
        tWebsite.setText(userAccountSettings.getWebsite());
        tDescription.setText(userAccountSettings.getDescription());
        progressBar.setVisibility(View.GONE);
    }

    private void setupToolBar() {
        ((ProfileActivity) getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(view -> {
            Log.d(TAG, "onClick: navigating to account settings");
            Intent intent = new Intent(context, AccountSettingsActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    /*
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "SetUpBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, getActivity(), bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
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

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve user information from the database
                setProfileWidgets(firebaseMethods.getUserAccountSettings(snapshot));
                // Retrieve images for the user in question
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
