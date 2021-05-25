package com.project.android.transtalk.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String mProfileUserId;
    private String mCurrentState;
    private String mCurrentUser;

    //User Interface
    private CircleImageView mProfileUserImage;
    private TextView mProfileUserName;
    private TextView mProfileEmail;
    private Button mSendFriendRequestButton;
    private Button mCancelFriendRequestButton;
    private Toolbar mToolbar;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //User Interface
        mProfileUserImage = findViewById(R.id.profile_user_image);
        mProfileUserName = findViewById(R.id.profile_user_name);
        mProfileEmail = findViewById(R.id.profile_user_email);
        mSendFriendRequestButton = findViewById(R.id.profile_send_request);
        mCancelFriendRequestButton = findViewById(R.id.profile_cancel_request);
        mToolbar = findViewById(R.id.profile_app_bar_layout);

        //Firebase
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mAuth = FirebaseAuth.getInstance();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProfileUserId = getIntent().getStringExtra("key");
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mCurrentState = "NOT_FRIENDS";

        mUsersDatabase.child(mProfileUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String userName = dataSnapshot.child("Name").getValue().toString();
                String userEmail = dataSnapshot.child("Email").getValue().toString();
                String userImageUrl = dataSnapshot.child("ImageUrl").getValue().toString();
                mProfileUserName.setText(userName);
                mProfileEmail.setText(userEmail);
                Picasso.get()
                        .load(userImageUrl)
                        .placeholder(R.drawable.contact_image)
                        .into(mProfileUserImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFriendRequestDatabase.child(mCurrentUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mProfileUserId)) {
                            String request_type = dataSnapshot.child(mProfileUserId).child("request_type").getValue().toString();
                            if (request_type.equals("received")) {

                                mCurrentState = "REQUEST_RECEIVED";
                                mSendFriendRequestButton.setText("Accept Friend Request");
                                mCancelFriendRequestButton.setVisibility(View.VISIBLE);
                                mCancelFriendRequestButton.setEnabled(true);
                            } else if (request_type.equals("sent")) {

                                mCurrentState = "REQUEST_SENT";
                                mSendFriendRequestButton.setText("Cancel Friend Request");
                                mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                mCancelFriendRequestButton.setEnabled(false);
                            }
                        } else {

                            mFriendDatabase.child(mCurrentUser)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(mProfileUserId)) {

                                                mCurrentState = "FRIENDS";
                                                mSendFriendRequestButton.setText("UnFriend Request");

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        mSendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSendFriendRequestButton.setEnabled(false);

                //NOT FRIENDS STATE
                if (mCurrentState.equals("NOT_FRIENDS")) {
//                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this, R.style.DialogTheme);
//                    alertDialog.setTitle("Write a Message");
//                    alertDialog.setView(getLayoutInflater().inflate(R.layout.send_custom_message, null))
//                            .setPositiveButton("Send", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
                                    mFriendRequestDatabase.child(mCurrentUser)
                                            .child(mProfileUserId)
                                            .child("request_type")
                                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mFriendRequestDatabase.child(mProfileUserId)
                                                        .child(mCurrentUser)
                                                        .child("request_type")
                                                        .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        HashMap<String, String> notificationData = new HashMap<>();
                                                        notificationData.put("from", mCurrentUser);
                                                        notificationData.put("type", "request");
                                                        mNotificationDatabase.child(mProfileUserId).push().setValue(notificationData)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        mCurrentState = "REQUEST_SENT";
                                                                        mSendFriendRequestButton.setText("Cancel Friend Request");
                                                                        mSendFriendRequestButton.setEnabled(true);
                                                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_LONG).show();
                                                                        mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        mCancelFriendRequestButton.setEnabled(false);


                                                                    }
                                                                });

                                                    }
                                                });

                                            } else {

                                                Toast.makeText(ProfileActivity.this, "Failed Sending Request", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });
                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.cancel();
//                                }
//                            });
//                    final AlertDialog dialog = alertDialog.create();
//                    dialog.show();
//                }
                //CANCEL REQUEST STATE
                if (mCurrentState.equals("REQUEST_SENT")) {

                    mFriendRequestDatabase.child(mCurrentUser)
                            .child(mProfileUserId)
                            .child("request_type")
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mFriendRequestDatabase.child(mProfileUserId)
                                    .child(mCurrentUser)
                                    .child("request_type")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        mSendFriendRequestButton.setEnabled(true);
                                        mCurrentState = "NOT_FRIENDS";
                                        mSendFriendRequestButton.setText("Send Friend Request");
                                        mSendFriendRequestButton.setEnabled(true);
                                        Toast.makeText(ProfileActivity.this, "Cancelled Request Successfully", Toast.LENGTH_LONG).show();
                                        mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                        mCancelFriendRequestButton.setEnabled(false);

                                    }
                                }
                            });
                        }
                    });
                }

                //ACCEPT STATE
                if (mCurrentState.equals("REQUEST_RECEIVED")) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser)
                            .child(mProfileUserId).child("date")
                            .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(mProfileUserId).child(mCurrentUser).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(mCurrentUser)
                                                    .child(mProfileUserId)
                                                    .child("request_type")
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    mFriendRequestDatabase.child(mProfileUserId)
                                                            .child(mCurrentUser)
                                                            .child("request_type")
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()) {

                                                                mSendFriendRequestButton.setEnabled(true);
                                                                mCurrentState = "FRIENDS";
                                                                mSendFriendRequestButton.setText("UnFriend Request");
                                                                Toast.makeText(ProfileActivity.this, "Friends", Toast.LENGTH_LONG).show();
                                                                mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                mCancelFriendRequestButton.setEnabled(false);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                        }
                    });

                }

                //UNFRIEND STATE
                if (mCurrentState.equals("FRIENDS")) {

                    mFriendDatabase.child(mCurrentUser)
                            .child(mProfileUserId)
                            .child("date")
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mFriendRequestDatabase.child(mProfileUserId)
                                    .child(mCurrentUser)
                                    .child("date")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        mFriendRequestDatabase.child(mCurrentUser)
                                                .child(mProfileUserId)
                                                .child("date")
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()) {

                                                    mSendFriendRequestButton.setEnabled(true);
                                                    mCurrentState ="NOT_FRIENDS";
                                                    mSendFriendRequestButton.setText("Send Friend Request");
                                                    Toast.makeText(ProfileActivity.this,"You are not friend anymore",Toast.LENGTH_LONG).

                                                            show();
                                                    mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                                    mCancelFriendRequestButton.setEnabled(false);

                                                }

                                            }
                                        });

                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        mCancelFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestDatabase.child(mCurrentUser)
                        .child(mProfileUserId)
                        .child("request_type")
                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mFriendRequestDatabase.child(mProfileUserId)
                                .child(mCurrentUser)
                                .child("request_type")
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    mSendFriendRequestButton.setEnabled(true);
                                    mCurrentState = "NOT_FRIENDS";
                                    mSendFriendRequestButton.setText("Send Friend Request");
                                    mSendFriendRequestButton.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, "Cancelled Request Successfully", Toast.LENGTH_LONG).show();
                                    mCancelFriendRequestButton.setVisibility(View.INVISIBLE);
                                    mCancelFriendRequestButton.setEnabled(false);

                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            mUsersDatabase.child(currentUser.getUid()).child("Online").setValue("true");

        }
    }
}