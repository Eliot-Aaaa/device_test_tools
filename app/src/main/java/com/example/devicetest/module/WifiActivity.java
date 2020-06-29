package com.example.devicetest.module;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.devicetest.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiActivity extends Activity {

    Switch wlan_switch;
    WifiManager mWifiManager;
    boolean isOpen = false;
    boolean isTimerSet = false;
    ListView wifi_scan_list;
    static final int START_SCAN = 0, GET_SCAN=1;
    Timer mtimer;
    TimerTask mtimerTask;
    WifiReceiver mWifiReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wifi);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        isOpen = mWifiManager.isWifiEnabled();
        wifi_scan_list = findViewById(R.id.wifi_scan_list);
        wifi_scan_list.setVisibility(View.VISIBLE);

        wlan_switch = findViewById(R.id.wlan_switch);
        wlan_switch.setChecked(isOpen);
        wlan_switch.setOnCheckedChangeListener(mListener);

        //regist wifi state monitor
        IntentFilter filter = new IntentFilter();
        mWifiReceiver = new WifiReceiver();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, filter);

        //init timer
        mtimer = new Timer();
        mtimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(GET_SCAN);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiReceiver);
    }

    public void setSwitchEnable(boolean enabled)
    {
        if (wlan_switch != null)
            wlan_switch.setEnabled(enabled);
    }

    //Wi-Fi state monitor
    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //wifi state action
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -111);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLING:
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        setSwitchEnable(true);
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        setSwitchEnable(true);
                        mHandler.sendEmptyMessage(START_SCAN);
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        break;
                    default:
                        break;
                }
            }
        }
    }

    CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isOpen = mWifiManager.isWifiEnabled();
            if ( isOpen ^ isChecked)
            {
                mWifiManager.setWifiEnabled(isChecked);
                setSwitchEnable(false);
            }
        }
    };

    Handler mHandler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case START_SCAN:
                    mWifiManager.startScan();
                    if (mtimer != null)
                    {
                        if (!isTimerSet) {
                            mtimer.schedule(mtimerTask, 1000, 500);
                            isTimerSet = true;
                        }
                    }
                    break;
                case GET_SCAN:
                    List<ScanResult> results = mWifiManager.getScanResults();
                    setList(results);
                    break;
                default:
                    break;
            }
        }
    };

    void setList(List<ScanResult> results)
    {
        String[] data = new String[results.size()];
        for (int i=0; i<results.size();i++)
        {
            data[i] = results.get(i).SSID;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
        if (wifi_scan_list == null) {
            wifi_scan_list = findViewById(R.id.wifi_scan_list);
        }
        wifi_scan_list.setAdapter(adapter);
        wifi_scan_list.setVisibility(View.VISIBLE);
    }
}
