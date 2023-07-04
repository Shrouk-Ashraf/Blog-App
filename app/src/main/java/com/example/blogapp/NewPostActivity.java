package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.relex.circleindicator.CircleIndicator;

public class NewPostActivity extends AppCompatActivity {

//    private ImageView newPostImage;
    private EditText newPostDesc;
    private Button newPostBtn;
    private ProgressBar newPostProgress;
    private Toolbar newPostToolbar;
    private CardView addImagesCv;

    private ViewPager new_image_pager;
    ArrayList<Uri> choosed_images ;
    ArrayList<String> images_URLs ;
    private ImageView add_more_images;
    private CircleIndicator mCircleIndicator;

    Context mContext= NewPostActivity.this;;

    List<Map<String,Object>> imagesMap =new ArrayList<>();



    private Uri postImageUri ;

    private StorageReference storageRef ;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    Task<DocumentSnapshot> userRef;
    String userId;
    String uEmail, uName, uImage;

    //Post Details to be edited
    String isUpdateKey;
    String editPostDesc;
    String editPostId;
    ArrayList<Uri> editImagesLists = new ArrayList<>();
    private List<Map<String, Object>> mEditImages;

    ImageView deleteImage ;
    private ViewPagerAdapter mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostDesc = findViewById(R.id.new_post_description);
        newPostBtn = findViewById(R.id.new_post_btn);
        newPostProgress = findViewById(R.id.newpost_progress);
        newPostToolbar = findViewById(R.id.new_post_toolbar);

        new_image_pager = findViewById(R.id.new_post_pager);
        add_more_images = findViewById(R.id.add_more_images);
        mCircleIndicator = findViewById(R.id.circle_indicator);

        addImagesCv = findViewById(R.id.add_images_cv);

        choosed_images = new ArrayList<>();
        images_URLs = new ArrayList<>();

        deleteImage = findViewById(R.id.trash_iv);

        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");


        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();


        //Get data through Intent from the main activity
        Intent mainIntent = getIntent();
        isUpdateKey = ""+mainIntent.getStringExtra("key");
        editPostId  = ""+mainIntent.getStringExtra("editPostId");

        //validate if we came here to update the post
        if(isUpdateKey.equals("editPost")){
            getSupportActionBar().setTitle("Update Post");
            newPostBtn.setText("Update");
            loadPostData(editPostId);
        }else{
            getSupportActionBar().setTitle("Add New Post");
            newPostBtn.setText("Upload");
        }




