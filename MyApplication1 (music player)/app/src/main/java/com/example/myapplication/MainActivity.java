package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Start the playlist activity
        Intent intent = new Intent(this, TrackListActivity.class);
        startActivity(intent);
        finish(); // Close HomeActivity since we don't need it anymore
    }
}