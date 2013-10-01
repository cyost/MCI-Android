package com.kynetx.mci.utils;

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
import android.os.Environment;
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
			//UploadMediaIndex.uploadMedia(MediaType.Photo, guid, urlPath, mediaTitle, description);
			UploadMediaIndex.uploadMedia(MediaType.Photo, guid, urlPath, mediaTitle, description);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static String getFileFromFilePath(String path)
	{
		String fileName = "";
		
		int start = path.lastIndexOf("/");
		fileName = path.substring(start + 1);
		
		return fileName;
	}
}
