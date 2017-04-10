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
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;

public class ADCFragment extends Fragment {


    public static final String TAG = "ADCFragment";
    View view;

    private String receivedMessage;
    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    private int maxXVal = 50;
    private int maxYVal = 3300;
    private double graphLastXValue = 0;

    private TextView currentValView;
    private TextView meanValView;

    OnSendWifiMessageListener wifiMessageListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            wifiMessageListener = (OnSendWifiMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSendWifiMessageListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_adc, container, false);

        wifiMessageListener.onSendWifiMessage("LTB 2");

        //Set button listener
//    Button buttonP2_0 = (Button) view.findViewById(R.id.button_p2_0);
//    buttonP2_0.setOnClickListener(this);

        graph = (GraphView) view.findViewById(R.id.graphADC);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
//    viewport.setMinX(0);
        viewport.setMaxX(maxXVal);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(maxYVal);
//    viewport.setScrollable(true);

        GridLabelRenderer renderer = graph.getGridLabelRenderer();
        renderer.setHorizontalLabelsVisible(false);
        renderer.setNumVerticalLabels(10);
//    renderer.setVerticalAxisTitle("U/mV");
//    renderer.setLabelVerticalWidth(45);
//    renderer.setPadding(35);

        currentValView = (TextView) view.findViewById(R.id.currentValueADC);
        meanValView = (TextView) view.findViewById(R.id.meanValueADC);

        return view;
    }

    private void appendDataToGraph(String readMessage) {

        receivedMessage += readMessage;
        if(!receivedMessage.endsWith("\n"))
            return;

        int value = 0;
        try {
            receivedMessage = receivedMessage.replace("\n", "");
            value = Integer.parseInt(receivedMessage);
        } catch (NumberFormatException e) {
            Log.d(TAG, e.getMessage());
        }
        receivedMessage = "";

        series.appendData(new DataPoint(++graphLastXValue,value), true, maxXVal+1);


        currentValView.setText(getString(R.string.currentValue) + String.valueOf(value) + "mV");


        int meanValue=0;
        double xRange = graph.getViewport().getMaxX(true) - graph.getViewport().getMinX(true);
        Iterator<DataPoint> it = series.getValues(graphLastXValue-xRange, graphLastXValue);
        xRange=0;
        while(it.hasNext()) {
            meanValue += it.next().getY();
            xRange++;
        }
        meanValue/=xRange;

        meanValView.setText(getString(R.string.meanValue) + String.valueOf(meanValue) + "mV");

    }

    public Handler getHandler() {
        return mHandler;
    }

    // // The Handler that gets information back from the BluetoothService
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
                    //String readMessage = readBuf.toString();
                    String readMessage = new String(readBuf);
                    appendDataToGraph(readMessage);
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
