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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;

/**
 * @author pedro
 *
 */
public class TaxonomyMetaContainer extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private LinkedHashMap<String, String[]> alltaxonomydata;
	private int selectedRow;
	private int noSimilarities;
	Integer min_numberHomologues;
	Double thresholdpPhylum;
	Double thresholdGenus;

	public TaxonomyMetaContainer(Table table, String name){
		super(table,name);
		this.connection = table.getConnection();
		this.min_numberHomologues = 5;
		this.thresholdpPhylum = 0.5;
		this.thresholdGenus =  0.3;
		this.setSelectedRow(-1);
	}
	

//		/**
//		 * 
//		 * @param conn
//		 */
//		public TaxonomyMetaContainer(Connection conn) {
//			this.connection = conn;
//			this.min_numberHomologues = 5;
//			this.thresholdpPhylum = 0.5;
//			this.thresholdGenus =  0.3;
//		}

	/**
	 * @return
	 */
	public GenericDataTable getPhylogenyData(){

		this.alltaxonomydata = new LinkedHashMap<>();
		HashMap<String, HashMap<String, Double>> phylumData = new HashMap<>();
		HashMap<String, HashMap<String, Double>> genusData = new HashMap<>();
		HashMap<String, TreeMap<Double, ArrayList<String[]>>> sqlData = new HashMap<>();
		HashMap<String, Integer> num_homologues_hash = new HashMap<>();

		LinkedHashMap<String,String[]> qls = new LinkedHashMap<String,String[]>();
		LinkedList<String> genes = new LinkedList<>();

		ArrayList<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Info");
		columnsNames.add("Genes");
		columnsNames.add("Phylum");
		columnsNames.add("Phylum Score");
		columnsNames.add("Genus");
		columnsNames.add("Genus Score");
		columnsNames.add("Phylum/Genus are concordant");




		GenericDataTable res = new GenericDataTable(columnsNames, "MetaTaxonomy", "Taxonomy data"){

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
				Statement stmt = this.connection.createStatement();
				ResultSet rs = stmt.executeQuery(
						"SELECT distinct(locusTag) from geneHomology order by locusTag" 
						);

				while(rs.next()){

					genes.add(rs.getString(1));
					String[] ql = new String[6];
					ql[0] = ""; //domain
					ql[1] = "No homologues enough"; //phylum
					ql[2] = "";//phylum score
					ql[3] = "";//genus
					ql[4] = "";//genus score
					ql[5] = ""; //phylum/genus concordance
					qls.put(rs.getString(1), ql);

				}

				/*
				 * 
				 * Construct two hashmaps of hashmaps in which for each gene is saved the number of times that a specific phylum and genus appears
				 * Crucial for the next step to assign taxons to genes
				 * 
				 */
				System.out.println("Building the phylum and genus maps for each gene ... \n");

				rs = stmt.executeQuery(
						"SELECT locusTag,locusID, bits, eValue,taxonomy FROM geneHomology " + 
								"LEFT JOIN geneHomology_has_homologues ON (geneHomology_has_homologues.geneHomology_s_key = geneHomology.s_key) "+
								"LEFT JOIN homologues on (geneHomology_has_homologues.homologues_s_key=homologues.s_key) "+
								"LEFT JOIN organism on (homologues.organism_s_key = organism.s_key)" //+
								//"WHERE locusTag = 'gene_id_10'"
						);



				while(rs.next()){

					String[] taxon = new String[2];
					taxon[0] = Float.toString(rs.getFloat("eValue"));
					taxon[1] = rs.getString("taxonomy");
					String locusTag = rs.getString("locusTag");
					String locusID = rs.getString("locusID");
					Double bits = rs.getDouble("bits");

					if(locusID !=null){ // only fills the hash if there is homologues


						if( sqlData.containsKey(locusTag)){
							num_homologues_hash.put(locusTag, num_homologues_hash.get(locusTag) + 1);

							TreeMap<Double, ArrayList<String[]>> value = sqlData.get(locusTag);
							if( value.containsKey(bits)){
								value.get(bits).add(taxon);
							}
							else{
								ArrayList<String[]> taxonList = new ArrayList<>();
								taxonList.add(taxon);
								value.put(bits, taxonList);
							}
						}
						else{
							num_homologues_hash.put(locusTag,1);
							TreeMap<Double, ArrayList<String[]>> value = new TreeMap<>();
							ArrayList<String[]> taxonList = new ArrayList<>();
							taxonList.add(taxon);
							value.put(bits, taxonList);
							sqlData.put(locusTag, value);

						}


					}
					else{
						this.noSimilarities++;
					}


				}
//				System.out.println("NOOOOO " + this.noSimilarities);
//				System.out.println("WITHHHH " + sqlData.size());
//				int sum =  this.noSimilarities + sqlData.size();
//				System.out.println("SUM " + sum);
				
				for (String gene : sqlData.keySet()){

					//System.out.println(gene);
					int num_homologues = num_homologues_hash.get(gene);
					if(num_homologues >= this.min_numberHomologues){

						int first_five = 0;

						HashMap<String, Double> auxMap_phylum = new HashMap<>();
						phylumData.put(gene, auxMap_phylum);

						HashMap<String, Double> auxMap_genus = new HashMap<>();
						genusData.put(gene, auxMap_genus);



						for (Double score : sqlData.get(gene).descendingKeySet()){

							for(String [] taxons : sqlData.get(gene).get(score)){

								String [] taxonomy = taxons[1].split(";");


								//phylum to test
								String phylum = "";

								if (taxonomy[0].equalsIgnoreCase("Eukaryota")){

									//qls.get(gene)[0] = taxonomy[0];//domain
									if(taxonomy.length <= 2) phylum = "Uncharacterized";
									else phylum = taxonomy[2].replaceFirst(" ", "");
								}

								else if (taxonomy[0].equalsIgnoreCase("Archaea") || taxonomy[0].equalsIgnoreCase("Bacteria")){

									//qls.get(gene)[0] = taxonomy[0];//domain
									if(taxonomy.length <= 2) phylum = "Uncharacterized";
									else phylum = taxonomy[1].replaceFirst(" ", "");							
								}

								else if (taxonomy[0].equalsIgnoreCase("Viruses") || taxonomy[0].contains("viruses")){
									//qls.get(gene)[0] = "Viruses";//domain
									if(taxonomy.length <= 2) phylum = "Uncharacterized";
									else if (taxonomy[0].equalsIgnoreCase("Viruses")) phylum = taxonomy[1].replaceFirst(" ", "");
									else phylum = taxonomy[0];

								}
								else{
									//qls.get(gene)[0] = "Others";
									phylum = "-";
								}

								//genus to test
								String genus = "";
								if(phylum.equals("Uncharacterized") || phylum.equals("-")) genus = "-";
								else genus = taxonomy[taxonomy.length-1].replace(".", ""); 

								if(genus.contains(" ")){
									String [] genusWrong = genus.split(" ");
									for(int i = 0; i< genusWrong.length; i++){
										if(! genusWrong[i].isEmpty()){
											genus = genusWrong[i];
											break;
										}
									}
								}
								if(genus.contains("[") || genus.contains("]")){
									genus = genus.replaceAll("[\\[\\]]", "");
								}

								//System.out.println(genus);
								//first five hits
								if(first_five < 5){

									first_five++;

									// ################## phylum ############

									if(phylumData.get(gene).containsKey(phylum)){

										HashMap<String, Double> newvalue = phylumData.get(gene);
										newvalue.put(phylum, newvalue.get(phylum)+2.0);
										phylumData.put(gene, newvalue);

									}

									else{

										HashMap<String, Double> newphylum = phylumData.get(gene);
										newphylum.put(phylum, 2.0);
										phylumData.put(gene, newphylum);
									}

									//// ################## genus ############

									if(genusData.get(gene).containsKey(genus)){

										HashMap<String, Double> newvalue = genusData.get(gene);
										newvalue.put(genus, newvalue.get(genus)+2.0);
										genusData.put(gene, newvalue);
									}
									else{

										HashMap<String, Double> newgenus = genusData.get(gene);
										newgenus.put(genus, 2.0);
										genusData.put(gene, newgenus);
									}

								}
								//others than first five hits
								else{

									// ################## phylum not first hits ############

									if(phylumData.get(gene).containsKey(phylum)){

										HashMap<String, Double> newvalue = phylumData.get(gene);
										newvalue.put(phylum, newvalue.get(phylum)+0.5);
										phylumData.put(gene, newvalue);

									}

									else{

										HashMap<String, Double> newphylum = phylumData.get(gene);
										newphylum.put(phylum, 0.5);
										phylumData.put(gene, newphylum);
									}

									//// ################## genus not first hits ############

									if(genusData.get(gene).containsKey(genus)){

										HashMap<String, Double> newvalue = genusData.get(gene);
										newvalue.put(genus, newvalue.get(genus)+0.5);
										genusData.put(gene, newvalue);
									}
									else{

										HashMap<String, Double> newgenus = genusData.get(gene);
										newgenus.put(genus, 0.5);
										genusData.put(gene, newgenus);
									}


								}


							}
						}

					}

				}

				System.out.println("Selecting the phylum and genus for each gene...\n");
				/*
				 * 
				 * Select the phylum and genus for each gene and give a score for that selection
				 * 
				 */


				//############## phylum selection and scoring ##############
				for (String gene : phylumData.keySet()){

					int num_homologues = num_homologues_hash.get(gene);
					HashMap<String, Double> phylummap = phylumData.get(gene);


					double max_score = (double) (5*2) + ((num_homologues-5) * 0.5);
					//double max_score = (double) num_homologues;
					double best_phylum_score = 0.0;			
					String selected_phylum ="";

					for (String phyla : phylummap.keySet()){

						if(phylummap.get(phyla) > best_phylum_score){
							best_phylum_score = phylummap.get(phyla);
							selected_phylum = phyla;

						}
						else if(phylummap.get(phyla) == best_phylum_score){//select the phylum between two simmilar scores

							for (Double score : sqlData.get(gene).descendingKeySet()){
								ArrayList<String[]> taxonList = sqlData.get(gene).get(score);
								//XXX: ORDENAR LISTA

								for (String[] taxon : taxonList) {
									String[] taxonSplit = taxon[1].split(";");

									if(taxonSplit.length > 2){
										if(taxonSplit[0].equalsIgnoreCase("Eukaryota") && taxonSplit[2].equalsIgnoreCase(phyla)){//select the new genus if appears first in the blast results
											selected_phylum = phyla;
											break;
										}

										else if((taxonSplit[0].equalsIgnoreCase("Archaea") || taxonSplit[0].equalsIgnoreCase("Bacteria")
												|| taxonSplit[0].equalsIgnoreCase("Viruses") || taxonSplit[0].contains("Viruses"))
												&& taxonSplit[1].equalsIgnoreCase(phyla)){
											selected_phylum = phyla;
											break;
										}

										else if(taxonSplit[ taxonSplit.length - 1].replace(".", "").equalsIgnoreCase(selected_phylum)){//keep the old genus if it appears first in blast results
											break;
										}
									}


								}
							}
						}
					}
					qls.get(gene)[1] = selected_phylum; //phylum
					double score = best_phylum_score/max_score; //score
					BigDecimal bd = new BigDecimal(score).setScale(4, RoundingMode.HALF_EVEN);
					score = bd.doubleValue();

					if(score >= this.thresholdpPhylum){
						qls.get(gene)[2] = Double.toString(score); 
					}
					else{
						qls.get(gene)[2] = Double.toString(score)+"(< " + this.thresholdpPhylum+")";

					}


				}

				//############## genus selection and scoring ##############

				for (String gene : genusData.keySet()){

					int num_homologues = num_homologues_hash.get(gene);
					HashMap<String, Double> genusmap = genusData.get(gene);
					//					System.out.println(gene);
					//					System.out.println(num_homologues);
					//					System.out.println(genusmap.size() + "\n");
					double max_score = (double) (5*2) + ((num_homologues-5) * 0.5);
					//double max_score = (double) num_homologues;
					double best_genus_score = 0;
					String selected_genus="";


					for (String genus : genusmap.keySet()){

						if(genusmap.get(genus) > best_genus_score){
							best_genus_score = genusmap.get(genus);
							selected_genus = genus;

						}
						else if(genusmap.get(genus) == best_genus_score){


							firstloop:
								for (Double score : sqlData.get(gene).descendingKeySet()){
									ArrayList<String[]> taxonList = sqlData.get(gene).get(score);

									if(taxonList.size()>1){
										Collections.sort(taxonList, new Comparator<String[]>() {
											public int compare(String[] strings, String[] otherStrings) {
												return strings[0].compareTo(otherStrings[0]);
											}
										});

									}


									for (String[] taxon : taxonList) {
										String[] taxonSplit = taxon[1].split(";");

										if(taxonSplit.length > 2){
											if(taxonSplit[ taxonSplit.length - 1].replace(".", "").equalsIgnoreCase(genus)){//select the new genus if appears first in the blast results
												selected_genus = genus;
												break firstloop;

											}
											else if(taxonSplit[ taxonSplit.length - 1].replace(".", "").equalsIgnoreCase(selected_genus)){//keep the old genus if it appears first in blast results
												break firstloop;
											}

										}



									}


								}
						}
					}

					qls.get(gene)[3] = selected_genus; //genus
					double score = best_genus_score/max_score; //score
					BigDecimal bd = new BigDecimal(score).setScale(4, RoundingMode.HALF_EVEN);
					score = bd.doubleValue();
					
					if(score >= this.thresholdGenus){
						qls.get(gene)[4] = Double.toString(score); 
					}
					else{
						qls.get(gene)[4] = Double.toString(score)+"(< " + this.thresholdGenus+")";
					}



				}


				// ############ Phylum / genus concordance #########

				//				int count = 0;
				//				String previous_gene = "";
				for(String gene : sqlData.keySet()){

					firstLoop:
						for(double score : sqlData.get(gene).descendingKeySet()){

							ArrayList<String[]> taxonList = sqlData.get(gene).get(score);


							for (String[] taxon : taxonList) {

								String[] taxonomy = taxon[1].split(";");

								if(taxonomy.length > 2){

									if(taxon[1].contains(qls.get(gene)[1])) qls.get(gene)[0] = taxonomy[0]; //##DOMAIN


									String genus = taxonomy[taxonomy.length-1].replace(".", "");
									if(genus.contains(" ")){
										String [] genusWrong = genus.split(" ");
										for(int i = 0; i< genusWrong.length; i++){
											if(! genusWrong[i].isEmpty()){
												genus = genusWrong[i];
												break;
											}
										}
									}
									if(genus.contains("[") || genus.contains("]")){
										genus = genus.replaceAll("[\\[\\]]", "");
									}


									if(qls.get(gene)[2].contains("<") || qls.get(gene)[4].contains("<")){
										qls.get(gene)[5] = "no minimum score";

									}
									else{
										if(qls.get(gene)[3].equalsIgnoreCase(genus)){
											
//											if(gene.equals("gene_id_10010")){
//												System.out.println(taxon[1]);
//												System.out.println(qls.get(gene)[1]);
//											}
	
											
											if(taxon[1].contains(qls.get(gene)[1])){
												qls.get(gene)[5] = "true";
												break firstLoop;

											}
											else{
												qls.get(gene)[5] = "false";
												break firstLoop;

											}
										}
										else{

											continue;
										}
									}

								}

							}

						}


				if(! qls.get(gene)[1].contains("No homologues enough") && qls.get(gene)[5].isEmpty()){

					qls.get(gene)[5] = "Uncharacterized genus";

				}

				}


				/*
				 * 
				 * ############## MAIN TABLE ############################
				 */
				for(String gene : qls.keySet()) {

					List<Object> ql = new ArrayList<Object>();
					String[] taxonomyData = qls.get(gene);
					ql.add("");
					ql.add(gene); // gene
					ql.add(taxonomyData[1]); //phylum
					ql.add(taxonomyData[2]); //score
					ql.add(taxonomyData[3]); //genus
					ql.add(taxonomyData[4]); //score
					ql.add(taxonomyData[5]); //concordance phylum/genus

					res.addLine(ql, gene);
					this.alltaxonomydata.put(gene, taxonomyData);
				}
				rs.close();
				stmt.close();
			}
			catch(Exception e)
			{e.printStackTrace();}

			return res;
	}

	/**
	 * @return
	 */
	public String[][] getStats() {

		int num_genes = 0 , no_homologues=0 , genes4inference=0 , noscore=0 , uncharacterized = 0, withminscore = 0, noconcordant=0 , genes4calculation=0
				, bacteria =0, eukaryota=0, archaea=0, viruses = 0 ;

		HashMap<String, Integer> phylum = new HashMap<>();
		HashMap<String, Integer> genus = new HashMap<>();
		ArrayList<String[]> res = new ArrayList<>();
		String[][] res_final = null;

		try {

			if(this.alltaxonomydata!=null) {

				num_genes = this.alltaxonomydata.size();
				for (String gene : this.alltaxonomydata.keySet()){

					if(this.alltaxonomydata.get(gene)[1].equals("No homologues enough")) no_homologues++;
					else genes4inference++;


					if(this.alltaxonomydata.get(gene)[5].equals("no minimum score")) noscore++;
					else if(this.alltaxonomydata.get(gene)[5].equals("uncharacterized genus")) uncharacterized++;
					else if(this.alltaxonomydata.get(gene)[5].equals("false")){
						withminscore++;
						noconcordant++;
					}
					else if (this.alltaxonomydata.get(gene)[5].equals("true")){
						withminscore++;
						genes4calculation++;

						if(this.alltaxonomydata.get(gene)[0].equals("Bacteria")) bacteria++;
						else if(this.alltaxonomydata.get(gene)[0].equals("Eukaryota")) eukaryota++;
						else if(this.alltaxonomydata.get(gene)[0].equals("Archaea")) archaea++;
						else if(this.alltaxonomydata.get(gene)[0].equals("Viruses") || this.alltaxonomydata.get(gene)[0].contains("viruses")) viruses++;


						if(phylum.containsKey(this.alltaxonomydata.get(gene)[1])){
							int newvalue = phylum.get(this.alltaxonomydata.get(gene)[1]) + 1;
							phylum.put(this.alltaxonomydata.get(gene)[1], newvalue);

						}
						else{
							phylum.put(this.alltaxonomydata.get(gene)[1], 1);

						}


						if(genus.containsKey(this.alltaxonomydata.get(gene)[3])){
							int newvalue = genus.get(this.alltaxonomydata.get(gene)[3]) + 1;
							genus.put(this.alltaxonomydata.get(gene)[3], newvalue);
						}
						else{
							genus.put(this.alltaxonomydata.get(gene)[3], 1);
						}
					}	
				}

				HashMap<String, Integer> orderedDomainHash = new HashMap<>();
				orderedDomainHash.put("Bacteria",bacteria);
				orderedDomainHash.put("Eukaryota",eukaryota);
				orderedDomainHash.put("Archaea",archaea);
				ArrayList<Integer> orderedDomainList = new ArrayList<>();
				for(String domain : orderedDomainHash.keySet()){
					orderedDomainList.add(orderedDomainHash.get(domain));
				}
				Collections.sort(orderedDomainList);
				Collections.reverse(orderedDomainList);


				ArrayList<Integer> orderedphylum = new ArrayList<>();
				for (String phyla : phylum.keySet()){
					orderedphylum.add(phylum.get(phyla));
				}
				Collections.sort(orderedphylum);
				Collections.reverse(orderedphylum);


				ArrayList<Integer> orderedgenus = new ArrayList<>();
				for (String genuss : genus.keySet()){
					orderedgenus.add(genus.get(genuss));
				}
				Collections.sort(orderedgenus);
				Collections.reverse(orderedgenus);

				String[] aux = new String[2];
				aux[0] = "Total number of genes";
				aux[1] = Integer.toString(num_genes);
				res.add(aux);
				
				aux = new String[2];
				aux[0] ="Number of genes with no enough homologues(< "+this.min_numberHomologues+")";
				aux[1] = Integer.toString(no_homologues);
				res.add(aux);
				
				aux = new String[2];
				aux[0] ="Number of genes that were actually included in the taxonomic composition inference";
				aux[1] = Integer.toString(genes4inference);
				res.add(aux);

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes with no minimum score achieved (either on phylum or genus level) to be included in the calculations";
				aux[1] = Integer.toString(noscore);
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes with minimum score achieved but uncharacterized";
				aux[1] = Integer.toString(uncharacterized);
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes with minimum score achieved";
				aux[1] = Integer.toString(withminscore);
				res.add(aux);

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes with minimum score achieved but with no concordant phylum and genus assignments";
				aux[1] = Integer.toString(noconcordant);
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes with minimum score achievedand with concordant phylum and genus assignments ";
				aux[1] = Integer.toString(genes4calculation);
				res.add(aux);

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Number of genes that were used at the end for the taxonomic description of the community";
				aux[1] = Integer.toString(genes4calculation);
				res.add(aux);

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] = "Relative abundances:";
				aux[1] = "";

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Domain:";
				aux[1] ="";
				res.add(aux);

				for(int count : orderedDomainList){

					for(String domain : orderedDomainHash.keySet()){
						if(orderedDomainHash.get(domain) == count){
							aux = new String[2];
							aux[0] = "\t"+domain;
							BigDecimal bd = new BigDecimal((double)count/genes4calculation*100).setScale(4, RoundingMode.HALF_EVEN);
							aux[1] = Double.toString(bd.doubleValue());
							res.add(aux);
						}
					}
				}
				//			res[16] = new String[] {"Bacteria", ""+(double)bacteria/genes4calculation*100};
				//			res[17] = new String[] {"Eukaryota", ""+(double)eukaryota/genes4calculation*100};
				//			res[18] = new String[] {"Archaea", ""+(double)archaea/genes4calculation*100};	
				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);
				
				aux = new String[2];
				aux[0] ="Others:";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="\t" + "Viruses";
				BigDecimal bd = new BigDecimal((double)viruses/genes4calculation*100).setScale(4, RoundingMode.HALF_EVEN);
				aux[1] = Double.toString(bd.doubleValue());
				
				res.add(aux);

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Phylum:";
				aux[1] ="";
				res.add(aux);


				for(int count: orderedphylum){

					for (String phyla : phylum.keySet()){

						if(phylum.get(phyla) == count){// && !phyla.equals(previousphylum)){

							aux = new String[2];
							aux[0] = "\t"+phyla;
							bd = new BigDecimal((double)count/genes4calculation*100).setScale(4, RoundingMode.HALF_EVEN);
							aux[1] = Double.toString(bd.doubleValue());
							res.add(aux);
							phylum.remove(phyla);
							//previousphylum = phyla;
							break;
						}
					}			
				}

				aux = new String[2];
				aux[0] ="";
				aux[1] ="";
				res.add(aux);

				aux = new String[2];
				aux[0] ="Genus:";
				aux[1] ="";
				res.add(aux);


				for(int count: orderedgenus){

					for (String genuss : genus.keySet()){

						if(genus.get(genuss) == count) { //&& !genuss.equals(previousgenus)){

							aux = new String[2];
							aux[0] = "\t"+genuss;
							bd = new BigDecimal((double)count/genes4calculation*100).setScale(4, RoundingMode.HALF_EVEN);
							aux[1] = Double.toString(bd.doubleValue());
							res.add(aux);
							//previousgenus = genuss;
							genus.remove(genuss);
							break;
						}
					}			
				}

				//				for(int i = 23 + iter ; i < res.length ; i++){
				//					res[i] = new String [] {"",""};
				//				}


				//			System.out.println(orderedphylum.size());
				//			System.out.println(phylum.size());
				//			System.out.println(orderedgenus.size());
				//			System.out.println(genus.size());

				res_final = new String[res.size()][];
				for(int i = 0; i < res.size(); i++){
					res_final[i] = new String []{res.get(i)[0], res.get(i)[1]};
				}
			}
			else{
				String [] aux = new String[2];
				aux[0] = "Click HERE to refresh !!";
				res_final = new String[1][];
				res_final[0] = new String [] {aux[0], aux[1]};
			}


		}
		catch(Exception e){e.printStackTrace();}
		return res_final;


	}

	/**
	 * @param gene
	 * @return
	 */
	public DataTable[] getRowInfo(String gene){//, String rowID) {

		DataTable[] datatables = new DataTable[2];
		TreeMap<Double, LinkedList<String>> sortFromBlastResults = new TreeMap<>();
		LinkedHashMap<Double, LinkedList<String>> sortedBlastResults = new LinkedHashMap<>();
		HashMap<String, HashMap<String, Double>> phylumData2 = new HashMap<>();
		HashMap<String, HashMap<String, Double>> genusData2 = new HashMap<>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Phylum");
		columnsNames.add("Scores");

		datatables[0] = new DataTable(columnsNames, "Phylum scores");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Genus");
		columnsNames.add("Scores");
		datatables[1] = new DataTable(columnsNames, "Genus scores");	

		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs;

			int num_homologues = 0;
			rs = stmt.executeQuery(
					"SELECT count(distinct(homologues_s_key)) FROM geneHomology " +
							"LEFT JOIN geneHomology_has_homologues ON (geneHomology_has_homologues.geneHomology_s_key = geneHomology.s_key) " +
							"WHERE query = '"+gene+"'"
					);

			while(rs.next()){

				num_homologues = rs.getInt(1);


			}
			if(num_homologues < this.min_numberHomologues){

				ArrayList<String> ql = new ArrayList<String>();
				ql.add("");
				ql.add("");
				datatables[0].addLine(ql);
				datatables[1].addLine(ql);
			}



			else{
				int first_five = 0;

				HashMap<String, Double> auxMap_phylum = new HashMap<>();
				phylumData2.put(gene, auxMap_phylum);

				HashMap<String, Double> auxMap_genus = new HashMap<>();
				genusData2.put(gene, auxMap_genus);
				genusData2.size();
				rs = stmt.executeQuery(
						"SELECT locusID,organism,taxonomy, bits, eValue "+ 
								"FROM geneHomology " +
								"LEFT JOIN geneHomology_has_homologues ON (geneHomology_has_homologues.geneHomology_s_key = geneHomology.s_key) " +
								"LEFT JOIN homologues on (geneHomology_has_homologues.homologues_s_key=homologues.s_key) "+
								"LEFT JOIN organism on (homologues.organism_s_key = organism.s_key) " +
								"WHERE query = '"+gene+"'" +
								"ORDER BY eValue asc"
						);

				while(rs.next()){

					if(sortFromBlastResults.containsKey(Double.parseDouble(rs.getString(4)))) {
						sortFromBlastResults.get(Double.parseDouble(rs.getString(4))).add(rs.getString(1));
					}
					else {
						LinkedList<String> locusList = new LinkedList<>();
						locusList.add(rs.getString(1));
						sortFromBlastResults.put(Double.parseDouble(rs.getString(4)), locusList);
					}
				}
				ArrayList<Double> bitslist = new ArrayList<>();
				for(Double d : sortFromBlastResults.keySet()){
					bitslist.add(d);
				}
				Collections.reverse(bitslist);
				for(Double d : bitslist){

					sortedBlastResults.put(d, sortFromBlastResults.get(d));

				}
				//				for(double d : sortedBlastResults.keySet()){
				//					System.out.println(d+ " " + sortedBlastResults.get(d));
				//				}

				for(double bits : sortedBlastResults.keySet()){

					LinkedList<String> list= sortFromBlastResults.get(bits);

					for(String locusid :list){

						rs = stmt.executeQuery(
								"SELECT locusID,organism,taxonomy FROM geneHomology "+
										"LEFT JOIN geneHomology_has_homologues ON (geneHomology_has_homologues.geneHomology_s_key = geneHomology.s_key) " +
										"LEFT JOIN homologues on (geneHomology_has_homologues.homologues_s_key=homologues.s_key) " +
										"LEFT JOIN organism on (homologues.organism_s_key = organism.s_key) " +
										"WHERE locusID = '"+locusid+"'" +
										"AND locusTag = '"+gene+"'"
								);

						while(rs.next()){

							String [] taxonomy = rs.getString(3).split(";");



							//phylum to test
							String phylum = "";

							if (taxonomy[0].equalsIgnoreCase("Eukaryota")){

								//qls.get(gene)[0] = taxonomy[0];//domain
								if(taxonomy.length <= 2) phylum = "Uncharacterized";
								else phylum = taxonomy[2].replaceFirst(" ", "");
							}

							else if (taxonomy[0].equalsIgnoreCase("Archaea") || taxonomy[0].equalsIgnoreCase("Bacteria")){

								//qls.get(gene)[0] = taxonomy[0];//domain
								if(taxonomy.length <= 2) phylum = "Uncharacterized";
								else phylum = taxonomy[1].replaceFirst(" ", "");							
							}

							else if (taxonomy[0].equalsIgnoreCase("Viruses") || taxonomy[0].contains("viruses")){
								//qls.get(gene)[0] = "Viruses";//domain
								if(taxonomy.length <= 2) phylum = "Uncharacterized";
								else if (taxonomy[0].equalsIgnoreCase("Viruses")) phylum = taxonomy[1].replaceFirst(" ", "");
								else phylum = taxonomy[0].replaceFirst(" ", "");

							}
							else{
								//qls.get(gene)[0] = "Others";
								phylum = "-";
							}

							//genus to test
							String genus = "";
							if(phylum.equals("Uncharacterized") || phylum.equals("-")) genus = "-";
							else genus = taxonomy[taxonomy.length-1].replace(".", ""); 

							if(genus.contains(" ")){
								String [] genusWrong = genus.split(" ");
								for(int i = 0; i< genusWrong.length; i++){
									if(! genusWrong[i].isEmpty()){
										genus = genusWrong[i];
										break;
									}
								}
							}
							if(genus.contains("[") || genus.contains("]")){
								genus = genus.replaceAll("[\\[\\]]", "");
							}




							//first five hits
							if(first_five < 5){

								first_five++;

								// ################## phylum ############

								if(phylumData2.get(gene).containsKey(phylum)){

									HashMap<String, Double> newvalue = phylumData2.get(gene);
									newvalue.put(phylum, newvalue.get(phylum)+2.0);
									phylumData2.put(gene, newvalue);

								}

								else{

									HashMap<String, Double> newphylum = phylumData2.get(gene);
									newphylum.put(phylum, 2.0);
									phylumData2.put(gene, newphylum);
								}

								//// ################## genus ############

								if(genusData2.get(gene).containsKey(genus)){

									HashMap<String, Double> newvalue = genusData2.get(gene);
									newvalue.put(genus, newvalue.get(genus)+2.0);
									genusData2.put(gene, newvalue);
								}
								else{

									HashMap<String, Double> newgenus = genusData2.get(gene);
									newgenus.put(genus, 2.0);
									genusData2.put(gene, newgenus);
								}

							}
							//others than first five hits
							else{

								// ################## phylum not first hits ############

								if(phylumData2.get(gene).containsKey(phylum)){

									HashMap<String, Double> newvalue = phylumData2.get(gene);
									newvalue.put(phylum, newvalue.get(phylum)+0.5);
									phylumData2.put(gene, newvalue);

								}

								else{

									HashMap<String, Double> newphylum = phylumData2.get(gene);
									newphylum.put(phylum, 0.5);
									phylumData2.put(gene, newphylum);
								}

								//// ################## genus not first hits ############

								if(genusData2.get(gene).containsKey(genus)){

									HashMap<String, Double> newvalue = genusData2.get(gene);
									newvalue.put(genus, newvalue.get(genus)+0.5);
									genusData2.put(gene, newvalue);
								}
								else{

									HashMap<String, Double> newgenus = genusData2.get(gene);
									newgenus.put(genus, 0.5);
									genusData2.put(gene, newgenus);
								}


							}
						}


					}

				}
				double max_score = (double) (5*2) + ((num_homologues-5) * 0.5);

				// ########### score for each phylum #######

				HashMap<String, Double> phylummap = phylumData2.get(gene);
				ArrayList<Double> sortedscoresphyla = new ArrayList<Double>();
				HashMap<String, Double> sortedHashphyla = new HashMap<>();
				for (String phyla : phylummap.keySet()){
					BigDecimal bd = new BigDecimal(phylummap.get(phyla)/max_score).setScale(4, RoundingMode.HALF_EVEN);
					double score = bd.doubleValue();
					sortedHashphyla.put(phyla, score);
					sortedscoresphyla.add(score);
				}
				Collections.sort(sortedscoresphyla);
				Collections.reverse(sortedscoresphyla);

				for(double score : sortedscoresphyla){

					for(String phyla : sortedHashphyla.keySet()){

						if(sortedHashphyla.get(phyla) == score) {
							ArrayList<String> ql = new ArrayList<>();
							ql.add(phyla);
							ql.add(Double.toString(score));
							datatables[0].addLine(ql);
							sortedHashphyla.remove(phyla);
							break;
						}
					}
				}

				//############ score for each genus ##############

				HashMap<String, Double> genusmap = genusData2.get(gene);
				ArrayList<Double> sortedscoresgenus = new ArrayList<Double>();
				HashMap<String, Double> sortedHashgenus = new HashMap<>();
				for (String genus : genusmap.keySet()){
					BigDecimal bd = new BigDecimal(genusmap.get(genus)/max_score).setScale(4, RoundingMode.HALF_EVEN);
					double score = bd.doubleValue();
					sortedHashgenus.put(genus, score);
					sortedscoresgenus.add(score);
				}
				Collections.sort(sortedscoresgenus);
				Collections.reverse(sortedscoresgenus);

				for(double score : sortedscoresgenus){

					for(String genus : sortedHashgenus.keySet()){

						if(sortedHashgenus.get(genus) == score) {
							ArrayList<String> ql = new ArrayList<>();
							ql.add(genus);
							ql.add(Double.toString(score));
							datatables[1].addLine(ql);
							sortedHashgenus.remove(genus);
							break;
						}
					}
				}


				rs.close();
				stmt.close();

			}






			//					//phylum to test
			//					String phylum = "";
			//					if (taxonomy[0].equalsIgnoreCase("Eukaryota")){
			//						phylum = taxonomy[2];
			//					}
			//					else if (taxonomy[0].equalsIgnoreCase("Archaea") || taxonomy[0].equalsIgnoreCase("Bacteria")){
			//						phylum = taxonomy[1];
			//					}
			//					else if (taxonomy[0].equalsIgnoreCase("Viruses")){
			//						phylum = taxonomy[1];
			//					}
			//
			//					//genus to test
			//					String genus = "";
			//
			//					if(phylum.equals("Uncharacterized") || phylum.equals("-")) genus = "-";
			//					else genus = taxonomy[taxonomy.length-1].replace(".", ""); 
			//
			//					if(genus.contains(" ")){
			//						String [] genusWrong = genus.split(" ");
			//						for(int i = 0; i< genusWrong.length; i++){
			//							if(! genusWrong[i].isEmpty()){
			//								genus = genusWrong[i];
			//								break;
			//							}
			//						}
			//					}
			//					if(genus.contains("[") || genus.contains("]")){
			//						genus = genus.replaceAll("[\\[\\]]", "");
			//					}


		}

		catch(Exception e) {

			e.printStackTrace();
		}

		return datatables;
	}

	/**
	 * @param file
	 * @param taxonomy
	 * @throws IOException
	 */
	public void exportFinalTaxonomicComposition(File file) throws IOException{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		String[][] composition = getStats();
		for(int row = 12; row < composition.length; row++){

			if(!composition[row][0].isEmpty()){ //&& !composition[row][1].isEmpty()){

				out.println(composition[row][0] + "\t" + composition[row][1]);
			}

		}
		out.flush();
		out.close();
	}

	/**
	 * @return the alltaxonomydata
	 */
	public LinkedHashMap<String, String[]> getAlltaxonomydata() {

		return alltaxonomydata;
	}

	/**
	 * @param alltaxonomydata the alltaxonomydata to set
	 */
	public void setAlltaxonomydata(LinkedHashMap<String, String[]> alltaxonomydata) {
		this.alltaxonomydata = alltaxonomydata;
	}

	/**
	 * @return the min_numberHomologues
	 */
	public Integer getMin_numberHomologues() {
		return min_numberHomologues;
	}

	/**
	 * @param min_numberHomologues the min_numberHomologues to set
	 */
	public void setMin_numberHomologues(Integer min_numberHomologues) {
		this.min_numberHomologues = min_numberHomologues;
	}

	/**
	 * @return the thresholdpPhylum
	 */
	public Double getThresholdpPhylum() {
		return thresholdpPhylum;
	}

	/**
	 * @param thresholdpPhylum the thresholdpPhylum to set
	 */
	public void setThresholdpPhylum(Double thresholdpPhylum) {
		this.thresholdpPhylum = thresholdpPhylum;
	}

	/**
	 * @return the thresholdGenus
	 */
	public Double getThresholdGenus() {
		return thresholdGenus;
	}

	/**
	 * @param thresholdGenus the thresholdGenus to set
	 */
	public void setThresholdGenus(Double thresholdGenus) {
		this.thresholdGenus = thresholdGenus;
	}

	public int compare(String strings, String otherStrings) {
		return strings.compareTo(otherStrings);
	}

	/**
	 * @return the selectedRow
	 */
	public int getSelectedRow() {
		return selectedRow;
	}

	/**
	 * @param selectedRow the selectedRow to set
	 */
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}


	/**
	 * @return the noSimilarities
	 */
	public int getNoSimilarities() {
		return noSimilarities;
	}


	/**
	 * @param noSimilarities the noSimilarities to set
	 */
	public void setNoSimilarities(int noSimilarities) {
		this.noSimilarities = noSimilarities;
	}
	
	

