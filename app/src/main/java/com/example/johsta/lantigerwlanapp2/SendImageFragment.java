package com.example.johsta.lantigerwlanapp2;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.IOException;

public class SendImageFragment extends Fragment {

    public static final String TAG = "SendImageFragment";
    View view;
    SwitchFragmentListener mCallback;
    OnSendWifiMessageListener mWifiListener;

    private Button captureButton;
    private Button sendButton;
    private Button newButton;

    private Camera mCamera;
    private CameraPreview mPreview;

    private Bitmap currentBitmap;
    private SendThread sendThread = null;
    ProgressDialog progressDialog;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

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
                    + " must implement OnSendBluetoothMessageListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sendimage, container, false);

        mWifiListener.onSendWifiMessage("3");

        if(checkCameraHardware(getActivity())){
            // Create an instance of Camera
            mCamera = getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(getActivity(), mCamera);
            FrameLayout preview = (FrameLayout) view.findViewById(R.id.camera_preview);
            preview.addView(mPreview);

            //Set button listener
            // Add a listener to the Capture button
            captureButton = (Button) view.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            mCamera.takePicture(null, null, mPicture);
                            captureButton.setEnabled(false);
                        }
                    }
            );

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("Bild wird gesendet...");
            progressDialog.setCancelable(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    sendThread.runFlag = false;
                }
            });
            progressDialog.setCanceledOnTouchOutside(false);
            sendButton = (Button) view.findViewById(R.id.button_send);
            sendButton.setEnabled(false);
            sendButton.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            sendThread = new SendThread();
                            sendThread.start();
                            progressDialog.show();
                        }
                    });

            newButton = (Button) view.findViewById(R.id.button_new);
            newButton.setEnabled(false);
            newButton.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View v) {
                            mCamera.startPreview();
                            sendButton.setEnabled(false);
                            newButton.setEnabled(false);
                            captureButton.setEnabled(true);
                        }
                    });

        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            mCamera.setDisplayOrientation(90);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //Bitmap auslesen und verkleinern
            currentBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, 320, 240, false);

            newButton.setEnabled(true);
            sendButton.setEnabled(true);
        }
    };

    public class SendThread extends Thread{
        public boolean runFlag = true;

        public SendThread() {
        }
        @Override
        public void run() {

            int imageWidth = currentBitmap.getWidth();
            int imageHeight = currentBitmap.getHeight();

            progressDialog.setProgress(0);
            progressDialog.setMax(imageWidth*imageHeight);
            progressDialog.setProgressNumberFormat(null);

            for (int i = 0; i < imageWidth; i++) {
                for (int j = 0; j < imageHeight; j++) {
                    int color = currentBitmap.getPixel(i, j);// & 0x00ffffff;
                    color = (((color>>3) & 0x1f) | ((color>>5) & 0x7e0) | ((color>>8) & 0xf800)) & 0xffff;

                    mWifiListener.onSendWifiMessage(Integer.toHexString(color) +"\r\n");

                    progressDialog.incrementProgressBy(1);
                    if(runFlag == false) return;
                    yield();
                }
            }
            progressDialog.dismiss();
        }
    }
}
