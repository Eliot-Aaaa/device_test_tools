package com.example.devicetest.module.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorder {

    private MediaRecorder mMediaRecorder;

    public AudioRecorder()
    {
        mMediaRecorder = new MediaRecorder();
    }

    public void startRecording(int outputfileformat, String extension, Context context) {
        stopRecording();
        File sampleFile;
        File sampleDir = Environment.getExternalStorageDirectory();
        if (!sampleDir.canWrite()) // Workaround for broken sdcard support on the device.
            sampleDir = new File("/sdcard/sdcard");

        try {
            sampleFile = File.createTempFile("recording", extension, sampleDir);
        } catch (IOException e) {
            return;
        }

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(outputfileformat);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(sampleFile.getAbsolutePath());
        mMediaRecorder.setAudioEncodingBitRate(12200);

        // Handle IOException
        try {
            mMediaRecorder.prepare();
        } catch(IOException exception) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            return;
        }
        // Handle RuntimeException if the recording couldn't start
        try {
            mMediaRecorder.start();
        } catch (RuntimeException exception) {
            AudioManager audioMngr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            boolean isInCall = ((audioMngr.getMode() == AudioManager.MODE_IN_CALL) ||
                    (audioMngr.getMode() == AudioManager.MODE_IN_COMMUNICATION));
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            return;
        }
    }

    public void stopRecording() {
        if (mMediaRecorder == null)
            return;

        try {
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.w("Recorder", "Catch RuntimeException on MediaRecorder.stop() due to called immediately after MediaRecorder.start().");
        }
        mMediaRecorder.release();
        mMediaRecorder = null;

    }

}
