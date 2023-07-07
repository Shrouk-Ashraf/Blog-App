package com.example.blogapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class PostDetailsActivity extends AppCompatActivity  implements CommentsAdapter.setOnClickListener{


    ImageView userPictureIv;
    ViewPager postPictures;
    TextView uNameTv,postTimeTv,postDescTv,pLikesTv, pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;
    private CircleIndicator mCircleIndicator;
    ProgressBar mProgressBar;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView commentUserIv;
    RecyclerView commentsRv;

    List<ModelComments> commentList;
    CommentsAdapter commentsAdapter;

    //get Details of user and post
    String uid ,myUid, myEmail,myName, myImg, postId, postLikes, hisDesc,hisName;
    ArrayList<Uri> imagesLists = new ArrayList<>();
    private List<Map<String, Object>> images;

    private ViewPagerAdapter mViewPager;

    private Toolbar postDetailsToolbar;

    private boolean mProcessComment = false;
    private boolean mProcessLike = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postDetailsToolbar = findViewById( R.id.post_details_toolbar);
        setSupportActionBar(postDetailsToolbar);
        getSupportActionBar().setTitle("Post Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //get id of the post using the intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        userPictureIv = findViewById(R.id.user_picture);
        postPictures = findViewById(R.id.show_new_post_image_pager);
        mCircleIndicator = findViewById(R.id.show_circle_indicator);
        uNameTv = findViewById(R.id.user_name);
        postTimeTv = findViewById(R.id.post_time);
        postDescTv = findViewById(R.id.post_description);
        pLikesTv = findViewById(R.id.post_likes);
        pCommentsTv = findViewById(R.id.post_comments);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profile_layout);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        commentUserIv = findViewById(R.id.comment_user_IV);
        mProgressBar = findViewById(R.id.commentProgressBar);
        commentsRv = findViewById(R.id.comments_RV);

        loadComments();
        loadPostInfo();

        checkUserStatus();
        loadUserInfo();

        setLikes();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment();
            }
        });
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likePost();
            }
        });
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions();
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostDetailsActivity.this, NewPostActivity.class);
                intent.putExtra("shareKey","share");
                intent.putExtra("sharePostId", postId);
                startActivity(intent);
            }
        });
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        commentsRv.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .collection("comments").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        commentList.clear();
                        for(DocumentSnapshot qs : documents){
                            ModelComments comments = qs.toObject(ModelComments.class);
                            commentList.add(comments);
                            commentsAdapter = new CommentsAdapter(getApplicationContext(),commentList,myUid,postId,PostDetailsActivity.this);
                            commentsAdapter.notifyDataSetChanged();
                            commentsRv.setAdapter(commentsAdapter);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    private void showMoreOptions() {
        //creating a pop menu having options delete
        PopupMenu popupMenu = new PopupMenu(PostDetailsActivity.this, moreBtn, Gravity.END);
        //show delete options in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            popupMenu.getMenu().add(R.menu.popup_menu,0,0,"Delete"); //add items in menu
            popupMenu.getMenu().add(R.menu.popup_menu,1,0,"Edit"); //add items in menu
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id ==0){
                    //delete is clicked
                    beginDeletePost();

                }else if (id ==1){
                    //Edit is clicked
                    //Start the NewPostActivity with key "editPost" and the id of the post clicked
                    Intent editIntent = new Intent(PostDetailsActivity.this,NewPostActivity.class);
                    editIntent.putExtra("key","editPost");
                    editIntent.putExtra("editPostId", postId);
                    startActivity(editIntent);
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDeletePost() {
        ProgressBar progressBar =new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);

        //1)Delete image urls
        //2)Delete from Database using post id

        StorageReference picReference;


        for(int i =0;  i<images.size(); i++){
            picReference = FirebaseStorage.getInstance().getReferenceFromUrl(images.get(i).get("url").toString());
            picReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    deleteFromFirestore(postId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(PostDetailsActivity.this,"ERROR in deleting",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void deleteFromFirestore(String post_id) {
        //image is deleted now delete database
        Task<Void> query = FirebaseFirestore.getInstance().collection("posts").document(post_id).delete();
        query.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(PostDetailsActivity.this,MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(PostDetailsActivity.this,"Deleted",Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void setLikes() {
        FirebaseFirestore.getInstance().collection("posts").document(postId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null && value.exists()){
                    List<String> whoLikes = (List<String>) value.get("whoLikes");
                    if (whoLikes.contains(myUid)){
                        //already liked
                        likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                        likeBtn.setText("Liked");
                        mProcessLike = true;
                    }else{
                        likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                        likeBtn.setText("Like");
                        mProcessLike = false;
                    }
                }else{
                    Log.d(TAG, "Current data: null");
                }
            }
        });

    }

    private void likePost() {
        Map<String , Object> data = new HashMap<>();
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("post_ID",postId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();
                for(DocumentSnapshot qs : snapshotList) {
                    ModelPosts post = qs.toObject(ModelPosts.class);
                    List<String> whoLikes = post.getWhoLikes();
                    Integer likes = Integer.parseInt(post.getLikes());
                    if(whoLikes.contains(myUid)){ //to dislike
                        likes = likes -1;
                        data.put("likes",likes.toString());
                        whoLikes.remove(myUid);
                        data.put("whoLikes",whoLikes);
                        post.setLikes(String.valueOf(likes));
                        Integer finalLikes = likes;
                        FirebaseFirestore.getInstance().collection("posts").document(postId).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mProcessLike = false;
                                Toast.makeText(PostDetailsActivity.this,"disLike",Toast.LENGTH_LONG).show();
                                pLikesTv.setText(finalLikes + " Likes");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostDetailsActivity.this,"Error in dislike  " +e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{ //to like
                        likes = likes +1;
                        data.put("likes",likes.toString());
                        whoLikes.add(myUid);
                        data.put("whoLikes",whoLikes);
                        Integer finalPostLikes = likes;
                        post.setLikes(String.valueOf(likes));
                        FirebaseFirestore.getInstance().collection("posts").document(postId).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                mProcessLike = true;
                                Toast.makeText(PostDetailsActivity.this,"Like",Toast.LENGTH_LONG).show();
                                pLikesTv.setText(finalPostLikes+" Likes");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostDetailsActivity.this,"Error",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void postComment() {
        mProgressBar.setVisibility(View.VISIBLE);
        String comment = commentEt.getText().toString().trim();
        Map<String , Object> data = new HashMap<>();
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this,"Comment is empty...",Toast.LENGTH_SHORT).show();
            return;
        }
        String time =String.valueOf(System.currentTimeMillis());
        SimpleDateFormat t = new SimpleDateFormat("dd/MM/yyyy hh:mm z");
        String theTime = t.format(new Date());
        data.put("cId",time);
        data.put("comment",comment);
        data.put("uid",myUid);
        data.put("email",myEmail);
        data.put("name",myName);
        data.put("user_img",myImg);
        data.put("timestamp",theTime);
        FirebaseFirestore.getInstance().collection("posts").document(postId)
                .collection("comments").document(time).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(PostDetailsActivity.this,"Comment Added",Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
                loadComments();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void updateCommentCount() {
        mProcessComment = true;
        Map<String , Object> data = new HashMap<>();
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("post_ID",postId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot qs : snapshotList) {
                    ModelPosts post = qs.toObject(ModelPosts.class);
                    if(mProcessComment){
                        String comments = ""+post.getComments();
                        Integer newComment = Integer.parseInt(comments)+1;
                        pCommentsTv.setText(newComment + " Comments");
                        data.put("comments", newComment.toString());
                        FirebaseFirestore.getInstance().collection("posts").document(postId).set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(PostDetailsActivity.this, "Comment count updated", Toast.LENGTH_SHORT).show();
                                mProcessComment = false;
                            }
                        });
                    }
                }
            }
        });

    }


    private void loadPostInfo() {
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("post_ID",postId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot qs : snapshotList) {
                    ModelPosts post = qs.toObject(ModelPosts.class);
                    //get data
                    String pDesc = post.getDescription();
                    postLikes = post.getLikes();
                    String pTime = post.getTimestamp();
                    uid = post.getUid();
                    String email = post.getEmail();
                    hisName = post.getName();
                    String uImage = post.getUser_img();
                    String pCommentsCount = post.getComments();

                    images = (List<Map<String,Object>>) qs.getData().get("images");

                    for(int i = 0; i< images.size(); i++){
                        imagesLists.add(Uri.parse(images.get(i).get("url").toString()));
                    }

                    //set Data
                    mViewPager = new ViewPagerAdapter(PostDetailsActivity.this, imagesLists,2);
                    postPictures.setAdapter(mViewPager);
                    postPictures.setCurrentItem(0,false);
                    mCircleIndicator.setViewPager(postPictures);
                    mViewPager.notifyDataSetChanged();

                    postDescTv.setText(pDesc);
                    pLikesTv.setText(postLikes + " Likes");
                    pCommentsTv.setText(pCommentsCount + " Comments");
                    postTimeTv.setText(pTime);
                    uNameTv.setText(hisName);
                    Glide.with(PostDetailsActivity.this).load(uImage)
                            .placeholder(R.drawable.default_profile_img).into(userPictureIv);

                }
            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();
        }else{
            //user is not signed in
            startActivity(new Intent(this,RegisterActivity.class));
            finish();
        }
    }

    private void loadUserInfo() {
        Query query = FirebaseFirestore.getInstance().collection("users");
        query.whereEqualTo("uid",myUid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot qs : snapshotList) {
                    myName = qs.getString("name");
                    myImg = qs.getString("img");
                    Glide.with(PostDetailsActivity.this).load(myImg)
                            .placeholder(R.drawable.default_profile_img).into(commentUserIv);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.action_search_btn).setVisible(false);
        menu.findItem(R.id.action_settings_btn).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_logout_btn){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClicked(Integer comments) {
        System.out.println(comments);
        pCommentsTv.setText(comments + " Comments");
    }
}