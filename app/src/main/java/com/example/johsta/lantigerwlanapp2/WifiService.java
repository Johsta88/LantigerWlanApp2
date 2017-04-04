package com.example.johsta.lantigerwlanapp2;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;


public class WifiService {

    // Debugging
    private static final String TAG = "WifiService";
    private static final boolean D = true;

    //Member fields
    private Handler mHandler;
    private WebSocketClient mClient;
    private int mState;


    //Connection states
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public WifiService(Context context, Handler handler, WebSocketClient client) {
        mHandler = handler;
        mClient = client;
        mState = STATE_NONE;


    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Connect to an WebSocket
     */
    public  synchronized void connect(){

    }

    /**
     * Stop Service
     */
    public synchronized void stop() {

    }

    /**
     * Write to an WebSocket
     */
    public void write(byte[] bytes){

    }

    /**
     * Read from an WebSocket
     */
    public void read(){

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {

    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {

    }
}

