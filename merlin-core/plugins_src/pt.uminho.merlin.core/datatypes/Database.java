package datatypes;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.COMPLEX, namingMethod="getName")
public class Database extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7041306953597159055L;
	//private Results qr;
	
	private Tables dbt;
	private Entities ents;
	private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private LinkedList<String> orphenComplexs;
	private MySQLMultiThread mySQLMultiThread;

	/**
	 * @param MySQLMultiThread
	 */
	public Database(MySQLMultiThread mySQLMultiThread) {
		
		//this.qr = new Results(MySQLMultiThread);
		this.mySQLMultiThread=mySQLMultiThread;
		//host = dsa.get_database_host(); port = dsa.get_database_port(); dbName = dsa.get_database_name(); usr = dsa.get_database_user(); pwd=dsa.get_database_password();

		this.ultimlyComplexComposedBy = new TreeMap<String,LinkedList<String>>();
		this.orphenComplexs = new LinkedList<String>();

		try {
			
			this.getComplex();
		}
		catch(Exception e){e.printStackTrace();}
	}

	/**
	 * @return
	 */
	public MySQLMultiThread getMySqlCredentials() {

		return this.mySQLMultiThread;
		//return new MySQLMultiThread( host, port, dbName, usr, pwd);
	}

//	@Clipboard(name="Results",order=2)
//	public Results getQr() {
//		return qr;
//	}
//
//	public void setQr(Results qr) {
//		this.qr = qr;
//		setChanged();
//		notifyObservers();
//	}
//
//	public void setQr2(Results qr) {
//		this.qr = qr;
//		//		setChanged();
//		//		notifyObservers();
//	}

	@Clipboard(name="Tables",order=3)
	public Tables getTables() {
		return dbt;
	}

	public void setTables(Tables dbt) {
		this.dbt = dbt;
		setChanged();
		notifyObservers();
	}

	public void setMySqlCredentials(MySQLMultiThread MySQLMultiThread){
		this.mySQLMultiThread=MySQLMultiThread;
		//	host = dsa.get_database_host(); port = dsa.get_database_port(); dbName = dsa.get_database_name(); usr = dsa.get_database_user(); pwd=dsa.get_database_password();
	}

	@Clipboard(name="Entities",order=1)
	public Entities getEntities() {
		return ents;
	}

	public void setEnts(Entities ents) {
		this.ents = ents;
	}

	public String getName()
	{
		return "Database: "+this.mySQLMultiThread.get_database_name()+"@"+this.mySQLMultiThread.get_database_host();
	}

	public LinkedList<String> getOrphenComplexs() {
		return orphenComplexs;
	}

	public TreeMap<String, LinkedList<String>> getUltimlyComplexComposedBy() {
		return ultimlyComplexComposedBy;
	}

	protected void getComplex() throws SQLException
	{
		HashMap<String,String[]> complexCodingGeneData = new HashMap<String,String[]>();

		HashMap<String,LinkedList<String>> complexComposedBy = new HashMap<String,LinkedList<String>>();

		HashMap<String,LinkedList<String>> proteinGenes = new HashMap<String,LinkedList<String>>();

		Connection connection = new Connection(mySQLMultiThread);

		Statement stmt = connection.createStatement();

		ResultSet rs = stmt.executeQuery("SHOW tables;");
		boolean go=false;
		while(rs.next())
		{
			if(rs.getString(1).equalsIgnoreCase("protein_composition"))
			{
				go=true;
			}
		}
		if(go)
		{
			rs = stmt.executeQuery("SELECT * FROM protein_composition"); 

			while(rs.next())
			{
				addToList(rs.getString(2), rs.getString(1), complexComposedBy);
			}

			rs = stmt.executeQuery(
					"SELECT gene.idgene, gene.name, protein_composition.subunit " +
							"FROM subunit JOIN protein_composition ON subunit.enzyme_protein_idprotein = protein_composition.subunit " +
							"JOIN gene ON idgene = subunit.gene_idgene"
					);

			while(rs.next()) {
				
				if(!complexCodingGeneData.containsKey(rs.getString(1))) {
					
					complexCodingGeneData.put(
							rs.getString(1), 
							new String[]{rs.getString(1), rs.getString(2), rs.getString(3)}
							);
				}

				addToList(rs.getString(1), rs.getString(4), proteinGenes);
			}

			//        HashMap<String,LinkedList<String>> ultimlyComplexComposedBy = new HashMap<String,LinkedList<String>>();

			getRestOfComplexs(proteinGenes, complexComposedBy, ultimlyComplexComposedBy);
		}
		
		stmt.close();
	}

	public TreeMap<String,LinkedList<String>> getRestOfComplexs(HashMap<String,LinkedList<String>> proteinGenes, 
			HashMap<String,LinkedList<String>> complexComposedBy, 
			TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy)
			{

		int intialUnkonen = complexComposedBy.size();

		Set<String> keys = complexComposedBy.keySet();

		LinkedList<String> found = new LinkedList<String>();

		for (Iterator<String> p_iter = keys.iterator(); p_iter.hasNext(); )
		{
			String key = (String)p_iter.next();

			LinkedList<String> subProts = complexComposedBy.get(key);

			boolean go = true;

			for(int i=0;i<subProts.size() && go;i++)
			{
				String sub = subProts.get(i);

				if(!ultimlyComplexComposedBy.containsKey(sub) && !proteinGenes.containsKey(sub)) go = false;
			}

			if(go) found.add(key);
		}

		for(int i=0;i<found.size();i++)
		{
			String key = found.get(i);

			LinkedList<String> subProts = complexComposedBy.get(key);

			complexComposedBy.remove(key);

			LinkedList<String> comps = new LinkedList<String>();

			for(int e=0;e<subProts.size();e++)
			{
				String sub = subProts.get(e);

				LinkedList<String> genesToAdd;

				if(ultimlyComplexComposedBy.containsKey(sub))
				{
					genesToAdd = ultimlyComplexComposedBy.get(sub);
				}
				else
				{
					genesToAdd = proteinGenes.get(sub);
				}

				for(int g=0;g<genesToAdd.size();g++)
				{
					comps.add(genesToAdd.get(g));
				}
			}

			ultimlyComplexComposedBy.put(key, comps);
		}

		if(complexComposedBy.isEmpty() || intialUnkonen==complexComposedBy.size())
		{

			if(intialUnkonen==complexComposedBy.size())
			{
				Set<String> kezs = complexComposedBy.keySet();

				for (Iterator<String> p_iter = kezs.iterator(); p_iter.hasNext(); )
				{
					String key = (String)p_iter.next();
					this.orphenComplexs.add(key);
				}
			}
			return ultimlyComplexComposedBy;
		}
		else
		{
			return getRestOfComplexs(proteinGenes, complexComposedBy, ultimlyComplexComposedBy);
		}

			}

	protected void addToList(String add, String key, HashMap<String,LinkedList<String>> h)
	{
		if(h.containsKey(key)) h.get(key).add(add);
		else
		{
			LinkedList<String> lis = new LinkedList<String>();
			lis.add(add);
			h.put(key, lis);
		}
	}

	protected void addToList(LinkedList<String> add, String key, HashMap<String,LinkedList<String>> h)
	{
		if(h.containsKey(key))
		{
			for(int i=0;i<add.size();i++)
			{
				h.get(key).add(add.get(i));
			}
		}
		else h.put(key, add);
	}
}
