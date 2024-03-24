package com.example.stayalert;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayalert.custom.classes.NotificationInfo;

import java.util.ArrayList;

public class NotificationsRecycleViewAdapter extends RecyclerView.Adapter<NotificationsRecycleViewAdapter.NotificationsViewHolder> {

    ArrayList<NotificationInfo> info;

    public NotificationsRecycleViewAdapter(ArrayList<NotificationInfo> data){
        this.info = data;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_notif_row,parent,false);
        NotificationsRecycleViewAdapter.NotificationsViewHolder notificationsViewHolder= new NotificationsRecycleViewAdapter.NotificationsViewHolder(view);

        return notificationsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {
        NotificationInfo notificationInfo = this.info.get(position);
        if(notificationInfo.getWasRead()){
            holder.readUnreadIV.setImageResource(R.drawable.ic_read_message);
        }else{
            holder.readUnreadIV.setImageResource(R.drawable.ic_unread_message);
        }
        holder.notifTitle.setText(notificationInfo.getNotifTitle());
        holder.notifDate.setText(notificationInfo.getNotifDate());
    }

    @Override
    public int getItemCount() {
        return this.info.size();
    }

    public static class NotificationsViewHolder extends  RecyclerView.ViewHolder{

        ImageView readUnreadIV;
        TextView notifTitle;
        TextView notifDate;
        TextView notificationName;

        ConstraintLayout notifLayout;


        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);

            readUnreadIV= itemView.findViewById(R.id.notificationReadUnread);
            notifTitle= itemView.findViewById(R.id.notificationTitle);
            notifDate= itemView.findViewById(R.id.notificationDate);
            notifLayout=itemView.findViewById(R.id.notificationLayout);
            notificationName = itemView.findViewById(R.id.notificationName);




        }

    }
}
