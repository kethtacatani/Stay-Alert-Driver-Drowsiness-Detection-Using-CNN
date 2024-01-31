package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import firebase.classes.FirebaseDatabase;

public class StatsFrag extends Fragment {
    RecyclerView detectionLogsRV;
    LinearLayoutManager linearLayoutManager;
    ArrayList<DetectionLogsInfo> info = new ArrayList<>();
    DetectionLogsRecycleViewAdapter adapter;
    FirebaseDatabase firebaseDB;
    FirebaseUser user;
    FirebaseFirestore db;
    Query query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_stats, container, false);
        firebaseDB=new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        detectionLogsRV=view.findViewById(R.id.detectionLogsRV);
        detectionLogsRV.setHasFixedSize(false);

        query= db.collection("users/"+user.getUid()+"/image_detection")
                // Order documents by the "timestamp" field in descending order
                .orderBy("timestamp", Query.Direction.DESCENDING)
                // Limit the result set to 15 documents
                .limit(15);

        info = firebaseDB.getDetectionLogsInfo(query, new FirebaseDatabase.ArrayListTaskCallback<Void>() {

            @Override
            public void onSuccess(ArrayList<DetectionLogsInfo> arrayList) {
                info =arrayList;
                linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
                detectionLogsRV.setLayoutManager(linearLayoutManager);
                adapter= new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
                detectionLogsRV.setAdapter(adapter);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });


        return view;
    }
}