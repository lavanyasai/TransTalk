package com.project.android.transtalk.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.adapters.UsersAdapter;
import com.project.android.transtalk.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersActivity extends AppCompatActivity {

    //User Interface
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private UsersAdapter usersAdapter;
    private ArrayList<Users> mUsers;
    private TextView mNoUsers;

    //Firebase
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = findViewById(R.id.users_app_bar_layout);
        mNoUsers = findViewById(R.id.no_users);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsers = new ArrayList<>();
        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUserId != null) {

            mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("Online").setValue("true");

        }

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!mUsers.isEmpty()) {
                    mUsers.clear();
                }
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Users users = postSnapshot.getValue(Users.class);
                    if(!postSnapshot.getKey().equals(mCurrentUserId)) {
                        mUsers.add(users);
                    }
                }
                if(!mUsers.isEmpty()) {
                    mNoUsers.setVisibility(View.INVISIBLE);
                    mUsersList.setVisibility(View.VISIBLE);
                    usersAdapter = new UsersAdapter(getApplicationContext(), mUsers);
                    mUsersList.setAdapter(usersAdapter);
                    RecyclerView.LayoutManager layoutManager =
                            new LinearLayoutManager(UsersActivity.this);
                    mUsersList.setLayoutManager(layoutManager);
                    mUsersList.setHasFixedSize(true);
                }
                else {
                    mNoUsers.setVisibility(View.VISIBLE);
                    mUsersList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
