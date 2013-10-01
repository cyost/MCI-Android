package com.kynetx.mci.services;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kynetx.mci.activities.PlayMediaActivity;
import com.kynetx.mci.activities.ServiceActivity;
import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.database.AuthTokenTable;
import com.kynetx.mci.models.AuthToken;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.os.AsyncTask;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

public class IndexingService extends Service {

	private static final int HTTP_NOTIFY = 0x2001;
	private static final String DEBUG_TAG = "MOBILE_CLOUD-SERVICE";
    public static final String EXTRA_UPDATE_RATE = "update-rate";
	private int updateRate = 5000; //every 5 seconds
    public static final String MCI_SERVICE = "com.kynetx.mci.services.IndexingService.SERVICE";
    //TODO: put in a config file or class
    public static final String GET_MEDIA_LIST_URL = "https://cs.kobj.net/sky/cloud/a169x727/mciListMedia";
    public static final String GET_MEDIA_PLAY_LIST_URL = "https://cs.kobj.net/sky/cloud/a169x727/mciMediaPlayList";
    public static final String HEADER_KOBJ = "Kobj-Session";
    public static final String HOST = "cs.kobj.net";
    
    private NotificationManager notifier = null;
    //private String ipAddress;
   
    DownloadMediaTask downloadMediaTask = null;
    CheckForNewMediaTask checkForNewMediaTask = null;
    private String url;
    boolean stop = false;
    boolean hasMedia = false;
	List<MediaIndex> mediaIndexes;
    String authToken = "";
    private String getJson = "";
    Intent startIntent;
    
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
        startObserver();
       
