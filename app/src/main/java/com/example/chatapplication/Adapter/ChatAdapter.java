package com.example.chatapplication.Adapter;

import static android.app.PendingIntent.getActivity;
import static android.app.PendingIntent.getService;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.metrics.Event;
import android.os.Vibrator;
import android.util.EventLog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

        // set message and time in the chat view
        Message message = messageModel.get(position);
        if(holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).sMsg.setText(message.getMessage());
            Date date = new Date(message.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
            String strTime = simpleDateFormat.format(date);
            ((SenderViewHolder) holder).sTime.setText(strTime);
        } else {
            ((RecieverViewHolder)holder).rMsg.setText(message.getMessage());
        }

        // Handle Long Press
        Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // vibrate for 100 milliseconds
                vibrate.vibrate(100);

                // Inflate Pop Up view
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popUpView = inflater.inflate(R.layout.menu_layout,null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true;
                final PopupWindow popUp = new PopupWindow(popUpView,width,height,focusable);
                popUp.setOutsideTouchable(true);

                int popupWidth = popUpView.getMeasuredWidth();
                int popupHeight = popUpView.getMeasuredHeight();

                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int x = location[0] + (v.getWidth() - popupWidth) / 2; // Adjust as needed
                int y = location[1] - popupHeight; // Adjust as needed

                popUp.showAtLocation(v, Gravity.NO_GRAVITY, x, y);
                Toast.makeText(context, x+" "+y, Toast.LENGTH_SHORT).show();

                //v.setBackgroundColor(Color.argb(128, 255, 0, 0));
                v.setBackgroundResource(R.drawable.translucent_overlay);

                popUpView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popUp.dismiss();
                        v.setBackground(null);
                        // Toast.makeText(context, "pop up dismissed", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                //Toast.makeText(context, "coordinates " + x + " and " + y, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
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
