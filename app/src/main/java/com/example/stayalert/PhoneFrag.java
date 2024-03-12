package com.example.stayalert;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PhoneFrag extends Fragment {

    RecyclerView contactsRV;
    ContactsRecycleViewAdapter contactsRecycleViewAdapter;
    LinearLayoutManager linearLayoutManager;
    TextView contactsTV, favoritesTV;
    ImageButton addFavorites;
    EditText searchET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_phone, container, false);

        if (ActivityCompat.checkSelfPermission(CameraActivity.context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.READ_CONTACTS);
        }

        contactsRV = view.findViewById(R.id.contactsRV);
        contactsTV = view.findViewById(R.id.phoneContacts);
        favoritesTV= view.findViewById(R.id.contactFavorites);
        addFavorites = view.findViewById(R.id.addContactFavorites);
        searchET = view.findViewById(R.id.phoneSearchTV);



        contactsRV.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        contactsRV.setLayoutManager(linearLayoutManager);
        CameraActivity cameraActivity=(CameraActivity)getActivity();

        displayContactList(CameraActivity.contactInfoList);

        contactsTV.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                favoritesTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.black_primary_selector)
                        .getDefaultColor());
                contactsTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.primary_black_selector)
                        .getDefaultColor());

                contactsTV.setBackgroundResource(R.drawable.bg_line_below_text);
                favoritesTV.setBackground(null);

                displayContactList(CameraActivity.contactInfoList);


            }
        });

        favoritesTV.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                contactsTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.black_primary_selector)
                        .getDefaultColor());
                favoritesTV.setTextColor(ContextCompat.getColorStateList(getContext(), R.drawable.primary_black_selector)
                        .getDefaultColor());

                favoritesTV.setBackgroundResource(R.drawable.bg_line_below_text);
                contactsTV.setBackground(null);
                displayContactList(CameraActivity.favoritesInfoList);


            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called whenever the text is changed.
                // The new text is s.toString().
            }

            @Override
            public void afterTextChanged(Editable s) {
                ArrayList<ContactsInfo> searchedInfoList = new ArrayList<>(searchContacts(CameraActivity.contactInfoList,searchET.getText().toString().trim()));
                displayContactList(searchedInfoList);
            }
        });

        return view;
    }



//    public void displayDetectionLogs(){
//        info= cameraActivity.detectionLogsInfo;
//        if(info!=null && !info.isEmpty()){
//            progressBar.setVisibility(View.GONE);
//            recyleViewAdapter = new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
//            detectionLogsRV.setAdapter(recyleViewAdapter);
//            recyleViewAdapter.notifyDataSetChanged();
//        }else{
//            queryDetection();
//        }
//    }



    public void displayContactList(ArrayList<ContactsInfo> info){
        if(CameraActivity.contactInfoList!=null){
            contactsRV.setLayoutManager( new LinearLayoutManager(getContext()));
            contactsRecycleViewAdapter= new ContactsRecycleViewAdapter(info,getContext());
            contactsRV.setAdapter(contactsRecycleViewAdapter);
        }


    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(CameraActivity.context, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    public ArrayList<ContactsInfo> searchContacts(ArrayList<ContactsInfo> info,String searchTerm) {
        ArrayList<ContactsInfo> searchedList = new ArrayList<>();
        for (ContactsInfo contact : info) {
            if (contact.getContactName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    contact.getContactNumber().contains(searchTerm)) {
                searchedList.add(contact);
            }
        }
        return searchedList;
    }
}