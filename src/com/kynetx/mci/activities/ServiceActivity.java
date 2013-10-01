package com.kynetx.mci.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.kynetx.mci.R;
import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.services.IRemoteIndexInterface;
import com.kynetx.mci.services.IndexingService;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceActivity extends Activity implements ServiceConnection  {

private static final String DEBUG_TAG = "INDEXING-SERVICE";
	
	TextView txtInfo;
	IRemoteIndexInterface remoteInterface = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtInfo = (TextView)findViewById(R.id.txtInfo);
		getDeviceId();
		Log.i(DEBUG_TAG, "Starting INDEXING-ServiceControl activity. DeviceId: " + Config.deviceId);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void getDeviceId()
	{
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH + "/mciChannelId.txt";
		File file = new File(path);
		try {
			FileInputStream in = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while((line = reader.readLine()) != null)
			{
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
	}
	
	public void btnStartClick(View view)
	{
		txtInfo.setText("Starting service...");
		Intent service = new Intent(IndexingService.MCI_SERVICE);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		startService(service);
		
	}
	
	public void btnStopClick(View view)
	{
		stopService();
	}
	
	private void stopService()
	{
		try {
			remoteInterface.stopService();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, "Error stopping service: " + e.getMessage());
		}
		txtInfo.setText("Stopping Service...");
		Intent service = new Intent(IndexingService.MCI_SERVICE);
		Log.d(DEBUG_TAG, "Stopping service");
		stopService(service);
		service = null;
		txtInfo.setText("Service Stopped");
	}
	
	public void btnGetInfoClick(View view)
	{
		try {
			
			//MediaIndex result = remoteInterface.getMediaIndex();
			String json = remoteInterface.getJson();
			
			if(json.length() > 1){
			//if(remoteInterface.doWeHaveMedia() == true){
				//txtInfo.setText("Returned Media: " + json);
				List<MediaIndex> mediaList = remoteInterface.getMediaList();
				txtInfo.setText(mediaList.get(0).mediaTitle);
				txtInfo.setText("\n\nJSON: " + remoteInterface.getJson());
				Log.d(DEBUG_TAG, "Media Type: " + mediaList.get(0).mediaType);
				if(mediaList.get(0).mediaType.equalsIgnoreCase("Video"))
				{
					playVideo(mediaList.get(0));
				}else if(mediaList.get(0).mediaType.equalsIgnoreCase("photo")){
					showPhoto(mediaList.get(0));
				}else{
					playMusic(mediaList.get(0));
				}
						
			}else{
				txtInfo.setText("No Media in  your queue.");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		//stopService();
	}
	
	public void btnGetPhotosClick(View view)
	{
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
		txtInfo.setText(list.toString());
	}
	

	public void btnTestHandshakeClick(View view)
	{
		txtInfo.setText("Testing...");
		if(isNetworkAvailable()){
			new HandshakeTask().execute(Config.CLOUD_OS_CLIENT_ID);
		}else {
			txtInfo.setText("Netowrk is not available");
		}
	}
	
	public void btnCaptureMediaClick(View view)
	{
		Intent intent = new Intent(this, CaptureMediaActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		remoteInterface = IRemoteIndexInterface.Stub.asInterface(service);
        Log.d(DEBUG_TAG, "Interface bound.");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		remoteInterface = null;
        /*Button getLastLoc = (Button) findViewById(R.id.get_last);
        getLastLoc.setVisibility(View.GONE);*/
        Log.d(DEBUG_TAG, "Remote interface no longer bound");
	}

	@Override
    protected void onResume() {
        super.onResume();
        // get a link to our remote service
        bindService(new Intent(IRemoteIndexInterface.class.getName()), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        // remove the link to the remote service
        unbindService(this);
        super.onPause();
    }
    
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    } 
    
    private void playVideo(MediaIndex media)
	{
    	Intent playMediaIntent = new Intent(this, PlayMediaActivity.class);
    	playMediaIntent.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
    	//playMediaIntent.putExtra(Constants.EXTRA_MEDIA_INDEX, media);
    	playMediaIntent.putExtra("media-title", media.mediaTitle);
    	playMediaIntent.putExtra("media-url", media.mediaURL);
    	playMediaIntent.putExtra("media-type", media.mediaType);
    	playMediaIntent.putExtra(Constants.EXTRA_GUID, media.mediaGUID);
    	
    	startActivity(playMediaIntent);
    	/*
    	Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse(media.mediaURL), "video/mp4");
    	startActivity(intent);
    	*/
	}
    
    private void playMusic(MediaIndex media)
    {
    	Intent playMediaIntent = new Intent(this, PlayMediaActivity.class);
    	playMediaIntent.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_MUSIC);
    	playMediaIntent.putExtra("media-title", media.mediaTitle);
    	playMediaIntent.putExtra("media-url", media.mediaURL);
    	playMediaIntent.putExtra("media-type", media.mediaType);
    	playMediaIntent.putExtra(Constants.EXTRA_GUID, media.mediaGUID);
    	
    	startActivity(playMediaIntent);
    }
    
    private void showPhoto(MediaIndex media)
    {
    	Intent playMediaIntent = new Intent(this, PlayMediaActivity.class);
    	playMediaIntent.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_PHOTO);
    	//playMediaIntent.putExtra(Constants.EXTRA_MEDIA_INDEX, media);
    	playMediaIntent.putExtra("media-title", media.mediaTitle);
    	playMediaIntent.putExtra("media-url", media.mediaURL);
    	playMediaIntent.putExtra("media-type", media.mediaType);
    	playMediaIntent.putExtra(Constants.EXTRA_GUID, media.mediaGUID);
    	
    	startActivity(playMediaIntent);
    
    }
    
    /**
     * Temp code to test HttpUrlConnection
     * 
     */
    /*
    private class HandshakeTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				  URL url = new URL(Config.APP_URL);
				  HttpURLConnection con = (HttpURLConnection) url.openConnection();
				  
				  readStream(con.getInputStream());
				} catch (Exception e) {
					  e.printStackTrace();
				}
			
			return null;
		}
		
		private void readStream(InputStream in) {
			  BufferedReader reader = null;
			  try {
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			      Log.d("WEB-CALL", line);
			    }
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
			} 
    	
		@Override
		protected void onPostExecute(String json)
		{
			txtInfo.setText("Done testing");
		}
    }
    
    private class HandshakeTask2 extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			}
			HttpGet get = new HttpGet(Config.APP_URL);
			HttpPost post = new HttpPost();
			HttpResponse response = null;
			HttpUriRequest request = null;
			String result = null;
			try 
			{
				
				request = new HttpGet(Config.APP_URL);
				//request = new HttpPost(Config.APP_URL);
				//request.addHeader("Host", "cs.kobj.net");
				//request.addHeader("Cache-Control", "max-age=3600, proxy-revalidate");
				//response = client.execute(get);
				response = client.execute(request);
				Log.d(DEBUG_TAG, response.getStatusLine().toString() + " - " + response.getStatusLine().getReasonPhrase());
				Header[] headers = response.getAllHeaders();
				HttpParams rParams = response.getParams();
				//Log.d(DEBUG_TAG, response.get)
				for (Header header : headers) {
					Log.d(DEBUG_TAG, header.getName() + ": " + header.getValue());
				}
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				readStream(is);
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			
			return null;
		}
		
		private void readStream(InputStream in) {
			  BufferedReader reader = null;
			  try {
			    reader = new BufferedReader(new InputStreamReader(in));
			    String line = "";
			    while ((line = reader.readLine()) != null) {
			      Log.d(DEBUG_TAG, line);
			    }
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
			} 
    	
		@Override
		protected void onPostExecute(String json)
		{
			txtInfo.setText("Done testing");
		}
    }
    */
    private class CopyPhotoTask extends AsyncTask<List<String>, Integer, Integer>
    {
    	
    	int photoCount = 0;
		@Override
		protected Integer doInBackground(List<String>... photos) {
			
			List<String> toCopy = photos[0];
			
			for (String photo : toCopy) {
				CopyFileUtility.copyFile(photo, null, MediaType.Photo);
				photoCount++;
				if(photoCount > 9)
				{
					break;
				}
			}
			
			return photoCount;
		}
		
		@Override
		protected void onPostExecute(Integer c)
		{
			txtInfo.setText("Done copying " + c + " files.");
		}
		
    	
    }
    
    private class HandshakeTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			
			Log.d(DEBUG_TAG, "Starting service from activity.");
			HttpClient client = HttpUtils.getNewHttpClient();
			
			if(Build.VERSION.SDK_INT < 14)
			{
				HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
			}
			//HttpGet get = new HttpGet(Config.APP_URL);
			//HttpPost post = new HttpPost();
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpUriRequest request = null;
			String result = null;
			try 
			{
				
				request = new HttpGet(Config.APP_URL);
				//request = new HttpPost(Config.APP_URL);
				request.addHeader("Host", "cs.kobj.net");
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
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				readStream(is);
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			
			return null;
		}
		
		private void readStream(InputStream in) {
			  BufferedReader reader = null;
			  try {
				    reader = new BufferedReader(new InputStreamReader(in));
				    String line = "";
				    while ((line = reader.readLine()) != null) {
				      Log.d(DEBUG_TAG, line);
			    }
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
			} 
    	
		@Override
		protected void onPostExecute(String json)
		{
			txtInfo.setText("Done testing");
		}
		
		
    }
}
