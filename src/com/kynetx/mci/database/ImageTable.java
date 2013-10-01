package com.kynetx.mci.database;

import java.sql.Connection;

import com.kynetx.mci.models.MCIBaseModel;

public class ImageTable extends MCIBaseTable
{

	public ImageTable(String tableName) {
		super(tableName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createTable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void dropTable(Connection cn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public MCIBaseModel insert(MCIBaseModel data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(MCIBaseModel data) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(MCIBaseModel data) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	

}
