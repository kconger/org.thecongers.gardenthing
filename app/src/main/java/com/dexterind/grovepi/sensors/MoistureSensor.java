package com.dexterind.grovepi.sensors;

import android.util.Log;

import com.dexterind.grovepi.sensors.base.AnalogSensor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MoistureSensor extends AnalogSensor {
    private static final String TAG = "GrovePi";

    public MoistureSensor(int pin) throws IOException, InterruptedException {
        super(pin,3);
    }

    public int read() {
        int moisture = -1;
        try {
            byte[] output = this.readBytes();
            ByteBuffer buffer = ByteBuffer.wrap(output);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            moisture = buffer.getShort();
        } catch (IOException e){
            Log.d(TAG, "Error:" + e.getMessage() );
        }
        return moisture;
    }
}
