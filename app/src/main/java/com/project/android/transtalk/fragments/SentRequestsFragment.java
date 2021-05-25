package com.project.android.transtalk.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.adapters.SentRequestsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SentRequestsFragment extends Fragment {


    private RecyclerView mRequestsList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mFriendRequestsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private TextView mNoSentRequests;
    private View mMainView;
    private ArrayList<String> mRequestsKeys;
    private SentRequestsAdapter requestsAdapter;

    public SentRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_sent_requests, container, false);

        mRequestsList=mMainView.findViewById(R.id.sent_requests_list);
        mAuth = FirebaseAuth.getInstance();
        mRequestsKeys = new ArrayList<>();
        mNoSentRequests = mMainView.findViewById(R.id.no_sent_requests);
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendRequestsDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest").child(mCurrentUserId);
        mFriendRequestsDatabase.keepSynced(true);
        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mFriendRequestsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mNoSentRequests.setVisibility(View.INVISIBLE);
                if(!mRequestsKeys.isEmpty()) {
                    mRequestsKeys.clear();
                }
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String key = postSnapshot.getKey();
                    String request_type = dataSnapshot.child(key).child("request_type").getValue().toString();
                    if(request_type.equals("sent")) {
                        mRequestsKeys.add(postSnapshot.getKey());
                    }
                }
                if(!mRequestsKeys.isEmpty()) {
                    mNoSentRequests.setVisibility(View.INVISIBLE);
                    mRequestsList.setVisibility(View.VISIBLE);
                    requestsAdapter = new SentRequestsAdapter(getContext(), mRequestsKeys);
                    mRequestsList.setAdapter(requestsAdapter);
                }
                else {
                    mNoSentRequests.setVisibility(View.VISIBLE);
                    mRequestsList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
