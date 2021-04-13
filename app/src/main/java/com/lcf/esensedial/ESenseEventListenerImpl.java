package com.lcf.esensedial;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lcf.esensedial.io.esense.esenselib.ESenseConfig;
import com.lcf.esensedial.io.esense.esenselib.ESenseEventListener;



//ESenseEventListener的实现类
public class ESenseEventListenerImpl implements ESenseEventListener {
    private MainActivity activity;
    Handler mHandler;

    public ESenseEventListenerImpl(MainActivity activity, Handler mHandler){
        this.activity=activity;
        this.mHandler=mHandler;
    }
    //调用 getBatteryVoltage() 成功的回调
    @Override
    public void onBatteryRead(double voltage) {
    }

    // 当使用registerEventListener()方法监听耳机按钮事件，如果点击了耳机按钮，会回调此方法
    @Override
    public void onButtonEventChanged(boolean pressed) {
        //耳机的按钮点击事件有时传不回来，或者延迟很大,几秒
        if(!pressed) {
            Log.d("Esense", "Message7 sent");
            Message message = new Message();
            message.what = 7;
            mHandler.sendMessage(message);
        }
    }

    // 调用 getAdvertisementAndConnectionInterval() 成功的回调
    @Override
    public void onAdvertisementAndConnectionIntervalRead(int minAdvertisementInterval, int maxAdvertisementInterval, int minConnectionInterval, int maxConnectionInterval) {
    }

    // 调用 getDeviceName() 成功的回调
    @Override
    public void onDeviceNameRead(String deviceName) {
    }

    //
    @Override
    public void onSensorConfigRead(ESenseConfig config) {
    }

    @Override
    public void onAccelerometerOffsetRead(int offsetX, int offsetY, int offsetZ) {
    }
}
