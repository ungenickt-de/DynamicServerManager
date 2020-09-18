package com.playerrealms.bungee.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QueryResult {
	private Map<String, Object> objects;
	
	protected QueryResult(ResultSet set) throws SQLException {
		
		objects = new HashMap<>();
		
		ResultSetMetaData meta = set.getMetaData();
		
		for(int i = 1; i <= meta.getColumnCount();i++){
			String label = meta.getColumnName(i);
			
			objects.put(label, set.getObject(i));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String columnName){
		return (T) objects.get(columnName);
	}

	public boolean hasKey(String key) {
		return objects.containsKey(key) && objects.get(key) != null;
	}
}
