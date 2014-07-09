package datatypes.regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;

public class MultipleRi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5491683363173651717L;
	private HashMap<String,LinkedList<String>> pairs;
	private LinkedList<String> pairsIndex;
	private HashMap<String, LinkedList<String[]>> ris;
	private HashMap<String, String> promoterNames;
	private HashMap<String, String> proteinNames;
	
	public MultipleRi()
	{
		this.pairs = new HashMap<String,LinkedList<String>>();
		this.pairsIndex = new LinkedList<String>();
		this.ris = new HashMap<String, LinkedList<String[]>>();
		this.promoterNames = new HashMap<String, String>();
		this.proteinNames = new HashMap<String, String>();
	}
	
	public void loadData(Connection connection)
	{
		try{
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT protein_idprotein, promoter_idpromoter, ri_function_idri_function, " +
				"binding_site_position, protein.name, promoter.name, " +
				"symbol FROM regulatory_event JOIN protein ON protein_idprotein = " +
				"idprotein JOIN promoter ON promoter_idpromoter = idpromoter " +
				"JOIN ri_function ON ri_function_idri_function = idri_function " +
				"ORDER BY protein_idprotein,promoter_idpromoter"
			);
			
			while(rs.next())
			{
				if(!pairsIndex.contains(rs.getString(1)+"@"+rs.getString(2)))
				{
					if(!pairs.containsKey(rs.getString(1)))
					{
						LinkedList<String> ll = new LinkedList<String>();
						ll.add(rs.getString(2));
						pairs.put(rs.getString(1), ll);
					}
					else
					{
						pairs.get(rs.getString(1)).add(rs.getString(2));
					}
					
					LinkedList<String[]> ll = new LinkedList<String[]>();
					ll.add(new String[]{rs.getString(7), rs.getString(4)});
					ris.put(rs.getString(1)+"@"+rs.getString(2), ll);
					pairsIndex.add(rs.getString(1)+"@"+rs.getString(2));
				}
				else
				{
					ris.get(rs.getString(1)+"@"+rs.getString(2)).add(new String[]{rs.getString(7), rs.getString(4)});
				}
				
				if(!proteinNames.containsKey(rs.getString(1)))
				{
					proteinNames.put(rs.getString(1), rs.getString(5));
				}
				
				if(!promoterNames.containsKey(rs.getString(2)))
				{
					promoterNames.put(rs.getString(2), rs.getString(6));
				}
			}
		} catch(Exception e)
		{e.printStackTrace();}
	}
	
	public DataTable getData()
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Protein");
		columnsNames.add("Promoter");
		columnsNames.add("Ri");
		columnsNames.add("Binding site position");
		
		DataTable res = new DataTable(columnsNames, "");
		
		
		Set<String> zam = pairs.keySet();
		
		
		for (Iterator<String> iter = zam.iterator(); iter.hasNext(); )
		{
			String idprot = iter.next();
			
			LinkedList<String> idpromoters = pairs.get(idprot);
			
			for(int i=0;i<idpromoters.size();i++)
			{
				LinkedList<String[]> data = this.ris.get(idprot+"@"+idpromoters.get(i));
				
				if(data!=null && data.size()>1)
				{
					for(int x=0;x<data.size();x++)
					{
						ArrayList<String> ql = new ArrayList<String>();
						ql.add(this.proteinNames.get(idprot));
						ql.add(this.promoterNames.get(idpromoters.get(i)));
						ql.add(data.get(x)[0]);
						ql.add(data.get(x)[1]);
						
						res.addLine(ql);
					}
				}
			}
		}
		
		
		return res;
	}
}
