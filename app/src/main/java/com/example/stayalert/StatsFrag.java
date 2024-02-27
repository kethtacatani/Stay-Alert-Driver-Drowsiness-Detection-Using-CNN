package com.example.stayalert;

import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
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
    Spinner detectionResultsSpinner, detectionChartSpinner, detectionLogsSpinner;
    int count=0;
    TextView drowsyCountTV, yawnCountTV, averageResponseTV;
    private LineChart lineChart;

    private List<String> xValues;





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

        instantiateAdapter(detectionResultsSpinner);
        instantiateAdapter(detectionChartSpinner);
        instantiateAdapter(detectionLogsSpinner);

        ImageButton detectionResultsImageButtonDD, detectionChartImageButtonDD, detectionLogsImageButtonDD;
        detectionResultsImageButtonDD = view.findViewById(R.id.detectionResultsButton);
        detectionChartImageButtonDD = view.findViewById(R.id.detectionChartButton);
        detectionLogsImageButtonDD= view.findViewById(R.id.detectionLogsButton);


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

        updateDetectionRecords();
        displayDetectionLogs();

        lineChart = view.findViewById(R.id.detectionChart);

        Description description = new Description();
        description.setText("Students Record");
        description.setPosition(150f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);

        xValues = Arrays.asList("8:00","9:00","10:00","11:00");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(4);
        xAxis.setGranularity(1f);

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(10);


        List<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry(0, 60f));
        entries1.add(new Entry(1, 70f));
        entries1.add(new Entry(2, 85f));
        entries1.add(new Entry(3, 95f));


        List<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(0, 50f));
        entries2.add(new Entry(1, 85f));
        entries2.add(new Entry(2, 65f));
        entries2.add(new Entry(3, 80f));

        LineDataSet dataSet1 = new LineDataSet(entries1, "Maths");
        dataSet1.setColor(Color.BLUE);

        LineDataSet dataSet2 = new LineDataSet(entries2, "Science");
        dataSet2.setColor(Color.RED);

        LineData lineData = new LineData(dataSet1, dataSet2);

        lineChart.setData(lineData);

        lineChart.invalidate();



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

        switch (range){
            case 0:
                day=0;
                break;
            case 1:
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
                day=-3;
                break;
            case 3:
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
                cameraActivity.getDetectionRecordsCount(startDate, endDate, new CameraActivity.TaskCallback() {
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

    public void updateDetectionRecords(){
        averageResponseTV.setText(String.format("%.2f", cameraActivity.averageResponse)+"s");
        drowsyCountTV.setText(cameraActivity.drowsyCount+"");
        yawnCountTV.setText(cameraActivity.yawnCountRes+"");
    }
}