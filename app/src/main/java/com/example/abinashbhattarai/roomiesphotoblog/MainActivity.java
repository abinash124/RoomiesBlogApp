package com.example.abinashbhattarai.roomiesphotoblog;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FloatingActionButton addPostButn;
    private FirebaseFirestore firebaseFirestore;
    private String currentUserId="nonEmpty";
    private BottomNavigationView mainBottomNavigationView;
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseFirestore=FirebaseFirestore.getInstance();
        mainToolbar=(Toolbar) findViewById(R.id.mainToolBar);
        addPostButn=findViewById(R.id.addPostButton);
        mAuth=FirebaseAuth.getInstance();
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Roomies Blog");

        if(mAuth.getCurrentUser()!=null) {
            mainBottomNavigationView = findViewById(R.id.mainBottomNavView);
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();
            replaceFragment(homeFragment);


            mainBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.bottom_nav_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_nav_notification:
                            replaceFragment(notificationFragment);
                            return true;
                        case R.id.bottom_nav_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });


            addPostButn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addPostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(addPostIntent);

                }
            });
        }



        }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser==null){
            sendToLogin();
        }
        else{
            currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){ // If the query/data does not exist (Name and Image field then we send them back to setu[ activity)
                            Intent backMainIntent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(backMainIntent);
                            finish();

                        }
                    }
                    else{
                        String errorMessage=task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error "+errorMessage, Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void sendToLogin(){
        Intent loginIntent= new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout_butn:
                logout();
                return true;

            case R.id.action_account_butn:

                Intent settingsIntent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(settingsIntent);
                return true;
                default:

                    return false;



        }
        //return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    public void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
