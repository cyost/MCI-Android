package com.kynetx.mci.activities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
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
import com.kynetx.mci.models.MediaIndex;
import com.kynetx.mci.services.IndexingService;
import com.kynetx.mci.services.MCIVideoCaptureObserver;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class PlayMediaActivity extends Activity {

	private static final String DEBUG_TAG = "play-media";
	VideoView videoPlayer;
	ProgressBar progressBar;
	ImageView imageViewer;
	MediaIndex media;
	MediaPlayer mediaPlayer;
	String guid;
	Button btnDone;
	MediaController mc;
	boolean removeDone = false;
	String videoFile;
	TextView txtTitle = null;
	
	Bitmap photo = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_media);
		
		btnDone = (Button)findViewById(R.id.btnPlayDone);
		btnDone.setVisibility(Button.GONE);
		
		mc = new MediaController(this);
		Intent intent = this.getIntent();
		String mediaTitle = intent.getStringExtra("media-title");
		String url = intent.getStringExtra("media-url");
		String mediaType = intent.getStringExtra("media-type");
		guid = intent.getStringExtra(Constants.EXTRA_GUID);
		Log.d(DEBUG_TAG, "guid: " + guid);
		txtTitle = (TextView)findViewById(R.id.txtPlayMediaTitle);
		txtTitle.setText(mediaTitle);
		
		videoPlayer = (VideoView)findViewById(R.id.videoViewPlayer);
		videoPlayer.setVisibility(View.GONE);
		
		progressBar = (ProgressBar)findViewById(R.id.prog);
		progressBar.setVisibility(View.GONE);
		
		imageViewer = (ImageView)findViewById(R.id.imageViewPlayer);
		imageViewer.setVisibility(View.GONE);
		
		
		
		if(mediaType.equalsIgnoreCase("video"))
		{
			
			videoPlayer.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.VISIBLE);
			txtTitle.setText("Buffering video...");
			new DownloadVideoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
			
		}else if(mediaType.equalsIgnoreCase("photo"))
		{
			imageViewer.setVisibility(ImageView.VISIBLE);
			showPhoto(intent, url);
		}
		else{
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					removeDone = true;
					btnDone.setVisibility(Button.VISIBLE);
					mediaPlayer.release();
					mediaPlayer = null;
				}
			});
			playMusic(intent, url);
		}
		Toast.makeText(this, "Playing : " + mediaTitle, Toast.LENGTH_LONG).show();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play_media, menu);
		return true;
	}

	
	
	private void restartService()
	{
		Intent service = new Intent(IndexingService.MCI_SERVICE);
		service.putExtra(IndexingService.EXTRA_UPDATE_RATE, 10000);
		stopService(service);
		startService(service);
		Toast.makeText(this, "Service started...:", Toast.LENGTH_SHORT).show();
	}
	
	private void playVideo(Intent intent, String url)
	{
		btnDone.setVisibility(View.VISIBLE);
		txtTitle.setText("Playing Video...");
		videoFile = url;
		
		try{
			getWindow().setFormat(PixelFormat.TRANSLUCENT);
			MediaController mc = new MediaController(PlayMediaActivity.this);
			//mc.setAnchorView(videoPlayer);
			mc.setMediaPlayer(videoPlayer);
	        videoPlayer.setMediaController(mc);
			//videoPlayer.setVideoURI(uri);			
			//videoPlayer.setVideoPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/20131020_100338.mp4");
	        //videoPlayer.setVideoPath(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.MCI_MEDIA_PATH + "test.mp4");
	        videoPlayer.setVideoPath(url);
			//videoPlayer.requestFocus();
			//videoPlayer.postInvalidateDelayed(1000);
			
			videoPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					// TODO Auto-generated method stub
					progressBar.setVisibility(View.GONE);
					videoPlayer.start();
				}
			});
			
			videoPlayer.setOnErrorListener(new OnErrorListener(){
	            @Override
	            public boolean onError(MediaPlayer mp, int what, int extra) {
	            // TODO Auto-generated method stub
		            Toast.makeText(getBaseContext(), "Error occured", Toast.LENGTH_LONG).show();
		            progressBar.setVisibility(View.GONE);
		            return false;
	            }
	        });

					
		}catch(Exception e)
		{
			Log.e("Play Media", e.getMessage());
			restartService();
		}
		
	}
	
	private void playMusic(Intent intent, String url)
	{
	
		try{
			
			MediaController mc = new MediaController(this);
			
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			
			mediaPlayer.setDataSource(url);
			//mediaPlayer.prepareAsync();
			//mediaPlayer.prepare(); // might take long! (for buffering, etc)
			//mediaPlayer.start();
			//mc.setMediaPlayer(mediaPlayer);
			new PlayMusicTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, guid);
		}catch(Exception e)
		{
			Log.e("Play Media", e.getMessage());
		}
		
	}
	
	private void showPhoto(Intent intent, String url)
	{
		new ShowPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
		
		btnDone.setVisibility(View.VISIBLE);
	}
	
	
	
	public void btnDoneOnClick(View view)
	{
		if(photo != null)
		{
			photo.recycle();
			photo = null;
		}
		
		if(videoFile != null)
		{
			File file = new File(videoFile);
			file.delete();
		}
		removeMedia();
		Config.mediaPlaying = false;
	
		//imageViewer = null;
		this.finish();
		
		
	}
	
	private void removeMedia()
	{
		
		new RemoveMediaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, guid);
		
	}
	
	private void closeActivity()
	{
		this.finish();
	}
	
	private class PlayMediaTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... url) {			
			
			Uri uri = Uri.parse(url[0]);
			
			try{
				
		        videoPlayer.setMediaController(mc);
				videoPlayer.setVideoURI(uri);			
				videoPlayer.requestFocus();
				//videoPlayer.postInvalidateDelayed(1000);
				
				videoPlayer.start();
			}catch(Exception e)
			{
				Log.e("Play Media", e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			btnDone.setVisibility(Button.VISIBLE);
		}
	}
	
	//private class ShowPhotoTask extends AsyncTask<String, Void, Bitmap>{
	private class ShowPhotoTask extends AsyncTask<String, Void, Void>{

		@Override
		//protected Bitmap doInBackground(String... url) {
		protected Void doInBackground(String... url) {
			//Bitmap photo = null;
			try
			{
				DownloadImage(url[0]);
			
			}catch(Exception e)
			{
				Log.e(DEBUG_TAG, "Error: " + e.getMessage());
			}
			//return photo;
			return null;
		}
		
		private InputStream OpenHttpConnection(String urlString) throws IOException {
	        InputStream in = null;
	        int response = -1;

	        URL url = new URL(urlString);
	        URLConnection conn = url.openConnection();

	        if (!(conn instanceof HttpURLConnection))
	            throw new IOException("Not an HTTP connection");

	        try {
	            HttpURLConnection httpConn = (HttpURLConnection) conn;
	            httpConn.setAllowUserInteraction(false);
	            httpConn.setInstanceFollowRedirects(true);
	            httpConn.setRequestMethod("GET");
	            httpConn.connect();
	            response = httpConn.getResponseCode();
	            if (response == HttpURLConnection.HTTP_OK) {
	                in = httpConn.getInputStream();
	            }
	        } catch (Exception ex) {
	            throw new IOException("Error connecting");
	        }
	        return in;
	    }

	    //private Bitmap DownloadImage(String URL) {
		private void DownloadImage(String URL) {
	        //Bitmap bitmap = null;
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 4;
	        InputStream in = null;
	        try {
	            in = OpenHttpConnection(URL);
	            options.inJustDecodeBounds =false;
	            
	            photo = BitmapFactory.decodeStream(in, null, options);
	            //photo = BitmapFactory.decodeByteArray(in, 0, in.read());
	            in.close();
	            
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	        	restartService();
	            e1.printStackTrace();
	        }
	        //return bitmap;
	    }
	    
	    @Override
		//protected void onPostExecute(Bitmap photo)
	    protected void onPostExecute(Void nothing)
		{
			imageViewer.setImageBitmap(photo);
			btnDone.setVisibility(Button.VISIBLE);
			//photo.recycle();
	    	removeMedia();
		}
	}
	
	//test this
	
	private class DownloadVideoTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... url) {
			String file = null;
			try{
				file = DownloadVideo(url[0]);
			}catch(Exception e)
			{
				Log.e(DEBUG_TAG, "Error downloading video: " + e.getMessage());
			}
			return file;
		}
		
		private InputStream OpenHttpConnection(String urlString) throws IOException {
	        InputStream in = null;
	        int response = -1;

	        URL url = new URL(urlString);
	        URLConnection conn = url.openConnection();

	        if (!(conn instanceof HttpURLConnection))
	            throw new IOException("Not an HTTP connection");

	        try {
	            HttpURLConnection httpConn = (HttpURLConnection) conn;
	            httpConn.setAllowUserInteraction(false);
	            httpConn.setInstanceFollowRedirects(true);
	            httpConn.setRequestMethod("GET");
	            httpConn.connect();
	            response = httpConn.getResponseCode();
	            if (response == HttpURLConnection.HTTP_OK) {
	                in = httpConn.getInputStream();
	            }
	        } catch (Exception ex) {
	            throw new IOException("Error connecting");
	        }
	        return in;
	    }

	    //private Bitmap DownloadImage(String URL) {
		private String DownloadVideo(String URL) {
	        //Bitmap bitmap = null;
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inSampleSize = 4;
	        InputStream in = null;
	        File videoDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH );
	        File mciVideoFile = null;
	        try {
	            in = OpenHttpConnection(URL);
	            options.inJustDecodeBounds =false;
	            
	            BufferedInputStream inStream = new BufferedInputStream(in, 1024 * 5);
	            
	            if(!videoDir.exists())
	            {
	            	videoDir.mkdir();
	            }
	            mciVideoFile = new File(videoDir + "/mciVideo.mp4");
	            FileOutputStream outStream = new FileOutputStream(mciVideoFile);
	            byte[] buff = new byte[5 * 1024];
	            int len;
	            
	            while((len = inStream.read(buff)) != -1){
	            	outStream.write(buff, 0, len);
	            }
	            outStream.flush();
	            outStream.close();
	            inStream.close();
	            in.close();
	            
	        } catch (IOException e1) {
	            // TODO Auto-generated catch block
	        	restartService();
	            e1.printStackTrace();
	        }
	        //return videoDir + "/test.mp4";
	        return mciVideoFile.getAbsolutePath();
	    }
		
		@Override
		protected void onPostExecute(String file)
		{
			/*btnDone.setVisibility(View.VISIBLE);
			videoPlayer.setVisibility(VideoView.GONE);
			Toast.makeText(getBaseContext(), "Video Done", Toast.LENGTH_SHORT).show();
			removeMedia();*/
			playVideo(null, file);
			//removeMedia();
		}
	}

		
	
	private class PlayMusicTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... url) {
			//mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			//mediaPlayer.setDataSource(url[0]);
			try {
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // might take long! (for buffering, etc)
			
			return null;
		}
		
	}
	private class RemoveMediaTask extends AsyncTask<String, Void, String>
	{

		@Override
		protected String doInBackground(String... mediaGuid) {
			/*Method: POST
			URL: https://cs.kobj.net/sky/event/<deviceToken>/<eid>/cloudos/mciMediaPlayRemove/?_rids=a169x727
			Header: Kobj-Session:<deviceToken>
			Body:
			{
			    "mediaGUID": mediaGUID
			};*/
			//https://cs.kobj.net/sky/event/5D73FA42-09EF-11E3-AB7F-02089790E4B9/51236986/web/submit/?_rids=a169x727
			//String postUrl = "https://cs.kobj.net/sky/event/5D73FA42-09EF-11E3-AB7F-02089790E4B9/51236986/cloudos/mciMediaPlayRemove/?_rids=a169x727";
			String postUrl = "https://cs.kobj.net/sky/event/" + Config.deviceId + "/51236986/cloudos/mciMediaPlayRemove/?_rids=a169x727";
			StringBuilder json = new StringBuilder();
			
			json.append("{");
			json.append("\"mediaGUID\": \"" + guid + "\"");
			json.append("}");
			 
			//HttpClient client = HttpUtils.getNewHttpClient();
			HttpClient client = new DefaultHttpClient();
			HttpContext context= new BasicHttpContext();
			HttpResponse response = null;
			HttpPost request = null;
			
			try 
			{
				
				request = new HttpPost(postUrl);
				//request.addHeader("Kobj-Session", "5D73FA42-09EF-11E3-AB7F-02089790E4B9");
				request.addHeader("Kobj-Session", Config.deviceId);
				request.addHeader("Host", "cs.kobj.net");
				request.addHeader("content-type", "application/json");
				
				request.setEntity(new ByteArrayEntity(json.toString().getBytes())); //google Request bin
				response = client.execute(request);
				//Log.d(DEBUG_TAG, response.getStatusLine().toString() + " - " + response.getStatusLine().getReasonPhrase());
				Header[] headers = response.getAllHeaders();
				HttpParams rParams = response.getParams();
				
				for (Header header : headers) {
					Log.d(DEBUG_TAG, header.getName() + ": " + header.getValue());
				}
				response.getEntity().consumeContent();
				
				
			} catch (Exception e) {
				  Log.e(DEBUG_TAG, e.getMessage());
			}finally{
				
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			removeDone = true;	
			//Toast.makeText(getBaseContext(), "Media has been removed from play queue", Toast.LENGTH_SHORT).show();
			//closeActivity();
			
		}
	}
	
}
