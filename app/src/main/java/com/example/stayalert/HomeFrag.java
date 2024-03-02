package com.example.stayalert;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import firebase.classes.FirebaseDatabase;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link Home_Frag#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFrag extends Fragment {

    private static final String TAG = "HomeFrag";
    public static ImageButton viewDetectionBtn;
    TextView timeofDay;
    TextView tempTV, humidTV, weatherType;
    public static TextView statusDriverTV, nameDriverTV;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();
    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appid = "449466108ec00a49fe1c77ffe6f31406";
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseDB = new FirebaseDatabase();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        viewDetectionBtn = view.findViewById(R.id.viewDetectionBtn);
        timeofDay = view.findViewById(R.id.timeOfDayTV);
        statusDriverTV= view.findViewById(R.id.statusDriverTV);
        nameDriverTV=view.findViewById(R.id.nameDriverTV);
        tempTV= view.findViewById(R.id.temperatureTV);
        humidTV= view.findViewById(R.id.hAndLTV);
        weatherType= view.findViewById(R.id.weatherTypeTV);

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        userData= cameraActivity.userInfo;

        if(!userData.isEmpty()){
            nameDriverTV.setText(userData.get("first_name").toString() + " " + userData.get("middle_name") + ". " + userData.get("last_name") + " " + userData.get("suffix"));
        }
        statusDriverTV.setText( cameraActivity.statusDriver);
        // Set an OnClickListener for the viewDetectionBtn
        viewDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraActivity.elevation= 30;
                cameraActivity.changeFrameLayoutElevation();

            }
        });


        timeofDay.setText("Good "+getTimeOfDay());

        getWeatherDetails("calape","Philippines");

        return view;
    }


    public String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "morning";
        } else if(hour >= 12 && hour < 13) {
            return "noon";
        }else if (hour >= 13 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }


    public void getWeatherDetails(String city, String country) {
        String tempUrl = "";

        if(!country.equals("")){
            tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;
        }else{
            tempUrl = url + "?q=" + city + "&appid=" + appid;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String output = "";
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    String description = jsonObjectWeather.getString("description");
                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                    double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                    float pressure = jsonObjectMain.getInt("pressure");
                    int humidity = jsonObjectMain.getInt("humidity");
                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    String wind = jsonObjectWind.getString("speed");
                    JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                    String clouds = jsonObjectClouds.getString("all");
                    JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                    String countryName = jsonObjectSys.getString("country");
                    String cityName = jsonResponse.getString("name");
                    output += "Current weather of " + cityName + " (" + countryName + ")"
                            + "\n Temp: " + df.format(temp) + " °C"
                            + "\n Feels Like: " + df.format(feelsLike) + " °C"
                            + "\n Humidity: " + humidity + "%"
                            + "\n Description: " + description
                            + "\n Wind Speed: " + wind + "m/s (meters per second)"
                            + "\n Cloudiness: " + clouds + "%"
                            + "\n Pressure: " + pressure + " hPa";

                    tempTV.setText((int)temp+"°");
                    humidTV.setText("H:"+humidity+"");
                    weatherType.setText(description);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CameraActivity.context, error.toString().trim(), Toast.LENGTH_SHORT).show();
            }


        });
        RequestQueue requestQueue = Volley.newRequestQueue(CameraActivity.context);
        requestQueue.add(stringRequest);
    }


}