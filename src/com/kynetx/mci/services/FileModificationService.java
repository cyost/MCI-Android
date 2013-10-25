package com.kynetx.mci.services;

import java.io.File;

import com.kynetx.mci.config.Constants;
import com.kynetx.mci.utils.EnvironmentUtils;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class FileModificationService extends Service{
	
	private static final String DEBUG_TAG = "FILE-OBSERVER";

	MCIFileObserver fileObserver;
	MCIFileObserver videoFileObserver;
	int numOfFos = 0;
	boolean initialLoadDone = false;
	private File sdCard;
	
	private String deviceChannelId;
	public static final String MCI_SERVICE = "com.kynetx.mci.services.FileModificationService.SERVICE"; 
	
	@Override
	public  void onCreate(){
		//Log.e(DEBUG_TAG, "Creating service");
		if(!EnvironmentUtils.isExternalStorageAvailable())
		{
			Toast.makeText(FileModificationService.this, "SDCard is not available", Toast.LENGTH_SHORT).show();
		}else{
			//File sdCard = new File("/sdcard/");
			sdCard = new File(Environment.getExternalStorageDirectory().getPath());
			if(sdCard != null)
			{
				
				//createFileObserver(sdCard);
			}
		}
	}
	
	private void createFileObserver(File file)
	{
		if(!file.isDirectory())
		{
			Log.e(DEBUG_TAG, "File does not exist: " + file.getAbsolutePath());
		}else
		{
			//TODO: Once done testing try with MCI_MEDI_PATH
			//TODO: Move the environment get external storage to EnviornmantUtils
			fileObserver = new MCIFileObserver(Environment.getExternalStorageDirectory().getAbsolutePath() 
					+ "/" + Constants.MCI_MEDIA_PATH + "/" + Constants.MCI_PHOTO_FOLDER, deviceChannelId);
			
			videoFileObserver = new MCIFileObserver(Environment.getExternalStorageDirectory().getAbsolutePath() 
					+ "/" + Constants.MCI_MEDIA_PATH + "/" + Constants.MCI_VIDEO_FOLDER, deviceChannelId);
			
			/*fileObserver = new MCIFileObserver(Environment.getExternalStorageDirectory().getAbsolutePath() 
					+ "/" + Constants.MCI_MEDIA_PATH, deviceChannelId);*/
		}
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Android 2.0, L5, version
        //Log.v(DEBUG_TAG, "onStartCommand() called, must be on L5 or later");
        if (flags != 0) {
            Log.e(DEBUG_TAG, "Redelivered or retrying file observer service start: " + flags);
        }
        
        doServiceStart(intent, startId);
        
        return Service.START_STICKY;
    }
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		doServiceStart(intent, startId);
	}
	
	private void doServiceStart(Intent intent, int startId){
		
		Log.d(DEBUG_TAG, "starting file observer service " + startId + "....");
    	
    	Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        
        deviceChannelId = intent.getStringExtra(Constants.EXTRA_DEVICE_CHANNEL_ID);
        createFileObserver(sdCard);
		Toast.makeText(getApplicationContext(), "File Observer Service started...", Toast.LENGTH_SHORT).show();
		fileObserver.startWatching();
		videoFileObserver.startWatching();
        
       
	}
	
	private final IRemoteFileModificationInterface.Stub remoteFileModificationInterface = new IRemoteFileModificationInterface.Stub() {
		
		@Override
		public void stopService() throws RemoteException {
			
		}
	};
	
	
	@Override
	public void onDestroy()
	{
		fileObserver.stopWatching();
		fileObserver = null;
		videoFileObserver.stopWatching();
		videoFileObserver = null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return remoteFileModificationInterface;
	}

}
