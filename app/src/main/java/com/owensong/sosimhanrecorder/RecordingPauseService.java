package com.owensong.sosimhanrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

/**
 * Created by OwenSong on 12/22/2018.
 */
public class RecordingPauseService extends Service {

    private static final String LOG_TAG = "RecordingService";

    private String mFileName = null;
    private String mFilePath = null;

    private MediaRecorder mRecorder = null;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;

    private TimerTask mIncrementTimerTask = null;
    private int pauseCount=0;

    private int finalCheck=0;

    IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        public RecordingPauseService getService() { // 서비스 객체를 리턴
            return RecordingPauseService .this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        pauseCount=intent.getExtras().getInt("pauseCount");
        startRecording();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pauseCount=intent.getExtras().getInt("pauseCount");
        startRecording();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void setFinalCheck() { // 임의 랜덤값을 리턴하는 메서드
        this.finalCheck=1;
    }

    public void startRecording() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        File f;
        mFileName = getString(R.string.default_file_name)+pauseCount+".mp4";
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/TempSoundRecorder/" + mFileName;
        f = new File(mFilePath);
    }

    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.reset();
        mRecorder.release();
       Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
        if(finalCheck==1) {
            new FileCombination(getApplicationContext(), pauseCount);
        }
    }

}
