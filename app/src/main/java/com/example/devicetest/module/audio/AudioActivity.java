package com.example.devicetest.module.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.devicetest.R;
import com.example.devicetest.common.Utill;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioActivity extends Activity {
    private AudioManager mAudioManager;
    private MyServiceConn conn;
    private Intent mServiceIntent;
    private AudioPlayer mAudioPlayer;

    //音频播放界面控件
    private static SeekBar playingSeekBar;
    private ImageButton button_volume_add, button_volume_del, button_rew, button_ff, button_play_pause;
    private ListView audio_file_list;
    private TabHost tabHost;

    //音频播放部分参数
    private int maxVolume;
    private String[] mFileList;
    private boolean isChoosed = false;

    //音频录制界面控件
    private TextView recordTimeTextView;
    private ImageButton btnStartRecord, btnStopRecord;

    private Timer mRecordTimeTimer;
    int count = 0;

    private AudioRecorder mAudioRecorder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_audio);

        //初始化音量调节部分参数
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //绑定service
        mServiceIntent = new Intent(this, AudioService.class);
        startService(mServiceIntent);
        conn = new MyServiceConn();
        bindService(mServiceIntent, conn, BIND_AUTO_CREATE);

        mAudioRecorder = new AudioRecorder();

        //初始化UI
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mServiceIntent);
        unbindService(conn);

        if (mRecordTimeTimer != null)
        {
            mRecordTimeTimer.cancel();
            mRecordTimeTimer = null;
        }
    }

    private void initView()
    {
        //设置tabHost
        tabHost = findViewById(R.id.audio_tabhost);
        tabHost.setup();

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.tab_play, tabHost.getTabContentView());
        inflater.inflate(R.layout.tab_record, tabHost.getTabContentView());

        tabHost.addTab(tabHost.newTabSpec(getApplicationContext().getResources().getString(R.string.audio_play_tag)).setIndicator(getApplicationContext().getResources().getString(R.string.audio_play_title)).setContent(R.id.tab_play));
        tabHost.addTab(tabHost.newTabSpec(getApplicationContext().getResources().getString(R.string.audio_record_tag)).setIndicator(getApplicationContext().getResources().getString(R.string.audio_record_title)).setContent(R.id.tab_record));

        //设置音频播放的UI
        playingSeekBar = findViewById(R.id.play_seek_bar);
        button_rew = findViewById(R.id.button_rew);
        button_ff = findViewById(R.id.button_ff);
        button_volume_add = findViewById(R.id.button_volume_add);
        button_volume_del = findViewById(R.id.button_volume_del);
        button_play_pause = findViewById(R.id.button_play_pause);
        audio_file_list = findViewById(R.id.audio_file_list);

        playingSeekBar.setProgress(0);
        playingSeekBar.setEnabled(isChoosed);
        playingSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangerListener);

        button_play_pause.setOnClickListener(mOnClickListener);
        button_volume_add.setOnClickListener(mOnClickListener);
        button_volume_del.setOnClickListener(mOnClickListener);
        button_rew.setOnClickListener(mOnClickListener);
        button_ff.setOnClickListener(mOnClickListener);

        button_play_pause.setEnabled(isChoosed);
        button_play_pause.setClickable(isChoosed);
        button_volume_del.setFocusable(true);

        //设置可播放音频文件列表
        setFileList();

        //设置音频录制的UI
        btnStartRecord = findViewById(R.id.button_start_record);
        btnStopRecord = findViewById(R.id.button_stop_record);
        recordTimeTextView = findViewById(R.id.record_time_text);

        btnStartRecord.setOnClickListener(mOnClickListener);
        btnStopRecord.setOnClickListener(mOnClickListener);
    }

    //设置seekBar拖动事件监听器
    SeekBar.OnSeekBarChangeListener mOnSeekBarChangerListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mAudioPlayer.seekTo(seekBar.getProgress());
        }
    };

    //设置按钮点击事件监听器
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int currentVolume;
            int currentPosition;
            switch (v.getId())
            {
                case R.id.button_volume_del:
                    currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVolume > 0)
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
                    break;
                case R.id.button_volume_add:
                    currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (currentVolume < maxVolume)
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
                    break;
                case R.id.button_play_pause:
                    if (!mAudioPlayer.isPlaying())
                    {
                        mAudioPlayer.continuePlay();
                    }
                    else
                    {
                        mAudioPlayer.pause();
                    }
                    break;
                case R.id.button_rew:
                    if (mAudioPlayer.isPlaying()) {
                        currentPosition = mAudioPlayer.getCurrentPosition();
                        if (currentPosition - 5 * 1000 > 0)
                            mAudioPlayer.seekTo(currentPosition - 5 * 1000);
                        else
                            mAudioPlayer.seekTo(0);
                    }
                    break;
                case R.id.button_ff:
                    if (mAudioPlayer.isPlaying()) {
                        currentPosition = mAudioPlayer.getCurrentPosition();
                        if (currentPosition + 5 * 1000 < mAudioPlayer.getDuration())
                            mAudioPlayer.seekTo(currentPosition + 5 * 1000);
                        else
                            mAudioPlayer.seekTo(mAudioPlayer.getDuration());
                    }
                    break;
                case R.id.button_start_record:
                    mAudioRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, ".amr", AudioActivity.this);
                    setTimeText();
                    break;
                case R.id.button_stop_record:
                    mAudioRecorder.stopRecording();
                    clearTimeText();
                    break;
                default:
                    break;
            }
        }
    };

    //设置handler参数，用于刷新UI
    static Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            int duration = bundle.getInt("duration");
            int currentPosition = bundle.getInt("currentPosition");
            playingSeekBar.setMax(duration);
            playingSeekBar.setProgress(currentPosition);
        }
    };

    class MyServiceConn implements ServiceConnection
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAudioPlayer = (AudioPlayer) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    //可播放音频文件列表设置显示函数
    private void setFileList()
    {
        String sdcardPath = System.getenv("EXTERNAL_STORAGE");
        List<String> filelist = Utill.getFileList(sdcardPath);
        if (filelist != null && filelist.size() != 0)
        {
            List<String> convertList = new ArrayList<String>();
            for (int i = 0; i < filelist.size(); i++)
            {
                String fileSuffix = filelist.get(i).substring(filelist.get(i).length()-4);
                if (fileSuffix.equals(".mp3") || fileSuffix.equals(".wav"))
                {
                    convertList.add(filelist.get(i));
                }
            }
            String[] strFileList = new String[convertList.size()];
            for (int i = 0; i < convertList.size(); i++)
                strFileList[i] = convertList.get(i);
            mFileList = strFileList;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFileList);
        audio_file_list.setAdapter(adapter);
        audio_file_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAudioPlayer.isPlaying())
                    mAudioPlayer.stop();
                mAudioPlayer.play(mFileList[position]);
                isChoosed = true;
                button_play_pause.setEnabled(isChoosed);
                button_play_pause.setClickable(isChoosed);
                playingSeekBar.setEnabled(isChoosed);
            }
        });
    }

    private void setTimeText()
    {
        if (mRecordTimeTimer == null)
        {
            mRecordTimeTimer = new Timer();
            mRecordTimeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mTimerHandler.sendEmptyMessage(0);
                }
            },0, 1000);
        }
    }

    private void clearTimeText()
    {
        if (mRecordTimeTimer != null)
        {
            mRecordTimeTimer.cancel();
            mRecordTimeTimer = null;
            count = 0;
        }
    }

    private String formatTime(int secTime)
    {
        int hour, min, sec;
        hour = secTime / (60 * 60);
        min = (secTime - hour * 60 * 60) / 60;
        sec = (secTime - hour * 60 * 60 - min * 60) % 60;
        String strHour = hour + "";
        String strMin = min + "";
        String strSec = sec + "";
        if (hour < 10)
            strHour = "0" + hour;
        if (min < 10)
            strMin = "0" + min;
        if (sec < 10)
            strSec = "0" + sec;
        String strTime = strHour + " : " + strMin + " : " + strSec;
        return strTime;
    }

    Handler mTimerHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            recordTimeTextView.setText(formatTime(count++));
        }
    };

}
