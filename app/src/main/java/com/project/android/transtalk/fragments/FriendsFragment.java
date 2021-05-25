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
import com.project.android.transtalk.adapters.FriendsAdapter;
import com.project.android.transtalk.models.Friends;
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
public class FriendsFragment extends Fragment {

    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;
    private TextView mNoFriends;
    private ArrayList<Friends> mFriends;
    private ArrayList<String> mFriendsKeys;
    private FriendsAdapter friendsAdapter;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList=mMainView.findViewById(R.id.friends_list);
        mAuth = FirebaseAuth.getInstance();
        mFriends = new ArrayList<>();
        mFriendsKeys = new ArrayList<>();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mNoFriends = mMainView.findViewById(R.id.no_friends);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mNoFriends.setVisibility(View.INVISIBLE);
                if(!mFriends.isEmpty()) {
                    mFriends.clear();
                }
                if(!mFriendsKeys.isEmpty()) {
                    mFriendsKeys.clear();
                }
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Friends friends = postSnapshot.getValue(Friends.class);
                    if(!postSnapshot.getKey().equals(mCurrentUserId)) {
                        mFriendsKeys.add(postSnapshot.getKey());
                        mFriends.add(friends);
                    }
                }
                if(!mFriendsKeys.isEmpty()) {
                    mNoFriends.setVisibility(View.INVISIBLE);
                    mFriendsList.setVisibility(View.VISIBLE);
                    friendsAdapter = new FriendsAdapter(getContext(), mFriends, mFriendsKeys);
                    mFriendsList.setAdapter(friendsAdapter);
                }
                else {
                    mNoFriends.setVisibility(View.VISIBLE);
                    mFriendsList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
