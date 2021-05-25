package com.project.android.transtalk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.project.android.transtalk.R;
import com.project.android.transtalk.fragments.AccountsFragment;
import com.project.android.transtalk.fragments.HomeFragment;
import com.project.android.transtalk.fragments.SentRequestsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    //User Interface
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton mAddFriends;
    private CircleImageView mNavigationDisplayImage;
    private TextView mNavigationDisplayName;
    private TextView mNavigationDisplayEmail;
    private CircleImageView mImageView;
    private TextView mNameView;
    private ActionBar actionBar;
    private View action_bar_view;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private String mCurrentUser;

    private String savedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            savedFragment = savedInstanceState.getString("Fragment");
        }
        //Firebase
        mAuth =FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        setContentView(R.layout.content_navigation_main);

        mToolbar = findViewById(R.id.main_app_bar_layout);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mAddFriends = findViewById(R.id.fab);

        setSupportActionBar(mToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        action_bar_view = inflater.inflate(R.layout.custom_main_action_bar,null);

        mNameView = action_bar_view.findViewById(R.id.custom_main_bar_name);
        mImageView = action_bar_view.findViewById(R.id.custom_main_bar_image);

        mAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent usersIntent = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(usersIntent);
            }
        });

        if(savedFragment == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
        }
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navigationHeaderView = navigationView.getHeaderView(0);
        mNavigationDisplayImage = navigationHeaderView.findViewById(R.id.navigation_display_image);
        mNavigationDisplayName = navigationHeaderView.findViewById(R.id.navigation_display_name);
        mNavigationDisplayEmail = navigationHeaderView.findViewById(R.id.navigation_display_email);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.openDrawer(GravityCompat.START);

            }
        });
        navigationHeaderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new AccountsFragment())
                        .commit();
                mDrawerLayout.closeDrawer(GravityCompat.START);

            }
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected (MenuItem menuItem){

                        String mTitle=null;
                        Fragment fragment=null;
                        switch(menuItem.getItemId()) {
                            case R.id.navigation_home:
                                mTitle=getString(R.string.app_name);
                                fragment=new HomeFragment();
                                break;
                            case R.id.navigation_account:
                                mTitle=getString(R.string.account);
                                fragment=new AccountsFragment();
                                break;
                            case R.id.navigation_sent_requests:
                                mTitle="Sent Requests";
                                fragment=new SentRequestsFragment();
                                break;
                            case R.id.navigation_logout:
                                mAuth.signOut();
                                sendToStart();
                                break;
                        }
                        if(mTitle!=null && fragment!=null) {
                            if (getSupportActionBar() != null)
                                getSupportActionBar().setTitle(mTitle);
                            DrawerLayout drawer = findViewById(R.id.drawer_layout);
                            drawer.closeDrawers();

                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.content_frame, fragment)
                                    .commit();
                        }
                    return true;
                }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser() != null) {
            //mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("Online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        if (currentUser == null) {
            sendToStart();
        }
        else {
            mCurrentUser = currentUser.getUid();
            mUsersDatabase.child(mCurrentUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String verify = dataSnapshot.child("Verified").getValue().toString();
                    String language = dataSnapshot.child("Language").getValue().toString();
                    if (verify.equals("false")) {
                        sendToVerification();
                    } else if (language.equals("false")) {
                        sendToLanguage();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });
            mUsersDatabase.child(currentUser.getUid()).child("Online").setValue("true");
            mUsersDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("Name").getValue().toString();
                    final String image = dataSnapshot.child("ImageUrl").getValue().toString();
                    String email = dataSnapshot.child("Email").getValue().toString();

                    mNavigationDisplayName.setText(name);
                    mNavigationDisplayEmail.setText(email);
                    if (!image.equals("Default")) {

                        Picasso.get()
                                .load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.contact_image)
                                .into(mNavigationDisplayImage, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                        Picasso.get()
                                                .load(image)
                                                .placeholder(R.drawable.contact_image)
                                                .into(mNavigationDisplayImage);

                                    }
                                });

                    }
                    if (!image.equals("Default")) {

                        Picasso.get()
                                .load(image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.contact_image)
                                .into(mImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                        Picasso.get()
                                                .load(image)
                                                .placeholder(R.drawable.contact_image)
                                                .into(mImageView);

                                    }
                                });

                    }
                    actionBar.setCustomView(action_bar_view);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void sendToVerification() {
        Intent startIntent = new Intent(MainActivity.this,VerificationActivity.class);
        startActivity(startIntent);
        finish();
    }

    private void sendToLanguage() {
        Intent startIntent = new Intent(MainActivity.this,LanguageActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}


