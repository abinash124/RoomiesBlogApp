package com.example.abinashbhattarai.roomiesphotoblog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Date;
import java.text.DateFormat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private List<BlogPost> blog_post_list;
    private Context context;
    private LayoutInflater myInflater;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public BlogRecyclerAdapter( List<BlogPost> blogPostList){
        this.blog_post_list=blogPostList;
        Log.i("Items", "Added");

    }

        @Override

    //Inflate the blogList
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view= myInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
            Log.i("Inflation: ", "Successfully inflated");
        context=parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }





    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final String blogPostId=blog_post_list.get(position).BlogPostId;
        final String blogUserId=blog_post_list.get(position).getUser_id();
        final String currentUserId=firebaseAuth.getCurrentUser().getUid();

        final String desc_data=blog_post_list.get(position).getDescription(); // Gets the description data
        holder.setDescriptionText(desc_data);

        String imageUri=blog_post_list.get(position).getImage_url();
        String userId=blog_post_list.get(position).getUser_id();
        String thumbUri=blog_post_list.get(position).getThumb_url();
        holder.setBlogImage(imageUri,thumbUri);



        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String userName=task.getResult().getString("name");

                    String userImage=task.getResult().getString("image");

                    holder.setUserData(userName,userImage);
                }
                else{

                    String exception=task.getException().toString();
                    Log.i("Error: ", exception);

                }

            }
        });

        long milliseconds=blog_post_list.get(position).getTimestamp().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(milliseconds));
        holder.setTime(dateString);
        firebaseFirestore.collection("Post/" + blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty()){
                    holder.setLikeCount(documentSnapshots.size());

                }
                else{
                    holder.setLikeCount(0);

                }
            }
        });

        firebaseFirestore.collection("Post/" + blogPostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.imageLikeButn.setImageDrawable(context.getDrawable(R.mipmap.action_like_red));

                }
                else{
                    holder.imageLikeButn.setImageDrawable(context.getDrawable(R.mipmap.action_like_white));
                }
            }
        });

        holder.imageLikeButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Post/" + blogPostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.isSuccessful()){

                            Map<String,Object> likeMap=new HashMap<>();
                            likeMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Post/" + blogPostId+"/Likes").document(currentUserId).set(likeMap);
                        }
                        else{
                            firebaseFirestore.collection("Post/" + blogPostId+"/Likes").document(currentUserId).delete();
                        }

                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        Log.i("Size: ", Integer.toString(blog_post_list.size()));
        return blog_post_list.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView descView;
        private TextView userName;
        private CircleImageView userImage;
        private View mView;
        private ImageView blogPostView;
        private TextView blogDate;


        private ImageView imageLikeButn;
        private ImageView imageCommentButn;
        private TextView blogLikeCount;


        public ViewHolder(View itemView) {
            super(itemView);
            this.mView=itemView;
            imageLikeButn=mView.findViewById(R.id.actionLike);
            imageCommentButn=mView.findViewById(R.id.actionComment);


        }

        public void setDescriptionText(String descText){
            descView=mView.findViewById(R.id.blog_description);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUri){
            blogPostView=mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions=new RequestOptions();
            requestOptions.placeholder(R.drawable.imageplaceholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri)).into(blogPostView);
        }

        public void setUserData(String name, String image){
            userName=mView.findViewById(R.id.blog_user_name);
            userImage=mView.findViewById(R.id.blog_user_image);

            userName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.thumbnailplaceholder);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(userImage);
        }

        public void setTime(String time) {
            blogDate=mView.findViewById(R.id.blog_post_date);
            blogDate.setText(time);



        }

        public void setLikeCount(int count){
            blogLikeCount.setText(count+ "Likes");
        }

    }
}
