package com.mobilesolutionworks.android.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by yunarta on 11/6/14.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CameraController
{

    private BurstCallback mBurstCallback;

    private SaveCallback mSaveCallback;

    private boolean mBurstOn;

    public void startExposureLock()
    {
        Camera.Parameters parameters = getParameters();
        parameters.setAutoExposureLock(false);

        setParameters(parameters);

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // e.printStackTrace();
                }

                Camera.Parameters parameters = getParameters();
                parameters.setAutoExposureLock(true);
//
                setParameters(parameters);
            }
        }.start();
    }

    public void startFocus()
    {
//        mBurstOn = true;
//        Camera.Parameters parameters = getParameters();
//        parameters.setAutoExposureLock(false);
//
//        setParameters(parameters);
        if (mStarted)
        {
            mCamera.autoFocus(new Camera.AutoFocusCallback()
            {

                @Override
                public void onAutoFocus(boolean success, Camera camera)
                {
                    mSaveCallback.onFocus(success);

//                Camera.Parameters parameters = getParameters();
//                parameters.setAutoExposureLock(true);
//
//                setParameters(parameters);
//                Log.d("Xamera", "onAutoFocus = " + success);
                }
            });
        }
//        mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback()
//        {
//            @Override
//            public void onAutoFocusMoving(boolean start, Camera camera)
//            {
//                Log.d("Xamera", "onAutoFocusMoving = " + start);
//            }
//        });
//        mCamera.takePicture(mBurstCallback, null, null, mSaveCallback);
    }

    public void stopBurst()
    {
        mBurstOn = false;
    }

    public static interface OnControllerListener
    {

        void onPictureTaken(byte[] data);

        void onFocus(boolean success);
    }

    protected CameraHelpers.SelectedCamera mSelectedCamera;

    protected Context mContext;

    protected Camera mCamera;

    protected SurfaceView mTarget;

    protected SurfaceHolder mSurfaceHolder;

    protected boolean mSurfaceValid;

    protected boolean mStartRequested;

    protected boolean mStarted;

    protected OnControllerListener mOnControllerListener;

    public CameraController(Context context)
    {
        mContext = context;

        mBurstCallback = new BurstCallback();
        mSaveCallback = new SaveCallback();
    }

    public void setOnControllerListener(OnControllerListener onControllerListener)
    {
        mOnControllerListener = onControllerListener;
    }

    public Camera.Parameters setSelectedCamera(CameraHelpers.SelectedCamera selectedCamera)
    {
        mSelectedCamera = selectedCamera;

        mCamera = Camera.open(mSelectedCamera.getNumber());
        return mCamera.getParameters();
    }

    public void setParameters(Camera.Parameters parameters)
    {
        if (mCamera != null)
        {
            mCamera.setParameters(parameters);
        }
    }

    public Camera.Parameters getParameters()
    {
        if (mCamera != null)
        {
            return mCamera.getParameters();
        }

        return null;
    }

    public void setTarget(SurfaceView target)
    {
        mTarget = target;

        mSurfaceHolder = mTarget.getHolder();

        Log.d("com.mobilesolutionworks.android.camera", "mSurfaceHolder.isCreating() = " + mSurfaceHolder.isCreating());
        if (mSurfaceHolder.isCreating())
        {
            mSurfaceHolder.addCallback(new SurfaceHolderCallback());
        }
        else
        {
            try
            {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mSurfaceValid = true;
            }
            catch (IOException e)
            {
                mSurfaceValid = false;
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        mStartRequested = true;
        if (mSurfaceValid)
        {
            mCamera.startPreview();
            mStarted = true;
        }
    }

    public void pause()
    {
        mStartRequested = false;
        if (mSurfaceValid)
        {
            mCamera.stopPreview();
            mStarted = false;
        }
    }

    public void destroy()
    {
        if (mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    public void takePhoto()
    {
        mCamera.takePicture(null, null, null, mSaveCallback);
    }


    private void _notifyPreviewFailed()
    {

    }

    private void _notifyCameraNotSet()
    {

    }

    public void setDisplayOrientation(int displayOrientation)
    {
        mCamera.setDisplayOrientation(displayOrientation);
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback
    {
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            Log.d("com.mobilesolutionworks.android.camera", "onSurfaceChange " + holder + ", " + holder.isCreating());
            if (mCamera == null)
            {
                _notifyCameraNotSet();
                return;
            }

            try
            {
                mCamera.setPreviewDisplay(holder);
                mSurfaceValid = true;

                if (mStartRequested && !mStarted)
                {
                    mCamera.startPreview();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                mSurfaceValid = false;
                _notifyPreviewFailed();
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d("com.mobilesolutionworks.android.camera", "onSurfaceChange " + holder + ", " + holder.isCreating());
            if (mCamera == null)
            {
                _notifyCameraNotSet();
                return;
            }

            try
            {
                mCamera.setPreviewDisplay(null);
                mSurfaceValid = false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                mSurfaceValid = false;
                _notifyPreviewFailed();
            }

        }
    }

    private class BurstCallback implements Camera.ShutterCallback
    {
        @Override
        public void onShutter()
        {
            if (mBurstOn)
            {
                synchronized (mCamera)
                {
                    mCamera.takePicture(new BurstCallback(), null, null, new SaveCallback());
                }
            }
        }
    }

    private class SaveCallback implements Camera.PictureCallback
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            if (mOnControllerListener != null)
            {
                mOnControllerListener.onPictureTaken(data);
            }
        }

        public void onFocus(boolean success)
        {
            if (success)
            {
                if (mOnControllerListener != null)
                {
                    mOnControllerListener.onFocus(success);
                }
            }
        }
    }
}
