package com.kynetx.mci.database;

import java.sql.Connection;

import com.kynetx.mci.models.MCIBaseModel;

public class ImageTable extends MCIBaseTable
{

	public ImageTable(String tableName) {
		super(tableName);
	}

	@Override
	protected void createTable() {
		
	}

	@Override
	protected void dropTable(Connection cn) {
		
	}

	@Override
	public MCIBaseModel insert(MCIBaseModel data) {
		return null;
	}

	@Override
	public int update(MCIBaseModel data) {
		return 0;
	}

	@Override
	public int delete(MCIBaseModel data) {
		return 0;
	}
	
	

}
