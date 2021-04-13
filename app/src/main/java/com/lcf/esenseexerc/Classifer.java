package com.lcf.esenseexerc;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * This image classifer classifies each drawing as one of the 10 digits
 */
public class Classifer {

    private static final String TAG = "Classifer";

    // Name of the model file (under assets folder)
    private static final String MODEL_PATH = "keras_tflite";

    // TensorFlow Lite interpreter for running inference with the tflite model
    private final Interpreter interpreter;

    /* Input */
    // Input size
    private static final int DIM_BATCH_SIZE = 1;    // batch size
    private static final int DIM_STEP_SIZE = 60;    // step size
    public static final int DIM_DATA_SIZE = 7;   // data size
    private static final int DIM_CHANNEL_SIZE = 1;    // channel size

    // A ByteBuffer to hold image data for input to model
    private final ByteBuffer inputBuffer;
    private final double[][] data = new double[DIM_STEP_SIZE][DIM_DATA_SIZE];

    /* Output*/
    // Output size is 7
    private static final int DIGITS = 7;

    // Output array [batch_size, number of digits]
    // 7 floats, each corresponds to the probability of each digit
    private float[][] outputArray = new float[DIM_BATCH_SIZE][DIGITS];

    public Classifer(Activity activity) throws IOException {
        interpreter = new Interpreter(loadModelFile(activity));
        inputBuffer =
                ByteBuffer.allocateDirect(4
                        * DIM_BATCH_SIZE
                        * DIM_STEP_SIZE
                        * DIM_DATA_SIZE
                        * DIM_CHANNEL_SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());
    }

    // Memory-map the model file in Assets
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * To classify, follow these steps:
     * 1. pre-process the input data
     * 2. run inference with the model
     * 3. post-process the output result for display in UI
     *
     * @return the digit with the highest probability
     */
    public int classify(double[][] data) {
        preprocess(data);
        runInference();
        return postprocess();
    }

    /**
     * Preprocess the data to tflite input
     *
     */
    private void preprocess(double[][] data) {
        inputBuffer.rewind();
        for(int i=0;i<DIM_STEP_SIZE;i++) {
            for(int j=0;j<DIM_DATA_SIZE;j++) {
                inputBuffer.putFloat((float)data[i][j]);
            }
        }
    }

    /**
     * Run inference with the classifer model
     * Input is preprocessed data
     * Output is an array of probabilities
     */
    private void runInference() {
        interpreter.run(inputBuffer, outputArray);
    }

    /**
     * Figure out the prediction of digit by finding the index with the highest probability
     *
     * @return
     */
    private int postprocess() {
        // Index with highest probability
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < outputArray[0].length; i++) {
            if (outputArray[0][i] > maxProb) {
                maxProb = outputArray[0][i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