        return Service.START_REDELIVER_INTENT;
    }

    private void startObserver()
    {
    	FileObserver observer = new FileObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()) 
		{
			
			@Override
			public void onEvent(int event, String file) {
				// TODO Auto-generated method stub
				String fileSaved;
				 if(event == FileObserver.CREATE && !file.equals(".probe")){ // check if its a "create" and not equal to .probe because thats created every time camera is launched
		                Log.d(DEBUG_TAG, "File created [" + MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + file + "]");
		                fileSaved = "New photo Saved: " + file;
		            }
			}
		};
		observer.startWatching();
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
        // Pre Android 2.0 version
        //super.onStart(intent, startId);
        Log.v(DEBUG_TAG, "onStart() called, must be on L3 or L4");
        doServiceStart(intent, startId);
    }
    
    public void doServiceStart(Intent intent, int startId)
    {
    	Log.d(DEBUG_TAG, "starting service " + startId + "....");
    	//updateRate = intent.getIntExtra(EXTRA_UPDATE_RATE, -1);
    	if(updateRate == -1)
    	{
    		//updateRate = 60000;
    	}
    	
    	if(authToken.length() < 3){
			getAuthToken();
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
        notify.setLatestEventInfo(getApplicationContext(), "MCI Service Started",
            "Service start.", intentBack);
        notifier.notify(HTTP_NOTIFY, notify);
        Log.d(DEBUG_TAG, "Service Started.");
        stop = false;
        startDownloadinMedia();
        //startCheckForNewMedia();
    }
    
    private void startDownloadinMedia(){
		
    	downloadMediaTask = new DownloadMediaTask();
		stop = false;
		downloadMediaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
	}
    
    private void startCheckForNewMedia(){
		
    	checkForNewMediaTask = new CheckForNewMediaTask();
		//stop = false;
    	checkForNewMediaTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        downloadMediaTask = null;
        super.onDestroy();
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return remoteIndexInterface;
	}
	
	
	private void playMedia()
	{
		if(Config.mediaPlaying == false)
		{
			if(mediaIndexes.size() > 0)
			{
				MediaIndex media = mediaIndexes.get(0);
				Intent intent = new Intent(getBaseContext(), PlayMediaActivity.class);
				
				intent.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_PHOTO);
		    	//playMediaIntent.putExtra(Constants.EXTRA_MEDIA_INDEX, media);
				intent.putExtra("media-title", media.mediaTitle);
				intent.putExtra("media-url", media.mediaURL);
				intent.putExtra("media-type", media.mediaType);
				intent.putExtra(Constants.EXTRA_GUID, media.mediaGUID);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Config.mediaPlaying = true;
				startActivity(intent);
			}
		}
	}
	
	private final IRemoteIndexInterface.Stub remoteIndexInterface = new IRemoteIndexInterface.Stub() {

		@Override
		public MediaIndex getMediaIndex() throws RemoteException {
			// TODO Auto-generated method stub
			MediaIndex mediaIndex = new MediaIndex();
			mediaIndex.index = UUID.randomUUID().toString();
			return mediaIndex;
		}

		@Override
		public void stopService() throws RemoteException {
			// TODO Auto-generated method stub
			stop = true;
		}
		
		@Override
		public String getJson(){
			return getJson;
		}
		
		public List<MediaIndex> getMediaList()
		{
			return mediaIndexes;
		}
		
		public boolean doWeHaveMedia()
		{
			return hasMedia;
		}
		
	}; 
	
	/**
	 * May store the authtoken in the config class as a constant for now.
	 * 
	 */
	private void getAuthToken(){
		
		Log.i(DEBUG_TAG, "Getting authToken");
		//AuthTokenTable authTable = new AuthTokenTable();
		authToken = Config.CLOUD_OS_CLIENT_ID;
		//AuthToken token = new AuthToken();
		//token.setToken(authToken);
		//authTable.insert(token);
		Log.i(DEBUG_TAG, "authToken: " + authToken);
		
	}
	
	private class DownloadMediaTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... authToken) {
			
			String json = "";
			
				
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			} 
			
			int i = 1;
			//boolean found = false;
			while(stop == false){
				Log.d(DEBUG_TAG, "Checking server: " + i++);
				if(Config.deviceId != null)
				{
					json = checkForMedia(client);
				}
				try {
					Thread.sleep(updateRate);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//stop = true;
			
			return json;
		}
		
		private String checkForMedia(HttpClient client){
			//hasMedia = false;
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			try 
			{
				
				request = new HttpGet(GET_MEDIA_PLAY_LIST_URL);
				//request = new HttpPost(Config.APP_URL);
				//request.addHeader("Kobj-Session", Config.ANDROID_DEVICE_CHANNEL);
				request.addHeader("Kobj-Session", Config.deviceId);
				request.addHeader("content-type", "application/json");
				//request.addHeader("Cache-Control", "max-age=3600, proxy-revalidate");
				//response = client.execute(get);
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
					String json = readStream(is);
					parseJson(json.toString());
					hasMedia = true;
					return json;
				}
				
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			return "";
		}
		
		private String readStream(InputStream in) 
		{
			BufferedReader reader = null;
			StringBuilder json = new StringBuilder();
			try 
			{
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	//Log.d(DEBUG_TAG, line);
			    	json.append(line);
			    }	    
			    getJson = json.toString();
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
			return json.toString();
		}
		
		@Override
		protected void onPostExecute(String json)
		{
			playMedia();
		}
		
		private void parseJson(String json)
		{
			if(json.length() > 10)
			{
				try{
					mediaIndexes = new ArrayList<MediaIndex>();
					JSONArray jsonArray = new JSONArray(json);
					for (int i=0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Log.d(DEBUG_TAG, jsonObject.getString("mediaURL"));
						MediaIndex media = new MediaIndex();
						media.index = jsonObject.getString("mediaGUID");
						//media.mediaDescription = jsonObject.getString("mediaDescription");
						media.mediaGUID = jsonObject.getString("mediaGUID");
						media.mediaTitle = jsonObject.getString("mediaTitle");
						media.mediaType = jsonObject.getString("mediaType");
						media.mediaURL = jsonObject.getString("mediaURL");
						//Log.e(DEBUG_TAG, media.mediaURL);
						mediaIndexes.add(media);
					}
					playMedia();
				}catch(JSONException e)
				{
					Log.e(DEBUG_TAG, "error parsing json: " + e.getMessage());
				}
			}
		}
	}
	
	private class CheckForNewMediaTask extends AsyncTask<Void, Void, String>
	{

		@Override
		protected String doInBackground(Void... params) {
			
			while(stop == false)
			{
				Log.e("checking", "for new media");
				
				
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
