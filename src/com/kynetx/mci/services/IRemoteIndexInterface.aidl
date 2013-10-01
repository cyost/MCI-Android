package com.kynetx.mci.services;

import com.kynetx.mci.models.MediaIndex;

interface IRemoteIndexInterface {
	
	MediaIndex getMediaIndex();
	String getJson();
	void stopService();
	List<MediaIndex> getMediaList();
	boolean doWeHaveMedia();
}