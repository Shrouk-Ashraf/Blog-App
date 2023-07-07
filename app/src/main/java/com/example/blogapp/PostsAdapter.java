package com.example.blogapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.viewHolder> {

    private Context mContext;
    private List<ModelPosts> postLists;

    private setOnClickListener setOnClickListener;
    String myUid;

    CollectionReference postRef;
    CollectionReference likesRef;
    boolean processLike = false;
    List<String> whoLikes = new ArrayList<>();


    public PostsAdapter(Context context, List<ModelPosts> postLists, setOnClickListener setOnClickListener) {
        mContext = context;
        this.postLists = postLists;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.setOnClickListener = setOnClickListener;
        postRef = FirebaseFirestore.getInstance().collection("posts");
        likesRef = FirebaseFirestore.getInstance().collection("liked posts");
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_posts,parent,false);

        return new viewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        //get Data
        String uID = postLists.get(position).getUid();
        String user_email = postLists.get(position).getEmail();
        String post_id = postLists.get(position).getPost_ID();
        String post_desc = postLists.get(position).getDescription();
        String post_timestamp = postLists.get(position).getTimestamp();
        String post_likes = postLists.get(position).getLikes();

        whoLikes = postLists.get(position).getWhoLikes();
        ArrayList<String> postImagesList = new ArrayList<>();

        for(int i=0; i< postLists.get(position).getImages().size(); i++){
            postImagesList.add(postLists.get(position).getImages().get(i).get("url").toString());
        }

        String user_name = postLists.get(position).getName();
        String user_picture = postLists.get(position).getUser_img();

        setViewPagerAdapter(postImagesList,holder);

        //set data
        holder.user_name.setText(user_name);
        holder.post_desc.setText(post_desc);
        holder.post_time.setText(post_timestamp);
        holder.pLikes.setText(post_likes+ " Likes");

        setLikes(holder,whoLikes);

        Glide.with(mContext).load(user_picture)
                .placeholder(R.drawable.default_profile_img).into(holder.user_picture);

        //set buttons
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show more options
                setOnClickListener.onItemClicked(postLists.get(position),holder.moreBtn);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer postLikes = Integer.valueOf(postLists.get(position).getLikes()); //get total number of likes for the post
                String postId = postLists.get(position).getPost_ID();
                Map<String , Object> data = new HashMap<>();

                if(processLike){ //to dislike
                    postLikes = postLikes -1;
                    data.put("likes",postLikes.toString());
                    whoLikes.remove(myUid);
                    data.put("whoLikes",whoLikes);
                    Integer finalPostLikes = postLikes;
                    postLists.get(position).setLikes(String.valueOf(postLikes));
                    postRef.document(postId).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            processLike = false;
                            Toast.makeText(mContext,"disLike",Toast.LENGTH_LONG).show();
                            holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                            holder.likeBtn.setText("Like");
                            holder.pLikes.setText(finalPostLikes + " Likes");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext,"Error in dislike  " +e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    postLikes = postLikes +1;
                    data.put("likes",postLikes.toString());
                    whoLikes.add(myUid);
                    data.put("whoLikes",whoLikes);
                    Integer finalPostLikes = postLikes;
                    postLists.get(position).setLikes(String.valueOf(postLikes));
                    postRef.document(postId).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            processLike = true;
                            Toast.makeText(mContext,"Like",Toast.LENGTH_LONG).show();
                            holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                            holder.likeBtn.setText("Liked");
                            holder.pLikes.setText(finalPostLikes+" Likes");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext,"Error",Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext,"comment btn", Toast.LENGTH_LONG).show();
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext,"share btn", Toast.LENGTH_LONG).show();
            }
        });


    }

    private void setLikes(viewHolder holder, List<String> whoLikes) {
        if (whoLikes.contains(myUid)){
            //already liked
            holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
            holder.likeBtn.setText("Liked");
            processLike = true;
        }else{
            holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
            holder.likeBtn.setText("Like");
            processLike = false;
        }
    }


    private void setViewPagerAdapter(ArrayList<String> postImagesList, viewHolder holder) {
        ShowPostsViewPager showPostsViewPager = new ShowPostsViewPager(mContext,postImagesList);


        holder.post_image.setAdapter(showPostsViewPager);


        holder.post_image.setCurrentItem(0,false);

        holder.postCircleIndicator.setViewPager(holder.post_image);
        showPostsViewPager.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return postLists.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{

        ImageView user_picture;
        ViewPager post_image;
        CircleIndicator postCircleIndicator;
        TextView user_name,post_time, post_desc;
        ImageButton moreBtn;
        Button likeBtn, commentBtn, shareBtn;
        TextView pLikes;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            user_picture = itemView.findViewById(R.id.user_picture);

            post_image = itemView.findViewById(R.id.show_new_post_image_pager);// for the viewpager
            postCircleIndicator = itemView.findViewById(R.id.show_circle_indicator);

            user_name = itemView.findViewById(R.id.user_name);
            post_time = itemView.findViewById(R.id.post_time);
            post_desc = itemView.findViewById(R.id.post_description);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            pLikes = itemView.findViewById(R.id.post_likes);


        }
    }

    public interface setOnClickListener{
        void  onItemClicked(ModelPosts modelPosts, ImageButton  moreBtn);
    }

}
