package datatypes.metabolic_regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Genes extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TreeMap<String,String> names;
	private TreeMap<Integer,String> ids;
	//private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public Genes(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(dbt, name);
		//this.ultimlyComplexComposedBy = ultimlyComplexComposedBy;
		this.connection=dbt.getConnection();
	}

	/**
	 * @param row
	 */
	public void removeGene(int row, boolean encodedOnly) {

		Statement stmt;

		try  {

			stmt = this.connection.createStatement();

			if(row==-1) {

				String aux ="";
				if(encodedOnly) {

					aux = "LEFT JOIN subunit ON (gene.idgene = gene_idgene) "+
							"LEFT JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein;";
				}

				ResultSet rs = stmt.executeQuery("SELECT * FROM gene "+aux);
				Set<String> genes = new HashSet<String>();

				while(rs.next()) {

					genes.add(rs.getString(1));
				}

				for(String gene_id : genes) {

					rs = stmt.executeQuery("SELECT enzyme_protein_idProtein, enzyme_ecNumber FROM subunit WHERE gene_idGene ="+gene_id);
					Set<String> proteins = new HashSet<String>();

					while(rs.next()) {

						proteins.add(rs.getString(1)+"__"+rs.getString(2));
					}

					for(String id : proteins) {

						this.removeGeneAssignemensts(gene_id, id, stmt);
					}

					stmt.execute("DELETE FROM gene where idgene = "+gene_id);
				}
			}
			else {

				String gene_id = ids.get(row);
				ResultSet rs = stmt.executeQuery("SELECT enzyme_protein_idProtein, enzyme_ecNumber FROM subunit WHERE gene_idGene ="+gene_id);

				Set<String> proteins = new HashSet<String> ();

				while(rs.next()) {

					proteins.add(rs.getString(1)+"__"+rs.getString(2));
				}

				for(String id : proteins) {

					this.removeGeneAssignemensts(gene_id, id, stmt);
				}

				stmt.execute("DELETE FROM gene where idgene = "+gene_id);
			}
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	} 

	/**
	 * @return
	 */
	public String[][] getChromosomes() {

		Statement stmt;
		String[][] res = null;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM chromosome order by name");

			if (rs.next()) {

				ResultSetMetaData rsmd = rs.getMetaData();
				rs.last();
				res = new String[rs.getRow()][rsmd.getColumnCount()];
				rs.beforeFirst();

				int row=0;
				while(rs.next()) {

					res[row][0] = rs.getString(1);
					res[row][1] = rs.getString(2);
					row++;
				}

				rs.close();            
				stmt.close();
			}

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[][] ch = new String[2][1];

		if(res!= null) {

			ch = new String[2][res.length+1];
			ch[0][0]="dummy";
			ch[1][0]="";
			for(int i = 0; i<res.length;i++) {

				ch[0][i+1]= res[i][0];
				ch[1][i+1]= res[i][1];
			}
		}

		return ch;
	} 

	/**
	 * @return
	 */
	public String[][] getProteins() {

		Statement stmt;
		String[][] res = null;

		try  {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT idProtein, name, ecnumber FROM protein " +
					" JOIN enzyme ON protein_idprotein=protein.idprotein "+
					" ORDER BY ecnumber;");

			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			res = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();

			int row=0;

			while(row<res.length) {

				res[row][0] = rs.getString(1)+"__"+rs.getString(3);
				res[row][1] = rs.getString(3)+"	-	"+rs.getString(2);

				rs.next();
				row++;
			}

			rs.close();            
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[][] prt = new String[2][res.length+1];
		prt[0][0]="dummy";
		prt[1][0]="";

		for(int i = 0; i<res.length;i++) {

			prt[0][i+1]= res[i][0];
			prt[1][i+1]= res[i][1];
		}

		return prt;
	} 

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int num=0;
		int noseq=0;
		int noname=0;
		//int nboolean_rule=0;

		Statement stmt;
		String[][] res = new String[11][];

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());

			while(rs.next()) {

				num++;
				if(rs.getString(2)==null) noseq++;
				if(rs.getString(3)==null) noname++;
				//if(rs.getString(4)==null) nboolean_rule++;
			}

			res[0] = new String[] {"Number of genes", ""+num};
			res[1] = new String[] {"Number of genes with no name associated", ""+noname};
			res[2] = new String[] {"Number of genes with no sequence associated", ""+noseq};
			//res[3] = new String[] {"Number of genes with no boolean rule associated", ""+nboolean_rule};

			rs = stmt.executeQuery("SELECT count(*) FROM aliases where class = 'g'");

			rs.next();
			String snumgenes = rs.getString(1);

			res[4] = new String[] {"Number of genes' synonyms", snumgenes};

			double synmed = (new Double(snumgenes)).intValue()/(new Double(num));

			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits(5);

			String synmedFormatted = formatter.format(synmed);
			res[5] = new String[] {"Average synonym number by gene", synmedFormatted};


			int prot=0, enz=0, trp=0;


			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(gene_idgene)) AS counter FROM subunit");
			rs.next();
			prot=rs.getInt(1);

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(gene_idgene)) AS counter, source FROM subunit " +
					"LEFT JOIN enzyme ON enzyme.protein_idprotein = subunit.enzyme_protein_idprotein " +
					"AND subunit.enzyme_ecnumber = enzyme.ecnumber " 
					+"GROUP BY SOURCE");

			while (rs.next()) {

				if(rs.getString("source").equalsIgnoreCase("TRANSPORTERS")) {

					trp=rs.getInt("counter");
				}
				else {

					enz+=rs.getInt("counter");
				}
				prot++;
			}

			res[7] = new String[] {"Number of genes that encode proteins", prot+""};
			int both = enz + trp - prot;
			
			res[8] = new String[] {"       Number of genes that only encode enzymes", enz-both+""};
			res[9] = new String[] {"       Number of genes that only encode transporters", trp-both+""};
			res[10] = new String[] {"      Number of genes that encode both", both+""};

			//			rs = stmt.executeQuery("SELECT count(distinct(gene_idgene)) FROM transcription_unit_gene");
			//
			//			rs.next();
			//
			//			res[9] = new String[] {"			Number of genes that belong to transcription units", rs.getString(1)};
			//
			//			rs = stmt.executeQuery("SELECT distinct(gene_idgene) " +
			//					"FROM subunit JOIN gene ON gene_idgene=idgene " +
			//					"WHERE enzyme_protein_idprotein IN (SELECT protein_idprotein " +
			//					"FROM regulatory_event)"
			//					);
			//
			//			LinkedList<String> tempGenes = new LinkedList<String>();
			//
			//			while(rs.next()) {
			//				
			//				tempGenes.add(rs.getString(1));
			//			}
			//
			//			rs = stmt.executeQuery("SELECT protein_idprotein " +
			//					"FROM regulatory_event"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1))) {
			//					
			//					LinkedList<String> ghenes = 
			//							this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tempGenes.contains(ghenes.get(i))) 
			//							tempGenes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) " +
			//							"FROM subunit JOIN sigma_promoter ON subunit.enzyme_protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(!tempGenes.contains(rs.getString(1))) tempGenes.add(rs.getString(1));
			//			}
			//
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM sigma_promoter " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = 
			//							this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tempGenes.contains(ghenes.get(i))) tempGenes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[10] = new String[] {"Number of regulatory genes", ""+tempGenes.size()};
			//
			//			rs = stmt.executeQuery("SELECT count(distinct(gene.idgene)) " +
			//					"FROM regulatory_event as event, transcription_unit, " +
			//					"transcription_unit_gene AS tug, transcription_unit_promoter as tup, " +
			//					"promoter,gene WHERE event.promoter_idpromoter=idpromoter AND " +
			//					"tup.promoter_idpromoter=idpromoter AND " +
			//					"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
			//					"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND gene_idgene=idgene"
			//					);
			//
			//			rs.next();
			//
			//			res[11] = new String[] {"Number of regulated genes", rs.getString(1)};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) FROM subunit " +
			//							"JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein"
			//					);
			//
			//			LinkedList<String> enzyme_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				enzyme_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM enzyme " +
			//							"JOIN protein_composition ON protein_composition.protein_idprotein = enzyme.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!enzyme_genes.contains(ghenes.get(i))) enzyme_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[12] = new String[] {"Number of genes that encode enzymes", ""+enzyme_genes.size()};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) FROM subunit " +
			//							"JOIN regulatory_event ON subunit.enzyme_protein_idprotein = regulatory_event.protein_idprotein"
			//					);
			//
			//			LinkedList<String> tf_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				tf_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM regulatory_event " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = regulatory_event.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tf_genes.contains(ghenes.get(i))) tf_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[13] = new String[] {"Number of genes that encode TFs", ""+tf_genes.size()};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) " +
			//							"FROM subunit JOIN sigma_promoter ON subunit.enzyme_protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			LinkedList<String> sigma_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				sigma_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM sigma_promoter " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!sigma_genes.contains(ghenes.get(i))) sigma_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			LinkedList<String> snork_genes = new LinkedList<String>();
			//
			//			for(int i=0;i<enzyme_genes.size();i++) 
			//				if(!snork_genes.contains(enzyme_genes.get(i))) snork_genes.add(enzyme_genes.get(i));
			//
			//			for(int i=0;i<tf_genes.size();i++) 
			//				if(!snork_genes.contains(tf_genes.get(i))) snork_genes.add(tf_genes.get(i));
			//
			//			for(int i=0;i<sigma_genes.size();i++) 
			//				if(!snork_genes.contains(sigma_genes.get(i))) snork_genes.add(sigma_genes.get(i));
			//
			//			res[14] = new String[] {"Non coding genes", ""+(num-snork_genes.size())};
			//
			//			rs = stmt.executeQuery("SELECT count(*) FROM transcription_unit");
			//			rs.next();

			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return
	 */
	public GenericDataTable getAllGenes() {

		names = new TreeMap<String,String>();
		ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Locus Tag");
		columnsNames.add("Names");
		columnsNames.add("Number of encoding subunits");
		columnsNames.add("Number of encoded proteins");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT idgene, locusTag, name, count(DISTINCT(module_id)), count(DISTINCT(enzyme_ecnumber))"+
					"FROM gene "+
					"LEFT JOIN subunit ON gene.idgene = gene_idgene "+
					"LEFT JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein "+
					"GROUP BY locusTag;");

			int g = 0;

			while(rs.next()) {

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));
				qrt.addLine(ql, rs.getString(1));
				names.put(rs.getString(1), rs.getString(2));
				g+=1;
			}

			rs.close();
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getRegulatoryGenes() {

		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Names");
		columnsNames.add("Numbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
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
			ResultSet rs = stmt.executeQuery("SELECT name, locusTag, idgene FROM subunit JOIN gene " +
					"ON gene_idgene=idgene WHERE protein_idprotein IN (SELECT protein_idprotein " +
					"FROM regulatory_event) order by name"
					);
			int g = 0;

			while(rs.next()) {

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				qrt.addLine(ql, rs.getString(1));
				names.put(rs.getString(1), rs.getString(2));
				g+=1;
			}

			rs.close();
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getEncodingGenes() {

		names = new TreeMap<String,String>();
		ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Locus Tag");
		columnsNames.add("Names");
		columnsNames.add("Number of encoded subunits");
		columnsNames.add("Number of encoded enzymes");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery(
					"SELECT idgene, locusTag, name, count(DISTINCT(module_id)), count(enzyme_ecnumber) "+
							"FROM gene "+
							"LEFT JOIN subunit ON (gene.idgene = gene_idgene) "+
							"LEFT JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein "+
					"GROUP BY locusTag;");


			int g = 0;
			while(rs.next()) {

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));
				qrt.addLine(ql, rs.getString(1));
				names.put(rs.getString(1), rs.getString(2));
				g+=1;
			}

			rs.close();
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getRegulatedGenes() {

		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Names");
		columnsNames.add("Bnumbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};
		try
		{
			//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
			Statement stmt;

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT DISTINCT gene.name, bnumber, gene.idgene " +
					"FROM regulatory_event as event, transcription_unit, transcription_unit_gene " +
					"AS tug, transcription_unit_promoter as tup, promoter,gene WHERE " +
					"event.promoter_idpromoter=idpromoter AND tup.promoter_idpromoter=idpromoter AND " +
					"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
					"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND gene_idgene=idgene " +
					"order by gene.name"
					);

			//        int ncols = rs.getMetaData().getColumnCount();

			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				for(int i=0;i<2;i++)
				{
					String in = rs.getString(i+1);

					if(in!=null) ql.add(in);
					else ql.add("");
				}
				qrt.addLine(ql, rs.getString(3));
				names.put(rs.getString(3), rs.getString(1));
			}

			rs.close();
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		boolean regulation = false, eprotein = false, ecompartment = false, orthology = false;

		DataTable[] results = new DataTable[5];

		DataTable[] results2;

		int tabs=1;

		List<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Synonyms");
		results[0] = new DataTable(columnsNames, "Synonyms");

		
		columnsNames = new ArrayList<String>();
		columnsNames.add("Regulations");
		results[1] = new DataTable(columnsNames, "Regulations");
		
		
		columnsNames = new ArrayList<String>();
		columnsNames.add("Ortholog");
		columnsNames.add("Homologue ID");
		columnsNames.add("Similarity");
		results[2] = new DataTable(columnsNames, "Orthologs");

		
		columnsNames = new ArrayList<String>();
		columnsNames.add("Encoded proteins");
		columnsNames.add("Class");
		columnsNames.add("EC/TC number");
		results[3] = new DataTable(columnsNames, "Encoded proteins");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Compartment");
		columnsNames.add("Score");
		columnsNames.add("Primary Location");
		results[4] = new DataTable(columnsNames, "Compartments");

		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT alias FROM aliases WHERE class = 'g' AND entity = "+id);

			while(rs.next()) {

				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				results[0].addLine(resultsList);
			}

			rs = stmt.executeQuery(
					"SELECT protein.name " +
							"FROM regulatory_event as event, transcription_unit, transcription_unit_gene " +
							"AS tug, transcription_unit_promoter as tup, promoter,gene, protein " +
							"WHERE event.promoter_idpromoter=idpromoter AND tup.promoter_idpromoter=idpromoter " +
							"AND tup.transcription_unit_idtranscription_unit=idtranscription_unit " +
							"AND tug.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
							"gene_idgene=idgene AND protein.idprotein = event.protein_idprotein AND idgene = "+id
					);

			while(rs.next()) {

				regulation = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				results[1].addLine(resultsList);
			}
			
			rs = stmt.executeQuery(
					"SELECT protein.name, protein.class, enzyme_ecnumber FROM subunit JOIN protein ON " +
							"subunit.enzyme_protein_idprotein = protein.idprotein WHERE subunit.gene_idgene = "+id
					);

			while(rs.next()) {

				eprotein = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				resultsList.add(rs.getString(2));
				resultsList.add(rs.getString(3));
				results[3].addLine(resultsList);
			}

			rs = stmt.executeQuery(
					"SELECT entry_id, locus_id, similarity FROM gene_has_orthology " +
					"JOIN orthology ON orthology_id = orthology.id " +
					"WHERE gene_idgene = "+id
					);

			while(rs.next()) {

				orthology = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				resultsList.add(rs.getString(2));
				resultsList.add(rs.getString(3));
				results[2].addLine(resultsList);
			}

			rs = stmt.executeQuery("SELECT idgene, compartment.name, primaryLocation, score " +
					"FROM gene " +
					"INNER JOIN gene_has_compartment ON (idgene = gene_has_compartment.gene_idgene) " +
					"INNER JOIN compartment ON (idcompartment = compartment_idcompartment) " +
					"WHERE idgene = " + id);

			while(rs.next()) {
				ecompartment=true;
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(2));
				ql.add(rs.getString(4));
				if(rs.getBoolean(3)) {

					ql.add(rs.getBoolean(3)+"");
				}
				else {

					ql.add("");
				}
				results[4].addLine(ql);
			}

			rs.close();
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		if(regulation) tabs++;
		if(orthology) tabs++;
		if(eprotein) tabs++;
		if(ecompartment) tabs++;

		results2 = new DataTable[tabs];

		tabs = 0;

		if(eprotein) {

			results2[tabs] = results[3];
			tabs++;
		}

		if(orthology) {

			results2[tabs] = results[2];
			tabs++;
		}
		
		results2[tabs] = results[0];

		if(regulation) {
			tabs++;
			results2[tabs] = results[1];

		}

		if(ecompartment) {
			tabs++;
			results2[tabs] = results[4];

		}

		return results2;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneName(String id) {

		return this.names.get(id);
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getGeneData(int selectedRow) {

		Statement stmt;
		String[][] res = null;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT idgene, chromosome.name, gene.name, transcription_direction, left_end_position, right_end_position, boolean_rule, locusTag " +
					"FROM chromosome JOIN gene ON (idchromosome = chromosome_idchromosome) WHERE idgene ="+ids.get(selectedRow));

			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			res = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();

			int row=0;
			while(row<res.length)
			{
				int col=1;
				while(col<rsmd.getColumnCount()+1)
				{
					res[row][col-1] = rs.getString(col);
					col++;
				}
				rs.next();
				row++;
			}

			rs.close();            
			stmt.close();

		}
		catch (SQLException ex)
		{
			// handle any errors



		}

		String[] data = new String[8];

		int i=0;
		while(i<8) {

			data[i]=res[0][i];
			i++;
		}

		//		res = this.getDbt().getDsa().select( "SELECT idProtein FROM Protein JOIN subunit ON (idProtein = protein_idProtein) WHERE gene_idgene ="+ids.get(selectedRow));
		//
		//		if(res.length>0)
		//		{
		//			data[6]=res[0][0];
		//		}


		return data;
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getSubunits(int selectedRow) {

		Statement stmt;
		String[][] res = null;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT enzyme_protein_idProtein, enzyme_ecNumber FROM subunit WHERE gene_idgene ="+ids.get(selectedRow));

			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			res = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();

			int row=0;
			while(row<res.length) {

				res[row][0] = rs.getString(1)+"__"+rs.getString(2);
				rs.next();
				row++;
			}

			rs.close();            
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		String[] sub;

		if(res.length>0) {

			sub = new String[res.length];

			for(int i=0; i<res.length;i++) {

				sub[i]=res[i][0];
			}
		}
		else {

			sub = new String[0];
		}
		return sub;
	}

	/**
	 * @param idChromosome
	 * @param name
	 * @param transcription_direction
	 * @param left_end_position
	 * @param right_end_position
	 * @param boolean_rule
	 * @param subunits
	 * @param locusTag
	 */
	public void insertNewGene(String idChromosome, String name,
			String transcription_direction, String left_end_position,
			String right_end_position, String[] subunits,String locusTag ) {

		Statement stmt; 

		try {

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT * FROM chromosome WHERE idchromosome = "+idChromosome);

			if(!rs.next()) {

				stmt.execute("INSERT INTO chromosome (name) VALUES('null')");
			}

			if(left_end_position.equals("")) {

				stmt.execute("INSERT INTO gene (chromosome_idchromosome, name, locusTag, origin) VALUES('"+idChromosome+"', '"+name+"', '"+locusTag+"','MANUAL')");
			}			
			else {

				stmt.execute("INSERT INTO gene (chromosome_idchromosome, name, transcription_direction, left_end_position, right_end_position, locusTag,origin) " +
						"VALUES('"+idChromosome+"', '"+Connection.mysqlStrConverter(name)+"', '"+transcription_direction+"', '"+Connection.mysqlStrConverter(left_end_position)+"', '"+
						Connection.mysqlStrConverter(right_end_position)+"',  '"+locusTag+"','MANUAL')");
			}
			String idNewGene = (this.select("SELECT LAST_INSERT_ID()"))[0][0];

			for(int s=0; s<subunits.length;s++) {

				if(!subunits[s].equals("dummy") && !subunits[s].isEmpty()) {

					//					rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+subunits[s]);
					//					List<String> ecn = new ArrayList<String>();
					//
					//					while(rs.next()) {
					//
					//						ecn.add(rs.getString(1));
					//					}
					//
					//					for(String e:ecn) {

					String protein_id=subunits[s].split("__")[0];
					String e=subunits[s].split("__")[1];

					stmt.execute("INSERT INTO subunit (enzyme_protein_idprotein, gene_idgene, enzyme_ecnumber) VALUES("+"'" + protein_id +"', '"+idNewGene+"', '"+e+"')");

					rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
							"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
							"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
							"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
							"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
							"AND reaction_has_enzyme.enzyme_protein_idprotein = "+ protein_id +" " +
							"AND reaction_has_enzyme.enzyme_ecnumber = '"+e+"'");

					Set<String> reactions_ids = new HashSet<String>();

					while(rs.next()) {

						reactions_ids.add(rs.getString(1));
					}

					rs= stmt.executeQuery("SELECT idreaction FROM reactions_view_noPath_or_noEC " +
							"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
							"WHERE enzyme_protein_idprotein = "+protein_id+" AND enzyme_ecnumber = '"+e+"'");

					while(rs.next()) {

						reactions_ids.add(rs.getString(1));
					}

					for(String idreaction: reactions_ids) {

						stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = '"+idreaction+"'");
					}
					//}
				}
			}
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();

		}
	}

	/**
	 * @param idChromosome
	 * @param name
	 * @param transcription_direction
	 * @param left_end_position
	 * @param right_end_position
	 * @param subunits
	 * @param selectedRow
	 * @param oldSubunits
	 * @param locusTag
	 */
	public void updateGene(String idChromosome, String name,
			String transcription_direction, String left_end_position,
			String right_end_position, String[] subunits, int selectedRow, String[] oldSubunits, String locusTag ) {

		Statement stmt; 
		try {

			stmt = this.connection.createStatement();

			if(left_end_position.equals("")) {

				stmt.execute("UPDATE gene SET " +
						"chromosome_idchromosome ='" + idChromosome + "', " +
						"name = '" + Connection.mysqlStrConverter(name) + "', " +
						//"boolean_rule ='" + boolean_rule + "', " +
						"locusTag = '"+locusTag+"' "+
						"WHERE  idgene ="+ids.get(selectedRow));
			}			
			else {

				stmt.execute("UPDATE gene SET "+
						"chromosome_idchromosome ='" + idChromosome + "', " +
						"name = '" +  Connection.mysqlStrConverter(name)+ "', " +
						"transcription_direction = '" + transcription_direction + "', " +
						"left_end_position ='" +  Connection.mysqlStrConverter(left_end_position) +  "', " +
						"right_end_position='" + Connection.mysqlStrConverter(right_end_position) + "', "+ 
						//"boolean_rule = '" + boolean_rule + "', " +
						"locusTag = '"+locusTag+"' "+
						"WHERE idgene =" + ids.get(selectedRow));
			}

			List<String> old_protein_ids = new ArrayList<String>();
			List<String> protein_ids = new ArrayList<String>();

			int i = 0;
			for(String id : oldSubunits) {

				old_protein_ids.add(i,id);
				i++;
			}

			i = 0;
			for(String id : subunits) {

				protein_ids.add(i,id);
				i++;
			}

			List<String> subunit_protein_id_add = new ArrayList<String>();

			for(String id : protein_ids) {

				if(!id.contains("dummy") && !id.isEmpty()) {

					if(old_protein_ids.contains(id)) {

						old_protein_ids.remove(id);
					}
					else {

						subunit_protein_id_add.add(id);
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : old_protein_ids) {

				this.removeGeneAssignemensts(ids.get(selectedRow), id, stmt);
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String protein_id_ec : protein_ids) {

				if(subunit_protein_id_add.contains(protein_id_ec)) {

					//					ResultSet rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+protein_id);
					//					List<String> ecn = new ArrayList<String>();
					//
					//					while(rs.next()) {
					//
					//						ecn.add(rs.getString(1));
					//					}
					//
					//					for(String ecnumber:ecn) {

					String protein_id=protein_id_ec.split("__")[0];
					String ecnumber=protein_id_ec.split("__")[1];

					stmt.execute("INSERT INTO subunit (enzyme_protein_idprotein, gene_idgene, enzyme_ecnumber) VALUES(" + protein_id +", '"+ids.get(selectedRow)+"', '"+ecnumber+"')");

					Proteins.insertEnzymes(protein_id,ecnumber,stmt,true);

					//						stmt.execute("UPDATE enzyme SET inModel = true, source = 'MANUAL' WHERE ecnumber = '"+ecnumber+"' AND protein_idprotein = " + protein_id);
					//
					//						rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
					//								"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
					//								"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
					//								"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
					//								"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
					//								"AND reaction_has_enzyme.enzyme_protein_idprotein = "+protein_id + " " +
					//								"AND reaction_has_enzyme.enzyme_ecnumber = '"+ecnumber+"'");
					//
					//						Set<String> reactions_ids = new HashSet<String>();
					//
					//						while(rs.next()) {
					//
					//							reactions_ids.add(rs.getString(1));
					//						}
					//
					//						rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
					//								"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
					//								"WHERE enzyme_protein_idprotein = "+protein_id+" AND enzyme_ecnumber = '"+ecnumber+"'");
					//
					//						while(rs.next()) {
					//
					//							reactions_ids.add(rs.getString(1));
					//						}
					//
					//						for(String idreaction: reactions_ids) {
					//
					//							stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction);
					//						}
					//}
				}

				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			}
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * @param selectedRow
	 * @param id
	 * @param stmt 
	 * @throws SQLException
	 */
	private void removeGeneAssignemensts(String gene_id, String id, Statement stmt) throws SQLException {

		stmt.execute("DELETE FROM subunit WHERE gene_idgene = "+gene_id+" AND enzyme_protein_idprotein = " + id.split("__")[0]+" AND enzyme_ecNumber = '" + id.split("__")[1]+"'" );

		ResultSet rs = stmt.executeQuery("SELECT * FROM subunit WHERE enzyme_protein_idprotein = " + id.split("__")[0]);

		boolean exists = rs.next();

		if(!exists) {

			//rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+id);
			List<String> enzymes_ids = new ArrayList<String>();
			enzymes_ids.add(id.split("__")[1]);
			
//			while(rs.next()) {
//
//				enzymes_ids.add(rs.getString(1));
//			}

			Boolean[] inModel = new Boolean[enzymes_ids.size()];
			for(int i= 0; i< inModel.length; i++) {

				inModel[i]=false;
			}


			for(String e:enzymes_ids) {

				Proteins.removeEnzymesAssignmensts(e, enzymes_ids, inModel, stmt,  id.split("__")[0], false);

				//				stmt.execute("UPDATE enzyme SET inModel = false, source = 'MANUAL' WHERE ecnumber = '"+e+"' AND protein_idprotein = " + id);
				//
				//				rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
				//						"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
				//						"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
				//						"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
				//						"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
				//						"AND reaction_has_enzyme.enzyme_protein_idprotein = "+id +" " +
				//						"AND reaction_has_enzyme.enzyme_ecnumber = '"+e+"'");
				//
				//				Set<String> reactions_ids = new HashSet<String>();
				//
				//				while(rs.next()) {
				//
				//					reactions_ids.add(rs.getString(1));
				//				}
				//
				//				rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
				//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
				//						"WHERE enzyme_protein_idprotein = "+id+" AND enzyme_ecnumber = '"+e+"'");
				//
				//				while(rs.next()) {
				//
				//					reactions_ids.add(rs.getString(1));
				//				}
				//
				//
				//				for(String idreaction: reactions_ids) {
				//
				//					List<String[]> proteins_array = new ArrayList<String[]>();
				//
				//					rs= stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme " +
				//							"LEFT JOIN enzyme ON (enzyme_protein_idprotein = enzyme.protein_idprotein AND enzyme_ecnumber = enzyme.ecnumber)"+
				//							"WHERE inModel AND reaction_idreaction = "+idreaction);
				//
				//					while(rs.next()) {
				//
				//						if(rs.getString(1).equalsIgnoreCase(id) && ecn.contains(rs.getString(2))) {}
				//						else {
				//
				//							proteins_array.add(new String[] {rs.getString(1),rs.getString(2)});
				//						}
				//					}
				//
				//					if(proteins_array.isEmpty()) {
				//
				//						stmt.execute("UPDATE reaction SET inModel = false, source = 'MANUAL' WHERE idreaction = "+idreaction);
				//					}
				//				}
			}
		}
	}
}