/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.stayalert;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.stayalert.databinding.ActivityHomeBinding;
import com.example.stayalert.env.ImageUtils;
import com.example.stayalert.env.Logger;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import firebase.classes.FirebaseDatabase;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
//        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
  MeowBottomNavigation bottomNavigation;
  private static final Logger LOGGER = new Logger();
  private static final String TAG = "CameraActivity";
  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  private static final String ASSET_PATH = "";
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private boolean debug = false;
  protected Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  protected int defaultModelIndex = 0;
  protected int defaultDeviceIndex = 0;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;
  protected ArrayList<String> modelStrings = new ArrayList<String>();

  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;

  protected TextView frameValueTextView, cropValueTextView, inferenceTimeTextView, msTV;
  protected ImageView bottomSheetArrowImageView;
  private ImageView plusImageView, minusImageView;
  protected ListView deviceView;
  protected TextView threadsTextView;
  protected ListView modelView;
  /** Current indices of device and model. */
  int currentDevice = -1;
  int currentModel = -1;
  int currentNumThreads = -1;
  FrameLayout frameLayout;
  ImageButton backBtn;
  boolean rearCam=false;

  public static int elevation=0;
  public static String eyeStatus="";
  public static String mouthStatus="";

  private String[] values = new String[30];
  private int currentIndex = 0;
  private int closeCount = 0;
  private Runnable runnableCode;
  boolean ring=false;
  Ringtone ringtone;
  boolean offlineMode=false, appStopped=false;
  Handler schedHandler;
  Runnable connectivityCheckRunnable;
  String statusDriver=" ACTIVE ";
  FirebaseUser user;
  FirebaseFirestore db;
  FirebaseDatabase firebaseDB;
  Map<String, Object> userInfo = new HashMap<>();

  ArrayList<String> deviceStrings = new ArrayList<String>();


  @Override
  protected void onCreate(final Bundle savedInstanceState) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      View decorView = getWindow().getDecorView();
      decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }
    firebaseDB= new FirebaseDatabase();
    db = FirebaseFirestore.getInstance();
    user = FirebaseAuth.getInstance().getCurrentUser();
    getUserInfo();

    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.tfe_od_activity_camera);
    frameLayout = findViewById(R.id.container);
    Handler handler = new Handler();
    Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    // Create a Ringtone object from the URI
    ringtone = RingtoneManager.getRingtone(CameraActivity.this, defaultRingtoneUri);


    bottomNavigation= findViewById(R.id.bottomNavigation);
    backBtn= findViewById(R.id.backBtn);


    bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_menu));
    bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_call));
    bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_home));
    bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.ic_stats));
    bottomNavigation.add(new MeowBottomNavigation.Model(5, R.drawable.ic_profile));





    runnableCode = new Runnable() {
      @Override
      public void run() {
        String newValue = eyeStatus;
        if ("closed".equals(newValue) && closeCount<30) {
          closeCount++;
        }else if (closeCount>0){
          closeCount--;
        }
        values[currentIndex] = newValue;
        currentIndex = (currentIndex + 1) % 30; // Wrap around to the beginning of the array
        double closePercentage = (closeCount / 30.0) * 100.0;


        if(closePercentage>90 && !ringtone.isPlaying() && !appStopped){
          Toast.makeText(CameraActivity.this, "Please WAKE UP!!!!", Toast.LENGTH_SHORT).show();

          // Play the default ringtone
          statusDriver=" SLEEPY ";
          ringtone.play();
          HomeFrag.statusDriverTV.setText(statusDriver);
          System.out.println("playinh ring");
        }
        else if(closePercentage<90 && HomeFrag.statusDriverTV!=null){
          statusDriver=" ACTIVE ";
          HomeFrag.statusDriverTV.setText(statusDriver);
          ringtone.stop();

        }

        // Schedule the next update
//        System.out.println("looping "+ closePercentage+"\n"+ Arrays.toString(values)+"\n closeCount: "+closeCount);

        handler.postDelayed(this, 100); // Schedule the task to run again after 100 milliseconds
      }
    };
    handler.post(runnableCode);

    Handler delayedExecutionHandler = new Handler();
    delayedExecutionHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
          minusImageView.performClick();
          updateActiveModel();

      }
    }, 3000);






    bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
      @Override
      public Unit invoke(MeowBottomNavigation.Model model) {
        switch (model.getId()){
          case 1:
            replaceFragment(new MenuFrag());
            Toast.makeText(CameraActivity.this, "Menu", Toast.LENGTH_SHORT).show();
            break;
          case 2:
            replaceFragment(new PhoneFrag());
            Toast.makeText(CameraActivity.this, "Phone", Toast.LENGTH_SHORT).show();

            break;
          case 3:
            replaceFragment(new HomeFrag());
            Toast.makeText(CameraActivity.this, "Home", Toast.LENGTH_SHORT).show();

            break;
          case 4:
            replaceFragment(new StatsFrag());
            Toast.makeText(CameraActivity.this, "Stats", Toast.LENGTH_SHORT).show();

            break;
          case 5:
            replaceFragment(new ProfileFrag());
            Toast.makeText(CameraActivity.this, "Profile", Toast.LENGTH_SHORT).show();

            break;
        }
        return null;
      }
    });

    backBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        elevation=0;
        changeFrameLayoutElevation();
      }
    });

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }

    threadsTextView = findViewById(R.id.threads);
    currentNumThreads = Integer.parseInt(threadsTextView.getText().toString().trim());
    plusImageView = findViewById(R.id.plus);
    minusImageView = findViewById(R.id.minus);
    deviceView = findViewById(R.id.device_list);
    deviceStrings.add("CPU");
    deviceStrings.add("GPU");
    deviceStrings.add("NNAPI");
    deviceView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter<String> deviceAdapter =
            new ArrayAdapter<>(
                    CameraActivity.this , R.layout.deviceview_row, R.id.deviceview_row_text, deviceStrings);
    deviceView.setAdapter(deviceAdapter);
    deviceView.setItemChecked(defaultDeviceIndex, true);
    currentDevice = defaultDeviceIndex;
    deviceView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateActiveModel();
              }
            });

    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    gestureLayout = findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
    modelView = findViewById((R.id.model_list));

    modelStrings = getModelStrings(getAssets(), ASSET_PATH);
    modelView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter<String> modelAdapter =
            new ArrayAdapter<>(
                    CameraActivity.this , R.layout.listview_row, R.id.listview_row_text, modelStrings);
    modelView.setAdapter(modelAdapter);
    modelView.setItemChecked(defaultModelIndex, true);
    currentModel = defaultModelIndex;
    modelView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateActiveModel();
              }
            });

    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
              gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
              gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            //                int width = bottomSheetLayout.getMeasuredWidth();
            int height = gestureLayout.getMeasuredHeight();

            sheetBehavior.setPeekHeight(height);
          }
        });
    sheetBehavior.setHideable(false);

    sheetBehavior.setBottomSheetCallback(
        new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
              case BottomSheetBehavior.STATE_HIDDEN:
                break;
              case BottomSheetBehavior.STATE_EXPANDED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                }
                break;
              case BottomSheetBehavior.STATE_COLLAPSED:
                {
                  bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                }
                break;
              case BottomSheetBehavior.STATE_DRAGGING:
                break;
              case BottomSheetBehavior.STATE_SETTLING:
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                break;
            }
          }

          @Override
          public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

    frameValueTextView = findViewById(R.id.frame_info);
    cropValueTextView = findViewById(R.id.crop_info);
    inferenceTimeTextView = findViewById(R.id.inference_info);
    msTV = findViewById(R.id.msTV);


    plusImageView.setOnClickListener(this);
    minusImageView.setOnClickListener(this);


  }

  ////////////////////////////////////////////

  private void getUserInfo(){
    firebaseDB.readData("users", user.getUid(),"cache", new FirebaseDatabase.OnGetDataListener() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        Log.d(TAG, "DocumentSnapshot datas: " + documentSnapshot.getData());
        userInfo = documentSnapshot.getData();
        bottomNavigation.show(3,true);
        replaceFragment(new HomeFrag());
        bottomNavigation.clearCount(1);
      }

      @Override
      public void onStart() {
        Log.d(TAG, "Start getting user info");
      }

      @Override
      public void onFailure(Exception e) {
        Log.d(TAG,"Failed getting user info: "+e);
        Toast.makeText(CameraActivity.this, "Unable to load user information", Toast.LENGTH_SHORT).show();
        bottomNavigation.show(3,true);
        replaceFragment(new HomeFrag());
        bottomNavigation.clearCount(1);
      }
    });

  }


  @Override
  public void onBackPressed() {
    if(backBtn.getVisibility()==View.VISIBLE){
      elevation=0;
      backBtn.setVisibility(View.GONE);
      msTV.setElevation(elevation);
      frameLayout.setElevation(elevation);
    }

  }
  public void stopActivity(){
    if (ringtone.isPlaying()) {
      System.out.println("is playinh");
    }
    ringtone.stop();
    appStopped=true;
    handler.removeCallbacks(runnableCode);
    finish();
  }



  private void replaceFragment(androidx.fragment.app.Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.frame_layout,fragment);
    transaction.commit();
  }
  public void changeFrameLayoutElevation() {
    int newElevation= elevation;
    if (frameLayout != null) {
      frameLayout.setElevation(newElevation);
      if(newElevation>0){
        backBtn.setElevation(newElevation);
        backBtn.setVisibility(View.VISIBLE);
        msTV.setElevation(newElevation);
        HomeFrag.viewDetectionBtn.setVisibility(View.GONE);
      }
      else{
        backBtn.setVisibility(View.GONE);
        msTV.setElevation(newElevation);
        HomeFrag.viewDetectionBtn.setVisibility(View.VISIBLE);
      }
    }
  }




  protected ArrayList<String> getModelStrings(AssetManager mgr, String path){
    ArrayList<String> res = new ArrayList<String>();
    try {
      String[] files = mgr.list(path);
      for (String file : files) {
        String[] splits = file.split("\\.");
        if (splits[splits.length - 1].equals("tflite")) {
          res.add(file);
        }
      }

    }
    catch (IOException e){
      System.err.println("getModelStrings: " + e.getMessage());
    }
    return res;
  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /** Callback for android.hardware.Camera API */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /** Callback for Camera2 API */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);
    ringtone.stop();
    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required",
                Toast.LENGTH_LONG)
            .show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // Check if this is the front-facing camera
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          final StreamConfigurationMap map =
                  characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
          if (map != null) {
            // Fallback to camera1 API for internal cameras that don't have full support.
            // This should help with legacy situations where using the camera2 API causes
            // distorted or otherwise broken previews.
//            useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
            useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                    || isHardwareLevelSupported(
                    characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
            LOGGER.i("Camera API lv2?: %s", useCamera2API);
            return cameraId;
          }
        }
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }


  protected void setFragment() {

//    String cameraId = chooseCamera();

    String cameraId = rearCam ? "0":chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivity.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              getLayoutId(),
              getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();


  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }


//  @Override
//  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//    setUseNNAPI(isChecked);
//    if (isChecked) apiSwitchCompat.setText("NNAPI");
//    else apiSwitchCompat.setText("TFLITE");
//  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.plus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads >= 9) return;
      numThreads++;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    } else if (v.getId() == R.id.minus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads == 1) {
        return;
      }
      numThreads--;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    }
  }

  protected void showFrameInfo(String frameInfo) {
    frameValueTextView.setText(frameInfo);
  }

  protected void showCropInfo(String cropInfo) {
    cropValueTextView.setText(cropInfo);
  }

  protected void showInference(String inferenceTime) {
    inferenceTimeTextView.setText(inferenceTime);
    msTV.setText(inferenceTime);

  }

  protected abstract void updateActiveModel();
  protected abstract void processImage();


  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void setNumThreads(int numThreads);

  protected abstract void setUseNNAPI(boolean isChecked);
}
