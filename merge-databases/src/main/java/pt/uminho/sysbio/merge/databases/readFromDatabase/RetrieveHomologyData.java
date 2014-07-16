package pt.uminho.sysbio.merge.databases.readFromDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.merge.databases.containers.EcNumber;
import pt.uminho.sysbio.merge.databases.containers.EcNumberRank;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneHomology;
import pt.uminho.sysbio.merge.databases.containers.Homologues;
import pt.uminho.sysbio.merge.databases.containers.HomologySetup;
import pt.uminho.sysbio.merge.databases.containers.Organism;
import pt.uminho.sysbio.merge.databases.containers.ProductRank;

/**
 * @author pedro
 *
 */
public class RetrieveHomologyData {

	private Connection connection;
	private Map<Integer, Organism> orgMap;

	private Map<Integer,EcNumber> ecnumberNameMap;	
	private Map<Integer, Homologues> homologuesMap;

	private Map<Integer, List<ProductRank>> productRankGeneHomologyMap;
	private Map<Integer, List<EcNumberRank>> ecNumberRankGeneHomologyMap;
	private Map<Integer, List<Homologues>> homologuesGeneHomologyMap;
	private Map<Integer, FastaSequence> fastasequenceGeneHomologyMap;

	private Map<Integer, List<GeneHomology>> geneHomologyHomologySetupMap;

	/**
	 * @param connection
	 */
	public RetrieveHomologyData(Connection connection) {
		super();
		this.connection = connection;
	}

	/**
	 * @throws SQLException
	 */
	public void retrieveAllData() throws SQLException{

		this.getOrganismFromDatabase();
		this.getProductRankFromDatabase();
		this.getEcNumberRankFromDatabase();
		this.getEcnumberNameFromDatabase();
		this.getHomologuesFromDatabase();
		this.getFastaSequenceFromDatabase();
		this.getGeneHomologyFromDatabase();
	}
	/**
	 * Method to retrieve the organism object given its key
	 * @throws SQLException
	 */ 
	public void getOrganismFromDatabase() throws SQLException{

		orgMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_organism = stmt.executeQuery("SELECT * FROM organism");

		while(rs_organism.next()){

			Organism org = new Organism(rs_organism.getString(2), rs_organism.getString(3), rs_organism.getInt(4));
			this.orgMap.put(rs_organism.getInt(1),org);
		}
		rs_organism.close();
		stmt.close();
	}

	/** Method to retrieve the productRank information and Link this table to the geneHomology
	 * @throws SQLException
	 */
	public void getProductRankFromDatabase() throws SQLException{

		Map<Integer, ProductRank> productMap = new HashMap<>();
		Map<Integer, List<Integer>> saveGeneHomologyKeysMapProduct = new HashMap<>();
		productRankGeneHomologyMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_ProductRank = stmt.executeQuery("SELECT * FROM productRank" +
				" INNER JOIN productRank_has_organism ON (productRank.s_key = productRank_has_organism.productRank_s_key) ");

		int previous_key = 0;
		while(rs_ProductRank.next()){

			//construct the productMap - map with the list of organisms belonging to the same product
			int productRankKey = rs_ProductRank.getInt(1);
			if (previous_key != productRankKey) {

				ProductRank prodRank = new ProductRank(rs_ProductRank.getString(3), rs_ProductRank.getInt(4));	
				List <Organism> orgList = new ArrayList<>();
				orgList.add(this.orgMap.get(rs_ProductRank.getInt(6)));
				prodRank.setOrg(orgList);
				productMap.put(productRankKey, prodRank);
				previous_key = productRankKey;
			}
			else {
				productMap.get(productRankKey).getOrgList().add(this.orgMap.get(rs_ProductRank.getInt(6)));				
			}


			//construct the intermediate map for linking productRank to geneHomology
			if (saveGeneHomologyKeysMapProduct.containsKey(rs_ProductRank.getInt(2))){
				saveGeneHomologyKeysMapProduct.get(rs_ProductRank.getInt(2)).add(rs_ProductRank.getInt(1));

			}
			else{
				List<Integer> productRankKeys = new ArrayList<>();
				productRankKeys.add(rs_ProductRank.getInt(1));
				saveGeneHomologyKeysMapProduct.put(rs_ProductRank.getInt(2), productRankKeys);
			}
		}	
		rs_ProductRank.close();


		ResultSet rs_Link_geneHomologyProductRank = stmt.executeQuery("SELECT * FROM productRank" +
				" INNER JOIN geneHomology ON (productRank.geneHomology_s_key = geneHomology.s_key)");

		// construct the map of the lists of productRanks per geneHomology keys
		while(rs_Link_geneHomologyProductRank.next()){
			int productGeneHomologyKey = rs_Link_geneHomologyProductRank.getInt(2);
			if(! productRankGeneHomologyMap.containsKey(productGeneHomologyKey)){
				List <Integer> productRankKeysPerGeneHomologyKey = new ArrayList<>();
				productRankKeysPerGeneHomologyKey.addAll(saveGeneHomologyKeysMapProduct.get(productGeneHomologyKey));
				List <ProductRank> prodRankPerGeneHomologykey = new ArrayList<>();
				for (int i : productRankKeysPerGeneHomologyKey){
					prodRankPerGeneHomologykey.add(productMap.get(i));
				}
				productRankGeneHomologyMap.put(productGeneHomologyKey, prodRankPerGeneHomologykey);
			}
		}
		rs_Link_geneHomologyProductRank.close();
		stmt.close();
	}

