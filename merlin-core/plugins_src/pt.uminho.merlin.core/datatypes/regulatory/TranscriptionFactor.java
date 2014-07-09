package datatypes.regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;

public class TranscriptionFactor extends Entity implements Serializable {

	private static final long serialVersionUID = -5115896854054366023L;

	private HashMap<String, String> names;

	public TranscriptionFactor(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		int num=0;
		int npromoter=0;
		LinkedList<String> numl = new LinkedList<String>();
		LinkedList<String> promotersids = new LinkedList<String>();
		int numultiplegene=0;
		
		String[][] res = new String[5][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());
			
			while(rs.next())
	        {
				if(!numl.contains(rs.getString(1)))
				{
					num++;
					numl.add(rs.getString(1));
				}
				if(!promotersids.contains(rs.getString(2)))
				{
					npromoter++;
					promotersids.add(rs.getString(2));
				}
	        }
			
			res[0] = new String[] {"Number of transcription factors", ""+num};
			res[1] = new String[] {"Number of regulated promoters", ""+npromoter};
			res[2] = new String[] {"Average number of TFs for promoters",
				""+(new Double(num).doubleValue())/(new Double(npromoter).doubleValue())};
			
			//stmt = super.table.getConnection().createStatement();
			rs = stmt.executeQuery("SELECT count(distinct(gene.idgene)) " +
				"FROM regulatory_event as event, transcription_unit, " +
				"transcription_unit_gene AS tug, transcription_unit_promoter " +
				"as tup, promoter,gene WHERE event.promoter_idpromoter=idpromoter " +
				"AND tup.promoter_idpromoter=idpromoter AND " +
				"tup.transcription_unit_idtranscription_unit=idtranscription_unit " +
				"AND tug.transcription_unit_idtranscription_unit=idtranscription_unit " +
				"AND gene_idgene=idgene"
			);
			
			rs.next();

			res[3] = new String[] {"Number of regulated genes", rs.getString(1)};
			
			
			//stmt = super.table.getConnection().createStatement();
			rs = stmt.executeQuery("SELECT distinct(protein.idprotein), count(gene_idgene), " +
				"ri_function_idri_function FROM regulatory_event JOIN protein ON " +
				"regulatory_event.protein_idprotein = protein.idprotein JOIN subunit " +
				"ON protein.idprotein = subunit.protein_idprotein GROUP BY protein.idprotein");

			while(rs.next())
	        {
				if((new Integer(rs.getString(2))).intValue()>1) numultiplegene++;
	        }
			
			res[4] = new String[] {"Number of TFs encoded by multiple genes", ""+numultiplegene};
			
		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public DataTable getAllTFs() throws Exception
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Number of regulated genes");

		
		HashMap<String,ArrayList<String[]>> index = new HashMap<String,ArrayList<String[]>>();
		
		ArrayList<String> check = new ArrayList<String>();

		Statement stmt = super.table.getConnection().createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT idprotein, gene.idgene, gene.name " +
        	"FROM regulatory_event JOIN protein ON regulatory_event.protein_idprotein " +
        	"= protein.idprotein JOIN subunit ON protein.idprotein = " +
        	"subunit.protein_idprotein JOIN gene ON gene.idgene = subunit.gene_idgene " +
        	"ORDER BY idprotein"
        );
        
