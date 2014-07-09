/**
 * 
 */
package datatypes.metagenomics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;


/**
 * @author pedro
 *
 */
public class EnzymesMetaContainer extends Entity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;
	//private Connection connection; 
	private HashMap<String, String[]> allenzymesdata;
	private double median;
	private TaxonomyMetaContainer taxonomy;

	public EnzymesMetaContainer(Table table, String name, TaxonomyMetaContainer taxonomy) {
		super(table, name);
		this.connection=table.getConnection();
		this.taxonomy = taxonomy;

	}

//		/**
//		 * 
//		 * @param conn
//		 * @param taxonomy
//		 */
//		public EnzymesMetaContainer(Connection conn, TaxonomyMetaContainer taxonomy){
//			this.connection = conn;
//			this.taxonomy = taxonomy;
//		}


	/**
	 * @return
	 */
	public GenericDataTable getEnzymesData() {
		//public GenericDataTable getEnzymesData(TaxonomyMetaContainer taxonomy) {
		//this.taxonomy.getPhylogenyData(5, 0.5, 0.3);
		LinkedHashMap<String, String[]> taxonomyData = this.taxonomy.getAlltaxonomydata();
		//		if(taxonomy.getAlltaxonomydata() == null){
		//			System.out.println("Warning !!!!  tax falta");
		//			return null;
		//		}
		this.allenzymesdata = new HashMap<String, String[]>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		columnsNames.add("Info");
		columnsNames.add("Names");
		columnsNames.add("ECnumber");
		columnsNames.add("Nº of reactions");
		columnsNames.add("Nº of genes");	
		columnsNames.add("Abundance");
		columnsNames.add("Nº of genes encoded by genus");
		columnsNames.add("Nº of genes with no genus");

		GenericDataTable res = new GenericDataTable(columnsNames, "MetaEnzymes", "Enzymes data"){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L ;


			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}};


			try {

				Statement stmt = this.connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT protein_idprotein,protein.name, enzyme.ecnumber," +
						"COUNT(DISTINCT(reaction_has_enzyme.reaction_idreaction)) " +
						"FROM enzyme LEFT JOIN protein ON protein.idprotein = enzyme.protein_idprotein " +
						"LEFT JOIN reaction_has_enzyme ON ecnumber = reaction_has_enzyme.enzyme_ecnumber " +
						"AND protein.idprotein = reaction_has_enzyme.enzyme_protein_idprotein " +
						"LEFT JOIN reaction ON reaction.idreaction = reaction_has_enzyme.reaction_idreaction " +
						"WHERE enzyme.inModel = 1 " + // AND reaction_has_enzyme.reaction_idreaction > 0" +
						" GROUP BY idprotein, ecnumber " +
						" ORDER BY ecnumber  ASC"); //Excludes enzymes that were not assigned to any reaction reaction_has_enzyme.reaction_idreaction > 0

				while(rs.next())
				{
					String[] ql = new String[8];
					if(rs.getString(2)!=null) ql[0] = rs.getString(2);//ecnumber_name
					else ql[0] = "";
					if(rs.getString(3)!=null) ql[1] = rs.getString(3);//ecnumber
					else ql[1] = "";
					if(rs.getString(4)!=null) ql[2] = rs.getString(4);// number reactions
					else ql[2] = "";
					index.add(rs.getString(1)); //protein_id
					qls.put(rs.getString(1), ql);
				}

				
