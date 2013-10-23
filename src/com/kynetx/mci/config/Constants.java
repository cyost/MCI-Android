package com.kynetx.mci.config;

import android.os.Environment;

public class Constants {

	public static final String EXTRA_MEDIA_TYPE = "media-type";
	public static final String EXTRA_MEDIA_PATH = "media-path";
	public static final String EXTRA_MEDIA_INDEX = "media-index";
	
	public static final String MCI_MEDIA_PATH = "mci_media";
	public static final String MCI_PHOTO_FOLDER = "mci_image";
	public static final String MCI_VIDEO_FOLDER = "mci_video";
	public static final String EXTRA_GUID = "guid";
	public static final int MEDIA_TYPE_PHOTO = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int MEDIA_TYPE_MUSIC = 3;
	public static final String MCI_MEDIA_PATH_ABSOLUTE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Constants.MCI_MEDIA_PATH;
	public static final String CHANNEL_FILE = "mciChannelId.txt";
	public static final String EXTRA_DEVICE_CHANNEL_ID = "extra_channel_id";
}
