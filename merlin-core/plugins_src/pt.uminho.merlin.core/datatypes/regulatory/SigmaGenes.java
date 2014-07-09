package datatypes.regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Genes;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class SigmaGenes extends Genes implements Serializable {

	private static final long serialVersionUID = 9008699561446234481L;
	private TreeMap<String,String> names;

	public SigmaGenes(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy)
	{
		super(dbt, name, ultimlyComplexComposedBy);
	}

	public String[][] getStats()
	{
		int num=0;
		int noseq=0;
		int noname=0;
		int nobnumber=0;
		int nboolean_rule=0;

		String[][] res = new String[5][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT distinct(idgene), gene.name, " +
					"transcription_direction, boolean_rule FROM gene JOIN subunit " +
					"ON gene.idgene = subunit.gene_idgene JOIN protein ON " +
					"protein.idprotein = subunit.protein_idprotein JOIN sigma_promoter " +
					"ON protein.idprotein = sigma_promoter.protein_idprotein"
			);
			/* gene.sequence_idSequence ,   bnumber,  */	

			while(rs.next())
			{
				num++;
				if(rs.getString(2)==null) noseq++;
				if(rs.getString(3)==null) noname++;
				if(rs.getString(4)==null) nobnumber++;
				if(rs.getString(5)==null) nboolean_rule++;
			}

			res[0] = new String[] {"Number of sigma genes", ""+num};
			res[1] = new String[] {"Number of sigma genes with no name associated", ""+noname};
			res[2] = new String[] {"Number of sigma genes with no sequence associated", ""+noseq};
			res[3] = new String[] {"Number of sigma genes with no bnumber associated", ""+nobnumber};
			res[4] = new String[] {"Number of sigma genes with no boolean rule associated", ""+nboolean_rule};

		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}

	public GenericDataTable getAllGenes()
	{
		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Names");
		columnsNames.add("Bnumbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data");

		try
		{
			Statement stmt = super.table.getConnection().createStatement();

			ResultSet rs = stmt.executeQuery(
					"SELECT distinct(idgene), gene.name, gene.sequence_idSequence, transcription_direction, boolean_rule FROM gene JOIN subunit ON gene.idgene = subunit.gene_idgene JOIN protein ON protein.idprotein = subunit.protein_idprotein JOIN sigma_promoter ON protein.idprotein = sigma_promoter.protein_idprotein"
			);

			/* bnumber, */

			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				for(int i=0;i<2;i++)
				{
					String in = rs.getString(i+2);

					if(in!=null) ql.add(in);
					else ql.add("");
				}
				qrt.addLine(ql, rs.getString(1));
				names.put(rs.getString(1), rs.getString(2));
			}

			rs.close();
			stmt.close();
		} 
		catch (SQLException ex)
		{
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return qrt;
	}
}