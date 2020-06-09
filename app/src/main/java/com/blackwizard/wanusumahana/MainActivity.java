package com.blackwizard.wanusumahana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
//import android.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseRegistrar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference UserReference;

    private CircleImageView navigationProfileImageView;
    private TextView navigationProfileUsername;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        //
        //different suggestion
        //currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        // if (firebaseAuth.getCurrentUser() == null) {
        //            SendUserToLoginActivity();
        //        }
        //        else{
        //            currentUserID = firebaseAuth.getCurrentUser().getUid();
        //        }
        //
        //
        //different suggestion
        //if(firebaseAuth.getCurrentUser().getUid() == null){
        //            currentUserID = "";
        //        }else{
        //            currentUserID = firebaseAuth.getCurrentUser().getUid();
        //        }
        //
        //
        //
        ////different suggestion
        //firebaseAuth=FirebaseAuth.getInstance();
        //        if(firebaseAuth.getCurrentUser()!=null) {
        //            currentUserID = firebaseAuth.getCurrentUser().getUid();
        //
        //        }
        //        else{
        //            currentUserID ="";
        //        }




        currentUserID = firebaseAuth.getCurrentUser().getUid(); //this line has a problem can still be removed later after modification
        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.mainPageToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        drawerLayout = findViewById(R.id.drawerLayout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigationView);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navigationProfileImageView = navView.findViewById(R.id.nav_profile);
        navigationProfileUsername = navView.findViewById(R.id.nav_profile_username);

        UserReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String fullName = dataSnapshot.child("fullname").getValue().toString();
                    String image = dataSnapshot.child("Profile Image").getValue().toString();

                    navigationProfileUsername.setText(fullName);
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(navigationProfileImageView);
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