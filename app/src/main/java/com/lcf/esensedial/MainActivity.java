package com.lcf.esensedial;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lcf.esensedial.io.esense.esenselib.ESenseManager;
import com.lcf.esensedial.io.esense.esenselib.ESenseConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.WAKE_LOCK;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button connectButton;
    private Button initButton1;
    private Button initButton2;
    private Button initButton3;
    private Button workButton;
    private ToggleButton recordButton;
    private TextView connectionTextView;
    private TextView deviceNameTextView;
    private TextView currentActivityTextView;
    private TextView orderTextView;
    private TextView recogTextView;
    private TextView ordersTextView;
    private TextView movesTextView;
    private TextView correctsTextView;
    private ImageView statusImageView;
    private Chronometer chronometer;

    private SoundPool mSoundPool;
    private int[] rsid;

    LinkedBlockingQueue<double[]> blockingQueue;
    double start_time;
    int first_chunk_flag;
    ArrayList<double[]> local_buffer;
    ArrayList<double[]> itpl_buffer;
    ArrayList<double[]> itpled_buffer;
    LinkedBlockingQueue<Integer> blockingQueue_predict;
    ArrayList<Integer> predict_list;
    ArrayList<String[]> log_list;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;
    ESenseManager eSenseManager;
    SensorListenerManager sensorListenerManager;
    ConnectionListenerManager connectionListenerManager;
    Classifer classifer;

    private String TAG = "Esense";
    private String deviceName = "eSense-0091";
    private int timeout = 10000;
    private String activityName = "";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private int target_rate = 50;
    private int windowSize = 60;
    private int windowStep = 5;

    File prop_path = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ESenseCaller");
    File prop_file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ESenseCaller" + File.separator + "Props.txt");
    ArrayList<String> recog_List;
    private int command_State;
    private DialBook db;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.esense);

        sharedPreferences = getSharedPreferences("eSenseSharedPrefs",Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();

        connectButton = findViewById(R.id.connectButton);
        initButton1 = findViewById(R.id.initButton1);
        initButton2 = findViewById(R.id.initButton2);
        initButton3 = findViewById(R.id.initButton3);
        workButton = findViewById(R.id.workButton);
        recordButton = findViewById(R.id.recordButton);

        connectButton.setOnClickListener(this);
        initButton1.setOnClickListener(this);
        initButton2.setOnClickListener(this);
        initButton3.setOnClickListener(this);
        workButton.setOnClickListener(this);
        recordButton.setOnClickListener(this);

        connectionTextView = findViewById(R.id.connectionTV);
        deviceNameTextView = findViewById(R.id.deviceNameTV);
        currentActivityTextView = findViewById(R.id.currentActivityTV);
        orderTextView = findViewById(R.id.ordersTV);
        recogTextView = findViewById(R.id.recogTV);
        ordersTextView = findViewById(R.id.ordersTextView);
        movesTextView = findViewById(R.id.movesTextView);
        correctsTextView = findViewById(R.id.correctsTextView);
        statusImageView = findViewById(R.id.statusImage);

        chronometer = findViewById(R.id.chronometer);

        try {
            classifer = new Classifer(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        blockingQueue = new LinkedBlockingQueue<>();
        sensorListenerManager = new SensorListenerManager(this, blockingQueue);
        connectionListenerManager = new ConnectionListenerManager(this, sensorListenerManager,
                connectionTextView, deviceNameTextView, statusImageView, sharedPrefEditor);

        AudioAttributes abs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build() ;
        mSoundPool =  new SoundPool.Builder()
                .setMaxStreams(100)   //设置允许同时播放的流的最大值
                .setAudioAttributes(abs)   //完全可以设置为null
                .build() ;
        rsid = new int[7];
        rsid[0] = mSoundPool.load(this, R.raw.l, 1);
        rsid[1] = mSoundPool.load(this, R.raw.r, 1);
        rsid[2] = mSoundPool.load(this, R.raw.u, 1);
        rsid[3] = mSoundPool.load(this, R.raw.d, 1);
        rsid[4] = mSoundPool.load(this, R.raw.yl, 1);
        rsid[5] = mSoundPool.load(this, R.raw.yr, 1);
        rsid[6] = mSoundPool.load(this, R.raw.alert, 1);

        if (!checkPermission()) {
            requestPermission();
        } else {
            Log.d(TAG, "Permission already granted..");
        }

        if(!prop_path.exists()) {
            prop_path.mkdir();
            try {
                prop_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if(!prop_file.exists()) {
                try {
                    prop_file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        command_State = 0;
        //sharedPrefEditor.putString("command", "0");
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
                eSenseManager.registerEventListener(new ESenseEventListenerImpl(this, mHandler));//设置其他方法的回调
                activityName = "INIT1";
                sharedPrefEditor.putString("activityName", activityName);
                sharedPrefEditor.commit();
                currentActivityTextView.setText(activityName);
                break;

            case R.id.initButton2:
                activityName = "INIT2";
                sharedPrefEditor.putString("activityName", activityName);
                sharedPrefEditor.commit();
                currentActivityTextView.setText(activityName);
                break;

            case R.id.initButton3:
                activityName = "INIT3";
                sharedPrefEditor.putString("activityName", activityName);
                sharedPrefEditor.commit();
                currentActivityTextView.setText(activityName);
                break;

            case R.id.workButton:
                activityName = "Work";
                sharedPrefEditor.putString("activityName", activityName);
                sharedPrefEditor.commit();
                currentActivityTextView.setText(activityName);
                break;

            case R.id.recordButton:
                if(recordButton.isChecked()) { //如果是绿三角状态
                    if (eSenseManager != null) {
                        ESenseConfig eSenseConfig = new ESenseConfig(ESenseConfig.AccRange.G_4, ESenseConfig.GyroRange.DEG_1000, ESenseConfig.AccLPF.BW_5, ESenseConfig.GyroLPF.BW_5);
                        eSenseManager.setSensorConfig(eSenseConfig);
                    }
                    if(activityName.equals("")){
                        recordButton.setChecked(false);
                        showAlertMessage();
                    }else{
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();

                        sharedPrefEditor.putString("checked", "on");
                        sharedPrefEditor.commit();
                        recordButton.setBackgroundResource(R.drawable.stop);

                        ordersTextView.setText("");
                        movesTextView.setText("");
                        correctsTextView.setText("");

                        startDataCollection(activityName);
                    }
                } else { //如果是红方块状态
                    chronometer.stop();

                    stopDataCollection();

                    sharedPrefEditor.putString("order", "");
                    sharedPrefEditor.commit();
                    orderTextView.setText("");
                    recogTextView.setText("");

                    sharedPrefEditor.putString("activityName", "");
                    sharedPrefEditor.commit();
                    activityName = "";
                    currentActivityTextView.setText("");

                    sharedPrefEditor.putString("checked", "off");
                    sharedPrefEditor.commit();
                    recordButton.setBackgroundResource(R.drawable.start);
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

            activityName = "";
            sharedPrefEditor.putString("activityName", activityName);
            sharedPrefEditor.commit();

            //Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
        }

        String isChecked =  sharedPreferences.getString("checked", null);
        String status =  sharedPreferences.getString("status", null);
        String activity =  sharedPreferences.getString("activityName", null);

        if(activity != null){
            activityName = activity;
            currentActivityTextView.setText(activityName);
        }
        if(status == null){
            connectionTextView.setText("Disconnected");
            deviceNameTextView.setText(deviceName);
            statusImageView.setImageResource(R.drawable.disconnected);
        }else if(status.equals("connected")){
            connectionTextView.setText("Connected");
            deviceNameTextView.setText(deviceName);
            statusImageView.setImageResource(R.drawable.connected);
        }else if(status.equals("disconnected")){
            connectionTextView.setText("Disconnected");
            deviceNameTextView.setText(deviceName);
            statusImageView.setImageResource(R.drawable.disconnected);
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

    public void startDataCollection(String activity) {
        sensorListenerManager.startDataCollection(activity);
    }

    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 7) {

                if(activityName.equals("Work")) {
                    if (command_State == 0) {
                        command_State = 1;
                        //sharedPrefEditor.putString("command", "1");
                        Log.d(TAG, "Dial Start");
                        Timer timer2 = new Timer();
                        Timer timer3 = new Timer();
                        first_chunk_flag = 0;
                        local_buffer = new ArrayList<>();
                        itpl_buffer = new ArrayList<>();
                        itpled_buffer = new ArrayList<>();

                        blockingQueue_predict = new LinkedBlockingQueue<>();
                        predict_list = new ArrayList<>();
                        log_list = new ArrayList<>();
                        recog_List = new ArrayList<>();

                        try {
                            db = new DialBook(prop_file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mSoundPool.play(rsid[6], 1, 1, 1, 0, 1);

                        timer2.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Message message = new Message();
                                message.what = 1;
                                mHandler.sendMessage(message);
                                if (sharedPreferences.getString("checked", null).equals("off")) {
                                    timer2.cancel();
                                }
                            }
                        }, 200, 200);

                        timer3.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Message message = new Message();
                                message.what = 2;
                                mHandler.sendMessage(message);
                                if (sharedPreferences.getString("checked", null).equals("off")) {
                                    timer3.cancel();
                                }
                            }
                        }, 200, 200);

                    }
                }

            } else if(msg.what == 1) {
                int n = blockingQueue.size();
                for(int i=0;i<n;i++) {
                    local_buffer.add(blockingQueue.poll());
                }
                if(first_chunk_flag == 0 && local_buffer.size() > 0) {
                    start_time = local_buffer.get(0)[7];
                    first_chunk_flag = 1;
                }
                for(int i=0;i<local_buffer.size();i++) {
                    if(local_buffer.get(i)[7] - start_time < (float)(1000 / 5)) {
                        itpl_buffer.add(local_buffer.get(i));
                        //Log.d(TAG, "itpl_buffer:" + itpl_buffer.toString());
                    } else {

                        itpl_buffer.add(local_buffer.get(i));
                        //Log.d(TAG, "itpl_buffer_Intering:" + itpl_buffer.toString());

                        int itpl_count = (target_rate / 5) - itpl_buffer.size();
                        if(itpl_count > 0 && itpl_buffer.size() > 1) {
                            for (int j = 0; j < itpl_count; j++) {
                                double max_interval = 0;
                                int max_index = 0;
                                for (int k = 0; k < itpl_buffer.size() - 1; k++) {
                                    double interval = itpl_buffer.get(k + 1)[7] - itpl_buffer.get(k)[7];
                                    if (interval > max_interval) {
                                        max_interval = interval;
                                        max_index = k;
                                    }
                                }
                                double[] itpl_temp = {itpl_buffer.get(max_index)[0] + (itpl_buffer.get(max_index + 1)[0] - itpl_buffer.get(max_index)[0]) / 2,
                                        itpl_buffer.get(max_index)[1] + (itpl_buffer.get(max_index + 1)[1] - itpl_buffer.get(max_index)[1]) / 2,
                                        itpl_buffer.get(max_index)[2] + (itpl_buffer.get(max_index + 1)[2] - itpl_buffer.get(max_index)[2]) / 2,
                                        itpl_buffer.get(max_index)[3] + (itpl_buffer.get(max_index + 1)[3] - itpl_buffer.get(max_index)[3]) / 2,
                                        itpl_buffer.get(max_index)[4] + (itpl_buffer.get(max_index + 1)[4] - itpl_buffer.get(max_index)[4]) / 2,
                                        itpl_buffer.get(max_index)[5] + (itpl_buffer.get(max_index + 1)[5] - itpl_buffer.get(max_index)[5]) / 2,
                                        itpl_buffer.get(max_index)[6] + (itpl_buffer.get(max_index + 1)[6] - itpl_buffer.get(max_index)[6]) / 2,
                                        itpl_buffer.get(max_index)[7] + (itpl_buffer.get(max_index + 1)[7] - itpl_buffer.get(max_index)[7]) / 2};
                                itpl_buffer.add(max_index + 1, itpl_temp);
                            }
                        }
                        //Log.d(TAG, "SR in 200ms:" + itpl_buffer.size());
                        for(int j=0;j<itpl_buffer.size();j++) {
                            itpled_buffer.add(Arrays.copyOf(itpl_buffer.get(j), 7));
                        }
                        start_time = itpl_buffer.get(itpl_buffer.size()-1)[7];
                        itpl_buffer.clear();
                    }
                }
                local_buffer.clear();


                if(itpled_buffer.size() > windowSize) {
                    int prepared_windows = ((itpled_buffer.size() - windowSize) / windowStep) + 1;
                    for(int i=0;i<prepared_windows;i++) {
                        double[][] window = new double[windowSize][7];
                        for(int j=0;j<windowSize;j++) {
                            window[j] = itpled_buffer.get(j+i*windowStep);
                        }
                        //Log.d(TAG, "New Window:" + Arrays.deepToString(window));
                        int predict = classifer.classify(window);
                        blockingQueue_predict.offer(predict);
                    }
                    itpled_buffer.subList(0, windowStep * prepared_windows).clear();
                }

                //-------------------------------------------------------------------------------------

            } else if(msg.what == 2) {

                int n2 = blockingQueue_predict.size();
                for(int i=0;i<n2-1;i++) {
                    int temp = blockingQueue_predict.poll();
                    if (predict_list.size() != 0) {
                        int last = predict_list.get(predict_list.size() - 1);
                        int n2ext = blockingQueue_predict.peek();
                        if (n2ext != 0 && last == n2ext) {
                            temp = last;
                        }
                    }
                    predict_list.add(temp);
                }
                ArrayList<Integer> zero_list = new ArrayList<>();
                for(int i=0;i<predict_list.size();i++) {
                    if(predict_list.get(i) == 0) {
                        zero_list.add(i);
                    }
                }
                if(zero_list.size() != 0) {
                    if(zero_list.size() == 1) {
                        if(zero_list.get(0) >= 6) {
                            String type = getType(0, zero_list.get(0));
                            recogTextView.setText(type);
                            do_Act(type);
                        }
                    } else {
                        if(zero_list.get(0) >= 6) {
                            String type = getType(0, zero_list.get(0));
                            recogTextView.setText(type);
                            do_Act(type);
                        }
                        for(int i=1;i<zero_list.size();i++) {
                            if(zero_list.get(i) - zero_list.get(i-1) >= 6) {
                                String type = getType(zero_list.get(i-1), zero_list.get(i));
                                recogTextView.setText(type);
                                do_Act(type);
                            }
                        }
                    }
                    if (zero_list.get(zero_list.size() - 1) >= 0) {
                        predict_list.subList(0, zero_list.get(zero_list.size() - 1) + 1).clear();
                    }
                }
            }
        }
    };

    public void do_Act(String type) {
        if (command_State == 1) {
            if (!type.equals("Yaw_Left")) {
                recog_List.add(type);
                mSoundPool.play(rsid[6], 1, 1, 1, 0, 1);
            } else {
                command_State = 0;
                //sharedPrefEditor.putString("command", "0");
                search_and_do(recog_List);
                Log.d(TAG, "Dialing!" + recog_List.toString());
                recog_List.clear();
            }
        }
        /*switch (type) {
            case "Shake_Left":
                mSoundPool.play(rsid[0], 1, 1, 1, 0, 1);
                break;
            case "Shake_Right":
                mSoundPool.play(rsid[1], 1, 1, 1, 0, 1);
                break;
            case "Nod_Up":
                mSoundPool.play(rsid[2], 1, 1, 1, 0, 1);
                break;
            case "Nod_Down":
                mSoundPool.play(rsid[3], 1, 1, 1, 0, 1);
                break;
            case "Yaw_Left":
                mSoundPool.play(rsid[4], 1, 1, 1, 0, 1);
                break;
            case "Yaw_Right":
                mSoundPool.play(rsid[5], 1, 1, 1, 0, 1);
                break;
        }*/
    }

    public void search_and_do(ArrayList<String> recog_List) {
        String phoneNumber = db.searchBook(recog_List);

        if(phoneNumber.equals("NO_FOUND")) {
            Toast.makeText(this, "Gesture not in DialBook.", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    public String getType(int start, int end) {
        int[] counter = {0, 0, 0, 0, 0, 0, 0};
        for(int i=start;i<=end;i++) {
            counter[predict_list.get(i)] += 1;
        }
        int max = -1;
        int biggest = -1;
        for(int i=0;i<7;i++) {
            if(counter[i] > max) {
                max = counter[i];
                biggest = i;
            }
        }
        String rtn = "";
        switch (biggest) {
            case 1:
                rtn = "Shake_Left";
                break;
            case 2:
                rtn = "Shake_Right";
                break;
            case 3:
                rtn = "Nod_Up";
                break;
            case 4:
                rtn = "Nod_Down";
                break;
            case 5:
                rtn = "Yaw_Left";
                break;
            case 6:
                rtn = "Yaw_Right";
                break;
        }
        return rtn;
    }

    public void stopDataCollection(){
        sensorListenerManager.stopDataCollection();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkPermission() {
        int recordResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int locationResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int writeResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int answerResult = ContextCompat.checkSelfPermission(getApplicationContext(), ANSWER_PHONE_CALLS);
        int callResult = ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE);
        int wakeResult = ContextCompat.checkSelfPermission(getApplicationContext(), WAKE_LOCK);

        return locationResult == PackageManager.PERMISSION_GRANTED &&
                writeResult == PackageManager.PERMISSION_GRANTED &&
                recordResult == PackageManager.PERMISSION_GRANTED &&
                answerResult == PackageManager.PERMISSION_GRANTED &&
                callResult == PackageManager.PERMISSION_GRANTED &&
                wakeResult == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,
                WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, ANSWER_PHONE_CALLS, CALL_PHONE, WAKE_LOCK}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean answerAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean callAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean wakeAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && storageAccepted && recordAccepted && answerAccepted && callAccepted && wakeAccepted){
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
                                                            WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, ANSWER_PHONE_CALLS, CALL_PHONE, WAKE_LOCK}, PERMISSION_REQUEST_CODE);
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
