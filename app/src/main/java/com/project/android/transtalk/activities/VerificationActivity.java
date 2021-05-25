package com.project.android.transtalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.project.android.transtalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerificationActivity extends AppCompatActivity {

    //User Interface
    private Button mVerify;
    private TextView mLabel;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        //User Interface
        mVerify = findViewById(R.id.verify_button);
        mLabel = findViewById(R.id.verify_email_label);
        mToolbar = findViewById(R.id.verify_app_bar_layout);
        mProgressBar = findViewById(R.id.verify_progress_bar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Verify Your Email");

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLabel.setText("Email has been sent to " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        mAuth.getCurrentUser().sendEmailVerification()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(VerificationActivity.this,"Verification email sent to " + mAuth.getCurrentUser().getEmail(),Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(VerificationActivity.this,"Could Not Send Verification Email. Please Try Again",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });

        mVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                //if(mAuth.getCurrentUser().isEmailVerified()) {
                mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("Verified").setValue("true");
                Intent languageIntent = new Intent(VerificationActivity.this, LanguageActivity.class);
                languageIntent.putExtra("UserId", getIntent().getStringExtra("UserId"));
                mProgressBar.setVisibility(View.INVISIBLE);
                startActivity(languageIntent);
                finish();
//                }
//                else {
//                    Toast.makeText(VerificationActivity.this, "Please Verify Your Email", Toast.LENGTH_SHORT).show();
//                }

            }
        });
    }

    @Override
    public void onBackPressed() {

        return;
    }
}