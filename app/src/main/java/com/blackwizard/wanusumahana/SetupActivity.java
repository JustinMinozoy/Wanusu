package com.blackwizard.wanusumahana;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText userName, fullName, country;
    private Button save;
    private CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        userName = findViewById(R.id.setupUsername);
        fullName = findViewById(R.id.setupFullName);
        country  = findViewById(R.id.setupCountry);
        profileImageView = findViewById(R.id.setupProfile);
        save  = findViewById(R.id.setupSave);

    }
}