package com.kynetx.mci.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore.LoadStoreParameter;
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

import com.kynetx.mci.R;
//import com.kynetx.mci.R.layout;
//import com.kynetx.mci.R.menu;


import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.models.Device;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.services.IndexingService;
import com.kynetx.mci.services.MediaCheckService;
import com.kynetx.mci.services.PhotoReceiver;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class StartupActivity extends Activity {

	private static final String DEBUG_TAG = "startup-activity";
	private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH;
	private static final String CHANNEL_FILE = "mciChannelId.txt";
	String lastFile;
	Spinner spnDevices;
	boolean firstLogin = true;
	String path;
	boolean stop = false;
	LinearLayout layout;
	PhotoReceiver photoReceiver;
	int maxPhotos = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		layout = (LinearLayout)findViewById(R.id.startupLayout);
		path = dir + "/" + CHANNEL_FILE;
		spnDevices = (Spinner)findViewById(R.id.spnSelectDevice);
		getDeviceId();
		Log.e(DEBUG_TAG, "In Startup");
		if(firstLogin == true){
			
			loadDevices();
			
		}else {
			loadDevices();
			//spnDevices.setVisibility(View.GONE);
			reloadMediaIndexes();
			layout.setVisibility(View.GONE);
			//getPhotos();
			moveTaskToBack(true);
			this.finish();
		}
		
		startService();
		photoReceiver = new PhotoReceiver();
		//checkForNewMedia();
	
	}
	
	private void putActivityInBack()
	{
		moveTaskToBack(true);
		//Config.startDone = true;
		this.finish();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.startup, menu);
		return true;
	}
	
	public void spnSelectDeviceClick(View view)
	{
		
		/*Device selected = (Device)spnDevices.getSelectedItem();
		Toast.makeText(this, "Selected: "+ selected.getChannelId(), Toast.LENGTH_SHORT).show();*/
	}
	
	private void getDeviceId()
	{
		
		File file = new File(path);
		
		if(file.exists() == true)
		{
			firstLogin = false;
			
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
				
				reader.close();
				in.close();
				//Config.deviceId = sb.toString();
			} catch (FileNotFoundException e) {
	
				Log.e(DEBUG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(DEBUG_TAG, e.getMessage());
			}
			//updateMediaIndexes();
		}
		else{
			firstLogin = true;
			
		}
	}
	
	private void copyPhoto(List<String> files)
	{
		new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
	}
	
	private void updateMediaIndexes()
	{
		new GetMediaListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void removeMediaIndexes(List<String> mediGuids)
	{
		new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediGuids);
	} 
	
	private void uploadNewMediaIndexes()
	{
		new UpdateMediaIndexTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void loadDevices()
	{
		List<Device> devices = new ArrayList<Device>();
		
		new DownloadDevicesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}
	
	private void reloadMediaIndexes()
	{
		new GetMediaListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void startService()
	{
		Intent service = new Intent(IndexingService.MCI_SERVICE);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		startService(service);
		Toast.makeText(this, "Service started...:", Toast.LENGTH_SHORT).show();
		
		
	}
	
	
	
	public void btnSaveOnClick(View view)
	{
		Device device = (Device) spnDevices.getSelectedItem();
		Toast.makeText(this, "Device: " + device.getName() + " - " + device.getChannelId(), Toast.LENGTH_SHORT).show();
		saveDeviceId(device);
		getPhotos();
		layout.setVisibility(View.GONE);
	}
	
	public void btnTestClick(View view)
	{
		Intent intent = new Intent(this, ServiceActivity.class);
		startActivity(intent);
	}
	
	private void saveDeviceId(Device device)
	{
		String deviceGuid = device.getChannelId();
		String deviceName = "\n" + device.getName();
		Config.deviceId = deviceGuid;
		Config.deviceName = device.getName();
		File mciDir = new File(dir);
		if(!mciDir.exists())
		{
			mciDir.mkdir();
		}
		
		File file = new File(mciDir, CHANNEL_FILE);
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			fo.write(deviceGuid.getBytes());
			
			fo.write(deviceName.getBytes());
			fo.flush();
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	private void startThread()
	{
		while(stop == false)
		{

			String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
					MediaStore.Images.ImageColumns.DATA,
					MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,
					MediaStore.Images.ImageColumns.DISPLAY_NAME,
					MediaStore.Images.ImageColumns.MIME_TYPE,
					MediaStore.Images.ImageColumns.DESCRIPTION};     
			
			final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
	        if(cursor != null){
	            int count = 0;
	            List<String> files = new ArrayList<String>();
	        	//while(cursor.moveToNext())
	            if(cursor.moveToFirst())
	        	{
	        		
	        		if(cursor.getString(2).equalsIgnoreCase("camera"))
	        		{
		        		count++;
		        		int cols = cursor.getColumnCount();
		        		
		        		//list.append("\n");
		        		
		        		if(lastFile.equals(cursor.getString(1)))
		        		{
		        			//do nothing
		        		}else {
		        			files.add(cursor.getString(1));
		        			//new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
		        			copyPhoto(files);
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
	        }
		}
	*/
	
	public void getPhotos()
	{
		new GetPhotosTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		/*StringBuilder list = new StringBuilder();
		list.append("Photos: ");
		
		String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
				MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaStore.Images.ImageColumns.DISPLAY_NAME,
				MediaStore.Images.ImageColumns.MIME_TYPE,
				MediaStore.Images.ImageColumns.DESCRIPTION};     
        @SuppressWarnings("deprecation")
		final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
        if(cursor != null){
            int count = 0;
            List<String> files = new ArrayList<String>();
        	while(cursor.moveToNext())
        	{
        		
        		if(cursor.getString(2).equalsIgnoreCase("camera"))
        		{
	        		count++;
	        		int cols = cursor.getColumnCount();
	        		
	        		list.append("\n");
	        		for(int i = 0; i < cols; i++)
	        		{
	        			list.append(" ^ " + cursor.getString(i));
	        		}
	        		if(count == 1)
	        		{
	        			lastFile = cursor.getString(1);
	        		}
	        		files.add(cursor.getString(1));
        		}
		            // you will find the last taken picture here
		            // according to Bojan Radivojevic Bomber comment do not close the cursor (he is right ^^)
		            //cursor.close();
        		
        	}
        	
        	new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
        }
		
		String photoDirectory = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
		list.append("\nDir: " + photoDirectory);*/
		
	}
	
	private void populateSpinner(List<Device> devices)
	{
		ArrayAdapter<Device> dataAdapter = new ArrayAdapter<Device>(this, android.R.layout.simple_spinner_item, devices);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDevices.setAdapter(dataAdapter);
	}

	
	private class DownloadDevicesTask extends AsyncTask<Void, Integer, List<Device>>{

		private String getJson = "";
		List<Device> devices = new ArrayList<Device>();
		@Override
		protected List<Device> doInBackground(Void... params) {
			
			String json = "";	
			
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			} 
			
			int i = 1;
			//boolean found = false;
			json = checkForDevices(client);
			
			return devices;
		}
		
		private String checkForDevices(HttpClient client){
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			try 
			{				
				request = new HttpGet("https://cs.kobj.net/sky/cloud/a169x727/mciMediaDevicesList");
				
				request.addHeader("Kobj-Session", Config.GUID_TO_GET_DEVICE_LIST);
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
					String json = readStream(is);
					parseJson(json.toString());
					
					return json;
				}
				
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			return "";
		}
			
		
		@Override
		protected void onPostExecute(List<Device> devices)
		{
			populateSpinner(devices);
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


		
		private void parseJson(String json)
		{
			if(json.length() > 10)
			{
				try{
					devices = new ArrayList<Device>();
					JSONArray jsonArray = new JSONArray(json);
					
					for (int i=0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Log.d(DEBUG_TAG, jsonObject.getString("mciDeviceChannel"));
						Device device = new Device();
						device.setChannelId(jsonObject.getString("mciDeviceChannel"));
						device.setIconUrl(jsonObject.getString("mciDeviceIcon"));
						device.setName(jsonObject.getString("mciDeviceName"));
						
						devices.add(device);
					}
					
				}catch(JSONException e)
				{
					Log.e(DEBUG_TAG, "error parsing json: " + e.getMessage());
				}
			}
		}
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
			//uploadNewMediaIndexes();
		}
		
	}
	
	private class CopyPhotoTask extends AsyncTask<List<String>, Integer, Integer>
    {
    	
    	int photoCount = 0;
		@Override
		protected Integer doInBackground(List<String>... photos) {
			
			List<String> toCopy = photos[0];
			
			for (String photo : toCopy) {
				CopyFileUtility.copyFile(photo, null, MediaType.Photo);
				photoCount++;
				if(photoCount >= Config.MEDIA_UPLOAD_LIMIT)
				{
					break;
				}
			}
			
			return photoCount;
		}
		
		@Override
		protected void onPostExecute(Integer c)
		{
			Toast.makeText(getBaseContext(), "Done copying " + c + " files.", Toast.LENGTH_SHORT).show();
		}
    }
	
	
	
	private class UpdateMediaIndexTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			//get list of files in media directories
			File path = new File(dir + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();
			/*boolean deleted = deleteFiles(files);
			List<File> newFiles = getMostRecentFiles();*/
			String filePath = "";
			for (File file : files) {
				int start = file.toString().indexOf(Constants.MCI_MEDIA_PATH);
				filePath = file.getAbsolutePath().substring(start);
				//TODO: need file name
				UploadMediaIndex.uploadMedia(MediaType.Photo, UUID.randomUUID().toString(), filePath, Config.deviceName, "Re-Indexed");
			}
						
			return null;
		}
		
		/*private boolean deleteFiles(File[] files)
		{
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
			for(int i=1; i<= Config.MEDIA_UPLOAD_LIMIT; i++)
			{
				String photo = files[idx - i].toString();
				Log.i("photo to copy ", photo);
				CopyFileUtility.copyFile(photo, null, MediaType.Photo);
				recentFiles.add(files[idx - i]);
			}
			
			
			return recentFiles;
		}*/
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			//putActivityInBack();
		}
		
	}
	
	private class GetPhotosTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			StringBuilder list = new StringBuilder();
			list.append("Photos: ");
			
			String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
					MediaStore.Images.ImageColumns.DATA,
					MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,
					MediaStore.Images.ImageColumns.DISPLAY_NAME,
					MediaStore.Images.ImageColumns.MIME_TYPE,
					MediaStore.Images.ImageColumns.DESCRIPTION};     
	        @SuppressWarnings("deprecation")
			final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
	        if(cursor != null){
	            int count = 0;
	            List<String> files = new ArrayList<String>();
	        	while(cursor.moveToNext())
	        	{
	        		
	        		if(cursor.getString(2).equalsIgnoreCase("camera"))
	        		{
		        		count++;
		        		int cols = cursor.getColumnCount();
		        		
		        		list.append("\n");
		        		for(int i = 0; i < cols; i++)
		        		{
		        			list.append(" ^ " + cursor.getString(i));
		        		}
		        		if(count == 1)
		        		{
		        			lastFile = cursor.getString(1);
		        		}
		        		files.add(cursor.getString(1));
	        		}
			            // you will find the last taken picture here
			            // according to Bojan Radivojevic Bomber comment do not close the cursor (he is right ^^)
			            //cursor.close();
	        		
	        	}
	        	
	        	new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
	        }
			
			String photoDirectory = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
			list.append("\nDir: " + photoDirectory);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			putActivityInBack();
		}
	}
		

}