        while(rs.next())
        {
        	if(!check.contains(rs.getString(1)+"."+rs.getString(2)))
        	{
        		check.add(rs.getString(1)+"."+rs.getString(2));
        		if(index.containsKey(rs.getString(1)))
        		{
        			index.get(rs.getString(1)).add(new 
        				String[]{rs.getString(1),rs.getString(2),rs.getString(3)}
        			);
        		}
        		else
        		{
        			ArrayList<String[]> lis = new ArrayList<String[]>();
        			lis.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3)});
        			index.put(rs.getString(1),lis);
        		}
        	}
        }
        
        //stmt = super.table.getConnection().createStatement();
        
        rs = stmt.executeQuery("SELECT protein.name, protein.idprotein, count(gene.idgene) " +
        	"FROM regulatory_event as event,transcription_unit, transcription_unit_gene " +
        	"AS tug, transcription_unit_promoter as tup, promoter,gene,protein, " +
        	"ri_function WHERE ri_function_idri_function=idri_function AND " +
        	"protein_idprotein=idprotein AND event.promoter_idpromoter=idpromoter " +
        	"AND tup.promoter_idpromoter=idpromoter AND " +
        	"tup.transcription_unit_idtranscription_unit=idtranscription_unit " +
        	"AND tug.transcription_unit_idtranscription_unit=idtranscription_unit " +
        	"AND gene_idgene=idgene GROUP BY protein.idprotein order by protein.idprotein"
        );

		DataTable qrt = new DataTable(columnsNames, "EnzymesContainer");
        while(rs.next())
        {
    		ArrayList<String> ql = new ArrayList<String>();
        	if(index.containsKey(rs.getString(2)))
        	{
        		ArrayList<String[]> lis = index.get(rs.getString(2));
        		String egenes = "";
        		for(int r=0;r<lis.size();r++)
        		{
        			if(egenes.equals("")) egenes = lis.get(r)[2];
        			else egenes += " "+lis.get(r)[2];
        		}
        		ql.add(rs.getString(1));
        		ql.add(egenes);
        		ql.add(rs.getString(3));
        	}
        	else
        	{
        		ql.add(rs.getString(1));
        		ql.add("");
        		ql.add(rs.getString(3));
        	}
        	qrt.addLine(ql);
        }
        return qrt;
	}
	
	public GenericDataTable getData()
	{
		this.names = new HashMap<String, String>();
		GenericDataTable qrt = null;
		
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Number of encoding genes");
		columnsNames.add("Number of regulated genes");
		
		try{
		
			HashMap<String,ArrayList<String[]>> index = new HashMap<String,ArrayList<String[]>>();
		
			ArrayList<String> check = new ArrayList<String>();

			Statement stmt = super.table.getConnection().createStatement();
        
			ResultSet rs = stmt.executeQuery("SELECT idprotein, gene.idgene, gene.name " +
				"FROM regulatory_event JOIN protein ON regulatory_event.protein_idprotein " +
				"= protein.idprotein JOIN subunit ON protein.idprotein = " +
				"subunit.protein_idprotein JOIN gene ON gene.idgene = subunit.gene_idgene " +
				"ORDER BY idprotein"
			);
        
			while(rs.next())
			{
				if(!check.contains(rs.getString(1)+"."+rs.getString(2)))
				{
					check.add(rs.getString(1)+"."+rs.getString(2));
					if(index.containsKey(rs.getString(1)))
					{
						index.get(rs.getString(1)).add(new 
							String[]{rs.getString(1),rs.getString(2),rs.getString(3)}
						);
					}
					else
					{
						ArrayList<String[]> lis = new ArrayList<String[]>();
						lis.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3)});
						index.put(rs.getString(1),lis);
					}
				}
			}
        
			//stmt = super.table.getConnection().createStatement();
        
			rs = stmt.executeQuery("SELECT protein.name, protein.idprotein, count(gene.idgene) " +
				"FROM regulatory_event as event,transcription_unit, transcription_unit_gene " +
				"AS tug, transcription_unit_promoter as tup, promoter,gene,protein, " +
				"ri_function WHERE ri_function_idri_function=idri_function AND " +
				"protein_idprotein=idprotein AND event.promoter_idpromoter=idpromoter " +
				"AND tup.promoter_idpromoter=idpromoter AND " +
				"tup.transcription_unit_idtranscription_unit=idtranscription_unit " +
				"AND tug.transcription_unit_idtranscription_unit=idtranscription_unit " +
				"AND gene_idgene=idgene GROUP BY protein.idprotein order by protein.idprotein"
			);

			qrt = new GenericDataTable(columnsNames, "TFs", "TU data");
			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				if(index.containsKey(rs.getString(2)))
				{
					ArrayList<String[]> lis = index.get(rs.getString(2));
					int egenes = 0;
					for(int r=0;r<lis.size();r++)
					{
						egenes++;
					}
					ql.add(rs.getString(1));
					ql.add(egenes+"");
					ql.add(rs.getString(3));
				}
				else
				{
					ql.add(rs.getString(1));
					ql.add("0");
					ql.add(rs.getString(3));
				}
				qrt.addLine(ql, rs.getString(2));

				this.names.put(rs.getString(2), rs.getString(1));
			}
		} catch(Exception e)
		{e.printStackTrace();}
        return qrt;
	}
	
	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		
		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(0), new Integer(1)});
		
		return res;
	}
	
	public String[] getSearchDataIds()
	{
		String[] res = new String[]{"Name", "Encoding genes", "All"};
		
		return res;
	}
	
	public boolean hasWindow()
	{
		return true;
	}
	
	public DataTable[] getRowInfo(String id)
	{
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Encoding genes");

		DataTable[] res = new DataTable[2];
		
		DataTable qrt = new DataTable(columnsNames, "Encoding genes");
		
		res[0] = qrt;
		
		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Regulated genes");

		DataTable qrt2 = new DataTable(columnsNames2, "Regulated genes");
		
		res[1] = qrt2;
		
		try{
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT distinct(gene.idgene), gene.name FROM regulatory_event " +
				"JOIN protein ON regulatory_event.protein_idprotein = protein.idprotein " +
				"JOIN subunit ON protein.idprotein = subunit.protein_idprotein " +
				"JOIN gene ON gene.idgene = subunit.gene_idgene " +
				"WHERE idprotein = "+id+" ORDER BY idprotein"
			);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(2));
				qrt.addLine(ql);
			}

			//stmt = super.table.getConnection().createStatement();
			rs = stmt.executeQuery(
				"SELECT distinct(gene.idgene), gene.name FROM regulatory_event as event,transcription_unit, " +
				"transcription_unit_gene AS tug, transcription_unit_promoter as tup, " +
				"promoter,gene,protein, ri_function " +
				"WHERE ri_function_idri_function=idri_function AND protein_idprotein=idprotein AND " +
				"event.promoter_idpromoter=idpromoter AND tup.promoter_idpromoter=idpromoter AND " +
				"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"gene_idgene=idgene AND protein.idprotein = "+id
			);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(2));
				qrt2.addLine(ql);
			}
		} catch(Exception e)
		{e.printStackTrace();}
		
		return res;
	}

	public String getName(String id)
	{
		return this.names.get(id);
	}
	
	public String getSingular()
	{
		return "TF: ";
	}
}
