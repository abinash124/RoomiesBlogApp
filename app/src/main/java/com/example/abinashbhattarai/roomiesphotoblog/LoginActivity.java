package com.example.abinashbhattarai.roomiesphotoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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

public class LoginActivity extends AppCompatActivity {
    private EditText loginText;
    private EditText passwordText;
    private Button loginButton;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginText=(EditText)findViewById(R.id.signUpemail);
        passwordText=(EditText)findViewById(R.id.signUpPassword);
        loginButton=(Button)findViewById(R.id.loginButton);
        signUpButton=(Button)findViewById(R.id.signUpButtn);
        mAuth=FirebaseAuth.getInstance();
        loginProgress=(ProgressBar)findViewById(R.id.loginProgress);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail=loginText.getText().toString();
                String loginPassword=passwordText.getText().toString();
                if(!(TextUtils.isEmpty(loginEmail)&&TextUtils.isEmpty(loginPassword))   ){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                startMain();

                            }
                            else{
                                String errorMessage=task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error " +errorMessage,Toast.LENGTH_LONG).show();

                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUp=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(signUp);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    protected void startMain(){
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();

        }
    }


}
