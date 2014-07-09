package datatypes.metabolic;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
@Datatype(structure=Structure.SIMPLE,namingMethod="getName")
public class CompoundsReactions extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;

	public CompoundsReactions(Table table, String name) {
		
		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		
		Set<String> compoundsNumber = new HashSet<String>();
		Set<String> compartmentsNumber = new HashSet<String>();
		Set<String> reactantsNumber = new HashSet<String>();
		Set<String> productsNumber = new HashSet<String>();

		String[][] res = new String[4][];
		Statement stmt;
		try {
			
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());
			
			while(rs.next()) {
				
				compoundsNumber.add(rs.getString(3));
				compartmentsNumber.add(rs.getString(4));
				
				if(rs.getString(5).startsWith("-")) {
					
					reactantsNumber.add(rs.getString(3));
				}
				else { 
					
					productsNumber.add(rs.getString(3));
				}
			}

			res[0] = new String[] {"Number of distinct compounds involved in reactions", ""+compoundsNumber.size()};
			res[1] = new String[] {"              Number of distinct reactants", ""+reactantsNumber.size()};
			res[2] = new String[] {"              Number of distinct products", ""+productsNumber.size()};
			res[3] = new String[] {"Number of compartments", ""+compartmentsNumber.size()};

			rs.close();
			stmt.close();
			
		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Compound Name");
		columnsNames.add("Formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("Number of Reactions");

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		GenericDataTable res = new GenericDataTable(columnsNames, "Compound", "Reaction"){

			private static final long serialVersionUID = 8508631277693697374L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;}};

				//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.usr, this.pwd);
				try
				{
					Statement stmt = this.connection.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT compound.name, compound.formula, COUNT(reaction_idreaction) AS numR, kegg_id "+
							"FROM stoichiometry JOIN(compound) "+
							"ON (compound_idcompound=idcompound) "+
							"GROUP BY compound.name "+
					"ORDER BY numR DESC ");

					while(rs.next()) {
						
						String[] ql = new String[4];
						ql[0] = rs.getString(3);
						ql[1] = rs.getString(1);
						ql[2] = rs.getString(2);
						ql[3] = rs.getString(4);
						
						if(rs.getString(1)==(null)) {
							
							index.add(rs.getString(2));
							qls.put(rs.getString(2), ql);
						}
						else {
							
							index.add(rs.getString(1));
							qls.put(rs.getString(1), ql);
						}
					}

					for(int i=0;i<index.size();i++) {
						
						List<Object> ql = new ArrayList<Object>();
						String[] cr = qls.get(index.get(i));
						ql.add("");
						ql.add(cr[1]); //compound name
						ql.add(cr[2]);// compound formula
						ql.add(cr[3]);// compound formula
						ql.add(cr[0]); //reactions
						res.addLine(ql, index.get(i));
						this.names.put(index.get(i), cr[1]);
					}
					rs.close();
					stmt.close();
					
				}
				catch(Exception e){e.printStackTrace();}

				return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		ArrayList<String> columnsNames = new ArrayList<String>();

		DataTable res[] = new DataTable[1];

		columnsNames.add("Reaction");
		columnsNames.add("Equation");
		res[0] = new DataTable(columnsNames, "Reactions for compound "+id);
		//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.usr, this.pwd);
		Statement stmt;
		try
		{
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT compound.name, compound.formula, reaction.name, reaction.equation "+
					"FROM (stoichiometry " +
					"JOIN(compound) "+
					"ON (compound_idcompound=idcompound) " +
					"JOIN (reaction) "+
					"ON (reaction_idreaction=idreaction)) "+
					"WHERE compound.name = '"+id+"';");

			if(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				res[0].addLine(ql);

				while(rs.next())
				{
					ql = new ArrayList<String>();
					ql.add(rs.getString(3));
					ql.add(rs.getString(4));
					res[0].addLine(ql);
				}

			}
			else
			{
				rs = stmt.executeQuery("SELECT compound.name, compound.formula, reaction.name, reaction.Equation "+
						"FROM (stoichiometry " +
						"JOIN(compound) "+
						"ON (compound_idcompound=idcompound) " +
						"JOIN (reaction) "+
						"ON (reaction_idreaction=idreaction)) "+
						"WHERE compound.formula = '"+id+"';");

				while(rs.next())
				{

					ArrayList<String> ql = new ArrayList<String>();
					ql.add(rs.getString(3));
					ql.add(rs.getString(4));
					res[0].addLine(ql);
				}


			}
			rs.close();
			stmt.close();
			
		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}

	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});

		return res;
	}

	public String[] getSearchDataIds()
	{
		String[] res = new String[]{"Name"};


		return res;
	}

	public boolean hasWindow()
	{
		return true;
	}

	public String getName()
	{
		return "Compounds/Reactions";
	}

}