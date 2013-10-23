package com.kynetx.mci.network.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

public class UploadMediaIndex {
	
	private static final String DEBUG_TAG = "upload-media-index"
;
	public static boolean uploadMedia(MediaType mediaType, String guid, String mediaPath, String mediaTitle, String description, String thumb)
	{
		String sMediaType = "";
		String mediaUrl= "";
		String ipAdress = HttpUtils.getIPAddress(true);

		switch(mediaType)
		{
			case Photo:
				sMediaType = "Photo";
				//saveImage(guid.toString());
				mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
				break;
			case Video:
				sMediaType = "Video";
				mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
				break;
			case Music:
				sMediaType="Music";
				mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
				break;
		}
		
		
		//String postUrl = "https://cs.kobj.net/sky/event/9F99C8F8-16A5-11E3-A3F6-561A3BC979B4/51236986/web/submit/?_rids=a169x727&element=mciAddMedia.post";
		String postUrl = "https://cs.kobj.net/sky/event/" + Config.deviceId +"/51236986/web/submit/?_rids=a169x727&element=mciAddMedia.post";
		StringBuilder json = new StringBuilder();
		//Bitmap bm = BitmapFactory.decodeFile(mediaUrl.toString());
		//ByteArrayOutputStream bo = new ByteArrayOutputStream();
		//bm.compress(Bitmap.CompressFormat.JPEG, 100, bo	);
		//byte[] bytes = bo.toByteArray();
		//String thumb = Base64.encodeToString(bytes, Base64.DEFAULT); 
		//json.append("{\"mediaCoverArt\": \"https://s3.amazonaws.com/k-mycloud/a169x672/A709A4EA-F897-11E2-9738-89683970C0C4.img?q=88528\",");
		json.append("{\"mediaCoverArt\": \"data:image/png;base64," + thumb +"\", ");
		//TODO: use base 64 for thumbnail
		//json.append("{\"mediaCoverArt\":"  + "\"" + mediaUrl +"\",");
		//json.append("{\"mediaCoverArt\":"  + "\"" + thumb +"\",");
		//json.append("{\"mediaCoverArt\":"  + "\"" + thumb +"\",");
		json.append("\"mediaGUID\": \"" + guid + "\",");
		json.append("\"mediaType\": \"" + sMediaType + "\",");
		json.append("\"mediaURL\":" + "\"" + mediaUrl +"\","); 
		json.append("\"mediaTitle\": \"" +mediaTitle + "\",");
		json.append("\"mediaDescription\": \"" + description + "\"");
		json.append("}");
		 
		//Log.e("json", json.toString());
		//HttpClient client = HttpUtils.getNewHttpClient();
		HttpClient client = new DefaultHttpClient();
		HttpContext context= new BasicHttpContext();
		HttpResponse response = null;
		HttpPost request = null;
		//writetoFile(json.toString());
		try 
		{
			
			request = new HttpPost(postUrl);
			//request.addHeader("Kobj-Session", "9F99C8F8-16A5-11E3-A3F6-561A3BC979B4");
			request.addHeader("Kobj-Session", Config.deviceId);
			request.addHeader("Host", "cs.kobj.net");
			//request.addHeader("content-type", "application/json;application/x-www-form-urlencoded;charset=UTF-8");//change to form encoded mime type: application/x-www-form-urlencoded
			request.addHeader("content-type", "application/json");//change to form encoded mime type: application/x-www-form-urlencoded
			
			request.setEntity(new ByteArrayEntity(json.toString().getBytes())); //google Request bin
			response = client.execute(request);
			//Log.d(DEBUG_TAG, response.getStatusLine().toString() + " - " + response.getStatusLine().getReasonPhrase());
			Header[] headers = response.getAllHeaders();
			HttpParams rParams = response.getParams();
			
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
		
		return true;
	
	}
	private static void writetoFile(String json)
	{
		
		//File mciDir = new File(dir);
		File mciDir = new File(Constants.MCI_MEDIA_PATH_ABSOLUTE);
		if(!mciDir.exists())
		{
			mciDir.mkdir();
		}
		
		File file = new File(mciDir, "jsonPost.txt");
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			fo.write(json.getBytes());
			
			
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
	
	
	private static void readStream(InputStream in) 
	{
		BufferedReader reader = null;
		StringBuilder json = new StringBuilder();
		try 
		{
		    reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";
		    while ((line = reader.readLine()) != null) {
		    	Log.d(DEBUG_TAG, line);
		    	json.append(line);
		    }	    
		    //getJson = json.toString();
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
}
