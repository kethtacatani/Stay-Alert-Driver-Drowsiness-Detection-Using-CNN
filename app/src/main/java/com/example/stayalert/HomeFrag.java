package com.example.stayalert;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
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
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.stayalert.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import firebase.classes.FirebaseDatabase;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

///**
// * A simple {@link Fragment} subclass.
// * Use the {@link Home_Frag#newInstance} factory method to
// * create an instance of this fragment.
// */
public class HomeFrag extends Fragment {

    private static final String TAG = "HomeFrag";
    public static ImageButton viewDetectionBtn;
    TextView timeofDay;
    public static TextView tempTV, humidTV, weatherType;
    public static TextView statusDriverTV, nameDriverTV;
    FirebaseFirestore db;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    Map<String, Object> userData = new HashMap<>();
    private static final String url = "https://api.openweathermap.org/data/2.5/weather";
    private static final String appid = "449466108ec00a49fe1c77ffe6f31406";
    private static DecimalFormat df = new DecimalFormat("#.##");

    private MapView mapView;
    FloatingActionButton floatingActionButton;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(CameraActivity.context, "Permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(20.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };

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

        if(!CameraActivity.weatherMap.isEmpty()){
            displayWeatherInfo(CameraActivity.weatherMap);
        }else{
            getWeatherDetails("Calape", "Philippines");
        }

        mapView = view.findViewById(R.id.mapView);
        floatingActionButton = view.findViewById(R.id.focusLocation);



        floatingActionButton.hide();
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {


                mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(1000.0).build());
                LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
                locationComponentPlugin.setEnabled(true);
                LocationPuck2D locationPuck2D = new LocationPuck2D();
                locationPuck2D.setBearingImage(AppCompatResources.getDrawable(CameraActivity.context, R.drawable.ic_baseline_location_on_24));
                locationComponentPlugin.setLocationPuck(locationPuck2D);
                locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                getGestures(mapView).addOnMoveListener(onMoveListener);

                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                        getGestures(mapView).addOnMoveListener(onMoveListener);
                        floatingActionButton.hide();
                    }
                });
            }
        });


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


    public static void getWeatherDetails(String city, String country) {
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
                    CameraActivity.weatherMap.put("description", description);
                    CameraActivity.weatherMap.put("temp", temp);
                    CameraActivity.weatherMap.put("feels_like", feelsLike);
                    CameraActivity.weatherMap.put("pressure", pressure);
                    CameraActivity. weatherMap.put("humidity", humidity);
                    CameraActivity. weatherMap.put("wind", wind);
                    CameraActivity. weatherMap.put("clouds", clouds);
                    CameraActivity.  weatherMap.put("country", countryName);
                    CameraActivity. weatherMap.put("city", cityName);

                    displayWeatherInfo(CameraActivity.weatherMap);

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

    public static void displayWeatherInfo(Map<String, Object> weatherMap){
        if(weatherMap!=null && tempTV!=null){
            tempTV.setText(String.format("%.0f", weatherMap.get("temp"))+"°" );
            humidTV.setText("H:" + weatherMap.get("humidity") + "");
            weatherType.setText((String) weatherMap.get("description"));
        }
    }


}