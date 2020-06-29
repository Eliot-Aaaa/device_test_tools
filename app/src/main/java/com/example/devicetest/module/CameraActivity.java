package com.example.devicetest.module;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.ScrollingMovementMethod;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.devicetest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraActivity extends Activity {

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private int mWidth;
    private int mHeight;
    private List<CameraInfo> infoList;
    private boolean isOpen = false;

    private TextureView mTextureView;
    private ImageButton mOpenBtn;
    private TextView camera_num, camera_info, info_desc;

    private final int REQUEST_CAMERA_PERMISSION = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_camera);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mTextureView = findViewById(R.id.camera_preview);
        mOpenBtn = findViewById(R.id.open_camera);
        camera_num = findViewById(R.id.camera_num);
        camera_info = findViewById(R.id.camera_info);
        info_desc = findViewById(R.id.info_desc);

        infoList = getCameraInfo(mCameraManager);

        mOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraIdDialog(CameraActivity.this);
            }
        });

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCameraInfoText();
        mTextureView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTextureView.setVisibility(View.GONE);
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
            mCameraDevice=null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice=null;
        }
    };

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }
    };

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_PERMISSION);
        }

        try {
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
            startBackgroundThread();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    public void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mWidth, mHeight);
            Surface surface = new Surface(texture);
            mBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) {
                        return;
                    }
                    mSession = cameraCaptureSession;
                    mBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        mSession.setRepeatingRequest(mBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Camera configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    List<CameraInfo> getCameraInfo(CameraManager cameraManager)
    {
        List<CameraInfo> infoList = new ArrayList<CameraInfo>();
        try {
            int cameraNum = cameraManager.getCameraIdList().length;
            for (int i = 0; i < cameraNum; i++)
            {
                CameraInfo info = new CameraInfo();
                info.setIndex(i);
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(String.valueOf(i));
                Size[] pictureSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                Size[] previewSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
                info.setPictureSizes(pictureSizes);
                info.setPreviewSizes(previewSizes);
                infoList.add(info);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return infoList;
    }

    void setCameraInfoText()
    {
        //设置文本框显示
        String str_camera_num = getResources().getText(R.string.camera_num) + " : " + infoList.size();
        String str_camera_info = "";
        camera_num.setText(str_camera_num);
        info_desc.setText(getResources().getText(R.string.camera_info) + " :");

        for (int i = 0; i < infoList.size(); i++)
        {
            str_camera_info += getResources().getText(R.string.camera_index) + " : " + i + "\n";
            str_camera_info += getResources().getText(R.string.camera_support_preview_size) + " :\n";
            Size[] previewSizes = infoList.get(i).getPreviewSizes();
            for (int j = 0; j < previewSizes.length; j++)
            {
                str_camera_info += getResources().getText(R.string.camera_size_title) + " :\t" + previewSizes[j].getWidth() + " * " + previewSizes[j].getHeight() + "\n";
            }
            str_camera_info += getResources().getText(R.string.camera_support_picture_size) + " :\n";
            Size[] pictureSizes = infoList.get(i).getPictureSizes();
            for (int j = 0; j < pictureSizes.length; j++)
            {
                str_camera_info += getResources().getText(R.string.camera_size_title) + " :\t" + pictureSizes[j].getWidth() + " * " + pictureSizes[j].getHeight() + "\n";
            }
            if (infoList.size() != i + 1)
                str_camera_info += "\n";
        }
        camera_info.setText(str_camera_info);

        camera_info.bringToFront();
        camera_info.setMovementMethod(ScrollingMovementMethod.getInstance());
        camera_num.bringToFront();
        info_desc.bringToFront();
        mOpenBtn.bringToFront();
    }

    void showCameraIdDialog(final Context context)
    {
        int length = infoList.size();
        String[] cameraIds = new String[length];
        for (int i = 0; i < length; i++)
        {
            cameraIds[i] = String.valueOf(i);
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(getResources().getText(R.string.camera_index_select_title))
                .setItems(cameraIds, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCameraId = String.valueOf(which);
                        showCameraSizeDialog(context);
                    }
                }).create();
        dialog.show();
    }

    void showCameraSizeDialog(Context context)
    {
        final Size[] previewSizes = infoList.get(Integer.valueOf(mCameraId).intValue()).getPreviewSizes();
        int length = previewSizes.length;
        String[] previewSizeStr = new String[length];
        for (int i = 0; i < length; i ++)
        {
            previewSizeStr[i] = previewSizes[i].getWidth() + " * " + previewSizes[i].getHeight();
        }
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(getResources().getText(R.string.camera_size_select_title))
                .setItems(previewSizeStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWidth = previewSizes[which].getWidth();
                        mHeight = previewSizes[which].getHeight();
                        openCamera();
                        mOpenBtn.setVisibility(View.GONE);
                    }
                }).create();
        dialog.show();
    }

}


class CameraInfo
{
    int index;
    Size[] previewSizes;
    Size[] pictureSizes;

    int getIndex()
    {
        return index;
    }

    void setIndex(int i)
    {
        index = i;
    }

    Size[] getPreviewSizes()
    {
        return previewSizes;
    }

    void setPreviewSizes(Size[] s)
    {
        previewSizes = s;
    }

    Size[] getPictureSizes()
    {
        return pictureSizes;
    }

    void setPictureSizes(Size[] s)
    {
        pictureSizes = s;
    }
}