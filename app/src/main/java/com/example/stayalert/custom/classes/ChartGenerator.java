package com.example.stayalert.custom.classes;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.stayalert.CameraActivity;
import com.example.stayalert.HomeFrag;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import firebase.classes.FirebaseDatabase;

public class ChartGenerator {

    String collection, document, source;
    FirebaseDatabase firebaseDB;
    FirebaseUser user;
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
    ArrayList<Integer> averageDrowsyResponseList = new ArrayList<>();
    String timeRange="";
    String range="today";
    FirebaseFirestore db;


    public ChartGenerator() {

        firebaseDB = new FirebaseDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db= FirebaseFirestore.getInstance();
    }

    public void fetchDetectionCounts(){
        int highestYLength=0;
        daysRange=80;
        checkCount=0;

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());
        xValues.clear();
        if(range.equals("day3")){
            daysRange=3;;
        }else if(range.equals("day7")){
            daysRange=7;
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

        firebaseDB.readData("average_response_count", user.getUid(), "default", new FirebaseDatabase.OnGetDataListener() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    for (int i = 1; i < 80; i++) {
                        String field=range+String.format("%02d",i);
                        if(documentSnapshot.contains(field) && i<= daysRange){
                            int value= Integer.parseInt(documentSnapshot.getData().get(field).toString()) ;
                            averageDrowsyResponseList.add(value);
                        }else{
                            break;
                        }
                    }
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

    }

    public void processDrowsyCount(DocumentSnapshot documentSnapshot, String range){
        drowsyList.clear();
        for (int i = 1; i < 80; i++) {
            String field=((range.equals("day7") || range.equals("day3"))?"day":range)+String.format("%02d",i);
            if(documentSnapshot.contains(field) && i<= daysRange){
                int value= Integer.parseInt(documentSnapshot.getData().get(field).toString()) ;
                drowsyStartTime= drowsyStartTime==0?i:drowsyStartTime;
                highestYLength= Math.max(value, highestYLength);
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
        if(lineChart!=null){
            processChart();
        }else{
            checkNewNotif();
        }
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
        if(lineChart!=null){
            processChart();
        }else{
            checkNewNotif();
        }
    }



    public void processChart(){
        if(checkCount!=2){
            return;
        }
        List<String> xValues= new ArrayList<>();
        int daysRange=0;
        timeRange="";

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());

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
            }else if(range.equals("day3")){
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
        yAxis.setGranularity(highestYLength>12?3f:1f);


        List<Entry> drowsyCountList = new ArrayList<>();
        List<Entry> yawnCountList = new ArrayList<>();

        drowsyCountList.add( new Entry(0, 0));
        yawnCountList.add( new Entry(0, 0));

        if(range.equals("day") || range.equals("day3") || range.equals("day7")){

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

    public void setLineChart(LineChart lineChart){
        this.lineChart=lineChart;
    }

    public String getTimeRange(){
        return timeRange;
    }

    public double getAverageDrowsyResponseSum(){
        double averageDrowsyResponseSum=0;
        for (int number : averageDrowsyResponseList) {
            if(number>0){
                averageDrowsyResponseSum += number;
            }
        }
        return averageDrowsyResponseSum/getDrowsyCount();
    }

    public void setRange(String range){
        this.range=range;
    }

    public int getDrowsyCount(){
        int sum = 0;
        for (int number : drowsyList) {
            if(number>0){
                sum += number;
            }
        }
        return sum;
    }

    public int getYawnCount(){
        int sum = 0;
        for (int number : yawnList) {
            if(number>0){
                sum += number;
            }
        }
        return sum;
    }

    public  ArrayList<Integer> getDrowsyList(){
        return drowsyList;
    }
    public  ArrayList<Integer> getYawnList(){
        return yawnList;
    }

    public int getLowestTime(){
        return  lowestTime;
    }

    public int getHighestTime(){
        return  highestTime;
    }

    public int getHighestYLength(){
        return  highestYLength;
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

    public void checkNewNotif(){
        if(checkCount!=2){
            return;
        }

        if ((Integer.parseInt(CameraActivity.drowsyCountDocument.get("day01").toString()) == 0 &&
                        Integer.parseInt(CameraActivity.yawnCountDocument.get("day01").toString()) == 0)){
            Log.d("ChartGenerator",  " no notif");
            firebaseDB.getNotificationsList();
            firebaseDB.updateCheckin();
            return;
        }

        Query query= db.collection("users/"+user.getUid()+"/user_notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        System.out.println("sizee "+querySnapshot.size());
                        Timestamp timestamp= new Timestamp(new Date());
                        if(!querySnapshot.isEmpty()){
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            timestamp = documentSnapshot.getTimestamp("timestamp");
                        }
                        if(querySnapshot.isEmpty() || !DateUtils.isToday(timestamp.toDate().getTime()) ){
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 0); // Move to yesterday
                            calendar.set(Calendar.HOUR_OF_DAY, 12);
                            Object timestampObject = CameraActivity.userInfo.get("last_sign_in");
                            Timestamp firestoreTimestamp = (Timestamp) timestampObject;
                            Date lastSignInDate = firestoreTimestamp.toDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                            String formattedTime = dateFormat.format(lastSignInDate);
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                            String detectionDate = sdf.format(lastSignInDate);
                            double averageResponse=getAverageDrowsyResponseSum();
                            int yawnCount= getYawnCount();
                            int drowsyCount= getDrowsyCount();
                            int lowestTime= getLowestTime();
                            int highestTime= getHighestTime();
                            int highestYLength= getHighestYLength();
                            Map<String, Object> notifInfo = new HashMap<>();
                            notifInfo.put("wasRead",false);
                            notifInfo.put("title","Detection Report Summary");
                            notifInfo.put("message","\\t\\tGood day, Lorem Ipsum. Here is the summary report for "+detectionDate+". You started using the app at "+formattedTime+"." +
                                    " \\n\\t\\tThis information, combined with the detection summary, can help you understand your drowsiness patterns and take necessary " +
                                    "actions to stay safe on the road.\\n\\t\\t\\u2022 Total Sleep Detected: "+drowsyCount+"\\n\\t\\t\\u2022 Total Yawn Detected: "+yawnCount+
                                    "\\n\\t\\t\\u2022 Average Drowsy Response Time: "+(Double.isNaN(averageResponse)?0:averageResponse)+"s");
                            notifInfo.put("timestamp",calendar.getTime());
                            notifInfo.put("drowsy_list",getDrowsyList());
                            notifInfo.put("yawn_list",getYawnList());
                            notifInfo.put("time_values",new ArrayList<Integer>(){
                                {
                                    add(lowestTime);
                                    add(highestTime);
                                    add(highestYLength);
                                }
                            });
                            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                            firebaseDB.writeUserInfo(notifInfo, "users/" + user.getUid() + "/user_notifications", "notif" + time, new FirebaseDatabase.TaskCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    HomeFrag.changeNotifIcon(true);
                                    firebaseDB.getNotificationsList();
                                    firebaseDB.updateCheckin();
                                    Log.d("ChartGenerator", " write notification success");
                                }
                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.d("ChartGenerator",  " write notification fail "+errorMessage);
                                }
                            });
                            //update check in after notif is writeen
                        }

                    }

                } else {
                    // Handle failures
                    Exception exception = task.getException();
                    Log.d( "checkNewNotif","Error getting documents: " + exception.getLocalizedMessage());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }
}