        //get the user's data
        userRef = firebaseFirestore.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.getResult().exists()){
                        uName = task.getResult().getString("name");
                        uEmail = task.getResult().getString("email");
                        uImage = task.getResult().getString("img");
                     }else{
                         startActivity(new Intent(NewPostActivity.this, RegisterActivity.class));
                     }
            }
        });


        add_more_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImagesFromGallery();
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });
    }

    private void loadPostData(String editPostId) {
        add_more_images.setVisibility(View.VISIBLE);
        addImagesCv.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("posts").whereEqualTo("post_ID",editPostId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments();

                for(DocumentSnapshot qs : snapshotList){
                    ModelPosts editPosts = qs.toObject(ModelPosts.class);

                    String editDescription = editPosts.getDescription();
                    mEditImages = (List<Map<String,Object>>) qs.getData().get("images");

                    for(int i = 0; i< mEditImages.size(); i++){
                        editImagesLists.add(Uri.parse(mEditImages.get(i).get("url").toString()));
                        choosed_images.add(Uri.parse(mEditImages.get(i).get("url").toString()));
                    }


                    //set the data to the views
                    newPostDesc.setText(editDescription);

                    mViewPager = new ViewPagerAdapter(NewPostActivity.this, editImagesLists,1);
                    new_image_pager.setAdapter(mViewPager);
                    new_image_pager.setCurrentItem(0,false);
                    mCircleIndicator.setViewPager(new_image_pager);
                    mViewPager.notifyDataSetChanged();

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(editImagesLists.size() != choosed_images.size()){
            System.out.println("Images choosed are: "+choosed_images);
            System.out.println("Images edited are: "+editImagesLists);
            Toast.makeText(mContext,"Back", Toast.LENGTH_LONG).show();
        }

    }

    private void uploadImages() {
        if(isUpdateKey.equals("editPost")){
            beginUpdate();
        }else {

            for (int i = 0; i < choosed_images.size(); i++) {
                Uri individualImage = choosed_images.get(i);
                if (individualImage != null) {

                    SimpleDateFormat time = new SimpleDateFormat("dd/MM/yyyy hh:mm z");
                    String theTime = time.format(new Date());

                    StorageReference file_path = storageRef.child("post_images").child(String.valueOf(System.currentTimeMillis()) + ".jpg");
                    String description = newPostDesc.getText().toString();

                    newPostProgress.setVisibility(View.VISIBLE);
                    file_path.putFile(individualImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                file_path.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> images = new HashMap<>();
                                            images.put("file_path", file_path.getPath());
                                            images.put("url", String.valueOf(task.getResult()));

                                            imagesMap.add(images);
                                            images_URLs.add(String.valueOf(task.getResult()));
                                            if (images_URLs.size() == choosed_images.size()) {
                                                storeLinksToFirestore(images_URLs, description, theTime, imagesMap);
                                            }
                                            Toast.makeText(NewPostActivity.this, "Post Was Added in storage", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "(Image Storage): " + error, Toast.LENGTH_LONG).show();
                                newPostProgress.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                }
            }
        }
    }

    private void beginUpdate() {
        newPostProgress.setVisibility(View.VISIBLE);
        editPostDesc = newPostDesc.getText().toString();
        SimpleDateFormat time = new SimpleDateFormat("dd/MM/yyyy hh:mm z");
        String theTime = time.format(new Date());
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("description",editPostDesc);
        postMap.put("timestamp",theTime);
        List<Map<String, Object>> image = mEditImages;

        if(image.size() < choosed_images.size())//add new images
            uploadNewImagesToStorage(image, postMap);
        else if(mViewPager.getCount() < choosed_images.size()) //delete existed images
            deleteImagesFromStorages(image,postMap);
        else //edit description only
            updateFireStore(postMap);
    }

    private void deleteImagesFromStorages(List<Map<String, Object>> image, Map<String, Object> postMap) {
        ArrayList<Uri> deletedImages = mViewPager.getDeletedImagesURLs();
        StorageReference picReference;
        for (int i =0 ; i <deletedImages.size() ; i++){
            Uri uri = deletedImages.get(i);
            picReference = FirebaseStorage.getInstance().getReferenceFromUrl(deletedImages.get(i).toString());
            StorageReference finalPicReference = picReference;
            String path = finalPicReference.getPath();
            picReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(NewPostActivity.this, "deleted from storage..", Toast.LENGTH_LONG).show();
                    firebaseFirestore.collection("posts").document(editPostId)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        if(doc.exists()){
                                            Map<String, Object> mapToRemove = null;
                                            if (image != null) {
                                                for(Map<String, Object> map : image){
                                                    if (map.containsKey("file_path")&&map.get("file_path").equals(path) ){
                                                        mapToRemove = map;
                                                        break;
                                                    }
                                                }
                                                if(mapToRemove != null){
                                                    image.remove(mapToRemove);
                                                    postMap.put("images", image);
                                                    firebaseFirestore.collection("posts").document(editPostId)
                                                            .update("images", image).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(NewPostActivity.this, "deleted from firebase..", Toast.LENGTH_LONG).show();

                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                    updateFireStore(postMap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed
                    Toast.makeText(NewPostActivity.this,"ERROR in deleting",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void uploadNewImagesToStorage(List<Map<String, Object>> image, Map<String, Object> postMap) {
        for (int i = image.size(); i < choosed_images.size(); i++) {
            Uri individualImage = choosed_images.get(i);
            if (individualImage != null) {
                StorageReference file_path = storageRef.child("post_images").child(String.valueOf(System.currentTimeMillis()) + ".jpg");
                file_path.putFile(individualImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            file_path.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Map<String, Object> images = new HashMap<>();
                                        images.put("file_path", file_path.getPath());
                                        images.put("url", String.valueOf(task.getResult()));

                                        image.add(images);
                                        images_URLs.add(String.valueOf(task.getResult()));

                                        if (image.size() == choosed_images.size()) {
                                            postMap.put("images", image);
                                            updateFireStore(postMap);
                                        }
                                        Toast.makeText(NewPostActivity.this, "Updated Post Was Added in storage", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(NewPostActivity.this, "(Image Storage): " + error, Toast.LENGTH_LONG).show();
                            newPostProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }
    }

    private void updateFireStore(Map<String, Object> postMap) {
        if(!TextUtils.isEmpty(editPostDesc)) {
            firebaseFirestore.collection("posts").document(editPostId).set(postMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewPostActivity.this, "Post Was Updated", Toast.LENGTH_LONG).show();
                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(NewPostActivity.this, "Can't Add the Post: " + error, Toast.LENGTH_LONG).show();

                    }
                    newPostProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }


    private void storeLinksToFirestore(ArrayList<String> images_urLs, String description, String timeStamp, List<Map<String, Object>> imagesMap) {
        String post_id = String.valueOf(System.currentTimeMillis());
        Map<String, Object> postMap = new HashMap<>();
        String likes = "0";
        String comments = "0";
        List<String> whoLikes = new ArrayList<>();
        postMap.put("images", imagesMap);
        postMap.put("description",description);
        postMap.put("uid", userId);
        postMap.put("post_ID",String.valueOf(System.currentTimeMillis()));
        postMap.put("timestamp", timeStamp);
        postMap.put("email", uEmail);
        postMap.put("name", uName);
        postMap.put("user_img", uImage);
        postMap.put("likes",likes);
        postMap.put("comments",comments);
        postMap.put("whoLikes",whoLikes);

        if(!TextUtils.isEmpty(description) && postImageUri != null) {
            firebaseFirestore.collection("posts").document(post_id).set(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(NewPostActivity.this, "Post Was Added", Toast.LENGTH_LONG).show();
                        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                        finish();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(NewPostActivity.this, "Can't Add the Post: " + error, Toast.LENGTH_LONG).show();

                    }
                    newPostProgress.setVisibility(View.INVISIBLE);
                }
            });
        }

    }

    private void pickImagesFromGallery() {

        if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            //now ask for the permission
            ActivityCompat.requestPermissions(NewPostActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            Toast.makeText(mContext, "Permission Accepted",Toast.LENGTH_LONG).show();
        }else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);
        }


    }




    //This method will be called after picking images from camera or gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 && resultCode == RESULT_OK ){
            if(data !=null){
                if(data.getClipData() != null){
                    int count = data.getClipData().getItemCount();
                    if (count >= 5){
                        Toast.makeText(mContext,"You Should Select less than 5", Toast.LENGTH_LONG).show();
                    }else {
                        for (int i = 0; i < count; i++) {
                            postImageUri = data.getClipData().getItemAt(i).getUri();
                            choosed_images.add(postImageUri);

                        }
                    }
                }else{
                    postImageUri = data.getData();
                    choosed_images.add(data.getData());
                }
                setViewPagerAdapter();
            }

        }
    }



    private void setViewPagerAdapter() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, choosed_images, 0);
        new_image_pager.setAdapter(viewPagerAdapter);
        new_image_pager.setCurrentItem(0,false);
        mCircleIndicator.setViewPager(new_image_pager);
        add_more_images.setVisibility(View.VISIBLE);
        addImagesCv.setVisibility(View.VISIBLE);
        viewPagerAdapter.notifyDataSetChanged();
    }
}