//	/**
//	 * @param args
//	 * @throws SQLException 
//	 * @throws IOException 
//	 */
//		public static void main(String[] args) throws SQLException, IOException {
//	
//			//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaFinal_SRS014692", "root", "password");
//			//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaLocal14692", "root", "password");
//			//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","HMP_buccalMucosa_SRS013711", "root", "password");
//			Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","Bucall_MucosaLocal", "root", "password");
//			//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","teste", "root", "password");
//	
//			TaxonomyMetaContainer phylogeny = new TaxonomyMetaContainer(c1);
//	
//			//taxonomy
//			//GenericDataTable table = phylogeny.getPhylogenyData(5, 1, 0.75);
//			GenericDataTable table = phylogeny.getPhylogenyData();
//			int falsecount = 0, truecount = 0, nohomologues=0, nominimumscore =0, total =0;
//			for (int row = 0 ; row < table.getRowCount(); row++ ){
//	
//				if(table.getValueAt(row, 1).equals("gene_id_10")){
//					System.out.println(row+1+"-"+table.getValueAt(row, 1) + "\t" +
//							table.getValueAt(row, 2)+ "\t" +
//							table.getValueAt(row, 3)+"\t" 
//							+table.getValueAt(row, 4)
//							+"\t"+table.getValueAt(row,5)
//							+"\t"+table.getValueAt(row,6)
//							);
//					if(table.getValueAt(row,6).equals("false")){
//						falsecount++;
//					}
//					else if (table.getValueAt(row,6).equals("true")) {
//	
//						truecount++;
//					}
//					else if (table.getValueAt(row,6).equals("no minimum score")){
//						nominimumscore++;
//					}
//					else{
//						nohomologues++;
//					}
//	
//				}
//				else continue;
//	
//				total = row+1;
//			}
//					System.out.println("\nTotal genes " + total + "\nNo enough homologues " + nohomologues + "\nNo minimum score "+ nominimumscore + "\nTrue concordance " + truecount + "\nFalse concordance " + falsecount);
//			
//					//stats
//			
//					String[][] res = phylogeny.getStats();
//					System.out.println("\n\n"+"STATS");
//					for (int row = 0 ; row < res.length; row++){
//						System.out.println(res[row][0]+ "\t" + res[row][1]);
//					}
			//rowinfo
//			System.out.println("\nROW INFO");
//			for (String gene : phylogeny.getAlltaxonomydata().keySet()){
//	
//				if(gene.equals("gene_id_10")){
//	
//					int column = 0;
//					System.out.println("\nGENE- " + gene );
//					DataTable[] data = phylogeny.getRowInfo(gene);
//					for (int i = 0 ; i < data.length; i++){
//	
//						for(int row = 0 ; row < data[i].getRowCount(); row ++){
//							if (i == 0){
//								System.out.println("Phylum" + "\t" + data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column +1)
//										);
//							}
//							else{
//								System.out.println("Genus" + "\t" + data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column +1)
//										);
//							}
//	
//						}
//	
//	
//					}
//	
//				}
//				else continue;
//	
//	
//			}
	
			//export composition
//			phylogeny.exportFinalTaxonomicComposition(new File("/home/pedro/Desktop/TaxonomicComposition.txt"), phylogeny);
	
			//		String a= "0.01";
			//		String b= "0.000000000001";
			//		
			//		System.out.println(phylogeny.compare(a, b));
	
	
//		}

}