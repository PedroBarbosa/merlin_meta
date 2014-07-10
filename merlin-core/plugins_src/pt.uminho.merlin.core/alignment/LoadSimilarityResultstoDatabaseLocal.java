/**
 * 
 */
package alignment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.merge.databases.containers.EcNumber;
import pt.uminho.sysbio.merge.databases.containers.EcNumberRank;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneHomology;
import pt.uminho.sysbio.merge.databases.containers.Homologues;
import pt.uminho.sysbio.merge.databases.containers.Organism;
import pt.uminho.sysbio.merge.databases.containers.ProductRank;


/**
 * @author pedro
 *
 */
public class LoadSimilarityResultstoDatabaseLocal {

	private Statement statement;
	private String homologySetupID;
	private int organism_s_key, homologues_s_key, geneHomology_s_key;
	private LoadedData loadedData;


	/**
	 * @param statement
	 * @param loadedData
	 * @throws SQLException 
	 */
	public LoadSimilarityResultstoDatabaseLocal(Statement statement) throws SQLException {
		this.statement = statement;

	}

	/**
	 * @param homologySetup
	 */
	public LoadSimilarityResultstoDatabaseLocal(Statement statement, String homology_setup_key, LoadedData loadedData) {

		this.statement = statement;
		this.homologySetupID = homology_setup_key;
		this.loadedData = loadedData;
	}

	/**
	 * @param locusTag
	 * @param query
	 * @param gene
	 * @param chromosome
	 * @param organelle
	 * @param uniprot_star
	 * @param status
	 * @param uniprot_ecnumber
	 * @throws SQLException
	 */
	private void loadGeneHomology(String locusTag, String query, String gene, String chromosome, 
			String organelle, int uniprot_star, String status, String uniprot_ecnumber) throws SQLException {

		if (! this.loadedData.getGenesMap().containsKey(query)){


			this.statement.execute("INSERT INTO geneHomology (locusTag, query, gene, chromosome, organelle, uniprot_star, homologySetup_s_key, status, uniprot_ecnumber)" +
					" VALUES ('"+ MySQL_Utilities.mysqlStrConverter(locusTag) +"','"+ MySQL_Utilities.mysqlStrConverter(query) + "','"+ MySQL_Utilities.mysqlStrConverter(gene) + "','" + chromosome + "','"+ organelle + 
					"'," +uniprot_star + ",'" +this.homologySetupID+ "','"+status+"','"+uniprot_ecnumber+"');");

			ResultSet rs =this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.geneHomology_s_key = rs.getInt(1);
			rs.close();
			this.loadedData.getGenesMap().put(query, this.geneHomology_s_key);
		}
		else{
			this.geneHomology_s_key = 0;
		}
	}

	/**
	 * @param organism
	 * @throws SQLException
	 */
	private void loadOrganism(Organism organism) throws SQLException {

		//		ResultSet rs =this.statement.executeQuery("SELECT * FROM organism where organism = '"+
		//				MySQL_Utilities.mysqlStrConverter(organism.getOrganism())+"';");
		//
		//		if(rs.next()) {
		//
		//			this.organism_s_key = rs.getInt(1);
		//			//this.undo_organism_s_key="";
		//		}
		//		else {
		synchronized (this.loadedData.getOrgMap()) {

			
			if (this.loadedData.getOrgMap().containsKey(organism.getOrganism())) {
				this.organism_s_key = this.loadedData.getOrgMap().get(organism.getOrganism());
			}

			else{
				System.out.println("TAX" + organism.getTaxRank());
				this.statement.execute("INSERT INTO organism (organism, taxonomy, taxrank) VALUES ('"
						+ MySQL_Utilities.mysqlStrConverter(organism.getOrganism()) +"','"+
						MySQL_Utilities.mysqlStrConverter(organism.getTaxonomy()) +"','"+organism.getTaxRank()+"');");

				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.organism_s_key = rs.getInt(1);
				//this.undo_organism_s_key=this.organism_s_key;

				this.loadedData.getOrgMap().put(organism.getOrganism(), this.organism_s_key);
				rs.close();							
			}
		}
	}

