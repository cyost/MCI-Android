package com.kynetx.mci.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.kynetx.mci.activities.ServiceActivity;
import com.kynetx.mci.config.*;
import com.kynetx.mci.models.Device;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

/**
 * This service needs to:
 * 1. Get List of Media Indexes from the server.
 * 2. Remove the indexes from the server.
 * 3. Delete Media from the Media Directories
 * 4. Copy over the latest 5 images to the Media Directory
 * 5. Upload Media Indexes for the images.
 *  
 * @author pbs
 *
 */
public class AutoStartService extends Service {

	private static final int HTTP_NOTIFY = 0x3001;
	private static final String DEBUG_TAG = "MCI-AUTOSTART-SERVICE";
	public static final String MCI_SERVICE = "com.kynetx.mci.services.AutoStartService.SERVICE";
	private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH;
	
	Intent startIntent;
	private NotificationManager notifier = null;
	boolean stop = false;
	
	GetMediaListTask getMediaListTask;
	
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
        notify.setLatestEventInfo(getApplicationContext(), "MCI AutoStart Started",
            "Service start.", intentBack);
        notifier.notify(HTTP_NOTIFY, notify);
        Log.d(DEBUG_TAG, "Service Started.");
        stop = false;
        startGetMediaList();
        //startCheckForNewMedia();
    }
    
    private void startGetMediaList()
    {
    	getMediaListTask = new GetMediaListTask();
    	stop = false;
    	getMediaListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    @Override
    public void onDestroy()
    {
    	Log.v(DEBUG_TAG, "onDestroy() called");
    	
    	// notify that we've stopped
        @SuppressWarnings("deprecation")
		Notification notify =
            new Notification(android.R.drawable.stat_notify_more, "MCI Service",
                System.currentTimeMillis());
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent toLaunch =
            new Intent(getApplicationContext(), ServiceActivity.class);
        PendingIntent intentBack =
            PendingIntent.getActivity(getApplicationContext(), 0, toLaunch, 0);
        notify.setLatestEventInfo(getApplicationContext(), "MCI Service", "Indexing Request stopped", intentBack);
        notifier.notify(HTTP_NOTIFY, notify);
        boolean stop = true;
        getMediaListTask = null;
        super.onDestroy();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return remoteAutoStartInterface;
	}
	
	@SuppressWarnings("unchecked")
	private void removeMediaIndexes(List<String> mediGuids)
	{
		new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediGuids);
	} 
	
	/*private void uploadNewMediaIndexes()
	{
		new UpdateMediaIndexTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}*/
	
	private final IRemoteAutoStartInterface.Stub remoteAutoStartInterface = new IRemoteAutoStartInterface.Stub() {
		
		@Override
		public void stopService() throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};
	
	
	private class GetMediaListTask extends AsyncTask<Void, Integer, List<String>>
	{
		String getMediaListUrl = "https://cs.kobj.net/sky/cloud/a169x727/mciListMedia";
		private String getJson = "";
		
		@Override
		protected List<String> doInBackground(Void... params) {
				
			
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			} 
			
			int i = 1;
			//boolean found = false;
			List<String> guids = checkForMedia(client);
			
			return guids;
			
			
		}
		
		private List<String> checkForMedia(HttpClient client)
		{
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			List<String> guids = new ArrayList<String>();
			String json;
			
			try 
			{				
				request = new HttpGet(getMediaListUrl);
				
				request.addHeader("Kobj-Session", Config.deviceId);
				request.addHeader("content-type", "application/json");
				
				response = client.execute(request, context );
				Log.d(DEBUG_TAG, response.getStatusLine().toString() + " - " + response.getStatusLine().getReasonPhrase());
				Header[] headers = response.getAllHeaders();
				HttpParams rParams = response.getParams();
				//Log.d(DEBUG_TAG, response.get)
				for (Header header : headers) {
					Log.d(DEBUG_TAG, header.getName() + ": " + header.getValue());
				}
				if(response.getStatusLine().getStatusCode() == 200){
					HttpEntity entity = response.getEntity();
					InputStream is = entity.getContent();
					guids = readStream(is);
					//parseJson(json.toString());
					
					return guids;
				}
				
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			
			return guids;
		}
		
		private List<String> readStream(InputStream in) 
		{
			BufferedReader reader = null;
			StringBuilder json = new StringBuilder();
			List<String> guids = new ArrayList<String>();
			
			try 
			{
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	//Log.d(DEBUG_TAG, line);
			    	json.append(line);
			    }	    
			    getJson = json.toString();
			    guids = parseJson(getJson);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return guids;
		}
		
		private List<String> parseJson(String json)
		{
			List<String> media = new ArrayList<String>();
			if(json.length() > 10)
			{
				try{
					media = new ArrayList<String>();
					JSONArray jsonArray = new JSONArray(json);
					
					for (int i=0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Log.d(DEBUG_TAG, "MediaGUID: " + jsonObject.getString("mediaGUID"));
						Device device = new Device();
						String mediaGuid = jsonObject.getString("mediaGUID");
						
						media.add(mediaGuid);
					}
					
				}catch(JSONException e)
				{
					Log.e(DEBUG_TAG, "error parsing json: " + e.getMessage());
				}
			}
			return media;
		}
		
		@Override
		protected void onPostExecute(List<String> mediaGuids)
		{
			removeMediaIndexes(mediaGuids);
		}
	}

	private class RemoveMediaIndexesTask extends AsyncTask<List<String>, Integer, Void>
	{

		List<String> mediaGuids = new ArrayList<String>();
		@Override
		protected Void doInBackground(List<String>... guids) {
			
			String removeUrl = "https://cs.kobj.net/sky/event/"+ Config.deviceId +"/"+ Config.EID +"/cloudos/mciRemoveMedia/?_rids=a169x727";
			
			HttpClient client = new DefaultHttpClient();
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpPost request = null;
			List<String> removeGuids = guids[0];
			
			for (String guid : removeGuids) {
				
				StringBuilder json = new StringBuilder();
				
				json.append("{\"mediaGUID\": \"" + guid + "\"");
				json.append("}");
				
				try 
				{
					
					request = new HttpPost(removeUrl);
					
					request.addHeader("Kobj-Session", Config.deviceId);
					request.addHeader("Host", "cs.kobj.net");
					request.addHeader("content-type", "application/json");//change to form encoded mime type: application/x-www-form-urlencoded
					
					request.setEntity(new ByteArrayEntity(json.toString().getBytes())); //google Request bin
					response = client.execute(request);
					response.getEntity().consumeContent();
					
				} catch (Exception e) {
					  Log.e(DEBUG_TAG, e.getMessage());
				}finally{
					
				}
			}
					
			deleteFiles();
			getMostRecentFiles();
			return null;
		}
		
		private boolean deleteFiles()
		{
			File path = new File(dir + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();
			boolean success = true;
			for(int i=0;i<files.length; i++)
			{
				files[i].delete();
			}
			return success;
		}
		
		private List<File> getMostRecentFiles()
		{
			List<File> recentFiles = new ArrayList<File>();
			
			StringBuilder list = new StringBuilder();
			list.append("Photos: ");
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
			File dir = new File(filePath);
			File[] files = dir.listFiles();
			
			
			int idx = files.length;
			//for(int i=1; i<= Config.MEDIA_UPLOAD_LIMIT; i++)
			int foundImages = 0;
			for(int i=1; i<= files.length; i++)
			{
				String photo = files[idx - i].toString();
				Log.i("photo to copy ", photo);
				if(photo.endsWith(".jpg")){
					CopyFileUtility.copyFile(photo, null, MediaType.Photo);
					recentFiles.add(files[idx - i]);
					foundImages++;
					if(foundImages >= Config.MEDIA_UPLOAD_LIMIT)
					{
						break;
					}
				}
			}
			
			
			return recentFiles;
		}
		@Override 
		protected void onPostExecute(Void nothing)
		{
			Log.d(DEBUG_TAG, "Indexes Updated.");
			//uploadNewMediaIndexes();
		}
		
	}
	/*
	private class UpdateMediaIndexTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			Log.d(DEBUG_TAG, "Updating Media");
			
			//get list of files in media directories
			File path = new File(dir + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();
			boolean deleted = deleteFiles(files);
			List<File> newFiles = getMostRecentFiles();
			for (File file : newFiles) {
				int start = file.toString().indexOf(Constants.MCI_MEDIA_PATH);
				String filePath = file.getAbsolutePath().substring(start);
				//TODO: need file name
				UploadMediaIndex.uploadMedia(MediaType.Photo, UUID.randomUUID().toString(), filePath, Config.deviceName, "Re-Indexed");
			}
						
			return null;
		}
		
		private boolean deleteFiles(File[] files)
		{
			Log.d(DEBUG_TAG, "Deleting Media");
			boolean success = true;
			for(int i=0;i<files.length; i++)
			{
				files[i].delete();
			}
			return success;
		}
		
		private List<File> getMostRecentFiles()
		{
			List<File> recentFiles = new ArrayList<File>();
			Log.d(DEBUG_TAG, "Getting most recent Media");
			//StringBuilder list = new StringBuilder();
			//list.append("Photos: ");
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
			File dir = new File(filePath);
			File[] files = dir.listFiles();
			
			
			int idx = files.length;
			//TODO: Test with video
			for(int i=1; i<= Config.MEDIA_UPLOAD_LIMIT; i++)
			{
				String photo = files[idx - i].toString();
				Log.i("photo to copy ", photo);
				CopyFileUtility.copyFile(photo, null, MediaType.Photo);
				recentFiles.add(files[idx - i]);
			}
			
			
			return recentFiles;
		}
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			//putActivityInBack();
		}
		
	}*/
}
