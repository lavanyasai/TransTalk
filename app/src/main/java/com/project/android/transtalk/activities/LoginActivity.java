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

public class LoginActivity extends AppCompatActivity {

    //User Interface
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private Toolbar mToolbar;
    private ProgressBar mProgressBar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //User Interface
        mEmail = findViewById(R.id.login_input_user_email);
        mPassword = findViewById(R.id.login_input_user_password);
        mLogin = findViewById(R.id.login_button);
        mToolbar = findViewById(R.id.login_app_bar_layout);
        mProgressBar = findViewById(R.id.login_progress_bar);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            mProgressBar.setVisibility(View.VISIBLE);
                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.putExtra("UserId", mAuth.getCurrentUser().getUid());
                            startActivity(mainIntent);
                            finish();
                        }

                        else {
                            mProgressBar.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this,"Sorry!!!Please Try Again",Toast.LENGTH_SHORT).show();
                            Log.v("LoginActivity","Exception="+task.getException());

                        }

                    }

                });
            }
        });


    }
}
