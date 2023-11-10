package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapplication.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

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
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}