package com.kynetx.mci.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.UUID;


import com.kynetx.mci.network.utils.UploadMediaIndex;
import com.kynetx.mci.config.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

public class CopyFileUtility {

	public enum MediaType {Photo, Video, Music }
	
	public static final String DEBUG_TAG = "copy-file";
	public static final String FILE_PATH_ROOT = "mci_media";
	public static final String FILE_PATH_IMAGE = "mci_image";
	public static final String FILE_PATH_VIDEO = "mci_video";
	public static final String FILE_PATH_MUSIC = "mci_music";
	public static final String FILE_NAME_IMAGE = "image_";
	public static final String FILE_NAME_VIDEO = "video_";
	
	
	public static String copyFile(String fromPath,  String fileName, MediaType type)
	{
		String guid = UUID.randomUUID().toString();
		switch(type){
			case Photo:
				copyPhoto(fromPath, guid);
				break;
			case Video:
				copyVideo(fromPath, guid);
			default:
				break;
		}
		
		return guid;
	}
	
	private static void copyPhoto(String fromPath, String guid)
	{
		//String copyFileName = FILE_NAME_IMAGE + fileName;
		//Get Enviornment external storage
		
		String copyFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_IMAGE;
		
		FileChannel source = null;
		FileChannel dest = null;
		File sourceFile = new File(fromPath);
		String fileName = getFileFromFilePath(fromPath);
		String copyFile = copyFilePath + "/"  + fileName;
		File destFile = new File(copyFile);
		
		File dir = new File(copyFilePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		try {
			source = new FileInputStream(sourceFile).getChannel();
			dest = new FileOutputStream(destFile).getChannel();
			if(dest != null && source != null)
			{
				dest.transferFrom(source, 0, source.size());
			}
			if(source != null){
				source.close();
			}
			if(dest != null)
			{
				dest.close();
			}
			
			String description = "Copied from device";
			String mediaTitle;
			if (Config.deviceName.length()>1)
			{
				mediaTitle = Config.deviceName;
			}else
			{
				mediaTitle = "Photo From Device";
			}
			String urlPath = FILE_PATH_ROOT + "/" + FILE_PATH_IMAGE + "/" + fileName;
			
			String thumb = createThumbnailBase64(destFile);
			UploadMediaIndex.uploadMedia(MediaType.Photo, guid, urlPath, mediaTitle, description, thumb);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			try {
				if(source != null){
					source.close();
				}
				if(dest != null)
				{
					dest.close();
				}
				//dest.close();
				//source.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
	}
	
	private static String createThumbnailBase64(File source)
	{
		final int THUMBNAIL_SIZE = 150;
		FileInputStream fis = null;
		String thumb = "";
		
		try{
			fis =  new FileInputStream(source);
			
			Bitmap bm = BitmapFactory.decodeStream(fis);
			//BitmapFactory.Options bfo = new BitmapFactory.Options();
			bm = Bitmap.createScaledBitmap(bm, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			boolean success = bm.compress(Bitmap.CompressFormat.JPEG, 100, bo);
			byte[] bytes = bo.toByteArray();
			thumb = Base64.encodeToString(bytes, Base64.NO_WRAP); 
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return thumb;
	}
	
	private static String createThumbnailBase64(Bitmap source)
	{
		final int THUMBNAIL_SIZE = 150;
		FileInputStream fis = null;
		String thumb = "";
		
		try{
			/*fis =  new FileInputStream(source);
			
			Bitmap bm = BitmapFactory.decodeStream(fis);*/
			//BitmapFactory.Options bfo = new BitmapFactory.Options();
			source = Bitmap.createScaledBitmap(source, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			boolean success = source.compress(Bitmap.CompressFormat.JPEG, 100, bo);
			byte[] bytes = bo.toByteArray();
			thumb = Base64.encodeToString(bytes, Base64.NO_WRAP); 
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return thumb;
	}
	
	
	
	private static String getFileFromFilePath(String path)
	{
		String fileName = "";
		
		int start = path.lastIndexOf("/");
		fileName = path.substring(start + 1);
		
		return fileName;
	}
	
	private static void copyVideo(String fromPath, String guid)
	{
		
		
		String copyFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO;
		
		FileChannel source = null;
		FileChannel dest = null;
		File sourceFile = new File(fromPath);
		String fileName = getFileFromFilePath(fromPath);
		String copyFile = copyFilePath + "/"  + fileName;
		File destFile = new File(copyFile);
		
		File dir = new File(copyFilePath);
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		
		try {
			source = new FileInputStream(sourceFile).getChannel();
			dest = new FileOutputStream(destFile).getChannel();
			if(dest != null && source != null)
			{
				dest.transferFrom(source, 0, source.size());
			}
			if(source != null){
				source.close();
			}
			if(dest != null)
			{
				dest.close();
			}
			
			String description = "Video from device";
			String mediaTitle;
			if (Config.deviceName.length()>1)
			{
				mediaTitle = Config.deviceName;
			}else
			{
				mediaTitle = "Video From Device";
			}
			String urlPath = FILE_PATH_ROOT + "/" + FILE_PATH_VIDEO + "/" + fileName;
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 1;
			
			Bitmap curThumb = ThumbnailUtils.createVideoThumbnail(copyFile, MediaStore.Video.Thumbnails.MICRO_KIND);
			
			String thumb = createThumbnailBase64(curThumb);
			//String thumb = "";
			UploadMediaIndex.uploadMedia(MediaType.Video, guid, urlPath, mediaTitle, description, thumb);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally
		{
			try {
				if(source != null){
					source.close();
				}
				if(dest != null)
				{
					dest.close();
				}
				//dest.close();
				//source.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
	}
}
