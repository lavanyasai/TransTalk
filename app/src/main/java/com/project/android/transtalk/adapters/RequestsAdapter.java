package com.project.android.transtalk.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder> {

    private ArrayList<String> mRequestsKeys = new ArrayList<>();
    private DatabaseReference mUsersDatabase;
    Context mContext;

    public RequestsAdapter(Context context, ArrayList<String> requestsKeys) {
        mContext = context;
        mRequestsKeys = requestsKeys;
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.received_request_item, parent, false);

        return new RequestsViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final RequestsViewHolder holder, final int position) {

        holder.bind(mRequestsKeys.get(holder.getAdapterPosition()));

    }

    @Override
    public int getItemCount() {
        return mRequestsKeys.size();
    }

    public class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Context mContext;
        private DatabaseReference mUsersDatabase;
        private DatabaseReference mFriendRequestDatabase;
        private DatabaseReference mFriendDatabase;
        private FirebaseAuth mAuth;
        private String mCurrentUser;

        protected RequestsViewHolder(View itemView, Context context) {
            super(itemView);

            mView = itemView;
            mContext = context;
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest");
            mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
            mAuth = FirebaseAuth.getInstance();
            mCurrentUser = mAuth.getCurrentUser().getUid();
        }

        public void bind(final String userKey) {

            mUsersDatabase.child(userKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String userName = dataSnapshot.child("Name").getValue().toString();
                    String userStatus = dataSnapshot.child("Status").getValue().toString();
                    final String userImage = dataSnapshot.child("ImageUrl").getValue().toString();
                    String userOnline = dataSnapshot.child("Online").getValue().toString();

                    Log.v("Adapter", "Adapter=" + userOnline);
                    TextView nameTextView = mView.findViewById(R.id.received_friend_name);
                    nameTextView.setText(userName);
                    TextView statusTextView = mView.findViewById(R.id.received_friend_status);
                    statusTextView.setText(userStatus);
                    CircleImageView userImageView = mView.findViewById(R.id.received_friend_image);
                    Picasso.get()
                            .load(userImage)
                            .placeholder(R.drawable.contact_image)
                            .into(userImageView);
                    ImageButton acceptView = mView.findViewById(R.id.received_friend_accept_request);
                    acceptView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                            mFriendDatabase.child(mCurrentUser)
                                    .child(userKey).child("date")
                                    .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendDatabase.child(userKey).child(mCurrentUser).child("date").setValue(currentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mFriendRequestDatabase.child(mCurrentUser)
                                                            .child(userKey)
                                                            .child("request_type")
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            mFriendRequestDatabase.child(userKey)
                                                                    .child(mCurrentUser)
                                                                    .child("request_type")
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        Toast.makeText(mContext, "Request Accepted", Toast.LENGTH_SHORT).show();
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
                    });
                    ImageButton declineView = mView.findViewById(R.id.received_friend_reject_request);
                    declineView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFriendRequestDatabase.child(mCurrentUser)
                                    .child(userKey)
                                    .child("request_type")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mFriendRequestDatabase.child(userKey)
                                            .child(mCurrentUser)
                                            .child("request_type")
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                Toast.makeText(mContext, "Request Rejected", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}

