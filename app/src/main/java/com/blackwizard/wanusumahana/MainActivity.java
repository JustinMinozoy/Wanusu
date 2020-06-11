package com.blackwizard.wanusumahana;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference UserReference, postReference;

    private CircleImageView navigationProfileImageView;
    private TextView navigationProfileUsername;

    String currentUserID;

    private ImageButton addNewPostButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();


        currentUserID = firebaseAuth.getCurrentUser().getUid(); //this line has a problem can still be removed later after modification
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        toolbar = findViewById(R.id.mainPageToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawerLayout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigationView);

        postList = findViewById(R.id.all_users_posts_lists);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navigationProfileImageView = navView.findViewById(R.id.nav_profile);
        navigationProfileUsername = navView.findViewById(R.id.nav_profile_username);

        addNewPostButton = findViewById(R.id.addNewPost);

        UserReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {

                    if (dataSnapshot.hasChild("fullname"))
                    {
                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        navigationProfileUsername.setText(fullName);
                    }
                    if (dataSnapshot.hasChild("Profile Image"))
                    {
                        String image = dataSnapshot.child("Profile Image").getValue().toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navigationProfileImageView);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile Name do not exist", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                UserMenuSelector(item);

                return false;

            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });

        displayAllUsersPosts();

    }






    private void displayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Posts, postsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, postsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                postsViewHolder.class,
                                postReference
                        )
                {
                    @Override
                    protected void populateViewHolder(postsViewHolder viewHolder, Posts model, int position)
                    {
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        //viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());
                        //viewHolder.setP(getApplicationContext(), model.getPostimage());
                        viewHolder.setProfileImage(getApplicationContext(), model.getProfileImage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostImage());
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }











    public static class postsViewHolder extends RecyclerView.ViewHolder{

        View view;

        public postsViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;

        }

        public void setFullname(String fullname)
        {
            TextView username = view.findViewById(R.id.postUsername);
            username.setText(fullname);
        }

        public void setProfileImage(Context ctx, String profileImage)
        {
            CircleImageView image = view.findViewById(R.id.postProfileImage);
           // Picasso.get(ctx).load(profileImage).into(image);
            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(image);
        }
        public void setTime(String time)
        {
            TextView PostTime = view.findViewById(R.id.postTime);
            PostTime.setText("   " + time);
        }
        public void setDate(String date)
        {
            TextView PostDate = view.findViewById(R.id.postDate);
            PostDate.setText("   " + date);
        }
        public void setDescription(String description)
        {
            TextView PostDescription = view.findViewById(R.id.postDescription);
            PostDescription.setText(description);
        }
        public void setPostimage(Context ctx1,  String postimage)
        {
            ImageView PostImage = view.findViewById(R.id.postImage);
            Picasso.get().load(postimage).into(PostImage);
        }

    }

    private void sendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);

    }

    //this checks whether the user is registered or not
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null){
            SendUserToLoginActivity();
        }

        //checking whether the user exists in the database
        else
        {
            CheckUserExistance();

        }

    }

    private void CheckUserExistance() {
        final String current_user_id = firebaseAuth.getCurrentUser().getUid();

        UserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }

        );
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                sendUserToPostActivity();
                break;

            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                firebaseAuth.signOut();

                SendUserToLoginActivity();

                break;

        }
    }
}