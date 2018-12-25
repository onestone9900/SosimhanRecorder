package com.owensong.sosimhanrecorder.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.owensong.sosimhanrecorder.FileCombination;
import com.owensong.sosimhanrecorder.R;
import com.owensong.sosimhanrecorder.RecordingPauseService;
import com.owensong.sosimhanrecorder.RecordingService;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;

import com.owensong.sosimhanrecorder.RecordingPauseService.MyBinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RecordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

/**
 * Modifed by OwenSong on 12/11/2018
 */

public class RecordFragment extends Fragment {

    private static final int START_RECORDING=1;
    private static final int STOP_RECORDING=2;
    private static final int PAUSE_STOP_RECORDING=3;
    private static final int PAUSE_START_RECORDING=4;

    private FloatingActionButton mPauseButton=null;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();
    private int position;
    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;
    private int recordingOption = STOP_RECORDING;
    long timeWhenPaused = 0; //stores time when user clicks pause button
    private TextView mCountTextView;
    private Button mCountButton;
    int count = 0;
    int pauseCount=1;
    int endingCheck=0;
    int startServiceCheck=0;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        //update recording prompt text
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);

        mCountButton=recordView.findViewById(R.id.btnCount);
        mCountTextView=recordView.findViewById(R.id.tvCount);
        mCountButton.setEnabled(false);
        mCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountTextView.setText(Integer.toString(++count));
            }
        });
        mPauseButton = recordView.findViewById(R.id.btnPause);
        mPauseButton.setEnabled(false);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recordingOption==PAUSE_STOP_RECORDING){
                    recordingOption=PAUSE_START_RECORDING;
                }else {
                    recordingOption=PAUSE_STOP_RECORDING;
                }
                onRecord(recordingOption);
            }
        });

        mRecordButton =recordView.findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recordingOption==STOP_RECORDING){
                    recordingOption=START_RECORDING;
                }else {
                    recordingOption=STOP_RECORDING;
                }
                onRecord(recordingOption);
            }
        });
        return recordView;
    }

    RecordingPauseService rp;
    boolean isService = false;
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
        // 서비스와 연결되었을 때 호출되는 메서드
        // 서비스 객체를 전역변수로 저장
            MyBinder mb = (MyBinder) service;
            rp = mb.getService();
            // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }
        public void onServiceDisconnected(ComponentName name) {
        // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord(int option){

        Intent intent = new Intent(getActivity(), RecordingService.class);
        Intent tempIntent = new Intent(getActivity(), RecordingPauseService.class);

        switch(option){
            case START_RECORDING:{
                // start recording
                mRecordButton.setImageResource(R.drawable.ic_media_stop);
                Toast.makeText(getActivity(),R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
                File tempFolder = new File(Environment.getExternalStorageDirectory() + "/TempSoundRecorder");
                if (!tempFolder.exists()) {
                    //folder /SoundRecorder doesn't exist, create the folder
                    tempFolder.mkdir();
                }

                File mainFolder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
                if (!mainFolder.exists()) {
                    mainFolder.mkdir();
                }
                mPauseButton.setBackgroundColor(Color.RED);
                mCountButton.setEnabled(true);
                mPauseButton.setEnabled(true);
                //start RecordingService
                getActivity().bindService(tempIntent.putExtra("pauseCount",pauseCount),conn, Context.BIND_AUTO_CREATE);
                //keep screen on while recording
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mRecordPromptCount++;
                startServiceCheck=1;
                break;
            }
            case STOP_RECORDING:{
                //stop recording
                mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
                mPauseButton.setImageResource(R.drawable.ic_media_pause);
                timeWhenPaused = 0;
                count = 0;
                mCountButton.setEnabled(false);
                mCountTextView.setText("0");
                mRecordingPrompt.setText(getString(R.string.record_prompt));
                mPauseButton.setBackgroundColor(Color.GRAY);
                mPauseButton.setEnabled(false);
                endingCheck=1;
                rp.setFinalCheck();
                if(startServiceCheck==1) {
                    getActivity().unbindService(conn);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                pauseCount=1;
                endingCheck=0;
                startServiceCheck=0;
                break;
            }
            case PAUSE_STOP_RECORDING:{
                //stop recording
                mRecordButton.setBackgroundColor(Color.GRAY);
                mRecordButton.setEnabled(false);
                pauseCount++;
                mPauseButton.setImageResource(R.drawable.ic_media_play);
                mCountButton.setEnabled(false);
                mRecordingPrompt.setText(getString(R.string.resume_recording_button));
                getActivity().unbindService(conn);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //allow the screen to turn off again once recording is finished
                startServiceCheck=0;
                break;
            }
            case PAUSE_START_RECORDING:{
                // start recording
                mPauseButton.setImageResource(R.drawable.ic_media_pause);
                mRecordButton.setBackgroundColor(Color.RED);
                mRecordButton.setEnabled(true);
                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mCountButton.setEnabled(true);
                //start RecordingService
                getActivity().bindService(tempIntent.putExtra("pauseCount",pauseCount),conn, Context.BIND_AUTO_CREATE);
                //keep screen on while recording
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mRecordPromptCount++;
                startServiceCheck=1;
                break;
            }
        }

    }

}