package net.oukranos.ophiuchus;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.oukranos.ophiuchus.utils.AppUtils;
import net.oukranos.ophiuchus.utils.Logger;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AudioRecordActivity extends AppCompatActivity {
    private Timer _timer = null;
    private TextView _txvCaption = null;
    private MediaRecorder _audioRec = null;
    private boolean _bIsRecording = false;
    private String _filename = "unknown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Intent recvIntent = getIntent();
        _filename = recvIntent.getStringExtra("AUDIO_FILENAME");

        /* Save a reference to the recorder caption */
        _txvCaption = (TextView) findViewById(R.id.txv_record_msg);

        /* Add functionality to the record button */
        final ImageButton btnRecord = (ImageButton) findViewById(R.id.btn_audio_record);
        btnRecord.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!_bIsRecording) {
                            if (!startRecording()) {
                                Logger.err("Failed to start recording");
                                return;
                            }

                            btnRecord.setImageResource(android.R.drawable.ic_media_pause);
                            btnRecord.setColorFilter(Color.parseColor("#800000"));

                            startTimer();

                            _bIsRecording = true;
                        } else {
                            if (!stopRecording()) {
                                Logger.err("Failed to stop recording");
                                return;
                            }

                            btnRecord.setImageResource(android.R.drawable.ic_media_play);
                            btnRecord.setColorFilter(Color.parseColor("#008000"));

                            stopTimer();

                            new Handler().postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new SetCaptionTask("Tap to start recording"));
                                    }
                                },
                                500
                            );

                            /* Cache the audio filename so that we know that this audio record
                             *  already exists in case the user tries to create another one */
                            OphiuchusApp app = AppUtils.getApplication(AudioRecordActivity.this);
                            app.cacheAudioFilename(_filename);

                            _bIsRecording = false;

                            Toast.makeText(AudioRecordActivity.this, "Recording Done!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        return;
                    }
                }
        );


        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean startRecording() {
        if (_audioRec != null) {
            Logger.err("Media Recorder instance was leaked");
            return false;
        }

        try {
            _audioRec = new MediaRecorder();
            _audioRec.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            _audioRec.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            _audioRec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            String storagePath = AppUtils.getStoragePath();
            AppUtils.prepareDirectory(storagePath);
            _audioRec.setOutputFile(storagePath + _filename + ".amr");


            _audioRec.prepare();
            _audioRec.start();
        } catch (IOException e) {
            Logger.err("Exception occurred: " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean stopRecording() {
        if (_audioRec != null) {
            _audioRec.stop();
            _audioRec.release();
            _audioRec = null;
        }

        return true;
    }

    private void startTimer() {
        final long lStart = System.currentTimeMillis();
        _timer = new Timer();
        _timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        long lElapsedSec = (System.currentTimeMillis() - lStart) / 1000;
                        long lElapsedMin = lElapsedSec / 60;
                        lElapsedSec = lElapsedSec % 60;

                        final String min = lElapsedMin < 10 ?
                                "0" + Long.toString(lElapsedMin) :
                                Long.toString(lElapsedMin);
                        final String sec = lElapsedSec < 10 ?
                                "0" + Long.toString(lElapsedSec) :
                                Long.toString(lElapsedSec);

                        runOnUiThread(new SetCaptionTask(min + ":" + sec));
                    }
                },
                0,
                1000
        );
    }

    private void stopTimer() {
        if (_timer != null) {
            _timer.cancel();
            _timer = null;
        }

        return;
    }

    private class SetCaptionTask implements Runnable {
        private String _message = "";

        public SetCaptionTask(String message) {
            if (message == null) {
                _message = "";
                return;
            }
            _message = message;
            return;
        }

        @Override
        public void run() {
            _txvCaption.setText(_message);
        }
    }
}
