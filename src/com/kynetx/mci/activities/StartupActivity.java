package com.kynetx.mci.activities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import com.kynetx.mci.models.MCIIndex;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.network.utils.MciSSLSocketFactory;
import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.services.AutoStartService;
import com.kynetx.mci.services.FileModificationService;
import com.kynetx.mci.services.IndexingService;
import com.kynetx.mci.services.MediaCheckService;
import com.kynetx.mci.services.PhotoReceiver;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.ReadDeviceIdFile;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class StartupActivity extends Activity {

	private static final String DEBUG_TAG = "startup-activity";
	//private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH;
	//private static final String CHANNEL_FILE = "mciChannelId.txt";
	String lastFile;
	Spinner spnDevices;
	Spinner spnIndexes;
	boolean firstLogin = true;
	//String path;
	boolean stop = false;
	LinearLayout layout;
	PhotoReceiver photoReceiver;
	int maxPhotos = 0;
	List<String> mediaGuids;
	Device device;
	Device oldDevice;
	List<MCIIndex> mciIndexes;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);
		layout = (LinearLayout)findViewById(R.id.startupLayout);
		//path = dir + "/" + CHANNEL_FILE;
		spnDevices = (Spinner)findViewById(R.id.spnSelectDevice);
		spnIndexes = (Spinner)findViewById(R.id.spnSelectIndex);
		spnIndexes.setOnItemSelectedListener(new OnIndexSelectedListener());
		//getDeviceId();
		Log.e(DEBUG_TAG, "In Startup");
		
		
		
		loadMCIIndexes();
			
		photoReceiver = new PhotoReceiver();
		//checkForNewMedia();
	
	}
	
	private void putActivityInBack()
	{
		layout.setVisibility(View.GONE);
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
	
	public void spnIndexClick(View view)
	{
		Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show();
	}
	public void spnSelectDeviceClick(View view)
	{
		
	}
	
	private void getDeviceId()
	{
		ReadDeviceIdFile deviceFileReader = new ReadDeviceIdFile();
		device = deviceFileReader.getDevice();
		oldDevice = device;
		if(device.getChannelId().isEmpty())
		{
			firstLogin = true;
		}else {
			firstLogin = false;
		}
			
		//getMediaList();
		
	}
	/*
	private void copyPhoto(List<String> files)
	{
		new CopyPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
	}
	*/
	private void getMediaList()
	{
		new GetMediaListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
/*	private void removeMediaIndexes(List<String> mediGuids)
	{
		new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mediGuids);
	} */
	private void removeMediaIndexes()
	{
		new RemoveMediaIndexesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	} 
	
	private void loadMCIIndexes()
	{
		mciIndexes = new ArrayList<MCIIndex>();
		MCIIndex mciIndex = new MCIIndex();
		mciIndex.setIndex("A2E3CC48-09EE-11E3-A275-7C5C1257AE36");
		mciIndex.setName("Ed Dev");
		mciIndex.setType("Dev");
		mciIndexes.add(mciIndex);
		
		mciIndex = new MCIIndex();
		mciIndex.setIndex("1FCEA696-230E-11E3-A7AA-D6A7E71C24E1");
		mciIndex.setName("Kynetx Dev");
		mciIndex.setType("Development");
		mciIndexes.add(mciIndex);
		
		mciIndex = new MCIIndex();
		mciIndex.setIndex("C7F668FC-2DF1-11E3-A1F7-6620F5C8F60F");
		mciIndex.setName("Fred Wilson");
		mciIndex.setType("Prod");
		mciIndexes.add(mciIndex);
		
		mciIndex = new MCIIndex();
		mciIndex.setIndex("2E0896B0-2DF2-11E3-AB9D-1699D61CF0AC");
		mciIndex.setName("Ben Goode");
		mciIndex.setType("Prod");
		mciIndexes.add(mciIndex);
		
		mciIndex = new MCIIndex();
		mciIndex.setIndex("6116CCAC-2DF2-11E3-8C5A-06B3E71C24E1");
		mciIndex.setName("Allison Sharp");
		mciIndex.setType("Prod");
		mciIndexes.add(mciIndex);
		
		mciIndex = new MCIIndex();
		mciIndex.setIndex("9A621674-2DF2-11E3-ACE6-6EA487B7806A");
		mciIndex.setName("Suzi Smith");
		mciIndex.setType("Prod");
		mciIndexes.add(mciIndex);
		
		
		ArrayAdapter<MCIIndex> dataAdapter = new ArrayAdapter<MCIIndex>(this, android.R.layout.simple_spinner_item, mciIndexes);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnIndexes.setAdapter(dataAdapter);
		
	}
	
	
	
	private void loadDevices(MCIIndex index)
	{
		new DownloadDevicesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, index.getIndex());
		
	}
	
	
	private void startAutoService()
	{
		Intent autoService = new Intent(AutoStartService.MCI_SERVICE);
		autoService.putExtra(Constants.EXTRA_DEVICE_CHANNEL_ID, device.getChannelId());
		startService(autoService);
	}
	
	private void startIndexingService()
	{
		Intent service = new Intent(IndexingService.MCI_SERVICE);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		startService(service);
		Toast.makeText(this, "Service started...:", Toast.LENGTH_SHORT).show();	
	}
	
		
	public void btnSaveOnClick(View view)
	{
		getMediaList();
		device = (Device) spnDevices.getSelectedItem();
		Toast.makeText(this, "Device: " + device.getName() + " - " + device.getChannelId(), Toast.LENGTH_SHORT).show();
		saveDeviceId(device);
		//getPhotos();
		layout.setVisibility(View.GONE);
		
		startAutoService();
		startIndexingService();
		
		putActivityInBack();
	}
	
	public void btnTestClick(View view)
	{
		Intent intent = new Intent(this, ServiceActivity.class);
		startActivity(intent);
	}
	
	private void saveDeviceId(Device device)
	{
		ReadDeviceIdFile deviceIdFile = new ReadDeviceIdFile();
		deviceIdFile.saveDevice(device);
		
		
	}
	
	
	public void getPhotos()
	{
		new GetPhotosTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
		
	}
	
	private void populateSpinner(List<Device> devices)
	{
		ArrayAdapter<Device> dataAdapter = new ArrayAdapter<Device>(this, android.R.layout.simple_spinner_item, devices);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDevices.setAdapter(dataAdapter);
	}

		
	private class OnIndexSelectedListener implements OnItemSelectedListener
	{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			// TODO Auto-generated method stub
			Toast.makeText(parent.getContext(), "Selected: " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
			MCIIndex index = (MCIIndex)parent.getItemAtPosition(pos);
			loadDevices(index);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class DownloadDevicesTask extends AsyncTask<String, Integer, List<Device>>{

		private String getJson = "";
		List<Device> devices = new ArrayList<Device>();
		@Override
		protected List<Device> doInBackground(String... params) {
			
			String json = "";	
			String indexId = params[0];
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			} 
			
			int i = 1;
			//boolean found = false;
			json = checkForDevices(client, indexId);
			
			return devices;
		}
		
		private String checkForDevices(HttpClient client, String mciIndexId){
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			try 
			{				
				request = new HttpGet("https://cs.kobj.net/sky/cloud/a169x727/mciMediaDevicesList");
				
				//request.addHeader("Kobj-Session", Config.GUID_TO_GET_DEVICE_LIST);
				request.addHeader("Kobj-Session", mciIndexId);
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
				
				//request.addHeader("Kobj-Session", Config.deviceId);
				if(oldDevice == null)
				{
					oldDevice = device;
				}
				request.addHeader("Kobj-Session", oldDevice.getChannelId());
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
		protected void onPostExecute(List<String> guids)
		{
			mediaGuids = guids;
			
			removeMediaIndexes();
		}
	}
	
	//private class RemoveMediaIndexesTask extends AsyncTask<List<String>, Integer, Void>
	private class RemoveMediaIndexesTask extends AsyncTask<Void, Integer, Void>
	{

		
		@Override
		protected Void doInBackground(Void... guids) {
			
			//String removeUrl = "https://cs.kobj.net/sky/event/"+ Config.deviceId +"/"+ Config.EID +"/cloudos/mciRemoveMedia/?_rids=a169x727";
			String removeUrl = "https://cs.kobj.net/sky/event/"+ oldDevice.getChannelId() +"/"+ Config.EID +"/cloudos/mciRemoveMedia/?_rids=a169x727";
			
			HttpClient client = new DefaultHttpClient();
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpPost request = null;
			List<String> removeGuids = mediaGuids;
			
			for (String guid : removeGuids) {
				
				StringBuilder json = new StringBuilder();
				
				json.append("{\"mediaGUID\": \"" + guid + "\"");
				json.append("}");
				
				try 
				{					
					request = new HttpPost(removeUrl);
					
					//request.addHeader("Kobj-Session", Config.deviceId);
					request.addHeader("Kobj-Session", oldDevice.getChannelId());
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
			//getMostRecentFiles();
			return null;
		}
		
		private boolean deleteFiles()
		{
			File path = new File(Constants.MCI_MEDIA_PATH_ABSOLUTE + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();
			boolean success = true;
			if(files != null){
				for(int i=0;i<files.length; i++)
				{
					files[i].delete();
				}
			}else {
				Log.e(DEBUG_TAG, "Files is null");
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
			Toast.makeText(getBaseContext(), "Setting Done to true", Toast.LENGTH_SHORT).show();
			Config.startDone = true;
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
	
	/*
	
	private class UpdateMediaIndexTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			
			//get list of files in media directories
			File path = new File(Constants.MCI_MEDIA_PATH_ABSOLUTE + "/" + Constants.MCI_PHOTO_FOLDER);
			File[] files = path.listFiles();
			boolean deleted = deleteFiles(files);
			List<File> newFiles = getMostRecentFiles();
			String filePath = "";
			for (File file : files) {
				int start = file.toString().indexOf(Constants.MCI_MEDIA_PATH);
				filePath = file.getAbsolutePath().substring(start);
				//TODO: need file name
				Bitmap bm = BitmapFactory.decodeFile(filePath);
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 10, bo	);
				byte[] bytes = bo.toByteArray();
				String thumb = Base64.encodeToString(bytes, Base64.DEFAULT);
				UploadMediaIndex.uploadMedia(MediaType.Photo, UUID.randomUUID().toString(), filePath, Config.deviceName, "Re-Indexed", thumb);
			}
						
			return null;
		}
		
		
		
		@Override
		protected void onPostExecute(Void nothing)
		{
			Config.startDone = true;
		}
		
	}
	*/
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
