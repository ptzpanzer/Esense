package com.lcf.esensedial;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lcf.esensedial.io.esense.esenselib.ESenseConfig;
import com.lcf.esensedial.io.esense.esenselib.ESenseEventListener;


public class EventListenerManager implements ESenseEventListener {

    Context context;
    Handler mHandler;

    public EventListenerManager(Context context, Handler mHandler) {
        this.context = context;
        this.mHandler = mHandler;
    }


    @Override
    public void onBatteryRead(double voltage) {

    }

    @Override
    public void onButtonEventChanged(boolean pressed) {

        if(pressed == true)
        {
            Log.d("Esense", "Button Pressed");
            Message message = new Message();
            message.what = 7;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void onAdvertisementAndConnectionIntervalRead(int minAdvertisementInterval, int maxAdvertisementInterval, int minConnectionInterval, int maxConnectionInterval) {

    }

    @Override
    public void onDeviceNameRead(String deviceName) {

    }

    @Override
    public void onSensorConfigRead(ESenseConfig config) {

    }

    @Override
    public void onAccelerometerOffsetRead(int offsetX, int offsetY, int offsetZ) {

    }
}
