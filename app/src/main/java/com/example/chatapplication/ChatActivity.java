package com.example.chatapplication;

import static java.text.DateFormat.getTimeInstance;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.chatapplication.Adapter.ChatAdapter;
import com.example.chatapplication.Adapter.UsersAdapter;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;


public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final String senderId = mAuth.getUid();
        String recieverId = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("username");
        String pfp = getIntent().getStringExtra("pfp");

        binding.userName.setText(userName);
        Picasso.get().load(pfp).placeholder(R.drawable.profile).into(binding.profilePic);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.navBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileViewActivity.class);
                intent.putExtra("uName",userName);
                intent.putExtra("profilePic",pfp);
                intent.putExtra("rId",recieverId);
                startActivity(intent);
            }
        });

        final ArrayList<Message> messages = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messages,this,recieverId);
        binding.chatRView.setAdapter(chatAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRView.setLayoutManager(linearLayoutManager);

        setScrollViewToBottom(chatAdapter);

        String senderRoom = senderId + recieverId;
        String receiverRoom = recieverId + senderId;

        database.getReference().child("chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        int flag = 0;
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message model = dataSnapshot.getValue(Message.class);
                            model.setMessageId(dataSnapshot.getKey());
                            flag++;
                            int finalFlag = flag;
                            database.getReference().child("chats").child(receiverRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshotR) {
                                    int k=0;
                                    for(DataSnapshot ds : snapshotR.getChildren() ) {
                                        model.setrMessageId(ds.getKey());
                                        k++;
                                        if(finalFlag ==k) {
                                            break;
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            messages.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                        //chatAdapter.notify();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ScrollView scrollView = binding.chatScrollView;

        scrollView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            private boolean hasDrawn = false;

            @Override
            public boolean onPreDraw() {
                if (!hasDrawn) {
                    // Scroll to the bottom after the layout is ready
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    hasDrawn = true;
                }
                return true;
            }
        });

        binding.messageInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    setScrollViewToBottom(chatAdapter);
            }
        });




        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.messageInput.getText().toString();
                if(!msg.equals("") && !msg.equals("\n")) {
                    final Message message = new Message(senderId, msg);

                    message.setTimeStamp(new Date().getTime());
                    binding.messageInput.setText("");

                    database.getReference().child("chats").child(senderRoom).push().setValue(message)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("chats").child(receiverRoom).push().setValue(message)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    setScrollViewToBottom(chatAdapter);
                                                }
                                            });
                                }
                            });
                    }
                }

        });

    }

    private void setScrollViewToBottom(ChatAdapter chatAdapter) {
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (chatAdapter.getItemCount() > 0) {
                    binding.chatRView.scrollToPosition(chatAdapter.getItemCount() - 1);
                    // Remove the listener to avoid multiple scrolls
                    binding.chatRView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        };

        //Objects.requireNonNull(binding.chatRView.getLayoutManager()).smoothScrollToPosition(binding.chatRView,new RecyclerView.State(), Objects.requireNonNull(binding.chatRView.getAdapter()).getItemCount());
        binding.chatRView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

    }



}