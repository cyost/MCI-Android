package com.kynetx.mci.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.UUID;

import com.kynetx.mci.R;
import com.kynetx.mci.config.Constants;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

public class CaptureMediaActivity extends Activity {
	
private static final String DEBUG_TAG = "photo_activity";
	
	public static final int CAMERA_REQUEST = 100;
	public static final int VIDEO_REQUEST = 200;
	/*public static final String FILE_PATH = "mci_images";
	public static final String FILE_NAME = "image_";*/
	public static final String FILE_PATH_ROOT = "mci_media";
	public static final String FILE_PATH_IMAGE = "mci_image";
	public static final String FILE_PATH_VIDEO = "mci_video";
	public static final String FILE_PATH_MUSIC = "mci_music";
	public static final String FILE_NAME_IMAGE = "image_";
	public static final String FILE_NAME_VIDEO = "video_";
	private ImageView imageView;
	private VideoView videoView;
	//private EditText _txtDescription;
	private Bitmap photo;
	private String videoFileName;
	private MediaRecorder mediaRecorder;
	String musicGuid;
	String musicFileName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media);
		imageView = (ImageView)findViewById(R.id.imageCaptured);
		videoView = (VideoView)findViewById(R.id.videoCaptured);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo, menu);
		return true;
	}

	public void btnTakePhotoClick(View view)
	{
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}
	
	public void btnRecordVideoClick(View view)
	{
		
/*		Intent videoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
		String guid = UUID.randomUUID().toString();
		String fileName = "video_" + guid.toString() + ".mp4";
		File videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() 
				+ "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/" + fileName);
		Uri videoUri = Uri.fromFile(videoFile);
		videoFileName =  FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/" + fileName;
		videoIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, videoUri);
		videoIntent.putExtra(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
		startActivityForResult(videoIntent, VIDEO_REQUEST);*/
//		
		Intent videoIntent = new Intent(this, RecordVideoActivity.class);
		startActivity(videoIntent);
//		
	}
	
	public void btnRecordAudioClick(View view){
		Button stop = (Button)findViewById(R.id.btnStopRecord);
		stop.setVisibility(Button.VISIBLE);
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		musicGuid = UUID.randomUUID().toString() ;
		musicFileName = "music_" + musicGuid + ".3gp";
		//musicFileName = "music_" + musicGuid + ".mp3";
		mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() 
				+ "/" + FILE_PATH_ROOT + "/" + FILE_PATH_MUSIC + "/" + musicFileName);
		mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() 
				+ "/" + FILE_PATH_ROOT + "/" + FILE_PATH_MUSIC;
		File dir = new File(filePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		try{
			mediaRecorder.prepare();
		}catch(Exception e)
		{
			Log.e(DEBUG_TAG, "Media Player Prepare failed: " + e.getMessage());
		}
		
		mediaRecorder.start();
	}
	
	public void btnStopOnClick(View view)
	{
		mediaRecorder.stop();
		mediaRecorder.release();
		mediaRecorder = null;
		
		Intent saveMedia = new Intent(this, SaveMediaActivity.class);
		String file =  FILE_PATH_ROOT + "/" + FILE_PATH_MUSIC + "/" + musicFileName;
		
		saveMedia.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_MUSIC);
		//saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, file.getAbsolutePath());
		saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, file);
		saveMedia.putExtra(Constants.EXTRA_GUID, musicFileName);
		startActivity(saveMedia);
		
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		//if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK)
		if(resultCode == RESULT_OK)
		{
			
			Intent saveMedia = new Intent(this, SaveMediaActivity.class);
			String file = null;
			switch(requestCode)
			{
				case CAMERA_REQUEST:
					String guid = UUID.randomUUID().toString();
					videoView.setVisibility(VideoView.GONE);
					imageView.setVisibility(ImageView.VISIBLE);
					photo = (Bitmap)data.getExtras().get("data");
					//Bitmap photoEnlarged = scaleBitMap(photo, 2400, 2400);
					imageView.setImageBitmap(photo);
					//_txtDescription.setVisibility(EditText.VISIBLE);
					//File file = saveImage(guid);
					file = saveImage(guid);
					saveMedia.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_PHOTO);
					//saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, file.getAbsolutePath());
					saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, file);
					saveMedia.putExtra(Constants.EXTRA_GUID, guid);
				
					break;
				case VIDEO_REQUEST:
					imageView.setVisibility(ImageView.GONE);
					videoView.setVisibility(ImageView.VISIBLE);
					
					/*Uri videoUri = data.getData();
					String path = videoUri.getPath();*/
					saveMedia.putExtra(Constants.EXTRA_MEDIA_TYPE, Constants.MEDIA_TYPE_VIDEO);
					
					saveMedia.putExtra(Constants.EXTRA_MEDIA_PATH, videoFileName);
					
					break;
			}
			
			startActivity(saveMedia);
		}
	}
	
	//private File saveImage(String guid)
	private String saveImage(String guid)
	{
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_IMAGE;
		//String filePath = "mnt/sdcard/"  + FILE_PATH_ROOT + "/" + FILE_PATH_IMAGE;
		///mnt/sdcard/mci_media/mci_image
		Log.d(DEBUG_TAG, filePath);
		Date createDate = new Date();
		String fileName = FILE_NAME_IMAGE + "_" + guid + ".png";
		String path = FILE_PATH_ROOT + "/" + FILE_PATH_IMAGE + "/" + fileName;
		
		File dir = new File(filePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			photo.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, e.getMessage());
		}
		//Toast.makeText(this, "file: " + filePath + "/" + fileName, Toast.LENGTH_LONG).show();
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, e.getMessage());
		}
		
		return path;
	}
	
	@SuppressWarnings("resource")
	private String saveVideo(Uri videoUri, String guid)
	{
		String path = videoUri.getPath();
		//String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO;
		//String sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/" + videoView.
		/*Log.d(DEBUG_TAG, filePath);
		Date createDate = new Date();
		File destination = new File(filePath);
		String fileName = FILE_NAME_VIDEO + "_" + guid + ".mp4";
		String path = FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/" + fileName;
		FileChannel src = null;
		FileChannel dst = null;
		
		File dir = new File(filePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		try {
			src = new FileInputStream(sourcePath).getChannel();
			dst = new FileOutputStream(destination).getChannel();
			dst.transferFrom(src, 0, src.size());
			
			//Log.i(TAG, "File Saved to " + _image.getFileName());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, e.getMessage());
		}
		//Toast.makeText(this, "file: " + filePath + "/" + fileName, Toast.LENGTH_LONG).show();
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(DEBUG_TAG, e.getMessage());
		}
		finally{
			try {
				src.close();
				dst.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}*/
		return path;
	}
	/*private File saveImage(){
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH;
		Log.d(TAG, filePath);
		Date createDate = new Date();
		String fileName = FILE_NAME + "3.png";
		
		File dir = new File(filePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(dir, fileName);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			_photo.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
			_image.setFileName(fileName);
			//Log.i(TAG, "File Saved to " + _image.getFileName());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		//Toast.makeText(this, "file: " + filePath + "/" + fileName, Toast.LENGTH_LONG).show();
		catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}
		
		return file;
	}*/
}
