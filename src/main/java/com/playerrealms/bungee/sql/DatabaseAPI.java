package com.playerrealms.bungee.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseAPI {

	private static HikariPool pool;
	
	public static void setup(String jdbc, String password){
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(jdbc);
		config.setUsername("playerrealms");
		config.setMaximumPoolSize(10);
		config.setPassword(password);
		config.setConnectionTestQuery("SELECT 1");
		
		pool = new HikariPool(config);
	}
	
	public static boolean isValid() {
		if(pool == null){
			return false;
		}
		try(Connection connection = getConnection()){
			return connection.isValid(5000);
		}catch (SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static int execute(String script, Object... args) throws SQLException{
		
		try(Connection connection = getConnection()){
			
			try(PreparedStatement ps = connection.prepareStatement(script)){
				for(int i = 0; i < args.length;i++){
					ps.setObject(i+1, args[i]);
				}
				
				return ps.executeUpdate();
			}
			
		}
		
	}
	
	public static List<QueryResult> query(String script, Object... args) throws SQLException{
		try(Connection connection = getConnection()){
			
			try(PreparedStatement ps = connection.prepareStatement(script)){
				for(int i = 0; i < args.length;i++){
					ps.setObject(i+1, args[i]);
				}
				
				try(ResultSet rs = ps.executeQuery()){
					
					List<QueryResult> result = new ArrayList<QueryResult>();
					
					while(rs.next()){
						result.add(new QueryResult(rs));
					}
					
					return Collections.unmodifiableList(result);
					
				}
			}
			
		}
	}
	
	private static Connection getConnection() throws SQLException{
		return pool.getConnection();
	}
	
}
