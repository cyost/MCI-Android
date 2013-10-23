package com.kynetx.mci.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.util.Log;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.config.Constants;
import com.kynetx.mci.models.Device;

public class ReadDeviceIdFile {

	private static final String DEBUG_TAG = "GET-DEVICE-FILE";
	
	public Device getDevice()
	{
		String path = Constants.MCI_MEDIA_PATH_ABSOLUTE + "/" + Constants.CHANNEL_FILE;
		Device device = new Device();
		
		File file = new File(path);
		
		if(file.exists() == true)
		{
						
			try {
				FileInputStream in = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				StringBuilder sb = new StringBuilder();
				int lineNum = 1;
				while((line = reader.readLine()) != null)
				{
					switch(lineNum)
					{
						case 1:
							Config.deviceId = line;
							device.setChannelId(line);
							break;
						case 2:
							Config.deviceName = line;
							device.setName(line);
							break;
					}
					lineNum ++;
					sb.append(line);
				}
				
				reader.close();
				in.close();
				//Config.deviceId = sb.toString();
			} catch (FileNotFoundException e) {
	
				Log.e(DEBUG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(DEBUG_TAG, e.getMessage());
			}
			
		}
		return device;
	}
	
	public void saveDevice(Device device)
	{
		String deviceGuid = device.getChannelId();
		String deviceName = "\n" + device.getName();
		Config.deviceId = deviceGuid;
		Config.deviceName = device.getName();
		//File mciDir = new File(dir);
		File mciDir = new File(Constants.MCI_MEDIA_PATH_ABSOLUTE);
		if(!mciDir.exists())
		{
			mciDir.mkdir();
		}
		
		File file = new File(mciDir, Constants.CHANNEL_FILE);
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			fo.write(deviceGuid.getBytes());
			
			fo.write(deviceName.getBytes());
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
	
}
