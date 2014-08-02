package com.mobilesolutionworks.android.camera;


import android.hardware.Camera;

/**
 * Created by yunarta on 11/6/14.
 */
public class CameraHelpers
{
    public static class SelectedCamera
    {
        private Camera.CameraInfo mInfo;

        private int mCameraNumber;

        public SelectedCamera(Camera.CameraInfo info, int cameraNumber)
        {
            mInfo = info;
            mCameraNumber = cameraNumber;
        }

        public Camera.CameraInfo getInfo()
        {
            return mInfo;
        }

        public int getNumber()
        {
            return mCameraNumber;
        }
    }

    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;

    public static SelectedCamera selectCamera(int facing)
    {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameras = Camera.getNumberOfCameras();
        for (int i = 0; i < cameras; i++)
        {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing)
            {
                return new SelectedCamera(cameraInfo, i);
            }
        }

        return null;
    }
}
