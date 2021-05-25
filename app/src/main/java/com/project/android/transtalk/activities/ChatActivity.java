package com.project.android.transtalk.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.android.transtalk.R;
import com.project.android.transtalk.TranslateMessage;
import com.project.android.transtalk.adapters.MessageAdapter;
import com.project.android.transtalk.models.Messages;
import com.project.android.transtalk.models.TimeAgo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String mCurrentUser;
    private TextView mLastSeenView;
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private EmojiconEditText mChatMessageView;

    //Firebase
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //User Interface
        mChatUser = getIntent().getStringExtra("user_key");
        mChatMessageView = findViewById(R.id.chat_message);
        ImageButton mChatSendBtn = findViewById(R.id.chat_send);
        Toolbar mChatToolbar = findViewById(R.id.chat_main_app_bar_layout);
        View mRootView = findViewById(R.id.chat_relative_layout);
        ImageButton mEmojiView = findViewById(R.id.chat_emoji);
        EmojIconActions mEmojIconActions = new EmojIconActions(this, mRootView, mChatMessageView, mEmojiView);
        mEmojIconActions.ShowEmojIcon();
        mEmojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View action_bar_view = inflater.inflate(R.layout.custom_chat_app_bar, null);

        TextView mTitleView = action_bar_view.findViewById(R.id.custom_bar_name);
        mLastSeenView = action_bar_view.findViewById(R.id.custom_bar_last_seen);
        CircleImageView mProfileImage = action_bar_view.findViewById(R.id.custom_bar_image);

        mAdapter = new MessageAdapter(messagesList,getApplicationContext());
        mMessagesList = findViewById(R.id.messages_list);
        LinearLayoutManager mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(getIntent().getStringExtra("user_name"));
        Picasso.get()
                .load(getIntent().getStringExtra("user_image"))
                .placeholder(R.drawable.contact_image)
                .into(mProfileImage);
        actionBar.setCustomView(action_bar_view);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("Online").getValue().toString();

                if(online.equals("true")) {
                    mLastSeenView.setText(R.string.online);
                }
                else {
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = TimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastSeenView.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mRootRef.child("Chat").child(mCurrentUser).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(!dataSnapshot.hasChild(mChatUser)) {

                            Map chatAddMap = new HashMap();
                            chatAddMap.put("seen",false);
                            chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                            Map chatUserMap = new HashMap();
                            chatUserMap.put("Chat/"+mCurrentUser+"/"+mChatUser,chatAddMap);
                            chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUser,chatAddMap);

                            mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError!=null) {
                                        Log.d("Chat_log",databaseError.getMessage().toString());
                                    }

                                }
                            });
                        }
                        else {



                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
            mUsersDatabase.child("Online").setValue("true");
        }
    }

    private void sendMessage() {
        final TranslateMessage translateMessage = new TranslateMessage();
        final String messageToBeSent = mChatMessageView.getText().toString();
        //final String message = StringEscapeUtils.escapeJava(mChatMessageView.getText().toString());
        final String message = mChatMessageView.getText().toString();
        mChatMessageView.setText("");
        mRootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String mCurrentUserLanguage = dataSnapshot.child(mCurrentUser).child("Language").getValue().toString();
                String mChatUserLanguage = dataSnapshot.child(mChatUser).child("Language").getValue().toString();
                String response = null;
                try {
                    response = translateMessage.translate(getApplicationContext(),message,mCurrentUser,mChatUser, mCurrentUserLanguage,mChatUserLanguage);
//                    response = response.replace("\\ ","\\");
//                    response = response.replace(" \\","\\");
//                    response = StringEscapeUtils.unescapeJava(response);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(!TextUtils.isEmpty(message)) {

                    String current_user_ref = "Messages/"+mCurrentUser+"/"+mChatUser;
                    String chat_user_ref = "Messages/"+mChatUser+"/"+mCurrentUser;

                    DatabaseReference userMessagePush = mRootRef.child("Messages").child(mChatUser).push();

                    String pushId = userMessagePush.getKey();

                    Map messageCurrentMap = new HashMap();
                    messageCurrentMap.put("message",messageToBeSent);
                    messageCurrentMap.put("seen",false);
                    messageCurrentMap.put("type","text");
                    messageCurrentMap.put("time",ServerValue.TIMESTAMP);
                    messageCurrentMap.put("from",mCurrentUser);

                    Map messageChatMap = new HashMap();
                    messageChatMap.put("message", response);
                    messageChatMap.put("seen",false);
                    messageChatMap.put("type","text");
                    messageChatMap.put("time",ServerValue.TIMESTAMP);
                    messageChatMap.put("from",mCurrentUser);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref+"/"+pushId,messageCurrentMap);
                    messageUserMap.put(chat_user_ref+"/"+pushId,messageChatMap);

                    mChatMessageView.setText("");

                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError!=null) {
                                Log.d("Chat_log",databaseError.getMessage().toString());
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUser).child(mChatUser);
        messageRef.keepSynced(true);
        messageRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        mAdapter.notifyDataSetChanged();
                        mMessagesList.scrollToPosition(messagesList.size()-1);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }
}
