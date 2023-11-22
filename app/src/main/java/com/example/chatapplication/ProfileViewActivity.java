package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.chatapplication.databinding.ActivityProfileViewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileViewActivity extends AppCompatActivity {

    ActivityProfileViewBinding binding;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        String receiverId = getIntent().getStringExtra("rId");
        String userName = getIntent().getStringExtra("uName");
        String pfpString = getIntent().getStringExtra("profilePic");


        String[] name;
        if(userName!=null) {
            name = userName.split(" ");
            String navBarHeading = name[0] + "'s  Profile";
            binding.mainNavbar.setText(navBarHeading);
        }

        binding.uName.setText(userName);
        if(pfpString!=null) {
            Uri pfp = Uri.parse(pfpString);
            Picasso.get().load(pfp).placeholder(R.drawable.profile).into(binding.userPfp);
        }


        database.getReference().child("Users").child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String email = snapshot.child("mail").getValue(String.class);
                if(email!=null)
                    binding.uEmail.setText(email);
                String bio = snapshot.child("bio").getValue(String.class);
                if(bio!=null)
                    binding.bioDisplay.setText(bio);
                String dob = snapshot.child("dob").getValue(String.class);
                if(dob!=null) {
                    binding.dobCard.setVisibility(View.VISIBLE);
                    binding.dobDisplay.setText(dob);
                    binding.dobDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                }
                String gender = snapshot.child("gender").getValue(String.class);
                if(gender!=null) {
                    binding.genderCard.setVisibility(View.VISIBLE);
                    binding.genderDisplay.setText(gender);
                    binding.genderDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                }
                String location = snapshot.child("location").getValue(String.class);
                if(location!=null) {
                    binding.locationCard.setVisibility(View.VISIBLE);
                    binding.locationDisplay.setText(location);
                    binding.locationDisplay.setTextColor(getResources().getColor(R.color.darker_blue));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }
}