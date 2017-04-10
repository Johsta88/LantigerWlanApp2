package com.example.johsta.lantigerwlanapp2;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class ESPFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ESPFragment";
    View view;
    SwitchFragmentListener mCallback;
    OnSendWifiMessageListener mWifiListener;

    private ToggleButton ledButtons [];
    private ImageView ledViews[];

    private String receivedMessage = "";

    private static final int LOW = 0;
    private static final int HIGH = 1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (SwitchFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SwitchFragmentListener");
        }

        try {
            mWifiListener = (OnSendWifiMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSendWifiMessageListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_esp, container, false);

        mWifiListener.onSendWifiMessage("ESP 4");

        //Set button listener
        ledButtons = new ToggleButton[3];

        ledButtons[0] = (ToggleButton) view.findViewById(R.id.button_redPin);
        ledButtons[1] = (ToggleButton) view.findViewById(R.id.button_bluePin);
        ledButtons[2] = (ToggleButton) view.findViewById(R.id.button_greenPin);

        for (int i = 0; i < ledButtons.length; i++) {
            ledButtons[i].setOnClickListener(this);
        }

        ledViews = new ImageView[3];

        ledViews[0] = (ImageView) view.findViewById(R.id.ld1_imageView);
        ledViews[1] = (ImageView) view.findViewById(R.id.ld2_imageView);
        ledViews[2] = (ImageView) view.findViewById(R.id.ld3_imageView);

        return view;
    }

    @Override
    public void onClick(View v) {

        int ledValue = 0;
        for (int i=0; i < ledButtons.length; i++) {
            if(ledButtons[i].isChecked())
                ledValue = (int) Math.pow(2,i);
            else
                ledValue &= (int) (0x7-Math.pow(2,i));
        }


        mWifiListener.onSendWifiMessage("ESP LED " + Integer.toBinaryString(ledValue) + "\r\n");
    }

    private void setLedState(String msg) {

        receivedMessage += msg;
        if(!receivedMessage.endsWith("\n"))
            return;

        int ledState = 0;
        try {
            receivedMessage = receivedMessage.replace("\n", "");
            ledState = Integer.parseInt(receivedMessage);
        } catch (NumberFormatException e) {
            Log.d(TAG, e.getMessage());
        }
        receivedMessage="";

        //Leds anzeigen
        for (int i=0; i < ledButtons.length; i++) {
            int currentLedState = (ledState>>i) & 0x1;
            if(currentLedState == HIGH) {
                ledButtons[i].setChecked(true);
                ledViews[i].setImageDrawable(getResources().getDrawable(R.drawable.led_on));
            }
            else {
                ledButtons[i].setChecked(false);
                ledViews[i].setImageDrawable(getResources().getDrawable(R.drawable.led_off));
            }
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    // The Handler that gets information back from the WifiService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case WifiService.STATE_CONNECTED:
                            break;
                        case WifiService.STATE_CONNECTING:
                            break;
                        case WifiService.STATE_NONE:
                            break;
                    }
                    break;
                case MainActivity.MESSAGE_WRITE:
//         byte[] writeBuf = (byte[]) msg.obj;
                    // // construct a string from the buffer
//         String writeMessage = new String(writeBuf);
//         Log.d(TAG, writeMessage);
                    // mConversationArrayAdapter.add("Me: " + writeMessage);
                    break;
                case MainActivity.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    String readMessage = new String(readBuf);

                    setLedState(readMessage);
                    break;
                case MainActivity.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
//        mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//        Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MainActivity.MESSAGE_TOAST:
//        Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
