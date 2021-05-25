package com.project.android.transtalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    //User Interface
    private EditText mFullName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mRegister;
    private Toolbar mToolbar;
    private CircleImageView mImage;
    private ProgressBar mRegistrationProgressBar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        //User Interface
        mFullName = findViewById(R.id.register_input_user_full_name);
        mEmail = findViewById(R.id.register_input_user_email);
        mPassword = findViewById(R.id.register_input_user_password);
        mRegister = findViewById(R.id.register_button);
        mImage = findViewById(R.id.register_user_image);
        mToolbar = findViewById(R.id.register_app_bar_layout);
        mRegistrationProgressBar = findViewById(R.id.register_progress_bar);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRegistrationProgressBar.setVisibility(View.VISIBLE);
                final String full_name = mFullName.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()) {

                            HashMap<String,String> userHashMap = new HashMap<>();
                            userHashMap.put("Name",full_name);
                            userHashMap.put("Email",email);
                            userHashMap.put("ImageUrl","Default");
                            userHashMap.put("ThumbUrl","Default");
                            userHashMap.put("Status","Hello");
                            userHashMap.put("Verified","false");
                            userHashMap.put("Language","false");
                            userHashMap.put("Key",mAuth.getCurrentUser().getUid());

                            mUserDatabase.child(mAuth.getCurrentUser().getUid()).setValue(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mRegistrationProgressBar.setVisibility(View.INVISIBLE);
                                    Intent verifyIntent = new Intent(RegistrationActivity.this,VerificationActivity.class);
                                    verifyIntent.putExtra("UserId",mAuth.getCurrentUser().getUid());
                                    startActivity(verifyIntent);
                                    finish();

                                }
                            });

                        }

                        else {
                            mRegistrationProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegistrationActivity.this,"Sorry!!!Please Try Again",Toast.LENGTH_SHORT).show();
                            Log.v("RegistrationActivity","Exception="+task.getException());
                        }

                    }
                });

            }
        });
    }

    @Override
    public void onBackPressed() {

        return;
    }
}
