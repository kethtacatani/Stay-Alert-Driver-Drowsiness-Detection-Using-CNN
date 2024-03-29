package com.example.stayalert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayalert.custom.classes.ChartGenerator;
import com.example.stayalert.custom.classes.DetectionLogsInfo;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import firebase.classes.FirebaseDatabase;

public class StatsFrag extends Fragment implements AdapterView.OnItemSelectedListener {
    RecyclerView detectionLogsRV;
    LinearLayoutManager linearLayoutManager;
    ArrayList<DetectionLogsInfo> info = new ArrayList<>();
    DetectionLogsRecycleViewAdapter recyleViewAdapter;
    FirebaseDatabase firebaseDB;
    FirebaseUser user;
    FirebaseFirestore db;
    CameraActivity cameraActivity;
    ProgressBar progressBar;
    public static Spinner detectionResultsSpinner, detectionChartSpinner, detectionLogsSpinner;
     ImageButton detectionResultsImageButtonDD, detectionChartImageButtonDD, detectionLogsImageButtonDD;
    int count=0;
    public static TextView drowsyCountTV, yawnCountTV, averageResponseTV;
    public static TextView timeRangeTV;
    public static  LineChart lineChart;

    private List<String> xValues= new ArrayList<>();
    ArrayList<Integer> drowsyList = new ArrayList<>();
    ArrayList<Integer> yawnList = new ArrayList<>();
    public static ChartGenerator chartGenerator;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_stats, container, false);
        firebaseDB=new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        cameraActivity=(CameraActivity)getActivity();

        detectionLogsRV=view.findViewById(R.id.detectionLogsRV);
        progressBar=view.findViewById(R.id.detectionLogsPB);
        detectionResultsSpinner=view.findViewById(R.id.detectionResultSpinner);
        detectionChartSpinner = view.findViewById(R.id.detectionChartSpinner);
        detectionLogsSpinner=view.findViewById(R.id.detectionLogsSpinner);
        drowsyCountTV= view.findViewById(R.id.drowsyCountTV);
        yawnCountTV = view.findViewById(R.id.yawnCount);
        averageResponseTV = view.findViewById(R.id.awakenessLevel);
        timeRangeTV = view.findViewById(R.id.timeRangeStats);

        instantiateAdapter(detectionResultsSpinner);
        instantiateAdapter(detectionChartSpinner);
        instantiateAdapter(detectionLogsSpinner);


        detectionResultsImageButtonDD = view.findViewById(R.id.detectionResultsButton);
        detectionChartImageButtonDD = view.findViewById(R.id.detectionChartButton);
        detectionLogsImageButtonDD= view.findViewById(R.id.detectionLogsButton);
        lineChart = view.findViewById(R.id.detectionChart);



        detectionResultsSpinner.setOnItemSelectedListener(this);
        detectionChartSpinner.setOnItemSelectedListener(this);
        detectionLogsSpinner.setOnItemSelectedListener(this);

        view.findViewById(R.id.textVisesw5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(cameraActivity, "clicked", Toast.LENGTH_SHORT).show();
                displayDetectionLogs();
            }
        });

        detectionResultsImageButtonDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectionResultsSpinner.performClick();
            }
        });

        detectionChartImageButtonDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectionChartSpinner.performClick();
            }
        });

        detectionLogsImageButtonDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectionLogsSpinner.performClick();
            }
        });


        detectionLogsRV.setHasFixedSize(false);
        linearLayoutManager = new LinearLayoutManager((CameraActivity)getActivity());
        detectionLogsRV.setLayoutManager(linearLayoutManager);
        chartGenerator = new ChartGenerator();

        updateDetectionRecords();
        displayDetectionLogs();
        viewDetectionChart("today");





        return view;
    }

    public void instantiateAdapter(Spinner spinner){
        ArrayAdapter<CharSequence> detectionResultAdapter = ArrayAdapter.createFromResource(cameraActivity,R.array.time_periods, android.R.layout.simple_spinner_item);
        detectionResultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(detectionResultAdapter);
        spinner.setGravity(Gravity.RIGHT);
    }

    public void displayDetectionLogs(){
        info= cameraActivity.detectionLogsInfo;
        if(info!=null && !info.isEmpty()){
            progressBar.setVisibility(View.GONE);
            recyleViewAdapter = new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
            detectionLogsRV.setAdapter(recyleViewAdapter);
            recyleViewAdapter.notifyDataSetChanged();
        }else{
            queryDetection();
        }
    }

    public void queryDetection(){
        info = firebaseDB.getDetectionLogsInfo(cameraActivity.query, new FirebaseDatabase.ArrayListTaskCallback<Void>() {
            @Override
            public void onSuccess(ArrayList<DetectionLogsInfo> arrayList) {
                progressBar.setVisibility(View.GONE);
                info =arrayList;
                recyleViewAdapter = new DetectionLogsRecycleViewAdapter(info, (CameraActivity)getActivity());
                detectionLogsRV.setAdapter(recyleViewAdapter);
            }

            @Override
            public void onFailure(String errorMessage) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(count>2){
            System.out.println("clic");
            int spinnerId = parent.getId();

            if (spinnerId == R.id.detectionResultSpinner) {
                performItemSelected("results",position);
            } else if (spinnerId == R.id.detectionChartSpinner) {
                performItemSelected("chart",position);
            } else if (spinnerId == R.id.detectionLogsSpinner) {
                performItemSelected("logs",position);
            }
        }
        count++;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void performItemSelected(String stat,int range){
        Date startDate = new Date();
        Date endDate= new Date();

        int day=0;
        String rangeString="day";

        switch (range){
            case 0:
                day=0;
                rangeString="today";
                break;
            case 1:
                rangeString="yesterday";
                day=-1;
                Calendar calendarEnd = Calendar.getInstance();
                calendarEnd.add(Calendar.DAY_OF_MONTH, 0);

                // Set the time to 12:00 AM
                calendarEnd.set(Calendar.HOUR_OF_DAY, 0);
                calendarEnd.set(Calendar.MINUTE, 0);
                calendarEnd.set(Calendar.SECOND, 0);
                calendarEnd.set(Calendar.MILLISECOND, 0);
                endDate= calendarEnd.getTime();
                break;
            case 2:
                rangeString="day3";
                day=-3;
                break;
            case 3:
                rangeString="day7";
                day=-7;
                break;
            case 4:
                day=-30;
                break;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, day);

        // Set the time to 12:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        startDate = calendar.getTime();

        switch (stat){
            case "results":
                cameraActivity.averageResponse=0;
                cameraActivity.getDetectionRecordsCount(rangeString, new CameraActivity.TaskCallback() {
                    @Override
                    public void onSuccess(Object result) {
                        updateDetectionRecords();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.d("getREsults", "Get count failed");
                    }
                });
                break;
            case "chart":
                viewDetectionChart(rangeString);

                break;
            case "logs":
                cameraActivity.query = db.collection("users/"+user.getUid()+"/image_detection")
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThanOrEqualTo("timestamp", endDate)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(10);
                queryDetection();
                break;
        }

    }

    public static void updateDetectionRecords(){
        averageResponseTV.setText(String.format("%.2f", CameraActivity.averageResponse)+"s");
        drowsyCountTV.setText(CameraActivity.drowsyCount+"");
        yawnCountTV.setText(CameraActivity.yawnCountRes+"");
    }

    public static void viewDetectionChart(String range){

        if(detectionChartSpinner==null){
            return;
        }
        if(range==null){
            String text = detectionChartSpinner.getSelectedItem().toString();
            if(text.contains("Today")){
                range="today";
            }else if(text.contains("Yesterday")){
                range="yesterday";
            }else if(text.contains("3")){
                range="day3";
            }else if(text.contains("7")){
                range="day7";
            }if(text.contains("30")){
                range="day";
            }
        }
        chartGenerator.setRange(range);
        chartGenerator.setLineChart(lineChart);
        chartGenerator.processYawnCount(CameraActivity.yawnCountDocument,range);
        chartGenerator.processDrowsyCount(CameraActivity.drowsyCountDocument,range);
        timeRangeTV.setText(chartGenerator.getTimeRange()+"");

    }
}