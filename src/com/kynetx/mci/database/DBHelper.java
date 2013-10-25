package com.kynetx.mci.database;

import com.kynetx.mci.config.Config;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String TAG = "DB-INDEXING";
	public static final String DATABASE_NAME = "mobile_cloud_index.db";
	public static final int VERSION = 1;
	
	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public DBHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		if(Config.IS_DEBUG){
			Log.i(TAG, "CREATING TABLES");
		}
		createTables(db);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion 
				+ " which will destroy old data. ");
		
		createTempTables(db);
		createTables(db);
		onCreate(db);
		
	}
	
	private void createTempTables(SQLiteDatabase db)
	{
		/*
		createTempTable(RetirementHome.TABLE_NAME, db);
		createTempTable(User.TABLE_NAME, db);
		createTempTable(HomeAmenitiesTable.TABLE_NAME, db);
		createTempTable(HomeImageTable.TABLE_NAME, db);
		*/
	}
	
	private void createTempTable(String tableName, SQLiteDatabase db)
	{
		String tempHome = tableName + "_temp";
		String drop = "Drop table if exists " + tempHome;
		db.execSQL(drop);
		
		String create = "CREATE TABLE " + tempHome + " as select * from " + tableName;
		db.execSQL(create);
	}
	
	private void createTables(SQLiteDatabase db)
	{
		
		/*try{
			AuthTokenTable tokenTable = new AuthTokenTable(db, this.context);
			tokenTable.createTable();
			
		}catch(SQLException e)
		{
			Log.e(TAG, e.getMessage());
		}*/
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}
}
