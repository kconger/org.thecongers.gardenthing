package com.dexterind.grovepi;

import android.util.Log;

import com.dexterind.grovepi.utils.Commands;
import com.dexterind.grovepi.utils.Statuses;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;

public class Board {

    private static final String TAG = "GrovePi";
    private static Board instance = null;
    private I2cDevice mDevice;

    // I2C Device Name
    private static final String I2C_DEVICE_NAME = "I2C1";
    public static final byte PIN_MODE_OUTPUT = 1;
    public static final byte PIN_MODE_INPUT = 0;
    private static final byte ADDRESS = 0x04;

    public Board() throws IOException, InterruptedException {

        final PeripheralManagerService manager;
        manager = new PeripheralManagerService();

        try {
            mDevice = manager.openI2cDevice(I2C_DEVICE_NAME, ADDRESS);
        } catch (IOException e) {
            Log.w(TAG, "Unable to access device", e);
        }
    }

    public static Board getInstance() throws IOException, InterruptedException {
        if(instance == null) {
            instance = new Board();
        }
        return instance;
    }

    public int writeI2c(int... bytes) throws IOException {
        // Convert array: int[] to byte[]
        final ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        for (int i = 0, len = bytes.length; i < len; i++) {
            byteBuffer.put((byte) bytes[i]);
        }
        sleep(100);
        mDevice.writeRegBuffer(0xfe, byteBuffer.array(), byteBuffer.limit());
        //device.write(0xfe, byteBuffer.array(), 0, byteBuffer.limit());
        return Statuses.OK;
    }

    public byte[] readI2c(int numberOfBytes) throws IOException {
        byte[] buffer = new byte[numberOfBytes];
        mDevice.readRegBuffer(1, buffer, buffer.length);
        //device.read(1, buffer, 0, buffer.length);
        return buffer;
    }

    public int setPinMode(int pin, int pinMode) throws IOException {
        return writeI2c(Commands.PMODE, pin, pinMode, Commands.UNUSED);
    }

    public void sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public void init() {
        try {
            mDevice.writeRegByte(0xfe, (byte)0x04);
            //device.write(0xfe, (byte)0x04);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            Log.d(TAG,exceptionDetails);
        }
    }

    public String version() throws IOException {
        writeI2c(Commands.VERSION, Commands.UNUSED, Commands.UNUSED, Commands.UNUSED);
        sleep(100);

        byte[] b = readI2c(4);
        readI2c(1);

        return String.format("%s.%s.%s", (int)b[1], (int)b[2], (int)b[3]);
    }

}
