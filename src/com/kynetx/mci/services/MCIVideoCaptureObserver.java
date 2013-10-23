package com.kynetx.mci.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.FileAccessLog;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;

public class MCIVideoCaptureObserver extends FileObserver {

	private static final String DEBUG_TAG = "VideoObserver";
	String absolutePath;
	boolean initialLoadDone = false;
	String deviceChannelId;
	String videoFile = "";
	boolean copyInProgress = false;
	
	public MCIVideoCaptureObserver(String path, String deviceId) {
		super(path + "/DCIM/Camera/", FileObserver.CLOSE_NOWRITE);
		absolutePath = path;
		this.deviceChannelId = deviceId;
		Log.e(DEBUG_TAG, "deviceId: " + this.deviceChannelId);
		Log.e(DEBUG_TAG, "Path: " + this.absolutePath);
	}

	@Override
	public void onEvent(int event, String path) {
		if (path == null) {
			return;
		}
		if(Config.startDone == true)
		{
			if ((FileObserver.CLOSE_NOWRITE & event)!=0) {
				if(path.endsWith(".mp4")){
					//FileAccessLog.accessLogMsg += absolutePath + "/" + path + " is deleted\n";
					Log.e(DEBUG_TAG, "Video Created: " + absolutePath + "/" + path);
					if(copyInProgress == false){
						copyInProgress = true;
						if(!path.equalsIgnoreCase(videoFile))
						{
							videoFile = path;
							new CopyVideoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);
						}
					}
				}
			}
		}		
	}
	
	private void finishCopy()
	{
		copyInProgress = false;
	}
	
	private class CopyVideoTask extends AsyncTask<String, Integer, Void>
    {
    	
    	int photoCount = 0;
		@Override
		protected Void doInBackground(String... video) {
			
			StringBuilder list = new StringBuilder();
			list.append("Photos: ");
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
						
			Log.e("video", video[0]);

			String media = filePath + "/" + video[0];
			int idx = 1;
						
			Log.e("video to copy ",media);
			
			
			CopyFileUtility.copyFile(media, null, MediaType.Video);
					
			return null;
		}
		
		@Override
		protected void onPostExecute(Void nana)
		{
			finishCopy();
		}
		
    	
    }

}
