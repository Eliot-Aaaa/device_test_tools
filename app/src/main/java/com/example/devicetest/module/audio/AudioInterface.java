package com.example.devicetest.module.audio;

import java.util.logging.Handler;

public interface AudioInterface {
    void play(String fileName);
    void continuePlay();
    void stop();
    void pause();
    void seekTo(int progress);
    boolean isPlaying();
    int getDuration();
    int getCurrentPosition();
}
