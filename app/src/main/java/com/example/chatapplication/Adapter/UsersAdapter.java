package com.example.chatapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.AddFriendsActivity;
import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.models.Message;
import com.example.chatapplication.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    ArrayList<Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users users = list.get(position);
        Picasso.get().load(users.getProfilePic()).placeholder(R.drawable.profile).into(holder.profilePic);
        holder.uName.setText(users.getUsername());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userID",users.getUserId());
            intent.putExtra("username",users.getUsername());
            intent.putExtra("pfp",users.getProfilePic());
            context.startActivity(intent);
        });

        setLastMessage(holder,position);

    }

    public void setLastMessage(@NonNull ViewHolder holder, int position) {

        Users users = list.get(position);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(FirebaseAuth.getInstance().getUid() + users.getUserId());
        databaseReference.orderByChild("timeStamp").limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChildren()) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String lMsg = Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString();
                        Message msg = dataSnapshot.getValue(Message.class);
                        String lastPersonToText = "You : ";
                        String finalMsg;
                        assert msg != null;
                        if(!msg.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                            lastPersonToText = "~ ";
                        }
                        if(lMsg.length()>30) {           // add lMsg!=null condition too if necessary
                            finalMsg = lastPersonToText + lMsg.substring(0,30);
                        } else {
                            finalMsg = lastPersonToText + lMsg;
                        }
                        holder.lMessage.setText(finalMsg);
                    }
                } else {
                    String[] name = users.getUsername().split(" ");
                    String defaultMsg = "Say Hi To " + name[0] + " \uD83D\uDC4B";
                    holder.lMessage.setText(defaultMsg);
                }
                //notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }




    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView uName, lMessage;
        ImageView profilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.pfp);
            uName = itemView.findViewById(R.id.username);
            lMessage = itemView.findViewById(R.id.lastMessage);

        }
    }

}
