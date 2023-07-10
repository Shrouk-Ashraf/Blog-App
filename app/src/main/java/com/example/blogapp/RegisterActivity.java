package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText reg_email_field;
    EditText reg_password_field;
    EditText reg_confirm_pass;
    Button reg_btn;
    Button reg_login_btn;
    ProgressBar reg_progress;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        reg_email_field = findViewById(R.id.reg_email);
        reg_password_field = findViewById(R.id.reg_pass);
        reg_confirm_pass = findViewById(R.id.reg_confirm_password);
        reg_btn = findViewById(R.id.reg_btn);
        reg_login_btn = findViewById(R.id.reg_login_btn);
        reg_progress = findViewById(R.id.setup_progressbar);

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();// to go back to login and destroy the intent
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email =  reg_email_field.getText().toString();
                String pass = reg_password_field.getText().toString();
                String confirm_pass = reg_confirm_pass.getText().toString();

                if(!TextUtils.isEmpty(email) &!TextUtils.isEmpty(pass) &!TextUtils.isEmpty(confirm_pass)){

                    if(pass.equals(confirm_pass)){
                        reg_progress.setVisibility(View.VISIBLE);
                        //create an account
                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    //instead of going to the main we will go the setup activity to set a name to the user
                                    Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                                    startActivity(intent);
//                                    sendToMain();
                                }else{
                                    Toast.makeText(RegisterActivity.this,"ERROR: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                }
                                reg_progress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }else{
                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password Field don't match each other", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //check if the user logged in or not
        if(currentUser != null){// it means the user already logged in
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent intent = new Intent(RegisterActivity.this, PostsMainActivity.class);
        startActivity(intent);
    }
}