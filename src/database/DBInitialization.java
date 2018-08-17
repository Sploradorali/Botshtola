package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import core.Configuration;

public class DBInitialization {
	public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String URL = "jdbc:derby:" + Configuration.getConfig().getDbPath();

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(DRIVER);
    	return DriverManager.getConnection(URL);
    }
}
