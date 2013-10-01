package com.kynetx.mci.models;

import android.os.Parcel;
import android.os.Parcelable;

public final class MediaIndex implements Parcelable {

	public String index;
	public String mediaURL;
	public String mediaDescription;
	public String mediaGUID;
	public String mediaTitle;
	public String mediaType;
	
	public static final Parcelable.Creator<MediaIndex> CREATOR = new Parcelable.Creator<MediaIndex>() {

		@Override
		public MediaIndex createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new MediaIndex(source);
		}

		@Override
		public MediaIndex[] newArray(int size) {
			// TODO Auto-generated method stub
			return new MediaIndex[size];
		}
		
	}; 
	
	
	public MediaIndex(){
		
	}
	
	private MediaIndex(Parcel source)
	{
		
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(index);
		dest.writeString(mediaURL);
		dest.writeString(mediaDescription);
		dest.writeString(mediaGUID);
		dest.writeString(mediaTitle);
		dest.writeString(mediaType);
	}
	
	public void readFromParcel(Parcel source)
	{
		index = source.readString();
		mediaURL = source.readString();
		mediaDescription = source.readString();
		mediaGUID = source.readString();
		mediaTitle = source.readString();
		mediaType = source.readString();
	}

}
