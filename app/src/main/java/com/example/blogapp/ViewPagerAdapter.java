package com.example.blogapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ViewPagerAdapter  extends PagerAdapter {
    private Context mContext;


    private ArrayList<Uri> imagesURLs;



    ArrayList<Uri> deletedImagesURLs = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int i ;

    public ViewPagerAdapter(Context context, ArrayList<Uri> imagesURLs, int i) {
        mContext = context;
        this.imagesURLs = imagesURLs;
        this.i =i;
    }

    @Override
    public int getCount() {
        return imagesURLs.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        mLayoutInflater =(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mLayoutInflater.inflate(R.layout.add_new_post_pager,container,false);
        ImageView imageView = view.findViewById(R.id.add_new_post_image);

        ImageView trash = view.findViewById(R.id.trash_iv);

        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);

        ImageView delete_view = view.findViewById(R.id.delete_icon);

        Glide.with(mContext).load(imagesURLs.get(position))
                .into(imageView);

        int del_position = position;

        if(this.i == 2){
            trash.setVisibility(View.INVISIBLE);
            delete_view.setVisibility(View.INVISIBLE);
        }
        else if(this.i==1){
            trash.setVisibility(View.VISIBLE);
            delete_view.setVisibility(View.INVISIBLE);
            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deletedImagesURLs.add(imagesURLs.get(del_position));
                    imagesURLs.remove(del_position);
                    notifyDataSetChanged();
                }
            });
            this.imagesURLs = imagesURLs;
        }else {
            delete_view.setVisibility(View.VISIBLE);
            trash.setVisibility(View.INVISIBLE);
            delete_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imagesURLs.remove(del_position);
                    notifyDataSetChanged();
                }
            });
        }
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

    public ArrayList<Uri> getDeletedImagesURLs() {
        return deletedImagesURLs;
    }

    public ArrayList<Uri> getImagesURLs() {
        return imagesURLs;
    }
}
