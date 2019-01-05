package com.owensong.sosimhanrecorder;

public class RecordingTime {

    private long mStartingTimeMillis;
    private long mElapsedMillis;

    RecordingTime(){
        this.mStartingTimeMillis=0;
        this.mElapsedMillis=0;
    }

    public long getmStartingTimeMillis() {
        return mStartingTimeMillis;
    }

    public void setmStartingTimeMillis(long mStartingTimeMillis) {
        this.mStartingTimeMillis = mStartingTimeMillis;
    }

    public long getmElapsedMillis() {
        return mElapsedMillis;
    }

    public void setmElapsedMillis(long mElapsedMillis) {
        this.mElapsedMillis = mElapsedMillis;
    }

    public void startRecording(){
        mStartingTimeMillis = System.currentTimeMillis();
    }

    public void stopRecording(){
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
    }
}