	/**Method to retrieve the ecNumberRank information and Link this table to the geneHomology
	 * @throws SQLException
	 */
	public void getEcNumberRankFromDatabase() throws SQLException {

		Map<Integer,EcNumberRank> ecnumberMap = new HashMap<>();
		Map<Integer, List<Integer>> saveGeneHomologyKeysMapEcnumber = new HashMap<>();
		ecNumberRankGeneHomologyMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_ecNumberRank = stmt.executeQuery("SELECT * FROM ecNumberRank " + 
				"INNER JOIN ecNumberRank_has_organism ON (ecNumberRank.s_key = ecNumberRank_has_organism.ecNumberRank_s_key) ");

		//construct the ecnumberMap - map with the list of organisms belonging to the same ecnumberRank
		int previous_rs = 0;
		while(rs_ecNumberRank.next()){

			int ecNumberRankKey = rs_ecNumberRank.getInt(1);
			if (previous_rs != ecNumberRankKey){
				EcNumberRank ecnumberRank = new EcNumberRank(rs_ecNumberRank.getString(3), rs_ecNumberRank.getInt(4));
				List <Organism> orgList = new ArrayList<>(); 
				orgList.add(this.orgMap.get(rs_ecNumberRank.getInt(6))); 
				ecnumberRank.setOrg(orgList);
				ecnumberMap.put(ecNumberRankKey, ecnumberRank);
				previous_rs = ecNumberRankKey;
			}
			else{
				ecnumberMap.get(ecNumberRankKey).getOrg().add(this.orgMap.get(rs_ecNumberRank.getInt(6)));
			}

			//construct the intermediate map for linking ecnumberRank to geneHomology
			if (saveGeneHomologyKeysMapEcnumber.containsKey(rs_ecNumberRank.getInt(2))){
				saveGeneHomologyKeysMapEcnumber.get(rs_ecNumberRank.getInt(2)).add(rs_ecNumberRank.getInt(1));

			}
			else{
				List<Integer> ecNumberKeys = new ArrayList<>();
				ecNumberKeys.add(rs_ecNumberRank.getInt(1));
				saveGeneHomologyKeysMapEcnumber.put(rs_ecNumberRank.getInt(2), ecNumberKeys);
			}
		}
		rs_ecNumberRank.close();


		ResultSet rs_LinkGeneHomologyEcnumberRank = stmt.executeQuery("SELECT * FROM ecNumberRank" +
				" INNER JOIN geneHomology ON (ecNumberRank.geneHomology_s_key = geneHomology.s_key)");

		// construct the map of the lists of ecNumberRanks per geneHomology keys
		while(rs_LinkGeneHomologyEcnumberRank.next()){
			int ecnumberGeneHomologyKey = rs_LinkGeneHomologyEcnumberRank.getInt(2);
			if(! ecNumberRankGeneHomologyMap.containsKey(ecnumberGeneHomologyKey)){
				List <Integer> ecNumberRankKeysPerGeneHomologyKey = new ArrayList<>(); 
				ecNumberRankKeysPerGeneHomologyKey.addAll(saveGeneHomologyKeysMapEcnumber.get(ecnumberGeneHomologyKey));
				List <EcNumberRank> ecNumberRankPerGeneHomologykey = new ArrayList<>();
				for (int i : ecNumberRankKeysPerGeneHomologyKey){
					ecNumberRankPerGeneHomologykey.add(ecnumberMap.get(i));
				}
				ecNumberRankGeneHomologyMap.put(rs_LinkGeneHomologyEcnumberRank.getInt(2), ecNumberRankPerGeneHomologykey);
			}
		}
		rs_LinkGeneHomologyEcnumberRank.close();
		stmt.close();
	}

