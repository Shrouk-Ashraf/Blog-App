package com.example.blogapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelPosts {
    String post_ID,description,email,uid, name, user_img;
    String timestamp;
    List<Map<String, Object>> images =new ArrayList<>();

    public ModelPosts() {
    }

    public ModelPosts(String post_ID, String description, String email, String uid, String timestamp,
                      String uImage, List<Map<String, Object>> images, String name) {
        this.post_ID = post_ID;
        this.description = description;
        this.email = email;
        this.name = name;
        this.uid = uid;
        this.timestamp = timestamp;
        this.images = images;
        this.user_img = uImage;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPost_ID() {

        return post_ID;
    }

    public void setPost_ID(String post_ID) {
        this.post_ID = post_ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<Map<String, Object>> getImages() {
        return images;
    }

    public void setImages(List<Map<String, Object>> images) {
        this.images = images;
    }
}
