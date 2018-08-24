package com.example.abinashbhattarai.roomiesphotoblog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.Manifest;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolBar;
    private CircleImageView profilePic;
    private Uri mainImageUri=null;
    private EditText setUpName;
    private Button setupButtn;
    private boolean isChanged=false;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private ProgressBar setupProgress;
    private FirebaseFirestore firebaseFirestore;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setupToolBar=findViewById(R.id.setupBar);
        setSupportActionBar(setupToolBar);
        getSupportActionBar().setTitle("Account Setup");

        profilePic=findViewById(R.id.profilePic);
        setUpName=findViewById(R.id.setupName);

        firebaseAuth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        setupButtn=findViewById(R.id.setup_settings_butn);
        setupProgress=findViewById(R.id.setupProgress);
        firebaseFirestore=FirebaseFirestore.getInstance();
        boolean flag=(firebaseAuth.getCurrentUser()==null);
        userId=firebaseAuth.getCurrentUser().getUid();
        Log.i("User id",userId.toString());

        setupProgress.setVisibility(View.VISIBLE);
        setupButtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");
                        setUpName.setText(name);

                        mainImageUri=Uri.parse(image);

                        RequestOptions placeHolderRequest=new RequestOptions();
                        placeHolderRequest.placeholder(R.mipmap.default_profile);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(profilePic);
                    }


                }
                else{
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"Error."+error,Toast.LENGTH_LONG).show();

                }
                setupProgress.setVisibility(View.INVISIBLE);
                setupButtn.setEnabled(true);

            }
        });



        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                     if(ContextCompat.checkSelfPermission(SetupActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                         Toast.makeText(SetupActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                         ActivityCompat.requestPermissions(SetupActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

                     }
                     else{
                        ImagePicker();
                     }
                }else{

                    ImagePicker();
                }
            }
        });

        setupButtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                final String userName = setUpName.getText().toString();

                if (!TextUtils.isEmpty(userName) && mainImageUri != null) {

                        setupProgress.setVisibility(View.VISIBLE);
                        if(isChanged) {


                        StorageReference image_path = storageReference.child("profile_images").child(userId + ".jpg");
                        image_path.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    firebaseStore(task, userName);


                                } else {
                                    String error = task.getException().getMessage();
                                    setupProgress.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }


                            }
                        });


                    }
                        else{
                            firebaseStore(null,userName);
                        }
                }

            }
        });

    }

    private void firebaseStore(Task<UploadTask.TaskSnapshot> task, final String userName) {
        Uri downloadUri;
        if(task!=null) {
            downloadUri = task.getResult().getDownloadUrl();
        }
        else{
            downloadUri=mainImageUri;
        }

        Map<String, String> userMap= new HashMap<>();
        userMap.put("name",userName);
        userMap.put("image",downloadUri.toString());

        firebaseFirestore.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SetupActivity.this,"The user settings are updated with user name" + userName, Toast.LENGTH_LONG).show();
                    Intent mainIntent= new Intent(SetupActivity.this,MainActivity.class);
                    startActivity(mainIntent);
                    finish();




                }
                else{
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"Error."+error,Toast.LENGTH_LONG).show();

                }
                setupProgress.setVisibility(View.INVISIBLE);

            }
        });
    }


    void ImagePicker(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();
                isChanged=true;
                profilePic.setImageURI(mainImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}


