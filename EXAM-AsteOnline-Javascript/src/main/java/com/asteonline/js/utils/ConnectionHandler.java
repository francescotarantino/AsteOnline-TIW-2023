package com.asteonline.js.utils;

import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class ConnectionHandler {
	/**
	 * This method is used to get a connection to the database.
	 * @param context the servlet context
	 * @return a Connection object
	 * @throws UnavailableException error while trying to connect to the database
	 */
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection connection = null;
		try {
			String driver = context.getInitParameter("DB_DRIVER");
			String url = context.getInitParameter("DB_URL");
			String user = context.getInitParameter("DB_USER");
			String password = context.getInitParameter("DB_PASSWORD");
			
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("There is an error loading database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get database connection");
		}
		
		return connection;
	}
}
