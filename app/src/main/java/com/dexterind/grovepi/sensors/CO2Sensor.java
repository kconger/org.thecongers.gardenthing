package com.dexterind.grovepi.sensors;

import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;

public class CO2Sensor {
    // UART Device Name
    private static final String UART_DEVICE_NAME = "UART0";
    private UartDevice mDevice;

    private static final String TAG = "GrovePi";

    public CO2Sensor() throws IOException, InterruptedException {
        super();
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            mDevice = manager.openUartDevice(UART_DEVICE_NAME);
            // Configure the UART port
            mDevice.setBaudrate(9600);
            mDevice.setDataSize(8);
            mDevice.setParity(UartDevice.PARITY_NONE);
            mDevice.setStopBits(1);

            // TODO: Actually get the CO2 sensor working
            String command = "FF0186000000000079";
            byte[] bcommand = HexStringToByteArray(command);

            int count = mDevice.write(bcommand, bcommand.length);
            Log.d(TAG, "Wrote " + count + " bytes to peripheral");

        } catch (IOException e) {
            Log.w(TAG, "Unable to access UART device", e);
        }
    }

    public int read() throws IOException, InterruptedException {
        int ppm = -1;
        // Maximum amount of data to read at one time
        byte[] buffer = new byte[512];

        int count;
        while ((count = mDevice.read(buffer, buffer.length)) > 0) {
            Log.d(TAG, "Read " + count + " bytes from peripheral");

        }

        return ppm;
    }

    public void close() {
        try {
            mDevice.close();
        } catch (IOException e) {
            Log.w(TAG, "Unable to close UART device", e);
        }
    }

    public static byte[] HexStringToByteArray(String s) {
        byte data[] = new byte[s.length()/2];
        for(int i=0;i < s.length();i+=2) {
            data[i/2] = (Integer.decode("0x"+s.charAt(i)+s.charAt(i+1))).byteValue();
        }
        return data;
    }

}
