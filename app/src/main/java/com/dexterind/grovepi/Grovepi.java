package com.dexterind.grovepi;

import android.util.Log;

import com.dexterind.grovepi.events.SensorEvent;
import com.dexterind.grovepi.events.StatusEvent;
import com.dexterind.grovepi.utils.Statuses;

import java.io.IOException;
import java.util.EventObject;
import java.util.concurrent.CopyOnWriteArrayList;


public final class Grovepi {
    private static final String TAG = "GrovePi";
    private static Grovepi instance;

    private boolean isInit = false;
    private boolean isHalt = false;

    public Board board;
    private final CopyOnWriteArrayList<GrovepiListener> listeners;

    public Grovepi() {
        Log.d(TAG, "Instancing a new GrovePi");

        try {
            board = Board.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        listeners = new CopyOnWriteArrayList<GrovepiListener>();
    }

    public static Grovepi getInstance() {
        if(instance == null) {
            instance = new Grovepi();
        }
        return instance;
    }

    public void init() {
        Log.d(TAG, "Init " + isInit);
        board.init();
        isInit = true;
        StatusEvent statusEvent = new StatusEvent(this, Statuses.INIT);
        fireEvent(statusEvent);
    }

    public void addListener(GrovepiListener listener) {
        Log.d(TAG, "Adding listener");
        listeners.addIfAbsent(listener);
    }

    public void removeListener(GrovepiListener listener) {
        if (listeners != null) {
            Log.d(TAG, "Removing listener");
            listeners.remove(listener);
        }
    }

    protected void fireEvent(EventObject event) {
        int i = 0;
        Log.d(TAG, "Firing event [" + listeners.toArray().length + " listeners]");

        for (GrovepiListener listener : listeners) {
            Log.d(TAG, "listener[" + i + "]");
            Log.d(TAG, event.getClass().toString());

            if (event instanceof StatusEvent) {
                listener.onStatusEvent((StatusEvent) event);
            } else if (event instanceof SensorEvent) {
                listener.onSensorEvent((SensorEvent) event);
            }
            i++;
        }
    }
}
