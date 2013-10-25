package com.kynetx.mci.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.kynetx.mci.activities.GetPhotoActivity;
import com.kynetx.mci.utils.CopyFileUtility;
import com.kynetx.mci.utils.CopyFileUtility.MediaType;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
		Log.e("picture", "got picture");
		String uri = intent.getData().toString();
		String data = intent.getDataString();
		
		Log.i("picture", uri);
		Log.i("picture", data);
		//test(context);
		getPhoto(uri);
		
	}

	private void getPhoto(String uri)
	{
		new GetPhotoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		
	}
	
	private void test(Context context)
	{
		
		String[] projection = new String[]{MediaStore.Images.ImageColumns._ID,
				MediaStore.Images.ImageColumns.DATA,
				MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,MediaStore.Images.ImageColumns.DATE_TAKEN,
				MediaStore.Images.ImageColumns.DISPLAY_NAME,
				MediaStore.Images.ImageColumns.MIME_TYPE,
				MediaStore.Images.ImageColumns.DESCRIPTION};     
        
		//File file = context.getDatabasePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
		/*final Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"); 
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
        		
        	}*/
	}
	
	private class GetPhotoTask extends AsyncTask<String, Integer, Void>
	{

		@Override
		protected Void doInBackground(String... params) {
			StringBuilder list = new StringBuilder();
			list.append("Photos: ");
			String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
			String path2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
			File dir = new File(filePath);
			File[] files = dir.listFiles();
			String name;
			
			//Need to get entire list of files then sort by created date.
			//Each device can sort media in different orders.
			List<String> sortedList = new ArrayList<String>();
			for (File file : files) {
				//Log.e("photo", file.toString());
				Date dt = new Date(file.lastModified());
				name = file.getName();
				//Log.e("photo", "Name: " + name);
				sortedList.add(file.toString());
			}
			
			Collections.sort(sortedList);
			int size = sortedList.size();
			Log.e("photo", " first: " + sortedList.get(size - 1));
			
			String photo = null;
			boolean found = false;
			int idx = 1;
			while(found == false)
			{
				photo = sortedList.get(size - idx);
				if(photo.endsWith(".jpg")){
					found = true;
				}
				idx++;
			}
			Log.e("photo to copy ", photo);
			CopyFileUtility.copyFile(photo, null, MediaType.Photo);
					
			return null;
		}
		
	}
	
}
