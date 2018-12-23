package com.owensong.sosimhanrecorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 12/28/2014.
 */
public class FileCombination {

    private String mFileName = null;
    private String mFilePath = null;
    private int pauseCount=0;
    private DBHelper mDatabase;
    private Context context;

    public FileCombination(Context context,int pauseCount) {
        mDatabase = new DBHelper(context);
        this.pauseCount=pauseCount;
        this.context=context;
        setFileNameAndPath();
        combine();
    }

    public void combine(){
        Movie[] inMovies = new Movie[pauseCount];
        String tempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TempSoundRecorder/"+ context.getString(R.string.default_file_name);
        try{
            for(int i=0;i<pauseCount;i++) {
                inMovies[i] = MovieCreator.build(tempFilePath + (i+1) + ".mp4");
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

          List<Track> audioTracks = new LinkedList<Track>();
          for (Movie m : inMovies){
              for (Track t : m.getTracks()){
                  if (t.getHandler().equals("soun")){
                      audioTracks.add(t);
                  }
              }
          }

          Movie output = new Movie();
          if (audioTracks.size() > 0){
              try{
                  output.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
              }catch (IOException e){
                  e.printStackTrace();
              }
          }

          Container out = new DefaultMp4Builder().build(output);

          FileChannel fc = null;

          try{
              fc = new FileOutputStream(new File(mFilePath)).getChannel();
          }
          catch (FileNotFoundException e){
              e.printStackTrace();
          }

          try{
              out.writeContainer(fc);
          }
          catch (IOException e){
              e.printStackTrace();
          }
          try{
              fc.close();
          }
          catch (IOException e) {}
          mDatabase.addRecording(mFileName, mFilePath, 1000);
    }

    public void setFileNameAndPath(){
        int count = 0;
        mFileName = context.getString(R.string.default_file_name) + "_" + (mDatabase.getCount() + count) + ".mp4";
        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFilePath += "/SoundRecorder/" + mFileName;
    }

}
