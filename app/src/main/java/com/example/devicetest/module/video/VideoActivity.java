package com.example.devicetest.module.video;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import com.example.devicetest.R;

public class VideoActivity extends Activity {

    VideoView video_play_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_video);
        video_play_view = findViewById(R.id.video_play_view);

        String path = Environment.getExternalStorageDirectory().getPath()+"/test.mp4";
        video_play_view.setVideoPath(path);
        MediaController mediaController = new MediaController(this);
        video_play_view.setMediaController(mediaController);
        video_play_view.requestFocus();
    }
}
