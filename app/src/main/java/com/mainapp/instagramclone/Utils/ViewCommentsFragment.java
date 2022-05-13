// 13.05.2022 - Reviewed. All Done.
package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Home.HomeActivity;
import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewCommentsFragment";

    private Photo mPhoto;
    private ArrayList<Comment> mComments;

    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;
    private Context mContext;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseMethods firebaseMethods;
    private DatabaseReference mDatabaseReference;

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);
        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = getActivity();

        try {
            mPhoto = getPhotoFromBundle();
            setupFirebaseAuth();
        } catch (NullPointerException exception) {
            Log.e(TAG, "onCreateView: NullPointerException: " + exception.getMessage());
        }

        return view;
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);
        String commentId = mDatabaseReference.push().getKey();
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        assert commentId != null;
        mDatabaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);

        mDatabaseReference.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentId)
                .setValue(comment);
    }

    private String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Uzhgorod"));
        return simpleDateFormat.format(new Date());
    }

    private String getCallingActivityFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null)
            return bundle.getString(getString(R.string.home_activity));
        else
            return null;
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null)
            return bundle.getParcelable(getString(R.string.photo));
        else
            return null;
    }

    private void setupWidgets() {
        CommentListAdapter adapter = new CommentListAdapter(mContext, R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(view1 -> {
            if (!mComment.getText().toString().equals("")) {
                Log.d(TAG, "onCreateView: attempting to submit new comment.");
                addNewComment(mComment.getText().toString());
                mComment.setText("");
                closeKeyboard();
            } else {
                Toast.makeText(mContext, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
            }
        });

        mBackArrow.setOnClickListener(view -> {
            Log.d(TAG, "setupWidgets: navigating back.");
            if (Objects.equals(getCallingActivityFromBundle(), getString(R.string.home_activity))) {
                getActivity().getSupportFragmentManager().popBackStack();
                ((HomeActivity) getActivity()).showLayout();
            } else {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    // Firebase BEGINNING

    /*
     *  Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null)
                Log.d(TAG, "onAuthStateChanged: signed in: " + user.getUid());
            else
                Log.d(TAG, "onAuthStateChanged: signed out.");
        };

        if (mPhoto.getComments().size() == 0) {
            mComments.clear();
            Comment firstComment = new Comment();
            assert mPhoto != null;
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();
        }

        mDatabaseReference.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Query query = mDatabaseReference
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot singleSnapshot: snapshot.getChildren()) {
                                    Photo photo = new Photo();
                                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                                    assert objectMap != null;
                                    photo.setCaption(Objects.requireNonNull(objectMap.get(mContext.getString(R.string.field_caption))).toString());
                                    photo.setTags(Objects.requireNonNull(objectMap.get(mContext.getString(R.string.field_tags))).toString());
                                    photo.setPhoto_id(Objects.requireNonNull(objectMap.get(mContext.getString(R.string.field_photo_id))).toString());
                                    photo.setDate_created(Objects.requireNonNull(objectMap.get(mContext.getString(R.string.field_date_created))).toString());
                                    photo.setImage_path(Objects.requireNonNull(objectMap.get(mContext.getString(R.string.field_image_path))).toString());

                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    assert mPhoto != null;
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());
                                    mComments.add(firstComment);

                                    for (DataSnapshot ds: singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()) {
                                        Comment comment = new Comment();
                                        comment.setUser_id(Objects.requireNonNull(ds.getValue(Comment.class)).getUser_id());
                                        comment.setComment(Objects.requireNonNull(ds.getValue(Comment.class)).getComment());
                                        comment.setDate_created(Objects.requireNonNull(ds.getValue(Comment.class)).getDate_created());
                                        mComments.add(comment);
                                    }

                                    photo.setComments(mComments);
                                    mPhoto = photo;

                                    setupWidgets();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: query cancelled.");
                            }
                        });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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
