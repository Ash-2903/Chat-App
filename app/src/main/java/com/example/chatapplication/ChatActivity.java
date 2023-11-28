package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.example.chatapplication.Adapter.ChatAdapter;
import com.example.chatapplication.databinding.ActivityChatBinding;
import com.example.chatapplication.databinding.MenuLayoutBinding;
import com.example.chatapplication.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.Date;


public class ChatActivity extends AppCompatActivity implements ChatAdapter.EditButtonClickListener {

    ActivityChatBinding binding;
    MenuLayoutBinding popUpBinding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    Message longClickedMessage;
    String senderRoom , receiverRoom;

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
        chatAdapter.setEditButtonClickListener(this);
        binding.chatRView.setAdapter(chatAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.chatRView.setLayoutManager(linearLayoutManager);

        setScrollViewToBottom(chatAdapter);

        senderRoom = senderId + recieverId;
        receiverRoom = recieverId + senderId;

        database.getReference().child("chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        int flag = 0;
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message model = dataSnapshot.getValue(Message.class);
                            assert model != null;
                            model.setMessageId(dataSnapshot.getKey());
                            model.setrMessageId(dataSnapshot.child("rMessageId").getValue(String.class));
                            flag++;
                            int finalFlag = flag;
                            database.getReference().child("chats").child(receiverRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshotR) {
                                    // "model.getrMessageId()" is null when the contents of receiver room are changed by the receiver
                                    Log.d("YourTag", "onDataChange: " + model.getrMessageId());
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
                    String rId = database.getReference().child("chats").child(receiverRoom).push().getKey();
                    message.setrMessageId(rId);

                    database.getReference().child("chats").child(senderRoom).push().setValue(message)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    message.setrMessageId(null);
                                    assert rId != null;
                                    database.getReference().child("chats").child(receiverRoom).child(rId).setValue(message)
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

        Log.d("YourTag", "Before setting onClickListener");



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


    @Override
    public void onEditButtonClick() {

        longClickedMessage = ChatAdapter.getMessageObject();
        ImageView editMsgBtn = binding.editMsgBtn;
        binding.sendBtn.setVisibility(View.GONE);
        editMsgBtn.setVisibility(View.VISIBLE);

        if(longClickedMessage!=null) {
            binding.messageInput.setText(longClickedMessage.getMessage());
            binding.editMsgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String editedMsgString = binding.messageInput.getText().toString();
                    binding.editMsgBtn.setVisibility(View.GONE);
                    binding.sendBtn.setVisibility(View.VISIBLE);
                    v.setBackgroundResource(R.color.bg_app);
                    Log.d("YourTag", "onClick: 1) " + longClickedMessage.getMessageId() + " 2) " + longClickedMessage.getrMessageId());
                    database.getReference().child("chats").child(receiverRoom).child(longClickedMessage.getrMessageId()).child("message").setValue(editedMsgString);
                    database.getReference().child("chats").child(senderRoom).child(longClickedMessage.getMessageId()).child("message").setValue(editedMsgString);
                    binding.messageInput.setText("");
                }
            });
        }

    }
}