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

public class VideoCaptureService extends Service {
	
	private static final String DEBUG_TAG = "MCIVideoCaptureService";
	private MCIVideoCaptureObserver videoObserver;
	
	private File sdCard;
	
	private String deviceChannelId;
	public static final String MCI_SERVICE = "com.kynetx.mci.services.VideoCaptureService.SERVICE"; 

	
	@Override
	public  void onCreate(){
		//Log.e(DEBUG_TAG, "Creating service");
		if(!EnvironmentUtils.isExternalStorageAvailable())
		{
			Toast.makeText(VideoCaptureService.this, "SDCard is not available", Toast.LENGTH_SHORT).show();
		}else{
			//File sdCard = new File("/sdcard/");
			sdCard = new File(Environment.getExternalStorageDirectory().getPath());
			if(sdCard != null)
			{
				
				//createFileObserver(sdCard);
			}
		}
	}
	
	private void createVideoObserver(File file)
	{
		if(!file.isDirectory())
		{
			Log.e(DEBUG_TAG, "File does not exist: " + file.getAbsolutePath());
		}else
		{
			//TODO: Once done testing try with MCI_MEDI_PATH
			//TODO: Move the environment get external storage to EnviornmantUtils
			videoObserver = new MCIVideoCaptureObserver(Environment.getExternalStorageDirectory().getAbsolutePath(), deviceChannelId);
		}
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Android 2.0, L5, version
        //Log.v(DEBUG_TAG, "onStartCommand() called, must be on L5 or later");
        if (flags != 0) {
            Log.e(DEBUG_TAG, "Redelivered or retrying video observer service start: " + flags);
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
		
		Log.d(DEBUG_TAG, "starting video observer service " + startId + "....");
    	
    	Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        
        deviceChannelId = intent.getStringExtra(Constants.EXTRA_DEVICE_CHANNEL_ID);
        createVideoObserver(sdCard);
		Toast.makeText(getApplicationContext(), "Video Observer Service started...", Toast.LENGTH_SHORT).show();
		videoObserver.startWatching();
        
       
	}
	
	
	@Override
	public void onDestroy()
	{
		videoObserver.stopWatching();
		videoObserver = null;
	}

	private final IRemoteFileModificationInterface.Stub remoteFileModificationInterface = new IRemoteFileModificationInterface.Stub() {
		
		@Override
		public void stopService() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return remoteFileModificationInterface;
	}
	

	

}
