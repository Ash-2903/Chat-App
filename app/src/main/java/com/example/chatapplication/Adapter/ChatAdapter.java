package com.example.chatapplication.Adapter;

import static android.app.PendingIntent.getActivity;
import static android.app.PendingIntent.getService;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.metrics.Event;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.util.EventLog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
            Date date = new Date(message.getTimeStamp());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
            String strTime = simpleDateFormat.format(date);
            ((RecieverViewHolder)holder).rTime.setText(strTime);
        }

        // Handle Long Press
        Vibrator vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Inflate Pop Up view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popUpView = inflater.inflate(R.layout.menu_layout, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popUp = new PopupWindow(popUpView, width, height, focusable);
        popUp.setTouchable(true);
        popUp.setOutsideTouchable(true);

        if(holder.getClass()==SenderViewHolder.class) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    // vibrate for 100 milliseconds
                    vibrate.vibrate(100);

                    // To calculate location of the view and where to place pop up window
                    int popupWidth = popUpView.getMeasuredWidth();
                    int popupHeight = popUpView.getMeasuredHeight();
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    int xOffset = 50;  // to adjust pop up window in the x-axis
                    int x = location[0] + ((v.getWidth() - popupWidth) / 2) - xOffset;
                    int y = location[1] - popupHeight;

                    v.setBackgroundResource(R.color.longPress);
                    popUp.showAtLocation(v, Gravity.NO_GRAVITY, x, y);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        popUp.setAttachedInDecor(true);
                    }

                    popUp.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            v.setBackgroundResource(R.color.bg_app);
                            //Toast.makeText(context, "outside touch detected", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return false;
                }
            });

            ImageView delete = popUpView.findViewById(R.id.deleteChat);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "delete triggered", Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are you sure you want to delete this message")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String senderRoom = FirebaseAuth.getInstance().getUid() + recId;
                                    String receiverRoom = recId + FirebaseAuth.getInstance().getUid();
                                    database.getReference().child("chats").child(senderRoom).child(message.getMessageId()).setValue(null);
                                    database.getReference().child("chats").child(receiverRoom).child(message.getMessageId()).setValue(null);
                                    popUp.dismiss();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    popUp.dismiss();
                                }
                            }).show();
                }
            });

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
