package com.project.android.transtalk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder>{

    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private Context mContext;
    private DatabaseReference mRootRef;

    public MessageAdapter(List<Messages> mMessagesList, Context context) {
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        this.mMessagesList = mMessagesList;
        mContext = context;
    }

    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch(viewType) {

            case 0:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_receive_single_item_layout, parent, false);

                return new MessagesViewHolder(v);

            case 1:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_send_single_item_layout, parent, false);

                return new MessagesViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder viewHolder, int position) {
        final Messages messages = mMessagesList.get(position);
        String from_user = messages.getFrom();
        final long sentTime = messages.getTime();

        mRootRef.child("Users").child(from_user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.child("ImageUrl").getValue().toString();
                viewHolder.messageText.setText((messages.getMessage()));
                Timestamp ts = new Timestamp(sentTime);
                Date date = new Date(ts.getTime());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                viewHolder.sentAtTime.setText(String.valueOf(simpleDateFormat.format(date)));
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.contact_image)
                        .into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        String currentUser = mAuth.getCurrentUser().getUid();
        final Messages messages = mMessagesList.get(position);
        String from_user = messages.getFrom();
        if(from_user.equals(currentUser)) {
            return 1;
        }
        else {
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        public EmojiconTextView messageText;
        public CircleImageView profileImage;
        public TextView sentAtTime;

        public MessagesViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.message_user_image);
            messageText = itemView.findViewById(R.id.message_text);
            sentAtTime = itemView.findViewById(R.id.message_time_text);

        }
    }
}
