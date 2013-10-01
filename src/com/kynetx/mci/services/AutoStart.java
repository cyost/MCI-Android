package com.kynetx.mci.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import com.kynetx.mci.activities.StartupActivity;
import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.models.Device;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class AutoStart extends BroadcastReceiver{

	private static final String DEBUG_TAG = "AutoStart";
	private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH;
	private static final String CHANNEL_FILE = "mciChannelId.txt";
	
	String path;
	boolean stop = false;
	boolean fileFound = false;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e(DEBUG_TAG, "In AutoStart");
		
		
		while(fileFound == false)
		{
			getDeviceId();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		Intent start = new Intent(context, StartupActivity.class);
		start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		context.startActivity(start);
		
		while(Config.startDone == false)
		{
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		/*
		reloadMediaIndexes();
		//
		*/
		//Create a new service called AutoStartService that will handle all of the reindexing
		startAutoStartService(context);
		startService(context);
	}
	
	private void getDeviceId()
	{
		path = dir + "/" + CHANNEL_FILE;
		
		File file = new File(path);
		Log.e(DEBUG_TAG, path);
		if(file.exists() == true)
		{					
			try {
				FileInputStream in = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				StringBuilder sb = new StringBuilder();
				int lineNum = 1;
				while((line = reader.readLine()) != null)
				{
					switch(lineNum)
					{
						case 1:
							Config.deviceId = line;
							break;
						case 2:
							Config.deviceName = line;
							break;
					}
					lineNum ++;
					sb.append(line);
				}
				Log.e(DEBUG_TAG, "DeviceId: " + Config.deviceId);
				Log.e(DEBUG_TAG, "Device Name: " + Config.deviceName);
				fileFound = true;
				reader.close();
				in.close();
		
			fileFound = true;
			Log.e(DEBUG_TAG, "File Found");
			} catch (FileNotFoundException e) {
	
				Log.e(DEBUG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(DEBUG_TAG, e.getMessage());
			}
			
		}
		
	}
	
	private void removeMediaIndexes(List<String> mediGuids)
	{
		Log.e(DEBUG_TAG, "Removing INdexes");
		new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediGuids);
	} 
	
	private void uploadNewMediaIndexes()
	{
		Log.e(DEBUG_TAG, "Upload New Media Indexes");
		new UpdateMediaIndexTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void reloadMediaIndexes()
	{
		Log.e(DEBUG_TAG, "Starting reload Media task");
		new GetMediaListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
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
			Log.e(DEBUG_TAG, "Checking for media");
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
						Log.d(DEBUG_TAG, jsonObject.getString("mediaGUID"));
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
			Log.d(DEBUG_TAG, "Done get Media Task");
			for (String guid : mediaGuids) {
				Log.d(DEBUG_TAG, "guid: " + guid);
			}
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
			//for (List<String> guid : removeGuids) {
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
					
			return null;
		}
		
		@Override 
		protected void onPostExecute(Void nothing)
		{
			uploadNewMediaIndexes();
		}
		
	}
	
	
	private class UpdateMediaIndexTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			//get list of files in media directories
			File path = new File(dir + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();		
			
			for (File file : files) {
				int start = file.toString().indexOf(Constants.MCI_MEDIA_PATH);
				String filePath = file.getAbsolutePath().substring(start);
				//TODO: need file name
				UploadMediaIndex.uploadMedia(MediaType.Photo, UUID.randomUUID().toString(), filePath, Config.deviceName, "Re-Indexed");
			}
			//call upload media index
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			//putActivityInBack();
		}
		
	}
	
	private void startService(Context context)
	{
		Intent service = new Intent(context, IndexingService.class);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		context.startService(service);
		Log.d(DEBUG_TAG, "Indexing Service started.");
		
	}

	private void startAutoStartService(Context context)
	{
		Intent service = new Intent(context, AutoStartService.class);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		context.startService(service);
		Log.d(DEBUG_TAG, "AutoStart Service started.");
		
	}
	
}
