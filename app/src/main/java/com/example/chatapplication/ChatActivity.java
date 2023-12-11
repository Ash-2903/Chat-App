package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.example.chatapplication.models.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatActivity extends AppCompatActivity implements ChatAdapter.EditButtonClickListener, LifecycleObserver {

    ActivityChatBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    Message longClickedMessage;
    String senderRoom;
    String receiverRoom;
    Users receiverUser, senderUser;


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        final String senderId = mAuth.getUid();
        String recieverId = getIntent().getStringExtra("userID");
        Log.d("adapterMessage", "onCreate: " + recieverId);
        String userName = getIntent().getStringExtra("username");
        String pfp = getIntent().getStringExtra("pfp");

        binding.userName.setText(userName);
        Picasso.get().load(pfp).placeholder(R.drawable.profile).into(binding.profilePic);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                finish();
            }
        });

        binding.navBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ProfileViewActivity.class);
                intent.putExtra("uName", userName);
                intent.putExtra("profilePic", pfp);
                intent.putExtra("rId", recieverId);
                startActivity(intent);
            }
        });

        assert recieverId != null;

        // To get Receiver User and to set the "Connection Status"
        database.getReference().child("Users").child(recieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                receiverUser = snapshot.getValue(Users.class);
                DataSnapshot currentSnapshot = snapshot.child("connected");
                if(currentSnapshot.exists()) {
                    Object connectedValue = currentSnapshot.getValue();
                    assert connectedValue != null;
                    if(connectedValue.equals("Online")) {
                        String connectionStatus = snapshot.child("connected").getValue(String.class);
                        binding.connection.setText(connectionStatus);
                    } else {
                        long timeStamp = (Long) connectedValue;
                        Date date = new Date(timeStamp);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a  (dd.MM)");
                        String strTime = "Last seen at " + simpleDateFormat.format(date);
                        binding.connection.setText(strTime);
                  }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        assert senderId != null;
        // To get Sender User
        database.getReference().child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderUser = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final ArrayList<Message> messages = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messages, this, recieverId);
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
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message model = dataSnapshot.getValue(Message.class);
                            assert model != null;
                            model.setMessageId(dataSnapshot.getKey());
                            model.setrMessageId(dataSnapshot.child("rMessageId").getValue(String.class));
                            database.getReference().child("chats").child(receiverRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshotR) {
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

        binding.messageInput.setOnClickListener(v -> setScrollViewToBottom(chatAdapter));

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = binding.messageInput.getText().toString();
                if (!msg.equals("") && !msg.equals("\n")) {
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
                                                    sendNotification(msg);
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
        binding.chatRView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }


    void sendNotification(String message) {

        String rToken = receiverUser.getFcmToken();
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", senderUser.getUsername());
            notificationObject.put("body", message);
            JSONObject dataObj = new JSONObject();
            dataObj.put("userId", mAuth.getUid());
            jsonObject.put("notification", notificationObject)
                    .put("data", dataObj)
                    .put("to", rToken);
            callApi(jsonObject);
            Log.d("myTag", "sendNotification: rToken : " + rToken + " user : " + senderUser.getUsername());

        } catch (Exception e) {

        }

    }

    void callApi(JSONObject jsonObject) {

        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        okhttp3.Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAAfTQTbug:APA91bFQEvxewr96cAMwNmy9X9useLsZZWZ7hJjMBlMMCHXHng3hFZrbeGVTc1l3AFPZvHyhjT7s4do53NeOcU437QzylhteJ9148QIK6m2xSfP0R66JD4-x4n5wqwKO3owSg2cEC8Tk")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

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