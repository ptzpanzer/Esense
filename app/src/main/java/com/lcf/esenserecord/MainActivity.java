package com.lcf.esenserecord;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lcf.esenserecord.io.esense.esenselib.ESenseManager;
import com.lcf.esenserecord.io.esense.esenselib.ESenseConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "Esense";
    private String deviceName = "eSense-0091";
    private String activityName = "Activity";
    private String backGroundName = "Background";
    private String combineName = "ActivityWhileBackground";
    private static String actor = "off";
    private int timeout = 10000;

    private Button connectButton;
    private ToggleButton recordButton;
    private ToggleButton startButton;
    private Button initButton1;
    private Button initButton2;
    private Button stayButton;
    private Button walkButton;
    private Button runButton;
    private Button upStairsButton;
    private Button headShakeLeftButton;
    private Button headShakeRightButton;
    private Button headNodUpButton;
    private Button headNodDownButton;
    private Button headYawLeftButton;
    private Button headYawRightButton;

    private ListView activityListView;
    private Chronometer chronometer;

    private TextView connectionTextView;
    private TextView deviceNameTextView;
    private TextView activityTextView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;

    Calendar currentTime;
    ESenseManager eSenseManager;
    Activity activityObj;
    DatabaseHandler databaseHandler;
    SensorListenerManager sensorListenerManager;
    ConnectionListenerManager connectionListenerManager;
    private static final int PERMISSION_REQUEST_CODE = 200;

    public static String getActor() {
        return actor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.esense);

        sharedPreferences = getSharedPreferences("eSenseSharedPrefs",Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();

        recordButton = findViewById(R.id.recordButton);
        startButton = findViewById(R.id.startStopButton);
        connectButton = findViewById(R.id.connectButton);
        initButton1 = findViewById(R.id.initButton1);
        initButton2 = findViewById(R.id.initButton2);
        stayButton = findViewById(R.id.stayButton);
        walkButton = findViewById(R.id.walkButton);
        runButton = findViewById(R.id.runButton);
        upStairsButton = findViewById(R.id.upStairsButton);
        headShakeLeftButton = findViewById(R.id.headShakeLeftButton);
        headShakeRightButton = findViewById(R.id.headShakeRightButton);
        headNodUpButton = findViewById(R.id.headNodUpButton);
        headNodDownButton = findViewById(R.id.headNodDownButton);
        headYawLeftButton = findViewById(R.id.headYawLeftButton);
        headYawRightButton = findViewById(R.id.headYawRightButton);

        recordButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        connectButton.setOnClickListener(this);
        initButton1.setOnClickListener(this);
        initButton2.setOnClickListener(this);
        stayButton.setOnClickListener(this);
        walkButton.setOnClickListener(this);
        runButton.setOnClickListener(this);
        upStairsButton.setOnClickListener(this);
        headShakeLeftButton.setOnClickListener(this);
        headShakeRightButton.setOnClickListener(this);
        headNodUpButton.setOnClickListener(this);
        headNodDownButton.setOnClickListener(this);
        headYawLeftButton.setOnClickListener(this);
        headYawRightButton.setOnClickListener(this);

        connectionTextView = findViewById(R.id.connectionTV);
        deviceNameTextView = findViewById(R.id.deviceNameTV);
        activityTextView = findViewById(R.id.activityTV);
        chronometer = findViewById(R.id.chronometer);

        databaseHandler = new DatabaseHandler(this);
        activityListView = findViewById(R.id.activityListView);
        ArrayList<Activity> activityHistory = databaseHandler.getAllActivities();
        if(activityHistory.size() > 0){
            activityListView.setAdapter(new ActivityListAdapter(this, activityHistory));
        }

        sensorListenerManager = new SensorListenerManager(this);
        connectionListenerManager = new ConnectionListenerManager(this, sensorListenerManager,
                connectionTextView, deviceNameTextView, sharedPrefEditor);


        if (!checkPermission()) {
            requestPermission();
        } else {
            Log.d(TAG, "Permission already granted..");
        }
    }

    public static boolean isESenseDeviceConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clear_menu:
                Toast.makeText(this, "Clear history...", Toast.LENGTH_SHORT).show();
                databaseHandler.deleteTable();
                ArrayList<Activity> activityHistory = databaseHandler.getAllActivities();
                activityListView.setAdapter(new ActivityListAdapter(this, activityHistory));
                return true;
            case R.id.reset_menu:
                Toast.makeText(this, "Reset connection..", Toast.LENGTH_SHORT).show();
                eSenseManager = null;
                
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.connectButton:
                connectEarables();
                break;

            case R.id.initButton1:
                activityName = "INIT1";
                backGroundName = "INIT1";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.initButton2:
                activityName = "INIT2";
                backGroundName = "INIT2";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.stayButton:
                backGroundName = "Staying";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.walkButton:
                backGroundName = "Walking";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.runButton:
                backGroundName = "Running";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.upStairsButton:
                backGroundName = "UpStairs";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headShakeLeftButton:
                activityName = "HeadShakingLeft";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headShakeRightButton:
                activityName = "HeadShakingRight";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headNodUpButton:
                activityName = "HeadNodUp";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headNodDownButton:
                activityName = "HeadNodDown";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headYawLeftButton:
                activityName = "HeadYawLeft";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.headYawRightButton:
                activityName = "HeadYawRight";
                combineName = activityName + "While" + backGroundName;
                sharedPrefEditor.putString("activityName", combineName);
                sharedPrefEditor.commit();
                setActivityName();
                break;

            case R.id.recordButton:
                if(recordButton.isChecked()) { //如果是绿三角状态
                    if (eSenseManager != null) {
                        ESenseConfig eSenseConfig = new ESenseConfig(ESenseConfig.AccRange.G_4, ESenseConfig.GyroRange.DEG_1000, ESenseConfig.AccLPF.BW_5, ESenseConfig.GyroLPF.BW_5);
                        eSenseManager.setSensorConfig(eSenseConfig);
                    }
                    if(activityName.equals("Activity") || backGroundName.equals("Background")){
                        recordButton.setChecked(false);
                        showAlertMessage();
                    }else{

                        activityObj = new Activity();

                        currentTime = Calendar.getInstance();
                        int hour = currentTime.get(Calendar.HOUR_OF_DAY) ;
                        int minute = currentTime.get(Calendar.MINUTE);
                        int second = currentTime.get(Calendar.SECOND);

                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();

                        if(activityObj != null){
                            String startTime = hour + " : " + minute + " : " + second;
                            activityObj.setActivityName(combineName);
                            activityObj.setStartTime(startTime);
                        }

                        sharedPrefEditor.putString("checked", "on");
                        sharedPrefEditor.commit();
                        recordButton.setBackgroundResource(R.drawable.stop);

                        startDataCollection(combineName);
                    }

                } else { //如果是红方块状态

                    currentTime = Calendar.getInstance();
                    int hour = currentTime.get(Calendar.HOUR_OF_DAY) ;
                    int minute = currentTime.get(Calendar.MINUTE);
                    int second = currentTime.get(Calendar.SECOND);

                    chronometer.stop();

                    if(activityObj != null){
                        String stopTime = hour + " : " + minute + " : " + second;
                        String duration = chronometer.getText().toString();
                        activityObj.setStopTime(stopTime);
                        activityObj.setDuration(duration);
                    }

                    sharedPrefEditor.putString("checked", "off");
                    sharedPrefEditor.commit();
                    recordButton.setBackgroundResource(R.drawable.start);

                    stopDataCollection();

                    if(databaseHandler != null){
                        if(activityObj != null){
                            databaseHandler.addActivity(activityObj);
                            ArrayList<Activity> activityHistory = databaseHandler.getAllActivities();
                            activityListView.setAdapter(new ActivityListAdapter(this, activityHistory));

                            for (Activity activity : activityHistory) {
                                String activityLog = "Activity : " + activity.getActivityName() + " , Start Time : " + activity.getStartTime()
                                        + " , Stop Time : " + activity.getStopTime() + " , Duration : " + activity.getDuration();
                                Log.d(TAG, activityLog);
                            }
                        }
                    }

                    activityObj = null;
                }
                break;

            case R.id.startStopButton:
                if(startButton.isChecked()) { //如果是绿三角状态
                    actor = "on";
                    sharedPrefEditor.putString("actor", "on");
                    sharedPrefEditor.commit();
                    startButton.setBackgroundResource(R.drawable.stop);

                } else { //如果是红方块状态
                    actor = "off";
                    sharedPrefEditor.putString("actor", "off");
                    sharedPrefEditor.commit();
                    startButton.setBackgroundResource(R.drawable.start);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isConnected = isESenseDeviceConnected();
        if(isConnected){
            //Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }else{
            sharedPrefEditor.putString("status", "disconnected");
            sharedPrefEditor.commit();

            activityName = "Activity";
            backGroundName = "Background";
            combineName = activityName + "While" + backGroundName;
            sharedPrefEditor.putString("activityName", combineName);
            sharedPrefEditor.commit();

            //Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        }

        String isChecked =  sharedPreferences.getString("checked", null);
        String status =  sharedPreferences.getString("status", null);
        String activity =  sharedPreferences.getString("activityName", null);
        String getAction = sharedPreferences.getString("action", null);

        if(activity != null){
            combineName = activity;
            setActivityName();
        }

        if(status == null){
            connectionTextView.setText("Disconnected");
            deviceNameTextView.setText(deviceName);
        }else if(status.equals("connected")){
            connectionTextView.setText("Connected");
            deviceNameTextView.setText(deviceName);
        }else if(status.equals("disconnected")){
            connectionTextView.setText("Disconnected");
            deviceNameTextView.setText(deviceName);
        }

        if(isChecked == null){
            recordButton.setChecked(false);
            recordButton.setBackgroundResource(R.drawable.start);
        }else if(isChecked.equals("on")){
            recordButton.setChecked(true);
            recordButton.setBackgroundResource(R.drawable.stop);
        }else if(isChecked.equals("off")){
            recordButton.setChecked(false);
            recordButton.setBackgroundResource(R.drawable.start);
        }

        if(getAction == null){
            startButton.setChecked(false);
            startButton.setBackgroundResource(R.drawable.start);
        }else if(getAction.equals("on")){
            startButton.setChecked(true);
            startButton.setBackgroundResource(R.drawable.stop);
        }else if(getAction.equals("off")){
            startButton.setChecked(false);
            startButton.setBackgroundResource(R.drawable.start);
        }

        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    public void setActivityName(){
        activityTextView.setText(activityName + " While " + backGroundName);
    }

    public void connectEarables(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Please check the Device ID:");
        final EditText edittext = new EditText(MainActivity.this);
        edittext.setText(String.format(Locale.getDefault(), "%s", deviceName));
        edittext.setInputType(InputType.TYPE_CLASS_TEXT); // Number keyboard
        builder.setView(edittext);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deviceName = edittext.getText().toString().trim();
                        eSenseManager = new ESenseManager(deviceName, MainActivity.this.getApplicationContext(), connectionListenerManager);
                        eSenseManager.connect(timeout);
                    }
                });
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel
                    }
                });
        builder.create().show();
    }

    public void startDataCollection(String activity){
        sensorListenerManager.startDataCollection(activity);
    }

    public void stopDataCollection(){
        sensorListenerManager.stopDataCollection();
    }

    private boolean checkPermission() {
        int recordResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int locationResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return locationResult == PackageManager.PERMISSION_GRANTED &&
                writeResult == PackageManager.PERMISSION_GRANTED && recordResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,
                WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && storageAccepted && recordAccepted){
                        Log.d(TAG, "Permission granted");
                    } else {
                        Log.d(TAG, "Permission denied");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to all permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION,
                                                            WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void showAlertMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please select an activityName !")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
