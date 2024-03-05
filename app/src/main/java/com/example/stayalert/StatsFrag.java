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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

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
    TextView timeRangeTV;
    private LineChart lineChart;
    int lowestTime=24;
    int highestTime=1;
    int yawnStartTime=0;
    int drowsyStartTime=0;
    int highestYLength=0;
    int checkCount=0;
    int daysRange=80;

    private List<String> xValues= new ArrayList<>();
    ArrayList<Integer> drowsyList = new ArrayList<>();
    ArrayList<Integer> yawnList = new ArrayList<>();





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

        ImageButton detectionResultsImageButtonDD, detectionChartImageButtonDD, detectionLogsImageButtonDD;
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

    public void updateDetectionRecords(){
        averageResponseTV.setText(String.format("%.2f", cameraActivity.averageResponse)+"s");
        drowsyCountTV.setText(cameraActivity.drowsyCount+"");
        yawnCountTV.setText(cameraActivity.yawnCountRes+"");
    }

    public void viewDetectionChart(String range){

        daysRange=80;
        checkCount=0;

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());
        String formattedDate = sdf.format(currentDate);
        xValues.clear();
        if(range.equals("day3")){
            daysRange=3;
            Date tomorrowDate;
            for (int i = 0; i < 2; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                tomorrowDate = calendar.getTime();
                xValues.add(sdf.format(tomorrowDate));
            }
            Collections.reverse(xValues);
            xValues.add(0,"Date");
            xValues.add(sdf.format(currentDate));
        }else if(range.equals("day7")){
            daysRange=7;
            Date tomorrowDate;
            for (int i = 0; i < 6; i++) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                tomorrowDate = calendar.getTime();
                xValues.add(sdf.format(tomorrowDate));
            }
            Collections.reverse(xValues);
            xValues.add(0,"Date");
            xValues.add(sdf.format(currentDate));
        }


        lowestTime=24;
        highestTime=1;
        yawnStartTime=0;
        drowsyStartTime=0;




        firebaseDB.readData("drowsy_count", user.getUid(), "default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    CameraActivity.drowsyCountDocument=documentSnapshot;
                    processDrowsyCount(documentSnapshot,range);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(Exception e) {
                Log.d("viewDetectionCount","Failed reading drowsy data");
            }
        });


        firebaseDB.readData("yawn_count", user.getUid(), "default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    CameraActivity.yawnCountDocument=documentSnapshot;
                    processYawnCount(documentSnapshot,range);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure(Exception e) {
                Log.d("viewDetectionCount","Failed reading yawn data");
            }
        });



    }

    public void processDrowsyCount(DocumentSnapshot documentSnapshot, String range){
        drowsyList.clear();
        for (int i = 1; i < 80; i++) {
            String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
            if(documentSnapshot.contains(field) && i<= daysRange){
                int value= Integer.parseInt(documentSnapshot.getData().get(field).toString()) ;
                drowsyStartTime= drowsyStartTime==0?i:drowsyStartTime;
                highestYLength=value>highestYLength?value:highestYLength;
                drowsyList.add(value);
                if(value>0){
                    lowestTime = (i < lowestTime) ? i : lowestTime;
                    highestTime = (i > highestTime) ? i : highestTime;
                }
            }else{
                break;
            }
        }


        checkCount++;
        displayChart(drowsyList,yawnList, range);
    }

    public void processYawnCount(DocumentSnapshot documentSnapshot, String range) {
        yawnList.clear();
        for (int i = 1; i < 80; i++) {
            String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
            if(documentSnapshot.contains(field) && i<= daysRange){
                int value= Integer.parseInt(documentSnapshot.getData().get(field).toString()) ;
                yawnStartTime = yawnStartTime == 0 ? i : yawnStartTime;
                highestYLength = value > highestYLength ? value : highestYLength;
                yawnList.add(value);
                if(value>0){
                    lowestTime = (i < lowestTime) ? i : lowestTime;
                    highestTime = (i > highestTime) ? i : highestTime;
                }
            }else{
                break;
            }
        }
        //remove trailing zeros at the end

        checkCount++;
        displayChart(drowsyList,yawnList, range);
    }

    public void displayChart(ArrayList<Integer> drowsyList,ArrayList<Integer> yawnList, String range){
        if(checkCount!=2){
            return;
        }
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());
        String formattedDate = sdf.format(currentDate);

        String timeRange="";




        if(highestTime==1 && lowestTime==24){
            drowsyList.clear();
            yawnList.clear();
        }else{
            if(range.equals("today") || range.equals("yesterday")){
                for (int i = 0; i < highestTime-lowestTime+2; i++) {
                    if(i==0){
                        xValues.add((lowestTime==1 || lowestTime==13)?"12:00":convertTo12(lowestTime)-1+":00");
                    }else if (i==1){
                        xValues.add((convertTo12(lowestTime))+":00");
                    }else {
                        xValues.add((convertTo12(lowestTime+i-1))+":00");
                    }
                }
                if(!drowsyList.isEmpty() || !yawnList.isEmpty()){
                    timeRange= xValues.get(0)+ ((lowestTime-1)>12?" PM":" AM")+" - "+xValues.get(xValues.size()-1)+ (highestTime>12?" PM":" AM");
                    timeRangeTV.setText(timeRange);
                }else{
                    timeRangeTV.setText("");
                }
                yawnList= removeElements(yawnList,yawnList.size()-highestTime, lowestTime-1);
                drowsyList= removeElements(drowsyList,drowsyList.size()-highestTime, lowestTime-1);

            }else if(range.equals("day")){

                for (int i = 0; i < highestTime-lowestTime+1; i++) {

                    Date date;
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    date = calendar.getTime();
                    xValues.add(sdf.format(date));
                }

                Collections.reverse(xValues);
                xValues.add(0,"Date");
                xValues.add(sdf.format(currentDate));

                yawnList= removeElements(yawnList,yawnList.size()-highestTime, lowestTime-1);
                drowsyList= removeElements(drowsyList,drowsyList.size()-highestTime, lowestTime-1);
            }



        }



        Description description = new Description();
        description.setText("Count");
        description.setTextSize(10f);
        description.setPosition(60f,15f);
        lineChart.setDescription(description);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.setTouchEnabled(true);




        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues));
        xAxis.setLabelCount(xValues.size());
        xAxis.setGranularity(1f);


        highestYLength=highestYLength>5?highestYLength:5;
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(highestYLength+1);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setLabelCount(highestYLength+1);


        List<Entry> drowsyCountList = new ArrayList<>();
        List<Entry> yawnCountList = new ArrayList<>();

        drowsyCountList.add( new Entry(0, 0));
        yawnCountList.add( new Entry(0, 0));

        if(range.equals("day") || range.equals("day3") || range.equals("day7")){
            timeRangeTV.setText("");

            for (int i = 0; i < drowsyList.size(); i++) {
                drowsyCountList.add(new Entry(i+1, drowsyList.get(drowsyList.size() - 1 - i)));
            }

            for (int i = 0; i < yawnList.size(); i++) {
                yawnCountList.add(new Entry(i+1, yawnList.get(yawnList.size() - 1 - i)));
            }

            //check if no detection for day1 in day30 chart
            if(Integer.parseInt(CameraActivity.drowsyCountDocument.getData().get("day01").toString())==0 && Integer.parseInt(CameraActivity.yawnCountDocument.getData().get("day01").toString())==0 && range.equals("day")){
                drowsyCountList.add(new Entry(drowsyCountList.size(), 0));
                yawnCountList.add(new Entry(yawnCountList.size(), 0));
            }

        }else{
            for (int i = 0; i < drowsyList.size(); i++) {
                drowsyCountList.add(new Entry(i+1, drowsyList.get(i)));
            }

            for (int i = 0; i < yawnList.size(); i++) {
                yawnCountList.add(new Entry(i+1, yawnList.get(i)));
            }
        }


        LineDataSet dataSet1 = new LineDataSet(drowsyCountList, "Drowsy");
        dataSet1.setColor(Color.BLUE);
        dataSet1.setValueFormatter(new MyValueFormatter());
        dataSet1.setDrawCircles(false);

        LineDataSet dataSet2 = new LineDataSet(yawnCountList, "Yawn");
        dataSet2.setColor(Color.RED);
        dataSet2.setValueFormatter(new MyValueFormatter());
        dataSet2.setDrawCircles(false);

        LineData lineData = new LineData(dataSet1, dataSet2);

        lineChart.setData(lineData);

        lineChart.invalidate();
    }

    private static ArrayList<Integer> removeElements(ArrayList<Integer> list, int endRemove, int startRemove) {
        if (endRemove > list.size() || startRemove > list.size()) {
            throw new IllegalArgumentException("n1 or n2 exceeds the size of the list");
        }

        ArrayList<Integer> newList = new ArrayList<>();

        // Add elements from original list excluding the first n2 elements
        for (int i = startRemove; i < list.size(); i++) {
            newList.add(list.get(i));
        }

        // Remove n1 elements from the end of the new list
        for (int i = 0; i < endRemove; i++) {
            newList.remove(newList.size() - 1);
        }

        return newList;
    }

    public int convertTo12(int hour){
        if(hour <13){
            return hour;
        }else{
            return hour-12;
        }
    }

    public class MyValueFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            // This will return the value as an integer string
            return String.valueOf((int) Math.round(value));
        }
    }
}