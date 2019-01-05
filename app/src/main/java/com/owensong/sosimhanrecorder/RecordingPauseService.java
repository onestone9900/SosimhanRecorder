package com.owensong.sosimhanrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

/**
 * Created by OwenSong on 12/22/2018.
 */
public class RecordingPauseService extends Service {

    private static final String LOG_TAG = "RecordingPauseService";
    private String mFileName = null;
    private String mFilePath = null;
    private MediaRecorder mRecorder = null;
    private int pauseCount=0;
    private int finalCheck=0;
    private RecordingTime recordingTime = null;
    private long totalRecordingTime = 0;
    IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        public RecordingPauseService getService() { // 서비스 객체를 리턴
            return RecordingPauseService .this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        recordingTime=new RecordingTime();
        pauseCount=intent.getExtras().getInt("pauseCount");
        startRecording();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
        }

        super.onDestroy();
    }

    public void setFinalCheck() {
        this.finalCheck=1;
    }

    public void setTotalRecordingTime(long totalRecordingTime){
        this.totalRecordingTime=totalRecordingTime;
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
            recordingTime.startRecording();
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

    public long stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        recordingTime.stopRecording();
        mRecorder = null;
        if(finalCheck==1) {
            totalRecordingTime = totalRecordingTime + recordingTime.getmElapsedMillis();
            new FileCombination(getApplicationContext(), pauseCount, totalRecordingTime);
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/TempSoundRecorder";
            try {
                File file = new File(mFilePath);
                File[] files = file.listFiles();

                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                file.delete();
            }catch (Exception e) {

            }
        }
        return recordingTime.getmElapsedMillis();
    }

}
