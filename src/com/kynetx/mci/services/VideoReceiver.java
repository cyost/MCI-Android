package com.kynetx.mci.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class VideoReceiver extends BroadcastReceiver  {

	private static final String DEBUG_TAG = "Video-Receiver";
	
	public VideoReceiver()
	{
		super();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(DEBUG_TAG, "got video");
	}

}
