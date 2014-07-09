package datatypes.metabolic;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
@Datatype(structure= Structure.LIST, namingMethod="getName",removable=true,removeMethod ="remove")
public class Compounds extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;

	/**
	 * @param table
	 * @param name
	 */
	public Compounds(Table table, String name) {
		
		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		int num=0;
		int noname=0;
		int noinchi=0;
		//int nocrn=0;
		int noformula=0;
		//int nosmiles=0;
		int glycans=0;
		int compounds=0;

		String[][] res = new String[9][];
		try {
			
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());

			while(rs.next())
			{
				num++;
				if(rs.getString(2)==null) noname++;
				if(rs.getString(3)==null) noinchi++;
				if(rs.getString(4).equals("glycan")){glycans++;}
				else{compounds++;}
				//if(rs.getString(5)==null) nocrn++;
				if(rs.getString(5)==null) noformula++;
				//if(rs.getString(11)==null) nosmiles++;
			}

			res[0] = new String[] {"Number of metabolites", ""+num};
			res[1] = new String[] {"Number of metabolites with no name associated", ""+noname};
			//res[2] = new String[] {"Number of compounds with no " +
			//	"CAS registry number associated", 
			//	""+nocrn};
			res[2] = new String[] {"Number of metabolites with no " +
					"formula associated", ""+noformula};
			res[3] = new String[] {"Number of metabolites with no " +
					"InChi associated", ""+noinchi};
			res[4] = new String[] {"Number of compounds", ""+compounds};
			res[5] = new String[] {"Number of glycan", ""+glycans};
			//res[5] = new String[] {"Number of compounds with no " +
			//	"smilies associated", ""+nosmiles};

			rs = stmt.executeQuery("SELECT compound_idcompound, stoichiometric_coefficient FROM stoichiometry");

			LinkedList<String> reagents = new LinkedList<String>();
			LinkedList<String> products = new LinkedList<String>();
			LinkedList<String> metabolitesInReaction = new LinkedList<String>();

			while(rs.next()) {
				
				if(!metabolitesInReaction.contains(rs.getString(1))) 
					metabolitesInReaction.add(rs.getString(1));

				//if((new Double(rs.getString(2))).doubleValue()>0)
				if(rs.getString(2).toString().startsWith("-")) {
					
					if(!products.contains(rs.getString(1))) products.add(rs.getString(1));
				}
				else {
					
					if(!reagents.contains(rs.getString(1))) reagents.add(rs.getString(1));
				}
			}

			res[6] = new String[] {"Number of metabolites that participate in reactions",""+metabolitesInReaction.size()};

			res[7] = new String[] {"Number of metabolites that are consumed in reactions",""+reagents.size()};

			res[8] = new String[] {"Number of metabolites that are produced in reactions",""+products.size()};

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
		columnsNames.add("Names");
		columnsNames.add("Formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("Type");
		//columnsNames.add("InChi");

		GenericDataTable res = new GenericDataTable(columnsNames, "Promoter", "Compound") {
			
			private static final long serialVersionUID = 1153164566285176L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {
			
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName() + " ORDER BY entry_type, name");
			
			while(rs.next()) {
				
				List<Object> ql = new ArrayList<Object>();

				ql.add("");
				ql.add(rs.getString("name"));
				ql.add(rs.getString("formula"));
				ql.add(rs.getString("kegg_id"));
				ql.add(rs.getString("entry_type"));
				res.addLine(ql, rs.getString(1));

				if( rs.getString(2)==(null)) {
					
					this.names.put(rs.getString(1), rs.getString(3));
				}
				else {
					
					this.names.put(rs.getString(1), rs.getString(2));
				}
			}
			rs.close();
			stmt.close();
		} 
		catch(Exception e) {
			
			e.printStackTrace();
		}

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchData()
	 */
	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(2)});
		res.put(new Integer(3), new Integer[]{new Integer(3)});
		res.put(new Integer(4), new Integer[]{new Integer(4)});

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchDataIds()
	 */
	public String[] getSearchDataIds() {
		
		//String[] res = new String[]{"Name" , "CAS registry number", "Formula",
		//	"InChi", "Smilies"};

		String[] res = new String[]{"Name" , "InChi", "Formula",
		};

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] res = new DataTable[2];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Entry Type");
		res[0] = new DataTable(columnsNames, "Entry type");
		
		columnsNames = new ArrayList<String>();
		columnsNames.add("Names");
		res[1] = new DataTable(columnsNames, "Synonyms");

		try {
			
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT alias FROM aliases WHERE class = 'c' AND entity = " + id);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[1].addLine(ql);
			}
			
			rs = stmt.executeQuery("SELECT entry_type FROM compound WHERE idCompound = " + id );
			
			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[0].addLine(ql);
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		
		return true;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName(java.lang.String)
	 */
	public String getName(String id) {
		
		return this.names.get(id);
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSingular()
	 */
	public String getSingular() {
		
		return "Metabolite: ";
	}
}
