package datatypes.metagenomics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;

//import jsc.contingencytables.FishersExactTest;
import jsc.distributions.Hypergeometric;

/**
 * @author pedro
 *
 */
public class PathwaysMetaContainer extends Entity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private HashMap<String, String[]> allpathwaydata;
	private EnzymesMetaContainer metaenzymes;
	private double pathwayMinScore;
	private double genusProportion;
	private double pValue;
	private int populationSize=0, sampleSize = 0;


		public PathwaysMetaContainer(Table table, String name, EnzymesMetaContainer metaenzymes) {
			super(table, name);
			this.connection = table.getConnection();
			this.metaenzymes = metaenzymes;
	
			this.pathwayMinScore = 0.5;
			this.genusProportion = 0.75;
			this.pValue = 0.1;	
		}

//	/**
//	 * 
//	 * @param conn
//	 * @param enzymes
//	 */
//	public PathwaysMetaContainer(Connection conn, EnzymesMetaContainer metaenzymes){
//		this.connection = conn;
//		this.metaenzymes = metaenzymes;
//		this.pathwayMinScore = 0.5;
//		this.genusProportion = 1.0;
//		this.pValue = 0.1;
//	}


	/**
	 * 
	 * @return
	 */
	public GenericDataTable getPathwaysData() {

		this.allpathwaydata = new HashMap<String, String[]>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		HashMap<String,int[]> hypergeometricAux = new HashMap<String,int[]>();
		this.populationSize = 0;
		this.sampleSize = 0;
		columnsNames.add("Info");
		//columnsNames.add("Code");
		columnsNames.add("Name");
		columnsNames.add("Nº of enzymes");
		columnsNames.add("Encoded enzymes");
		//columnsNames.add("Nº of reactions");
		//columnsNames.add("Present reactions");
		//columnsNames.add("Score");
		//columnsNames.add("Pathway identification");
		columnsNames.add("p-value");
		columnsNames.add("Is significantly present?");
		columnsNames.add("Abundance");
		columnsNames.add("Nº of genus executing this pathway");
		//columnsNames.add("Genus executing this pathway");

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		GenericDataTable res = new GenericDataTable(columnsNames, "MetaPathway", "Pathways data"){

			private static final long serialVersionUID = 1236477181642906433L;

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

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT idpathway, code, name FROM pathway " +
					"RIGHT JOIN pathway_has_enzyme ON idpathway = pathway_idpathway " +
					"GROUP BY idpathway ORDER BY name");

			while(rs.next()) {

				String[] ql = new String[13];
				ql[0] = rs.getString(2); //code
				ql[1] = rs.getString(3); //name
				ql[2] = "0"; // number of enzymes
				ql[3] = "0"; // number of present enzymes
				ql[4] = "0"; //number of reaction
				ql[5] = "0"; // number of present reactions
				ql[6] = "0"; // score
				ql[7] = "0"; // pathways identification
				ql[8] = "0"; // pathways abundance
				ql[9] = "0"; // number of taxonomic genus contributing for this pathway
				ql[10] = ""; // genus contributing for this pathway
				ql[11] = "0"; // hypergeometric probability
				ql[12] = ""; //is statistically significant to be present
				index.add(rs.getString(1));
				qls.put(rs.getString(1), ql);
			}


			/*
			 * #################### NUMBER OF ENZYMES #################
			 */
			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, count(distinct(enzyme_protein_idprotein)) " +
							"FROM pathway " +
							"RIGHT JOIN pathway_has_enzyme ON pathway_idpathway=pathway.idpathway " +
							"GROUP BY pathway_idpathway ORDER BY name"
					);

			while(rs.next()) {
				qls.get(rs.getString(1))[2] = rs.getString(2); // number of enzymes	
				this.populationSize += rs.getInt(2); //total number of enzymes existing in pathways
				int [] enzAux = new int[2];
				enzAux[0] = rs.getInt(2);
				enzAux[1] = 0;
				hypergeometricAux.put(rs.getString(1), enzAux); // map with the total number of enzymes as well as the present ones
			}

			/*
			 * 
			 *  ################### NUMBER OF PRESENT ENZYMES ###############
			 */
			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, count(distinct(ecnumber)) from pathway_has_enzyme "+
							"RIGHT JOIN enzyme ON enzyme_protein_idprotein = enzyme.protein_idprotein "+
							"WHERE inModel = 1 GROUP BY pathway_idpathway" 

					);

			while(rs.next()){
				if(rs.getString(1) != null){

					qls.get(rs.getString(1))[3] = rs.getString(2); // number of present enzymes
					this.sampleSize += rs.getInt(2); // total number of present enzymes in all pathways
					hypergeometricAux.get(rs.getString(1))[1] = rs.getInt(2); //numbe of present enzymes per pathway
				}
			}


			/*
			 * 
			 * ################## NUMBER OF REACTION ############################
			 */
			rs = stmt.executeQuery("SELECT pathway_idpathway, count(distinct(reaction_idreaction)) "+
					"FROM pathway_has_reaction "+
					"GROUP BY pathway_idpathway "
					);

			while(rs.next()) {
				if (qls.containsKey(rs.getString(1))){
					qls.get(rs.getString(1))[4] = rs.getString(2); // number of  reactions
				}

			}

			/*
			 * 
			 * ########################## NUMBER OF PRESENT REACTIONS ##################
			 */
			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, count(distinct(name)) from reaction RIGHT JOIN pathway_has_reaction " +
							"ON reaction_idreaction = idreaction " +
							"where inModel = 1 " +
							"GROUP BY pathway_idpathway"
					);

			while(rs.next()){
				if (qls.containsKey(rs.getString(1))){
					qls.get(rs.getString(1))[5] = rs.getString(2); // number of present reaction 

				}

			}

			/*
			 * 
			 * ########################## HYPERGEOMETRIC PROBABILITY FOR PATHWAY PRESENCE #############################
			 */
			System.out.println("Samp size " + this.sampleSize);
			System.out.println("Pop size " + this.populationSize);
			double overall_ratio = (double) this.sampleSize/this.populationSize;
			boolean isPositivelySignificant = false;
			for (String pathway : hypergeometricAux.keySet()){
				
				if(hypergeometricAux.get(pathway)[0]>3){
					double pathway_ratio = (double) hypergeometricAux.get(pathway)[1]/hypergeometricAux.get(pathway)[0];
					if(pathway_ratio >= overall_ratio) isPositivelySignificant = true;

					Hypergeometric hyper = new Hypergeometric(this.sampleSize, this.populationSize, hypergeometricAux.get(pathway)[0]);
					double probabilityMassFunct = hyper.pdf(((double) hypergeometricAux.get(pathway)[1]));
					BigDecimal bd = new BigDecimal(probabilityMassFunct).setScale(7, RoundingMode.HALF_EVEN);
					probabilityMassFunct = bd.doubleValue();

					qls.get(pathway)[11] = Double.toString(probabilityMassFunct); 
					
					if(probabilityMassFunct < this.pValue && isPositivelySignificant ){
						qls.get(pathway)[12] = "True"; 
					}
					else if(probabilityMassFunct < this.pValue && isPositivelySignificant == false){
						qls.get(pathway)[12] = "Negatively significant"; 
					}
					else qls.get(pathway)[12] = "No significant"; 
					
					isPositivelySignificant = false;
				}
				else  qls.get(pathway)[12] = "Few enzymes";

			}

			
			/*
			 * 
			 * ########################## PATHWAY SCORE FOR COVERAGE #############################
			 */

			int presentenzymes =0;
			//this.metaenzymes.getEnzymesData();
			double median = this.metaenzymes.getMedian(); //median of all enzymes abundances
			double ab = 0.0;
			int previous_id = 0;
			boolean notfirst = false;

			rs = stmt.executeQuery(
					"SELECT idpathway, enzyme_protein_idprotein from pathway "+
							"RIGHT JOIN pathway_has_enzyme ON pathway_idpathway = idpathway "+
							"RIGHT JOIN enzyme ON enzyme_protein_idprotein = enzyme.protein_idprotein "+
							"WHERE inModel = 1 and idpathway is not null order by pathway_idpathway;" 
					);


			while (rs.next()){

				if(Integer.parseInt(rs.getString(1)) == previous_id){

					ab = this.metaenzymes.getAbundance(rs.getString(2));

					if(ab>=median){
						presentenzymes++;
					}
					previous_id = rs.getInt(1);
				}

				else{
					if(notfirst){

						//							System.out.println(previous_id);
						//						
						//							System.out.println(qls.get(Integer.toString(previous_id))[6]);
						//							System.out.println(qls.get(Integer.toString(previous_id))[2]);

						double score = (double) presentenzymes/Integer.parseInt(qls.get(Integer.toString(previous_id))[2]);
						BigDecimal bd = new BigDecimal(score).setScale(3, RoundingMode.HALF_EVEN);
						score = bd.doubleValue();


						qls.get(Integer.toString(previous_id))[6] = Double.toString(score); //score


						presentenzymes = 0;		
						ab = this.metaenzymes.getAbundance(rs.getString(2));
						if(ab>=median){
							presentenzymes++;
						}
						previous_id = rs.getInt(1);
					}

					else{
						previous_id = rs.getInt(1);
						ab = this.metaenzymes.getAbundance(rs.getString(2));
						if(ab>=median){
							presentenzymes++;
						}
						notfirst=true;
					}


				}
			}



			/*
			 * 
			 * ############## PATHWAY COVERAGE #############################
			 */
			for(String pathwayid : qls.keySet()){
				;
				//if(Double.parseDouble(qls.get(proteinid)[6]) >= this.pathwayMinScore){
				if(qls.get(pathwayid)[12].equals("True")){
					qls.get(pathwayid)[7] = "1";
				}
				else{
					qls.get(pathwayid)[7] = "0"; //pathway identification/coverage
				}
			}


			/*
			 * 
			 * ############### PATHWAY ABUNDANCE #############################
			 */

			HashMap<String, ArrayList<Double>> EnzAb_PerPathway = new HashMap<>();
			int previous_pathway = 0;
			rs = stmt.executeQuery(
					"SELECT idpathway, enzyme_protein_idprotein,enzyme_ecnumber FROM pathway " +
							"RIGHT JOIN pathway_has_enzyme ON idpathway = pathway_idpathway " +
							"ORDER BY idpathway "
					);

			while(rs.next()){

				if(rs.getInt(1) == previous_pathway){

					EnzAb_PerPathway.get(rs.getString(1)).add(this.metaenzymes.getAbundance(rs.getString(2))); // abundances of all enzymes in each pathway

					previous_pathway = rs.getInt(1);
				}

				else{
					ArrayList<Double> aux = new ArrayList<>();
					aux.add(this.metaenzymes.getAbundance(rs.getString(2)));
					EnzAb_PerPathway.put(rs.getString(1), aux);
					previous_pathway = rs.getInt(1);
				}
			}

			for (String idpathway : EnzAb_PerPathway.keySet()){

				//if(Double.parseDouble(qls.get(idpathway)[6]) >= this.pathwayMinScore){
				if(qls.get(idpathway)[12].equals("True")){
					ArrayList<Double> sorted = EnzAb_PerPathway.get(idpathway);
					Collections.sort(sorted);
					Collections.reverse(sorted);

					int num_enz = 0;
					if(sorted.size() == 1){
						num_enz = 1;
					}
					else{
						num_enz = sorted.size()/2;
					}



					double sum_ab = 0;
					for (int i = 0 ; i < num_enz; i++){ // sum only the upper half enzyme abundances
						sum_ab += sorted.get(i);
					}


					double path_ab = (double) sum_ab/num_enz; //pathway abundance
					BigDecimal bd = new BigDecimal(path_ab).setScale(5, RoundingMode.HALF_EVEN);
					path_ab = bd.doubleValue();


					qls.get(idpathway)[8] = Double.toString(path_ab);

				}

				else qls.get(idpathway)[8] = "0.0";

			}

			/*
			 * 
			 * ############### TAXONOMIC GENUS CONTRIBUTING FOR PATHWAY #############################
			 */


			for(String idpathway : qls.keySet()){
				ArrayList<String> presentEnz = new ArrayList<>();

				int numb_genus = 0, num_enzInpathway = 0;
				String pathwaycode = qls.get(idpathway)[0];
				String confidentGenusInpathway = "";
				HashMap<String, Integer> genusPathwayMap = new HashMap<>();

				rs = stmt.executeQuery(
						"SELECT pathway_idpathway, enzyme_protein_idprotein, inModel from enzyme " +
								"LEFT JOIN pathway_has_enzyme ON enzyme_ecnumber = ecnumber " +
								"LEFT JOIN pathway ON idpathway = pathway_idpathway "+
								"where code = '"+pathwaycode+"'"
						);


				while(rs.next()) {
					num_enzInpathway++;
					if (rs.getInt(3) == 1) presentEnz.add(rs.getString(2));

				}


//				if((double)presentEnz.size()/num_enzInpathway >= this.pathwayMinScore){ //#####Only calculates the genus per pathway if it is present in the metagenome
				if(qls.get(idpathway)[12].equals("True")){ //#####Only calculates the genus per pathway if it is statiscally considered present in metagenome

					for(String idprotein : presentEnz){

						if(! this.metaenzymes.getAllenzymesdata().get(idprotein)[6].contains("No genus")){

							String genus = this.metaenzymes.getAllenzymesdata().get(idprotein)[5];
							genus = genus.replaceAll("\\(.*?\\)", "");


							if(genus.contains(";")){

								String [] all_genus = genus.split(";");

								for (int i = 0 ; i < all_genus.length; i++){

									//count genus in pathway
									if (genusPathwayMap.containsKey(all_genus[i])) genusPathwayMap.put(all_genus[i], genusPathwayMap.get(all_genus[i]) +1);
									else genusPathwayMap.put(all_genus[i], 1);
								}

							}

							else{
								if (genusPathwayMap.containsKey(genus)) genusPathwayMap.put(genus, genusPathwayMap.get(genus) +1);
								else genusPathwayMap.put(genus, 1);
							}
						}

					}


					//only genus with values equals to the number of present enzymes will be counted as present in each pathway
					//means that a genus is selected only if it codifies all the present enzymes in the pathway
					for(String genus : genusPathwayMap.keySet()){

						if(genusPathwayMap.get(genus) >= (double)presentEnz.size()*this.genusProportion){
							numb_genus++;
							confidentGenusInpathway += genus + ";";
						}
					}

					if(confidentGenusInpathway.isEmpty()){
						qls.get(idpathway)[9] = "No proportion achieved for genus inference";
						qls.get(idpathway)[10] = "-";
					}
					else{
						qls.get(idpathway)[9] = Integer.toString(numb_genus);
						qls.get(idpathway)[10] = confidentGenusInpathway.substring(0, confidentGenusInpathway.length()-1);
					}

				}

				else{

					qls.get(idpathway)[9] = "-";
					qls.get(idpathway)[10] = "-";

				}
			}


			/*
			 * 
			 * ############## MAIN TABLE ############################
			 */
			for(int i=0;i<index.size();i++) {

				List<Object> ql = new ArrayList<Object>();
				String[] pathwaysData = qls.get(index.get(i));
				ql.add("");
				//ql.add(pathwaysData[0]); //code
				ql.add(pathwaysData[1]);
				ql.add(pathwaysData[2]);
				ql.add(pathwaysData[3]);
				//ql.add(pathwaysData[4]);
				//ql.add(pathwaysData[5]);
				//ql.add(pathwaysData[6]); //score
				//ql.add(pathwaysData[7]); //pathways coverage
				ql.add(pathwaysData[11]); //hypergeometric
				ql.add(pathwaysData[12]); //isPresent?
				ql.add(pathwaysData[8]); // abundance
				ql.add(pathwaysData[9]);
				//ql.add(pathwaysData[10]);  //genus executing the pathway
				res.addLine(ql, index.get(i));
				this.allpathwaydata.put(index.get(i), pathwaysData);
			}
			rs.close();
			stmt.close();

		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}


	/**
	 * 
	 * @return
	 */
	public String[][] getStats() {


		int pres_pathways = 0 , high_abund=0 , comp_enz=0 , inc_enz=0 , no_reac = 0, comp_reac=0 , inc_reac=0;
		int onegenus = 0, nogenus = 0, somegenus = 0, noSingificantPathway = 0;


		String[][] res = new String[13][];

		try {

			if(this.allpathwaydata != null){


				for (String idpathway : this.allpathwaydata.keySet()){

					if(this.allpathwaydata.get(idpathway)[7].equals("1")) pres_pathways++;

					if(Double.parseDouble(this.allpathwaydata.get(idpathway)[8]) >= 0.5) high_abund++;

					if(this.allpathwaydata.get(idpathway)[2].equals(this.allpathwaydata.get(idpathway)[3])){
						comp_enz++;
					}
					else{
						inc_enz++;
					}

					if(this.allpathwaydata.get(idpathway)[4].equals("0")) no_reac++;

					if(this.allpathwaydata.get(idpathway)[4].equals(this.allpathwaydata.get(idpathway)[5]) && ! this.allpathwaydata.get(idpathway)[4].equals("0")) comp_reac++;

					if(! this.allpathwaydata.get(idpathway)[4].equals(this.allpathwaydata.get(idpathway)[5]) && ! this.allpathwaydata.get(idpathway)[4].equals("0"))	inc_reac++;

					if(this.allpathwaydata.get(idpathway)[9].equals("1")) onegenus++;
					else if(this.allpathwaydata.get(idpathway)[9].equals("No proportion achieved for genus inference")) nogenus++;
					else if(this.allpathwaydata.get(idpathway)[9].equals("-")) noSingificantPathway++;
					else somegenus++;

				}

//				res[0] = new String[] {"Present pathways (score higher than threshold ["+this.pathwayMinScore+"])", ""+pres_pathways};
				res[0] = new String[] {"Pathways statistically present", ""+pres_pathways};
//				res[1] = new String[] {"Pathways highly abundant in the metagenome (abundance higher than > 0.5)", ""+high_abund};
				res[1] = new String[] {"", ""};
				res[2] = new String[] {"Pathways with all the complete enzymes present", ""+comp_enz};
				res[3] = new String[] {"Pathways that don't contain all complete enzymes", ""+inc_enz};
				res[4] = new String[] {"", ""};
				res[5] = new String[] {"Pathways that are not composed of reactions", "" +no_reac};
				res[6] = new String[] {"Pathways with all the reactions present ", ""+comp_reac};
				res[7] = new String[] {"Pathways that don't contain all reactions", ""+inc_reac};
				res[8] = new String[] {"", ""};
				res[9] = new String[] {"Pathways with more than one genus assignment with high confidence level", ""+somegenus};
				res[10] = new String[] {"Pathways with exactly one genus assignment with high confidence level", ""+onegenus};
				res[11] = new String[] {"Pathways with no genus assignment with high confidence level", ""+nogenus};
//				res[12] = new String[] {"Pathways with score lower than threshold ["+this.pathwayMinScore+"])(thus with no pathway genus assignment)", ""+noSingificantPathway};
				res[12] = new String[] {"Pathways no statistically present (thus with no pathway genus assignment)", ""+noSingificantPathway};
			}



			else{	
				res[0] = new String [] {"Click HERE to refresh !!", ""};
			}


		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}


	/**
	 * 
	 * @param pathwaycode
	 * @return
	 */
	public DataTable[] getRowInfo(String pathwayid) {

		DataTable[] datatables = new DataTable[3];

		ArrayList<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Genus contributing for this pathway");
		columnsNames.add("Number of enzymes encoded");
		columnsNames.add("Is this genus executing this pathway?");
		datatables[0] = new DataTable(columnsNames, "Genus");


		columnsNames = new ArrayList<String>();
		columnsNames.add("Encoded Enzymes");
		columnsNames.add("Non encoded Enzymes");
		ArrayList<String> presentEnz = new ArrayList<>();
		ArrayList<String> absentEnz = new ArrayList<>();
		datatables[1] = new DataTable(columnsNames, "Enzymes");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Encoded Reactions");
		columnsNames.add("Non encoded Reactions");
		ArrayList<String> presentReac = new ArrayList<>();
		ArrayList<String> absentReac = new ArrayList<>();
		datatables[2] = new DataTable(columnsNames, "Reactions");	

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs;

			//genus


			ArrayList<String> presentE = new ArrayList<>();
			int num_enzInpathway = 0;
			HashMap<String, Integer> genusPathwayMap = new HashMap<>();

			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, enzyme_protein_idprotein, inModel from enzyme " +
							"LEFT JOIN pathway_has_enzyme ON enzyme_ecnumber = ecnumber " +
							"LEFT JOIN pathway ON idpathway = pathway_idpathway "+
							"where idpathway = '"+pathwayid+"'"
					);


			while(rs.next()) {
				num_enzInpathway++;
				if (rs.getInt(3) == 1) presentE.add(rs.getString(2));

			}


//			if((double)presentE.size()/num_enzInpathway >= this.pathwayMinScore){ //#####Only calculates the genus per pathway if it is present in the metagenome
			if(this.allpathwaydata.get(pathwayid)[12].equals("True")){//#####Only calculates the genus per pathway if it is statistically considered present in metagenome
				
				for(String idprotein : presentE){

					if(! this.metaenzymes.getAllenzymesdata().get(idprotein)[6].contains("No genus")){

						String genus = this.metaenzymes.getAllenzymesdata().get(idprotein)[5].replaceAll("\\([^\\(]*\\)", "");//retirar parentesis a string tb

						if(genus.contains(";")){

							String [] all_genus = genus.split(";");

							for (int i = 0 ; i < all_genus.length; i++){

								//count genus in pathway
								if (genusPathwayMap.containsKey(all_genus[i])) genusPathwayMap.put(all_genus[i], genusPathwayMap.get(all_genus[i]) +1);
								else genusPathwayMap.put(all_genus[i], 1);
							}

						}

						else{
							if (genusPathwayMap.containsKey(genus)) genusPathwayMap.put(genus, genusPathwayMap.get(genus) +1);
							else genusPathwayMap.put(genus, 1);
						}
					}

				}

				ArrayList<Integer> num_times = new ArrayList<>();
				for (String genus : genusPathwayMap.keySet()){
					num_times.add(genusPathwayMap.get(genus));
				}
				Collections.sort(num_times);
				Collections.reverse(num_times);
				for(int i : num_times){

					for(String genus: genusPathwayMap.keySet()){

						if(genusPathwayMap.get(genus) == i) {
							ArrayList<String> ql = new ArrayList<>();
							ql.add(genus);
							ql.add(Integer.toString(genusPathwayMap.get(genus)));

							if(genusPathwayMap.get(genus) >= (double)presentE.size()*this.genusProportion) ql.add("True");
							else ql.add("False");
							datatables[0].addLine(ql);
							genusPathwayMap.remove(genus);
							break;
						}
					}

				}
			}
			else{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add("Pathway is not statistically present");
				ql.add("");
				ql.add("");
				datatables[0].addLine(ql);
			}



			//enzymess
			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, enzyme_ecnumber, inModel from enzyme " +
							"LEFT JOIN pathway_has_enzyme ON enzyme_ecnumber = ecnumber " +
							"LEFT JOIN pathway ON idpathway = pathway_idpathway "+
							"where idpathway = '"+pathwayid+"'"
					);

			while(rs.next()) {

				if (rs.getInt(3) == 0){
					absentEnz.add(rs.getString(2));
				}
				else{
					presentEnz.add(rs.getString(2));
				}
			}
			int smallerlist = Math.min(presentEnz.size(), absentEnz.size());
			for (int i = 0 ; i < smallerlist; i++){

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(presentEnz.get(i));
				ql.add(absentEnz.get(i));
				datatables[1].addLine(ql);
			}

			if(smallerlist == presentEnz.size()){ 
				for (int i = smallerlist; i < absentEnz.size(); i++){
					ArrayList<String> ql = new ArrayList<String>();
					ql.add("");
					ql.add(absentEnz.get(i));
					datatables[1].addLine(ql);
				}
			}
			else{

				for ( int i = smallerlist; i < presentEnz.size(); i++){

					ArrayList<String> ql = new ArrayList<String>();
					ql.add(presentEnz.get(i));
					ql.add("");
					datatables[1].addLine(ql);
				}

			}



			//reactions
			rs = stmt.executeQuery(
					"SELECT pathway_idpathway, reaction.name, inModel from reaction " +
							"LEFT JOIN pathway_has_reaction ON reaction_idreaction = idreaction " +
							"LEFT JOIN pathway ON idpathway = pathway_idpathway " +
							"where idpathway = '"+pathwayid+"'" 
					);

			while(rs.next()) {

				if (rs.getInt(3) == 0){
					absentReac.add(rs.getString(2));
				}
				else{
					presentReac.add(rs.getString(2));
				}
			}

			int smallerlistReac = Math.min(absentReac.size(), presentReac.size());
			for (int i = 0 ; i < smallerlistReac; i++){

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(presentReac.get(i));
				ql.add(absentReac.get(i));
				datatables[2].addLine(ql);
			}

			if(smallerlistReac == presentReac.size()){ 
				for (int i = smallerlistReac; i < absentReac.size(); i++){
					ArrayList<String> ql = new ArrayList<String>();
					ql.add("");
					ql.add(absentReac.get(i));
					datatables[2].addLine(ql);
				}
			}
			else{

				for ( int i = smallerlistReac; i < presentReac.size(); i++){

					ArrayList<String> ql = new ArrayList<String>();
					ql.add(presentReac.get(i));
					ql.add("");
					datatables[2].addLine(ql);
				}

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
	public HashMap<String, ArrayList<String>> getPathwaysPerGenus(){

		HashMap<String, ArrayList<String>> pathwaysPerGenus = new HashMap<>();

		for (String idpathway : this.allpathwaydata.keySet()){

			String name = this.allpathwaydata.get(idpathway)[1];
			String genus = this.allpathwaydata.get(idpathway)[10];


			if(genus.equals("-")) continue; //no genus for this pathway

			else{ // there is genus for this pathway

				if(genus.contains(";")){

					String [] all_genus = genus.split(";");

					for (int i = 0 ; i < all_genus.length; i++){

						if (pathwaysPerGenus.containsKey(all_genus[i])) pathwaysPerGenus.get(all_genus[i]).add(name);
						else{
							ArrayList<String> pathways = new ArrayList<>();
							pathways.add(name);
							pathwaysPerGenus.put(all_genus[i], pathways);
						}
					}

				}

				else{

					if (pathwaysPerGenus.containsKey(genus)) pathwaysPerGenus.get(genus).add(name);
					else{
						ArrayList<String> pathways = new ArrayList<>();
						pathways.add(name);
						pathwaysPerGenus.put(genus, pathways);
					}
				}
			}


		}

		return pathwaysPerGenus;

	}


	/**
	 * 
	 * @return
	 */
	public DataTable getPathwayCoverage(){

		ArrayList<String> columnsNames = new ArrayList<>();
		columnsNames.add("Pathway");
		columnsNames.add("Code");
		columnsNames.add("Enzymes");
		columnsNames.add("Present");
		columnsNames.add("Coverage");
		columnsNames.add("p-value");
		columnsNames.add("Is significantly present?");
		columnsNames.add("Abundance");
		DataTable res = new DataTable(columnsNames, "Pathway coverage");

		try{

			for(String id: this.allpathwaydata.keySet()){
				//if(this.allpathwaydata.get(id)[7].equals("1")){
					ArrayList<String> ql = new ArrayList<>();
					ql.add(this.allpathwaydata.get(id)[1]);
					ql.add(this.allpathwaydata.get(id)[0]);
					ql.add(this.allpathwaydata.get(id)[2]);
					ql.add(this.allpathwaydata.get(id)[3]);
					ql.add(this.allpathwaydata.get(id)[7]);
					ql.add(this.allpathwaydata.get(id)[11]);
					ql.add(this.allpathwaydata.get(id)[12]);
					ql.add(this.allpathwaydata.get(id)[8]);
					res.addLine(ql);
				//}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void exportPathwaysPerGenus(File file) throws IOException{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		HashMap<String, ArrayList<String>> path = getPathwaysPerGenus();
		for(String genus : path.keySet()){
			System.out.println(genus);
			out.println(genus + "\t" + path.get(genus).toString());
		}
		out.flush();
		out.close();
	}
	/**
	 * @param file
	 * @param path
	 * @throws IOException
	 */
	public void exportPathwayCoverage(File file) throws IOException{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		DataTable cov = getPathwayCoverage();
		int row = 0;
		while(row < cov.getRowCount()){
			if(row==0){
				out.println(cov.getColumnsNames()[0] + "\t" +cov.getColumnsNames()[1] + "\t" +cov.getColumnsNames()[2]
						 + "\t" +cov.getColumnsNames()[3] + "\t" +cov.getColumnsNames()[4]+ "\t" +cov.getColumnsNames()[5]
								 + "\t" +cov.getColumnsNames()[6] + "\t" + cov.getColumnsNames()[7]);
			}
			out.println(cov.getValueAt(row, 0) + "\t" + cov.getValueAt(row, 1) + "\t" + cov.getValueAt(row, 2)
					+ "\t" + cov.getValueAt(row, 3) + "\t" + cov.getValueAt(row, 4) + "\t" + cov.getValueAt(row, 5)
					 + "\t" + cov.getValueAt(row, 6) + "\t" + cov.getValueAt(row, 7));

			row++;
		}
		out.flush();
		out.close();
	}


	/**
	 * @return the allpathwaydata
	 */
	public HashMap<String, String[]> getAllpathwaydata() {
		return allpathwaydata;
	}


	/**
	 * @param allpathwaydata the allpathwaydata to set
	 */
	public void setAllpathwaydata(HashMap<String, String[]> allpathwaydata) {
		this.allpathwaydata = allpathwaydata;
	}


	public boolean isENzymesDefined(){

		return metaenzymes.getAllenzymesdata() != null;	
	}

	/**
	 * @return the pathwayMinScore
	 */
	public double getPathwayMinScore() {
		return pathwayMinScore;
	}

	/**
	 * @param pathwayMinScore the pathwayMinScore to set
	 */
	public void setPathwayMinScore(double pathwayMinScore) {
		this.pathwayMinScore = pathwayMinScore;
	}

	/**
	 * @return the genusProportion
	 */
	public double getGenusProportion() {
		return genusProportion;
	}

	/**
	 * @param genusProportion the genusProportion to set
	 */
	public void setGenusProportion(double genusProportion) {
		this.genusProportion = genusProportion;
	}


	/**
	 * @return the pValue
	 */
	public double getpValue() {
		return pValue;
	}

	/**
	 * @param pValue the pValue to set
	 */
	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	/**
	 * @return the populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize the populationSize to set
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * @return the sampleSize
	 */
	public int getSampleSize() {
		return sampleSize;
	}

	/**
	 * @param sampleSize the sampleSize to set
	 */
	public void setSampleSize(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	
	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, IOException {
//					
					Hypergeometric hyper = new Hypergeometric(15, 44, 10);
					System.out.println(hyper.pdf(3));
//		//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","teste", "root", "password");
//		//	Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","salivaFinal_SRS019120", "root", "password");
//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaLocal_SRS019120", "root", "password");
//		//	Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","SalivaLocal14692", "root", "password");
//		//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","HMP_buccalMucosa_SRS013711", "root", "password");
//		//Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","Bucall_MucosaLocal", "root", "password");
//
//		TaxonomyMetaContainer taxonomy = new TaxonomyMetaContainer(c1);
//		taxonomy.getPhylogenyData();
//		EnzymesMetaContainer enzymes = new EnzymesMetaContainer(c1, taxonomy);
//		enzymes.getEnzymesData();
//		PathwaysMetaContainer path = new PathwaysMetaContainer(c1, enzymes);
//
//
//		//pathways
//		GenericDataTable table = path.getPathwaysData();
//		for (int row = 0 ; row < table.getRowCount(); row++ ){
//
//			System.out.println(row+1+"-"+table.getValueAt(row, 1) + "\t" +
//					table.getValueAt(row, 2)+ "\t" +
//					table.getValueAt(row, 3)+"\t" 
//					+table.getValueAt(row, 4)
//					+"\t"+table.getValueAt(row,5) + "\t"
//					+table.getValueAt(row, 6)
//					+ "\t" + table.getValueAt(row, 7) 
//					+ "\t" + table.getValueAt(row, 8)
//					//						+ "\t" + table.getValueAt(row, 9) 
//					//						+ "\t" + table.getValueAt(row, 10) 
//					//						+ "\t" + table.getValueAt(row, 11) 
//					);
//
//		}
//
//		//stats
//		//			String[][] res = path.getStats();
//		//			System.out.println("\n\n"+"STATS");
//		//			for (int row = 0 ; row < res.length; row++){
//		//				System.out.println(res[row][0]+ "\t" + res[row][1]);
//		//			}
//
//		//rowinfo
//		//		for (String id : path.getAllpathwaydata().keySet()){
//		//
//		//			int column = 0;
//		//			String pathcode = path.getAllpathwaydata().get(id)[0];
//		//			System.out.println("\n\n\nPATHWAY- " + pathcode );
//		//			DataTable[] data = path.getRowInfo(pathcode);
//		//			for (int i = 0 ; i < data.length; i++){
//		//
//		//				for(int row = 0 ; row < data[i].getRowCount(); row ++){
//		//					if (i == 0){
//		//						System.out.println(row+1 + "\t" + data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column +1)
//		//								);
//		//						//						if(row == data[i].getRowCount()-1){
//		//						//							System.out.println("REACTIONS");
//		//						//						}
//		//					}
//		//
//		//					else{
//		//
//		//						System.out.println(row+1+ "\t" + data[i].getValueAt(row, column) + "\t" + data[i].getValueAt(row, column+1) 
//		//								);
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
//		//			//pathways coverage
//		//			path.exportPathwayCoverage(new File("/home/pedro/Desktop/Pathwaycoverage.txt"), path);
//		//	
//		//	
//		//			//pathways per genus
//		//			HashMap<String, ArrayList<String>> pathwaysPerGenusresult = path.getPathwaysPerGenus();
//		//			System.out.println("\n\nPathways per genus:\n");
//		//	
//		//			if(pathwaysPerGenusresult.size() == 0){
//		//				System.out.println("No genus inference for pathway data!");
//		//			}
//		//			else{
//		//	
//		//				for(String genus : pathwaysPerGenusresult.keySet()){
//		//	
//		//					System.out.println(genus + "\t" + pathwaysPerGenusresult.get(genus).toString());
//		//				}
//		//	
//		//			}
//
	}

}