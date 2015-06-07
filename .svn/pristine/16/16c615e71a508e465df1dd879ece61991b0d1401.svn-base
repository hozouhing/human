package com.jeff.orm;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.jeff.util.PropertiesUtil;
import com.mysql.jdbc.Connection;

public class ConnectionProvider {

	static String driverName;
	static String url;
	static {
		driverName = (String) PropertiesUtil.getJdbcProp().get("driver");
		String database = (String) PropertiesUtil.getJdbcProp().get("database");
		String usename = (String) PropertiesUtil.getJdbcProp().get("username");
		String password = (String) PropertiesUtil.getJdbcProp().get("password");
		String others = (String) PropertiesUtil.getJdbcProp().getProperty(
				"others", "");
		url = (String) PropertiesUtil.getJdbcProp().get("url");
		url += "/";
		url += database;
		url += "?";
		url += "user=" + usename + "&password=" + password;
		if (!others.equals("")) {
			url += "&" + others;
		}
	}

	public static Connection get() throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Class.forName(driverName);
		conn = (Connection) DriverManager.getConnection(url);
		return conn;
	}

}
