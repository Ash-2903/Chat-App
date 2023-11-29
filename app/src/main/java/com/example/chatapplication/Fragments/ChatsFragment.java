package com.example.chatapplication.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapplication.Adapter.UsersAdapter;
import com.example.chatapplication.R;
import com.example.chatapplication.databinding.FragmentChatsBinding;
import com.example.chatapplication.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    FragmentChatsBinding binding;
    //ArrayList<Users> list = new ArrayList<>();
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    public ChatsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentChatsBinding.inflate(inflater,container,false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        getFriends(new FriendsCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFriendsReceived(ArrayList<Users> friends) {
                Log.d("Friends", "Received friends: " + friends.size());
                UsersAdapter adapter = new UsersAdapter(friends,getContext());
//                for(int i=0;i< friends.size();i++)
//                    Log.d("bruno", "onFriendsReceived: " + friends.get(i).getUserId());
                binding.chatRecyclerView.setAdapter(adapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                binding.chatRecyclerView.setLayoutManager(layoutManager);
            }
        });

        return binding.getRoot();
    }



    public interface FriendsCallback {
        void onFriendsReceived(ArrayList<Users> friends);
    }

    public void getFriends( FriendsCallback callback) {
        ArrayList<Users> friends = new ArrayList<>();

        String currentUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        database.getReference().child("chats").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friends.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String room = dataSnapshot.getKey();
                    if(room!=null) {
                        String firstPerson = room.substring(0,(room.length()/2));

                        if(firstPerson.equals(currentUid)) {
                            String friend = room.substring(room.length()/2);
                            //Log.d("friends", "friend : " + friend);

                            database.getReference().child("Users").child(friend).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users friendModel = new Users();
                                    friendModel.setUsername(snapshot.child("username").getValue(String.class));
                                    //friendModel.setUserId(snapshot.getKey());
                                    friendModel.setUserId(snapshot.child("userId").getValue(String.class));
                                    //Log.d("bruno", "onDataChange: " + snapshot.child("userId").getValue(String.class));
                                    if(snapshot.child("profilePic").getValue(String.class)!=null) {
                                        friendModel.setProfilePic(snapshot.child("profilePic").getValue(String.class));
                                    }
                                    //Log.d("friends", "onDataChange: " + friendModel );
                                    friends.add(friendModel);
                                    callback.onFriendsReceived(friends);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    //Log.d("friends", "onCancelled: ");
                                }
                            });
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }




}