package com.example.harold.bustracker;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Choose A Bus Route");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);

        FloatingActionButton signOut = (FloatingActionButton) findViewById(R.id.fab_signout);
        Button busStop1 = (Button) findViewById(R.id.button1);
        Button busStop2 = (Button) findViewById(R.id.button2);
        Button busStop3 = (Button) findViewById(R.id.button3);
        Button busStop4 = (Button) findViewById(R.id.button4);
        Button busStop5 = (Button) findViewById(R.id.button5);

        busStop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusStop", 0);
                i.putExtra("RouteNumber", 567);
                startActivity(i);
            }
        });

        busStop2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusStop", 1);
                i.putExtra("RouteNumber", 424);
                startActivity(i);
            }
        });

        busStop3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusStop", 2);
                i.putExtra("RouteNumber", 434);
                startActivity(i);
            }
        });

        busStop4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusStop", 3);
                i.putExtra("RouteNumber", 423);
                startActivity(i);
            }
        });

        busStop5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, User.class);
                i.putExtra("BusStop", 4);
                i.putExtra("RouteNumber", 423);
                startActivity(i);
            }
        });

        mAuth = FirebaseAuth.getInstance();


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}