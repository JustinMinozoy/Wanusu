package com.blackwizard.wanusumahana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    //initializing components
    private EditText userName, fullName, Country;
    private Button save;
    private CircleImageView profileImageView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;
    private ProgressDialog loadingBar;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //casting components
        userName = findViewById(R.id.setupUsername);
        fullName = findViewById(R.id.setupFullName);
        Country  = findViewById(R.id.setupCountry);
        profileImageView = findViewById(R.id.setupProfile);
        save  = findViewById(R.id.setupSave);
        loadingBar = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformations();
            }
        });


    }

    private void saveAccountSetupInformations() {
        String username = userName.getText().toString();
        String fullname = fullName.getText().toString();
        String country =  Country.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please Enter Your Username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please Enter Your Full name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please Enter Your Country", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, while we are creating your new account!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username" , username);
            userMap.put("fullname" , fullname);
            userMap.put("country" , country);
            userMap.put("status" , "Hey there! This application is developed by Blackwizard Technology");
            userMap.put("gender" , "none" );
            userMap.put("dob" , "none");
            userMap.put("relationshipstatus" , "none" );
            usersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        loadingBar.dismiss();

                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account has been created successfully!", Toast.LENGTH_LONG ).show();
                    }
                    else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT ).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}