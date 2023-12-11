package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.chatapplication.Adapter.FragmentsAdapter;
import com.example.chatapplication.databinding.ActivityMainBinding;
import com.example.chatapplication.models.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {

    ActivityMainBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt("SelectedTheme", AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        // LifeCycle of the App
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if(getIntent().getExtras()!=null) {
            String uId = getIntent().getExtras().getString("userId");
            Log.d("myTag", "onCreate: " + uId);
            assert uId != null;
            database.getReference().child("Users").child(uId).get().addOnCompleteListener(task -> {
                Users users = task.getResult().getValue(Users.class);
                if (users != null) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("userID", uId);
                    intent.putExtra("username", users.getUsername());
                    intent.putExtra("pfp", users.getProfilePic());
                    // Put the entire Users object into the intent using Serializable or Parcelable
                    startActivity(intent);
                    finish();
                }
            });
        }

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        getFCMToken();

        // menu drop down and items
        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,v);
                popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.setting) {
                            Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            finish();
                        } else if(item.getItemId() == R.id.gpChats) {
                            Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        } else if(item.getItemId() == R.id.logout) {
                            mAuth.signOut();
                            Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        binding.addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddFriendsActivity.class);
                startActivity(intent);
                finish();
            }
        });


        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Log.d("connectFlag", "onDataChange: inside onDataChange");
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                Log.d("connectFlag", "onDataChange: inside onDataChange " + connected );
                if (connected) {
                    database.getReference().child("Users").child(Objects.requireNonNull(mAuth.getUid())).child("connected").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    database.getReference().child("Users").child(mAuth.getUid()).child("connected").setValue("Online");
                    //Log.d("connectFlag", "onDataChange: the user is online ");
                } else {
                    database.getReference().child("Users").child(Objects.requireNonNull(mAuth.getUid())).child("connected").setValue(ServerValue.TIMESTAMP);
                    //Log.d("connectFlag", "onDataChange: the user is offline");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });





    }

    // TO SET THE CONNECTION TO ONLINE / OFFLINE

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        // App in background
        Log.d("connectFlag", "onDataChange: the user is offline");
        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("connected").setValue(ServerValue.TIMESTAMP);
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App in foreground
        Log.d("connectFlag", "onDataChange: the user is online ");
        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("connected").setValue("Online");
    }



    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()) {
                    String token = task.getResult();
                    Log.d("myToken", "onComplete: "+ token);
                    database.getReference().child("Users").child(Objects.requireNonNull(mAuth.getUid())).child("fcmToken").setValue(token);
                }
            }
        });
    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }



}