package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PostsAdapter.setOnClickListener {

    private FloatingActionButton addPost_btn;
    private Toolbar mainToolbar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private String current_user_id;


    //set Recyclerview
    RecyclerView mRecyclerView;
    List<ModelPosts> postsList;
    PostsAdapter mPostsAdapter;

    ImageButton moreBtn;

    SwipeRefreshLayout mSwipeRefreshLayout;

    List<Map<String, Object>> postImagesList = new ArrayList<>();



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("InMuse");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();


        addPost_btn= findViewById(R.id.addpost_btn);
        mSwipeRefreshLayout = findViewById(R.id.swipe);

        FirebaseMessaging.getInstance().subscribeToTopic("post");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
                Toast.makeText(MainActivity.this,"Refreshed",Toast.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        addPost_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                startActivity(newPostIntent);
            }
        });


        //init recyclerview
        mRecyclerView = findViewById(R.id.postsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest first , for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to rv
        mRecyclerView.setLayoutManager(layoutManager);
        moreBtn = findViewById(R.id.moreBtn);
        //init post list
        postsList = new ArrayList<>();
        loadPosts();

    }

    private void loadPosts() {
        //path of all posts
        FirebaseFirestore.getInstance().collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                postsList.clear();
                for(QueryDocumentSnapshot qs : task.getResult()){
                    ModelPosts modelPosts = qs.toObject(ModelPosts.class);
                    postsList.add(modelPosts);
                }
                //adapter
                mPostsAdapter =new PostsAdapter(MainActivity.this, postsList,MainActivity.this);
                mPostsAdapter.notifyDataSetChanged();
                //set adapter to rv
                mRecyclerView.setAdapter(mPostsAdapter);
            }
        });

    }

    private void searchPosts(String searchQuery){

        FirebaseFirestore.getInstance().collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                postsList.clear();
                System.out.println("search" );
                for(QueryDocumentSnapshot qs : task.getResult()){
                    ModelPosts modelPosts = qs.toObject(ModelPosts.class);

                    //show the post if it contains any thing the user searched for
                    if(modelPosts.getDescription().toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        postsList.add(modelPosts);
                    }
                    //adapter
                    mPostsAdapter =new PostsAdapter(MainActivity.this, postsList,MainActivity.this);
                    //set adapter to rv
                    mRecyclerView.setAdapter(mPostsAdapter);
                }
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null){
            sendToLogin();
        }else{
            current_user_id = mAuth.getCurrentUser().getUid();
            mFirebaseFirestore.collection("users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){// if the user doesn't have name or image
                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                        else {
                            loadPosts();
                        }
                    }else{
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "ERROR: "+ error,Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate our toolbar with options menu
        getMenuInflater().inflate(R.menu.main_menu,menu);

        //searchview to search posts by post description
        MenuItem item = menu.findItem(R.id.action_search_btn);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Type Here to Search.....");

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button
                if(!TextUtils.isEmpty(query)){
                    searchPosts(query);
                }else{
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user press on any letter
                if(!TextUtils.isEmpty(newText)){
                    searchPosts(newText);
                }else{
                    loadPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_logout_btn:
                logOut();
                return true;

            case R.id.action_settings_btn:
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);

            default:
                return false;
        }

    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }
    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClicked(ModelPosts modelPosts, ImageButton moreBtn) {
        Toast.makeText(MainActivity.this, modelPosts.getPost_ID(),Toast.LENGTH_LONG).show();
        String myUid = modelPosts.getUid();
        String post_id = modelPosts.getPost_ID();
        postImagesList = modelPosts.getImages();
        showMoreOptions(moreBtn,current_user_id, myUid, post_id, postImagesList,modelPosts);
    }

    private void showMoreOptions(ImageButton moreBtn, String uID, String myUid, String post_id, List<Map<String, Object>> postImagesList, ModelPosts modelPosts) {
        //creating a pop menu having options delete
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, moreBtn, Gravity.END);
        //show delete options in only post(s) of currently signed-in user
        if (uID.equals(myUid)){
            popupMenu.getMenu().add(R.menu.popup_menu,0,0,"Delete"); //add items in menu
            popupMenu.getMenu().add(R.menu.popup_menu,1,0,"Edit"); //add items in menu
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id ==0){
                    //delete is clicked
                    beginDelete(post_id,postImagesList,uID);
                    postsList.remove(modelPosts);
                    mPostsAdapter.notifyDataSetChanged();
                }else if (id ==1){
                    //Edit is clicked
                    //Start the NewPostActivity with key "editPost" and the id of the post clicked
                    Intent editIntent = new Intent(MainActivity.this,NewPostActivity.class);
                    editIntent.putExtra("key","editPost");
                    editIntent.putExtra("editPostId", post_id);
                    startActivity(editIntent);
                    mPostsAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void beginDelete(String post_id, List<Map<String, Object>> postImagesList, String uID) {
        ProgressBar progressBar =new ProgressBar(MainActivity.this);
        progressBar.setVisibility(View.VISIBLE);

        //1)Delete image urls
        //2)Delete from Database using post id

        StorageReference picReference;


        for(int i =0;  i<postImagesList.size(); i++){
            picReference = FirebaseStorage.getInstance().getReferenceFromUrl(postImagesList.get(i).get("url").toString());
            picReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    deleteFromFirestore(post_id);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,"ERROR in deleting",Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_LONG).show();

                }
            }
        });
    }


}