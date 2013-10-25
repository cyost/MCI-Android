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
			
			return "Done";
			
		}
		
	}
}
