package com.kynetx.mci.services;

import java.util.ArrayList;
import java.util.List;

import com.kynetx.mci.activities.ServiceActivity;

import com.kynetx.mci.models.MediaIndex;

import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

public class MediaCheckService extends Service 
{
	
	private static final String DEBUG_TAG = "MOBILE_CLOUD-SERVICE";
	private static final int HTTP_NOTIFY = 0x3001;
    public static final String EXTRA_UPDATE_RATE = "update-rate";
	private int updateRate = 60000; //every 5 seconds
    public static final String MCI_MEDIA_SERVICE = "com.kynetx.mci.services.MediaCheckService.SERVICE";
    boolean stop = false;
    private NotificationManager notifier = null;

    String lastFile = "";
    Intent startIntent;
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return remoteMediaCheckInterface;
	}
	
	 @Override 
	    public void onCreate()
	    {
	    	super.onCreate();
	    	Log.d(DEBUG_TAG, "Creating service");
	    	notifier = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	    	
	    }
	    
	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	        // Android 2.0, L5, version
	        Log.v(DEBUG_TAG, "onStartCommand() called, must be on L5 or later");
	        if (flags != 0) {
	            Log.w(DEBUG_TAG, "Redelivered or retrying service start: " + flags);
	        }
	        startIntent = intent;
	        doServiceStart(intent, startId);
	       
	        return Service.START_REDELIVER_INTENT;
	    }

	    
	    public void doServiceStart(Intent intent, int startId)
	    {
	    	Log.d(DEBUG_TAG, "starting service " + startId + "....");
	    	//updateRate = intent.getIntExtra(EXTRA_UPDATE_RATE, -1);
	    	if(updateRate == -1)
	    	{
	    		//updateRate = 60000;
	    	}
	    	
	    	
	    	
	    	Log.d(DEBUG_TAG, "Update Rate: " + updateRate);
	    	Criteria criteria = new Criteria();
	        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
	        criteria.setPowerRequirement(Criteria.POWER_LOW);
	        
	        @SuppressWarnings("deprecation")
			Notification notify =
	                new Notification(android.R.drawable.stat_notify_more, "Index Request ", System.currentTimeMillis());
	        notify.flags |= Notification.FLAG_AUTO_CANCEL;
	        Intent toLaunch =
	            new Intent(getApplicationContext(), ServiceActivity.class);
	        
	        PendingIntent intentBack =
	                PendingIntent.getActivity(getApplicationContext(), 0, toLaunch, 0);
	        notify.setLatestEventInfo(getApplicationContext(), "MCI Service Media Check",
	            "Service start at " + updateRate + " ms intervals with [] as the provider.", intentBack);
	        notifier.notify(HTTP_NOTIFY, notify);
	        Log.d(DEBUG_TAG, "Service Started.");
	        stop = false;
	        
	    }
	    
	    private void startCheckingForNewMedia(){
			
	    	new CheckForNewMediaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	    
	    private final IRemoteMediaCheckInterface.Stub remoteMediaCheckInterface = new IRemoteMediaCheckInterface.Stub() {

			@Override
			public boolean doWeHaveMedia() throws RemoteException {
				// TODO Auto-generated method stub
				return false;
			}

			
			
			
		};
		
		
		private class CheckForNewMediaTask extends AsyncTask<Void, Void, MediaIndex>
		{
			
			@Override
			protected MediaIndex doInBackground(Void... arg0) {
				
				StringBuilder list = new StringBuilder();
				list.append("Photos: ");
				
				String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
						MediaStore.Images.ImageColumns.DATA,
						MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,
						MediaStore.Images.ImageColumns.DISPLAY_NAME,
						MediaStore.Images.ImageColumns.MIME_TYPE,
						MediaStore.Images.ImageColumns.DESCRIPTION};     
		       
		        while(stop == false)
			    {
		        	
		        	/*
					final CursorLoader cursor = new CursorLoader(startIntent, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, 
							null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
		        	CursorLoader cursor = new CursorLoader(sta)
		        	//final Cursor cursor = CursorLoader
			        if(cursor != null){
			            int count = 0;
			            List<String> files = new ArrayList<String>();
			            String file = "";
			        	//while(cursor.moveToNext())
			            if(cursor.moveToFirst())
			        	{
			        		
			        		if(cursor.getString(2).equalsIgnoreCase("camera"))
			        		{
				        		count++;
				        		int cols = cursor.getColumnCount();
				        		
				        		list.append("\n");
				        		
				        		if(lastFile.equals(cursor.getString(1)))
				        		{
				        			//do nothing
				        		}else {
				        			file = cursor.getString(1);
				        			CopyFileUtility.copyFile(file, null, MediaType.Photo);
				        				
				        			
				        		}
			        		}
					            // you will find the last taken picture here
					            // according to Bojan Radivojevic Bomber comment do not close the cursor (he is right ^^)
					            //cursor.close();
			        		
			        	}
			            try {
							Thread.sleep(6000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	//new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
			        }
					
					String photoDirectory = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
					list.append("\nDir: " + photoDirectory);
					*/
		        	Log.e("MediaCheckService", "Running");
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
		        
		        
				return null;
			}
			
		}
		
}
