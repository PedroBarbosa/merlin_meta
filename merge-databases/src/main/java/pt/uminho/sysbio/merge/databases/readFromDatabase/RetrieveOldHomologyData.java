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
import pt.uminho.sysbio.merge.databases.containers.BlastSetup;
import pt.uminho.sysbio.merge.databases.containers.EcNumber;
import pt.uminho.sysbio.merge.databases.containers.EcNumberRank;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneBlast;
import pt.uminho.sysbio.merge.databases.containers.Homology;
import pt.uminho.sysbio.merge.databases.containers.HomologyDataTableContainer;
import pt.uminho.sysbio.merge.databases.containers.Organism;
import pt.uminho.sysbio.merge.databases.containers.ProductRank;


/**
 * @author pedro
 *
 */
public class RetrieveOldHomologyData {

	private Connection connection;
	private Map<Integer, Organism> orgMap;

	private Map<Integer, HomologyDataTableContainer> homologyDataHomologyMap;
	private Map<Integer, List<EcNumber>> ecNumberNameHomologyMap;
	private Map<Integer,EcNumber> ecnumberNameMap;

	private Map<Integer, List<ProductRank>> productRankGeneblastMap;
	private Map<Integer, List<EcNumberRank>> ecNumberRankGeneblastMap;
	private Map<Integer, List<Homology>> homologyGeneblastMap;
	private Map<Integer, FastaSequence> fastasequenceGeneBlastMap;

	private Map<Integer, List<GeneBlast>> geneBlastBlastSetupMap;

	/**
	 * @param connection
	 */
	public RetrieveOldHomologyData(Connection connection) {
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
		this.getHomologyDataFromDatabase();
		this.getHomologyFromDatabase();
		this.getFastaSequenceFromDatabase();
		this.getGeneBlastFromDatabase();
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
		//System.out.println(orgMap.size());
	}

	/** Method to retrieve the productRank information and Link this table to the geneblast
	 * @throws SQLException
	 */
	public void getProductRankFromDatabase() throws SQLException{

		Map<Integer, ProductRank> productMap = new HashMap<>();
		Map<Integer, List<Integer>> saveBlastKeyProductKeysList = new HashMap<>();
		productRankGeneblastMap = new HashMap<>();

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


			//construct the intermediate map for linking productRank to Geneblast
			if (saveBlastKeyProductKeysList.containsKey(rs_ProductRank.getInt(2))){
				saveBlastKeyProductKeysList.get(rs_ProductRank.getInt(2)).add(rs_ProductRank.getInt(1));

			}
			else{
				List<Integer> productRankKeys = new ArrayList<>();
				productRankKeys.add(rs_ProductRank.getInt(1));
				saveBlastKeyProductKeysList.put(rs_ProductRank.getInt(2), productRankKeys);
			}
		}
		rs_ProductRank.close();


		ResultSet rs_LinkGeneBlastProductRank = stmt.executeQuery("SELECT * FROM productRank" +
				" INNER JOIN geneblast ON (productRank.geneblast_s_key = geneblast.s_key)");

