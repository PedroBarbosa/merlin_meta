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

public class Sigma extends Entity implements Serializable {

	private static final long serialVersionUID = 7041578259945388261L;

	private HashMap<String, String> names;

	public Sigma(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		int num=0;
		int nproteins=0;
		int npromoter=0;
		LinkedList<String> proteinsids = new LinkedList<String>();
		LinkedList<String> promotersids = new LinkedList<String>();
		
		String[][] res = new String[3][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());
			
			while(rs.next())
	        {
				num++;
				if(!proteinsids.contains(rs.getString(1)))
				{
					nproteins++;
					proteinsids.add(rs.getString(1));
				}
				if(!promotersids.contains(rs.getString(2)))
				{
					npromoter++;
					promotersids.add(rs.getString(2));
				}
	        }
			
			/*
			
			
			
			*/
			
//			res[0] = new String[] {"Number of sigma promoters", ""+num};
//			res[1] = new String[] {"Number of proteins that are sigma promoters",
//				""+npromoter};
//			res[2] = new String[] {"Number of promoters that are regulated by sigma promoters",
//				""+nproteins};
			
			res[0] = new String[] {"Number of sigma factors", ""+nproteins};
			res[1] = new String[] {"Number of promotars regulated by sigma factors", ""+num};
			res[2] = new String[] {"Number of sigma factor regulations", ""+npromoter};
			
			
		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public DataTable getAllSigmass() throws Exception
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Regulated genes");

		DataTable qrt = new DataTable(columnsNames, "Sigma");
		
		HashMap<String,ArrayList<String[]>> index = new HashMap<String,ArrayList<String[]>>();
		
		ArrayList<String> check = new ArrayList<String>();

		Statement stmt = super.table.getConnection().createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT idprotein, gene.idgene, gene.name " +
       		"FROM sigma_promoter JOIN protein ON sigma_promoter.protein_idprotein = protein.idprotein " +
       		"JOIN subunit ON protein.idprotein = subunit.protein_idprotein " +
       		"JOIN gene ON gene.idgene = subunit.gene_idgene " +
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
        
        rs.close();
        stmt.close();
        
       //stmt = super.table.getConnection().createStatement();
        
        rs = stmt.executeQuery("SELECT protein.idprotein, protein.name, gene.idgene, gene.name " +
        	"FROM sigma_promoter as event, transcription_unit, transcription_unit_gene AS tug, " +
        	"transcription_unit_promoter as tup, promoter,gene,protein " +
        	"WHERE protein_idprotein=idprotein AND event.promoter_idpromoter=idpromoter " +
        	"AND tup.promoter_idpromoter=idpromoter AND " +
        	"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
        	"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
        	"gene_idgene=idgene " +
        	"ORDER BY protein.idprotein"
        );

        ArrayList<String> hashIndex = new ArrayList<String>();
		HashMap<String,String> indexSigmaNames = new HashMap<String,String>();
		HashMap<String,ArrayList<String>> indexSigmaAfectedGenes = new HashMap<String,ArrayList<String>>();
		
        while(rs.next())
        {
        	if(!indexSigmaNames.containsKey(rs.getString(1)))
        	{
        		indexSigmaNames.put(rs.getString(1), rs.getString(2));
        	}
        	if(!indexSigmaAfectedGenes.containsKey(rs.getString(1)))
        	{
        		ArrayList<String> lis = new ArrayList<String>();
    			lis.add(rs.getString(4));
        		indexSigmaAfectedGenes.put(rs.getString(1), lis);
        	}
        	else
        	{
        		indexSigmaAfectedGenes.get(rs.getString(1)).add(rs.getString(4));
        	}
        	if(!hashIndex.contains(rs.getString(1))) hashIndex.add(rs.getString(1));
        }
        
        rs.close();
        stmt.close();
        
        for(int i=0;i<hashIndex.size();i++)
        {
        	String encodGenes = "";
        	String regulatedGenes = "";
        	
        	if(indexSigmaAfectedGenes.containsKey(hashIndex.get(i)))
        	{
        		ArrayList<String> lis = indexSigmaAfectedGenes.get(hashIndex.get(i));
        		for(int r=0;r<lis.size();r++)
        		{
        			if(regulatedGenes.equals("")) regulatedGenes = lis.get(r);
        			else regulatedGenes += " "+lis.get(r);
        		}
        	}
        	
        	if(index.containsKey(hashIndex.get(i)))
        	{
        		ArrayList<String[]> lis = index.get(hashIndex.get(i));
        		for(int r=0;r<lis.size();r++)
        		{
        			if(encodGenes.equals("")) encodGenes = lis.get(r)[2];
        			else encodGenes += " "+lis.get(r)[2];
        		}
        	}
        	ArrayList<String> ql = new ArrayList<String>();
        	ql.add(indexSigmaNames.get(hashIndex.get(i)));
        	ql.add(encodGenes);
        	ql.add(regulatedGenes);
        	qrt.addLine(ql);
        }
        
        return qrt;
	}
	
