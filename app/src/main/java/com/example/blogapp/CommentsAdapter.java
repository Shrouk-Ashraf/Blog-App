package com.example.blogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.viewHolder> {
    Context mContext ;
    List<ModelComments> commentList;
    String myUid, postId;

    public CommentsAdapter(Context context, List<ModelComments> commentList, String myUid, String postId) {
        mContext = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_comments,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getName();
        String email = commentList.get(position).getEmail();
        String image = commentList.get(position).getUser_img();
        String cid = commentList.get(position).getcId();
        String comment  = commentList.get(position).getComment();
        String timestamp = commentList.get(position).getTimestamp();

        Glide.with(mContext).load(image)
                .placeholder(R.drawable.default_profile_img).into(holder.userIv);
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.timeTv.setText(timestamp);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myUid.equals(uid)){
                    //my comment
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure to delete this comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //delete comment
                            deleteComment(cid,position);
                            notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }else{
                    Toast.makeText(mContext,"Can't delete other's comment",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComment(String cid, int position) {
        FirebaseFirestore.getInstance().collection("posts").document(postId).collection("comments")
                .document(cid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        commentList.remove(position);
                        notifyDataSetChanged();
                    }
                });
        Map<String , Object> data = new HashMap<>();
        FirebaseFirestore.getInstance().collection("posts").whereEqualTo("post_ID",postId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> snapshotList = queryDocumentSnapshots.getDocuments() ;
                for(DocumentSnapshot qs : snapshotList) {
                        ModelPosts post = qs.toObject(ModelPosts.class);
                        String comments = "" + post.getComments();
                        Integer newComment = Integer.parseInt(comments)-1;
                        data.put("comments",newComment.toString());
                        FirebaseFirestore.getInstance().collection("posts").document(postId).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(mContext,"Comment count updated",Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            }
                        });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder{
        ImageView userIv;
        TextView nameTv, commentTv, timeTv;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            userIv = itemView.findViewById(R.id.userIV);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
