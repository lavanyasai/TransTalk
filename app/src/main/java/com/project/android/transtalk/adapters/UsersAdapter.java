package com.project.android.transtalk.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.activities.ProfileActivity;
import com.project.android.transtalk.models.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private ArrayList<Users> mUsers;
    private Context mContext;

    public UsersAdapter(Context context, ArrayList<Users> users) {
        mContext = context;
        mUsers = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_item_layout, parent, false);

        return new UsersViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersViewHolder holder, int position) {
        TextView nameTextView = holder.getUserNameView();
        nameTextView.setText(mUsers.get(position).getName());
        TextView statusTextView = holder.getUserStatusView();
        statusTextView.setText(mUsers.get(position).getStatus());
        CircleImageView userImage = holder.getUserImageView();
        Picasso.get()
                .load(mUsers.get(position).getImageUrl())
                .placeholder(R.drawable.contact_image)
                .into(userImage);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(mContext, ProfileActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                profileIntent.putExtra("key", mUsers.get(position).getKey());
                mContext.startActivity(profileIntent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Context mContext;

        protected UsersViewHolder(View itemView, Context context) {
            super(itemView);

            mView = itemView;
            mContext = context;
        }

        private TextView getUserNameView() {
            return mView.findViewById(R.id.user_name);
        }

        private TextView getUserStatusView() {
            return mView.findViewById(R.id.user_message);
        }

        private CircleImageView getUserImageView() {
            return mView.findViewById(R.id.user_image);
        }
    }
}
