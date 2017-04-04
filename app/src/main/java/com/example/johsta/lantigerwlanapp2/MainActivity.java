package com.example.johsta.lantigerwlanapp2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codebutler.android_websockets.WebSocketClient;

import java.net.URI;

public class MainActivity extends Activity implements SwitchFragmentListener, OnSendWifiMessageListener {

    // Debugging
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    // Message types sent from the WifiService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the WifiService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Layout Views
    private ActionBar mActionBar;

    //Wifi and WebSockets
    private WifiInfo mWifiInfo = null;
    private WebSocketClient mWebSocketClient = null;
    private WifiService mWifiService = null;
    private WifiManager mWifiManager = null; //TODO Ersatz für Bluetoothadapter

/*    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;*/ //TODO muss das implementiert werden

    // constant which is passed to startActivityForResult
    // must be greater than 0
    // system passes it back in your onActivityResult() implementation as
    // requestCode parameter
    private static final int REQUEST_ENABLE_WIFI = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            MainFragment firstFragment = new MainFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }


        mWebSocketClient = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET /*+ URLEncoder.encode(name)*/), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                showToast("Connected to ESP");
            }

/*            *
         * On receiving the message from web socket server
         * */
            @Override
            public void onMessage(String message) {
                Log.d(TAG, String.format("Got string message! %s", message));


            }

            @Override
            public void onMessage(byte[] data) {
                Log.d(TAG, String.format("Got binary message! %s", data));
            }

/*            *
         * Called when the connection is terminated
         * */
            @Override
            public void onDisconnect(int code, String reason) {

                String message = String.format("Disconnected! Code: %d Reason: %s", code, reason);

                showToast(message);

                // clear the session id from shared preferences
                //utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error! : " + error);

                showToast("Error! : " + error);
            }

        }, null);

        mWebSocketClient.connect();
    }

/*    *
         * Method to send message to web socket server
         **/
    private void sendMessageToServer(String message) {
        if (mWebSocketClient != null && mWebSocketClient.isConnected()) {
            mWebSocketClient.send(message);
        }
    }

    private void showToast(final String message) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Stop the Wifi service
        if (mWifiService != null)
            mWifiService.stop();

        if(mWebSocketClient != null & mWebSocketClient.isConnected()){
            mWebSocketClient.disconnect();
        }
        if (D)
            Log.e(TAG, "--- ON DESTROY ---");
    }

    /**
     * Sends a message.
     *
     * @param message
     *          A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if(mWifiService == null) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mWifiService.getState() != WifiService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and send it
            byte[] send = message.getBytes();
            mWebSocketClient.send(send);//TODO Ohne WifiService
            //mWifiService.write(send);

            // // Reset out string buffer to zero and clear the edit text field
            // mOutStringBuffer.setLength(0);
            // mOutEditText.setText(mOutStringBuffer);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

/*        if (id == R.id.action_discover) {
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        }*/ //TODO DeviceListActivity Einbinden???

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mWifiService != null) {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                sendMessage("r"); //Go to default state of the Landtiger
                mWifiService.setHandler(mHandler);
            }
        }
    }

    @Override
    public void switchFragment(String tag) {
        // Create fragment
        Fragment newFragment = null;

        switch(tag) {
            case GPIOFragment.TAG:
                GPIOFragment gpioFragment = new  GPIOFragment();
                if (mWifiService != null)
                    mWifiService.setHandler(gpioFragment.getHandler());
                newFragment = gpioFragment;
                break;
            case ADCFragment.TAG:
                ADCFragment adcFragment = new  ADCFragment();
                if (mWifiService != null)
                    mWifiService.setHandler(adcFragment.getHandler());
                newFragment = adcFragment;
                break;
            case SendImageFragment.TAG:
                newFragment = new SendImageFragment();
                break;
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(tag);

        // Commit the transaction
        transaction.commit();

    }

    @Override
    public void onSendWifiMessage(String msg) {
        sendMessage(msg);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {

            case REQUEST_ENABLE_WIFI:
                // When the request to enable Wifi returns
                if (resultCode == Activity.RESULT_OK) {

                } else {
                    // User did not enable Wifi or an error occured
                    Log.d(TAG, "Wifi not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
/*
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    // If the Service is connected to another device, stop the
                    // connection.
                    if (mWifiService != null) {
                        mWifiService.stop();
                    } else {
                        // Attempt to connect to the device
                        mWifiService = new mWifiService(this, mHandler);
                    }
                    mWifiService.connect(device);
                } //TODO Lösung finden
                break;*/
        }
    }

    // // The Handler that gets information back from the WifiService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);

                    switch (msg.arg1) {
                        case WifiService.STATE_CONNECTED:
                            /*mActionBar.setSubtitle(getString(R.string.title_connected_to) *//*+ mConnectedDeviceName*//*);*/ //Fixme Actionbar
                            // mConversationArrayAdapter.clear();
                            break;
                        case WifiService.STATE_CONNECTING:
                            /*mActionBar.setSubtitle(R.string.title_connecting);*/ //Fixme Actionbar
                            break;
                        case WifiService.STATE_NONE:
                            /*mActionBar.setSubtitle(R.string.title_not_connected);*/ //Fixme Actionbar
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
//         byte[] writeBuf = (byte[]) msg.obj;
                    // // construct a string from the buffer
//         String writeMessage = new String(writeBuf);
//         Log.d(TAG, writeMessage);
                    // mConversationArrayAdapter.add("Me: " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    // mConversationArrayAdapter.add(mConnectedDeviceName+": " +
                    // readMessage);
                    Log.d(TAG, readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
/*                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.title_connected_to) + mConnectedDeviceName, Toast.LENGTH_SHORT).show();*///TODO Lösung finden
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
