package com.kynetx.mci.models;

public abstract class MCIBaseModel {

	protected long _id;
	
	public long getId()
	{
		return _id;
	}
	
	public void setId(long value)
	{
		_id = value;
	}
}
