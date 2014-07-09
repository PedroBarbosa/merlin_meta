
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

/**
 * @author ODias
 *
 */
public class TransportersContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1085515932445411650L;
	
	private HashMap<String, String> names;
	private Connection connection;

	/**
	 * @param table
	 * @param name
	 */
	public TransportersContainer(Table table, String name) {
		
		super(table, name);
		this.connection=table.getConnection();
	}
	
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		
		int num=0;
		int noname=0;
		int compounds=0;

		String[][] res = new String[3][];
		try {
			
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());

			while(rs.next()) {
				
				num++;
				if(rs.getString(2)==null) noname++;
				else{compounds++;}
			}

			res[0] = new String[] {"Number of metabolites", ""+num};
			res[1] = new String[] {"Number of metabolites with no name associated", ""+noname};
			res[4] = new String[] {"Number of compounds", ""+compounds};

			rs = stmt.executeQuery("SELECT compound_idcompound, stoichiometric_coefficient FROM stoichiometry");

			LinkedList<String> reagents = new LinkedList<String>();

			while(rs.next()) {
				
			}


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
		columnsNames.add("Genes");
		columnsNames.add("TC family");
		columnsNames.add("Number of encoded proteins");

		GenericDataTable res = new GenericDataTable(columnsNames, "Transporters", "Transport proteins encoding genes") {
			
			private static final long serialVersionUID = 1153164566285176L;

			@Override
			public boolean isCellEditable(int row, int col) {
				
				if (col==0) {
					
					return true;
				}
				else {
					
					return false;
				}
			}
		};

		try {
			
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM sw_transporters");
			
			while(rs.next()) {
				
				List<Object> ql = new ArrayList<Object>();

				ql.add("");
				ql.add(rs.getString(2));
				ql.add(rs.getString(5));
				ql.add(rs.getString(3));
				ql.add(rs.getString(6));
				res.addLine(ql, rs.getString(1));

				if( rs.getString(2)==(null))
				{
					this.names.put(rs.getString(1), rs.getString(3));
				}
				else
				{
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
	
	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(2)});
		res.put(new Integer(3), new Integer[]{new Integer(3)});
		res.put(new Integer(4), new Integer[]{new Integer(4)});

		return res;
	}
	
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
		
		return null;
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
		
		return "Transport protein: ";
	}
}
