package com.mainapp.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mainapp.instagramclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class GridImageAdapter extends ArrayAdapter<String> {
    private final LayoutInflater inflater;
    private final int layoutResource;
    private final String mAppend;

    public GridImageAdapter(Context context, int layoutResource, String mAppend, ArrayList<String> imageURLs) {
        super(context, layoutResource, imageURLs);  // For reference to items in imageURLs
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutResource = layoutResource;
        this.mAppend = mAppend;
    }

    private static class ViewHolder {
        SquareImageView image;
        ProgressBar mProgressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        /*
        *   ViewHolder build pattern
        */
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mProgressBar = convertView.findViewById(R.id.gridImageProgressBar);
            viewHolder.image = convertView.findViewById(R.id.gridImageView);

            convertView.setTag(viewHolder); // For storing widgets in memory
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String imageURL = getItem(position);

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mAppend + imageURL, viewHolder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                if (viewHolder.mProgressBar != null)
                    viewHolder.mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                if (viewHolder.mProgressBar != null)
                    viewHolder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (viewHolder.mProgressBar != null)
                    viewHolder.mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                if (viewHolder.mProgressBar != null)
                    viewHolder.mProgressBar.setVisibility(View.GONE);
            }
        });

        return convertView;
    }
}