	/**Method to retrieve the ecNumber object given its key
	 * @throws SQLException
	 */
	public void getEcnumberNameFromDatabase() throws SQLException{
		ecnumberNameMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_ecNumberName = stmt.executeQuery("SELECT * FROM ecNumber");

		while(rs_ecNumberName.next()){
			int ecNumberNameKey = rs_ecNumberName.getInt(1);
			EcNumber ecnumberName = new EcNumber(rs_ecNumberName.getString(2));
			this.ecnumberNameMap.put(ecNumberNameKey, ecnumberName);	
		}
		rs_ecNumberName.close();
		stmt.close();
	}

	/** Method to retrieve the homologues information 
	 * @throws SQLException
	 */
	public void getHomologuesFromDatabase() throws SQLException{
		Map<Integer, List<EcNumber>> ecNumberNameHomologuesMap = new HashMap<>();
		Map<Integer, List<Integer>> saveHomologuesKeysMapEcnumbersName = new HashMap<>();

		this.homologuesMap = new HashMap<>();


		Statement stmt = this.connection.createStatement();
		ResultSet rs_homologuesSkeyEcnumberSkeysMap = stmt.executeQuery("SELECT * FROM homologues " +
				"INNER JOIN homologues_has_ecNumber ON (homologues.s_key = homologues_has_ecNumber.homologues_s_key)");


		// 1 construct the intermediate map for linking ecNumberName to homologues
		while (rs_homologuesSkeyEcnumberSkeysMap.next()){			
			if (saveHomologuesKeysMapEcnumbersName.containsKey(rs_homologuesSkeyEcnumberSkeysMap.getInt(9))){
				saveHomologuesKeysMapEcnumbersName.get(rs_homologuesSkeyEcnumberSkeysMap.getInt(9)).add(rs_homologuesSkeyEcnumberSkeysMap.getInt(10));	
			}
			else{
				List<Integer> ecnumberSkeys = new ArrayList<>();
				ecnumberSkeys.add(rs_homologuesSkeyEcnumberSkeysMap.getInt(10));
				saveHomologuesKeysMapEcnumbersName.put(rs_homologuesSkeyEcnumberSkeysMap.getInt(9), ecnumberSkeys);
			}
		}
		rs_homologuesSkeyEcnumberSkeysMap.close();


		ResultSet rs_homologuesEcnumber = stmt.executeQuery("SELECT * FROM homologues_has_ecNumber");

		// 1 construct the map of the lists of ecNUmberNames per homologues keys

		while(rs_homologuesEcnumber.next()){
			int homologuesSkey = rs_homologuesEcnumber.getInt(1);
			if(! ecNumberNameHomologuesMap.containsKey(homologuesSkey)){

				List <Integer> ecnumberKeysPerHomologuesKey = new ArrayList<>(); 
				ecnumberKeysPerHomologuesKey.addAll(saveHomologuesKeysMapEcnumbersName.get(homologuesSkey));
				List <EcNumber> ecnumberListPerHomologuesKey = new ArrayList<>();
				for (int i : ecnumberKeysPerHomologuesKey){		

					ecnumberListPerHomologuesKey.add(this.ecnumberNameMap.get(i));
				}
				ecNumberNameHomologuesMap.put(homologuesSkey, ecnumberListPerHomologuesKey);
			}
		}

		rs_homologuesEcnumber.close();
		//1 construct the homologuesMap - map the homologue object to its key
		ResultSet rs_homologues = stmt.executeQuery("SELECT * FROM homologues");

		while(rs_homologues.next()){
			Homologues homologue = new Homologues(rs_homologues.getString(3), rs_homologues.getString(4), rs_homologues.getDouble(5),
					rs_homologues.getString(6), rs_homologues.getString(7), rs_homologues.getInt(8));

			homologue.setOrg(this.orgMap.get(rs_homologues.getInt(2)));

			homologue.setEcNumber(ecNumberNameHomologuesMap.get(rs_homologues.getInt(1)));
		
			this.homologuesMap.put(rs_homologues.getInt(1), homologue);

		}

		rs_homologues.close();
	}

