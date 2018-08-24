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

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerConfirmPassword;
    private FirebaseAuth mAuth;
    private Button registerButn;
    private Button loginMenuButn;
    private ProgressBar signUpProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmail=findViewById(R.id.signUpemail);
        registerPassword=findViewById(R.id.signUpPassword);
        registerConfirmPassword=findViewById(R.id.signUpconfirmPswd);
        registerButn=(Button)findViewById(R.id.signUpButton);
        loginMenuButn=(Button)findViewById(R.id.backLogInButn);
        signUpProgress=findViewById(R.id.signUpProgress);
        mAuth=FirebaseAuth.getInstance();

        registerButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reg_email=registerEmail.getText().toString();
                String reg_password=registerPassword.getText().toString();
                String reg_confirmPassword=registerConfirmPassword.getText().toString();
                if(!(TextUtils.isEmpty(reg_email)&&TextUtils.isEmpty(reg_confirmPassword)&&TextUtils.isEmpty(reg_confirmPassword))){
                    if(reg_password.equals(reg_confirmPassword)){
                        signUpProgress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(reg_email,reg_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    Intent setup= new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setup);
                                    finish();

                                }
                                else{
                                    String error=task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error: "+ error,Toast.LENGTH_LONG).show();
                                }
                                signUpProgress.setVisibility(View.INVISIBLE);


                            }
                        });


                    }
                    else{
                        Toast.makeText(RegisterActivity.this,"Password field doesn't match",Toast.LENGTH_LONG).show();

                    }


                }
                else{
                    Toast.makeText(RegisterActivity.this,"All the feilds are required for registeration",Toast.LENGTH_LONG).show();
                }
            }
        });

        loginMenuButn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backLogin=new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(backLogin);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
                sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
