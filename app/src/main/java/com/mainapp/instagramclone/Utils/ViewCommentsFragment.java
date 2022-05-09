package com.mainapp.instagramclone.Utils;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mainapp.instagramclone.Models.Comment;
import com.mainapp.instagramclone.Models.Photo;
import com.mainapp.instagramclone.R;

import java.util.ArrayList;

public class ViewCommentsFragment extends Fragment {
    private static final String TAG = "ViewCommentsFragment";

    private Photo mPhoto;
    private ArrayList<Comment> mComments;

    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

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

        try {
            mPhoto = getPhotoFromBundle();
        } catch (NullPointerException exception) {
            Log.e(TAG, "onCreateView: NullPointerException: " + exception.getMessage());
        }

        Comment firstComment = new Comment();
        assert mPhoto != null;
        firstComment.setComment(mPhoto.getCaption());
        firstComment.setUser_id(mPhoto.getUser_id());
        firstComment.setDate_created(mPhoto.getDate_created());

        mComments.add(firstComment);
        CommentListAdapter adapter = new CommentListAdapter(getActivity(), R.layout.layout_comment, mComments);
        mListView.setAdapter(adapter);

        return view;
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null)
            return bundle.getParcelable(getString(R.string.photo));
        else
            return null;
    }
}