	public GenericDataTable getData()
	{
		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Regulated genes");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Sigma", "Sigma data");
		
		try{
		
			HashMap<String,ArrayList<String[]>> index = new HashMap<String,ArrayList<String[]>>();
		
			ArrayList<String> check = new ArrayList<String>();

			Statement stmt = super.table.getConnection().createStatement();
        
			ResultSet rs = stmt.executeQuery("SELECT idprotein, gene.idgene, gene.name " +
				"FROM sigma_promoter JOIN protein ON sigma_promoter.protein_idprotein = protein.idprotein " +
				"JOIN subunit ON protein.idprotein = subunit.protein_idprotein " +
				"JOIN gene ON gene.idgene = subunit.gene_idgene " +
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
        
			rs.close();
			stmt.close();
        
			//stmt = super.table.getConnection().createStatement();
        
			rs = stmt.executeQuery("SELECT protein.idprotein, protein.name, gene.idgene, gene.name " +
				"FROM sigma_promoter as event, transcription_unit, transcription_unit_gene AS tug, " +
				"transcription_unit_promoter as tup, promoter,gene,protein " +
				"WHERE protein_idprotein=idprotein AND event.promoter_idpromoter=idpromoter " +
				"AND tup.promoter_idpromoter=idpromoter AND " +
				"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"gene_idgene=idgene " +
				"ORDER BY protein.idprotein"
			);

			ArrayList<String> hashIndex = new ArrayList<String>();
			HashMap<String,String> indexSigmaNames = new HashMap<String,String>();
			HashMap<String,ArrayList<String>> indexSigmaAfectedGenes = new HashMap<String,ArrayList<String>>();
		
			while(rs.next())
			{
				if(!indexSigmaNames.containsKey(rs.getString(1)))
				{
					indexSigmaNames.put(rs.getString(1), rs.getString(2));
				}
				if(!indexSigmaAfectedGenes.containsKey(rs.getString(1)))
				{
					ArrayList<String> lis = new ArrayList<String>();
					lis.add(rs.getString(4));
					indexSigmaAfectedGenes.put(rs.getString(1), lis);
				}
				else
				{
					indexSigmaAfectedGenes.get(rs.getString(1)).add(rs.getString(4));
				}
				if(!hashIndex.contains(rs.getString(1))) hashIndex.add(rs.getString(1));
			}
        
			rs.close();
			stmt.close();
        
			for(int i=0;i<hashIndex.size();i++)
			{
				String encodGenes = "";
				int regulatedGenes = 0;
				
				if(indexSigmaAfectedGenes.containsKey(hashIndex.get(i)))
				{
					ArrayList<String> lis = indexSigmaAfectedGenes.get(hashIndex.get(i));
					
					regulatedGenes = lis.size();
					
//					for(int r=0;r<lis.size();r++)
//					{
//						if(regulatedGenes.equals("")) regulatedGenes = lis.get(r);
//						else regulatedGenes += " "+lis.get(r);
//					}
				}
        	
				if(index.containsKey(hashIndex.get(i)))
				{
					ArrayList<String[]> lis = index.get(hashIndex.get(i));
//					encodGenes = lis.size();
					for(int r=0;r<lis.size();r++)
					{
						if(encodGenes.equals("")) encodGenes = lis.get(r)[2];
						else encodGenes += " "+lis.get(r)[2];
					}
				}
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add(indexSigmaNames.get(hashIndex.get(i)));
				ql.add(encodGenes);
				ql.add(""+regulatedGenes);
				qrt.addLine(ql, hashIndex.get(i));

				this.names.put(hashIndex.get(i), indexSigmaNames.get(hashIndex.get(i)));
			}
			
		} catch(Exception e)
		{e.printStackTrace();}
        
        return qrt;
		
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
	
	public DataTable[] getRowInfo(String id)
	{
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Synonyms");

		DataTable[] res = new DataTable[2];
		
		DataTable qrt = new DataTable(columnsNames, "Synonyms");
		
		res[0] = qrt;
		
		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Regulated genes");

		DataTable qrt2 = new DataTable(columnsNames2, "Regulated genes");
		
		res[1] = qrt2;
		
		try
		{
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT alias FROM aliases WHERE class = 'p' AND entity = " + id
			);

			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				qrt.addLine(ql);
			}

			//stmt = super.table.getConnection().createStatement();
			rs = stmt.executeQuery(
				"SELECT distinct(gene.idgene), gene.name FROM sigma_promoter AS " +
				"event, transcription_unit, transcription_unit_gene AS tug, " +
				"transcription_unit_promoter AS tup, promoter,gene,protein " +
				"WHERE protein_idprotein=idprotein AND event.promoter_idpromoter=idpromoter " +
				"AND tup.promoter_idpromoter=idpromoter AND " +
				"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
				"gene_idgene=idgene AND protein.idprotein = " + id +
				" ORDER BY gene.name"
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
		return "Sigma: ";
	}
}
