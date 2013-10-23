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
		
		String deviceId = "";
		while(fileFound == false)
		{
			deviceId = getDeviceId();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		startAutoStartService(context, deviceId);
		startService(context, deviceId);
	}
	
	private String getDeviceId()
	{
		path = dir + "/" + CHANNEL_FILE;
		String deviceId = "";
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
							deviceId = line;
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
		return deviceId;
	}
	
	private void startService(Context context, String deviceId)
	{
		Intent service = new Intent(context, IndexingService.class);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		context.startService(service);
		Log.d(DEBUG_TAG, "Indexing Service started.");
		
	}

	private void startAutoStartService(Context context, String deviceId)
	{
		Intent service = new Intent(context, AutoStartService.class);
		service.putExtra(Constants.EXTRA_DEVICE_CHANNEL_ID, deviceId);
		
		context.startService(service);
		Log.d(DEBUG_TAG, "AutoStart Service started.");
		
	}
	
}
