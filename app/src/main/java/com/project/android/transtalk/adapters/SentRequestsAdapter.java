package com.project.android.transtalk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SentRequestsAdapter extends RecyclerView.Adapter<SentRequestsAdapter.SentRequestsViewHolder> {

    private ArrayList<String> mRequestsKeys = new ArrayList<>();
    private DatabaseReference mUsersDatabase;
    Context mContext;

    public SentRequestsAdapter(Context context, ArrayList<String> requestsKeys) {
        mContext=context;
        mRequestsKeys=requestsKeys;
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public SentRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sent_requests, parent, false);

        return new SentRequestsViewHolder(view,mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final SentRequestsViewHolder holder, final int position) {

        holder.bind(mRequestsKeys.get(holder.getAdapterPosition()));

    }

    @Override
    public int getItemCount() {
        return mRequestsKeys.size();
    }

    public class SentRequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Context mContext;
        private DatabaseReference mUsersDatabase;
        private DatabaseReference mFriendRequestDatabase;
        private DatabaseReference mFriendDatabase;
        private FirebaseAuth mAuth;
        private String mCurrentUser;

        protected SentRequestsViewHolder(View itemView,Context context) {
            super(itemView);

            mView = itemView;
            mContext=context;
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
                    String userImage = dataSnapshot.child("ImageUrl").getValue().toString();

                    TextView nameTextView = mView.findViewById(R.id.sent_request_friend_name);
                    nameTextView.setText(userName);
                    CircleImageView userImageView = mView.findViewById(R.id.sent_request_friend_image);
                    Picasso.get()
                            .load(userImage)
                            .placeholder(R.drawable.contact_image)
                            .into(userImageView);
                    Button withDrawView = mView.findViewById(R.id.sent_friend_withdraw);
                    withDrawView.setOnClickListener(new View.OnClickListener() {
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

                                                Toast.makeText(mContext,"Withdrawed Request",Toast.LENGTH_SHORT).show();

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

