package com.jeff.orm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.jeff.util.PropertiesUtil;
import com.mysql.jdbc.Connection;

public class OrmContext {

	private static OrmContext ormContext;

	private static final Logger logger = Logger.getLogger(OrmContext.class);

	private List<Connection> connections = new ArrayList<Connection>();

	private static ThreadLocal<Connection> connection = new ThreadLocal<>();

	private List<Connection> getConnections() {
		return connections;
	}

	public static OrmContext getInstance() {
		if (ormContext == null) {
			ormContext = new OrmContext();
			try {
				ormContext.initContext();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ormContext;
	}

	private void initContext() throws ClassNotFoundException, SQLException {
		logger.info("Begin to initialize the orm context...");
		int poolSize = Integer.valueOf((String) PropertiesUtil.getProp()
				.get("poolSize"));
		logger.info("The jdbc connection pool size is " + poolSize + "...");
		logger.info("Begin to initialize the jdbc connection pool...");
		for (int i = 0; i < poolSize; i++) {
			connections.add(ConnectionProvider.get());
			logger.info("Succeed to set up the Num " + i + " connection...");
		}
		logger.info("JDBC connnection pool has been initialized successfully...");
		logger.info("Orm context has been initialized successfully...");
	}

	public void release() {
		for (Connection c : this.getConnections()) {
			try {
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void push() {
		Connection connection = connections.get(0);
		setCurrentConnection(connection);
		connections.remove(connection);
	}

	public void pop() {
		Connection connection = getCurrentConnection();
		connections.add(connection);
	}

	public static Connection getCurrentConnection() {
		return OrmContext.connection.get();
	}

	public static void setCurrentConnection(Connection connection) {
		OrmContext.connection.set(connection);
	}

}
