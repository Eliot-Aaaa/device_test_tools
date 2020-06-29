package com.example.devicetest.module;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.devicetest.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends Activity {
    BluetoothAdapter mBluetoothAdapter;
    Switch bt_switch;
    ProgressBar bt_scan_progress_bar;
    ListView bt_scan_list;
    Context mContext;
    boolean isOpen;
    List<String> device_list;
    BluetoothReceiver mReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_bluetooth);
        bt_switch = findViewById(R.id.bt_switch);
        bt_scan_list = findViewById(R.id.bt_scan_list);
        bt_scan_progress_bar = findViewById(R.id.bt_scan_progress_bar);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = getApplicationContext();
        device_list = new ArrayList<>();
        isOpen = mBluetoothAdapter.isEnabled();

        if (isOpen)
            startScan();

        bt_switch.setChecked(isOpen);
        if (!isOpen)
            bt_scan_progress_bar.setVisibility(View.INVISIBLE);

        if (mBluetoothAdapter == null)
            Toast.makeText(mContext, mContext.getResources().getText(R.string.bluetooth_no_device), Toast.LENGTH_SHORT).show();

        bt_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOpen = mBluetoothAdapter.isEnabled();
                if ( isOpen ^ isChecked )
                {
                    if (isChecked) {
                        mBluetoothAdapter.enable();
                    } else {
                        mBluetoothAdapter.disable();
                    }
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mReceiver = new BluetoothReceiver();
        registerReceiver(mReceiver, filter);

    }

    class BluetoothReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() != null)
                    device_list.add(device.getName());
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                String[] data = new String[device_list.size()];
                for (int i = 0; i < device_list.size(); i++)
                {
                    data[i] = device_list.get(i);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, data);
                bt_scan_progress_bar.setVisibility(View.GONE);
                bt_scan_list.setAdapter(adapter);
            }else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (state == BluetoothAdapter.STATE_ON)
                    startScan();
                else if (state == BluetoothAdapter.STATE_OFF)
                    clearList();
            }
        }
    }

    void startScan()
    {
        if (mBluetoothAdapter != null){
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
            bt_scan_progress_bar.setVisibility(View.VISIBLE);
        }
    }

    void clearList(){
        String[] data = new String[]{};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, data);
        bt_scan_list.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
