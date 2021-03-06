package pt.uminho.sysbio.common.database.connector.datatypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySQL_Schemas {

	private String username;
	private String password;
	private String host;
	private String port;


	/**
	 * Set a new MySQL_Schemas Instance
	 * 
	 * @param username
	 * @param password
	 * @param host
	 * @param port
	 */
	public MySQL_Schemas(String username, String password, String host, String port){

		this.username = username;
		this.password =password;
		this.host = host;
		this.port = port;
	}

	/**
	 * @param multiThreadConnection
	 */
	public MySQL_Schemas(MySQLMultiThread multiThreadConnection) {
		this.username = multiThreadConnection.get_database_user();
		this.password = multiThreadConnection.get_database_password();
		this.host = multiThreadConnection.get_database_host();
		this.port = multiThreadConnection.get_database_port();
	}

	/**
	 * Creates a connection to a given schema, on the MySQL database, once provided with the following parameters.
	 * 
	 * @param schema
	 * @return
	 */
	private Connection createConnection(String schema){

		String driver_class_name = "com.mysql.jdbc.Driver";
		String url_db_connection = "jdbc:mysql://"+this.host+":"+this.port+"/"+schema;
		Connection connection = null;

		try {

			Class.forName(driver_class_name).newInstance();
			connection = (Connection) DriverManager.getConnection(url_db_connection,this.username , this.password);
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		catch (InstantiationException e) {

			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {

			e.printStackTrace();
		}
		catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
		return connection;

	}

	/**
	 Creates a connection to a default schema, on the MySQL database, once provided with the following parameters.
	 * 
	 * @return
	 * @throws SQLException 
	 */
	private Connection createConnection() throws SQLException{

		String driver_class_name = "com.mysql.jdbc.Driver";

		String url_db_connection = "jdbc:mysql://"+this.host+":"+this.port;//+"/mysql";
		
		Connection connection = null;
		try  {
			
			Class.forName(driver_class_name).newInstance();
			connection = (Connection) DriverManager.getConnection(url_db_connection,this.username , this.password);
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return connection;
	}

	/**
	 * Close the connection to a given schema, on the MySQL database.
	 * 
	 * @param connection
	 */
	private void closeConnection(Connection connection){

		try {

			connection.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * Creates a Schema in the MySQL database.
	 * 
	 * @param connection
	 * @param schema
	 */
	private Connection createSchema(Connection connection, String schema){

		Statement statement = null;
		try {

			statement = connection.createStatement();
			statement.execute( "CREATE DATABASE IF NOT EXISTS " + schema + " CHARACTER SET utf8 COLLATE utf8_bin");
			statement.close();
			
			return this.createConnection(schema);
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks whether a table exists or not in a given database.
	 * 
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public boolean checkTable(String schema, String table) {

		Connection connection = this.createConnection(schema);
		Statement statement = null;
		ResultSet rs = null;
		boolean result = false;

		try {

			statement = connection.createStatement();

			statement.execute( "show tables like '"+table+"'");
			rs=statement.getResultSet();

			result = rs.next();
			
			this.closeConnection(connection);
		}
		catch (SQLException ex) 
		{
			ex.printStackTrace();
			//			System.out.println("SQLException: " + ex.getMessage());
			//			System.out.println("SQLState: " + ex.getSQLState());
			//			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return result;
	}

	/**
	 * Parser for a MySQL script, which creates a set of tables in the MySQL database.
	 * 
	 * @param connection
	 * @param filePath
	 * @throws FileNotFoundException 
	 */
	private void sqlByStrBuffer(Connection connection, String filePath) throws Exception {

		FileReader fr = new FileReader(new File(filePath));
		BufferedReader br = new BufferedReader(fr);

		this.sqlByStrBuffer(connection, br);
		fr.close();
	}

	/**
	 * Parser for a MySQL script, which creates a set of tables in the MySQL database.
	 * 
	 * @param connection
	 * @param filePath
	 * @throws FileNotFoundException 
	 */
	private void sqlByStrBuffer(Connection connection, BufferedReader br) throws Exception {

		StringBuffer stat = new StringBuffer();

		String str = br.readLine();
		Statement statement;

		while(str!=null) {

			if(str.compareTo("")!=0 ) {

				stat.append(str+"\n");
				if(str.startsWith("DELIMITER")) {

					stat=new StringBuffer();
					String delimiter = str.replace("DELIMITER", "").trim();
					str = br.readLine();

					String query="";

					while(!str.trim().equals(delimiter)) {

						query+=" "+str;
						str = br.readLine();
					}

					try  {

						statement = connection.createStatement();
						statement.execute(query.trim()+";");
					} 
					catch (SQLException e) {

						System.out.println(query);
						e.printStackTrace();
						br.close();
						throw new Exception();
					}
				}

				if(str.contains(";")) {

					try  {

						statement = connection.createStatement();
						statement.execute(stat.toString());
					} 
					catch (SQLException e) {

						System.out.println(stat.toString());
						e.printStackTrace();
						br.close();
						throw new Exception();
					}
					stat=new StringBuffer();
				}
			}
			str = br.readLine();
		}
		br.close();

	}
	
	private void sqlByStrBufferFromBackup(Connection connection, File file) throws Exception {

		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);

		StringBuffer stat = new StringBuffer();

		String str = br.readLine();
		Statement statement;

		while(str!=null) {

			if(str.compareTo("")!=0 ) {

				stat.append(str+"\n");
				if(str.startsWith("DELIMITER")) {

					stat=new StringBuffer();
					String delimiter = str.replace("DELIMITER", "").trim();
					str = br.readLine();

					String query="";

					while(!str.trim().equals(delimiter)) {

						query+=" "+str;
						str = br.readLine();
					}

					try  {

						statement = connection.createStatement();
						statement.execute(query.trim()+";");
					} 
					catch (SQLException e) {

						System.out.println(file);
						System.out.println(query);
						e.printStackTrace();
						br.close();
						throw new Exception();
					}
				}

				if(str.contains(";")) {

					try  {

						statement = connection.createStatement();
						statement.execute(stat.toString());
					} 
					catch (SQLException e) {

						System.out.println(stat.toString());
						e.printStackTrace();
						br.close();
						throw new Exception();
					}
					stat=new StringBuffer();
				}
			}
			str = br.readLine();
		}
		br.close();
		fr.close();
	}
	
	/**
	 * Retrives all the existing Schemas in the MySQL database.
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public List<String> getSchemas() throws SQLException {

		List<String> list= new ArrayList<String>();
		List<String> schemasList = new ArrayList<String>();

		Connection connection = this.createConnection();
		ResultSet rs;
		Statement statement = null;

		try {

			statement = (Statement) connection.createStatement();
			statement.execute( "SHOW DATABASES ");
			rs = statement.getResultSet();

			while(rs.next()) {

				list.add(rs.getString(1));
			}

			for(String s: list) {

				if(checkTable(s,"geneblast") || checkTable(s,"geneHomology")) {
					
					schemasList.add(s);
				}
			}
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		this.closeConnection(connection);
		return schemasList;
	}

	/**
	 * @param schemaName
	 * @return
	 */
	public boolean existsSchema(String schemaName) {

		boolean ret = false;
		try {

			Connection connection = this.createConnection();
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery( "SHOW DATABASES LIKE '"+connection+"'");
			ret=rs.next();
			statement.close();
			this.closeConnection(connection);
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Creates a new Schema and with a set of tables defined in a SQL script. 
	 * 
	 * @param schema
	 * @param filePath
	 * @return
	 */
	public boolean newSchemaAndScript(String schema, String filePath[]){

		try {

			Connection conn = this.createConnection();

			conn = this.createSchema(conn, schema);		

			for(int i=0;i<filePath.length;i++) {
				
				this.sqlByStrBuffer(conn, filePath[i]);
			}

			this.closeConnection(conn);	

			return true;
		}
		catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Creates a new Schema From a database backup with a set of tables defined in a SQL script.
	 * @param schema
	 * @param file
	 * @return
	 */
	public boolean newSchemaAndScriptFromBackup(String schema, File file){

		try {

			Connection conn = this.createConnection();

			this.createSchema(conn, schema);		

			this.closeConnection(conn);

			Connection connection = this.createConnection(schema);

			this.sqlByStrBufferFromBackup(connection, file);
			this.closeConnection(connection);	

			return true;
		}
		catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Implements a SQL script
	 * 
	 * @param schema
	 * @param filePath
	 * @return
	 */
	public boolean cleanSchema(String schema, String[] filePath){

		try {

			Connection connection = this.createConnection(schema);

			for(int i=0;i<filePath.length;i++) {

				this.sqlByStrBuffer(connection, filePath[i]);
			}

			this.closeConnection(connection);	

			return true;
		}
		catch (Exception e) {

			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @return if exists connection
	 * @throws SQLException 
	 */
	public boolean isConnected() throws SQLException {
		
		//try {
			
			boolean isConnected = false;
			
			Connection connection = this.createConnection();
			
			if(connection != null) {
				
				isConnected=true;
				this.closeConnection(connection);
			}

			return isConnected;
//		}
//		catch (Exception e) {
//			
//			e.printStackTrace();
//			return false;
//		}
	}

	/**
	 * DROP the given database
	 * 
	 * @param database
	 * @return
	 */
	public boolean dropDatabase(String database) {
		
		try {
			
			Connection connection = this.createConnection();
			Statement statement = (Statement) connection.createStatement();
			statement.execute( "DROP SCHEMA "+database);
			statement.close();
			connection.close();
			return true;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			//			System.out.println("SQLException: " + ex.getMessage());
			//			System.out.println("SQLState: " + ex.getSQLState());
			//			System.out.println("VendorError: " + ex.getErrorCode());
			return false;
		}


	}

}


