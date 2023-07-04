package com.example.blogapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelPosts {
    String post_ID,description,email,uid, name, user_img, likes,comments;
    String timestamp;
    List<Map<String, Object>> images =new ArrayList<>();
    List<String> whoLikes = new ArrayList<>();

    public ModelPosts() {
    }

    public ModelPosts(String post_ID, String description, String email, String uid, String name, String user_img, String postLikes, String timestamp, List<Map<String, Object>> images, List<String> whoLike, String comments) {
        this.post_ID = post_ID;
        this.description = description;
        this.email = email;
        this.uid = uid;
        this.name = name;
        this.user_img = user_img;
        this.likes = postLikes;
        this.timestamp = timestamp;
        this.images = images;
        this.whoLikes = whoLike;
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_img() {
        return user_img;
    }

    public void setUser_img(String user_img) {
        this.user_img = user_img;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
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

    public List<String> getWhoLikes() {
        return whoLikes;
    }

    public void setWhoLikes(List<String> whoLikes) {
        this.whoLikes = whoLikes;
    }
}
