package com.astro.dsoplanner;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.IOException;

public class Player {
	
	private String mFileName = null;	
	private MediaRecorder mRecorder = null;	
	private MediaPlayer   mPlayer = null;
	private MediaPlayer.OnCompletionListener listener;
	
	public Player(String fileName){
		mFileName=fileName;
	}
	public Player(String fileName, MediaPlayer.OnCompletionListener listener){
		mFileName=fileName;
		this.listener=listener;
	}
	public void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            if(listener!=null){
            	mPlayer.setOnCompletionListener(listener);
            	mPlayer.setLooping(false);
            }
        	mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            
        }
    }
	
    public void stopPlaying() {
    	if(mPlayer!=null){
    		mPlayer.release();
    		mPlayer = null;
    	}
    }
    
    public boolean isPlaying(){
    	return (mPlayer!=null);
    }
    /**
     * 
     * @return true if the recording has started, false otherwise
     */
    public boolean startRecording() {
        mRecorder = new MediaRecorder();
       

        try {
        	mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        	mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        	mRecorder.setOutputFile(mFileName);
        	mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        	mRecorder.prepare();
        	mRecorder.start();
        } catch (Exception e) {
            return false;
        }

        
        return true;
    }

    public void stopRecording() {
    	try{
    		mRecorder.stop();
    		
    	}
    	catch(Exception e){}
    	mRecorder.release();
		mRecorder = null;
    	
    }
    public boolean isRecording(){
    	return (mRecorder!=null);
    }
    public void release(){
    	if(isPlaying()) stopPlaying();
    	if(isRecording()) stopRecording();
    }
}
