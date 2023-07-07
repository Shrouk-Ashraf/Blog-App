package com.example.blogapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ShowPostsViewPager extends PagerAdapter {
    Context mContext;
    ArrayList<String> postImagesList;
    LayoutInflater mLayoutInflater;

    public ShowPostsViewPager(Context context, ArrayList<String> postImagesList) {
        mContext = context;
        this.postImagesList = postImagesList;
    }


    @Override
    public int getCount() {
        return postImagesList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mLayoutInflater =(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.show_new_post_pager,container,false);
        ImageView imageView = view.findViewById(R.id.show_new_post_image);

        Glide.with(mContext).load(postImagesList.get(position))
                        .into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
