package com.example.devicetest.module;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.devicetest.R;
import com.example.devicetest.common.Utill;

public class DeviceInfoActivity extends Activity {
    TextView device_information;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_device_info);
        device_information = findViewById(R.id.device_information);
        Utill.getSDCardAvailSize(getApplicationContext());
        device_information.setText(getResources().getString(R.string.device_info_model) + ":" + Utill.getModel() + "\n");
    }
}
