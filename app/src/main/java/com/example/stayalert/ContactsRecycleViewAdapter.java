package com.example.stayalert;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import helper.classes.ViewDetectedImageHelper;

public class ContactsRecycleViewAdapter extends  RecyclerView.Adapter<ContactsRecycleViewAdapter.ContactsInfoViewHolder>{


    ArrayList<ContactsInfo> contactsInfo;
    Context context;

    public ContactsRecycleViewAdapter(ArrayList<ContactsInfo> data, Context context){
        this.contactsInfo=data;
        this.context=context;
    }

    @NonNull
    @Override
    public ContactsInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_contacts_row,parent,false);
        ContactsRecycleViewAdapter.ContactsInfoViewHolder contactsInfoViewHolder = new ContactsRecycleViewAdapter.ContactsInfoViewHolder(view);

        return contactsInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsInfoViewHolder holder, int position) {
        ContactsInfo contactsInfo = this.contactsInfo.get(position);
        holder.contactName.setText(contactsInfo.getContactName());
        holder.contactNumber.setText(contactsInfo.getContactNumber());
        holder.position = holder.getAdapterPosition();
        holder.info=contactsInfo;

    }

    @Override
    public int getItemCount() {
        return  this.contactsInfo.size();
    }

    public static class ContactsInfoViewHolder extends RecyclerView.ViewHolder{

        ImageView contactPhoto;
        TextView contactName;
        TextView contactNumber;
        ImageButton contactCallBtn;
        View rootView;
        int position;
        ContactsInfo info;

        public ContactsInfoViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView=itemView;
            contactPhoto = itemView.findViewById(R.id.contactPhoto);
            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            contactCallBtn= itemView.findViewById(R.id.contactCall);

            contactCallBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });



        }
    }



}

