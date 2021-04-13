package com.lcf.esensedial;

import android.content.Context;
import android.util.Log;

import com.lcf.esensedial.io.esense.esenselib.ESenseConfig;
import com.lcf.esensedial.io.esense.esenselib.ESenseEvent;
import com.lcf.esensedial.io.esense.esenselib.ESenseSensorListener;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;


public class SensorListenerManager implements ESenseSensorListener {

    private final String TAG = "SensorListenerManager";

    private double[] accel;
    private double[] gyro;
    private boolean dataCollecting;

    Context context;
    ESenseConfig eSenseConfig;
    String activityName;

    private long avg_count;
    private double[] zero;
    private double[] tG;
    private double[] tG2;
    private double[] left;
    private double[] face;

    private LinkedBlockingQueue<double[]> blockingQueue;

    private double normFactor_acc = 3.3154878759650015;
    private double NormFactor_gyr = 0.13354095623012127;
    private int sampleRate = 50;

    public double[] lst_add(double[] lst1, double[] lst2) {
        double[] rtn = new double[3];
        rtn[0] = lst1[0] + lst2[0];
        rtn[1] = lst1[1] + lst2[1];
        rtn[2] = lst1[2] + lst2[2];
        return rtn;
    }

    public double[] dec_Product(double[] lst, double num) {
        double[] rtn = new double[3];
        rtn[0] = lst[0] * num;
        rtn[1] = lst[1] * num;
        rtn[2] = lst[2] * num;
        return rtn;
    }

    public double[] lst_direction(double[] lst) {
        double scala = Math.sqrt(lst[0]*lst[0] + lst[1]*lst[1] + lst[2]*lst[2]);
        double[] rtn = dec_Product(lst, 1.0 / scala);
        return rtn;
    }

    public double dot_product(double[] lst1, double[] lst2) {
        double rtn = lst1[0]*lst2[0] + lst1[1]*lst2[1] + lst1[2]*lst2[2];
        return rtn;
    }

    public double[] cross_product(double[] lst1, double[] lst2) {
        double[] rtn = new double[3];
        rtn[0] = lst1[1]*lst2[2] - lst1[2]*lst2[1];
        rtn[1] = lst1[2]*lst2[0] - lst1[0]*lst2[2];
        rtn[2] = lst1[0]*lst2[1] - lst1[1]*lst2[0];
        return rtn;
    }

    public SensorListenerManager(Context context, LinkedBlockingQueue<double[]> inBlockingQueue) {
        this.context = context;
        eSenseConfig = new ESenseConfig();
        activityName = "";
        this.blockingQueue = inBlockingQueue;
    }

    /**
     * Called when there is new sensor data available
     *
     * @param evt object containing the sensor samples received
     */
    @Override
    public void onSensorChanged(ESenseEvent evt) {
        //Log.d(TAG, "onSensorChanged()");
        if (dataCollecting) {
            accel = evt.convertAccToG(eSenseConfig);
            gyro = evt.convertGyroToDegPerSecond(eSenseConfig);

            switch (activityName) {
                case "INIT1":
                    avg_count++;
                    zero = lst_add(zero, gyro);
                    break;
                case "INIT2":
                    avg_count++;
                    tG = lst_add(tG, accel);
                    break;
                case "INIT3":
                    avg_count++;
                    tG2 = lst_add(tG2, accel);
                    break;
                case "Work":
                    double[] temp = new double[8];
                    temp[0] = dot_product(accel, tG) / normFactor_acc;
                    temp[1] = dot_product(accel, face) / normFactor_acc;
                    temp[2] = dot_product(accel, left) / normFactor_acc;

                    gyro = lst_add(gyro, dec_Product(zero, -1));

                    double rx = gyro[0] * (1.0 / sampleRate);
                    double ry = gyro[1] * (1.0 / sampleRate);
                    double rz = gyro[2] * (1.0 / sampleRate);
                    double cos = Math.cos(Math.PI * (rx/360));
                    double sin = Math.sin(Math.PI * (rx/360));
                    Quaternion qx = new Quaternion(cos, sin, 0, 0);
                    cos = Math.cos(Math.PI * (ry/360));
                    sin = Math.sin(Math.PI * (ry/360));
                    Quaternion qy = new Quaternion(cos, 0, sin, 0);
                    cos = Math.cos(Math.PI * (rz/360));
                    sin = Math.sin(Math.PI * (rz/360));
                    Quaternion qz = new Quaternion(cos, 0, 0, sin);
                    Quaternion qf = Quaternion.normalize(Quaternion.quan_Mult(Quaternion.quan_Mult(qx,qz), qy));

                    double[] qf_vec = qf.getVec();
                    double deg_half = Math.acos(qf_vec[0]);
                    double sin_deg_half = Math.sin(deg_half);
                    double[] axis = new double[3];
                    axis[0] = qf_vec[1] / sin_deg_half;
                    axis[1] = qf_vec[2] / sin_deg_half;
                    axis[2] = qf_vec[3] / sin_deg_half;
                    double[] new_axis = new double[3];
                    new_axis[0] = dot_product(axis, tG);
                    new_axis[1] = dot_product(axis, face);
                    new_axis[2] = dot_product(axis, left);
                    new_axis = lst_direction(new_axis);

                    if(new_axis[0] < 0) {
                        for(int i=0; i<3; i++) {
                            new_axis[i] *= -1;
                        }
                        deg_half *= -1;
                    }
                    temp[3] = new_axis[0];
                    temp[4] = new_axis[1];
                    temp[5] = new_axis[2];
                    temp[6] = deg_half / NormFactor_gyr;
                    temp[7] = evt.getTimestamp();

                    blockingQueue.offer(temp);
                    break;
            }
        }
    }

    public void startDataCollection(String activity) {
        this.activityName = activity;
        dataCollecting = true;

        switch (activityName) {
            case "INIT1":
                avg_count = 0;
                zero = new double[3];
                Arrays.fill(zero, 0.0);
                break;
            case "INIT2":
                avg_count = 0;
                tG = new double[3];
                Arrays.fill(tG, 0.0);
                break;
            case "INIT3":
                avg_count = 0;
                tG2 = new double[3];
                Arrays.fill(tG2, 0.0);
                break;
            case "Work":
                avg_count = 0;
                break;
        }
    }

    public void stopDataCollection() {
        dataCollecting = false;
        switch (activityName) {
            case "INIT1":
                zero = dec_Product(zero, 1.0 / avg_count);
                Log.d("Esense", "Zero:" + Arrays.toString(zero));
                break;
            case "INIT2":
                tG = dec_Product(tG, 1.0 / avg_count);
                tG = lst_direction(tG);
                Log.d("Esense", "tG:" + Arrays.toString(tG));
                break;
            case "INIT3":
                tG2 = dec_Product(tG2, 1.0 / avg_count);
                tG2 = lst_direction(tG2);
                Log.d("Esense", "tG2:" + Arrays.toString(tG2));
                left = lst_direction(cross_product(tG, tG2));
                Log.d("Esense", "Left:" + Arrays.toString(left));
                face = lst_direction(cross_product(tG, left));
                Log.d("Esense", "Face:" + Arrays.toString(face));
                break;
            case "Work":
                blockingQueue.clear();
                break;
        }
    }
}