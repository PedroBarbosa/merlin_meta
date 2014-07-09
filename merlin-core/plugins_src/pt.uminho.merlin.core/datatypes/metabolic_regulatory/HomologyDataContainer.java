package datatypes.metabolic_regulatory;

import java.io.File;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import operations.SaveProject;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.merlin.utilities.Pair;
import pt.uminho.sysbio.merlin.utilities.PairComparator;
import utilities.BlastScorer;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class HomologyDataContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[][] ecnPercent, prodPercent, product, enzyme;
	private Map<Integer, String> keys, prodItem, ecItem, initialProdItem, initialChromosome, initialEcItem, 
	namesList, locusList, initialLocus, initialNames, chromosome, notesMap;
	private Map<Integer, String> committedProdItem, committedEcItem, committedNamesList, 
	committedLocusList, committedChromosome, committedNotesMap;
	private Map<Integer, Boolean> committedSelected;
	private Map<String, String> score, score1, score2, scoreP, scoreP1, scoreP2;
	private Map<Integer, Boolean> initialSelectedGene, selectedGene;
	private Map<String, Integer> tableRowIndex;
	private Map<Integer, String[]> editedProductData, editedEnzymeData;
	private Double alpha, beta, threshold, blastHmmerWeight;
	private int selectedRow;
	private boolean isEukaryote, blastPAvailable, stats_blastXAvailable, hmmerAvailable, stats_hmmerAvailable, stats_blastPAvailable;
	private int minimumNumberofHits;
	private Map<Integer, String> blastPGeneDataEntries;
	private Map<String, Integer> hmmerGeneDataEntries;
	private Connection connection;
	//private boolean firstRun;
	private Map<Integer, String[]> committedProductList, committedEnzymeList;
	private boolean hasCommittedData;
	private Map<Integer, String> integrationLocusList, integrationChromosome, 
	integrationNamesList, integrationProdItem, integrationEcItem;
	private Map<Integer, Boolean> integrationSelectedGene;
	private Map<Integer, Integer> reverseKeys;

	/**
	 * @param dbt
	 * @param name
	 */
	public HomologyDataContainer(Table dbt, String name) {

		super(dbt, name);
		this.connection = dbt.getConnection();

		this.prodItem = new TreeMap<Integer,String>();
		this.ecItem = new TreeMap<Integer,String>();
		this.selectedGene = new TreeMap<Integer,Boolean>();
		this.locusList =  new TreeMap<Integer, String>();
		this.namesList = new TreeMap<Integer, String>();
		this.setNotesMap(new TreeMap<Integer, String>()); 
		this.editedEnzymeData = new TreeMap<Integer, String[]>();
		this.editedProductData=new TreeMap<Integer, String[]>();
		this.setScore1(new TreeMap<String, String>());
		this.setScore2(new TreeMap<String, String>());
		this.setScore(new TreeMap<String, String>());
		this.setScoreP1(new TreeMap<String, String>());
		this.setScoreP2(new TreeMap<String, String>());
		this.setScoreP(new TreeMap<String, String>());
		this.chromosome = new TreeMap<Integer, String>();
		this.tableRowIndex = new TreeMap<String, Integer>();
		this.alpha=0.5;
		this.blastHmmerWeight = 0.5;
		this.threshold = 0.5;
		this.beta = 0.15;
		this.minimumNumberofHits = 3;
		this.isEukaryote = this.setIsEukaryote();
		this.setSelectedRow(-1);
		this.setBlastType();
	}

	/**
	 * 
	 */
	private void setBlastType() {

		Statement stmt;

		try {

			this.hmmerAvailable = false;
			this.blastPAvailable = false;
			this.stats_blastXAvailable = false;
			this.stats_hmmerAvailable = false;
			this.stats_blastPAvailable = false;

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT program" +
					" FROM homologySetup " +
					"INNER JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key) " +
					"WHERE status = 'PROCESSED' OR status = 'NO_SIMILARITY'");

			int ncbip_count = 0;
			//int ncbix_count = 0;
			int hmmer_count = 0;

			while (rs .next()) {

				if(rs.getString(1).equalsIgnoreCase("hmmer")) {

					this.hmmerAvailable = true;
					this.stats_hmmerAvailable = true;
					hmmer_count ++;
				}

				if(rs.getString(1).equalsIgnoreCase("ncbi-blastp")
						|| rs.getString(1).equalsIgnoreCase("blastp")) {

					this.blastPAvailable = true;
					this.stats_blastPAvailable = true;
					ncbip_count ++;
				}

				if(rs.getString(1).equalsIgnoreCase("ncbi-blastx")
						|| rs.getString(1).equalsIgnoreCase("blastx")) {

					this.stats_blastXAvailable = true;
				//	ncbix_count ++;
				}
			}

			if(ncbip_count != hmmer_count) {

				if(ncbip_count> hmmer_count) {

					this.hmmerAvailable = false;
				}
				else {

					this.blastPAvailable = false;
				}
			}
			rs = stmt.executeQuery("SELECT * FROM homologyData ");
			this.hasCommittedData=rs.next();

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		this.setBlastType();

		String[][] res_bp = null, res_hm = null, res_bx = null;
		int res_length = 0;
		int countStats = 0;

		if(this.stats_blastPAvailable) {
			
			res_bp = this.getSpecificStats("'ncbi-blastp' OR program = 'blastp'");
			countStats++;
			res_length += res_bp.length;
		}
		
		if(this.stats_hmmerAvailable) {

			if (countStats>0)
				res_length+=3;
			
			res_hm = this.getSpecificStats("'hmmer'");
			countStats++;
			res_length += res_hm.length;
		}

		if(this.stats_blastXAvailable) {

			if (countStats>0)
				res_length+=3;
			
			res_bx = this.getSpecificStats("'ncbi-blastx' OR program = 'blastx'");
			countStats++;
			res_length += res_bx.length;
		}

		String[][] res = new String[res_length][];

		if(countStats>1) {

			int index = 0;
			
			if(this.stats_blastPAvailable) {

				res [index] = new String[]{"BLASTP"};

				for(int i = 0 ; i< res_bp.length; i++) {

					index++;
					res[index] = res_bp [i];
				}
			}

			if(this.stats_hmmerAvailable) {

				if(index>0) {

					index++;
					res[index] = new String[]{};
					index++;
				}

				//index++;
				res[index] = new String[]{"HMMER"};

				for(int i = 0 ; i< res_hm.length; i++) {

					index++;
					res[index] = res_hm [i];
				}
			}

			if(this.stats_blastXAvailable) {

				if(index>0) {

					index++;
					res[index] = new String[]{};
					index++;
				}

				//index++;
				res[index] = new String[]{"BLASTX"};

				for(int i = 0 ; i< res_bx.length; i++) {

					index++;
					res[index] = res_bx [i];
				}
			}

			return res;
		}
		else if(this.blastPAvailable) {

			return res_bp;
		}
		if(this.hmmerAvailable) {

			return res_hm;
		}
		else {

			return res_bx;
		}
	}

	/**
	 * @param program
	 * @return
	 */
	public String[][] getSpecificStats(String program) {

		int num=0, noLocusTag=0, noQuery=0, noGene=0, noChromosome=0, noOrganelle=0, no_similarity=0;

		String[][] res = new String[15][];
		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT geneHomology.s_key, locusTag, query, gene, chromosome, organelle " +
					" FROM geneHomology" +
					" LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
					" WHERE status<>'NO_SIMILARITY' AND (program = "+program+" )");

			while(rs.next()) {

				num++;
				if(rs.getString(2)==null) noLocusTag++;
				if(rs.getString(3)==null) noQuery++;
				if(rs.getString(4)==null || rs.getString(4).isEmpty()) noGene++;
				if(rs.getString(5)==null) noChromosome++;
				if(rs.getString(6)==null || rs.getString(6).isEmpty()) noOrganelle++;
			}

			stmt = this.connection.createStatement();
			rs = stmt.executeQuery("SELECT *" +
					" FROM geneHomology" +
					" LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
					" WHERE status='NO_SIMILARITY' AND (program = "+program+" )");

			while(rs.next()) {
				no_similarity++;
				num++;
			}

			res[0] = new String[] {"Number of Genes", ""+num};
			res[1] = new String[] {"Number of Genes without similarities", ""+no_similarity};
			res[2] = new String[] {"Number of Genes with unavailable locus tag", ""+noLocusTag};
			res[3] = new String[] {"Number of Genes with unavailable query", ""+noQuery};
			res[4] = new String[] {"Number of Genes with unavailable gene name", ""+noGene};
			res[5] = new String[] {"Number of Genes with unavailable chromosome identifier", ""+noChromosome};
			res[6] = new String[] {"Number of Genes with unavailable organelle alocation", ""+noOrganelle};

			int r1=0;

			String[][] rs1 = this.select("SELECT count(*)" +
					" FROM homologySetup " +
					" LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
					" LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
					" WHERE program = "+program+" ");

			String homologues = rs1[r1][0];

			res[7] = new String[] {"Number of homologue genes  ", homologues};

			double homologueAv = (new Double(homologues)).intValue()/(new Double(num));

			res[8] = new String[] {"Average homologues per gene", ""+homologueAv};

			rs = stmt.executeQuery("SELECT DISTINCT taxonomy, organism "+
					" FROM homologySetup" +
					" LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
					" LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
					" LEFT JOIN homologues ON (homologues_s_key = homologues.s_key)" +
					" LEFT JOIN organism ON (organism_s_key = organism.s_key)" +
					" WHERE program = "+program+" ");

			int orgNum=0, eukaryota=0, bacteria=0, archea=0, virus=0, other=0;

			while(rs.next()) {

				orgNum++;
				if(rs.getString(1)!=null) {

					if(rs.getString(1).startsWith("Eukaryota")) eukaryota++;
					if(rs.getString(1).startsWith("Bacteria")) bacteria++;
					if(rs.getString(1).startsWith("Archaea")) archea++;
					if(rs.getString(1).startsWith("Viruses")) virus++;
					if(rs.getString(1).startsWith("other sequences")) other++;
				}
			}

			res[9] = new String[] {"Number of organisms with at least one homologue gene", ""+orgNum};
			res[10] = new String[] {"\t Eukaryota:\t", ""+eukaryota};
			res[11] = new String[] {"\t Bacteria:\t", ""+bacteria};
			res[12] = new String[] {"\t Archaea:\t", ""+archea};
			res[13] = new String[] {"\t Viruses:\t", ""+virus};
			res[14] = new String[] {"\t other sequences:\t", ""+other};

			rs.close();
			stmt.close();
		}
		catch(Exception e){e.printStackTrace();}

		return res;
	}

	/**
	 * @param alfa
	 * @return
	 */
	public GenericDataTable getAllGenes() {

		this.isEukaryote = this.setIsEukaryote();

		this.getProject().setInitialiseHomologyData(false);

		this.setBlastType();

		ArrayList<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Info");
		columnsNames.add("Genes");
		columnsNames.add("Status");
		columnsNames.add("Name");

		if(isEukaryote()) {
			columnsNames.add("Chromosome");
		}
		columnsNames.add("Product");
		columnsNames.add("Score");
		columnsNames.add("EC Number(s)");
		columnsNames.add("Score");
		columnsNames.add("Notes");
		columnsNames.add("Select");

		this.keys = new TreeMap<Integer, String>();
		this.reverseKeys = new TreeMap<Integer, Integer>();
		GenericDataTable qrt = new GenericDataTable(columnsNames, "EC Numbers", "") {

			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col<4 || (isEukaryote() && col == 4) || this.getColumnClass(col).equals(Boolean.class) 
						|| this.getColumnClass(col).equals(String[].class) || this.getColumnName(col).equals("Notes"))  {
					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();
			DecimalFormatSymbols separator = new DecimalFormatSymbols();
			separator.setDecimalSeparator('.');
			DecimalFormat format = new DecimalFormat("##.##",separator);
			boolean thresholdBool = false;

			//GENE |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			Map<Integer,List<Object>> geneData = this.getGeneInformation(stmt);

			//PROD |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			Map<String,String> prodRank = new TreeMap<String,String>();
			Map<String,String> prodName = new TreeMap<String,String>();
			Map<String,List<String>> prodKeys = this.getProductRank(stmt,prodRank,prodName,format);//, alpha);

			//EC |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			Map<String,String> ecRank = new TreeMap<String,String>();
			Map<String,String> ecName = new TreeMap<String,String>();
			Map<String,List<String>> ecKeys = this.getECRank(stmt,ecRank,ecName,format);//, alpha);

			//////////////////////////////////////////////////////////////////////////

			int size = this.getArraySize(this.keys.values());

			this.enzyme = new String[(size)+1][];
			this.ecnPercent = new String[(size)+1][];
			this.prodPercent = new String[(size)+1][];
			this.product = new String[(size)+1][];

			for(Integer index  : geneData.keySet()) {

				List<Object> dataList = geneData.get(index);
				dataList = this.processProductNamesData(index, dataList, prodKeys, prodRank, prodName, format);
				dataList = this.processECNumberData(index, dataList, ecKeys, ecRank, ecName, format,  thresholdBool);
				geneData.put(index, dataList);
			}

			if(this.blastPAvailable && this.hmmerAvailable) {

				geneData = this.processBlastHmmerSimilarities(geneData, size);
			}

			for(Integer key:this.keys.keySet()) {

				qrt.addLine(geneData.get(key), this.keys.get(key));
			}

			stmt.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
		//				for(int i=0; i<qrt.getRowCount(); i++)
		//				{
		//					for(Object o : qrt.getRow(i)){
		//						System.out.print(o+"\t");
		//					}
		//					System.out.println();
		//				}

		return qrt;
	}

	/**
	 * @param geneData
	 * @param size
	 * @return
	 */
	private Map<Integer, List<Object>> processBlastHmmerSimilarities(Map<Integer,List<Object>> geneData, int size) {

		Map<Integer,List<Object>> newGeneData = new TreeMap<Integer,List<Object>>();
		Map<String,Integer> newtableRowIndex = new TreeMap<String, Integer>();
		Map<Integer, String> newKeys = new TreeMap<Integer, String>(), 
				newName = new TreeMap<Integer, String>(), 	newInitialNames = new TreeMap<Integer, String>(),
				newLocus = new TreeMap<Integer, String>(), newInitialLocus = new TreeMap<Integer, String>(), 
				newChromosome = new TreeMap<Integer, String>(), newinitialChromosome = new TreeMap<Integer, String>(),
				newNotesMap = new TreeMap<Integer, String>();

		int counter = 0;
		String[][] jointProducts = new String[size][];
		String[][] jointProductsPercent = new String[size][];
		String[][] jointEnzymes = new String[size][];
		String[][] jointEnzymesPercent = new String[size][];

		this.initialProdItem = new TreeMap<Integer, String>();
		this.initialEcItem = new TreeMap<Integer, String>();
		this.initialSelectedGene = new TreeMap<Integer, Boolean>();
		this.reverseKeys =  new TreeMap<Integer, Integer>();

		for(Integer index  : this.blastPGeneDataEntries.keySet()) {

			Integer key = Integer.parseInt(this.keys.get(index)); 
			newKeys.put(counter , key+"");
			this.reverseKeys.put(key, counter);
			newtableRowIndex.put(key+"",counter);

			if(this.initialLocus.containsKey(key)) {

				newInitialLocus.put(counter , this.initialLocus.get(index));
			}
			else {

				if(this.initialLocus.containsKey(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index)))) {

					newInitialLocus.put(counter , this.initialLocus.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))));
				}
			}

			if(this.initialNames.containsKey(key)) {

				newInitialNames.put(counter , this.initialNames.get(index));
			}

			if(this.initialChromosome.containsKey(key)) {

				newinitialChromosome.put(counter , this.initialChromosome.get(index));
			}

			//////////////////////////////////////////////////////////////////////////////

			if(this.namesList.containsKey(key)) {

				newName.put(key , this.namesList.get(key));
			}

			if(this.locusList.containsKey(key)) {

				newLocus.put(key , this.locusList.get(key));
			}

			if(this.chromosome.containsKey(key)) {

				newChromosome.put(key , this.chromosome.get(key));
			}

			if(this.notesMap.containsKey(key)) {

				newNotesMap.put(key , this.notesMap.get(key));
			}

			List<String> products = new ArrayList<String>(),productsPercentage = new ArrayList<String>(),
					ec = new ArrayList<String>(), ecPercentage = new ArrayList<String>();

			List<Object> dataList = this.joinBlastAndHmmer(geneData.get(index), geneData.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))),
					products, productsPercentage,
					ec, ecPercentage, 
					index, counter);

			jointProducts[key] = products.toArray(new String[products.size()]);
			jointProductsPercent[key] =  productsPercentage.toArray(new String[productsPercentage.size()]);
			jointEnzymes[key] = ec.toArray(new String[ec.size()]);
			jointEnzymesPercent[key] = ecPercentage.toArray(new String[ecPercentage.size()]);

			newGeneData.put(counter, dataList);
			counter++;
		}

		this.product = jointProducts;
		this.prodPercent =  jointProductsPercent;
		this.enzyme = jointEnzymes;
		this.ecnPercent = jointEnzymesPercent;
		this.keys = newKeys;
		this.tableRowIndex=newtableRowIndex;
		this.namesList = newName;
		this.initialNames = newInitialNames;
		this.locusList = newLocus;
		this.initialLocus = newInitialLocus;
		this.chromosome = newChromosome;
		this.initialChromosome = newinitialChromosome;
		this.notesMap = newNotesMap;

		return newGeneData;
	}

	/**
	 * @param data_blast
	 * @param data_hmmer
	 * @param ecPercentage 
	 * @param ec 
	 * @param productsPercentage 
	 * @param products 
	 * @param index
	 * @param row 
	 * @return
	 */
	private List<Object> joinBlastAndHmmer(List<Object> data_blast, List<Object> data_hmmer, List<String> products, List<String> productsPercentage, List<String> ec, List<String> ecPercentage, int index, int row) {

		List<Object> ql = new ArrayList<Object>();

		boolean selected = false, processNext=true;

		for(int i = 0; i < data_blast.size() ; i++) {

			if(processNext) {

				Object data = data_blast.get(i);

				if (data == null) {

					ql.add(data);
				}
				else if(data.getClass().isArray()) {

					int name = i;

					List<Object> scores_list = this.processArrays((String[]) data, (String[]) data_hmmer.get(name),true,index, products, productsPercentage, row); 

					for(Object obj:scores_list) {

						ql.add(obj);
					}

					i++;
					i++;
					int ecs = i;

					scores_list = this.processArrays((String[]) data_blast.get(ecs), (String[]) data_hmmer.get(ecs), false,index, ec, ecPercentage, row); 

					if(scores_list.size()>0 && !((String) scores_list.get(1)).isEmpty()) {

						if(((String) scores_list.get(1)).equals("manual")) {

							selected = true;
						}
						else {

							if(Double.parseDouble(((String) scores_list.get(1))) >= this.threshold) {

								selected = true;
							}
							else {

								selected = false;
								scores_list.set(1,"<"+this.threshold);
							}
						}
					}

					for(Object obj : scores_list) {

						ql.add(obj);
					}
					processNext=false;
				}
				else if(data.getClass().equals(Boolean.class)) {

					ql.add(selected);
					this.initialSelectedGene.put(row, selected);
					processNext = true;
				}
				else {

					ql.add(data);
					processNext = true;
				}
			}
			else {

				processNext=true;
			}
		}

		return ql;
	}

	/**
	 * @param blast_data
	 * @param hmmer_data
	 * @param product
	 * @param index
	 * @param dataList
	 * @param dataPercentage
	 * @param row 
	 * @return
	 */
	private List<Object> processArrays(String[] blast_data, String[] hmmer_data, boolean product, int index, List<String> dataList, List<String> dataPercentage, int row) {

		List<Object> array = new ArrayList<Object>();

		Set<String> data = new TreeSet<String>();

		for(int i = 0; i < blast_data.length; i++) {

			data.add(blast_data[i]);

		}

		for(int i = 0; i < hmmer_data.length; i++) {

			data.add(hmmer_data[i]);
		}

		@SuppressWarnings("unchecked")
		Pair<String,Double> pairs[] = new Pair[data.size()];

		int j=0;
		for(String data_name:data) {

			Double blast_score=0.0, hmmer_score=0.0, final_score = 0.0;

			for(int i = 0; i < blast_data.length; i++) {


				if(product) {

					if(this.product[Integer.parseInt(keys.get(index))][i].equals(data_name)) {

						if(prodPercent[Integer.parseInt(keys.get(index))][i].isEmpty()) {

							blast_score = 0.0;
						}
						else {
							blast_score = Double.parseDouble(prodPercent[Integer.parseInt(keys.get(index))][i]);
						}
					}
				}
				else {

					if(this.enzyme[Integer.parseInt(keys.get(index))][i].equals(data_name)) {

						if(ecnPercent[Integer.parseInt(keys.get(index))][i].isEmpty()) {

							blast_score = 0.0;
						}
						else {

							blast_score = Double.parseDouble(ecnPercent[Integer.parseInt(keys.get(index))][i]);
						}
					}
				}

			}

			for(int i = 0; i < hmmer_data.length; i++) {

				if(product) {

					if(this.product[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].equals(data_name)) {


						if(prodPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].isEmpty()) {
							hmmer_score = 0.0;
						}
						else {

							hmmer_score = Double.parseDouble(prodPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i]);
						}
					}
				}
				else {

					if(this.enzyme[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].equals(data_name)) {

						if(ecnPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].isEmpty()) {

							hmmer_score = 0.0;
						}
						else {

							hmmer_score = Double.parseDouble(ecnPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i]);
						}
					}
				}
			}

			final_score = (blast_score*this.blastHmmerWeight + hmmer_score*(1-this.blastHmmerWeight));

			if(blast_score>0 && hmmer_score>0) {

				//final_score = (blast_score*this.blastHmmerWeight + hmmer_score*(1-this.blastHmmerWeight));
			}
			else if(blast_score>0) {

				//				if (this.hmmerAvailable && this.blastHmmerWeight!=0) {
				//
				//					final_score = (blast_score*0.75);
				//				}
				//				else {
				//
				//					final_score = (blast_score);
				//				}
			}
			else if(hmmer_score>0) {

				//				if (this.blastPAvailable && this.blastHmmerWeight!=1) {
				//
				//					final_score = (hmmer_score*0.75);
				//				}
				//				else {
				//
				//					final_score = (hmmer_score);
				//				}
			}
			Pair<String,Double> pair = new Pair<String,Double>(data_name,final_score);
			pairs[j] = pair;
			j++;
		}

		Arrays.sort(pairs, new PairComparator<Double>());

		String[] results_data = new String[data.size()];
		String[] results_score = new String[data.size()];
		DecimalFormatSymbols separator = new DecimalFormatSymbols();
		separator.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("##.##",separator);

		j=0;
		while (j < pairs.length) {

			String data_name = pairs[j].getA();
			Double final_score = pairs[j].getB();
			results_data[j] = data_name;

			if(final_score>=0) {

				results_score[j] = format.format(final_score);
			}
			else {

				results_score[j]="manual";

			}

			dataList.add(j,data_name);
			dataPercentage.add(j,results_score[j]);

			j++;
		}

		if(product) {

			if(results_data.length>0) {

				this.initialProdItem.put(row, results_data[0]);
			}
			else{

				this.initialProdItem.put(row, "");
			}
		}
		else {

			if(results_score.length>0 && !results_data[0].isEmpty()) {

				if(results_score[0].equalsIgnoreCase("manual")) {

					this.initialEcItem.put(row, results_data[0]);
				} 
				else if(Double.parseDouble(results_score[0]) >= this.threshold) {

					this.initialEcItem.put(row, results_data[0]);
				}
				else {

					this.initialEcItem.put(row, "");
				}
			}
		}

		if(results_data.length>0) {

			array.add(0,results_data);
			array.add(1,results_score[0]);
		}
		else {

			array.add(0,new String[0]);
			array.add(1,"");
		}

		return array;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneLocus(Integer id) {

		return this.initialLocus.get(id);
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(int row) {

		int key = Integer.parseInt(this.getKeys().get(row));

		ResultSet rset;
		String[][] rs;
		int r;
		DataTable[] res = null;

		try {

			rs = this.select("SELECT query FROM geneHomology WHERE geneHomology.s_key = '" + key+"' ");
			String query = "";
			r=0;

			while(r<rs.length) {

				query = rs[r][0];
				r++;
			}

			boolean gene_hmmerAvailable = false, gene_blastPAvailable = false, gene_blastXAvailable = false;

			Statement stmt = this.connection.createStatement();

			rset = stmt.executeQuery("SELECT program" +
					" FROM homologySetup " +
					"INNER JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key) " +
					"WHERE query = '" + query +"' ");

			while (rset .next()) {

				if(rset.getString(1).equalsIgnoreCase("hmmer")) {

					gene_hmmerAvailable = true;
				}

				if(rset.getString(1).equalsIgnoreCase("ncbi-blastp")
						|| rset.getString(1).equalsIgnoreCase("blastp")) {

					gene_blastPAvailable = true;
				}

				if(rset.getString(1).equalsIgnoreCase("ncbi-blastx")
						|| rset.getString(1).equalsIgnoreCase("blastx")) {

					gene_blastXAvailable = true;
				}
			}

			stmt.close();
			rset.close();

			int statsCounter =  0;

			if(gene_hmmerAvailable) {

				statsCounter++;
			}

			if(gene_blastPAvailable) {

				statsCounter++;
			}

			if(gene_blastXAvailable) {

				statsCounter++;
			}
			res = new DataTable[statsCounter*2+2];

			int datatableCounter = 0;
			ArrayList<String> columnsNames = new ArrayList<String>();

			ArrayList<String> ql;

			if(gene_blastPAvailable) {

				columnsNames.add("Reference ID");
				columnsNames.add("Locus ID");
				columnsNames.add("Status");
				columnsNames.add("Organism");
				columnsNames.add("E Value");
				columnsNames.add("Score (bits)");
				columnsNames.add("Product");
				columnsNames.add("EC Number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - BLASTP");

				String previous_homology_s_key="";
				String ecnumber="";
				ql = new ArrayList<String>();
				stmt = this.connection.createStatement();
				rset = stmt.executeQuery("SELECT referenceID,locusID,organism, geneHomology_has_homologues.eValue, bits, product," +
						" homologues.s_key, ecnumber, homologues.uniprot_star, program " +
						"FROM homologySetup " +
						"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
						"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
						"LEFT JOIN homologues on (homologues.s_key = homologues_s_key)" +
						"LEFT JOIN organism on (homologues.organism_s_key = organism.s_key)" +
						"LEFT JOIN homologues_has_ecNumber on (homologues.s_key = homologues_has_ecNumber.homologues_s_key)" +
						"LEFT JOIN ecNumber on (homologues_has_ecNumber.ecNumber_s_key = ecNumber.s_key)" +
						"WHERE query = '" + query +"' " +
						"AND (program='ncbi-blastp' OR program='blastp')");

				while(rset.next()) {

					String s_key = "";
					if(rset.getString(7) != null) {

						s_key = rset.getString(7);


						if(previous_homology_s_key.equals(s_key)) {

							ecnumber+=", "+rset.getString(8);
						}
						else {

							previous_homology_s_key=s_key;
							if(!ql.isEmpty()) {

								ql.add(ecnumber);
								res[datatableCounter].addLine(ql);
							}
							ecnumber="";
							ql = new ArrayList<String>();
							ql.add(rset.getString(1));
							ql.add(rset.getString(2));
							ql.add(rset.getString(9));
							ql.add(rset.getString(3));
							ql.add(rset.getString(4));
							ql.add(rset.getString(5));
							ql.add(rset.getString(6));

							if(rset.getString(8)!=null) {

								ecnumber=rset.getString(8);
							}
						}
						if(rset.isLast())
						{
							ql.add(ecnumber);
							res[datatableCounter].addLine(ql);
						}		
					}
					else {

						ql = new ArrayList<String>();
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						res[datatableCounter].addLine(ql);
					}
				}
				stmt.close();
				datatableCounter++;
				columnsNames = new ArrayList<String>();
				columnsNames.add("Organism");
				columnsNames.add("Phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy - BLASTP");

				rs = this.select(
						"SELECT organism,taxonomy " +
								"FROM homologySetup " +
								"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
								"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
								"LEFT JOIN homologues ON (homologues_s_key = homologues.s_key)" +
								"LEFT JOIN organism ON (organism_s_key = organism.s_key) " +
								"WHERE query = '" + query +"' " +
						"AND (program='ncbi-blastp' OR program='blastp')");

				r=0;
				while(r<rs.length) {

					ql = new ArrayList<String>();
					ql.add(rs[r][0]);
					ql.add(rs[r][1]);
					res[datatableCounter].addLine(ql);
					r++;
				}
				datatableCounter++;
			}

			if(gene_blastXAvailable) {

				columnsNames.add("Reference ID");
				columnsNames.add("Locus ID");
				columnsNames.add("Status");
				columnsNames.add("Organism");
				columnsNames.add("E Value");
				columnsNames.add("Score (bits)");
				columnsNames.add("Product");
				columnsNames.add("EC Number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - BLASTX");

				String previous_homology_s_key="";
				String ecnumber="";
				ql = new ArrayList<String>();
				stmt = this.connection.createStatement();
				rset = stmt.executeQuery("SELECT referenceID,locusID,organism, geneHomology_has_homologues.eValue, bits, product, homologues.s_key, ecnumber, homologues.uniprot_star, program " +
						"FROM homologySetup " +
						"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
						"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
						"LEFT JOIN homologues on (homologues.s_key = homologues_s_key)" +
						"LEFT JOIN organism on (organism_s_key = organism.s_key)" +
						"LEFT JOIN homologues_has_ecNumber on (homologues.s_key = homologues_has_ecNumber.homologues_s_key)" +
						"LEFT JOIN ecNumber on (ecNumber_s_key = ecNumber.s_key)" +
						"WHERE query = '" + query +"' " +
						"AND (program='ncbi-blastx' OR program='blastx')");

				while(rset.next()) {

					String s_key = "";
					if(rset.getString(7) != null) {

						s_key = rset.getString(7);


						if(previous_homology_s_key.equals(s_key)) {

							ecnumber+=", "+rset.getString(8);
						}
						else {

							previous_homology_s_key=s_key;
							if(!ql.isEmpty()) {

								ql.add(ecnumber);
								res[datatableCounter].addLine(ql);
							}
							ecnumber="";
							ql = new ArrayList<String>();
							ql.add(rset.getString(1));
							ql.add(rset.getString(2));
							ql.add(rset.getString(9));
							ql.add(rset.getString(3));
							ql.add(rset.getString(4));
							ql.add(rset.getString(5));
							ql.add(rset.getString(6));

							if(rset.getString(8)!=null) {

								ecnumber=rset.getString(8);
							}
						}
						if(rset.isLast())
						{
							ql.add(ecnumber);
							res[datatableCounter].addLine(ql);
						}		
					}
					else {

						ql = new ArrayList<String>();
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						res[datatableCounter].addLine(ql);
					}
				}
				stmt.close();
				datatableCounter++;
				columnsNames = new ArrayList<String>();
				columnsNames.add("Organism");
				columnsNames.add("Phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy - BLASTX");

				rs = this.select(
						"SELECT organism,taxonomy " +
								"FROM homologySetup " +
								"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
								"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
								"LEFT JOIN homologues ON (homologues_s_key = homologues.s_key)" +
								"LEFT JOIN organism ON (organism_s_key = organism.s_key) " +
								"WHERE query = '" + query +"' " +
						"AND (program='ncbi-blastx' OR program='blastx')");

				r=0;
				while(r<rs.length) {

					ql = new ArrayList<String>();
					ql.add(rs[r][0]);
					ql.add(rs[r][1]);
					res[datatableCounter].addLine(ql);
					r++;
				}
				datatableCounter++;
			}

			if(gene_hmmerAvailable) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("Reference ID");
				columnsNames.add("Locus ID");
				columnsNames.add("Status");
				columnsNames.add("Organism");
				columnsNames.add("E Value");
				columnsNames.add("Score (bits)");
				columnsNames.add("Product");
				columnsNames.add("EC Number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - HMMER");

				ql = new ArrayList<String>();

				String previous_homology_s_key="";
				String ecnumber="";
				ql = new ArrayList<String>();
				stmt = this.connection.createStatement();
				rset = stmt.executeQuery("SELECT referenceID,locusID,organism, geneHomology_has_homologues.eValue, bits, product, homologues.s_key, ecnumber, homologues.uniprot_star, program " +
						"FROM homologySetup " +
						"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
						"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
						"LEFT JOIN homologues on (homologues.s_key = homologues_s_key)" +
						"LEFT JOIN organism on (organism_s_key = organism.s_key)" +
						"LEFT JOIN homologues_has_ecNumber on (homologues.s_key = homologues_has_ecNumber.homologues_s_key)" +
						"LEFT JOIN ecNumber on (homologues_has_ecNumber.ecNumber_s_key = ecNumber.s_key)" +
						"WHERE query = '" + query +"' " +
						"AND program='hmmer'");

				while(rset.next()) {

					String s_key = "";
					if(rset.getString(7) != null) {

						s_key = rset.getString(7);


						if(previous_homology_s_key.equals(s_key)) {

							ecnumber+=", "+rset.getString(8);
						}
						else {

							previous_homology_s_key=s_key;
							if(!ql.isEmpty()) {

								ql.add(ecnumber);
								res[datatableCounter].addLine(ql);
							}
							ecnumber="";
							ql = new ArrayList<String>();
							ql.add(rset.getString(1));
							ql.add(rset.getString(2));
							ql.add(rset.getString(9));
							ql.add(rset.getString(3));
							ql.add(rset.getString(4));
							ql.add(rset.getString(5));
							ql.add(rset.getString(6));

							if(rset.getString(8)!=null) {

								ecnumber=rset.getString(8);
							}
						}
						if(rset.isLast())
						{
							ql.add(ecnumber);
							res[datatableCounter].addLine(ql);
						}		
					}	
					else {

						ql = new ArrayList<String>();
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						ql.add("");
						res[datatableCounter].addLine(ql);
					}
				}

				stmt.close();
				datatableCounter++;

				columnsNames = new ArrayList<String>();
				columnsNames.add("Organism");
				columnsNames.add("Phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy HMMER");

				rs = this.select(
						"SELECT organism,taxonomy " +
								"FROM homologySetup " +
								"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key)" +
								"LEFT JOIN geneHomology_has_homologues ON (geneHomology_s_key = geneHomology.s_key)" +
								"LEFT JOIN homologues ON (homologues_s_key = homologues.s_key)" +
								"LEFT JOIN organism ON (organism_s_key = organism.s_key) " +
								"WHERE query = '" + query +"' " +
						"AND program='hmmer'");

				r=0;
				while(r<rs.length) {

					ql = new ArrayList<String>();
					ql.add(rs[r][0]);
					ql.add(rs[r][1]);
					res[datatableCounter].addLine(ql);
					r++;
				}
				datatableCounter++;
			}
			{

				columnsNames = new ArrayList<String>();
				columnsNames.add("FASTA a.a. Sequence");
				res[datatableCounter] = new DataTable(columnsNames, "Sequence");

				rs = this.select(
						"SELECT DISTINCT(sequence) " +
								"FROM fastaSequence " +
								"LEFT JOIN geneHomology ON (fastaSequence.geneHomology_s_key = geneHomology.s_key)" +
								"WHERE query = '" + query +"' " );

				r=0;
				while(r<rs.length) {
					ql = new ArrayList<String>();
					ql.add(this.setSequenceView(rs[r][0]));
					res[datatableCounter].addLine(ql);
					r++;
				}	

				datatableCounter++;
			}

			{
				columnsNames = new ArrayList<String>();
				columnsNames.add("program");
				columnsNames.add("version");
				columnsNames.add("databaseID");
				columnsNames.add("eValue");
				columnsNames.add("matrix");
				columnsNames.add("wordSize");
				columnsNames.add("gapCosts");
				columnsNames.add("max number Of alignments");
				res[datatableCounter] = new DataTable(columnsNames, "Setup Parameters");

				rs = this.select(
						"SELECT program, version, databaseID, eValue, matrix, wordSize, gapCosts, maxNumberOfAlignments " +
								"FROM homologySetup " +
								"LEFT JOIN geneHomology ON (homologySetup_s_key = homologySetup.s_key)" +
								"WHERE query = '" + query +"' " );
				r=0;
				while(r<rs.length) {

					ql = new ArrayList<String>();
					ql.add(rs[r][0]);
					ql.add(rs[r][1]);
					ql.add(rs[r][2]);
					ql.add(rs[r][3]);
					ql.add(rs[r][4]);
					ql.add(rs[r][5]);
					ql.add(rs[r][6]);
					ql.add(rs[r][7]);
					res[datatableCounter].addLine(ql);
					r++;
				}	
			}
		}
		catch(Exception e) {

			e.printStackTrace();
		}

		return res;
	}

	/**
	 * @param id
	 * @return
	 * @throws SQLException 
	 */
	public DataTable getECBlastSelectionPane(String locus) {

		ArrayList<String> columnsNames = new ArrayList<String>();
		DecimalFormatSymbols separator = new DecimalFormatSymbols();
		separator.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("##.##",separator);

		columnsNames.add("Products");
		columnsNames.add("frequency (%)");
		columnsNames.add("occurrences");
		columnsNames.add("frequency score");
		columnsNames.add("taxonomy score");
		columnsNames.add("final score");
		columnsNames.add("program");

		DataTable q = new DataTable(columnsNames, "EC Number(s) Selection");

		try 
		{
			String[][] rs;
			rs = this.select("SELECT ecNumberRank.s_key, ecNumber, rank, program " +
					"FROM ecNumberRank"+
					//"where geneHomology_s_key = "+ id+" ORDER BY rank desc;");
					" LEFT JOIN geneHomology ON (geneHomology_s_key = geneHomology.s_key)" +
					" LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
					"WHERE locusTag = '" + locus+"' " +
					//"WHERE geneHomology.s_key = '" + id+"' " +
					"ORDER BY program, rank DESC;");

			int r=0;
			double blastTotal=0;
			double hmmerTotal=0;

			while(r<rs.length) {
				if(rs[r][3].equals("hmmer")) {

					hmmerTotal+=Double.parseDouble((rs[r][2]));
				}
				else {

					blastTotal+=Double.parseDouble((rs[r][2]));
				}
				r++;
			}

			r=0;
			while(r<rs.length) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs[r][1]);
				if(rs[r][3].equals("hmmer")) {
					ql.add(format.format(Double.parseDouble(rs[r][2])/hmmerTotal*100)+" %");
				}
				else {
					ql.add(format.format(Double.parseDouble(rs[r][2])/blastTotal*100)+" %");
				}
				ql.add(rs[r][2]);
				ql.add(this.getScore1().get(rs[r][0]));
				ql.add(this.getScore2().get(rs[r][0]));
				ql.add(this.getScore().get(rs[r][0]));
				ql.add(rs[r][3]);
				q.addLine(ql);
				r++;	
			}

		}
		catch (SQLException e) {e.printStackTrace();}
		return q;
	}

	/**
	 * @param id
	 * @return
	 */
	public DataTable getProductSelectionPane(String locus){

		ArrayList<String> columnsNames = new ArrayList<String>();
		DecimalFormatSymbols separator = new DecimalFormatSymbols();
		separator.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("##.##",separator);

		columnsNames.add("Products");
		columnsNames.add("frequency (%)");
		columnsNames.add("occurrences");
		columnsNames.add("frequency score");
		columnsNames.add("taxonomy score");
		columnsNames.add("final score");
		columnsNames.add("program");

		DataTable q = new DataTable(columnsNames, "Product Selection");

		try {

			String[][] rs = this.select(
					"SELECT productRank.s_key, productName, rank, program " +
							"FROM productRank " +
							//"where geneHomology_s_key = "+ id+" ORDER BY rank desc;");
							" LEFT JOIN geneHomology ON (geneHomology_s_key = geneHomology.s_key)" +
							" LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
							" WHERE locusTag = '" + locus+"' " +
							//" WHERE geneHomology.s_key = '" + id +"' " +
					"ORDER BY program, rank DESC;");

			int r=0;
			double blastTotal=0;
			double hmmerTotal=0;

			while(r<rs.length) {
				if(rs[r][3].equals("hmmer")) {

					hmmerTotal+=Double.parseDouble((rs[r][2]));
				}
				else {

					blastTotal+=Double.parseDouble((rs[r][2]));
				}
				r++;
			}

			r=0;
			while(r<rs.length) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs[r][1]);
				if(rs[r][3].equals("hmmer")) {
					ql.add(format.format(Double.parseDouble(rs[r][2])/hmmerTotal*100)+" %");
				}
				else {
					ql.add(format.format(Double.parseDouble(rs[r][2])/blastTotal*100)+" %");
				}
				ql.add(rs[r][2]);
				ql.add(this.getScoreP1().get(rs[r][0]));
				ql.add(this.getScoreP2().get(rs[r][0]));
				ql.add(this.getScoreP().get(rs[r][0]));
				ql.add(rs[r][3]);
				q.addLine(ql);
				r++;	
			}

		}


		catch(Exception e){e.printStackTrace();}
		return q;
	}

	/**
	 * @param selectedItem
	 * @param row
	 * @return
	 */
	public String getECPercentage(String selectedItem, int row) {

		for(int i = 0; i < this.enzyme[Integer.parseInt(keys.get(row))].length; i++) {

			if(this.enzyme[Integer.parseInt(keys.get(row))][i].equals(selectedItem)) {

				return this.ecnPercent[Integer.parseInt(keys.get(row))][i];
			}
		}
		return "manual";
	}

	/**
	 * @param selectedItem
	 * @param row
	 * @return
	 */
	public String getProductPercentage(String selectedItem, int row) {

		for(int i = 0; i < this.product[Integer.parseInt(keys.get(row))].length; i++) {

			if(this.product[Integer.parseInt(keys.get(row))][i].equals(selectedItem)) {

				if(this.prodPercent[Integer.parseInt(keys.get(row))][i]=="0") {

					return "manual";
				}
				else{ 

					return this.prodPercent[Integer.parseInt(keys.get(row))][i];
				}
			}
		}
		return "manual";
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {

		return "Homology Data";
	}


	/**
	 * Remove the BLAST instance from AIBench 
	 */
	public void remove() {

		List<ClipboardItem> items = Core.getInstance().getClipboard().getItemsByClass(HomologyDataContainer.class);
		ClipboardItem torem = null;
		for(ClipboardItem item : items){
			if(item.getUserData().equals(this)){
				torem = item;
				break;
			}
		}
		Core.getInstance().getClipboard().removeClipboardItem(torem);
		System.gc();
	}

	/**
	 * @return
	 */
	public Map<String, List<String>> get_uniprot_ecnumbers() {

		try {

			Map<String, List<String>> result = new HashMap<String, List<String>>();
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT locusTag, uniprot_ecnumber FROM geneHomology");

			while(rs.next()) {

				if(rs.getString(2)!= null && !rs.getString(2).isEmpty()) {

					List<String> ecnumbers = new ArrayList<String>();
					String[] ecs = rs.getString(2).split(", ");

					for(String ec : ecs) {

						ecnumbers.add(ec.trim());
					}
					result.put(rs.getString(1), ecnumbers);
				}

			}
			stmt.close();
			return result;
		}
		catch(Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param row
	 * @param data
	 * 
	 * Sets a Map of user edited Product Lists
	 * 
	 */
	public void setEditedProductData(int row, String[] data) {

		this.editedProductData.put(row, data);
	}

	/**
	 * @return a Map of user edited Product Lists
	 */
	public Map<Integer, String[]> getEditedProductData() {

		return editedProductData;
	}

	/**
	 * @param data
	 * 
	 * Creates a new user edited product list
	 */
	public void setEditedProductData(Map<Integer, String[]> data) {
		this.editedProductData = data;
	}

	/**
	 * @param row
	 * @param data
	 * 
	 * Sets a Map of user edited Enzyme List
	 * 
	 */
	public void setEditedEnzymeData(int row, String[] data) {
		this.editedEnzymeData.put(row, data);
	}

	/**
	 * @return a Map of user edited Enzyme List
	 */
	public Map<Integer, String[]> getEditedEnzymeData() {
		return editedEnzymeData;
	}

	public void setEditedEnzymeData(Map<Integer, String[]> data) {
		this.editedEnzymeData = data;
	}

	/**
	 * @param selectedGene
	 * 
	 * Sets a Map of user edited Selected Gene
	 * 
	 */
	public void setSelectedGene(Map<Integer, Boolean> selectedGene) {
		this.selectedGene = selectedGene;
	}

	/**
	 * @return a Map of user edited Selected Gene
	 */
	public Map<Integer, Boolean> getSelectedGene() {
		return selectedGene;
	}

	/**
	 * Get previously committed Data
	 * 
	 */
	public void getCommittedData() {

		this.committedLocusList = new TreeMap<Integer, String>();
		this.committedNamesList = new TreeMap<Integer, String>();
		this.committedProdItem = new TreeMap<Integer, String>();
		this.committedEcItem = new TreeMap<Integer, String>();
		this.committedChromosome = new TreeMap<Integer, String>();
		this.committedNotesMap = new TreeMap<Integer, String>();
		this.committedSelected = new TreeMap<Integer, Boolean>();
		this.committedProductList = new TreeMap<Integer, String[]>();
		this.committedEnzymeList = new TreeMap<Integer, String[]>();

		Statement stmt;

		ResultSet rs;

		try {

			stmt = this.connection.createStatement();
			rs = stmt.executeQuery("SELECT * FROM homologyData ");

			while(rs.next()) {
				
				if(this.tableRowIndex.containsKey(rs.getString(2))) {
					
				int row = this.tableRowIndex.get(rs.getString(2));
				if(rs.getString(3) != null && !rs.getString(3).equalsIgnoreCase("null") && !rs.getString(3).isEmpty()) {

					this.committedLocusList.put(row, rs.getString(3));
				}

				if(rs.getString(4) != null && !rs.getString(4).equalsIgnoreCase("null") && !rs.getString(4).isEmpty()) {

					this.committedNamesList.put(row, rs.getString(4));
				}
				if(rs.getString(5) != null && !rs.getString(5).equalsIgnoreCase("null") && !rs.getString(5).isEmpty()) {

					this.committedProdItem.put(row, rs.getString(5));
				}

				if(rs.getString(6) != null && !rs.getString(6).equalsIgnoreCase("null") && !rs.getString(6).isEmpty()) {

					this.committedEcItem.put(row, rs.getString(6));
				}

				if(rs.getString(7) != null && !rs.getString(7).equalsIgnoreCase("null") && !rs.getString(7).isEmpty()) {

					this.committedSelected.put(row, rs.getBoolean(7));
				}

				if(rs.getString(8) != null && !rs.getString(8).equalsIgnoreCase("null") && !rs.getString(8).isEmpty()) {

					this.committedChromosome.put(row, rs.getString(8));
				}

				if(rs.getString(9) != null && !rs.getString(9).equalsIgnoreCase("null") && !rs.getString(9).isEmpty()) {

					this.committedNotesMap.put(row, rs.getString(9));
				}
			}
			}

			Set<String> dataSet = new HashSet<String>();
			int dataKey=-1;
			rs = stmt.executeQuery("SELECT geneHomology_s_key, otherNames FROM homologyData " +
					"RIGHT JOIN productList ON (homologyData.s_key = homologyData_s_key);");

			while(rs.next()) {

				if(dataSet.isEmpty()) { 

					dataKey = this.tableRowIndex.get(rs.getString(1));
					dataSet.add(rs.getString(2));
				}
				else {
					if (rs.getInt(1)==dataKey) {
						dataSet.add(rs.getString(2));
					}
					else {
						this.committedProductList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
						dataKey = this.tableRowIndex.get(rs.getString(1));
						dataSet = new HashSet<String>();
						dataSet.add(rs.getString(2));
					}

					if(rs.isLast()) {
						this.committedProductList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
					}
				}
			}

			dataSet = new HashSet<String>();
			dataKey = -1;
			rs = stmt.executeQuery("SELECT geneHomology_s_key, otherECnumbers FROM homologyData " +
					"RIGHT JOIN ecNumberList ON (homologyData.s_key = homologyData_s_key);");

			while(rs.next()) {

				if(dataSet.isEmpty()) { 

					dataKey = this.tableRowIndex.get(rs.getString(1));
					dataSet.add(rs.getString(2));
				}
				else {
					if (rs.getInt(1)==dataKey) {
						dataSet.add(rs.getString(2));
					}
					else {
						this.committedEnzymeList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
						dataKey = this.tableRowIndex.get(rs.getString(1));
						dataSet = new HashSet<String>();
						dataSet.add(rs.getString(2));
					}
					if(rs.isLast()) {
						this.committedEnzymeList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
					}
				}
			}

		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save user changes to the local database
	 * 
	 */
	public boolean commitToDatabase() {

		//this.tableRowIndex = new TreeMap<String, Integer>();
		boolean result = true;

		Statement stmt;

		ResultSet rs;
		try {

			stmt = this.connection.createStatement();

			Map<Integer, String> database_locus = new HashMap<Integer, String>();

			rs = stmt.executeQuery("SELECT s_key, locusTag FROM geneHomology");

			while(rs.next()) {

				database_locus.put(rs.getInt(1), rs.getString(2));
			}

			for(Integer key : this.getLocusList().keySet()) {

				rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"';");
				//this.tableRowIndex.put(keys.get(row), row);

				if(rs.next()) {

					stmt.execute("UPDATE homologyData SET locusTag='"+this.mysqlStrConverter(this.getLocusList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
				}
				else {

					stmt.execute("INSERT INTO homologyData (geneHomology_s_key, locusTag, product, ecNumber) " +
							"VALUES ('"+key+"','"+this.mysqlStrConverter(this.getLocusList().get(key))+"','null','null')");
				}
			}

			for(Integer key : this.getNamesList().keySet()) {

				rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

				//this.tableRowIndex.put(keys.get(row), row);

				if(this.getNamesList().get(key)!=null && !this.getNamesList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET geneName='"+this.mysqlStrConverter(this.getNamesList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, geneName, locusTag, product, ecNumber) " +
								"VALUES ('"+key+"', '"+this.mysqlStrConverter(this.getNamesList().get(key))+"','"+ database_locus.get(key)+"','null','null')");
					}
				}
			}

			for(Integer key : this.getChromosome().keySet()) {

				if(this.getChromosome().get(key)!=null && !this.getChromosome().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

					//this.tableRowIndex.put(keys.get(row), row);

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET chromosome='"+this.mysqlStrConverter(this.getChromosome().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key,chromosome, locusTag, product, ecNumber) " +
								"VALUES ('"+key+"','"+ this.mysqlStrConverter(this.getChromosome().get(key))+"','"+ database_locus.get(key)+"', 'null', 'null')");
					}
				}
			}

			for(Integer key : this.getNotesMap().keySet()) {

				if(this.getNotesMap().get(key)!=null && !this.getNotesMap().get(key).equalsIgnoreCase("null") && !this.getNotesMap().get(key).isEmpty() && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

					//this.tableRowIndex.put(keys.get(row), row);

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET notes='"+this.mysqlStrConverter(this.getNotesMap().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, notes, locusTag, product, ecNumber) " +
								"VALUES ('"+key+"','"+this.mysqlStrConverter(this.getNotesMap().get(key))+"','"+ database_locus.get(key)+"', 'null', 'null')");
					}
				}
			}

			for(Integer key : this.getSelectedGene().keySet()) {

				if(this.getSelectedGene().get(key)!=null && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

					//this.tableRowIndex.put(keys.get(row), row);

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET selected="+this.getSelectedGene().get(key)+" WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, selected, locusTag, product, ecNumber) " +
								"VALUES ('"+key+"',"+this.getSelectedGene().get(key)+",'"+ database_locus.get(key)+"', 'null', 'null')");
					}
				}
			}

			for(Integer key : this.getProductList().keySet()) {

				if(this.getProductList().get(key)!=null && !this.getProductList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET product='"+MySQL_Utilities.mysqlStrConverter(this.getProductList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, product, locusTag, ecNumber) " +
								"VALUES ('"+key+"', '"+this.mysqlStrConverter(this.getProductList().get(key))+"','"+ database_locus.get(key)+"', 'null')");
					}
				}
			}

			for(Integer key : this.getEditedProductData().keySet()) {

				if(this.getEditedProductData().get(key)!=null && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");
					String s_key;

					if(rs.next()) {

						s_key = rs.getString(1);
						stmt.execute("UPDATE homologyData SET product='"+MySQL_Utilities.mysqlStrConverter(this.getProductList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, product, locusTag, ecNumber) " +
								"VALUES ('"+key+"', '"+this.mysqlStrConverter(this.getProductList().get(key))+"','"+ database_locus.get(key)+"', 'null')");

						rs=stmt.executeQuery("SELECT last_insert_id()");
						rs.next();
						s_key = rs.getString(1);
					}

					rs = stmt.executeQuery("SELECT * FROM productList WHERE homologyData_s_key=\'"+s_key+"\'");

					if(rs.next()) {
						stmt.execute("DELETE FROM productList WHERE homologyData_s_key=\'"+s_key+"\'");
					}

					String [] products = this.getEditedProductData().get(key);

					for(String product : products) {

						stmt.execute("INSERT INTO productList (homologyData_s_key, otherNames) VALUES ('"+s_key+"',\'"+MySQL_Utilities.mysqlStrConverter(product)+"')");			
					}
				}
			}

			for(Integer key : this.getEnzymesList().keySet()) {

				if(this.getEnzymesList().get(key)!=null && !this.getEnzymesList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");

					if(rs.next()) {

						stmt.execute("UPDATE homologyData SET ecNumber='"+MySQL_Utilities.mysqlStrConverter(this.getEnzymesList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, ecNumber, locusTag, product) " +
								"VALUES ('"+key+"', '"+this.mysqlStrConverter(this.getEnzymesList().get(key))+"','"+ database_locus.get(key)+"', 'null')");
					}
				}
			}

			for(Integer key : this.getEditedEnzymeData().keySet()) {

				if(this.getEditedEnzymeData().get(key)!=null && database_locus.containsKey(key)) {

					rs = stmt.executeQuery("SELECT * FROM homologyData WHERE geneHomology_s_key='"+key+"'");
					String s_key;

					if(rs.next()) {
						s_key = rs.getString(1);
						stmt.execute("UPDATE homologyData SET ecNumber='"+MySQL_Utilities.mysqlStrConverter(this.getEnzymesList().get(key))+"' WHERE geneHomology_s_key='"+key+"' ;");
					}
					else {

						stmt.execute("INSERT INTO homologyData (geneHomology_s_key, ecNumber, locusTag, product) " +
								"VALUES ('"+key+"', '"+this.mysqlStrConverter(this.getEnzymesList().get(key))+"','"+ database_locus.get(key)+"', 'null')");

						rs=stmt.executeQuery("SELECT last_insert_id()");
						rs.next();
						s_key = rs.getString(1);
					}


					rs = stmt.executeQuery("SELECT * FROM ecNumberList WHERE homologyData_s_key=\'"+s_key+"\'");

					if(rs.next()) {
						stmt.execute("DELETE FROM ecNumberList WHERE homologyData_s_key=\'"+s_key+"\'");
					}

					String [] ecs = this.getEditedEnzymeData().get(key);

					for(String ec : ecs) {

						stmt.execute("INSERT INTO ecNumberList (homologyData_s_key, otherECNumbers) VALUES ('"+s_key+"',\'"+ec+"')");					
					}
				}
			}

			this.hasCommittedData = true;
			this.setEditedProductData(new TreeMap<Integer, String[]>());
			this.setEditedEnzymeData(new TreeMap<Integer, String[]>());
			this.setLocusList(new TreeMap<Integer, String>());
			this.setNamesList(new TreeMap<Integer, String>());
			this.setSelectedGene(new TreeMap<Integer, Boolean>());
			this.setChromosome(new TreeMap<Integer, String>());
			this.setNotesMap(new TreeMap<Integer, String>());
			this.setProductList(new TreeMap<Integer, String>());
			this.setEnzymesList(new TreeMap<Integer, String>());

			if(this.getProject().getFileName()!=null && !this.getProject().getFileName().isEmpty()) {

				SaveProject saveProject = new SaveProject();
				saveProject.saveProject(this.getProject());
				saveProject.save(new File(this.getProject().getFileName().replace("\\", "/")));
			}

		} catch (Exception e) {

			result = false;
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * @param stmt
	 * @param ecRank
	 * @param ecName
	 * @param format
	 * @return
	 */
	public Map<String,List<String>> getECRank(Statement stmt, Map<String,String> ecRank,Map<String,String> ecName, DecimalFormat format){//, double alpha){
		Map<String,List<String>> ecKeys = new TreeMap<String, List<String>>();
		try
		{
			//rs = stmt.executeQuery("SELECT s_key,geneHomology_s_key,ecnumber,rank  FROM ecnumberrank");
			ResultSet rs = stmt.executeQuery("SELECT ecNumberRank.s_key, geneHomology_s_key, ecNumber, rank, organism, taxRank FROM ecNumberRank " +
					"LEFT JOIN ecNumberRank_has_organism ON(ecNumberRank_s_key=ecNumberRank.s_key) " +
					"LEFT JOIN organism ON (organism.s_key=organism_s_key)");
			BlastScorer bs = new BlastScorer();			
			Map<String,List<Integer>> orgRank = new TreeMap<String,List<Integer>>();
			Map<String, String> ecNumberRank = new TreeMap<String, String>();

			while(rs.next())
			{	
				//number of eckeys for each gene

				if(ecKeys.containsKey(rs.getString(2)))
				{
					Set<String> s_key = new TreeSet<String>(ecKeys.get(rs.getString(2)));
					s_key.add(rs.getString(1));
					ecKeys.put(rs.getString(2),new ArrayList<String>(s_key));
				}
				else
				{
					Set<String> s_key = new TreeSet<String>();
					s_key.add(rs.getString(1));
					ecKeys.put(rs.getString(2),new ArrayList<String>(s_key));
				}
				// organism rank for each ecnumber
				if(orgRank.containsKey(rs.getString(1)))
				{
					List<Integer> orgTax = orgRank.get(rs.getString(1));
					orgTax.add(rs.getInt(6));
					orgRank.put(rs.getString(1), orgTax);
				}
				else
				{
					List<Integer> orgTax = new ArrayList<Integer>();
					orgTax.add(rs.getInt(6));
					orgRank.put(rs.getString(1), orgTax);
				}

				ecNumberRank.put(rs.getString(1), rs.getString(4));
				ecName.put(rs.getString(1), rs.getString(3));
				//ecRank.put(rs.getString(1), rs.getString(4));
			}
			int maxRank=0;
			rs = stmt.executeQuery("SELECT MAX(taxRank) FROM organism;");
			//while(rs.next()){maxRank=rs.getInt(1));}
			String thisRank =null;
			while(rs.next()){if((thisRank = rs.getString(1))!=null){maxRank=Integer.parseInt(thisRank);}}

			Map<String,Double> homologuesCount = new TreeMap<String, Double>();
			rs = stmt.executeQuery("SELECT  ecNumberRank.s_key, count(geneHomology_has_homologues.homologues_s_key) " +
					"FROM geneHomology_has_homologues " +
					"JOIN ecNumberRank ON (ecNumberRank.geneHomology_s_key=geneHomology_has_homologues.geneHomology_s_key) " +
					"GROUP BY ecNumberRank.s_key;");
			while(rs.next()){homologuesCount.put(rs.getString(1), Double.parseDouble(rs.getString(2)));}

			for(String key : orgRank.keySet())
			{	
				bs = new BlastScorer();
				bs.setS1(Double.parseDouble(ecNumberRank.get(key))/homologuesCount.get(key));
				bs.setS2(orgRank.get(key),maxRank);
				bs.setAlpha(this.alpha);
				bs.setBeta(this.beta);
				bs.setMinimumNumberofHits(this.minimumNumberofHits);

				Map<String, String> s1 = this.getScore1();
				s1.put(key,format.format(bs.getS1()));
				this.setScore1(s1);

				Map<String, String> s2 = this.getScore2();
				s2.put(key, format.format(bs.taxonomyAverage()));
				this.setScore2(s2);

				Map<String, String> s = this.getScore();
				s.put(key, format.format(bs.getS()));
				this.setScore(s);

				ecRank.put(key, bs.getS()+"");
			}
		}
		catch (Exception e) {e.printStackTrace();}
		return ecKeys;
	}

	/**
	 * @param stmt
	 * @param prodRank
	 * @param prodName
	 * @param format
	 * @return
	 */
	public Map<String,List<String>> getProductRank(Statement stmt, Map<String,String> prodRank,Map<String,String> prodName, DecimalFormat format){//, double alpha) {
		Map<String,List<String>> prodKeys = new TreeMap<String, List<String>>();

		try {

			//rs = stmt.executeQuery("SELECT s_key,geneHomology_s_key,ecnumber,rank  FROM ecnumberrank");
			ResultSet rs = stmt.executeQuery("SELECT productRank.s_key, geneHomology_s_key, productName, rank, organism, taxRank " +
					"FROM productRank " +
					"LEFT JOIN productRank_has_organism ON(productRank_s_key=productRank.s_key) " +
					"LEFT JOIN organism ON(organism.s_key=organism_s_key)");
			BlastScorer bs = new BlastScorer();			
			Map<String,List<Integer>> orgRank = new TreeMap<String,List<Integer>>();
			Map<String, String> productRank = new TreeMap<String, String>();

			while(rs.next()) {

				//number of productKeys for each gene
				if(prodKeys.containsKey(rs.getString(2))) {

					Set<String> s_key = new TreeSet<String>(prodKeys.get(rs.getString(2)));
					s_key.add(rs.getString(1));
					prodKeys.put(rs.getString(2),new ArrayList<String>(s_key));
				}						
				else {

					Set<String> s_key = new TreeSet<String>();
					s_key.add(rs.getString(1));
					prodKeys.put(rs.getString(2),new ArrayList<String>(s_key));
				}

				// organism rank for each product
				if(orgRank.containsKey(rs.getString(1))) {

					List<Integer> orgTax = orgRank.get(rs.getString(1));
					orgTax.add(rs.getInt(6));
					orgRank.put(rs.getString(1), orgTax);
				}
				else {

					List<Integer> orgTax = new ArrayList<Integer>();
					orgTax.add(rs.getInt(6));
					orgRank.put(rs.getString(1), orgTax);
				}

				productRank.put(rs.getString(1), rs.getString(4));
				prodName.put(rs.getString(1), rs.getString(3));
			}

			int maxRank=0;
			rs = stmt.executeQuery("SELECT MAX(taxRank) FROM organism;");
			//while(rs.next()){maxRank=rs.getInt(1));}
			String thisRank =null;
			while(rs.next()){if((thisRank = rs.getString(1))!=null){maxRank=Integer.parseInt(thisRank);}}

			Map<String,Double> homologuesCount = new TreeMap<String, Double>();
			rs = stmt.executeQuery("SELECT  productRank.s_key, count(geneHomology_has_homologues.homologues_s_key) " +
					"FROM geneHomology_has_homologues " +
					"JOIN productRank ON (productRank.geneHomology_s_key=geneHomology_has_homologues.geneHomology_s_key) " +
					"GROUP BY productRank.s_key;");
			while(rs.next()){homologuesCount.put(rs.getString(1), Double.parseDouble(rs.getString(2)));}

			for(String key : orgRank.keySet()) {

				bs = new BlastScorer();
				bs.setS1(Double.parseDouble(productRank.get(key))/homologuesCount.get(key));
				bs.setS2(orgRank.get(key),maxRank);
				bs.setAlpha(this.alpha);
				bs.setBeta(this.beta);
				bs.setMinimumNumberofHits(this.minimumNumberofHits);

				Map<String, String> s1 = this.getScoreP1();
				if(s1==null){s1 = new TreeMap<String, String>();}
				s1.put(key,format.format(bs.getS1()));
				this.setScoreP1(s1);

				Map<String, String> s2 = this.getScoreP2();
				if(s2==null){s2 = new TreeMap<String, String>();}
				s2.put(key, format.format(bs.taxonomyAverage()));
				this.setScoreP2(s2);

				Map<String, String> s = this.getScoreP();
				if(s==null){s = new TreeMap<String, String>();}
				s.put(key, format.format(bs.getS()));
				this.setScoreP(s);

				prodRank.put(key, bs.getS()+"");
			}
		}
		catch (Exception e) {e.printStackTrace();}
		return prodKeys;
	}

	/**
	 * @param stmt
	 * @param prodRank
	 * @param prodName
	 * @return
	 */
	public Map<String,List<String>> getProductRank(Statement stmt, Map<String,String> prodRank,Map<String,String> prodName) {
		Map<String,List<String>> prodKeys = new TreeMap<String,List<String>>();
		try
		{
			ResultSet rs = stmt.executeQuery("SELECT s_key, geneHomology_s_key, productName, rank  FROM productRank");
			String gene = "";
			while(rs.next())
			{
				if(rs.getString(2).equals(gene))
				{
					List<String> s_key=prodKeys.get(gene);
					s_key.add(rs.getString(1));
					prodKeys.put(gene,s_key);
				}
				else
				{
					gene = rs.getString(2);
					List<String> s_key = new ArrayList<String>();
					s_key.add(rs.getString(1));
					prodKeys.put(rs.getString(2), s_key);		
				}
				prodName.put(rs.getString(1), rs.getString(3));
				prodRank.put(rs.getString(1), rs.getString(4));
			}

		}
		catch (Exception e) {e.printStackTrace();}
		return prodKeys;
	}

	/**
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
	public Map<Integer,List<Object>> getGeneInformation(Statement stmt) throws SQLException{

		this.hmmerGeneDataEntries = new  TreeMap<String, Integer>();
		this.blastPGeneDataEntries = new  TreeMap<Integer, String>();
		this.initialProdItem = new TreeMap<Integer,String>();
		this.initialEcItem = new TreeMap<Integer,String>();
		this.initialChromosome = new TreeMap<Integer,String>();
		this.initialSelectedGene = new TreeMap<Integer,Boolean>();
		this.initialLocus = new TreeMap<Integer, String>();
		this.initialNames = new TreeMap<Integer, String>();
		//this.namesList = new TreeMap<Integer, String>(); limpa nomes gravados no objeto
		this.tableRowIndex = new TreeMap<String, Integer>();


		TreeMap<Integer,List<Object>> geneData = new TreeMap<Integer,List<Object>>();
		//ResultSet rs = stmt.executeQuery("SELECT geneHomology.s_key, locusTag, gene, chromosome, organelle FROM geneHomology order by geneHomology.s_key;");
		ResultSet rs = stmt.executeQuery("SELECT geneHomology.s_key, locusTag, gene, chromosome, organelle, uniprot_star, program, query" +
				" FROM geneHomology" +
				" LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
				" WHERE status = 'PROCESSED' OR status = 'NO_SIMILARITY' " +
				" ORDER BY locusTag, status DESC;");

		int tableIndex = 0;
		while(rs.next()) {

			List<Object> ql = new ArrayList<Object>();
			ql.add("");

			ql.add(rs.getString(2));
			ql.add(rs.getString(6));
			ql.add(rs.getString(3));

			//			String a = rs.getString(4);
			//			if(a==null || a.equals("null"))
			//			{
			//				ql.add(rs.getString(5));
			//				this.chromosome.put(tableIndex, rs.getString(5));
			//			}
			//			else
			//			{
			//				ql.add(a);
			//				this.chromosome.put(tableIndex, a);
			//			}



			if( (this.blastPAvailable && (rs.getString(7).equalsIgnoreCase("ncbi-blastp") || rs.getString(7).equalsIgnoreCase("blastp"))) ||
					(this.stats_blastXAvailable && (rs.getString(7).equalsIgnoreCase("ncbi-blastx") || rs.getString(7).equalsIgnoreCase("blastx"))) ||
					(this.hmmerAvailable && rs.getString(7).equalsIgnoreCase("hmmer"))) {

				geneData.put(tableIndex, ql);
				this.keys.put(tableIndex, rs.getString(1));//genes list
				this.tableRowIndex.put(rs.getString(1),tableIndex);
				this.reverseKeys.put(rs.getInt(1), tableIndex);//reverse genes list


				if(this.isEukaryote) {

					ql.add(rs.getString(4));
					this.initialChromosome.put(tableIndex, rs.getString(4));
				}
				if(rs.getString(7).equalsIgnoreCase("hmmer")) {

					this.hmmerGeneDataEntries.put(rs.getString(8), tableIndex);
				}

				if(rs.getString(7).equalsIgnoreCase("ncbi-blastp") || rs.getString(7).equalsIgnoreCase("blastp")) {

					this.blastPGeneDataEntries.put(tableIndex, rs.getString(8));
				}
				this.initialLocus.put(tableIndex, rs.getString(2));
				this.initialNames.put(tableIndex, rs.getString(3));
				tableIndex++;
			}

		}

		return geneData;
	}

	/**
	 * @param index
	 * @param dataList
	 * @param prodKeys
	 * @param prodRank
	 * @param prodName
	 * @param format
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> processProductNamesData(int index, List<Object> dataList, Map<String,List<String>> prodKeys, Map<String,String> prodRank, Map<String,String> prodName,DecimalFormat format){
		int j;
		double rankP;
		//double total = 0;
		String[][] prod = new String[2][0];
		Pair<String,Double> products[] = null;
		this.product[Integer.parseInt(keys.get(index))]= new String[0];
		this.prodPercent[Integer.parseInt(keys.get(index))] = new String[0];

		if(prodKeys.containsKey(this.keys.get(index))) {

			this.product[Integer.parseInt(keys.get(index))] = new String[prodKeys.get(this.keys.get(index)).size()];
			this.prodPercent[Integer.parseInt(keys.get(index))] = new String[prodKeys.get(this.keys.get(index)).size()];
			products = new Pair[prodKeys.get(this.keys.get(index)).size()];
			j=0;

			while(j<prodKeys.get(this.keys.get(index)).size()) {

				Double score = Double.parseDouble(prodRank.get(prodKeys.get(this.keys.get(index)).get(j)));
				products[j]=new Pair<String, Double>(prodName.get(prodKeys.get(this.keys.get(index)).get(j)), score);
				j++;
			}

			Arrays.sort(products, new PairComparator<Double>());

			prod = new String[2][prodKeys.get(this.keys.get(index)).size()+1];
			this.product[Integer.parseInt(keys.get(index))]=
					new String[prodKeys.get(this.keys.get(index)).size()+1];
			this.prodPercent[Integer.parseInt(keys.get(index))]=
					new String[prodKeys.get(this.keys.get(index)).size()+1];
			this.product[Integer.parseInt(keys.get(index))][0]="";
			this.prodPercent[Integer.parseInt(keys.get(index))][0]="";
			prod[0][0]="";
			prod[1][0]="";
			j = 0;

			while (j < prodKeys.get(this.keys.get(index)).size()) {

				Double score = Double.parseDouble(prodRank.get(prodKeys.get(this.keys.get(index)).get(j)));
				prod[0][j+1] = products[j].getA();
				this.product[Integer.parseInt(keys.get(index))][j+1]=prod[0][j+1];
				if(score>0) {

					rankP = products[j].getB();
					prod[1][j+1]= format.format(rankP);
				}
				else {

					prod[1][j+1]="manual";
					this.product[Integer.parseInt(keys.get(index))][j+1]=prod[0][j+1];
				}
				this.prodPercent[Integer.parseInt(keys.get(index))][j+1] = prod[1][j+1];
				j++;
			}

			if(prod[0].length>0) {

				dataList.add(prod[0]);
			}
			else {

				dataList.add(new String[0]);
			}

			if(prod[1].length>0) {

				dataList.add(prod[1][1]);
			}
			else {

				dataList.add(prod[1][0]);
			}

			this.initialProdItem.put(index, prod[0][1]);
		}
		else
		{
			dataList.add(new String[0]);
			dataList.add("");
		}
		return dataList;
	}

	/**
	 * @param index
	 * @param dataList
	 * @param ecKeys
	 * @param ecRank
	 * @param ecName
	 * @param format
	 * @param thresholdBool
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> processECNumberData(int index, List<Object> dataList, Map<String,List<String>> ecKeys, Map<String,String> ecRank, Map<String,String> ecName,DecimalFormat format, boolean thresholdBool){
		int j;
		double rankP;
		//double total = 0;
		String[][] ecn = new String[2][0];
		Pair<String,Double> ecnumber[] = null;

		this.enzyme[Integer.parseInt(keys.get(index))]= new String[0];
		this.ecnPercent[Integer.parseInt(keys.get(index))] = new String[0];

		if (ecKeys.containsKey(this.keys.get(index))) {

			//Pair<String,Integer> ecnumber[] = new Pair[ecKeys.get(this.keys.get(i)).size()];
			ecnumber = new Pair[ecKeys.get(this.keys.get(index)).size()];
			j=0;

			while(j<ecKeys.get(this.keys.get(index)).size()) {

				Double score = Double.parseDouble(ecRank.get(ecKeys.get(this.keys.get(index)).get(j)));
				//ecnumber[j]=new Pair<String, Integer>(ecName.get(ecKeys.get(this.keys.get(i)).get(j)),Integer.parseInt(ecRank.get(ecKeys.get(this.keys.get(i)).get(j))));
				ecnumber[j]=new Pair<String, Double>(ecName.get(ecKeys.get(this.keys.get(index)).get(j)), score);
				//total += Integer.parseInt(ecRank.get(ecKeys.get(this.keys.get(i)).get(j)));
				//total += score;
				j++;
			}

			Arrays.sort(ecnumber, new PairComparator<Double>());

			ecn = new String[2][ecKeys.get(this.keys.get(index)).size()+1];
			this.enzyme[Integer.parseInt(keys.get(index))]=
					new String[ecKeys.get(this.keys.get(index)).size()+1];
			this.ecnPercent[Integer.parseInt(keys.get(index))]=
					new String[ecKeys.get(this.keys.get(index)).size()+1];
			this.enzyme[Integer.parseInt(keys.get(index))][0]="";
			this.ecnPercent[Integer.parseInt(keys.get(index))][0]="";
			ecn[0][0]="";
			ecn[1][0]="";
			j = 0;

			while (j < ecKeys.get(this.keys.get(index)).size()) {

				Double score = Double.parseDouble(ecRank.get(ecKeys.get(this.keys.get(index)).get(j)));
				ecn[0][j+1] = ecnumber[j].getA();
				this.enzyme[Integer.parseInt(keys.get(index))][j+1]=ecn[0][j+1];
				if(score>0) {					
					rankP = ecnumber[j].getB();
					ecn[1][j+1]= format.format(rankP);
				}
				else {
					ecn[1][j+1]="manual";
				}
				this.ecnPercent[Integer.parseInt(keys.get(index))][j+1] = ecn[1][j+1];
				j++;
			}

			if(ecn[1].length>0 && ecnumber[0].getB()>=(new Double(this.threshold)))	{
				this.initialEcItem.put(index, ecn[0][1]);
			}
		}

		dataList.add(ecn[0]);
		boolean selected;
		String ec_score = "";
		String note = "";

		if(ecn[1].length>0 && ecnumber[0].getB()>=(new Double(this.threshold))) {

			ec_score = ecn[1][1];
			selected = new Boolean(true);
		}
		else {

			if(ecn[1].length>0) {

				ec_score = "<"+this.threshold;
			}
			selected = new Boolean(false);
		}

		dataList.add(ec_score);
		dataList.add(note);
		dataList.add(selected);
		this.initialSelectedGene.put(index, selected);

		return dataList;
	}


	/**
	 * @return
	 * @throws SQLException
	 */
	public boolean setIsEukaryote() {

		Statement stmt;
		try  {

			stmt = this.connection.createStatement();

			ResultSet rset = stmt.executeQuery("SELECT taxonomy FROM organism;");

			if(rset.next()) {

				if(rset.getString(1).startsWith("Eukaryota")) {

					return true;
				}
			}
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}


	/**
	 * @param chromosome
	 */
	public void setChromosome(Map<Integer, String> chromosome) {
		this.chromosome = chromosome;
	}

	/**
	 * @param dataSet
	 * @return
	 */
	public int getArraySize(Collection <String> dataSet){
		int max = 0;
		for(String key: dataSet)
		{
			if(Integer.parseInt(key)>max) {
				max=Integer.parseInt(key);
			}
		}
		return max+1;
	}



	/**
	 * @return the initial locus Tag list
	 */
	public Map<Integer, String> getInitialLocus() {
		return initialLocus;
	}

	/**
	 * @return the initial names List
	 */
	public Map<Integer, String> getInitialNames() {
		return initialNames;
	}

	//		/**
	//		 * @return the initial selected product (after the pair comparator selection)
	//		 */
	//		public Map<Integer, String[]> getInitialProduct() {
	//			return initialProduct;
	//		}
	//
	//		/**
	//		 * @return the initial selected enzyme (after the pair comparator selection)
	//		 */
	//		public Map<Integer, String> getInitialEnzyme() {
	//			return initialEnzyme;
	//		}

	/**
	 * @return
	 */
	public Map<Integer, String> getChromosome() {
		return chromosome;
	}

	/**
	 * @param score1 the score1 to set
	 */
	public void setScore1(Map<String, String> score1) {
		this.score1 = score1;
	}

	/**
	 * @return the score1
	 */
	public Map<String, String> getScore1() {
		return score1;
	}

	/**
	 * @param score2 the score2 to set
	 */
	public void setScore2(Map<String, String> score2) {
		this.score2 = score2;
	}

	/**
	 * @return the score2
	 */
	public Map<String, String> getScore2() {
		return score2;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(Map<String, String> score) {
		this.score = score;
	}

	/**
	 * @return the score
	 */
	public Map<String, String> getScore() {
		return score;
	}

	/**
	 * @param score1 the score1 to set
	 */
	public void setScoreP1(Map<String, String> scoreP1) {
		this.scoreP1 = scoreP1;
	}

	/**
	 * @return the score1
	 */
	public Map<String, String> getScoreP1() {
		return scoreP1;
	}

	/**
	 * @param score2 the score2 to set
	 */
	public void setScoreP2(Map<String, String> scoreP2) {
		this.scoreP2 = scoreP2;
	}

	/**
	 * @return the score2
	 */
	public Map<String, String> getScoreP2() {
		return scoreP2;
	}

	/**
	 * @param score the score to set
	 */
	public void setScoreP(Map<String, String> scoreP) {
		this.scoreP = scoreP;
	}

	/**
	 * @return the score
	 */
	public Map<String, String> getScoreP() {
		return scoreP;
	}

	/**
	 * @return the alpha
	 */
	public Double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(Double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @param sequence
	 * @return
	 */
	private String setSequenceView(String sequence){
		String seq = new String();
		for(int i=0;i<sequence.toCharArray().length;i++)
		{
			if(i!=0 && (i%70)==0){seq+="\n";}
			seq+=sequence.charAt(i);			
		}
		return seq;
	}

	/**
	 * @return
	 */
	public int getSelectedRow() {
		return selectedRow;
	}

	/**
	 * @param selectedRow
	 */
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}

	/**
	 * @param input
	 * @return
	 */
	private String mysqlStrConverter(String input){
		if(input== null) {
			return input;
		}
		return input.replace("\\'","'").replace("-","\\-").replace("'","\\'").replace("[","\\[").replace("]","\\]");
	}

	/**
	 * @return a list of user selected products
	 */
	public Map<Integer, String> getProductList() {
		return prodItem;
	}

	/**
	 * @return a list of user selected ec numbers
	 */
	public Map<Integer, String> getEnzymesList() {

		return ecItem;
	}

	/**
	 * @return a list of user selected gene names
	 */
	public Map<Integer, String> getNamesList() {

		return namesList ;
	}

	/**
	 * @param prodItem
	 * 
	 * Sets a list of user selected products ec numbers
	 * 
	 */
	public void setProductList(Map<Integer, String> prodItem) {
		this.prodItem = prodItem;		
	}


	/**
	 * @param ecItem
	 * 
	 * Sets a list of user selected ec numbers
	 * 
	 */
	public void setEnzymesList(Map<Integer, String> ecItem) {
		this.ecItem = ecItem;

	}

	/**
	 * @param namesList
	 * 
	 * Sets a list of gene names
	 * 
	 */
	public void setNamesList(Map<Integer, String> namesList) {
		this.namesList = namesList ;
	}


	/**
	 * @param locusList
	 * 
	 * Sets a list of user selected gene Locus Tags
	 * 
	 */
	public void setLocusList(Map<Integer, String> locusList) {
		this.locusList=locusList;

	}

	/**
	 * @return a list of user selected locus Tags
	 */
	public Map<Integer, String> getLocusList() {
		return locusList;
	}

	/**
	 * @return the notesMap
	 */
	public Map<Integer, String> getNotesMap() {
		return notesMap;
	}

	/**
	 * @param notesMap the notesMap to set
	 */
	public void setNotesMap(Map<Integer, String> notesMap) {
		this.notesMap = notesMap;
	}

	/**
	 * @return the isEukaryote
	 */
	public boolean isEukaryote() {
		return isEukaryote;
	}

	/**
	 * @param isEukaryote the isEukaryote to set
	 */
	public void setEukaryote(boolean isEukaryote) {
		this.isEukaryote = isEukaryote;
	}

	/**
	 * @return the threshold
	 */
	public Double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the beta
	 */
	public Double getBeta() {
		return beta;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(Double beta) {
		this.beta = beta;
	}

	/**
	 * @return the minimumNumberofHits
	 */
	public int getMinimumNumberofHits() {
		return minimumNumberofHits;
	}

	/**
	 * @param minimumNumberofHits the minimumNumberofHits to set
	 */
	public void setMinimumNumberofHits(int minimumNumberofHits) {
		this.minimumNumberofHits = minimumNumberofHits;
	}

	/**
	 * @return the blastHmmerWeight
	 */
	public Double getBlastHmmerWeight() {

		return blastHmmerWeight;
	}

	/**
	 * @param blastHmmerWeight the blastHmmerWeight to set
	 */
	public void setBlastHmmerWeight(Double blastHmmerWeight) {

		this.blastHmmerWeight = blastHmmerWeight;
	}

	/**
	 * @return the blastPAvailable
	 */
	public boolean isBlastPAvailable() {
		return blastPAvailable;
	}

	/**
	 * @param blastPAvailable the blastPAvailable to set
	 */
	public void setBlastPAvailable(boolean blastPAvailable) {
		this.blastPAvailable = blastPAvailable;
	}

	/**
	 * @return the blastXAvailable
	 */
	public boolean isBlastXAvailable() {
		return stats_blastXAvailable;
	}

	/**
	 * @param blastXAvailable the blastNAvailable to set
	 */
	public void setBlastXAvailable(boolean blastXAvailable) {
		this.stats_blastXAvailable = blastXAvailable;
	}

	/**
	 * @return the blastPGeneDataEntries
	 */
	public Map<Integer, String> getBlastPGeneDataEntries() {
		return blastPGeneDataEntries;
	}

	/**
	 * @param blastPGeneDataEntries the blastPGeneDataEntries to set
	 */
	public void setBlastPGeneDataEntries(Map<Integer, String> blastPGeneDataEntries) {
		this.blastPGeneDataEntries = blastPGeneDataEntries;
	}

	/**
	 * @return the hmmerAvailable
	 */
	public boolean isHmmerAvailable() {
		return hmmerAvailable;
	}

	/**
	 * @param hmmerAvailable the hmmerAvailable to set
	 */
	public void setHmmerAvailable(boolean hmmerAvailable) {
		this.hmmerAvailable = hmmerAvailable;
	}

	/**
	 * @return the committedProdItem
	 */
	public Map<Integer, String> getCommittedProdItem() {
		return committedProdItem;
	}

	/**
	 * @param committedProdItem the committedProdItem to set
	 */
	public void setCommittedProdItem(Map<Integer, String> committedProdItem) {
		this.committedProdItem = committedProdItem;
	}

	/**
	 * @return the committedEcItem
	 */
	public Map<Integer, String> getCommittedEcItem() {
		return committedEcItem;
	}

	/**
	 * @param committedEcItem the committedEcItem to set
	 */
	public void setCommittedEcItem(Map<Integer, String> committedEcItem) {
		this.committedEcItem = committedEcItem;
	}

	/**
	 * @return the committedNamesList
	 */
	public Map<Integer, String> getCommittedNamesList() {
		return committedNamesList;
	}

	/**
	 * @param committedNamesList the committedNamesList to set
	 */
	public void setCommittedNamesList(Map<Integer, String> committedNamesList) {
		this.committedNamesList = committedNamesList;
	}

	/**
	 * @return the committedLocusList
	 */
	public Map<Integer, String> getCommittedLocusList() {
		return committedLocusList;
	}

	/**
	 * @param committedLocusList the committedLocusList to set
	 */
	public void setCommittedLocusList(Map<Integer, String> committedLocusList) {
		this.committedLocusList = committedLocusList;
	}

	/**
	 * @return the committedChromosome
	 */
	public Map<Integer, String> getCommittedChromosome() {
		return committedChromosome;
	}

	/**
	 * @param committedChromosome the committedChromosome to set
	 */
	public void setCommittedChromosome(Map<Integer, String> committedChromosome) {
		this.committedChromosome = committedChromosome;
	}

	/**
	 * @return the committedNotesMap
	 */
	public Map<Integer, String> getCommittedNotesMap() {
		return committedNotesMap;
	}

	/**
	 * @param committedNotesMap the committedNotesMap to set
	 */
	public void setCommittedNotesMap(Map<Integer, String> committedNotesMap) {
		this.committedNotesMap = committedNotesMap;
	}

	/**
	 * @return the committedSelected
	 */
	public Map<Integer, Boolean> getCommittedSelected() {
		return committedSelected;
	}

	/**
	 * @param committedSelected the committedSelected to set
	 */
	public void setCommittedSelected(Map<Integer, Boolean> committedSelected) {
		this.committedSelected = committedSelected;
	}

	/**
	 * @return
	 */
	public boolean hasCommittedData() {
		return hasCommittedData;
	}

	/**
	 * @return the committedProductList
	 */
	public Map<Integer, String[]> getCommittedProductList() {
		return committedProductList;
	}

	/**
	 * @param committedProductList the committedProductList to set
	 */
	public void setCommittedProductList(Map<Integer, String[]> committedProductList) {
		this.committedProductList = committedProductList;
	}

	/**
	 * @return the committedEnzymeList
	 */
	public Map<Integer, String[]> getCommittedEnzymeList() {
		return committedEnzymeList;
	}

	/**
	 * @param committedEnzymeList the committedEnzymeList to set
	 */
	public void setCommittedEnzymeList(Map<Integer, String[]> committedEnzymeList) {
		this.committedEnzymeList = committedEnzymeList;
	}

	/**
	 * @return the initialChromosome
	 */
	public Map<Integer, String> getInitialChromosome() {
		return initialChromosome;
	}

	/**
	 * @param initialChromosome the initialChromosome to set
	 */
	public void setInitialChromosome(Map<Integer, String> initialChromosome) {
		this.initialChromosome = initialChromosome;
	}

	/**
	 * @return the initialProdItem
	 */
	public Map<Integer, String> getInitialProdItem() {
		return initialProdItem;
	}

	/**
	 * @param initialProdItem the initialProdItem to set
	 */
	public void setInitialProdItem(Map<Integer, String> initialProdItem) {
		this.initialProdItem = initialProdItem;
	}

	/**
	 * @return the initialEcItem
	 */
	public Map<Integer, String> getInitialEcItem() {

		return initialEcItem;
	}

	/**
	 * @param initialEcItem the initialEcItem to set
	 */
	public void setInitialEcItem(Map<Integer, String> initialEcItem) {

		this.initialEcItem = initialEcItem;
	}

	/**
	 * @return the initialSelectedGene
	 */
	public Map<Integer, Boolean> getInitialSelectedGene() {
		return initialSelectedGene;
	}

	/**
	 * @param initialSelectedGene the initialSelectedGene to set
	 */
	public void setInitialSelectedGene(Map<Integer, Boolean> initialSelectedGene) {
		this.initialSelectedGene = initialSelectedGene;
	}

	/**
	 * @return the keys
	 */
	public Map<Integer, String> getKeys() {
		return keys;
	}

	/**
	 * @param keys the keys to set
	 */
	public void setKeys(Map<Integer, String> keys) {
		this.keys = keys;
	}

	/**
	 * @param mappedLocusList
	 */
	public void setIntegrationLocusList(Map<Integer, String> mappedLocusList) {

		this.integrationLocusList = mappedLocusList;
	}

	/**
	 * @param mappedNamesList
	 */
	public void setIntegrationNamesList(Map<Integer, String> mappedNamesList) {

		this.integrationNamesList = mappedNamesList;
	}

	/**
	 * @param mappedProdItem
	 */
	public void setIntegrationProdItem(Map<Integer, String> mappedProdItem) {

		this.integrationProdItem = mappedProdItem;
	}

	/**
	 * @param mappedEcItem
	 */
	public void setIntegrationEcItem(Map<Integer, String> mappedEcItem) {

		this.integrationEcItem = mappedEcItem;
	}

	/**
	 * @return the integrationProdItem
	 */
	public void setIntegrationChromosome(Map<Integer, String> integrationChromosome) {

		this.integrationChromosome = integrationChromosome;
	}

	/**
	 * @param mappedSelectedGene
	 */
	public void setIntegrationSelectedGene(Map<Integer, Boolean> mappedSelectedGene) {

		this.integrationSelectedGene = mappedSelectedGene;
	}

	/**
	 * @return the integrationLocusList
	 */
	public Map<Integer, String> getIntegrationLocusList() {
		return integrationLocusList;
	}

	/**
	 * @return the integrationNamesList
	 */
	public Map<Integer, String> getIntegrationNamesList() {
		return integrationNamesList;
	}

	/**
	 * @return the integrationProdItem
	 */
	public Map<Integer, String> getIntegrationProdItem() {
		return integrationProdItem;
	}

	/**
	 * @return the integrationNamesList
	 */
	public Map<Integer, String> getIntegrationChromosome() {
		return integrationChromosome;
	}

	/**
	 * @return the integrationEcItem
	 */
	public Map<Integer, String> getIntegrationEcItem() {
		return integrationEcItem;
	}

	/**
	 * @return the integrationSelectedGene
	 */
	public Map<Integer, Boolean> getIntegrationSelectedGene() {
		return integrationSelectedGene;
	}

	/**
	 * @return the reverseKeys
	 */
	public Map<Integer, Integer> getReverseKeys() {
		return reverseKeys;
	}

	/**
	 * @param reverseKeys the reverseKeys to set
	 */
	public void setReverseKeys(Map<Integer, Integer> reverseKeys) {
		this.reverseKeys = reverseKeys;
	}



	/**
	public void commitToDatabase() {

		if(this.dsa==null)
		{
			this.dsa=super.getDbt().getMySqlCredentials();
		}
		Statement stmt;
		Map<String,String> homologyNewEntryKey = new TreeMap<String, String>();
		ResultSet rs;
		try 
		{
			stmt = this.dsa.createStatement();
			//products
			Set<Integer> prodKeys = new TreeSet<Integer>(this.getEditedProductData().keySet());
			for(Integer row : prodKeys)
			{
				//Map<String, String> toDeleteKey = new TreeMap<String,String>();
				Set<String> toDeleteName = new TreeSet<String>();
				Set<String> existsName = new TreeSet<String>();
				existsName.add("");
				Set<String> products = new TreeSet<String>(Arrays.asList(this.getEditedProductData().get(row)));
				rs = stmt.executeQuery("SELECT homologyData.s_key, product FROM homologyData WHERE homology_geneHomology_s_key=\'"+keys.get(row)+"\'");
				while(rs.next())
				{
					String name = rs.getString(2);
					toDeleteName.add(name);
					for(String pd: products)
					{
						if(pd.trim().equals(name.trim()))
						{
							existsName.add(pd);
							//toDeleteKey.remove(rs.getString(1));
							//toDeleteName.remove(pd);
							break;
						}
						else
						{
							//toDeleteKey.put(rs.getString(1), name);

						}
					}
				}

				products.removeAll(existsName);
				toDeleteName.removeAll(existsName);
				//				for(String key:toDeleteKey.keySet())
				//				{
				//					stmt.execute("DELETE FROM homologyData where s_key = \'" + key + "\'");
				//				}
				List<String> toDeletePR = new ArrayList<String>();
				rs = stmt.executeQuery("SELECT productRank.s_key, productName FROM productRank WHERE geneHomology_s_key = \'"+keys.get(row)+ "\'");
				while(rs.next())
				{
					for(String tdn:toDeleteName)
					{
						if(tdn.equals(rs.getString(2)))
						{
							toDeletePR.add(rs.getString(1));
						}
					}
				}
				for(String key:toDeletePR)
				{
					stmt.execute("DELETE FROM productRank WHERE s_key=\'"+key+"\'" );
				}
				for(String pd: products)
				{

					rs=stmt.executeQuery("SELECT homology.s_key FROM homology WHERE geneHomology_s_key='"+keys.get(row)+"' AND organism_s_key=1 AND referenceID=\'0\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO homology (geneHomology_s_key, organism_s_key, referenceID, gene, eValue, bits) VALUES (\'"+keys.get(row)+"\',\'1\',\'0\',\'null\',\'0.0\',\'0.0\')");
						rs=stmt.executeQuery("SELECT last_insert_id()");
					}
					rs.next();
					String homology_s_key=rs.getString(1);
					homologyNewEntryKey.put(keys.get(row),homology_s_key);

					rs=stmt.executeQuery("SELECT homologyData.s_key FROM homologyData WHERE homology_geneHomology_s_key='"+keys.get(row)+"' AND homology_s_key="+homology_s_key);
					if(!rs.next())
					{	
						stmt.execute("INSERT INTO homologyData (homology_geneHomology_s_key,homology_s_key,definition,calculated_mw,product) " +
								"VALUES(\'"+keys.get(row)+"\',\'"+homology_s_key+"\',\'"+this.mysqlStrConverter(pd)+"\',0,\'"+this.mysqlStrConverter(pd)+"\');");
						stmt.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) VALUES(\'"+keys.get(row)+"\',\'"+this.mysqlStrConverter(pd)+"\', \'0\');");
					}
					else
					{
						stmt.execute("UPDATE homologyData definition=\'"+this.mysqlStrConverter(pd)+"\', product=\'"+this.mysqlStrConverter(pd)+"\' " +
								"WHERE homology_geneHomology_s_key="+keys.get(row)+" AND homology_s_key="+homology_s_key+"");
					}

					String rank="1";
					//if(this.getProductList().get(row).equals(pd)){rank="0";}
					//else{rank="1";}
					rs=stmt.executeQuery("SELECT productrank.s_key FROM productrank WHERE geneHomology_s_key='"+keys.get(row)+"' AND productName=\'"+this.mysqlStrConverter(pd)+"\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) VALUES(\'"+keys.get(row)+"\',\'"+this.mysqlStrConverter(pd)+"\',"+rank+");");
						//rs=stmt.executeQuery("SELECT last_insert_id()");
						stmt.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES(last_insert_id(),1);");
					}
					//					else
					//					{
					//						stmt.execute("UPDATE productRank SET rank="+rank+" WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND productName=\'"+this.mysqlStrConverter(pd)+"\'");
					//					}
					//					rs.next();
					//					String productRank_s_key=rs.getString(1);
					//
					//					rs=stmt.executeQuery("SELECT * FROM productRank_has_organism WHERE organism_s_key=1 AND productRank_s_key=\'"+productRank_s_key+"\'");
					//					if(rs.getFetchSize()==0)
					//					{
					//						stmt.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES("+productRank_s_key+",1);");
					//					}
				}
				this.getEditedProductData().remove(row);
			}
			for(Integer row : new TreeSet<Integer>(this.getProductList().keySet()))
			{
				stmt.execute("UPDATE productRank SET rank=0 WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND productName=\'"+this.mysqlStrConverter(this.getProductList().get(row))+"\'");
				this.getProductList().remove(row);
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//enzymes
			Set<Integer> ecKeys = new TreeSet<Integer>(this.getEditedEnzymeData().keySet());
			//int z=0;
			for(Integer row : ecKeys)
			{
				//Map<String, Set<String>> toDeleteKey = new TreeMap<String, Set<String>>();
				Set<String> toDeleteEnzyme = new TreeSet<String>();
				Set<String> existsEC = new TreeSet<String>();
				existsEC.add("");
				String previous_homology_s_key = "", ecnumber="";
				Set<String> enzymes = new TreeSet<String>(Arrays.asList(this.getEditedEnzymeData().get(row)));
				Set<String> eckeys=new TreeSet<String>();
				rs = stmt.executeQuery("SELECT homology.s_key, ecnumber.s_key ,ecnumber FROM homology " +
						"LEFT JOIN homology_has_ecnumber on (homology.s_key = homology_has_ecnumber.homology_s_key) " +
						"JOIN ecnumber on (homology_has_ecnumber.ecnumber_s_key = ecnumber.s_key) " +
						"WHERE homology.geneHomology_s_key =" + keys.get(row) + ";");
				while(rs.next())
				{
					if(previous_homology_s_key.equals(rs.getString(1)))
					{
						ecnumber+=", "+rs.getString(3);
						eckeys.add(rs.getString(2));
					}
					else
					{
						previous_homology_s_key=rs.getString(1);
						if(!ecnumber.isEmpty())
						{
							toDeleteEnzyme.add(ecnumber);
							for(String en: enzymes)
							{
								if(en.equals(ecnumber))
								{
									existsEC.add(en);
									//toDeleteKey.remove(rs.getString(1));
									//toDeleteEnzyme.remove(ecnumber);	
									break;
								}
								else
								{
									//toDeleteKey.put(rs.getString(1),eckeys);
								}
							}
						}
						if(rs.getString(3)!=null)
						{
							ecnumber=rs.getString(3);
							eckeys.add(rs.getString(2));
						}
						else
						{
							ecnumber="";
							eckeys=new TreeSet<String>();
						}
					}
				}
				enzymes.removeAll(existsEC);
				toDeleteEnzyme.removeAll(existsEC);

				//				for(String key:toDeleteKey.keySet())
				//				{
				//					stmt.executeUpdate("DELETE FROM homology_has_ecNumber WHERE( homology_s_key = \'" + key + "\' AND ecNumber_s_key =\'" + toDeleteKey.get(key) + "\');");
				//				}

				List<String> toDeleteER = new ArrayList<String>();
				rs = stmt.executeQuery("SELECT ecNumberRank.s_key, ecNumber FROM ecNumberRank WHERE geneHomology_s_key =\'"+keys.get(row)+"\'");
				while(rs.next())
				{
					for(String tde:toDeleteEnzyme)
					{
						if(tde.equals(rs.getString(2)))
						{
							toDeleteER.add(rs.getString(1));
						}
					}
				}
				for(String key:toDeleteER){stmt.execute("DELETE FROM ecNumberRank WHERE s_key = \'" +key + "\'");}
				int entry=0;
				for(String en: enzymes)
				{
					String homology_s_key;
					if(homologyNewEntryKey.containsKey(keys.get(row)))
					{
						homology_s_key = homologyNewEntryKey.get(keys.get(row));
					}
					else{
						rs=stmt.executeQuery("SELECT homology.s_key FROM homology WHERE geneHomology_s_key='"+keys.get(row)+"' AND organism_s_key=1 AND referenceID=\'"+entry+"\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO homology (geneHomology_s_key, organism_s_key, referenceID, gene, eValue, bits) VALUES (\'"+keys.get(row)+"\',\'1\',\'"+entry+"\',\'null\',\'0.0\',\'0.0\')");
							rs=stmt.executeQuery("SELECT last_insert_id()");
						}
						rs.next();
						homology_s_key=rs.getString(1);
					}
					String rank="1";
					//if(this.getEnzymesList().get(row).equals(en)){rank="0";}
					//else{rank="1";}
					rs=stmt.executeQuery("SELECT ecNumberRank.s_key FROM ecNumberRank WHERE geneHomology_s_key='"+keys.get(row)+"' AND ecnumber=\'"+en+"\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) VALUES(\'"+keys.get(row)+"\',\'"+this.mysqlStrConverter(en)+"\',"+rank+");");
						stmt.execute("INSERT INTO ecnumberrank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES(last_insert_id(),1);");
						//						rs=stmt.executeQuery("SELECT last_insert_id()");
					}

					StringTokenizer st= new StringTokenizer(en, ", ");
					while(st.hasMoreElements())
					{
						en=st.nextToken();
						rs = stmt.executeQuery("SELECT s_key FROM ecNumber WHERE ecNumber = \'" + this.mysqlStrConverter(en) + "\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO ecNumber (ecNumber) VALUES (\'" + this.mysqlStrConverter(en) + "\');");
							rs = stmt.executeQuery("SELECT last_insert_id()");
						}
						rs.next();
						String ec_skey=rs.getString(1);

						rs=stmt.executeQuery("SELECT * FROM homology_has_ecNumber WHERE homology_s_key="+homology_s_key+" AND homology_geneHomology_s_key='"+keys.get(row)+"' AND ecNumber_s_key=\'"+ec_skey+"\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO homology_has_ecNumber (homology_s_key, ecNumber_s_key, homology_geneHomology_s_key) VALUES (\'"+homology_s_key+"\',\'"+ec_skey+"\',\'"+keys.get(row)+"\');");
						}
					}
					entry++;
					//					else
					//					{
					//						stmt.execute("UPDATE ecNumberRank SET rank="+rank+" WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND ecnumber=\'"+this.mysqlStrConverter(en)+"\'");
					//					}
					//					rs.next();
					//					String ecNumberRank_s_key=rs.getString(1);
					//
					//					rs=stmt.executeQuery("SELECT * FROM ecnumberrank_has_organism WHERE organism_s_key=1 AND ecNumberRank_s_key=\'"+ecNumberRank_s_key+"\'");
					//					if(!rs.next())
					//					{
					//						stmt.execute("INSERT INTO ecnumberrank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES("+ecNumberRank_s_key+",1);");
					//					}
				}

				this.getEditedEnzymeData().remove(row);
			}
			for(Integer row : new TreeSet<Integer>(this.getEnzymesList().keySet()))
			{
				stmt.execute("UPDATE ecNumberRank SET rank=0 WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND ecnumber=\'"+this.mysqlStrConverter(this.getEnzymesList().get(row))+"\'");
				this.getEnzymesList().remove(row);
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//locustags
			for(Integer row : new TreeSet<Integer>(this.getLocusList().keySet()))
			{
				stmt.execute("UPDATE geneHomology SET locusTag=\'"+mysqlStrConverter(getLocusList().get(row))+"\' WHERE s_key = '" +keys.get(row)+"'");
				this.getLocusList().remove(row);
			}
			//names
			for(Integer row : new TreeSet<Integer>(this.getNamesList().keySet()))
			{
				stmt.execute("UPDATE geneHomology SET gene=\'"+ mysqlStrConverter(getNamesList().get(row))+"\' WHERE s_key = '" +keys.get(row)+"'");
				this.getNamesList().remove(row);
			}
			//chromosomes - organelles
			//for(Integer row : this.getEditedProductData().keySet())
			//{
			//	rs = stmt.executeQuery("SELECT geneHomology.s_key, chromosome, organelle, FROM geneHomology where geneHomology.s_key=" + keys.get(row) + ";");
			//	while(rs. next())
			//	{
			//		
			//	}
			//}
			stmt.close();


			Workbench.getInstance().warn("Data successfully loaded into database! \n" +
			"You sould save your project now...");

		}
		catch (SQLException e) {
			Workbench.getInstance().error("There was an error while loading data into database!");
			e.printStackTrace();}
	}
	 */

}
