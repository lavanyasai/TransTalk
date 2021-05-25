package com.project.android.transtalk.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.activities.ChatActivity;
import com.project.android.transtalk.models.Chats;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    private ArrayList<Chats> mChats = new ArrayList<>();
    private ArrayList<String> mChatsKeys = new ArrayList<>();
    private DatabaseReference mUsersDatabase;
    Context mContext;

    public ChatsAdapter(Context context, ArrayList<Chats> chats, ArrayList<String> chatsKeys) {
        mContext = context;
        mChats = chats;
        mChatsKeys = chatsKeys;
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_item, parent, false);

        return new ChatsViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, final int position) {

        holder.bind(mChats.get(holder.getAdapterPosition()), mChatsKeys.get(holder.getAdapterPosition()));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId = mChatsKeys.get(holder.getAdapterPosition());
                mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("Name").getValue().toString();
                        final String userThumb = dataSnapshot.child("ThumbUrl").getValue().toString();
                        String userOnline = dataSnapshot.child("Online").getValue().toString();

                        Intent chatIntent = new Intent(mContext, ChatActivity.class);
                        chatIntent.putExtra("user_key", userId);
                        chatIntent.putExtra("user_name", userName);
                        chatIntent.putExtra("user_image", userThumb);
                        mContext.startActivity(chatIntent);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Context mContext;
        private DatabaseReference mUsersDatabase;

        protected ChatsViewHolder(View itemView, Context context) {
            super(itemView);

            mView = itemView;
            mContext = context;
            mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        }

        public void bind(Chats chats, String userKey) {

            mUsersDatabase.child(userKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    String userName = dataSnapshot.child("Name").getValue().toString();
                    String userStatus = dataSnapshot.child("Status").getValue().toString();
                    String userImage = dataSnapshot.child("ImageUrl").getValue().toString();
                    String userOnline = dataSnapshot.child("Online").getValue().toString();

                    TextView nameTextView = mView.findViewById(R.id.friend_user_name);
                    nameTextView.setText(userName);
                    TextView statusTextView = mView.findViewById(R.id.friend_user_message);
                    statusTextView.setText(userStatus);
                    CircleImageView userImageView = mView.findViewById(R.id.friend_user_image);
                    Picasso.get()
                            .load(userImage)
                            .placeholder(R.drawable.contact_image)
                            .into(userImageView);
                    ImageView userOnlineView = mView.findViewById(R.id.friend_user_online);
                    if (userOnline.equals("true")) {
                        userOnlineView.setVisibility(View.VISIBLE);
                    } else {
                        userOnlineView.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}


