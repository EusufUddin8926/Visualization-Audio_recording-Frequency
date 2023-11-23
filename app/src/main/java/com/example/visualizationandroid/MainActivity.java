package com.example.visualizationandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.visualizer.amplitude.AudioRecordView;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private String[] requiredPermissions = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    private MediaRecorder recorder = null;
    private File audioFile = null;
    AudioRecordView audioRecordView;
    private Button startAudioBtn, stopAudioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audioRecordView = findViewById(R.id.audioRecordView);
        startAudioBtn = findViewById(R.id.startRecording);
        stopAudioButton = findViewById(R.id.stopRecording);

        startAudioBtn.setOnClickListener(view -> startRecording());

        stopAudioButton.setOnClickListener(view -> stopRecording());

        audioRecordView.setChunkAlignTo(AudioRecordView.AlignTo.CENTER);
        audioRecordView.setChunkRoundedCorners(true);
        audioRecordView.setChunkSoftTransition(true);



       // setSwitchListeners();
    }

    private void startRecording() {
        if (!permissionsIsGranted(requiredPermissions)) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 200);
            return;
        }

        startAudioBtn.setEnabled(false);
        stopAudioButton.setEnabled(true);
        long seed = System.currentTimeMillis();
        Random rng = new Random(seed);
        long randomNumber = rng.nextLong();
        // Creating file
        audioFile = new File(getExternalCacheDir().getAbsolutePath() + "/" + randomNumber + ".mp3");
        // Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.setAudioSamplingRate(48000);
        recorder.setAudioEncodingBitRate(48000);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName(), e.getMessage() != null ? e.getMessage() : e.toString());
            return;
        }
        recorder.start();

        startDrawing();
    }

    private void stopRecording() {
        findViewById(R.id.startRecording).setEnabled(true);
        findViewById(R.id.stopRecording).setEnabled(false);
        // Stopping recorder
        if (recorder != null) {
            try {
                recorder.stop();


            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                recorder.reset();
                recorder.release();
                recorder = null;
            }
        }
        stopDrawing();
    }



    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (recorder != null) {
                int currentMaxAmplitude = recorder.getMaxAmplitude();
                audioRecordView.update(currentMaxAmplitude);
            }
            // Post the same runnable with a delay of 500 milliseconds
            handler.postDelayed(this, 50);
        }
    };


    private void startDrawing() {
        handler.postDelayed(runnable, 50);
    }

    private void stopDrawing() {
        handler.removeCallbacks(runnable);
        audioRecordView.recreate();
    }

    private boolean permissionsIsGranted(String[] perms) {
        for (String perm : perms) {
            int checkVal = ActivityCompat.checkSelfPermission(this, perm);
            if (checkVal != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        startRecording();
    }
}