	/**
	 * @param taxonomy
	 * @param myOrganismTaxonomy
	 * @return
	 */
	private Integer rankTaxonomy(String taxonomy, String myOrganismTaxonomy) {

		if(taxonomy== null || myOrganismTaxonomy == null) {

			return 0;
		}
		else {

			String[] taxonomyArray = taxonomy.split(";");
			String[] myOrganismTaxonomyArray = myOrganismTaxonomy.split(";");

			for(int i = 0; i< myOrganismTaxonomyArray.length; i++) {

				if(taxonomyArray.length>i) {

					if(!myOrganismTaxonomyArray[i].equals(taxonomyArray[i])) {

						return i;
					}
				}
				else {

					return taxonomyArray.length;
				}
			}
			return myOrganismTaxonomyArray.length;
		}
	}

	/**
	 * @param homologue
	 * @throws SQLException
	 */
	private void loadHomologuesRemote(Homologues homologue) throws SQLException{

		String product = homologue.getProduct();
		if(homologue.getProduct()==null) {

			product =homologue.getDefinition();
		}

		int homologues_s_key = 0;
		synchronized( this.loadedData.getHomologuesMap()) {


			if (this.loadedData.getHomologuesMap().containsKey(homologue.getLocusID())){

				homologues_s_key = this.loadedData.getHomologuesMap().get(homologue.getLocusID());
			}

			else{
				this.statement.execute("INSERT INTO homologues (locusID," +
						" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
						"VALUES ('"+ homologue.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologue.getDefinition()) + "','"+ homologue.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
						"'"+ homologue.getOrganelle() +"',"+homologue.getUniprot_star()+")");

				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();

				homologues_s_key = rs.getInt(1);
				rs.close();

				this.loadedData.getHomologuesMap().put(homologue.getLocusID(), homologues_s_key);
			}
		}

