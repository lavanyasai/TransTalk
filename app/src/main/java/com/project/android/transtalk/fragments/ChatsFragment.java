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
import com.project.android.transtalk.adapters.ChatsAdapter;
import com.project.android.transtalk.models.Chats;
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
public class ChatsFragment extends Fragment {


    private RecyclerView mChatsList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mChatsDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private View mMainView;
    private TextView mNoChats;
    private ArrayList<Chats> mChats;
    private ArrayList<String> mChatsKeys;
    private ChatsAdapter chatsAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsList=mMainView.findViewById(R.id.chats_list);
        mAuth = FirebaseAuth.getInstance();
        mChats = new ArrayList<>();
        mChatsKeys = new ArrayList<>();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mNoChats = mMainView.findViewById(R.id.no_chats);
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUserId);
        mChatsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mChatsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mNoChats.setVisibility(View.INVISIBLE);
                if(!mChats.isEmpty()) {
                    mChats.clear();
                }
                if(!mChatsKeys.isEmpty()) {
                    mChatsKeys.clear();
                }
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Chats chats = postSnapshot.getValue(Chats.class);
                    mChatsKeys.add(postSnapshot.getKey());
                    mChats.add(chats);
                }
                if(!mChatsKeys.isEmpty()) {
                    mNoChats.setVisibility(View.INVISIBLE);
                    mChatsList.setVisibility(View.VISIBLE);
                    chatsAdapter = new ChatsAdapter(getContext(), mChats, mChatsKeys);
                    mChatsList.setAdapter(chatsAdapter);
                }
                else {
                    mNoChats.setVisibility(View.VISIBLE);
                    mChatsList.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
