package com.example.harold.bustracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.harold.bustracker.AccountActivity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Admin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin);

        mAuth = FirebaseAuth.getInstance();

        signOut = (Button) findViewById(R.id.logOut);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
                startActivity(new Intent(Admin.this, LoginActivity.class));
                finish();
            }
        });





    }


}
