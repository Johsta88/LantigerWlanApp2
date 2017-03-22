package com.example.johsta.lantigerwlanapp2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.codebutler.android_websockets.WebSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class WifiService {

    // Debugging
    private static final String TAG = "WifiService";
    private static final boolean D = true;

    //Member fields
    private Handler mHandler;
    private ConnectThread mConnectThread = null;
    private ConnectedThread mConnectedThread = null;
    private int mState;

    //Connection states
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public WifiService(Handler handler) {
        mHandler = handler;
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

    public synchronized void connect(WebSocketClient client) {//TODO wird an ConnectThread übergeben
        if (D) Log.d(TAG, "connect to: " + client);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(client);
        mConnectThread.start();

        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The WebsocketSocket on which the connection was made
     * @param client  The Websocket Client that has been connected
     */
    public synchronized void connected(WebSocketClient socket, WebSocketClient client) { //TODO ersetzen übergeben des Websockets und der ?SSID?
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        /*bundle.putString(MainActivity.DEVICE_NAME, client.getName());*/ //TODO einen Namen/Identifier bekommen
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {//TODO ersetzen durch WebSocket

        // Debugging
        private static final String TAG = "ConnectThread";
        private static final boolean D = true;

        private final WebSocketClient mmSocket;
        private final WebSocketClient mmDevice;

        public ConnectThread(WebSocketClient device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            WebSocketClient tmp = null;
            mmDevice = device;

            /*tmp = device.connect();*/ //TODO was macht das?
/*            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }*/
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");

            // Cancel discovery because it will slow down the connection
            /*mBluetoothAdapter.cancelDiscovery();*/ //TODO was macht das?

            // Make a connection to the BluetoothSocket
 /*            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                connectionFailed();
               try {
                    mmSocket.disconnect();
                } catch (IOException closeException) {
                    Log.e(TAG, "unable to close() socket during connection failure");
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (WifiService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);*/ //TODO für Wifi ausprogrammieren
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
  /*          try {
                mmSocket.close();
            } catch (IOException e) { }*/ //TODO für Wifi ausprogrammieren
        }
    }

    private class ConnectedThread extends Thread{

        // Debugging
        private static final String TAG = "ConnectedThread";
        private static final boolean D = true;

        private final WebSocketClient mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(WebSocketClient socket) {
            Log.d(TAG, "create ConnectedThread");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
/*            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);//TODO für Wifi ausprogrammieren
            }*/

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public synchronized void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    //Send the obtained bytes to the UI activity
                    byte[] bufCopy = new byte[bytes];
                    for(int i =0; i<bytes; i++)
                        bufCopy[i] = buffer[i];//TODO Jens buffer wurde überschrieben
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, bufCopy).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, bytes).sendToTarget();

            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
 /*           try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }*/ //TODO für Wifi ausprogrammieren
        }
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out); //TODO write von WebSocket
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        setState(STATE_NONE);
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
    }


}

