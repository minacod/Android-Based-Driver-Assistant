package com.example.android.captureframes;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by shafy on 23/06/2018.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera ,SurfaceHolder holder ) {
        super(context);
        mCamera = camera;
        mHolder = holder;
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // create the surface and start camera preview
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager)getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        if(display.getRotation() == Surface.ROTATION_0) {
            mCamera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90) {
            mCamera.setDisplayOrientation(0);
        }

        if(display.getRotation() == Surface.ROTATION_180) {
            mCamera.setDisplayOrientation(270);
        }

        if(display.getRotation() == Surface.ROTATION_270) {
            mCamera.setDisplayOrientation(180);
        }

        mCamera.setParameters(parameters);
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
     }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // mCamera.release();
     }
}