		if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key) && this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(this.homologues_s_key)){
		}
		else{

			this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
					"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologue.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homologue.getReferenceID())+"'" +
					",'"+ homologue.getGene()+"','"+ homologue.getEvalue()+"','"+ homologue.getScore()+"');");

			if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
				this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(this.loadedData.getHomologuesMap().get(homologue.getLocusID()));
			}
			else{
				Set <Integer> setHomologues = new HashSet<>();
				setHomologues.add( this.loadedData.getHomologuesMap().get(homologue.getLocusID()));
				this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);	
			}

		}


	}

	/**
	 * @param homologue
	 * @throws SQLException
	 */
	private void loadHomologuesLocal(Homologues homologue) throws SQLException{
		String product = homologue.getProduct();
		if(homologue.getProduct()==null) {

			product =homologue.getDefinition();
		}

		this.homologues_s_key = 0;
		synchronized( this.loadedData.getHomologuesMap()) {


			if (this.loadedData.getHomologuesMap().containsKey(homologue.getLocusID())){

				this.homologues_s_key = this.loadedData.getHomologuesMap().get(homologue.getLocusID());
			}

			else{
				this.statement.execute("INSERT INTO homologues (locusID," +
						" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
						"VALUES ('"+ homologue.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologue.getDefinition()) + "','"+ homologue.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
						"'"+ homologue.getOrganelle() +"',"+homologue.getUniprot_star()+")");

				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();

				this.homologues_s_key = rs.getInt(1);
				rs.close();

				this.loadedData.getHomologuesMap().put(homologue.getLocusID(), this.homologues_s_key);
			}
		}

		if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key) && this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(this.homologues_s_key)){
		}
		else{

			this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
					"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologue.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homologue.getReferenceID())+"'" +
					",'"+ homologue.getGene()+"','"+ homologue.getEvalue()+"','"+ homologue.getScore()+"');");

			if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
				this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(this.loadedData.getHomologuesMap().get(homologue.getLocusID()));
			}
			else{
				Set <Integer> setHomologues = new HashSet<>();
				setHomologues.add( this.loadedData.getHomologuesMap().get(homologue.getLocusID()));
				this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);	
			}

		}

	}

	/**
	 * @param sequence
	 * @throws SQLException
	 */
	private void loadFastaSequence(FastaSequence sequence) throws SQLException{

		//		ResultSet rs = this.statement.executeQuery("SELECT * FROM fastaSequence WHERE geneHomology_s_key = '"+this.geneHomology_s_key+"'");
		//
		//		if(!rs.next()) {
		//System.out.println("sequence \t" + this.loadedData.getFastaSequencesMap().get(sequence));
		if (!this.loadedData.getFastaSequencesMap().containsKey(sequence)){

			this.statement.execute("INSERT INTO fastaSequence (geneHomology_s_key, sequence) VALUES('"+ this.geneHomology_s_key + "','"+ sequence.getSequence() +"');");

			ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();

			int fastaSequence_skey = rs.getInt(1);
			rs.close();

			this.loadedData.getFastaSequencesMap().put(sequence.getSequence(), fastaSequence_skey);
		}
	}

	/**
	 * @param ecNumber
	 * @throws SQLException
	 */
	private void loadECNumbers(String ecNumber, String locusID) throws SQLException{

		//this.statement.execute("LOCK tables ecNumber read,homology_has_ecNumber read;");

		//		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumber WHERE ecNumber = '"+ecNumber+"'");
		//		
		//		if(!rs.next()) {

		synchronized( this.loadedData.getEcNumbersMap()) {

			if (this.loadedData.getEcNumbersMap().containsKey(ecNumber)){


				if (this.loadedData.getEcNumberToHomologues().containsKey(this.loadedData.getHomologuesMap().get(locusID))){

					if(!this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(locusID)).contains(this.loadedData.getEcNumbersMap().get(ecNumber))) {

						this.statement.execute("INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) VALUES('"+
								this.loadedData.getHomologuesMap().get(locusID) +"','"+ this.loadedData.getEcNumbersMap().get(ecNumber) +"');");	

						this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(locusID)).add(this.loadedData.getEcNumbersMap().get(ecNumber));
					}

				}
				else {

					if(!this.loadedData.getEcNumberToHomologues().containsKey(this.loadedData.getHomologuesMap().get(locusID))){
						this.statement.execute( "INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) VALUES('"+
								this.loadedData.getHomologuesMap().get(locusID) +"','"+ this.loadedData.getEcNumbersMap().get(ecNumber) +"');");	

						Set <Integer> setEcsNameKey = new HashSet<>();
						setEcsNameKey.add( this.loadedData.getEcNumbersMap().get(ecNumber));
						this.loadedData.getEcNumberToHomologues().put((this.loadedData.getHomologuesMap().get(locusID)), setEcsNameKey);
					}

				}


			}
			else{
				this.statement.execute("INSERT INTO ecNumber SET ecNumber='"+ ecNumber +"'");
				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				int ecnumber_s_key = rs.getInt(1);
				rs.close();

				this.loadedData.getEcNumbersMap().put(ecNumber, ecnumber_s_key);

				if (this.loadedData.getEcNumberToHomologues().containsKey(this.loadedData.getHomologuesMap().get(locusID))){

					this.statement.execute("INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) VALUES('"+
							this.loadedData.getHomologuesMap().get(locusID) +"','"+ ecnumber_s_key + "');");

					this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(locusID)).add(this.loadedData.getEcNumbersMap().get(ecNumber));
				}

				else{
					if(!this.loadedData.getEcNumberToHomologues().containsKey(this.loadedData.getHomologuesMap().get(locusID))){
						this.statement.execute("INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) VALUES('"+
								this.loadedData.getHomologuesMap().get(locusID) +"','"+ ecnumber_s_key + "');");

						Set <Integer> setEcsNameKey = new HashSet<>();
						setEcsNameKey.add(ecnumber_s_key);
						this.loadedData.getEcNumberToHomologues().put(this.loadedData.getHomologuesMap().get(locusID), setEcsNameKey);
					}
				}

			}



		}


		//}

		//		if(ecn.containsKey(ecNumber))
		//		{
		//			ecn.put(ecNumber, ecn.get(ecNumber)+1);
		//		}
		//		else
		//			ecn.put(ecNumber, 1);

		//		return ecn;

		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 * @param databaseID
	 * @param program
	 * @param version
	 * @param eValue
	 * @param matrix
	 * @param wordSize
	 * @param gapCosts
	 * @param maxNumberOfAlignments
	 * @throws SQLException
	 */
	public String loadHomologySetup(String databaseID, String program, String version, String eValue, String matrix, String wordSize, String gapCosts,
			int maxNumberOfAlignments) throws SQLException{
		ResultSet rs = this.statement.executeQuery("SELECT * FROM homologySetup " +
				"WHERE databaseID = '"+databaseID+"' " +
				"AND program ='"+program+"' " +
				"AND version ='"+version+"' " +
				"AND eValue ='"+eValue+"' " +
				"AND matrix ='"+matrix+"' " +
				"AND wordSize ='"+wordSize+"' " +
				"AND gapCosts ='"+gapCosts+"' " +
				"AND maxNumberOfAlignments = '"+maxNumberOfAlignments+"'");

		if(!rs.next()) {

			this.statement.execute("INSERT INTO homologySetup (program, version, databaseID, eValue, matrix, wordSize, gapCosts, maxNumberOfAlignments) " +
					"VALUES ('"+ program +"','"+version +"','"+ databaseID +"','"+eValue +"','"+matrix +"'," +
					"'"+wordSize +"','"+gapCosts +"','"+maxNumberOfAlignments +"');");
			rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}
		this.homologySetupID = rs.getString(1);
		rs.close();

		return this.homologySetupID;

	}
	
	/**
	 * 
	 * @param productName
	 * @param rank
	 * @param prodRankOrg
	 * @throws SQLException
	 */
	private void loadProductRank (String productName, Integer rank, List<Integer> prodRankOrg) throws SQLException{
		//		ResultSet rs = this.statement.executeQuery("SELECT * FROM productRank " +
		//				"WHERE geneHomology_s_key = '"+this.geneHomology_s_key+"' AND " +
		//				"productName = '"+MySQL_Utilities.mysqlStrConverter(productName)+"' AND rank = '"+rank+"'");
		//
		//		if(!rs.next()) {

		//		}	

		if (this.loadedData.getProductRankMap().containsKey(this.geneHomology_s_key)){



			if(!this.loadedData.getProductRankMap().get(this.geneHomology_s_key).contains(productName)){


				this.statement.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) " +
						"VALUES('"+ this.geneHomology_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(productName) + "','" +rank+ "');");

				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();

				int productrank_s_key = rs.getInt(1);
				rs.close();


				this.loadedData.getProductRankMap().get(this.geneHomology_s_key).add(productName);


				if(! this.loadedData.getProductRankToOrganism().containsKey(productrank_s_key)){


					for (int i = 0; i < prodRankOrg.size(); i++){

						int orgkey = prodRankOrg.get(i);

						this.statement.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key)" +
								" VALUES('"+ productrank_s_key + "','" +orgkey +"');");	

						if (i == 0){
							Set <Integer> setOrgsProd = new HashSet<>();
							setOrgsProd.add(this.loadedData.getOrgMap().get(orgkey));
							this.loadedData.getProductRankToOrganism().put(productrank_s_key, setOrgsProd);	
						}
						else{

							this.loadedData.getProductRankToOrganism().get(productrank_s_key).add(orgkey);				
						}
					}

				}


			}

		}
		else{
			this.statement.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) " +
					"VALUES('"+ this.geneHomology_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(productName) + "','" +rank+ "');");

			ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();

			int productrank_s_key = rs.getInt(1);
			rs.close();

			Set <String> setProdsGene = new HashSet<>();
			setProdsGene.add(productName);
			this.loadedData.getProductRankMap().put(this.geneHomology_s_key, setProdsGene);


			for (int i = 0; i < prodRankOrg.size(); i++){

				int orgkey = prodRankOrg.get(i);

				this.statement.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key)" +
						" VALUES('"+ productrank_s_key + "','" +orgkey+ "');");	


				if (i == 0){
					Set <Integer> setOrgsProd = new HashSet<>();
					setOrgsProd.add(this.loadedData.getOrgMap().get(orgkey));
					this.loadedData.getProductRankToOrganism().put(productrank_s_key, setOrgsProd);	
				}
				else{
					this.loadedData.getProductRankToOrganism().get(productrank_s_key).add(orgkey);	

				}			

			}

		}
	}

	/**
	 * 
	 * @param pd
	 * @param productOrg
	 * @throws SQLException
	 */
	private void loadProductRankLocal (Map <String, Integer> pd, Map <String, List <Integer>> productOrg) throws SQLException{


		for(String product:pd.keySet()) {

			this.statement.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) " +
					"VALUES("+ this.geneHomology_s_key +",'"+ MySQL_Utilities.mysqlStrConverter(product) + "','" +pd.get(product)+ "');");

			ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			int prodkey = rs.getInt(1);

			for(int orgKey : productOrg.get(product)) {

				this.statement.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES("+prodkey + "," + orgKey+ ");");
			}
			rs.close();
		}

	}
	
	/**
	 * 
	 * @param ecnumber
	 * @param rank
	 * @param ecRankOrg
	 * @throws SQLException
	 */
	private void loadECNumberRank(String ecnumber, Integer rank, List<Integer> ecRankOrg) throws SQLException{

		//		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumberRank " +
		//				"WHERE geneHomology_s_key = '"+this.geneHomology_s_key+"' AND " +
		//				"ecNumber = '"+MySQL_Utilities.mysqlStrConverter(ecnumber)+"' AND rank = '"+rank+"'");

		//	if(!rs.next()) {

		if (this.loadedData.getEcNumberRankMap().containsKey(this.geneHomology_s_key)){


			if(!this.loadedData.getEcNumberRankMap().get(this.geneHomology_s_key).contains(ecnumber)){


				this.statement.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) " +
						"VALUES('"+ this.geneHomology_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(ecnumber) + "','" +rank+ "');");

				ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();

				int ecnumberrank_s_key = rs.getInt(1);
				rs.close();


				this.loadedData.getEcNumberRankMap().get(this.geneHomology_s_key).add(ecnumber);


				if(! this.loadedData.getEcNumberRankToOrganism().containsKey(ecnumberrank_s_key)){


					for (int i = 0; i < ecRankOrg.size(); i++){

						int orgkey = ecRankOrg.get(i);

						this.statement.execute("INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key)" +
								//" VALUES('"+ ecnumberrank_s_key + "','" +this.loadedData.getOrgMap().get(orgkey.getOrganism())+ "');");	
								" VALUES('"+ ecnumberrank_s_key + "','" +orgkey+ "');");	

						if (i == 0){
							Set <Integer> setOrgsEc = new HashSet<>();
							//setOrgsEc.add(this.loadedData.getOrgMap().get(orgkey.getOrganism()));
							setOrgsEc.add(this.loadedData.getOrgMap().get(orgkey));
							this.loadedData.getEcNumberRankToOrganism().put(ecnumberrank_s_key, setOrgsEc);	
						}
						else{
							//this.loadedData.getEcNumberRankToOrganism().get(ecnumberrank_s_key).add(this.loadedData.getOrgMap().get(orgkey.getOrganism()));	
							this.loadedData.getEcNumberRankToOrganism().get(ecnumberrank_s_key).add(orgkey);

						}			

					}

				}


			}

		}
		else{
			this.statement.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) " +
					"VALUES('"+ this.geneHomology_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(ecnumber) + "','" +rank+ "');");

			ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();

			int ecnumberrank_s_key = rs.getInt(1);
			rs.close();

			Set <String> setEcsGene = new HashSet<>();
			setEcsGene.add(ecnumber);
			this.loadedData.getEcNumberRankMap().put(this.geneHomology_s_key, setEcsGene);


			for (int i = 0; i < ecRankOrg.size(); i++){

				int orgkey = ecRankOrg.get(i);

				this.statement.execute("INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key)" +
						" VALUES('"+ ecnumberrank_s_key + "','" +orgkey+ "');");	

				if (i == 0){
					Set <Integer> setOrgsEc = new HashSet<>();
					setOrgsEc.add(this.loadedData.getOrgMap().get(orgkey));
					this.loadedData.getEcNumberRankToOrganism().put(ecnumberrank_s_key, setOrgsEc);	
				}
				else{
					this.loadedData.getEcNumberRankToOrganism().get(ecnumberrank_s_key).add(orgkey);	

				}			

			}

		}
	}

	//}
	private void loadECnumberRankLocal (Map <Set<String>, Integer> ecn, Map <Set<String>, List <Integer>> ecOrg) throws SQLException{

		for(Set<String> ecnumber: ecn.keySet()) {

			String concatEC="";
			for(String s:ecnumber) {

				if(concatEC.isEmpty())
					concatEC = s;
				else
					concatEC += ", " + s;
			}
			//for(String ecnumber_concat : ecn.keySet()) {

			this.statement.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) " +
					"VALUES("+ this.geneHomology_s_key +",'"+ MySQL_Utilities.mysqlStrConverter(concatEC) + "','" +ecn.get(concatEC)+ "');");

			ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			int ecKey = rs.getInt(1);

			for(int orgKey : ecOrg.get(ecnumber)) {

				this.statement.execute("INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES("+ecKey + "," + orgKey+ ");");
			}
			rs.close();
		}

	}

	/**
	 * 
	 * @param geneHomology
	 */
	public void loadDataFromMerge(GeneHomology geneHomology) {
		try {

			this.loadGeneHomology(geneHomology.getLocusTag(), geneHomology.getQuery(), geneHomology.getGene(), geneHomology.getChromossome(),
					geneHomology.getOrganelle(), geneHomology.getUniprot_star(), geneHomology.getStatus(), geneHomology.getUniprot_ecnumber());

			if (this.geneHomology_s_key != 0){
				this.loadFastaSequence(geneHomology.getSequence());

				if(geneHomology.getHomologues() !=null) {

					for(Homologues homologue : geneHomology.getHomologues()){

						Organism org = homologue.getOrg();
						this.loadOrganism(org);	


						try {
							loadHomologuesRemote(homologue);
						}
						catch (Exception e){
							System.out.println(homologue);
							System.out.println();
						}

						if(homologue.getEcNumber()!=null) {

							for (EcNumber ecNumber : homologue.getEcNumber()){
								try {

									loadECNumbers(ecNumber.getEcNumber(), homologue.getLocusID());
								}
								catch (Exception e) {

									System.out.println(homologue);
									System.out.println(ecNumber);
									//String s = homologue.getLocusID();
									//System.out.println(this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(s)).contains(141));
								}
							}		
						}

					}


					if (geneHomology.getEcNumber_rank() != null){

						for(EcNumberRank ecnumberrank : geneHomology.getEcNumber_rank()){
							List <Integer> ecRankOrg = new ArrayList<Integer>();
							//List <Organism> ecRankOrg = new ArrayList<Organism>();
							for (Organism org : ecnumberrank.getOrg()){
								int orgkey = this.loadedData.getOrgMap().get(org.getOrganism());
								ecRankOrg.add(orgkey);
								//ecRankOrg.add(org); before
							}

							loadECNumberRank(ecnumberrank.getEcNumber(), ecnumberrank.getRank(), ecRankOrg);
						}	
					}

					if (geneHomology.getProduct_rank() != null){

						for(ProductRank productrank : geneHomology.getProduct_rank()){
							List <Integer> prodRankOrg = new ArrayList<Integer>();

							//List <Organism> prodRankOrg = new ArrayList<Organism>();
							for (Organism org : productrank.getOrgList()){
								int orgkey = this.loadedData.getOrgMap().get(org.getOrganism());
								prodRankOrg.add(orgkey);
								//ecRankOrg.add(org); before
							}	
							loadProductRank(productrank.getProductName(), productrank.getRank(), prodRankOrg);
						}			
					}
				}
			}
			else{
				System.out.println("\nGene " + geneHomology.getQuery() + " already in the database \n");
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param geneid
	 * @param sequence
	 * @param homologuesHash
	 */
	public void loadDataFromLocalBlast(String geneid, FastaSequence sequence, LinkedHashMap<String, String[]> homologuesHash){

		try {

			String myOrganismTaxonomy= "cellular organisms";
			Map<String, Integer> pd = new HashMap<String,Integer>();
			Map<String, List<Integer>> productOrg = new HashMap<String,List<Integer>>();
			Map<Set<String>, Integer> ecn = new HashMap<Set<String>,Integer>();
			Map<Set<String>, List<Integer>> ecOrg = new HashMap<Set<String>,List<Integer>>();


			//############## Gene and sequence ###########


			this.loadGeneHomology(geneid, geneid, "", "","", -1, "PROCESSED", null);

			if (this.geneHomology_s_key != 0){
				this.loadFastaSequence(sequence);
			}

			for(String homologueID : homologuesHash.keySet()){

				//########## Organism #######
				String [] value= homologuesHash.get(homologueID);
				String organism = value[7];
				String taxonomy = value[8];

				//int taxRank = rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy);
				int taxRank = 1;
				Organism org = new Organism(organism,taxonomy, taxRank);
				this.loadOrganism(org);	


				//############ Homologues #########
				int uniprot_star;


				if (value[3].equals("Reviewed")){
					uniprot_star = 1;
				}
				else if (value[3].equals("Unreviewed")){
					uniprot_star = 0;
				}
				else{
					uniprot_star = -1;
				}

				String definition = value[5];
				String product = value[4];

				double mw =0;

				String gene = value[2];
				double score = Double.parseDouble(value[0]);
				double evalue = Double.parseDouble(value[1]);

				Homologues homologue = new Homologues(homologueID, definition, mw, product, null, uniprot_star, homologueID, gene, evalue, score);

				this.loadHomologuesLocal(homologue);

				//########### ecNumber ###############


				if (! value[6].isEmpty()){
					String ecnumber_concat = value[6];
					String [] ecnumbers = ecnumber_concat.split(";");
					Set<String> ecnumbersUniqueString = new HashSet<String>();

					for (int i = 0; i< ecnumbers.length ; i++){
						ecnumbers[i] = ecnumbers[i].substring(3);//take out EC:
						loadECNumbers(ecnumbers[i], homologueID);
						ecnumbersUniqueString.add(ecnumbers[i]);
					}

					//ecnumber_concat =ecnumber_concat.replaceAll("[;]", ",").substring(0, ecnumber_concat.length()-1);
					//ecnumber_concat =ecnumber_concat.replaceAll("EC=", " ").substring(1);


					//################ ecNumber Rank ############
					if(ecn.containsKey(ecnumbersUniqueString)) {

						ecn.put(ecnumbersUniqueString, ecn.get(ecnumbersUniqueString)+1);

						List<Integer> orgKey = ecOrg.get(ecnumbersUniqueString);	
						orgKey.add(this.loadedData.getOrgMap().get(organism));
						ecOrg.put(ecnumbersUniqueString, orgKey);
					}
					else{
						List<Integer> orgKey = new ArrayList<Integer>();
						ecn.put(ecnumbersUniqueString, 1);
						orgKey.add(this.loadedData.getOrgMap().get(organism));
						ecOrg.put(ecnumbersUniqueString, orgKey);
					}

				}

				// ################# product Rank ##############
				if(pd.containsKey(product)) {

					pd.put(product, pd.get(product)+1);

					List<Integer> orgKey = productOrg.get(product);	
					orgKey.add(this.loadedData.getOrgMap().get(organism));
					productOrg.put(product, orgKey);
				}
				else{
					List<Integer> orgKey = new ArrayList<Integer>();
					pd.put(product, 1);
					orgKey.add(this.loadedData.getOrgMap().get(organism));
					productOrg.put(product, orgKey);

				}

			}
			// ############# load productrank and ecnumberank ###########
			this.loadProductRankLocal(pd, productOrg);

			if (!ecn.isEmpty()) {

				this.loadECnumberRankLocal(ecn, ecOrg);		
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param geneid
	 * @param sequence
	 * @throws SQLException
	 */
	public void loadNosimilaritiesGenesFromLocalBlast(String geneid, FastaSequence sequence) throws SQLException{
		//############## Gene and sequence ###########

		this.loadGeneHomology(geneid, geneid, "", "","", -1, "NO_SIMILARITY", null);

		if (this.geneHomology_s_key != 0){
			this.loadFastaSequence(sequence);
		}
	}

}