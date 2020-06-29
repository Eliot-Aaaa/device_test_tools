package com.example.devicetest.common;

import android.content.Context;
import android.os.Build;

import com.example.devicetest.R;

public class Config {

    public String getDeviceModel()
    {
        String DeviceModel = Build.MODEL;
        return DeviceModel;
    }

    public String[] getConfig(Context context)
    {
        String[] config;
        String DeviceModel = getDeviceModel();
        switch (DeviceModel)
        {
            case "EVB_8MM":
                config = new String[]{context.getResources().getString(R.string.title_device_info),
                        context.getResources().getString(R.string.title_wifi),
                        context.getResources().getString(R.string.title_bluetooth),
                        context.getResources().getString(R.string.title_audio),
                        context.getResources().getString(R.string.title_video),
                        context.getResources().getString(R.string.title_camera),
                        context.getResources().getString(R.string.title_web)};
                break;
            default:
                config = new String[]{"Device Info"};
        }
        return config;
    }
}
