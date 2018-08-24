package com.example.abinashbhattarai.roomiesphotoblog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private Toolbar newPostToolBar;
    private EditText newDescriptionText;
    private Button addButton;
    private ImageView newPostImage;
    private Uri postImageUri=null;
    private ProgressBar postProgressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private Bitmap compressedImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newPostToolBar=findViewById(R.id.newPostToolBar);
        setSupportActionBar(newPostToolBar);
        getSupportActionBar().setTitle("Add new post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        newDescriptionText=findViewById(R.id.newPostDescription);
        addButton=findViewById(R.id.newPosttButn);
        newPostImage=findViewById(R.id.newPostPicture);
        postProgressBar=findViewById(R.id.newPostProgressBar);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();

        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();




        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPostImagePicker();

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String descriptionText=newDescriptionText.getText().toString();
                if(!TextUtils.isEmpty(descriptionText)&& postImageUri!=null){
                    postProgressBar.setVisibility(View.VISIBLE);
                    final String randomName= UUID.randomUUID().toString();

                    StorageReference path=storageReference.child("post_image").child(randomName+ ".jpg"); // Make a storage location using random fileName
                    path.putFile(postImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                final String downloadUri=task.getResult().getDownloadUrl().toString();

                                File actualImageFile= new File(postImageUri.getPath());
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(10)
                                            .compressToBitmap(actualImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                byte[] thumbData=baos.toByteArray();

                                UploadTask uploadData= storageReference.child("post_image/thumbs").child(randomName+".jpg").putBytes(thumbData);
                                uploadData.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadThumb= taskSnapshot.getDownloadUrl().toString();
                                        Map<String, Object> postMap= new HashMap<>();
                                        postMap.put("image_url",downloadUri);
                                        postMap.put("description",descriptionText);
                                        postMap.put("user_id",currentUserId);
                                        postMap.put("thumb_url",downloadThumb);
                                        postMap.put("timestamp",FieldValue.serverTimestamp());


                                        firebaseFirestore.collection("Post").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(NewPostActivity.this,"Post was succesfully added",Toast.LENGTH_LONG).show();
                                                    Intent mainIntent=new Intent(NewPostActivity.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();

                                                }
                                                else{
                                                    String errorMessage=task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this,"Error"+errorMessage,Toast.LENGTH_LONG).show();




                                                }
                                                postProgressBar.setVisibility(View.INVISIBLE);

                                            }
                                        });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });






                            }
                            else{
                                postProgressBar.setVisibility(View.INVISIBLE);
                                String errorMessage=task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this,"Error"+errorMessage,Toast.LENGTH_LONG).show();

                            }

                        }
                    });


                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postImageUri=result.getUri();
                newPostImage.setImageURI(postImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



    void newPostImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512,512)
                .setAspectRatio(1,1)
                .start(NewPostActivity.this);
    }


}
