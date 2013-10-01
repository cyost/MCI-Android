package com.kynetx.mci.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.util.Log;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.models.MCIBaseModel;



public abstract class MCIBaseTable {

	//protected SQLiteDatabase _db;
	//protected DBHelper dbHelper = null;
	//Connection cn = null;
	public static final String TAG = "MCIBaseTable";
	
	private static final String CONNECTION_STRING = "jdbc:sqlite:mci.mci_db";
	protected static String tableName = "";
	protected static String primaryKey = "";
	//protected Context context = null;
	
	
	public MCIBaseTable(String tableName)
	{
		this.tableName = tableName;
		this.primaryKey = tableName + "_id";
		
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			if(Config.IS_DEBUG)
			{
				Log.e(TAG, e.getMessage());
			}
		}
		
		/*this.tableName = tableName.toLowerCase();
		this.primaryKey = tableName.toLowerCase() + "_id";
		this.context = context;*/
		
	}
	
	protected abstract void createTable(); 
	protected abstract void dropTable(Connection cn);
	public abstract MCIBaseModel insert(MCIBaseModel data);
	public abstract int update(MCIBaseModel data);
	public abstract int delete(MCIBaseModel data);
	
	
	protected boolean execute(PreparedStatement stmt){
		
		boolean success = false;	
		try {
			
			success = stmt.execute();
			
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}finally{
			try {
				stmt.close();
			} catch (SQLException e) {
				
			}
		}
		
		return success;
	}
	
	protected boolean execute(String sql, Connection cn){
		
		boolean success = false;	
		Statement stmt = null;
		try {
			stmt = cn.createStatement();
			success = stmt.execute(sql);
			
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}finally{
			try {
				stmt.close();
			} catch (SQLException e) {
				
			}
		}
		
		return success;
	}
	
	protected long getIdValue(Connection cn)
	{
		long id = 0;
		Statement stmt = null;
		boolean success = false;
		
		String idSql = "Select max( " + primaryKey + ") from " + tableName;
		try {
			stmt = cn.createStatement();
			success = stmt.execute(idSql);
			ResultSet result =  stmt.getResultSet();
			id = result.getLong(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}finally
		{
			try {
				stmt.close();
			} catch (SQLException e) {
				
			}
		}
		
	
		
		return id;
	}
	
	protected Connection connect()
	{
		Connection cn = null;
		
		try {
			cn = DriverManager.getConnection(CONNECTION_STRING);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return cn;
				
	}
	
	protected void closeConnection(Connection cn)
	{
		try {
			cn.close();
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
}
