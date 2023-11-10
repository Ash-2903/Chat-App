package com.example.chatapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter {

    ArrayList<Message> messageModel;
    Context context;
    String recId;
    int SENDER_VIEW_TYPE = 1, RECEIVER_VIEW_TYPE = 2;

    public ChatAdapter(ArrayList<Message> messageModel, Context context) {
        this.messageModel = messageModel;
        this.context = context;
    }

    public ChatAdapter(ArrayList<Message> messageModel, Context context, String recId) {
        this.messageModel = messageModel;
        this.context = context;
        this.recId = recId;
    }

    @Override
    public int getItemViewType(int position) {
        if(messageModel.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_outgoing_chat,parent,false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_incoming_chat,parent,false);
            return new RecieverViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageModel.get(position);
        if(holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).sMsg.setText(message.getMessage());
        } else {
            ((RecieverViewHolder)holder).rMsg.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageModel.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {
        TextView rMsg, rTime;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            rMsg = itemView.findViewById(R.id.receiveText);
            rTime = itemView.findViewById(R.id.receiveTime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView sMsg, sTime;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sMsg = itemView.findViewById(R.id.senderText);
            sTime = itemView.findViewById(R.id.senderTime);
        }
    }

}
