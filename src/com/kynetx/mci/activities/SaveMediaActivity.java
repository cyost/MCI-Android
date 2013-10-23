package com.kynetx.mci.activities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
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

import com.kynetx.mci.R;
//import com.kynetx.mci.R.layout;
//import com.kynetx.mci.R.menu;
import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.network.utils.HttpUtils;
import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class SaveMediaActivity extends Activity {
	
	/*public static final String FILE_PATH_ROOT = "mci_media";
	public static final String FILE_PATH_IMAGE = "mci_image";
	public static final String FILE_PATH_VIDEO = "mci_video";
	public static final String FILE_PATH_MUSIC = "mci_music";
	public static final String FILE_NAME_IMAGE = "image_";*/

	private static final String DEBUG_TAG = "share-media";
	TextView textViewMediaType;
	ImageView imageView;
	VideoView videoView;
	EditText txtTitle;
	EditText txtDescription;
	String mediaPath;
	int extraMediaType;
	MediaType mediaType;
	//String mediaUrl;
	//private Bitmap photo;
	private String guid;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save_media);
		
		textViewMediaType = (TextView)findViewById(R.id.txtMediaType);
		imageView = (ImageView)findViewById(R.id.saveImageView);
		videoView = (VideoView) findViewById(R.id.saveVideoView);
		
		extraMediaType = this.getIntent().getIntExtra(Constants.EXTRA_MEDIA_TYPE, 0);
		mediaPath = this.getIntent().getStringExtra(Constants.EXTRA_MEDIA_PATH);
		guid = this.getIntent().getStringExtra(Constants.EXTRA_GUID);
		textViewMediaType.setText("Media: " + mediaType);
		txtDescription = (EditText)findViewById(R.id.txtDescription);
		txtTitle = (EditText)findViewById(R.id.txtTitle);
		
		switch(extraMediaType)
		{
			case Constants.MEDIA_TYPE_PHOTO:
				imageView.setVisibility(ImageView.VISIBLE);
				mediaType = MediaType.Photo;
				viewPhoto();
				break;
			case Constants.MEDIA_TYPE_VIDEO:
				videoView.setVisibility(VideoView.VISIBLE);
				String videoPath = this.getIntent().getStringExtra(Constants.EXTRA_MEDIA_PATH);
				mediaType = MediaType.Video;
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + videoPath);
				Uri videoUri = Uri.fromFile(file);
				viewVideo(videoUri);
				break;
			case Constants.MEDIA_TYPE_MUSIC:
				mediaType = MediaType.Music;
				break;
		}
		
		/*if(mediaType == Constants.MEDIA_TYPE_PHOTO)
		{
			imageView.setVisibility(ImageView.VISIBLE);
			viewPhoto();
		}else{
			videoView.setVisibility(VideoView.VISIBLE);
			String videoPath = this.getIntent().getStringExtra(Constants.EXTRA_MEDIA_PATH);
			
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + videoPath);
			Uri videoUri = Uri.fromFile(file);
			viewVideo(videoUri);
		}*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.save_media, menu);
		return true;
	}

	
	public void btnShareMediaClick(View view)
	{
		/*switch(mediaType)
		{
			case Constants.MEDIA_TYPE_PHOTO:
				shareMedia();
				break;
			case Constants.MEDIA_TYPE_VIDEO:
				shareMedia();
				break;
			case Constants.MEDIA_TYPE_MUSIC:
				shareMedia();
				break;
		}
		*/
		shareMedia();
	}
	
	private void viewVideo(Uri videoUri)
	{
		//Uri videoUri = Uri.parse(mediaPath);
		videoView.setVideoURI(videoUri);
		videoView.start();
	}
	
	private void viewPhoto()
	{
		Bitmap photo = BitmapFactory.decodeFile(mediaPath);
		imageView.setImageBitmap(photo);
	}
	
	private void shareMedia()
	{
		
		new ShareMediaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "My String");
	}
	
	
	//TODO: make sure this activity is no longer used.
	private class ShareMediaTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... authToken) {
			
			String mediaTitle = txtTitle.getText().toString();
			String description = txtDescription.getText().toString();
			Bitmap bm = BitmapFactory.decodeFile(mediaPath);
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 10, bo	);
			byte[] bytes = bo.toByteArray();
			String thumb = Base64.encodeToString(bytes, Base64.DEFAULT);
			UploadMediaIndex.uploadMedia(mediaType, guid, mediaPath, mediaTitle, description, thumb);
			/*String sMediaType = "";
			String mediaUrl= "";
			String ipAdress = HttpUtils.getIPAddress(true);

			switch(mediaType)
			{
				case Constants.MEDIA_TYPE_PHOTO:
					sMediaType = "Photo";
					//saveImage(guid.toString());
					mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
					break;
				case Constants.MEDIA_TYPE_VIDEO:
					sMediaType = "Video";
					mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
					break;
				case Constants.MEDIA_TYPE_MUSIC:
					sMediaType="Music";
					mediaUrl = "http://" + ipAdress + ":8080/" + mediaPath;
					break;
			}
			
			
			String mediaTitle = txtTitle.getText().toString();
			String description = txtDescription.getText().toString();
			
			//String postUrl = "https://cs.kobj.net/sky/event/9F99C8F8-16A5-11E3-A3F6-561A3BC979B4/51236986/web/submit/?_rids=a169x727&element=mciAddMedia.post";
			String postUrl = "https://cs.kobj.net/sky/event/" + Config.deviceId +"/51236986/web/submit/?_rids=a169x727&element=mciAddMedia.post";
			StringBuilder json = new StringBuilder();
			
			json.append("{\"mediaCoverArt\": \"https://s3.amazonaws.com/k-mycloud/a169x672/A709A4EA-F897-11E2-9738-89683970C0C4.img?q=88528\",");
			json.append("\"mediaGUID\": \"" + guid + "\",");
			json.append("\"mediaType\": \"" + sMediaType + "\",");
			json.append("\"mediaURL\":" + "\"" + mediaUrl +"\","); 
			json.append("\"mediaTitle\": \"" +mediaTitle + "\",");
			json.append("\"mediaDescription\": \"" + description + "\"");
			json.append("}");
			 
			//HttpClient client = HttpUtils.getNewHttpClient();
			HttpClient client = new DefaultHttpClient();
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpPost request = null;
			
			try 
			{
				
				request = new HttpPost(postUrl);
				//request.addHeader("Kobj-Session", "9F99C8F8-16A5-11E3-A3F6-561A3BC979B4");
				request.addHeader("Kobj-Session", Config.deviceId);
				request.addHeader("Host", "cs.kobj.net");
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
			*/
			return "Done";
			
		}
		
		
		
		/*private void readStream(InputStream in) 
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
		}*/
	}
}