	/** Method to retrieve fastaSequence information and Link this to the geneHomology table
	 * @throws SQLException
	 */
	public void getFastaSequenceFromDatabase() throws SQLException{

		Map<Integer, FastaSequence> fastasequenceMap = new HashMap<>();
		fastasequenceGeneHomologyMap = new HashMap<>();
		Statement stmt = this.connection.createStatement();
		ResultSet rs_FastaSequence = stmt.executeQuery("SELECT * FROM fastaSequence");

		//construct the fastasequenceMap - map  the fastaSequence object with its key
		while(rs_FastaSequence.next()){
			int fastaSequenceKey = rs_FastaSequence.getInt(1);
			FastaSequence fastaSequence = new FastaSequence(rs_FastaSequence.getString(3));
			fastasequenceMap.put(fastaSequenceKey, fastaSequence);	
		}
		rs_FastaSequence.close();


		ResultSet rs_LinkFastasequenceGeneHomology = stmt.executeQuery("SELECT * FROM fastaSequence " +
				"INNER JOIN geneHomology ON (fastaSequence.geneHomology_s_key = geneHomology.s_key)");

		// construct the map of the fastaSequence per geneHomology key
		while(rs_LinkFastasequenceGeneHomology.next()){
			int fastaSequenceGeneHomologyKey = rs_LinkFastasequenceGeneHomology.getInt(2);
			fastasequenceGeneHomologyMap.put(fastaSequenceGeneHomologyKey, fastasequenceMap.get(rs_LinkFastasequenceGeneHomology.getInt(1)));
		}
		rs_LinkFastasequenceGeneHomology.close();
		stmt.close();
	}

