package com.kynetx.mci.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import android.util.Log;

import com.kynetx.mci.config.Config;
import com.kynetx.mci.models.AuthToken;
import com.kynetx.mci.models.MCIBaseModel;


public class AuthTokenTable extends MCIBaseTable {

	private static final String TABLE_NAME = "auth_token";
	//private static final String PRIMARY_KEY = "auth_token_id";
	
	//Columns
	private static final String TOKEN = "token";
	private static final String CREATE_DATE = "create_date";
		
	
	public AuthTokenTable() {
		super(TABLE_NAME);
		if(Config.CREATE_TABLES){
			createTable();
		}
	}

	@Override
	public void createTable() {
		
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE Table " + TABLE_NAME );
		sql.append(" (");
		sql.append(primaryKey + " long autoincrement");
		sql.append(TOKEN + " text, ");
		sql.append(CREATE_DATE + " long ");
		sql.append(")");
		
		Connection cn = connect();
		dropTable(cn);
		
		execute(sql.toString(), cn);
		
		try {
			cn.close();
		} catch (SQLException e) {
			
		}
	}

	@Override
	public void dropTable(Connection cn) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		
		execute(sql, cn);	
		
	}

	@Override
	public MCIBaseModel insert(MCIBaseModel data) {
		
		StringBuilder sql = new StringBuilder();
		sql.append("Insert into " + TABLE_NAME + "(");
		sql.append(TOKEN + ", " + CREATE_DATE);
		sql.append(") values (?, ?)");
		
		PreparedStatement pStmt = null;
		
		AuthToken authToken = (AuthToken)data;
		Date createDate = new Date();
		Connection cn = connect();
		try {
			pStmt = cn.prepareStatement(sql.toString());
			cn.setAutoCommit(true);
			pStmt.setString(1, authToken.getToken());
			
			pStmt.setLong(2, createDate.getTime());
			execute(pStmt);
		} catch (SQLException e) {
			Log.e(TAG, e.getMessage());
		}finally
		{
			try {
				pStmt.close();
				cn.close();
			} catch (SQLException e) {
				
			}
			
		}
		long id = getIdValue(cn);
		authToken.setId(id);
		authToken.setCreateDate(createDate.getTime());
		
		return authToken;
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
