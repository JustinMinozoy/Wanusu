package com.blackwizard.wanusumahana;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton selectPostImage;
    private EditText postDescription;
    private Button updatePostButton;

    private static final int galleryPick = 1;
    private Uri imageUri;

    private String description;
    private StorageReference postImagesReference;

    private String saveCurrentDate, saveCurrentTime, postRandomName;

    private String downloadUrl;

    private DatabaseReference usersReference, postsReference;

    private FirebaseAuth firebaseAuth;

    private String currentUsreID;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUsreID = firebaseAuth.getCurrentUser().getUid();

        postImagesReference = FirebaseStorage.getInstance().getReference();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");


        selectPostImage = findViewById(R.id.updatePostImageButton);
        postDescription = findViewById(R.id.postDescription);
        updatePostButton = findViewById(R.id.updatePostButton);

        loadingBar = new ProgressDialog(this);

        mToolbar = findViewById(R.id.updatePostToolBar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {
        description = postDescription.getText().toString();
        if (imageUri == null) {
            Toast.makeText(this, "Please select post image", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please describe your post", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post!");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            storingImageToFirebaseStorage();
        }
    }

    private void storingImageToFirebaseStorage() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentDate = currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath  = postImagesReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName +".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {

                    // downloadUrl = task.getResult().getDownloadUrl().toString(); the line has an error
                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString(); //corrected with this

                    Toast.makeText(PostActivity.this, "Image Uploaded successfully to Firebase", Toast.LENGTH_SHORT).show();
                    
                    savingPostInformationToDatabase();
                }
                else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error Occured." + message , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savingPostInformationToDatabase() {
        usersReference.child(currentUsreID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("Profile Image").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUsreID);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("postImage", downloadUrl);
                    postMap.put("profileImage", userProfileImage);
                    postMap.put("fullname", userFullName );

                    postsReference.child(currentUsreID + postRandomName).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();

                                Toast.makeText(PostActivity.this, "New Post is updated Successfully", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();
                            }
                            else {
                                Toast.makeText(PostActivity.this, "Error Occured while updating your Post.", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home)
            {
                sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);

    }

    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}