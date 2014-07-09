package datatypes.metabolic;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;


/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class Pathway extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;
	
	/**
	 * @param dbt
	 * @param name
	 */
	public Pathway(Table dbt, String name) {
		
		super(dbt, name);
		this.connection=dbt.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		
		int num=0;
		int noname=0;
		int nosbml=0;

		String[][] res = new String[3][];
		
		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());

			while(rs.next())
			{
				num++;
				if(rs.getString(2)==null) noname++;
				if(rs.getString(3)==null) nosbml++;
			}

			res[0] = new String[] {"Number of pathways", ""+num};
			res[1] = new String[] {"Number of pathways with no name associated", ""+noname};
			res[2] = new String[] {"Number of pathways with no SBML file associated",
					""+nosbml};
			rs.close();
			stmt.close();

		} 
		catch(Exception e) {
			e.printStackTrace();}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Code");
		columnsNames.add("Name");
		columnsNames.add("Number of reactions");
		columnsNames.add("Number of enzymes");

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		GenericDataTable res = new GenericDataTable(columnsNames, "Promoter", "Pathway"){
			private static final long serialVersionUID = 1236477181642906433L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}};

			try
			{
				//MySQLMultiThread dsa =  new MySQLMultiThread( host, port, dbName, usr, pwd);
				Statement stmt = this.connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT idpathway, code, name FROM pathway ORDER BY name");

				while(rs.next()) {
					
					String[] ql = new String[4];
					ql[0] = rs.getString(2);
					ql[1] = rs.getString(3);
					ql[2] = "0";
					ql[3] = "0";
					index.add(rs.getString(1));
					qls.put(rs.getString(1), ql);
				}

				rs = stmt.executeQuery(
						"SELECT pathway_idpathway, count(reaction_idreaction) " +
						"FROM pathway " +
						"RIGHT JOIN pathway_has_reaction ON pathway_idpathway=pathway.idpathway " +
						"GROUP BY pathway_idpathway ORDER BY name"
				);

				while(rs.next()) {
					
					qls.get(rs.getString(1))[2] = rs.getString(2);
				}

				rs = stmt.executeQuery(
						"SELECT pathway_idpathway, count(enzyme_protein_idprotein) " +
						"FROM pathway " +
						"RIGHT JOIN pathway_has_enzyme ON pathway_idpathway=pathway.idpathway " +
						"GROUP BY pathway_idpathway ORDER BY name"
				);

				while(rs.next()) {
					
					qls.get(rs.getString(1))[3] = rs.getString(2);
				}

				for(int i=0;i<index.size();i++) {
					
					List<Object> ql = new ArrayList<Object>();
					String[] gark = qls.get(index.get(i));
					ql.add("");
					ql.add(gark[0]);
					ql.add(gark[1]);
					ql.add(gark[2]);
					ql.add(gark[3]);
					res.addLine(ql, index.get(i));
					this.names.put(index.get(i), gark[0]);
				}
				rs.close();
				stmt.close();
			
			}
			catch(Exception e)
			{e.printStackTrace();}

			return res;
	}

	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});

		return res;
	}

	public String[] getSearchDataIds() {
		
		String[] res = new String[]{"Name"};


		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		return true;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] res = new DataTable[2];
		ArrayList<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Reactions");
		columnsNames.add("Equation");
		DataTable qrt = new DataTable(columnsNames, "Reactions");
		columnsNames = new ArrayList<String>();
		columnsNames.add("Enzymes");
		columnsNames.add("Protein name");
		columnsNames.add("Class");
		DataTable qrt2 = new DataTable(columnsNames, "Enzymes");
		res[0] = qrt;
		res[1] = qrt2;
		try
		{
			//MySQLMultiThread this.connection =  new MySQLMultiThread( host, port, dbName, usr, pwd);
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT distinct(reaction.idreaction), name, equation " +
					"FROM pathway_has_reaction " +
					"LEFT JOIN reaction ON idreaction = reaction_idreaction " +
					"WHERE pathway_idpathway = " + id + " " +
					"ORDER BY name;"
					
			);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				qrt.addLine(ql);
			}

			//stmt = dsa.createStatement();
			rs = stmt.executeQuery(
					"SELECT distinct(enzyme.ecnumber), protein.name, class, inModel FROM enzyme " +
					"LEFT JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_ecnumber = enzyme.ecnumber " +
					"LEFT JOIN protein ON protein.idprotein = enzyme.protein_idprotein " +
					"WHERE pathway_idpathway="+id+ " " +
					"ORDER BY enzyme.ecnumber;"
			);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				if(rs.getString(4).equals("1"))
				{
					ql.add("true");
				}
				else
				{
					ql.add("false");
				}
				qrt2.addLine(ql);
			}
			rs.close();
			stmt.close();
		
		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}

	public String getName(String id)
	{
		return this.names.get(id);
	}

	public String getSingular()
	{
		return "Pathway: ";
	}
}
