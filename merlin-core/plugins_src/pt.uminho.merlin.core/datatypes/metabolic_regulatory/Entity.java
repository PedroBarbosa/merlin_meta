package datatypes.metabolic_regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Project;
import datatypes.Table;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author ODias
 *
 */
@Datatype(structure = Structure.LIST,namingMethod="getName")
public abstract class Entity extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected ArrayList<Entity> subenties = null;
	protected Table table;
	protected String name;
	private Connection connection;

	private Project project;

	/**
	 * @return the connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Entity(Table dbt, String name) {
		
		this.subenties = new ArrayList<Entity>();
		this.table = dbt;
		this.name = name;
		this.connection=dbt.getConnection();
	}

	public Table getDbt() {
		return table;
	}

	public void setDbt(Table dbt) {
		this.table = dbt;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@ListElements
	public ArrayList<Entity> getSubenties() {
		return subenties;
	}

	public void setSubenties(ArrayList<Entity> subenties) {
		this.subenties = subenties;
	}

	public void addEl(Entity ent){
		subenties.add(ent);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * @return
	 */
	public String[][] getStats() {
		
		String[][] res = null;
		try
		{
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM "+this.table.getName());
			
			rs.next();
			String num = rs.getString(1);
			
			res = new String[][] {{"Number of entries", num}};
		} 
		catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public GenericDataTable getData()
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		GenericDataTable res = new GenericDataTable(columnsNames, "", "");
		
		return res;
	}
	
	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		
		return res;
	}
	
	public String[] getSearchDataIds()
	{
		String[] res = new String[0];
		
		return res;
	}
	
	public boolean hasWindow()
	{
		return false;
	}
	
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] res = new DataTable[1];
		
		ArrayList<String> columnsNames = new ArrayList<String>();

		DataTable qrt = new DataTable(columnsNames, "");
		
		res[0] = qrt;
		
		return res;
	}
	
	public String getName(String id)
	{
		return "";
	}
	
	public String getSingular()
	{
		return "";
	}
	
	/**
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public String[][] select(String query) throws SQLException {

		String[][] rset = null;
		try 
		{
			Statement stmt = this.connection.createStatement();
			ResultSet rs=stmt.executeQuery(query);

			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			rset = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();

			int row=0;
			while(row<rset.length)
			{
				int col=1;
				while(col<rsmd.getColumnCount()+1)
				{

					rset[row][col-1] = rs.getString(col);
					col++;
				}
				rs.next();
				row++;
			}

			rs.close();            
			stmt.close();

		} catch (SQLException ex)
		{
			System.out.println(query);
			ex.printStackTrace();
		}

		return rset; 
	}

	/**
	 * @param project
	 */
	public void setProject(Project project) {
		this.project = project;	
		
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}
}
