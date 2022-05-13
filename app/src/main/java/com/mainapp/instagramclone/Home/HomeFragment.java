package com.mainapp.instagramclone.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.Like;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.Models.UserAccountSettings;
import com.mainapp.instagramclone.R;
import com.mainapp.instagramclone.Utils.MainfeedListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mListView = view.findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }
    
    private void getFollowing() {
        Log.d(TAG, "getFollowing: searching for following.");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_following))
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user: " +
                            singleSnapshot.child(getString(R.string.field_user_id)).getValue());
                    mFollowing.add(Objects.requireNonNull(singleSnapshot.child(getString(
                            R.string.field_user_id)).getValue()).toString());
                }
                getPhotos();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos.");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        for (int i = 0; i < mFollowing.size(); i++) {
            final int count = i;
            Query query = databaseReference
                    .child(getString(R.string.dbname_user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                        assert objectMap != null;
                        photo.setCaption(Objects.requireNonNull(objectMap.get(getString(R.string.field_caption))).toString());
                        photo.setTags(Objects.requireNonNull(objectMap.get(getString(R.string.field_tags))).toString());
                        photo.setPhoto_id(Objects.requireNonNull(objectMap.get(getString(R.string.field_photo_id))).toString());
                        photo.setDate_created(Objects.requireNonNull(objectMap.get(getString(R.string.field_date_created))).toString());
                        photo.setImage_path(Objects.requireNonNull(objectMap.get(getString(R.string.field_image_path))).toString());

                        ArrayList<Comment> mComments = new ArrayList<>();
                        for (DataSnapshot ds: singleSnapshot.child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(Objects.requireNonNull(ds.getValue(Comment.class)).getUser_id());
                            comment.setComment(Objects.requireNonNull(ds.getValue(Comment.class)).getComment());
                            comment.setDate_created(Objects.requireNonNull(ds.getValue(Comment.class)).getDate_created());
                            mComments.add(comment);
                        }
                        photo.setComments(mComments);
                        mPhotos.add(photo);
                    }

                    if (count >= mFollowing.size() - 1) {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void displayPhotos() {
        if (mPhotos != null) {
            Collections.sort(mPhotos, (object1, object2) ->
                    object2.getDate_created().compareTo(object1.getDate_created()));
        }
        mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos);
        mListView.setAdapter(mAdapter);
    }
}
