package com.example.devicetest.module.audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class AudioService extends Service {

    private AudioPlayer mAudioPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mAudioPlayer = new AudioPlayer();
        mAudioPlayer.setHandler(AudioActivity.handler);
        return mAudioPlayer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mAudioPlayer.isPlaying())
            mAudioPlayer.stop();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}


