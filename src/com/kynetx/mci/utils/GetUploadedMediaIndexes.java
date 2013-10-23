package com.kynetx.mci.utils;

import java.io.BufferedReader;
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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.util.Log;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.models.Device;
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.network.utils.HttpUtils;

public class GetUploadedMediaIndexes {

	private static final String DEBUG_TAG = "GET-UPLOADED-INDEXES";
	private static final String getMediaListUrl = "https://cs.kobj.net/sky/cloud/a169x727/mciListMedia";
	private String getJson = "";

	private List<MediaIndex> mediaIndexes;
	public GetUploadedMediaIndexes()
	{
		mediaIndexes = new ArrayList<MediaIndex>();
		//downLoadMediaIndexes();
	}
	
	public List<MediaIndex> getMediaIndexes()
	{
		return downLoadMediaIndexes();
	}
	
	protected List<MediaIndex> downLoadMediaIndexes() {
			
		
		HttpClient client = HttpUtils.getNewHttpClient();
		
		if(Build.VERSION.SDK_INT < 14)
		{
			HttpUtils.workAroundReverseDnsBugInHoneycombAndEarlier(client);
		} 
		
		List<MediaIndex> indexes = checkForMedia(client);
		return indexes;
		
	}
	
	private List<MediaIndex> checkForMedia(HttpClient client)
	{
		HttpContext context= new BasicHttpContext();
		HttpResponse response = null;
		HttpUriRequest request = null;
		List<MediaIndex> indexes = new ArrayList<MediaIndex>();
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
				indexes = readStream(is);
				//parseJson(json.toString());
				
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
		List<MediaIndex> indexes = new ArrayList<MediaIndex>();
		
		try 
		{
		    reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";
		    while ((line = reader.readLine()) != null) {
		    	//Log.d(DEBUG_TAG, line);
		    	json.append(line);
		    }	    
		    getJson = json.toString();
		    indexes = parseJson(getJson);
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
		return indexes;
	}
	
	private List<MediaIndex> parseJson(String json)
	{
		/*
		 * {
        "mediaCoverArt": "https://s3.amazonaws.com/k-mycloud/a169x672/A709A4EA-F897-11E2-9738-89683970C0C4.img?q=88528",
        "mediaGUID": "1DDE8D87-5C60-4B40-AEDB-DDD89B0D2655",
        "mediaType": "Video",
        "mediaURL": "http://www.youtube.com/watch?v=l-qSATlrlMA",
        "mediaTitle": "Kid Rock",
        "mediaDescription": " Kid Rock tribute to Johnny Cash"

		 */
		List<MediaIndex> media = new ArrayList<MediaIndex>();
		if(json.length() > 10)
		{
			try{
				media = new ArrayList<MediaIndex>();
				JSONArray jsonArray = new JSONArray(json);
				MediaIndex index;
				for (int i=0; i<jsonArray.length(); i++) {
					index = new MediaIndex();
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					Log.d(DEBUG_TAG, "MediaGUID: " + jsonObject.getString("mediaGUID"));
					Device device = new Device();
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
	
}