//              #################################### ENZYMES ABUNDANCE ##########################
//				int num_genes = 0;
//				rs = stmt.executeQuery("SELECT COUNT(DISTINCT(s_key)) FROM geneHomology");// TOTAL NUMBER OF GENES
//				while(rs.next()){
//					num_genes = rs.getInt(1);	
//				}
				
				int metabolic_genes = 0;
				rs = stmt.executeQuery("SELECT COUNT(DISTINCT(locusTag)) FROM subunit " +
									"LEFT JOIN gene ON (gene.idgene = subunit.gene_idgene) "	//TOTAL NUMBER OF EC NUMBER GENES																					
						);
				while(rs.next()){
				metabolic_genes = rs.getInt(1);	
			}
				
				rs = stmt.executeQuery(
						"SELECT enzyme.protein_idprotein, COUNT(gene_idgene) " +
								"FROM enzyme " +
								"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " +
								"GROUP BY enzyme.protein_idprotein"
						);

				while(rs.next()) {

					if(qls.containsKey(rs.getString(1))){
						qls.get(rs.getString(1))[3] = rs.getString(2); //genes count

						//double abundance = (double) rs.getInt(2)/num_genes; //considering all genes
						//double abundance = (double) rs.getInt(2)/taxonomy.getNoSimilarities(); //discarding no similarities
						double abundance = (double) rs.getInt(2)/metabolic_genes; //considering only genes encoding ecnumber
						BigDecimal bd = new BigDecimal(abundance).setScale(4, RoundingMode.HALF_EVEN);
						abundance = bd.doubleValue();

						qls.get(rs.getString(1))[4] = Double.toString(abundance); //abundance


					}
					else{
						continue;
					}

				}


				//			######### GENUS ENCODING ###############

				for(int i=0;i<index.size();i++) {
					String ecnumber = qls.get(index.get(i))[1];
					int nogenus = 0, genes4ecnumber = 0;
					HashMap <String, Double> genusWithecnumber = new HashMap<>();
					HashMap <String, Double> genusWithecnumberNEW = new HashMap<>();
					String result = "";


					rs = stmt.executeQuery(
							"SELECT DISTINCT gene.locusTag FROM enzyme " +
									"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " + 
									"LEFT JOIN gene ON gene.idgene = subunit.gene_idgene " + 
									"WHERE subunit.enzyme_ecnumber = '"+ecnumber+"'"
							);


					while(rs.next()){

						genes4ecnumber++;
						String gene_id = rs.getString(1);
						if(taxonomyData.containsKey(gene_id)){

							if( !taxonomyData.get(gene_id)[4].contains("<") && taxonomyData.get(gene_id)[5].equals("true")){

								String genus = taxonomyData.get(gene_id)[3].replace(" ", "");

								if(genusWithecnumber.containsKey(genus)){
									genusWithecnumber.put(genus, genusWithecnumber.get(genus) + 1.0);
								}
								else{
									genusWithecnumber.put(genus, 1.0);
								}

							}
							else{

								nogenus++;
							}

						}
					}

					if(genusWithecnumber.size() == 0){
						qls.get(index.get(i))[6] = "No genus minimum score";
					}
					else{


						ArrayList<Double> proportions = new ArrayList<>();
						for(String genus : genusWithecnumber.keySet()){
							genusWithecnumberNEW.put(genus, (double) genusWithecnumber.get(genus)/genes4ecnumber * 100);
							proportions.add(genusWithecnumberNEW.get(genus));
						}

						Collections.sort(proportions);
						Collections.reverse(proportions);

						for(Double prop : proportions){
							for(String genus : genusWithecnumberNEW.keySet()){
								if(genusWithecnumberNEW.get(genus) == prop){

									BigDecimal bd = new BigDecimal(prop).setScale(3, RoundingMode.HALF_EVEN);
									prop = bd.doubleValue();
									result += genus + "(" + prop + "%);";

									genusWithecnumberNEW.remove(genus);
									break;
								}
								else{
									continue;
								}
							}

						}

						qls.get(index.get(i))[5] = result.substring(0, result.length()-1); //genus encoding this ecnumber


						int genesWithgenus = 0;
						for(String genus : genusWithecnumber.keySet())	genesWithgenus += genusWithecnumber.get(genus);

						Double withgenusprop = (double)genesWithgenus/genes4ecnumber*100;	
						BigDecimal bd = new BigDecimal(withgenusprop).setScale(3, RoundingMode.HALF_EVEN);
						withgenusprop = bd.doubleValue();
						qls.get(index.get(i))[6] = Integer.toString(genesWithgenus) + " (" + withgenusprop + "%)";//genes possible to infer genus

					}	


					Double nogenusprop = (double)nogenus/genes4ecnumber*100;
					BigDecimal bd = new BigDecimal(nogenusprop).setScale(3, RoundingMode.HALF_EVEN);
					nogenusprop = bd.doubleValue();
					qls.get(index.get(i))[7] = Integer.toString(nogenus) + " (" + nogenusprop + "%)"; //genes not possible to infer genus
				}


				for(int i=0;i<index.size();i++) {

					List<Object> ql = new ArrayList<Object>();
					String[] enzymeData = qls.get(index.get(i));
					ql.add("");
					ql.add(enzymeData[0]);//ecnumber_name
					ql.add(enzymeData[1]);//ecnumber
					ql.add(enzymeData[2]);// number reactions
					ql.add(enzymeData[3]);//genes count
					ql.add(enzymeData[4]);//abundance
					//ql.add(enzymeData[5]);//string of genus 
					ql.add(enzymeData[6]);//genus
					ql.add(enzymeData[7]);//nogenus
					res.addLine(ql, index.get(i));
					this.allenzymesdata.put(index.get(i), enzymeData);
				}
				rs.close();
				stmt.close();

			}
			catch(Exception e){e.printStackTrace();}

			return res;
	}


	/**
	 * @return
	 */
	public String[][] getStats() {


		int total_enzymes = 0,oxidoreductases=0,transferases=0,hydrolases=0,lyases=0, isomerases =0, ligases = 0;
		//int total_inc_enzymes = 0, inc_oxidoreductases=0, inc_transferases= 0, inc_hydrolases =0, inc_lyases = 0, inc_isomerases = 0, inc_ligases = 0;
		int numb_different_enzymes = 0,enzymesAllgenus = 0, enzymesNoGenus = 0, enzymesSomegenus = 0;

		String[][] res = new String[12][];

		try {

			//Statement stmt = this.connection.createStatement();

			if(this.allenzymesdata != null){
				//			if (! this.allenzymesdata.isEmpty()){

				for (String id : this.allenzymesdata.keySet()){

					String ecNumber = this.allenzymesdata.get(id)[1]; 
					int ecnumber_counts = Integer.parseInt(this.allenzymesdata.get(id)[3]);
					int endindex = this.allenzymesdata.get(id)[7].indexOf(" ");
					int nogenus = Integer.parseInt(this.allenzymesdata.get(id)[7].substring(0, endindex));


					total_enzymes+= ecnumber_counts;

					if(ecNumber.startsWith("1")) oxidoreductases+= ecnumber_counts;
					else if(ecNumber.startsWith("2")) transferases+= ecnumber_counts;
					else if(ecNumber.startsWith("3")) hydrolases+= ecnumber_counts;
					else if(ecNumber.startsWith("4")) lyases+= ecnumber_counts;
					else if(ecNumber.startsWith("5")) isomerases+= ecnumber_counts;
					else ligases+= ecnumber_counts;


					if(nogenus == 0){
						enzymesAllgenus++;
					}
					else if(nogenus == ecnumber_counts){
						enzymesNoGenus++;
					}
					else{
						enzymesSomegenus++;
					}

					numb_different_enzymes++;	
				}

				//				################## Fazer queries a base de dados para saber as enzimas incompletas ##################
				//				Statement stmt = this.connection.createStatement();
				//				ResultSet rs = stmt.executeQuery(
				//						
				//						);
				//				
				//				while (rs.next()){
				//					total_inc_enzymes+= ecnumber_counts;
				//
				//					if(ecNumber.startsWith("1")) inc_oxidoreductases+= ecnumber_counts;
				//					else if(ecNumber.startsWith("2")) inc_transferases+= ecnumber_counts;
				//					else if(ecNumber.startsWith("3")) inc_hydrolases+= ecnumber_counts;
				//					else if(ecNumber.startsWith("4")) inc_lyases+= ecnumber_counts;
				//					else if(ecNumber.startsWith("5")) inc_isomerases+= ecnumber_counts;
				//					else inc_ligases+= ecnumber_counts;
				//
				//				}

				res[0] = new String[] {"Enzymes present in the annotation of the metagenome (includes all the ocurrences of each enzyme)", ""+total_enzymes};
				res[1] = new String[] {"      Oxidoreductases(1.-)", ""+oxidoreductases};
				res[2] = new String[] {"      Transferases(2.-)", ""+transferases};
				res[3] = new String[] {"      Hydrolases(3.-)", ""+hydrolases};
				res[4] = new String[] {"	  Lyases(4.-)", ""+lyases};
				res[5] = new String[] {"      Isomerases(5.-)", ""+isomerases};
				res[6] = new String[] {"      Ligases(6.-)", ""+ligases};
				res[7] = new String[] {"", ""};
				res[8] = new String[] {"Number of different enzymes in the metagenome", ""+numb_different_enzymes};
				res[9] = new String[] {"Enzymes in which all their encoding genes have a taxonomic genus assignment",""+enzymesAllgenus};
				res[10] = new String[] {"Enzymes in which at least one of their encoding genes have a taxonomic genus assignment",""+enzymesSomegenus};
				res[11] = new String[] {"Enzymes in which none of their encoding genes have a taxonomic genus assignment",""+enzymesNoGenus};
				//				res[11] = new String[] {"", ""};
				//				res[12] = new String[] {"Number of incomplete enzymes present in the annotation of the metagenome", ""+total_inc_enzymes};
				//				res[13] = new String[] {"	  Oxidoreductases(1.-)", ""+inc_oxidoreductases};
				//				res[14] = new String[] {"	  Transferases(2.-)", ""+inc_transferases};
				//				res[15] = new String[] {"	  Hydrolases(3.-)", ""+inc_hydrolases};
				//				res[16] = new String[] {"	  Lyases(4.-)", ""+inc_lyases};	
				//				res[17] = new String[] {"	  Isomerases(5.-)", ""+inc_isomerases};
				//				res[18] = new String[] {"	  Ligases(6.-)", ""+inc_ligases};



				//stmt.close();
			}

			else{
				res[0] = new String [] {"Click HERE to refresh !!", ""};
			}
		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}


	/**
	 * @param ecnumber
	 * @param id
	 * @return
	 */
	public DataTable[] getRowInfo( String id) {

		LinkedHashMap<String, String[]> taxonomyData = taxonomy.getAlltaxonomydata();
		DataTable[] datatables = new DataTable[3];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("genus");
		columnsNames.add("number of genes");
		columnsNames.add("proportion");
		datatables[0] = new DataTable(columnsNames, "Encoded genus");


		columnsNames = new ArrayList<String>();
		//columnsNames.add("name");
		columnsNames.add("locus tag");
		//columnsNames.add("sequence");
		columnsNames.add("genus");
		columnsNames.add("genus score");
		datatables[1] = new DataTable(columnsNames, "Encoding genes");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("Reaction");
		columnsNames.add("Equation");
		columnsNames.add("in Model");
		columnsNames.add("Reversible");
		datatables[2] = new DataTable(columnsNames, "Encoded Reactions");



		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs;

			//#################### ENCODING GENUS #########################

			int genes4ecnumber = 0;
			HashMap <String, Integer> genusWithecnumber = new HashMap<>();
			HashMap <String, Double> genusWithecnumberNEW = new HashMap<>();

			rs = stmt.executeQuery(
					"SELECT DISTINCT gene.locusTag FROM enzyme " +
							"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " + 
							"LEFT JOIN gene ON gene.idgene = subunit.gene_idgene " + 
							"WHERE subunit.enzyme_protein_idprotein = '"+id+"'"
					);


			while(rs.next()){

				genes4ecnumber++;
				String gene_id = rs.getString(1);
				if(taxonomyData.containsKey(gene_id)){

					if( !taxonomyData.get(gene_id)[4].contains("<") && taxonomyData.get(gene_id)[5].equals("true")){

						String genus = taxonomyData.get(gene_id)[3].replace(" ", "");

						if(genusWithecnumber.containsKey(genus)){
							genusWithecnumber.put(genus, genusWithecnumber.get(genus) + 1);
						}
						else{
							genusWithecnumber.put(genus, 1);
						}

					}
				}
			}


			if(genusWithecnumber.size() != 0){

				ArrayList<Double> proportions = new ArrayList<>();
				for(String genus : genusWithecnumber.keySet()){
					genusWithecnumberNEW.put(genus, (double) genusWithecnumber.get(genus)/genes4ecnumber * 100);
					proportions.add(genusWithecnumberNEW.get(genus));
				}

				Collections.sort(proportions);
				Collections.reverse(proportions);

				for(Double prop : proportions){
					for(String genus : genusWithecnumber.keySet()){
						if(genusWithecnumberNEW.get(genus) == prop){

							BigDecimal bd = new BigDecimal(prop).setScale(3, RoundingMode.HALF_EVEN);
							prop = bd.doubleValue();
							ArrayList<String> ql = new ArrayList<String>();
							ql.add(genus);
							ql.add(genusWithecnumber.get(genus).toString());
							ql.add(prop + "%");
							datatables[0].addLine(ql);
							genusWithecnumber.remove(genus);
							break;
						}
						else{
							continue;
						}
					}

				}

			}	


			//#################### ENCODING GENES #########################

			rs = stmt.executeQuery("SELECT DISTINCT gene.name, gene.locusTag FROM enzyme " +
					"LEFT JOIN subunit ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein " +
					"LEFT JOIN gene ON gene.idgene = subunit.gene_idgene " +
					//"WHERE subunit.enzyme_ecnumber = '" + ecnumber+"' ");// +
					"WHERE subunit.enzyme_protein_idprotein = " + id);

			HashMap<String, String> encodedGenes = new HashMap<>();

			while(rs.next()) {
				String name = rs.getString(1);
				if (name == null){
					name ="Missing";
				}
				encodedGenes.put(rs.getString(2), name ); //locusTag && gene name
			}

			for ( String locusTag : encodedGenes.keySet()){

				rs = stmt.executeQuery("SELECT sequence FROM fastaSequence " +
						"LEFT JOIN geneHomology ON geneHomology.s_key = fastaSequence.geneHomology_s_key " +
						"WHERE locusTag = '" + locusTag+"'");

				while (rs.next()){
					ArrayList<String> ql = new ArrayList<String>();
					//ql.add(encodedGenes.get(locusTag));
					ql.add(locusTag);
					//ql.add(rs.getString(1)); //fastasequence
					if(taxonomyData.get(locusTag)[3].isEmpty()){
						ql.add("No homologues enough for genus inference");
					}
					else{
						ql.add(taxonomyData.get(locusTag)[3].replace(" ", ""));
					}

					if(taxonomyData.get(locusTag)[4].contains("<")){
						int index = taxonomyData.get(locusTag)[4].indexOf("(");
						ql.add(taxonomyData.get(locusTag)[4].substring(0, index));
					}
					else{
						ql.add(taxonomyData.get(locusTag)[4]);
					}
					datatables[1].addLine(ql);
				}
			}


			//######################### REACTIONS ##########################


			String aux = " AND originalReaction ";
			//			if(this.getProject().isCompartmentalisedModel()) {
			//
			//				aux = aux.concat(" AND NOT originalReaction ");
			//			}
			//			else {
			//
			//				aux = aux.concat(" AND originalReaction ");
			//			}

			rs = stmt.executeQuery(
					"SELECT reaction.name, reaction.equation, inModel, reversible FROM reaction " +
							"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = reaction.idreaction " +
							//"WHERE reaction_has_enzyme.enzyme_ecnumber = '" + ecnumber+"' " +
							"WHERE reaction_has_enzyme.enzyme_protein_idprotein = " + id
							+aux+"" +
					" ORDER BY inModel DESC, reversible DESC, name");

			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				//ql.add(rs.getString(3));
				if(rs.getBoolean(3)) {

					ql.add("true");
				}
				else {

					ql.add("-");
				}
				if(rs.getBoolean(4)) {

					ql.add("true");
				}
				else {

					ql.add("-");
				}
				datatables[2].addLine(ql);
			}

			rs.close();
			stmt.close();

		}							
		catch(Exception e) {

			e.printStackTrace();
		}

		return datatables;
	}


	/**
	 * 
	 * @return
	 */
	public DataTable getEnzymeCoverage(){

		ArrayList<String> columnsNames = new ArrayList<>();
		columnsNames.add("Ecnumber");
		columnsNames.add("Coverage");
		columnsNames.add("Number of genes");
		//columnsNames.add("Abundance");
		DataTable res = new DataTable(columnsNames, "Enzymes coverage");

		try{
	
			for(String enzyme : this.allenzymesdata.keySet()){
				ArrayList<String> ql = new ArrayList<>();
				ql.add(this.allenzymesdata.get(enzyme)[1]); //ecnumber
				ql.add("1"); // coverage
				ql.add(this.allenzymesdata.get(enzyme)[3]); // genes count
				//ql.add(this.allenzymesdata.get(enzyme)[4]); // abundance
				res.addLine(ql);
			}
//			Statement stmt = this.connection.createStatement();
//			//ResultSet rs = stmt.executeQuery("SELECT ecnumber, inModel from enzyme");
//			ResultSet rs = stmt.executeQuery("SELECT ecnumber, inModel from enzyme WHERE inModel = 1");
//			while (rs.next()){
//
//				ArrayList<String> ql = new ArrayList<>();
//				ql.add(rs.getString(1));
//				ql.add(rs.getString(2));
//				res.addLine(ql);
//			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}


	/**
	 * 
	 * @param file
	 * @param enz
	 * @throws IOException
	 */
	public void exportEnzymesCoverage(File file) throws IOException{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		DataTable cov = getEnzymeCoverage();
		int row = 0;
		while(row < cov.getRowCount()){
			if(row==0){
				out.println(cov.getColumnsNames()[0] + "\t" +cov.getColumnsNames()[1] + "\t" +cov.getColumnsNames()[2]);
			}
			out.println(cov.getValueAt(row, 0) + "\t" + cov.getValueAt(row, 1) + "\t" + cov.getValueAt(row, 2));
			
			row++;
		}
		out.flush();
		out.close();
	}


	/**
	 * 
	 * @return
	 */
	public double getMedian(){

		ArrayList<Double> abundances = new ArrayList<>();


		for (String proteinid : this.allenzymesdata.keySet()){
			abundances.add(Double.parseDouble(allenzymesdata.get(proteinid)[4]));
		}


		Collections.sort(abundances);

		if(abundances.size() % 2 ==0){
			this.median = (abundances.get(abundances.size()/2 -1) + abundances.get(abundances.size()/2)) / 2.0;

		}
		else{
			this.median = abundances.get(abundances.size()/2);
		}

		return this.median;

	}


	/**
	 * 
	 * @param proteinid
	 * @return
	 */
	public double getAbundance(String proteinid){
		if(this.allenzymesdata.containsKey(proteinid)){
			return Double.parseDouble(this.allenzymesdata.get(proteinid)[4]);
		}
		else{
			return 0.0;
		}


	}


	/**
	 * @param idTaxonomyMetaContainer
	 * @return
	 */
	public String getName(String id) {

		return this.allenzymesdata.get(id)[0]; //ecnumber_name
	}


	/**
	 * @return the allenzymesdata
	 */
	public HashMap<String, String[]> getAllenzymesdata() {
		return allenzymesdata;
	}


	/**
	 * @param allenzymesdata the allenzymesdata to set
	 */
	public void setAllenzymesdata(HashMap<String, String[]> allenzymesdata) {
		this.allenzymesdata = allenzymesdata;
	}

	public boolean isTaxonomyDefined(){

		return taxonomy.getAlltaxonomydata() != null;

	}

	/**
	 * @return the taxonomy
	 */
	public TaxonomyMetaContainer getTaxonomy() {
		return taxonomy;
	}

	/**
	 * @param taxonomy the taxonomy to set
	 */
	public void setTaxonomy(TaxonomyMetaContainer taxonomy) {
		this.taxonomy = taxonomy;
	}


	/**
	 * 
	 * @param args
	 * @throws SQLException
	 * @throws IOException
	 */
	//	public static void main (String [] args) throws SQLException, IOException{
	//		//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","teste", "root", "password");
	//		//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaFinal_SRS014692", "root", "password");
	//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaLocal14692", "root", "password");
	//		//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","HMP_buccalMucosa_SRS013711", "root", "password");
	//		//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","Bucall_MucosaLocal", "root", "password");
	//
	//		TaxonomyMetaContainer taxonomyMetaContainer= new TaxonomyMetaContainer(c1);
	//		EnzymesMetaContainer enz = new EnzymesMetaContainer(c1,taxonomyMetaContainer);
	//
	//		//enzymes
	//
	//		GenericDataTable table = enz.getEnzymesData();
	//		for (int row = 0 ; row < table.getRowCount(); row++ ){
	//
	//			for ( int column = 0; column < table.getColumnCount()-1; column++){
	//				if(column == 0){
	//					System.out.println(row+1+"- "+table.getValueAt(row, column) + "\t\t\t\t\t\n" + table.getValueAt(row, column+1)+ "\t\t" +table.getValueAt(row, column+2)+
	//							"\t" +table.getValueAt(row, column+3) + "\t"+table.getValueAt(row, column+4) 
	//							+ "\t"+table.getValueAt(row, column+5) + "\t"+table.getValueAt(row, column+6) + "\n");
	//				}
	//				else{
	//					continue;
	//				}
	//			}
	//
	//		}
	//		//stats
	//		String[][] res = enz.getStats();
	//
	//		for (int row = 0 ; row < res.length; row++){
	//			System.out.println(res[row][0]+ "\t" + res[row][1]);
	//		}
	//
	//		//rowinfo
	//
	//		//		for (String id : enz.getAllenzymesdata().keySet()){
	//		//
	//		//			int column = 0;
	//		//			String ec = enz.getAllenzymesdata().get(id)[1];
	//		//			System.out.println("\n\n\nID - " + id + "\t" + "ECnumber - " + ec + "\nENCODED GENES");
	//		//			DataTable[] data = enz.getRowInfo(ec, id);
	//		//			for (int i = 0 ; i < data.length; i++){
	//		//
	//		//				for(int row = 0 ; row < data[i].getRowCount(); row ++){
	//		//					if (i == 0){
	//		//						System.out.println(data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column+1) + 
	//		//								"\t" + data[i].getValueAt(row, column+2) + "\t" + data[i].getValueAt(row, column+3) +
	//		//								"\t" + data[i].getValueAt(row, column+4));
	//		//						
	//		//						if(row == data[i].getRowCount()-1){
	//		//							System.out.println("REACTIONS");
	//		//						}
	//		//					}
	//		//
	//		//					else{
	//		//						System.out.println("\t" + data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column +1)
	//		//								+ "\t" + data[i].getValueAt(row, column +2) + "\t" + data[i].getValueAt(row, column + 3));
	//		//
	//		//					}
	//		//
	//		//				}
	//		//
	//		//
	//		//			}
	//		//
	//		//		}
	//
	//
	//		//enzymes coverage
	//		enz.exportEnzymesCoverage(new File("/home/pedro/Desktop/coverage.csv"), enz);
	//
	//	}

}