	/**  Method to 1) retrieve the geneHomology information and 2) link this to the homologySetup table
	 * @throws SQLException
	 */
	public void getGeneHomologyFromDatabase() throws SQLException{
		Map<Integer, List<String []>> saveGeneHomologyKeysMapHomologues= new HashMap<>();
		this.homologuesGeneHomologyMap = new HashMap<>();

		Map<Integer,GeneHomology> geneHomologyMap = new HashMap<>();	
		Map <Integer, List<Integer>> saveHomologySetupKeysMapGeneHomologies = new HashMap<>();
		this.geneHomologyHomologySetupMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_LinkGeneHomologyHomologues = stmt.executeQuery("SELECT * FROM geneHomology_has_homologues ");

		//1 construct the intermediate map for linking homologues to geneHomology key
		while (rs_LinkGeneHomologyHomologues.next()){

			int genehomologySkey = rs_LinkGeneHomologyHomologues.getInt(1);

			if (saveGeneHomologyKeysMapHomologues.containsKey(genehomologySkey)){
				String [] homology = new String[5];
				for (int i = 0 ; i <5 ; i++){
					String aux = rs_LinkGeneHomologyHomologues.getString(i+2);
					homology[i] = aux;
				}
				saveGeneHomologyKeysMapHomologues.get(genehomologySkey).add(homology);
			}	
			else{
				List<String []> homologues = new ArrayList<>();
				String [] homology = new String[5];
				for (int i = 0 ; i <5 ; i++){
					String aux = rs_LinkGeneHomologyHomologues.getString(i+2);
					homology[i] = aux;
				}
				homologues.add(homology);
				saveGeneHomologyKeysMapHomologues.put(genehomologySkey, homologues);
			}
		}	
		rs_LinkGeneHomologyHomologues.close();



		ResultSet rs_LinkGeneHomologyHomologues2 = stmt.executeQuery("SELECT * FROM geneHomology_has_homologues " +
				"INNER JOIN geneHomology ON (geneHomology_has_homologues.geneHomology_s_key = geneHomology.s_key)");

		//1 construct the map of the list of homologues per geneHomology keys
		while (rs_LinkGeneHomologyHomologues2.next()){
			int geneHomologyKey = rs_LinkGeneHomologyHomologues2.getInt(1);
			if(! this.homologuesGeneHomologyMap.containsKey(geneHomologyKey)){
				List<String []> homologuesinfoPergeneHomologyKey = new ArrayList<>(); 
				homologuesinfoPergeneHomologyKey.addAll(saveGeneHomologyKeysMapHomologues.get(geneHomologyKey));

				List <Integer> homologueKey = new ArrayList<>();
				HashMap<Integer, String []> scoresETC = new HashMap<>();
				for (String [] i : homologuesinfoPergeneHomologyKey) {
					int key = Integer.parseInt(i[0]);
					homologueKey.add(key);

					scoresETC.put(key, i);
				}

				List <Homologues> homologuesListPerGeneHomologyKey = new ArrayList<>();
				for (int i : homologueKey){
					
//					System.out.println(this.homologuesMap.get(i).getLocusID());
//					System.out.println(this.homologuesMap.get(i).getDefinition());
//					System.out.println(this.homologuesMap.get(i).getCalculated_mw());
//					System.out.println(this.homologuesMap.get(i).getProduct());
//					System.out.println(this.homologuesMap.get(i).getOrganelle());
//					System.out.println(this.homologuesMap.get(i).getUniprot_star());
//					
//					System.out.println(scoresETC.get(i)[1]);
//					System.out.println(scoresETC.get(i)[2]);
//					System.out.println(Double.parseDouble(scoresETC.get(i)[3]));
//					System.out.println(Double.parseDouble(scoresETC.get(i)[4]));

					Homologues homologueTOTAL = new Homologues(this.homologuesMap.get(i).getLocusID(), this.homologuesMap.get(i).getDefinition(), this.homologuesMap.get(i).getCalculated_mw()
							, this.homologuesMap.get(i).getProduct(), this.homologuesMap.get(i).getOrganelle(), this.homologuesMap.get(i).getUniprot_star()
							, scoresETC.get(i)[1], scoresETC.get(i)[2], Double.parseDouble(scoresETC.get(i)[3]), Double.parseDouble(scoresETC.get(i)[4]));
					
					homologueTOTAL.setOrg(this.homologuesMap.get(i).getOrg());
					homologueTOTAL.setEcNumber(this.homologuesMap.get(i).getEcNumber());
					homologuesListPerGeneHomologyKey.add(homologueTOTAL);
				}
				this.homologuesGeneHomologyMap.put(geneHomologyKey, homologuesListPerGeneHomologyKey);
			}
		}
		rs_LinkGeneHomologyHomologues2.close();



		ResultSet rs_geneHomology = stmt.executeQuery(" SELECT * FROM geneHomology ");

		// 1 construct the geneHomologyMap - map the geneHomology object to its key
		while (rs_geneHomology.next()){
			GeneHomology genehomology = new GeneHomology(rs_geneHomology.getString(3), rs_geneHomology.getString(4),rs_geneHomology.getString(5), 
					rs_geneHomology.getString(6), rs_geneHomology.getString(7), rs_geneHomology.getInt(8), rs_geneHomology.getString(9), rs_geneHomology.getString(10));

			int genehomologySkey = rs_geneHomology.getInt(1);
			genehomology.setSequence(this.fastasequenceGeneHomologyMap.get(genehomologySkey));
			genehomology.setHomologues(this.homologuesGeneHomologyMap.get(genehomologySkey));
			genehomology.setEcNumber_rank(this.ecNumberRankGeneHomologyMap.get(genehomologySkey));
			genehomology.setProduct_rank(this.productRankGeneHomologyMap.get(genehomologySkey));
			geneHomologyMap.put(genehomologySkey, genehomology);


			// 2construct the intermediate map for linking geneHomology to homologySetup
			if (saveHomologySetupKeysMapGeneHomologies.containsKey(rs_geneHomology.getInt(2))){
				saveHomologySetupKeysMapGeneHomologies.get(rs_geneHomology.getInt(2)).add(rs_geneHomology.getInt(1));	
			}
			else{
				List<Integer> genehomologySkeys = new ArrayList<>();
				genehomologySkeys.add(rs_geneHomology.getInt(1));
				saveHomologySetupKeysMapGeneHomologies.put(rs_geneHomology.getInt(2), genehomologySkeys);
			}
		}
		rs_geneHomology.close();



		ResultSet rs_LinkGeneHomologyHomologySetup = stmt.executeQuery("SELECT * FROM geneHomology " +
				"INNER JOIN homologySetup ON (geneHomology.homologySetup_s_key = homologySetup.s_key)");

		//2 construct the map of the lists of geneHomologies per blastSetup keys
		while (rs_LinkGeneHomologyHomologySetup.next()){

			int homologySetupSkey = rs_LinkGeneHomologyHomologySetup.getInt(2);
			if(! geneHomologyHomologySetupMap.containsKey(homologySetupSkey)){
				List <Integer> geneHomologyKeysPerHomologySetupKey = new ArrayList<>(); 
				geneHomologyKeysPerHomologySetupKey.addAll(saveHomologySetupKeysMapGeneHomologies.get(homologySetupSkey));
				List <GeneHomology> geneHomologyListPerBlastSetupKey = new ArrayList<>();
				for (int i : geneHomologyKeysPerHomologySetupKey){
					geneHomologyListPerBlastSetupKey.add(geneHomologyMap.get(i));
				}
				geneHomologyHomologySetupMap.put(homologySetupSkey, geneHomologyListPerBlastSetupKey);
			}
		}
		rs_LinkGeneHomologyHomologySetup.close();
		stmt.close();
	}

