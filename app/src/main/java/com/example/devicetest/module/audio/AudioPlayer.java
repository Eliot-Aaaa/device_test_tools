package com.example.devicetest.module.audio;

import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayer extends Binder implements AudioInterface {

    private MediaPlayer mMediaPlayer;
    private Timer mTimer;
    private Handler mHandler;

    public AudioPlayer()
    {
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public void play(String fileName) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(fileName);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                    addTimer();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void continuePlay() {
        mMediaPlayer.start();
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void addTimer() {
        //播放进度需要不停的获取，不停的刷新进度条，使用计时器每1000毫秒获取一次播放进度
        //发消息至Handler，把播放进度放进Message对象中，在Handler中更新SeekBar的进度
        if (mTimer == null) {

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    // 获取到歌曲总时长
                    int duration = getDuration();
                    // 获取歌曲当前进度
                    int currentPosition = getCurrentPosition();
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);

                }
            }, 5, 1000);
        }
    }

    void setHandler(Handler handler)
    {
        mHandler = handler;
    }
}
