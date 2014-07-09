package datatypes.metabolic;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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
public class EnzymesContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;

	/**
	 * @param table
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public EnzymesContainer(Table table, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int enz_num=0,hom_num=0,kegg_num=0,man_num=0;
		int trans_num=0;
		int encoded_enz=0, encoded_hom=0, encoded_kegg=0, encoded_man=0, encoded_trans=0;

		String[][] res = new String[11][];

		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "+this.table.getName());			

			while(rs.next()) {
				
				if(rs.getString("source").equalsIgnoreCase("TRANSPORTERS")) {
					
					trans_num++;
					
					if(rs.getBoolean("inModel")){

						encoded_trans++;
					}
				}
				else {
					
					if(rs.getString("source").equalsIgnoreCase("KEGG")) {
						
						kegg_num++;
						
						if(rs.getBoolean("inModel")){

							encoded_kegg++;
							encoded_enz++;
							
						}
					}
					
					if(rs.getString("source").equalsIgnoreCase("HOMOLOGY")) {
						
						hom_num++;
						
						if(rs.getBoolean("inModel")){

							encoded_hom++;
							encoded_enz++;
						}
					}
					
					if(rs.getString("source").equalsIgnoreCase("MANUAL")) {
						
						man_num++;
						
						if(rs.getBoolean("inModel")){

							encoded_man++;
							encoded_enz++;
						}
					}
					
					enz_num++;
				}
			}

			res[0] = new String[] {"Total number of enzymes", ""+enz_num};
			res[1] = new String[] {"      From homology", ""+hom_num};
			res[2] = new String[] {"      From KEGG", ""+kegg_num};
			res[3] = new String[] {"      Added manually", ""+man_num};
			res[4] = new String[] {"Total number of encoded enzymes", ""+encoded_enz};
			res[5] = new String[] {"      From homology", ""+encoded_hom};
			res[6] = new String[] {"      From KEGG", ""+encoded_kegg};
			res[7] = new String[] {"       Added manually", ""+encoded_man};
			res[9] = new String[] {"Total number of carrier-reaction pairs", ""+trans_num};
			res[10] = new String[] {"Total number of encoded carrier-reaction pairs", ""+encoded_trans};

			rs.close();
			stmt.close();
		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}

	/**
	 * @param encoded
	 * @return
	 */
	public GenericDataTable getAllEnzymes(boolean encoded) {

		this.names = new HashMap<String, String>();
		List<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Names");
		columnsNames.add("EC numbers");
		columnsNames.add("Number of reactions");
		columnsNames.add("Source");
		columnsNames.add("Encoded in Genome");
		columnsNames.add("Catalysing reactions in Model");


		GenericDataTable enzymeDataTable = new GenericDataTable(columnsNames, "Enzymes",""){
			private static final long serialVersionUID = 8668268767599264758L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0){return true;}
				else {return false;}
			}
		};

		
		String originalReaction = "";
		if(this.getProject().isCompartmentalisedModel()) {

			originalReaction = originalReaction.concat(" WHERE NOT originalReaction ");
		}
		else {

			originalReaction = originalReaction.concat(" WHERE originalReaction ");
		}
		
		String encodedEnzyme="";
		if(encoded) { 

			encodedEnzyme=" AND enzyme.inModel ";
		}
		
		ResultSet rs;
		try {
			

			Statement stmt = this.connection.createStatement();
			rs = stmt.executeQuery("SELECT protein.name, enzyme.ecnumber," +
					"COUNT(DISTINCT(reaction_has_enzyme.reaction_idreaction)), enzyme.source, enzyme.inModel, reaction.inModel, idprotein," +
					"count(DISTINCT(reaction.inModel)) " +
					"FROM enzyme " +
					"LEFT JOIN protein ON protein.idprotein = enzyme.protein_idprotein " +
					"LEFT JOIN reaction_has_enzyme ON ecnumber = reaction_has_enzyme.enzyme_ecnumber " +
					"AND protein.idprotein = reaction_has_enzyme.enzyme_protein_idprotein " +
					"LEFT JOIN reaction ON reaction.idreaction = reaction_has_enzyme.reaction_idreaction " +
					originalReaction+encodedEnzyme+
					" GROUP BY idprotein, ecnumber " +
					" ORDER BY ecnumber  ASC, reaction.inModel DESC");
			
			while(rs.next()) {

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");

				for(int i=1;i<7;i++) {

					if(i>4 && i<7) {
						
						if(i==6) {
							
							if(rs.getBoolean(i)==false && rs.getInt(8)==1) {
							
								ql.add(false);
							}
							else {
								
								ql.add(true);
							}
							
						}
						else {
							
							ql.add(rs.getBoolean(i));
						}
					}
					else {

						String aux = rs.getString(i);

						if(aux!=null) 
							ql.add(aux);
						else 
							ql.add("");	
					}
				}
				enzymeDataTable.addLine(ql,rs.getString(7));
				this.names.put(rs.getString(7), rs.getString(1));
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}

		return enzymeDataTable;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		columnsNames.add("Names");
		columnsNames.add("ECnumber");
		columnsNames.add("Optimal pH");
		columnsNames.add("Post translational modification");
		columnsNames.add("Number of coding genes");

		GenericDataTable res = new GenericDataTable(columnsNames, "TUs", "TU");

		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT idprotein, protein.name, ecnumber, optimal_ph, posttranslational_modification " +
							"FROM enzyme " +
							"LEFT JOIN protein ON protein.idprotein = enzyme.protein_idprotein"
					);

			while(rs.next())
			{
				String[] ql = new String[5];
				if(rs.getString(2)!=null) ql[0] = rs.getString(2);
				else ql[0] = "";
				if(rs.getString(3)!=null) ql[1] = rs.getString(3);
				else ql[1] = "";
				if(rs.getString(4)!=null) ql[2] = rs.getString(4);
				else ql[2] = "";
				if(rs.getString(5)!=null) ql[3] = rs.getString(5);
				else ql[3] = "";
				ql[4] = "0";
				index.add(rs.getString(1));
				qls.put(rs.getString(1), ql);
			}

			rs = stmt.executeQuery(
					"SELECT enzyme.protein_idprotein, COUNT(gene_idgene) " +
							"FROM enzyme " +
							"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " +
							"GROUP BY enzyme.protein_idprotein"
					);

			while(rs.next()) {

				qls.get(rs.getString(1))[4] = rs.getString(2);
			}

			for(int i=0;i<index.size();i++) {

				List<Object> ql = new ArrayList<Object>();
				String[] enzymeData = qls.get(index.get(i));
				ql.add(enzymeData[0]);
				ql.add(enzymeData[1]);
				ql.add(enzymeData[2]);
				ql.add(enzymeData[3]);
				ql.add(enzymeData[4]);
				res.addLine(ql, index.get(i));
				this.names.put(index.get(i), enzymeData[0]);
			}
			rs.close();
			stmt.close();

		}
		catch(Exception e){e.printStackTrace();}

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchData()
	 */
	public HashMap<Integer,Integer[]> getSearchData() {

		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchDataIds()
	 */
	public String[] getSearchDataIds() {

		String[] res = new String[]{"Name", "ECnumber"};

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {

		return true;
	}

	/**
	 * @param ecnumber
	 * @param id
	 * @return
	 */
	public DataTable[] getRowInfo(String ecnumber, String id) {

		//String id = this.index.get(Integer.parseInt(row));
		DataTable[] datatables = new DataTable[6];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Reaction");
		columnsNames.add("Equation");
		columnsNames.add("Source");
		columnsNames.add("in Model");
		columnsNames.add("Reversible");
		datatables[0] = new DataTable(columnsNames, "Encoded Reactions");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Name");
		columnsNames.add("Locus tag");
		columnsNames.add("KO");
		columnsNames.add("Origin");
		columnsNames.add("Notes");
		columnsNames.add("Similarity");
		columnsNames.add("Orthologue");
		datatables[1] = new DataTable(columnsNames, "Encoding genes");	
		
		columnsNames = new ArrayList<String>();
		columnsNames.add("GPR status");
		columnsNames.add("Reaction");
		columnsNames.add("Rule");
		columnsNames.add("Module Name");
		datatables[2] = new DataTable(columnsNames, "Gene-Protein-Reaction");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("Pathway ID");
		columnsNames.add("Pathway Name");
		datatables[3] = new DataTable(columnsNames, "Pathways");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("Synonyms");
		datatables[4] = new DataTable(columnsNames, "Synonyms");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Locus tag");
		columnsNames.add("Compartment");
		columnsNames.add("Score");
		columnsNames.add("Primary Location");
		datatables[5] = new DataTable(columnsNames, "Compartments");

		try {

			Statement stmt = this.connection.createStatement();
			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" AND NOT originalReaction ");
			}
			else {

				aux = aux.concat(" AND originalReaction ");
			}
			ResultSet rs;
			rs = stmt.executeQuery(
					"SELECT reaction.name, reaction.equation, source, inModel, reversible FROM reaction " +
							"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = reaction.idreaction " +
							"WHERE reaction_has_enzyme.enzyme_ecnumber = '" + ecnumber+"' " +
							"AND reaction_has_enzyme.enzyme_protein_idprotein = " + id
							+aux+"" +
									" ORDER BY inModel DESC, reversible DESC, name");
			
			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				if(rs.getBoolean(4)) {
					
					ql.add("true");
				}
				else {
					
					ql.add("-");
				}
				if(rs.getBoolean(5)) {
					
					ql.add("true");
				}
				else {
					
					ql.add("-");
				}
				datatables[0].addLine(ql);
			}

			rs = stmt.executeQuery(
					"SELECT DISTINCT gene.name, gene.locusTag, orthology.entry_id, origin, note, similarity, locus_id FROM enzyme " +
							"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " +
							"LEFT JOIN gene ON gene.idgene = subunit.gene_idgene " +
							"LEFT JOIN gene_has_orthology ON gene.idgene = gene_has_orthology.gene_idgene " +
							"LEFT JOIN orthology ON orthology.id = orthology_id " +
							"WHERE subunit.enzyme_ecnumber = '" + ecnumber+"' " +
							"AND subunit.enzyme_protein_idprotein = " + id
							);

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));
				ql.add(rs.getString(6));
				ql.add(rs.getString(7));
				datatables[1].addLine(ql);
			}

			rs = stmt.executeQuery(
					"SELECT DISTINCT code, name FROM pathway_has_enzyme " +
							"LEFT JOIN pathway ON pathway_has_enzyme.pathway_idpathway = pathway.idpathway " +
							"WHERE pathway_has_enzyme.enzyme_ecnumber = '" + ecnumber+"' " +
							"AND pathway_has_enzyme.enzyme_protein_idprotein = " + id);

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				datatables[3].addLine(ql);
			}
			
			rs = stmt.executeQuery(
					"SELECT DISTINCT gpr_status, reaction,  definition, name FROM subunit " +
							"LEFT JOIN module ON (id = module_id) " +
							"WHERE enzyme_ecnumber = '" + ecnumber+"' " +
							"AND enzyme_protein_idprotein = " + id);

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				datatables[2].addLine(ql);
			}

			rs = stmt.executeQuery("SELECT alias FROM aliases WHERE class = 'p' AND entity = " + id);

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				datatables[4].addLine(ql);
			}

			rs = stmt.executeQuery("SELECT idgene, compartment.name, primaryLocation, score, locusTag " +
					"FROM gene " +
					"INNER JOIN gene_has_compartment ON (idgene = gene_has_compartment.gene_idgene) " +
					"INNER JOIN compartment ON (idcompartment = compartment_idcompartment)" +
					"LEFT JOIN subunit ON subunit.gene_idgene = idgene " +
					"WHERE subunit.enzyme_ecnumber = '" + ecnumber+"' " +
							"AND subunit.enzyme_protein_idprotein = " + id
							);

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(5));
				ql.add(rs.getString(2));
				ql.add(rs.getString(4));
				if(rs.getBoolean(3)) {

					ql.add(rs.getBoolean(3)+"");
				}
				else {

					ql.add("");
				}
				datatables[5].addLine(ql);
			}

			rs.close();
			stmt.close();

		}
		catch(Exception e) {

			e.printStackTrace();
		}

		return datatables;
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

		return "Enzyme: ";
	}
}
