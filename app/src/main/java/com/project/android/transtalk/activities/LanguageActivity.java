package com.project.android.transtalk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LanguageActivity extends AppCompatActivity {

    //User Interface
    private Toolbar mToolbar;
    private Spinner mSpinner;
    private Button mSetLanguage;
    private ProgressBar mProgressBar;

    private String[] languages={
            "English",
            "Hindi",
            "Marathi",
            "Tamil",
            "Telugu",
    };

    //Firebase
    FirebaseAuth mAuth;
    DatabaseReference mUsersDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        mToolbar = findViewById(R.id.language_app_bar_layout);
        mSpinner = findViewById(R.id.languages_list);
        mSetLanguage = findViewById(R.id.language_set);
        mProgressBar = findViewById(R.id.language_progress_bar);

        ArrayAdapter<String> languagesAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.languages));
        languagesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(languagesAdapter);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        mSetLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                String languageCode;
                int position = mSpinner.getSelectedItemPosition();
                switch (position) {
                    case 1:
                        languageCode = "en";
                        break;
                    case 2:
                        languageCode = "te";
                        break;
                    case 3:
                        languageCode = "es";
                        break;
                    case 4:
                        languageCode = "hi";
                        break;
                    case 5:
                        languageCode = "mr";
                        break;
                    case 6:
                        languageCode = "ta";
                        break;
                    case 7:
                        languageCode = "zh-CN";
                        break;
                    case 8:
                        languageCode = "ko";
                        break;
                    default:
                        languageCode = "en";
                }
                mUsersDatabase.child("Language").setValue(languageCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(LanguageActivity.this, "Language Set Successfully", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                            Intent mainIntent = new Intent(LanguageActivity.this, MainActivity.class);
                            mainIntent.putExtra("UserId", getIntent().getStringExtra("UserId"));
                            startActivity(mainIntent);
                            finish();
                        }
                    }
                });
            }
        });
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Select Language");
    }

    @Override
    public void onBackPressed() {

        return;
    }

}
