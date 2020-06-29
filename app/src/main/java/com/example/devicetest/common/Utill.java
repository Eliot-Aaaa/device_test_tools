package com.example.devicetest.common;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.devicetest.R;
import com.example.devicetest.module.BluetoothActivity;
import com.example.devicetest.module.CameraActivity;
import com.example.devicetest.module.DeviceInfoActivity;
import com.example.devicetest.module.WebPageActivity;
import com.example.devicetest.module.WifiActivity;
import com.example.devicetest.module.audio.AudioActivity;
import com.example.devicetest.module.video.VideoActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Utill {

    public static final String[] REQUEST_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    /******************************************************************************/

    //common function: get device model
    public static String getModel()
    {
        String Model = Build.MODEL;
        return Model;
    }

    public static void turnToSelectModul(Context context, String moduleName)
    {
        //跳转设备信息界面
        if ( moduleName.equals(context.getResources().getString(R.string.title_device_info)) )
        {
            Intent intent = new Intent(context, DeviceInfoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转Wi-Fi测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_wifi)) )
        {
            Intent intent = new Intent(context, WifiActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转蓝牙测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_bluetooth)) )
        {
            Intent intent = new Intent(context, BluetoothActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转音频测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_audio)) )
        {
            Intent intent = new Intent(context, AudioActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转视频测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_video)) )
        {
            Intent intent = new Intent(context, VideoActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转相机测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_camera)) )
        {
            Intent intent = new Intent(context, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        //跳转网页测试界面
        else if ( moduleName.equals(context.getResources().getString(R.string.title_web)) )
        {
            Intent intent = new Intent(context, WebPageActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else
            Toast.makeText(context, "not correct module", Toast.LENGTH_SHORT);
    }

    //通用函数
    //获取文件列表
    public static List<String> getFileList(String path)
    {
        List<String> fileList = new ArrayList<String>();
        File file = new File(path);
        if (!path.substring(path.length()-1, path.length()).equals("/"))
        {
            path = path + "/";
        }
        File[] files = file.listFiles();
        if (file != null && files.length > 0)
        {
            for (int i = 0; i < files.length; i++)
            {
                if (!files[i].isDirectory())
                    fileList.add(path + files[i].getName());
            }
        }
        return fileList;
    }

    public static void checkPermission(Context context, String[] permissions)
    {
        List<String> permissionList = new ArrayList<String>();
        for (String permission : permissions)
        {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED )
                permissionList.add(permission);
        }
        if (permissionList.size() > 0) {
            String[] permissionArray = new String[permissionList.size()];
            for (int i = 0; i < permissionList.size(); i++) {
                permissionArray[i] = permissionList.get(i);
            }
            ActivityCompat.requestPermissions((Activity) context, permissionArray, 0);
        }
    }

    /******************************************************************************/
    //设备信息相关函数
    //获取SD卡大小
    public static long getSDCardAvailSize(Context context) {
        String state = Environment.getExternalStorageState();
        long aaa=0;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
            long totalSeize = blockSize * blockCount;
            aaa = availCount * blockSize;
            Log.e("size", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB");
            Log.e("size", "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 + "KB");
        }
        return aaa;
    }

    //设备信息相关函数： 获取内存信息
    public static void getMemoryInfo(Context context)
    {
        ActivityManager activityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo=new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        Log.v("size", "availMem:"+memInfo.availMem/1024+" kb");
        Log.v("size", "threshold:"+memInfo.threshold/1024+" kb");//low memory threshold
        Log.v("size", "totalMem:"+memInfo.totalMem/1024+" kb");
        Log.v("size", "lowMemory:"+memInfo.lowMemory);  //if current is in low memory
    }
    /******************************************************************************/

    /******************************************************************************/
    //Wi-Fi 相关函数
    //获取wifi列表
    public static List<ScanResult> getWifiList(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanWifiList = wifiManager.getScanResults();
        List<ScanResult> wifiList = new ArrayList<>();
        if (scanWifiList != null && scanWifiList.size() > 0) {
            HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
            for (int i = 0; i < scanWifiList.size(); i++) {
                ScanResult scanResult = scanWifiList.get(i);
                if (!scanResult.SSID.isEmpty()) {
                    String key = scanResult.SSID + " " + scanResult.capabilities;
                    if (!signalStrength.containsKey(key)) {
                        signalStrength.put(key, i);
                        wifiList.add(scanResult);
                    }
                }
            }
        }
        return wifiList;
    }

    /******************************************************************************/

}
