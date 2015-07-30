package replica;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Store {

	/** Constructor 
	 * Connects to a database and creates a table. 
	 */
	public Store() {
		Connection c = null;
		Statement stmt = null;
		try {
			// Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Open a connection
			c = DriverManager.getConnection("jdbc:sqlite:kvstore.db");

			// Check the table has already created  
			DatabaseMetaData md = c.getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				if (rs.getString(3).equals("TPC")) {
					c.close();
					return;
				}
			}

			// Execute a query
			stmt = c.createStatement();
			String sql = "CREATE TABLE TPC " +
					"(ID INTEGER PRIMARY KEY   AUTOINCREMENT   NOT NULL," +
					" KEY            CHAR(50) NOT NULL UNIQUE," +
					" VALUE          CHAR(50) NOT NULL)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			// Handle errors for Class.forName and handle errors for JDBC
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}

	/** Insert a new key or update a key in the database
	 * @param args key 
	 * @param args val value of the key
	 * @return true if everything is done, otherwise returns false 
	 */
	public boolean put(String key, String val) throws Exception {
		Connection c = null;
		Statement stmt = null;
		int insert = 0;
		try {
			// Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Open a connection
			c = DriverManager.getConnection("jdbc:sqlite:kvstore.db");
			c.setAutoCommit(false);

			// Execute a query
			stmt = c.createStatement();
			String sql = "INSERT OR REPLACE INTO TPC (KEY,VALUE) VALUES ( '"+ key +"', '" + val + "' );"; 
			insert = stmt.executeUpdate(sql);
		} catch ( Exception e ) {
			// Handle errors for Class.forName and handle errors for JDBC
			if (e.getMessage().contains("UNIQUE")) 
				return false;
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			throw e;
		} finally {
			try {
				stmt.close();
				c.commit();
				c.close();
			} catch (SQLException e) {
				// Handle errors for JDBC
				e.printStackTrace();
			}
		}
		if (insert > 0)
			System.out.println("Stored ("+key+","+val+")");
		return true;
	}

	/** Get the value of the key from the database
	 * @param key
	 * @return the value of the key
	 */
	public String get(String key) {
		String value = null;
		Connection c = null;
		Statement stmt = null;
		try {
			// Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Open a connection
			c = DriverManager.getConnection("jdbc:sqlite:kvstore.db");
			c.setAutoCommit(false);

			// Execute a query
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM TPC WHERE KEY='"+key+"';" );
			if (rs.next()) {
				value = rs.getString("value");
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			// Handle errors for Class.forName
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
		System.out.println("Retrieved ("+key+","+value+")");
		return value;

	}

	/** Delete a key in the database
	 * @param key
	 * @return true if everything is done otherwise returns false
	 */
	public boolean delete(String key) {		//TODO catch error if not found and do not log
		Connection c = null;
		Statement stmt = null;
		try {
			// Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Open a connection
			c = DriverManager.getConnection("jdbc:sqlite:kvstore.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");

			// Execute a query
			stmt = c.createStatement();
			String sql = "DELETE from TPC where KEY='"+ key +"';";
			stmt.executeUpdate(sql);
			c.commit();

			stmt.close();
			c.close();
		} catch ( Exception e ) {
			// Handle errors for Class.forName
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			return false;
		}
		System.out.println("Deleted "+key);
		return true;
	}

	/** Get all keys and values from the database
	 * @return list of available keys and values in the database
	 */
	public Map<String,String> load() {
		String value = null;
		String key = null;
		Connection c = null;
		Statement stmt = null;
		Map<String,String> db = new ConcurrentHashMap<>();
		try {
			// Register JDBC driver
			Class.forName("org.sqlite.JDBC");
			// Open a connection
			c = DriverManager.getConnection("jdbc:sqlite:kvstore.db");
			c.setAutoCommit(false);

			// Execute a query
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT * FROM TPC;" );
			while (rs.next()) {
				key = rs.getString("key");
				value = rs.getString("value");
				db.put(key, value);
				System.out.println(key + value);
			}
			rs.close();
			stmt.close();
			c.close();
		} catch ( Exception e ) {
			// Handle errors for Class.forName
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
		System.out.println("Database was loaded");
		return db;

	}
}
