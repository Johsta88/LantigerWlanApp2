package com.example.johsta.lantigerwlanapp2;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import java.net.URI;


public class WifiService {

    // Debugging
    private static final String TAG = "WifiService";
    private static final boolean D = true;

    //Member fields
    private Handler mHandler;
    private WebSocketClient mClient;
    private int mState;

/*    private ConnectedThread mConnectedThread = null;*/

    //Connection states
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    public WifiService(Context context, Handler handler) {
        mHandler = handler;
        mState = STATE_NONE;

    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public int getState() {
        return mState;
    }

    /**
     * Connect to an WebSocket
     */
    public void connect() {
        if (D) Log.d(TAG, "connect");

        mClient = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                // Send the name of the connected device back to the UI Activity
                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.TOAST, "onConnect WifiService");
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                setState(STATE_CONNECTED);
            }

            /**
             * On receiving the message from web socket server
             **/
            @Override
            public void onMessage(String message) {
                Log.d(TAG, String.format("Got string message! %s", message));

                byte[] data = message.getBytes();
                mHandler.obtainMessage(MainActivity.MESSAGE_READ, data).sendToTarget();
            }

            @Override
            public void onMessage(byte[] data) {
                Log.d(TAG, String.format("Got binary message! %s", data));

                mHandler.obtainMessage(MainActivity.MESSAGE_READ, data).sendToTarget();
            }

            /*            *
                     * Called when the connection is terminated
                     * */
            @Override
            public void onDisconnect(int code, String reason) {

                // Send a failure message back to the Activity
                setState(STATE_NONE);
                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.TOAST, "onDisconnect WifiService");
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                // clear the session id from shared preferences
                //utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error! : " + error);

                // Send a failure message back to the Activity
                setState(STATE_NONE);
                Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString(MainActivity.TOAST, "onError WifiService");
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }

        }, null);

        mClient.connect();

        setState(STATE_CONNECTED);
    }

    /**
     * Method to send message to web socket server
     */
    public void sendMessageToServer(String message) {
        if (mClient != null && mClient.isConnected()) {
            mClient.send(message);

            byte[] bytes = message.getBytes();
            mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, bytes).sendToTarget();
        }
    }

    public void sendMessageToServer(byte[] message) {
        if (mClient != null && mClient.isConnected()) {
            mClient.send(message.toString());

            mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, message).sendToTarget();
        }
    }
}