		// construct the map of the lists of productRanks per geneBlast keys
		while(rs_LinkGeneBlastProductRank.next()){
			int productBlastKey = rs_LinkGeneBlastProductRank.getInt(2);
			if(! productRankGeneblastMap.containsKey(productBlastKey)){
				List <Integer> productRankKeysPerGeneBlastKey = new ArrayList<>();
				productRankKeysPerGeneBlastKey.addAll(saveBlastKeyProductKeysList.get(productBlastKey));
				List <ProductRank> prodRankPerBlastkey = new ArrayList<>();
				for (int i : productRankKeysPerGeneBlastKey){
					prodRankPerBlastkey.add(productMap.get(i));
				}
				productRankGeneblastMap.put(productBlastKey, prodRankPerBlastkey);
			}
		}
		rs_LinkGeneBlastProductRank.close();
		stmt.close();
	}

	/**Method to retrieve the ecNumberRank information and Link this table to the geneblast
	 * @throws SQLException
	 */
	public void getEcNumberRankFromDatabase() throws SQLException {

		Map<Integer,EcNumberRank> ecnumberMap = new HashMap<>();
		Map<Integer, List<Integer>> saveBlastKeysMapEcnumber = new HashMap<>();
		ecNumberRankGeneblastMap = new HashMap<>();

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

			//construct the intermediate map for linking ecnumberRank to Geneblast
			if (saveBlastKeysMapEcnumber.containsKey(rs_ecNumberRank.getInt(2))){
				saveBlastKeysMapEcnumber.get(rs_ecNumberRank.getInt(2)).add(rs_ecNumberRank.getInt(1));

			}
			else{
				List<Integer> blastKeys = new ArrayList<>();
				blastKeys.add(rs_ecNumberRank.getInt(1));
				saveBlastKeysMapEcnumber.put(rs_ecNumberRank.getInt(2), blastKeys);
			}
		}
		rs_ecNumberRank.close();


		ResultSet rs_LinkGeneBlastEcnumberRank = stmt.executeQuery("SELECT * FROM ecNumberRank" +
				" INNER JOIN geneblast ON (ecNumberRank.geneblast_s_key = geneblast.s_key)");

		// construct the map of the lists of ecNumberRanks per geneBlast keys
		while(rs_LinkGeneBlastEcnumberRank.next()){
			int ecnumberBlastKey = rs_LinkGeneBlastEcnumberRank.getInt(2);
			if(! ecNumberRankGeneblastMap.containsKey(ecnumberBlastKey)){
				List <Integer> ecNumberRankKeysPerGeneBlastKey = new ArrayList<>(); 
				ecNumberRankKeysPerGeneBlastKey.addAll(saveBlastKeysMapEcnumber.get(ecnumberBlastKey));
				List <EcNumberRank> ecNumberRankPerBlastkey = new ArrayList<>();
				for (int i : ecNumberRankKeysPerGeneBlastKey){
					ecNumberRankPerBlastkey.add(ecnumberMap.get(i));
				}
				ecNumberRankGeneblastMap.put(rs_LinkGeneBlastEcnumberRank.getInt(2), ecNumberRankPerBlastkey);
			}
		}
		rs_LinkGeneBlastEcnumberRank.close();
		stmt.close();
	}

	/** Method to retrieve homologyData information and Link this to the homology table
	 * @throws SQLException
	 */
	public void getHomologyDataFromDatabase() throws SQLException{
		Map<Integer, HomologyDataTableContainer> homologyDataMap = new HashMap<>();
		homologyDataHomologyMap = new HashMap<>();

		Statement stmt = this.connection.createStatement();
		ResultSet rs_homologyData = stmt.executeQuery("SELECT * FROM homologyData");

		//construct the homologyDataMap - map  the homology object with its key
		while(rs_homologyData.next()){

			HomologyDataTableContainer homologyData  = new HomologyDataTableContainer(rs_homologyData.getString(4), rs_homologyData.getString(5), 
					rs_homologyData.getDouble(6),rs_homologyData.getString(7), rs_homologyData.getString(8), rs_homologyData.getInt(9));

			homologyDataMap.put(rs_homologyData.getInt(1),homologyData);

		}

		rs_homologyData.close();

		ResultSet rs_LinkHomologyHomologyData = stmt.executeQuery("SELECT * FROM homologyData " +
				"INNER JOIN homology ON (homologyData.homology_s_key = homology.s_key)");

		// construct the map of the homologyData per homology key
		while(rs_LinkHomologyHomologyData.next()){
			int homologyDataHomologyKey = rs_LinkHomologyHomologyData.getInt(2);
			homologyDataHomologyMap.put(homologyDataHomologyKey, homologyDataMap.get(rs_LinkHomologyHomologyData.getInt(1)));
		}

		rs_LinkHomologyHomologyData.close();
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
			ecnumberNameMap.put(ecNumberNameKey, ecnumberName);	
		}
		rs_ecNumberName.close();
		stmt.close();
	}

	/** Method to 1) retrieve the homology information and 2) link this to the geneblast table
	 * @throws SQLException
	 */
	public void getHomologyFromDatabase() throws SQLException{

		this.getEcnumberNameFromDatabase();

		ecNumberNameHomologyMap = new HashMap<>();
		Map<Integer, List<Integer>> saveHomologyKeysMapEcnumbersName = new HashMap<>();

		homologyGeneblastMap = new HashMap<>();
		Map<Integer, List<Integer>> saveBlastKeysHomologyKeysList= new HashMap<>();
		Map<Integer, Homology> homologyMap = new HashMap<>();


		Statement stmt = this.connection.createStatement();
		ResultSet rs_homologySkeyEcnumberSkeysMap = stmt.executeQuery("SELECT * FROM " +
				"homology_has_ecNumber");

		// 1 construct the intermediate map for linking ecNumberName to homology
		while (rs_homologySkeyEcnumberSkeysMap.next()) {

			//			System.out.println(rs_homologySkeyEcnumberSkeysMap.getInt(1));
			//			System.out.println("\t"+rs_homologySkeyEcnumberSkeysMap.getInt(2));

			if (saveHomologyKeysMapEcnumbersName.containsKey(rs_homologySkeyEcnumberSkeysMap.getInt(1))) {

				saveHomologyKeysMapEcnumbersName.get(rs_homologySkeyEcnumberSkeysMap.getInt(1)).add(rs_homologySkeyEcnumberSkeysMap.getInt(2));	
			}
			else{

				List<Integer> ecnumberSkeys = new ArrayList<>();
				ecnumberSkeys.add(rs_homologySkeyEcnumberSkeysMap.getInt(2));
				saveHomologyKeysMapEcnumbersName.put(rs_homologySkeyEcnumberSkeysMap.getInt(1), ecnumberSkeys);
			}
		}


		//System.out.println("saveHomologyKeysMapEcnumbersName" + saveHomologyKeysMapEcnumbersName.size());

		rs_homologySkeyEcnumberSkeysMap.close();

		ResultSet rs_homology = stmt.executeQuery("SELECT * FROM homology_has_ecNumber" );

		while(rs_homology.next()){

			int homologySkey = rs_homology.getInt(1);
			if(! ecNumberNameHomologyMap.containsKey(homologySkey)){

				List <Integer> ecnumberKeysPerHomologyKey = new ArrayList<>(); 
				ecnumberKeysPerHomologyKey.addAll(saveHomologyKeysMapEcnumbersName.get(homologySkey));

				List <EcNumber> ecnumberListPerHomologyKey = new ArrayList<>();

				for (int i : ecnumberKeysPerHomologyKey){

					ecnumberListPerHomologyKey.add(this.ecnumberNameMap.get(i));
				}
				ecNumberNameHomologyMap.put(homologySkey, ecnumberListPerHomologyKey);
			}

		}
		rs_homology = stmt.executeQuery("SELECT * FROM homology");

		while(rs_homology.next()) {

			int homologySkey = rs_homology.getInt(1);

			Homology homology = new Homology(rs_homology.getString(4), rs_homology.getString(5), rs_homology.getDouble(6),
					rs_homology.getDouble(7));
//			if (homologyMap.containsKey(homologySkey)) {
//				
//				System.out.println("NUNCA PODE ACONTECER!!!!");
//			}
			homology.setOrg(this.orgMap.get(rs_homology.getInt(3)));
			homology.setHomologyData(this.homologyDataHomologyMap.get(homologySkey));
			homology.setEcNumber(ecNumberNameHomologyMap.get(homologySkey));
			homologyMap.put(homologySkey, homology);
			
//			if (saveBlastKeysHomologyKeysList.size()>9 && saveBlastKeysHomologyKeysList.size()<11) {
			//				
			//				System.out.println(saveBlastKeysHomologyKeysList);
			//				System.out.println();
			//			}
		}
		rs_homology.close();

		ResultSet rs_LinkGeneblastHomology = stmt.executeQuery("SELECT * FROM homology ");

		// 2 construct the intermediate map for linking homology to geneblast key
		while (rs_LinkGeneblastHomology.next()){

			int blastSkey = rs_LinkGeneblastHomology.getInt(2);

			if (saveBlastKeysHomologyKeysList.containsKey(blastSkey)){

				saveBlastKeysHomologyKeysList.get(blastSkey).add(rs_LinkGeneblastHomology.getInt(1));	
			}
			else {

				List<Integer> homologyKeys = new ArrayList<>();
				homologyKeys.add(rs_LinkGeneblastHomology.getInt(1));
				saveBlastKeysHomologyKeysList.put(blastSkey, homologyKeys);
			}

			//			if (saveBlastKeysHomologyKeysList.size()>9 && saveBlastKeysHomologyKeysList.size()<11) {
			//				
			//				System.out.println(saveBlastKeysHomologyKeysList);
			//				System.out.println();
			//			}
		}
		rs_LinkGeneblastHomology.close();
		stmt.close();

		//		ResultSet rs_LinkGeneblastHomology2 = stmt.executeQuery("SELECT * FROM homology");
		//
		//		// 2 construct the map of the lists of homology per geneblast keys
		//		while (rs_LinkGeneblastHomology2.next()){
		//
		//			int blastSkey = rs_LinkGeneblastHomology2.getInt(2);
		//
		//			if(! homologyGeneblastMap.containsKey(blastSkey)){
		//
		//				List <Homology> HomologyListPerBlastKey = new ArrayList<>();
		//
		//				for (int i : saveBlastKeysHomologyKeysList.get(blastSkey)){
		//
		//					HomologyListPerBlastKey.add(homologyMap.get(i));
		//				}
		//				homologyGeneblastMap.put(blastSkey, HomologyListPerBlastKey);
		//			}
		//		}

		for(int geneBlastKey : saveBlastKeysHomologyKeysList.keySet() ) {

			List <Homology> homologyListPerBlastKey = new ArrayList<>();

			for (int i : saveBlastKeysHomologyKeysList.get(geneBlastKey)){

				homologyListPerBlastKey.add(homologyMap.get(i));
			}
			homologyGeneblastMap.put(geneBlastKey, homologyListPerBlastKey);
		}
	}

	/** Method to retrieve fastaSequence information and Link this to the geneblast table
	 * @throws SQLException
	 */
	public void getFastaSequenceFromDatabase() throws SQLException{

		Map<Integer, FastaSequence> fastasequenceMap = new HashMap<>();
		fastasequenceGeneBlastMap = new HashMap<>();
		Statement stmt = this.connection.createStatement();
		ResultSet rs_FastaSequence = stmt.executeQuery("SELECT * FROM fastaSequence");

		//construct the fastasequenceMap - map  the fastaSequence object with its key
		while(rs_FastaSequence.next()){
			int fastaSequenceKey = rs_FastaSequence.getInt(1);
			FastaSequence fastaSequence = new FastaSequence(rs_FastaSequence.getString(3));
			fastasequenceMap.put(fastaSequenceKey, fastaSequence);	
		}
		rs_FastaSequence.close();


		ResultSet rs_LinkFastasequenceGeneBlast = stmt.executeQuery("SELECT * FROM fastaSequence " +
				"INNER JOIN geneblast ON (fastaSequence.geneblast_s_key = geneblast.s_key)");

		// construct the map of the fastaSequence per geneblast key
		while(rs_LinkFastasequenceGeneBlast.next()){
			int fastaSequenceBlastKey = rs_LinkFastasequenceGeneBlast.getInt(2);
			fastasequenceGeneBlastMap.put(fastaSequenceBlastKey, fastasequenceMap.get(rs_LinkFastasequenceGeneBlast.getInt(1)));
		}
		rs_LinkFastasequenceGeneBlast.close();
		stmt.close();
	}

	/**  Method to 1) retrieve the geneblast information and 2) link this to the blastSetup table
	 * @throws SQLException
	 */
	public void getGeneBlastFromDatabase() throws SQLException{
		Map<Integer, GeneBlast> geneBlastMap = new HashMap<>();	
		Map <Integer, List<Integer>> saveBlastSetupKeysMapGeneBlasts = new HashMap<>();
		geneBlastBlastSetupMap = new HashMap<>();


		Statement stmt = this.connection.createStatement();
		ResultSet rs_geneblast = stmt.executeQuery(" SELECT * FROM geneblast ");

		// 1 construct the geneBlastMap - map the geneblast object to its key
		while (rs_geneblast.next()){
			GeneBlast geneblast = new GeneBlast(rs_geneblast.getString(3), rs_geneblast.getString(4),rs_geneblast.getString(5), 
					rs_geneblast.getString(6), rs_geneblast.getString(7), rs_geneblast.getString(9), rs_geneblast.getString(10), rs_geneblast.getInt(8));

			int geneblastSkey = rs_geneblast.getInt(1);

			if(this.fastasequenceGeneBlastMap.containsKey(geneblastSkey)) {

				geneblast.setSequence(this.fastasequenceGeneBlastMap.get(geneblastSkey));
			}

			if(this.homologyGeneblastMap.containsKey(geneblastSkey)) {

				geneblast.setHomology(this.homologyGeneblastMap.get(geneblastSkey));
				geneblast.setEcNumber_rank(this.ecNumberRankGeneblastMap.get(geneblastSkey));
				geneblast.setProduct_rank(this.productRankGeneblastMap.get(geneblastSkey));
			}

			geneBlastMap.put(geneblastSkey, geneblast);


			// 2construct the intermediate map for linking geneblasts to blatSetupkey
			if (saveBlastSetupKeysMapGeneBlasts.containsKey(rs_geneblast.getInt(2))){
				saveBlastSetupKeysMapGeneBlasts.get(rs_geneblast.getInt(2)).add(rs_geneblast.getInt(1));	
			}
			else{
				List<Integer> geneblastSkeys = new ArrayList<>();
				geneblastSkeys.add(rs_geneblast.getInt(1));
				saveBlastSetupKeysMapGeneBlasts.put(rs_geneblast.getInt(2), geneblastSkeys);
			}

		}
		rs_geneblast.close();


		ResultSet rs_LinkGeneblastBlastSetup = stmt.executeQuery("SELECT * FROM geneblast " +
				"INNER JOIN blastSetup ON (geneblast.blastSetup_s_key = blastSetup.s_key)");

		//2 construct the map of the lists of geneblasts per blastSetup keys
		while (rs_LinkGeneblastBlastSetup.next()){

			int blastSetupSkey = rs_LinkGeneblastBlastSetup.getInt(2);
			if(! geneBlastBlastSetupMap.containsKey(blastSetupSkey)){
				List <Integer> blastKeysPerBlastSetupKey = new ArrayList<>(); 
				blastKeysPerBlastSetupKey.addAll(saveBlastSetupKeysMapGeneBlasts.get(blastSetupSkey));
				List <GeneBlast> GeneBlastListPerBlastSetupKey = new ArrayList<>();
				for (int i : blastKeysPerBlastSetupKey){
					GeneBlastListPerBlastSetupKey.add(geneBlastMap.get(i));
				}
				geneBlastBlastSetupMap.put(blastSetupSkey, GeneBlastListPerBlastSetupKey);
			}
		}
		rs_LinkGeneblastBlastSetup.close();
		stmt.close();
	}

	/** Method to retrieve the blastSetup information
	 * @throws SQLException
	 */
	public List<BlastSetup> getBlastSetupFromDatabase() throws SQLException{
		List<BlastSetup> blastSetupList = new ArrayList<>();
		Statement stmt = this.connection.createStatement();
		ResultSet rs_blastSetup = stmt.executeQuery(" SELECT * FROM blastSetup ");
		// construct the blastSetup object

		while (rs_blastSetup.next()){
			int blastSetupKey = rs_blastSetup.getInt(1);
			BlastSetup blastsetup = new BlastSetup(rs_blastSetup.getString(2), rs_blastSetup.getString(3), rs_blastSetup.getString(4), 
					rs_blastSetup.getString(5), rs_blastSetup.getString(6) , rs_blastSetup.getString(7), rs_blastSetup.getString(8), 
					rs_blastSetup.getInt(9));
			
			if(geneBlastBlastSetupMap.containsKey(blastSetupKey)){
				
				blastsetup.setGeneblast(new ConcurrentLinkedQueue<>(geneBlastBlastSetupMap.get(blastSetupKey)));
				blastSetupList.add(blastsetup);
			}
			
		}
		rs_blastSetup.close();
		stmt.close();		
		return blastSetupList;

	}

	public static void main (String [] args) throws SQLException{

		//Connection c = new Connection("192.168.1.143", "3306","HMP", "pedro", "password"); //file 3 
		//Connection c = new Connection("localhost", "3306","HMP", "root", "password"); // file 4 this pc
		Connection c1 = (Connection) new Connection("127.0.0.1", "3306","salivaOld", "root", "password");
		RetrieveOldHomologyData r = new RetrieveOldHomologyData(c1);
		r.getOrganismFromDatabase();
		r.getProductRankFromDatabase();
		r.getEcNumberRankFromDatabase();
		r.getHomologyDataFromDatabase();
		r.getHomologyFromDatabase();
		r.getFastaSequenceFromDatabase();
		r.getGeneBlastFromDatabase();
		System.out.println(r.getBlastSetupFromDatabase());	
		
		
	}

}