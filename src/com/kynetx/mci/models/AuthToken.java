package com.kynetx.mci.models;

import java.util.Calendar;

public class AuthToken extends MCIBaseModel{
	
	private String token;
	private long createDate;
	private Calendar cCreateDate;
	
	public String getToken(){
		return token;
	}
	
	public void setToken(String value)
	{
		token = value;
	}
	
	public long getCreateDateAsLong()
	{
		return createDate;
	}
	
	public void setCreateDate(long value)
	{
		createDate = value;
		cCreateDate = Calendar.getInstance();
		cCreateDate.setTimeInMillis(value);
	}
}
