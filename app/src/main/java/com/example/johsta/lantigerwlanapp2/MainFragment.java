package com.example.johsta.lantigerwlanapp2;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MainFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "MainFragment";
    View view;
    SwitchFragmentListener mCallback;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        Button gpioButton = (Button) view.findViewById(R.id.button_gpio);
        gpioButton.setOnClickListener(this);

        Button adcButton = (Button) view.findViewById(R.id.button_adc);
        adcButton.setOnClickListener(this);

        Button sendImageButton = (Button) view.findViewById(R.id.button_sendImage);
        sendImageButton.setOnClickListener(this);

        Button espButton = (Button) view.findViewById(R.id.button_esp);
        espButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_gpio:
                //switch fragment
                mCallback.switchFragment(GPIOFragment.TAG);
                break;
            case R.id.button_adc:
                //switch fragment
                mCallback.switchFragment(ADCFragment.TAG);
                break;
            case R.id.button_sendImage:
                //switch fragment
                mCallback.switchFragment(SendImageFragment.TAG);
                break;
            case R.id.button_esp:
                //switch fragment
                mCallback.switchFragment(ESPFragment.TAG);
                break;
        }

    }
}
