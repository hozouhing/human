package com.jeff.orm;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.jeff.util.PropertiesUtil;
import com.mysql.jdbc.Connection;

public class ConnectionProvider {

	static String driverName;
	static String url;
	static {
		driverName = (String) PropertiesUtil.getProp().get("driver");
		String database = (String) PropertiesUtil.getProp().get("database");
		String usename = (String) PropertiesUtil.getProp().get("username");
		String password = (String) PropertiesUtil.getProp().get("password");
		String others = (String) PropertiesUtil.getProp().getProperty("others",
				"");
		url = (String) PropertiesUtil.getProp().get("url");
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
