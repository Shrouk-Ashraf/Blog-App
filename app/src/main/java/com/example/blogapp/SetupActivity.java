package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    CircleImageView setupimage;
    EditText setupName;
    Button setupBtn;
    Context mContext= SetupActivity.this;;
    ProgressBar setup_Progress;

    private Uri mainImageUri = null;
    boolean isImageChanged = false;

    private StorageReference storageRef ;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Toolbar setup_toolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(setup_toolbar);
        getSupportActionBar().setTitle("Account Setup");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        userId = mAuth.getCurrentUser().getUid();

        setupimage = findViewById(R.id.circleImageView);
        setupName = findViewById(R.id.setup_name);
        setupBtn = findViewById(R.id.setup_btn);
        setup_Progress = findViewById(R.id.setup_progressbar);

        setup_Progress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        //check if the user has already image and name
        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        //when data exists then retrieve it
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("img");

                        mainImageUri = Uri.parse(image);

                        setupName.setText(name);

                        //to show nothing until the glide shows the image we want a placeholder
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile_img);

                        Glide.with(mContext)
                                .setDefaultRequestOptions(placeholderRequest)
                                .load(image)
                                .into(setupimage);
                        Toast.makeText(SetupActivity.this, "Data Exists", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(SetupActivity.this, "ERROR in image Firestore", Toast.LENGTH_LONG).show();
                }
                setup_Progress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });
        //once the user click on the button, set the data in firebase
        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StorageReference image_path = storageRef.child("profile_images").child(userId + ".jpg");
                String userName = setupName.getText().toString();
                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {
                setup_Progress.setVisibility(View.VISIBLE);

                 if (isImageChanged) { // to avoid the error of making two files for same image
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storeToTheFirestore(image_path, userName);
                                } else {
                                    Toast.makeText(SetupActivity.this, "ERROR in image storage", Toast.LENGTH_LONG).show();
                                    setup_Progress.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                }else{
                     storeToTheFirestore(image_path, userName);
                }
                }
            }
        });



        setupimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //now we want the user to select an image from the storage
                    //now lets check if the permission is granted or not
                    if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        //now ask for the permission
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        Toast.makeText(mContext, "Permission Denied",Toast.LENGTH_LONG).show();
                    }else{
                        //now when take the permission get the image and start crop it
                        CropImage.activity()
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setGuidelines(CropImageView.Guidelines.ON) // this will pass the param. to the onActivityResult
                                .setAspectRatio(1,1)
                                .start(SetupActivity.this);
                    }
                }
        });
    }

    private void storeToTheFirestore(StorageReference image_path, String userName) {
        image_path.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloaded_image_uri = task.getResult();
                Map<String,String> userMap = new HashMap<>();
                userMap.put("name", userName);
                userMap.put("img", downloaded_image_uri.toString());
                userMap.put("email",mAuth.getCurrentUser().getEmail());
                userMap.put("uid", userId);

                //add the image url and name to the firestore
                db.collection("users").document(userId).set(userMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "user settings are updater", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(SetupActivity.this, "ERROR in image Firestore", Toast.LENGTH_LONG).show();

                        }
                        setup_Progress.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                //set the image
                setupimage.setImageURI(mainImageUri);
                isImageChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}