package com.kynetx.mci.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.models.Device;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.FileAccessLog;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

public class MCIFileObserver extends FileObserver{

	private static final String DEBUG_TAG = "MCIFileObserver";
	String absolutePath;
	boolean initialLoadDone = false;
	String deviceChannelId;
	List<String> deletedFiles;
	
	public MCIFileObserver(String path, String deviceId) {
		super(path, FileObserver.DELETE);
		absolutePath = path;
		this.deviceChannelId = deviceId;
		Log.e(DEBUG_TAG, "deviceId: " + this.deviceChannelId);
		deletedFiles = new ArrayList<String>();
		
	}

	@Override
	public void onEvent(int event, String path) {
		//deletedFiles = new ArrayList<String>();
		if (path == null) {
			return;
		}
		
		if ((FileObserver.DELETE & event)!=0) {
		
			FileAccessLog.accessLogMsg += absolutePath + "/" + path + " is deleted\n";
			Log.e(DEBUG_TAG, "file deleted: " + absolutePath + "/" + path);
			Log.e(DEBUG_TAG, "file deleted: "+ path);
			deletedFiles.add(path);
			getMediaList();
		}
		//the monitored file or directory was deleted, monitoring effectively stops
		if ((FileObserver.DELETE_SELF & event)!=0) {
			FileAccessLog.accessLogMsg += absolutePath + "/" + " is deleted\n";
		}
		
	}
	
	private void getMediaList()
	{
		new GetMediaListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	@SuppressWarnings("unchecked")
	private void removeMediaIndexes(List<MediaIndex> mediaIndexes)
	{
		if(mediaIndexes.size() > 0)
		{
			List<String> indexGuids = new ArrayList<String>();
			//TODO: need to change for multiple files
			//String fileToDelete = deletedFiles.get(0);
			String fileToDelete;
			for(int i=0; i<deletedFiles.size(); i++){
				fileToDelete = deletedFiles.get(i);
				for (MediaIndex index : mediaIndexes) {
					if(index.mediaURL.endsWith(fileToDelete)){
						indexGuids.add(index.mediaGUID);
						Log.e(DEBUG_TAG, " file and index: " + fileToDelete + " - " + index.mediaGUID);
					} 
				}
			}
			new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, indexGuids);
		}
	}
	
	
	/***
	 * AsyncTask that will get all of the media indexes from the cloud and
	 * find the index for the deleted files. When done it will call another 
	 * asynctask to remove the indexes from the cloud.
	 * @author pbs
	 *
	 */
	private class GetMediaListTask extends AsyncTask<Void, Integer, List<MediaIndex>>
	{
		String getMediaListUrl = "https://cs.kobj.net/sky/cloud/a169x727/mciListMedia";
		private String getJson = "";
		//private List<MediaIndex> indexes = new ArrayList<MediaIndex>(); 
		
		@Override
		protected List<MediaIndex> doInBackground(Void... params) {
				
			
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			} 
			
			int i = 1;
			
			List<MediaIndex> indexes = checkForMedia(client);
			
			return indexes;
			
			
		}
		
		private List<MediaIndex> checkForMedia(HttpClient client)
		{
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			//List<String> guids = new ArrayList<String>();
			String json;
			List<MediaIndex> indexes = new ArrayList<MediaIndex>();
			try 
			{				
				request = new HttpGet(getMediaListUrl);
				
				request.addHeader("Kobj-Session", deviceChannelId);
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
					indexes = readStream(is);
										
					return indexes;
				}
				
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			
			return indexes;
		}
		
		private List<MediaIndex> readStream(InputStream in) 
		{
			BufferedReader reader = null;
			StringBuilder json = new StringBuilder();
			List<MediaIndex> mediaIndexes = new ArrayList<MediaIndex>();
			
			try 
			{
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			    	//Log.d(DEBUG_TAG, line);
			    	json.append(line);
			    }	    
			    getJson = json.toString();
			    mediaIndexes = parseJson(getJson);
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
			return mediaIndexes;
		}
		
		private List<MediaIndex> parseJson(String json)
		{
			List<MediaIndex> media = new ArrayList<MediaIndex>();
			if(json.length() > 10)
			{
				try{
					
					JSONArray jsonArray = new JSONArray(json);
					MediaIndex index;
					for (int i=0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Log.d(DEBUG_TAG, "MediaGUID: " + jsonObject.getString("mediaGUID"));
						index = new MediaIndex();
						index.mediaGUID = jsonObject.getString("mediaGUID");
						index.mediaURL = jsonObject.getString("mediaURL");
						media.add(index);
					}
					
				}catch(JSONException e)
				{
					Log.e(DEBUG_TAG, "error parsing json: " + e.getMessage());
				}
			}
			return media;
		}
		
		@Override
		protected void onPostExecute(List<MediaIndex> mediaIndexes)
		{
			removeMediaIndexes(mediaIndexes);
		}
	}

	
	/***
	 * AsyncTask that will remove the MediaIndex of the deleted file or files
	 * @author pbs
	 *
	 */
	private class RemoveMediaIndexesTask extends AsyncTask<List<String>, Integer, Void>
	{

		List<String> mediaGuids = new ArrayList<String>();
		@Override
		protected Void doInBackground(List<String>... guids) {
			
			String removeUrl = "https://cs.kobj.net/sky/event/"+ deviceChannelId +"/"+ Config.EID +"/cloudos/mciRemoveMedia/?_rids=a169x727";
			
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
					
					//request.addHeader("Kobj-Session", Config.deviceId);
					request.addHeader("Kobj-Session", deviceChannelId);
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
			Log.e(DEBUG_TAG, "Indexes Updated.");
			
		}
		
	}

}