	/** Method to retrieve the homologySetup information
	 * @throws SQLException
	 */
	public List<HomologySetup> getHomologySetupFromDatabase() throws SQLException{
		List<HomologySetup> homologySetupList = new ArrayList<>();
		Statement stmt = this.connection.createStatement();
		ResultSet rs_homologySetup = stmt.executeQuery(" SELECT * FROM homologySetup ");
		// construct the homologySetup object and put it into a list

		while (rs_homologySetup.next()){
			int homologySetupKey = rs_homologySetup.getInt(1);
			HomologySetup homologysetup = new HomologySetup(rs_homologySetup.getString(2), rs_homologySetup.getString(3), rs_homologySetup.getString(4), 
					rs_homologySetup.getString(5), rs_homologySetup.getString(6) , rs_homologySetup.getString(7), rs_homologySetup.getString(8), 
					rs_homologySetup.getInt(9));
			homologysetup.setGenehomology(new ConcurrentLinkedQueue<>(this.geneHomologyHomologySetupMap.get(homologySetupKey)));
			homologySetupList.add(homologysetup);
		}
		rs_homologySetup.close();
		stmt.close();		
		return homologySetupList;
	}

//	public static void main (String [] args) throws SQLException{
//
//		Connection c = new Connection("localhost", "3306","HMP", "root", "password");
//		RetrieveHomologyData r = new RetrieveHomologyData(c);
//		r.getOrganismFromDatabase();
//		r.getProductRankFromDatabase();
//		r.getEcNumberRankFromDatabase();
//		r.getHomologuesFromDatabase();
//		r.getFastaSequenceFromDatabase();
//		r.getGeneHomologyFromDatabase();
//		System.out.println(r.getHomologySetupFromDatabase());		
//	}

}
