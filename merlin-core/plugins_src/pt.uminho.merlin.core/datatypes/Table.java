package datatypes;


import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.COMPLEX,namingMethod="getName")
public class Table extends Observable implements Serializable {

	private static final long serialVersionUID = 7688483767626932038L;

	private String name;
	private String[] columms;
	//private Results results;
	private Connection connection;
	//private String host, port, dbName, usr, pwd;
	
//	/**
//	 * @param name
//	 * @param columms
//	 * @param results
//	 * @param connection
//	 */
//	public Table(String name, String[] columms, Results results, Connection connection) {
//		
//		this.name = name;
//		this.columms = columms;
//		//this.results = results;
//		//this.host = mySQLMultiThread.get_database_host(); port = mySQLMultiThread.get_database_port(); dbName = mySQLMultiThread.get_database_name(); usr = mySQLMultiThread.get_database_user(); pwd=mySQLMultiThread.get_database_password();
//		this.connection=connection;
//	}
	
	/**
	 * @param name
	 * @param columms
	 * @param results
	 * @param connection
	 */
	public Table(String name, String[] columms, Connection connection) {
		
		this.name = name;
		this.columms = columms;
		//this.host = mySQLMultiThread.get_database_host(); port = mySQLMultiThread.get_database_port(); dbName = mySQLMultiThread.get_database_name(); usr = mySQLMultiThread.get_database_user(); pwd=mySQLMultiThread.get_database_password();
		this.connection=connection;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String[] getColumms() {
		return columms;
	}

	public void setColumms(String[] columms) {
		this.columms = columms;
	}

	public String toString() {
		return name;
	}

//	public MySQLMultiThread getMySqlCredentials() {
//	//return new MySQLMultiThread( host, port, dbName, usr, pwd);
//	if(this.mySQLMultiThread == null )
//	{
//		this.mySQLMultiThread=new MySQLMultiThread( this.host, this.port, this.dbName, this.usr, this.pwd);
//	}
//	return this.mySQLMultiThread;
//}
//}
	
	/**
	 * @return
	 */
	public Connection getConnection() {
	return this.connection;
}

//	/**
//	 * @return
//	 */
//	public Results getResults() {
//		return results;
//	}
//
//	/**
//	 * @param results
//	 */
//	public void setResults(Results results) {
//		this.results = results;
//	}
	
	public DataTable getValues() throws SQLException {
		
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		for(int i=0;i<columms.length;i++) columnsNames.add(columms[i]);

		DataTable qrt = new DataTable(columnsNames, this.name);
		
		//MySQLMultiThread dsa =  new MySQLMultiThread( host, port, dbName, usr, pwd);
		Statement stmt = this.connection.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.name);
        
        int ncols = rs.getMetaData().getColumnCount();
        
        while(rs.next())
        {
        	ArrayList<String> ql = new ArrayList<String>();
        	for(int i=0;i<ncols;i++)
        	{
        		String in = rs.getString(i+1);

				if(in!=null) ql.add(in);
				else ql.add("");
        	}
        	qrt.addLine(ql);
        }
        
        rs.close();
        stmt.close();
        //this.mySQLMultiThread.closeConnection();
        return qrt;
	}
	
	public String getSize() throws SQLException {
		
		//MySQLMultiThread dsa =  new MySQLMultiThread( host, port, dbName, usr, pwd);
		Statement stmt = this.connection.createStatement();
		
		ResultSet rs = stmt.executeQuery("SELECT count(*) FROM "+this.name);
		
		rs.next();
		
		String result = rs.getString(1);
		//this.mySQLMultiThread.closeConnection();
		return result;
	}
}
