package com.kynetx.mci.utils;

import android.os.Environment;

/**
 * Class to handle all common Environment calls.
 * @author pbs
 *
 */
public class EnvironmentUtils {

	/**
	 * Check if the external storage is available
	 * @return true if it is, false if not.
	 */
	public static boolean isExternalStorageAvailable()
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	
}
