package com.kynetx.mci.services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kynetx.mci.activities.GetPhotoActivity;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoReceiver extends BroadcastReceiver {

	
	
	public PhotoReceiver()
	{
		super();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i("picture", "got picture");
		String uri = intent.getData().toString();
		String data = intent.getDataString();
		
		Log.i("picture", uri);
		Log.i("picture", data);
		getPhoto();
		
	}

	private void getPhoto()
	{
		new GetPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}
	
	private class GetPhotoTask extends AsyncTask<Void, Integer, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			StringBuilder list = new StringBuilder();
			list.append("Photos: ");
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
			String path2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
			File dir = new File(filePath);
			File[] files = dir.listFiles();
			/*
			for (File file : files) {
				Log.e("photo", file.toString());
			}*/
			
			int idx = files.length;
			String photo = files[idx - 1].toString();
			Log.i("photo to copy ", photo);
			CopyFileUtility.copyFile(photo, null, MediaType.Photo);
					
			return null;
		}
		
	}
	
}
