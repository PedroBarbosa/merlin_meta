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
import java.util.concurrent.atomic.AtomicBoolean;


import pt.uminho.sysbio.merlin.utilities.DatabaseProgressStatus;
import alignment.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import pt.uminho.sysbio.merge.databases.containers.*;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import datatypes.Project;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;

/**
 * @author oDias
 *
 */
public class LoadSimilarityResultstoDatabaseLocal {

	private NCBIQBlastAlignmentProperties rqb;
	private HomologyDataClient homologyData;
	private Statement statement;
	private String homologySetupID, databaseID, version, program, query; 
	private HashMap<String, List<Integer>> prodOrg;
	private boolean loaded;
	private int maxNumberOfAlignments, organism_s_key, homologues_s_key, geneHomology_s_key,undo_geneHomology_s_key;
	private double eVal;
	private AtomicBoolean cancel;
	private LoadedData loadedData;



	/**
	 * @param hd
	 * @param rqb
	 * @param maxNumberOfAlignments
	 * @param statement
	 * @param cancel
	 */
	public LoadSimilarityResultstoDatabaseLocal(HomologyDataClient hd, NCBIQBlastAlignmentProperties rqb, int maxNumberOfAlignments, Statement statement, AtomicBoolean cancel) {

		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.rqb = rqb;
		this.homologyData = hd;
		this.program = hd.getProgram();
		this.version = hd.getVersion();
		this.databaseID = hd.getDatabaseID();
		this.query = hd.getQuery();
		this.setLoaded(true);
		this.statement = statement;
		this.cancel = cancel;
	}

	/**
	 * @param hd
	 * @param maxNumberOfAlignments
	 * @param eValue
	 * @param project
	 */
	public LoadSimilarityResultstoDatabaseLocal(HomologyDataClient hd, int maxNumberOfAlignments, double eValue, Statement statement, AtomicBoolean cancel) {

		this.eVal = eValue;
		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.homologyData = hd;
		this.program = hd.getProgram();
		this.version = hd.getVersion();
		this.databaseID = hd.getDatabaseID();
		this.query = hd.getQuery();
		this.setLoaded(true);
		this.statement = statement;
		this.cancel = cancel;
	}

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

	//	public LoadSimilarityResultstoDatabase(LocalBlast blastlocalHash){
	//		this.localblast = blastlocalHash;
	//
	//	}

	/**
	 * @param homologyData
	 * @param rqb
	 * @param alignmentNumber
	 * @param project
	 * @param cancel
	 * @return 
	 * @throws SQLException 
	 */
	public LoadSimilarityResultstoDatabaseLocal (HomologyDataClient homologyData, NCBIQBlastAlignmentProperties rqb, int alignmentNumber,
			Project project, AtomicBoolean cancel) throws SQLException {

		Statement stmt = project.getDatabase().getMySqlCredentials().openConnection().createStatement();

		new LoadSimilarityResultstoDatabaseLocal(homologyData, rqb, alignmentNumber, stmt, cancel);
	}

	/**
	 * @param homologyData
	 * @param numberOfAlignments
	 * @param eValue
	 * @param project
	 * @param cancel
	 * @throws SQLException 
	 */
	public LoadSimilarityResultstoDatabaseLocal(HomologyDataClient homologyData, int numberOfAlignments, double eValue, Project project,
			AtomicBoolean cancel) throws SQLException {

		Statement stmt = project.getDatabase().getMySqlCredentials().openConnection().createStatement();

		new LoadSimilarityResultstoDatabaseLocal(homologyData, numberOfAlignments, eValue, stmt, cancel);

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
	 * @param locusTag
	 * @param query
	 * @param gene
	 * @param chromosome
	 * @param organelle
	 * @throws SQLException
	 */
	private void loadGeneHomologyRemote(String locusTag, String query, String gene, String chromosome, String organelle, String uniprot_star) throws SQLException {

		this.undo_geneHomology_s_key =-1;
		int uniprot_star_int = -1;

		if(uniprot_star!= null) {

			uniprot_star_int = MySQL_Utilities.get_boolean_int(uniprot_star);
		}


		if(this.homologyData.isNoSimilarity()) {

			ResultSet rs = this.statement.executeQuery("SELECT * FROM geneHomology WHERE query = '"+MySQL_Utilities.mysqlStrConverter(query)+"' AND homologySetup_s_key = " +this.homologySetupID);
			if(!rs.next()) {

				this.statement.execute("INSERT INTO geneHomology (locusTag, query, homologySetup_s_key, status, uniprot_star, uniprot_ecnumber) VALUES ('"
						+ locusTag +"','"+ MySQL_Utilities.mysqlStrConverter(query) + "','" +this.homologySetupID+ "','"+DatabaseProgressStatus.NO_SIMILARITY+"','"+uniprot_star_int+"','"+this.homologyData.getUniprot_ecnumber()+"');");

				rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}
			this.geneHomology_s_key = rs.getInt(1);
			rs.close();
		}
		else {

			ResultSet rs = this.statement.executeQuery("SELECT * FROM geneHomology WHERE query = '"+MySQL_Utilities.mysqlStrConverter(query)+"' AND homologySetup_s_key = " +this.homologySetupID);
			if(rs.next()) {

				this.geneHomology_s_key = rs.getInt(1);
				this.undo_geneHomology_s_key = this.geneHomology_s_key;
				this.statement.execute("UPDATE geneHomology " +
						"SET locusTag='"+ locusTag +"', " +
						"homologySetup_s_key = '" +this.homologySetupID+ "'," +
						"gene = '" +MySQL_Utilities.mysqlStrConverter(gene)+ "'," +
						"chromosome = '" +chromosome+ "'," +
						"organelle = '" +organelle+ "'," +
						"uniprot_star = '" +uniprot_star_int+ "'," +
						"status = '"+DatabaseProgressStatus.PROCESSING+"'" +
						"WHERE query = '"+MySQL_Utilities.mysqlStrConverter(query)+"';");
				rs.close();
			}
			else {

				this.statement.execute("INSERT INTO geneHomology (locusTag, query, gene, chromosome, organelle, uniprot_star, homologySetup_s_key, status, uniprot_ecnumber) VALUES ('"
						+ locusTag +"','"+ MySQL_Utilities.mysqlStrConverter(query) + "','"+ MySQL_Utilities.mysqlStrConverter(gene) + "','" + chromosome + "','"+ organelle + 
						"'," +uniprot_star_int + ",'" +this.homologySetupID+ "','"+DatabaseProgressStatus.PROCESSING+"','"+this.homologyData.getUniprot_ecnumber()+"');");
				rs =this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.geneHomology_s_key = rs.getInt(1);
				this.undo_geneHomology_s_key = this.geneHomology_s_key;
				rs.close();
			}
		}

	}

	/**
	 * @param locusTag
	 */
	private void updataGeneStatus(String locusTag) {

		try {

			this.statement.execute("UPDATE geneHomology SET status ='"+DatabaseProgressStatus.PROCESSED+"' WHERE locusTag ='" + locusTag +"';");
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
	}


	/**
	 * @param organism
	 * @param taxonomy
	 * @param myOrganismTaxonomy
	 * @throws SQLException
	 */
	@Deprecated
	private void loadOrganism(String organism, String taxonomy, String myOrganismTaxonomy, String locus) throws NullPointerException {

		try {
			if(organism==null) {

				System.out.println("LoadBlastResultstoDatabase null pointer locus:"+locus+"\t\torganism:\t"+organism+"\t\tmyOrgTax:\t"+myOrganismTaxonomy);
				organism = "unknown";
			}

			if(taxonomy==null) {

				System.out.println("LoadBlastResultstoDatabase null pointer locus:"+locus+"\t\tttax:\t"+taxonomy+"\t\tmyOrgTax:\t"+myOrganismTaxonomy);
				taxonomy = "unknown";
			}

			ResultSet rs =this.statement.executeQuery("SELECT * FROM organism where organism = '"+MySQL_Utilities.mysqlStrConverter(organism)+"';");

			if(rs.next()) {

				this.organism_s_key = rs.getInt(1);
				//this.undo_organism_s_key="";
			}
			else {

				this.statement.execute("INSERT INTO organism (organism, taxonomy, taxrank) VALUES ('"
						+ MySQL_Utilities.mysqlStrConverter(organism) +"','"+ MySQL_Utilities.mysqlStrConverter(taxonomy) +"','"+this.rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy)+"');");
				rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.organism_s_key = rs.getInt(1);
				//this.undo_organism_s_key=this.organism_s_key;
			}
		}
		catch (NullPointerException e) {

			System.out.println(this.homologyData.getOrganism());
			System.out.println();
			System.out.println("LoadBlastResultstoDatabase null pointer locus:"+locus+"\t\torganism:\t"+organism+"\t\ttax:\t"+taxonomy+"\t\tmyOrgTax:\t"+myOrganismTaxonomy);
			//e.printStackTrace();
			throw e;
		}
		catch (SQLException e) {

			System.out.println(this.homologyData.getOrganism());
			System.out.println("LoadBlastResultstoDatabase sql organism "+organism+" tax "+taxonomy+" myOrgTax "+myOrganismTaxonomy);
			//e.printStackTrace();
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
	 * @param referenceID
	 * @param gene
	 * @param eValue
	 * @param bits
	 * @throws SQLException
	 */	
	@Deprecated
	private void loadHomology(String referenceID, String gene, double eValue, double bits) {

		try {

			if(gene!=null) {

				gene=MySQL_Utilities.mysqlStrConverter(gene);

				ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' " +
						"AND homologues_s_key = '"+ this.homologues_s_key+"' AND referenceID='"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"' AND gene='"+ gene +"' AND eValue='"+ eValue +"' AND bits='"+ bits +"'");

				if(!rs.next()) {

					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key,homologues_s_key, referenceID, gene ,eValue, bits) VALUES('"+this.geneHomology_s_key +"','"+ 
							this.homologues_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"','"+ gene +"','"+ eValue +"','"+ bits +"');");
				}
			}
			else {

				ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' " +
						"AND homologues_s_key = '"+ this.homologues_s_key+"' AND referenceID='"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"'AND eValue='"+ eValue +"' AND bits='"+ bits +"'");

				if(!rs.next()) {


					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key,homologues_s_key, referenceID ,eValue, bits) VALUES('"+this.geneHomology_s_key +"','"+ 
							this.homologues_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"','"+ eValue +"','"+ bits +"');");
				}
			}
		}
		catch (SQLException e) {

			e.printStackTrace();
		}


	}

	/**
	 * @param homologyDataTableContainer
	 * @param homology
	 * @throws SQLException
	 */@Deprecated
	 private  void loadHomologuesRemoteMergeOld(HomologyDataTableContainer homologyDataTableContainer, Homology homology, String organismm) throws SQLException {
		 System.out.println(this.statement.getConnection().isValid(3));

		 String product = homologyDataTableContainer.getProduct();
		 if(homologyDataTableContainer.getProduct()==null) {

			 product =homologyDataTableContainer.getDefinition();
		 }

		 int homologues_s_key = 0;
		 synchronized( this.loadedData.getHomologuesMap()) {


			 if (this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){

				 homologues_s_key = this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID());
			 }

			 else{
				 this.statement.execute("INSERT INTO homologues (locusID," +
						 " organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
						 "VALUES ('"+ homologyDataTableContainer.getLocusID() +"','"+this.loadedData.getOrgMap().get(organismm) +"','"+MySQL_Utilities.mysqlStrConverter(homologyDataTableContainer.getDefinition()) + "','"+ homologyDataTableContainer.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
						 "'"+ homologyDataTableContainer.getOrganelle() +"',"+homologyDataTableContainer.getUniprot_star()+")");

				 ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				 rs.next();

				 homologues_s_key = rs.getInt(1);
				 rs.close();

				 this.loadedData.getHomologuesMap().put(homologyDataTableContainer.getLocusID(), homologues_s_key);
			 }
		 }

		 if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key) && this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(homologues_s_key)){
		 }
		 else{

			 this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
					 "VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
					 ",'"+ homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");

			 if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){

				 this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(homologues_s_key);
			 }
			 else{
				 Set <Integer> setHomologues = new HashSet<>();
				 setHomologues.add( this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
				 this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);	
			 }

		 }




		 //		synchronized( this.loadedData.getHomologuesMap()) {
		 //
		 //		if (this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //			if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
		 //				if(!this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()))){
		 //
		 //					
		 //					
		 //					
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
		 //				}
		 //			}
		 //			else{
		 //				if (! this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){	
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+ homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					Set <Integer> setHomologues = new HashSet<>();
		 //					setHomologues.add( this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
		 //					this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);		
		 //				}
		 //			}
		 //		}
		 //		else{
		 //			if (! this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //
		 //				if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
		 //					this.statement.execute("INSERT INTO homologues (locusID," +
		 //							" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
		 //							"VALUES ('"+ homologyDataTableContainer.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologyDataTableContainer.getDefinition()) + "','"+ homologyDataTableContainer.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
		 //							"'"+ homologyDataTableContainer.getOrganelle() +"',"+homologyDataTableContainer.getUniprot_star()+")");
		 //
		 //					ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
		 //					rs.next();
		 //
		 //					int homologues_s_key = rs.getInt(1);
		 //					rs.close();
		 //
		 //					this.loadedData.getHomologuesMap().put(homologyDataTableContainer.getLocusID(), homologues_s_key);
		 //
		 //
		 //
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+homologues_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+ homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(homologues_s_key);
		 //				}
		 //				else{
		 //					if (! this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
		 //						
		 //					this.statement.execute("INSERT INTO homologues (locusID," +
		 //							" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
		 //							"VALUES ('"+ homologyDataTableContainer.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologyDataTableContainer.getDefinition()) + "','"+ homologyDataTableContainer.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
		 //							"'"+ homologyDataTableContainer.getOrganelle() +"',"+homologyDataTableContainer.getUniprot_star()+")");
		 //
		 //					ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
		 //					rs.next();
		 //
		 //					int homologuesss_s_key = rs.getInt(1);
		 //					rs.close();
		 //
		 //					this.loadedData.getHomologuesMap().put(homologyDataTableContainer.getLocusID(), homologuesss_s_key);
		 //
		 //
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+homologuesss_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					Set <Integer> setHomologues = new HashSet<>();
		 //					setHomologues.add(homologuesss_s_key);
		 //					this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);
		 //				}
		 //			}
		 //		 }
		 //		}
		 //	}


		 //		if (this.loadedData.getGeneToHomologuesMap().containsKey(this.geneHomology_s_key)){
		 //
		 //			if (this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //
		 //				if(!this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()))) {
		 //
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
		 //				}
		 //			}
		 //
		 //			else{
		 //				if (! this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //					this.statement.execute("INSERT INTO homologues (locusID," +
		 //							" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
		 //							"VALUES ('"+ homologyDataTableContainer.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologyDataTableContainer.getDefinition()) + "','"+ homologyDataTableContainer.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
		 //							"'"+ homologyDataTableContainer.getOrganelle() +"',"+homologyDataTableContainer.getUniprot_star()+")");
		 //
		 //					ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
		 //					rs.next();
		 //
		 //					int homologuess_s_key = rs.getInt(1);
		 //					rs.close();
		 //
		 //					this.loadedData.getHomologuesMap().put(homologyDataTableContainer.getLocusID(), homologuess_s_key);
		 //
		 //					if(!this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).contains(this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()))) {
		 //
		 //						this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //								"VALUES('"+this.geneHomology_s_key+"','"+homologuess_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //								",'"+ homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //						this.loadedData.getGeneToHomologuesMap().get(this.geneHomology_s_key).add(homologuess_s_key);
		 //					}
		 //				}
		 //
		 //			}
		 //		}
		 //
		 //		else{
		 //
		 //			if (this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //
		 //				this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //						"VALUES('"+this.geneHomology_s_key+"','"+this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()) +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //						",'"+ homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //				Set <Integer> setHomologues = new HashSet<>();
		 //				setHomologues.add( this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
		 //				this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);		
		 //			}
		 //
		 //			else{
		 //				if (! this.loadedData.getHomologuesMap().containsKey(homologyDataTableContainer.getLocusID())){
		 //					
		 //					this.statement.execute("INSERT INTO homologues (locusID," +
		 //							" organism_s_key, definition, calculated_mw, product, organelle, uniprot_star) " +
		 //							"VALUES ('"+ homologyDataTableContainer.getLocusID() +"','"+this.organism_s_key +"','"+MySQL_Utilities.mysqlStrConverter(homologyDataTableContainer.getDefinition()) + "','"+ homologyDataTableContainer.getCalculated_mw() +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
		 //							"'"+ homologyDataTableContainer.getOrganelle() +"',"+homologyDataTableContainer.getUniprot_star()+")");
		 //
		 //					ResultSet rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
		 //					rs.next();
		 //
		 //					int homologuesss_s_key = rs.getInt(1);
		 //					rs.close();
		 //
		 //					this.loadedData.getHomologuesMap().put(homologyDataTableContainer.getLocusID(), homologuesss_s_key);
		 //
		 //
		 //					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, homologues_s_key, referenceID, gene, eValue, bits) " +
		 //							"VALUES('"+this.geneHomology_s_key+"','"+homologuesss_s_key +"','"+ MySQL_Utilities.mysqlStrConverter(homology.getReferenceID())+"'" +
		 //							",'"+homology.getGene()+"','"+ homology.geteValue()+"','"+ homology.getBits()+"');");
		 //
		 //					Set <Integer> setHomologues = new HashSet<>();
		 //					setHomologues.add(homologuesss_s_key);
		 //					this.loadedData.getGeneToHomologuesMap().put(this.geneHomology_s_key, setHomologues);
		 //				}
		 //			
		 //			}
		 //		}

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
	  * @param locusID
	  * @param definition
	  * @param calculated_mw
	  * @param product
	  * @param pd
	  * @param organelle
	  * @return
	  * @throws SQLException 
	  */
	 private Map<String, Integer> loadHomologuesRemoteOld(String locusID, String definition, double calculated_mw, String product, Map<String, Integer> pd, String organelle) throws SQLException {

		 //definition = definition.replaceAll("\\","");
		 //definition = MySQL_Utilities.mysqlStrConverter(definition);
		 if(product==null) {

			 product=definition;
		 }
		 else {

			 //			product = product.replaceAll("\\","");
			 //			product = MySQL_Utilities.mysqlStrConverter(product);
		 }

		 //this.statement.execute("LOCK tables homologyData read;");


		 ResultSet rs;
		 try {
			 rs = this.statement.executeQuery("SELECT * FROM homologues WHERE organism_s_key = '"+this.organism_s_key+"' AND locusID='"+ MySQL_Utilities.mysqlStrConverter(locusID) +"' " +
					 "AND definition='"+ MySQL_Utilities.mysqlStrConverter(definition) +"' " +
					 "AND calculated_mw= "+ calculated_mw +" " +
					 "AND product='"+ MySQL_Utilities.mysqlStrConverter(product) +"' " +
					 "AND organelle='"+ MySQL_Utilities.mysqlStrConverter(organelle) +"'");


		 } 
		 catch (SQLException e1) {

			 System.out.println("SELECT * FROM homologues WHERE organism_s_key = '"+this.organism_s_key+"' AND locusID='"+ MySQL_Utilities.mysqlStrConverter(locusID) +"' " +
					 "AND definition='"+ MySQL_Utilities.mysqlStrConverter(definition) +"' " +
					 "AND calculated_mw= "+ calculated_mw +" " +
					 "AND product='"+ MySQL_Utilities.mysqlStrConverter(product) +"' " +
					 "AND organelle='"+ MySQL_Utilities.mysqlStrConverter(organelle) +"'");
			 throw new SQLException();
		 }

		 if(!rs.next()) {

			 int uniprot_star_int = -1;
			 String star = null;

			 //System.out.println(this.homologyData.getUniprotStar());

			 if(this.homologyData.getUniprotStar().containsKey(locusID)) {

				 star = this.homologyData.getUniprotStar().get(locusID).toString();
			 }
			 else {

				 try {

					 star =  UniProtAPI.isStarred(locusID)+"";
				 }
				 catch(Exception e) {

					 star = null;
				 }
				 catch(Error e) {

					 star = null;
				 }

				 if(star!= null) {

					 uniprot_star_int = MySQL_Utilities.get_boolean_int(star);
				 }
			 }

			 this.statement.execute("INSERT INTO homologues ( organism_s_key, locusID," +
					 " definition, calculated_mw, product, organelle, uniprot_star) " +
					 "VALUES ('"+ this.organism_s_key+"','"+ locusID +"','"+
					 MySQL_Utilities.mysqlStrConverter(definition) + "','"+ calculated_mw +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +
					 "'"+ organelle +"',"+uniprot_star_int+")");
			 rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			 rs.next();

			 this.homologues_s_key = rs.getInt(1);
		 }

		 if(pd.containsKey(product)) {

			 pd.put(product, pd.get(product)+1);
			 List<Integer> orgKey = this.prodOrg.get(product);
			 //			if(!orgKey.contains(this.organism_s_key))
			 //			{
			 orgKey.add(this.organism_s_key);
			 //			}
			 this.prodOrg.put(product, orgKey);
		 }
		 else {

			 pd.put(product, 1);
			 List<Integer> orgKey = new ArrayList<Integer>();
			 orgKey.add(this.organism_s_key);
			 this.prodOrg.put(product, orgKey);
		 }

		 //this.statement.execute("UNLOCK tables;");
		 return pd;
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
	  * @param homologue_s_key
	  * @throws SQLException
	  */@Deprecated
	  private void loadECNumbers(String ecNumber, int homologue_s_key) throws SQLException{
		  ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumber WHERE ecNumber = '"+ecNumber+"'");
		  int ecNumber_s_key;
		  if(!rs.next()) {
			  this.statement.execute( "INSERT INTO ecNUmber (ecNumber) VALUES ('"+ecNumber+"');");

			  rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			  rs.next();
			  ecNumber_s_key = rs.getInt(1);
		  }
		  else{
			  ecNumber_s_key = rs.getInt(1);
		  }

		  rs = this.statement.executeQuery("SELECT * FROM homologues_has_ecNumber WHERE homologues_s_key = '"+homologue_s_key+"' AND " +
				  "ecNumber_s_key = '"+ecNumber_s_key+"'");

		  if (!rs.next()){
			  this.statement.execute( "INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) VALUES ('"+homologue_s_key+"'" +
					  ",'"+ecNumber_s_key+"');");
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
	   * @param databaseID
	   * @param program
	   * @param version
	   * @throws SQLException
	   */@Deprecated
	   private void loadHomologySetupRemote(String databaseID, String program, String version) throws SQLException{

		   //this.statement.execute("LOCK tables homologySetup read;");
		   ResultSet rs = this.statement.executeQuery("SELECT * FROM homologySetup " +
				   "WHERE databaseID = '"+databaseID+"' " +
				   "AND program='"+program+"' " +
				   "AND eValue='"+this.rqb.getBlastExpect()+"' " +
				   "AND matrix='"+this.rqb.getBlastMatrix()+"' " +
				   "AND wordSize='"+this.rqb.getBlastWordSize()+"' " +
				   "AND gapCosts='"+this.rqb.getBlastGapCosts()+"' " +
				   "AND maxNumberOfAlignments='"+this.maxNumberOfAlignments+"' " +
				   "AND version='"+version+"'");

		   if(!rs.next()) {

			   this.statement.execute("INSERT INTO homologySetup (databaseID, program, version, eValue, matrix, wordSize, gapCosts, maxNumberOfAlignments) " +
					   "VALUES ('"+ databaseID +"','"+ program +"','"+version +"','"+this.rqb.getBlastExpect() +"','"+this.rqb.getBlastMatrix() +"'," +
					   "'"+this.rqb.getBlastWordSize() +"','"+this.rqb.getBlastGapCosts() +"','"+this.maxNumberOfAlignments +"');");
			   rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			   rs.next();
		   }
		   this.homologySetupID = rs.getString(1);
		   rs.close();
		   //this.statement.execute("UNLOCK tables;");
	   }

	   /**
	    * @param databaseID
	    * @param program
	    * @param version
	    * @throws SQLException
	    */
	   private void loadHmmerSetup(String databaseID, String program, String version) throws SQLException{

		   //this.statement.execute("LOCK tables homologySetup read;");

		   ResultSet rs = this.statement.executeQuery("SELECT * FROM homologySetup " +
				   "WHERE databaseID = '"+databaseID+"' " +
				   "AND program='"+program+"' " +
				   "AND eValue='"+this.eVal+"' " +
				   "AND maxNumberOfAlignments='"+this.maxNumberOfAlignments+"' " +
				   "AND version='"+version+"'");
		   if(!rs.next())
		   {

			   this.statement.execute("INSERT INTO homologySetup (databaseID, program, version, eValue, maxNumberOfAlignments) " +
					   "VALUES ('"+ databaseID +"','"+ program +"','"+version +"','"+this.eVal +"','"+this.maxNumberOfAlignments +"');");
			   rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			   rs.next();
		   }
		   this.homologySetupID = rs.getString(1);
		   rs.close();
		   //this.statement.execute("UNLOCK tables;");
	   }

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
	    * @param pd
	    * @throws SQLException
	    */@Deprecated
	    private void loadProductRank(Map<String, Integer> pd) throws SQLException {

	    	//this.statement.execute("LOCK tables productRank read, productRank_has_organism read;");

	    	for(String product:pd.keySet()) {

	    		ResultSet rs = this.statement.executeQuery("SELECT * FROM productRank WHERE geneHomology_s_key = "+this.geneHomology_s_key+" AND " +
	    				"productName = '"+MySQL_Utilities.mysqlStrConverter(product)+"' AND rank = '"+pd.get(product)+"'");

	    		if(!rs.next()) {

	    			this.statement.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) " +
	    					"VALUES("+ this.geneHomology_s_key +",'"+ MySQL_Utilities.mysqlStrConverter(product) + "','" +pd.get(product)+ "');");

	    			rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
	    			rs.next();
	    			int prodkey = rs.getInt(1);

	    			for(int orgKey : this.prodOrg.get(product)) {

	    				this.statement.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES("+prodkey + "," + orgKey+ ");");
	    			}
	    		}
	    		rs.close();
	    	}
	    	//this.statement.execute("UNLOCK tables;");
	    }

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
	     * @param ecn
	     * @param ecOrg
	     * @throws SQLException
	     */@Deprecated
	     private void loadECNumberRank(Map<Set<String>, Integer> ecn, Map<Set<String>, List<Integer>> ecOrg) throws SQLException {

	    	 //this.statement.execute("LOCK tables ecNumberRank read, ecNumberRank_has_organism read;");

	    	 for(Set<String> ecnumber: ecn.keySet()) {

	    		 String concatEC="";
	    		 for(String s:ecnumber) {

	    			 if(concatEC.isEmpty())
	    				 concatEC = s;
	    			 else
	    				 concatEC += ", " + s;
	    		 }

	    		 ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumberRank " +
	    				 "WHERE geneHomology_s_key = '"+this.geneHomology_s_key+"' AND " +
	    				 "ecNumber = '"+concatEC+"' AND rank = '"+ecn.get(ecnumber)+"'");

	    		 if(!rs.next()) {

	    			 this.statement.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) " +
	    					 "VALUES('"+ this.geneHomology_s_key +"','"+ concatEC + "','" +ecn.get(ecnumber)+ "');");
	    			 rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");

	    			 if(rs.next()) {
	    				 String eckey = rs.getString(1);

	    				 for(int orgKey : ecOrg.get(ecnumber)) {

	    					 this.statement.execute("INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES('"+ eckey + "','" + orgKey+ "');");
	    				 }
	    			 }
	    		 }
	    		 rs.close();
	    	 }

	    	 //this.statement.execute("UNLOCK tables;");
	     }

	     /**
	      * @param geneblastT
	      */
	     @Deprecated
	     public void loadDataRemote(GeneBlast geneblastT){

	    	 try {
	    		// if(geneblastT.getQuery().equals("gene_id_7707")){
	    			 this.loadGeneHomology(geneblastT.getLocusTag(), geneblastT.getQuery(), geneblastT.getGene(), geneblastT.getChromossome(),
	    					 geneblastT.getOrganelle(), geneblastT.getUniprot_star(), geneblastT.getStatus(), geneblastT.getUniprot_ecnumber());

	    			 if (this.geneHomology_s_key != 0){
	    				 this.loadFastaSequence(geneblastT.getSequence());



	    				 if(geneblastT.getHomology()!=null) {

	    					 //startTime = System.currentTimeMillis();

	    					 for(Homology homology : geneblastT.getHomology()){

	    						 Organism org = homology.getOrg();
	    						 this.loadOrganism(org);	
	    						 //System.out.println(this.loadedData.getOrgMap().size());


	    						 HomologyDataTableContainer homologyDataTableContainer = homology.getHomologyData();

	    						 if(homologyDataTableContainer.getLocusID().equals("AAP42186")){

	    							 System.out.println("Gene :" + geneblastT.getQuery());
	    							 System.out.println("LocusID: " + homologyDataTableContainer.getLocusID());
	    							 System.out.println("LocusKey: " + this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
	    							 System.out.println("Org: " + org.getOrganism());
	    							 System.out.println("Orgkey: " + this.loadedData.getOrgMap().get(org.getOrganism()));
	    							 loadHomologuesRemoteMergeOld(homologyDataTableContainer, homology, org.getOrganism());
	    						 }
	    						 else{
	    							 try {
	    								 loadHomologuesRemoteMergeOld(homologyDataTableContainer, homology, org.getOrganism());
	    							 }
	    							 catch (Exception e){
	    								 System.out.println("Gene :" + geneblastT.getQuery());
	    								 System.out.println("LocusID: " + homologyDataTableContainer.getLocusID());
	    								 System.out.println("LocusKey: " + this.loadedData.getHomologuesMap().get(homologyDataTableContainer.getLocusID()));
	    								 System.out.println("Org: " + org.getOrganism());
	    								 System.out.println("Orgkey: " + this.loadedData.getOrgMap().get(org.getOrganism()));
	    								 System.out.println("\n"+homology);
	    								 System.out.println(e.getMessage());
	    								 System.out.println(e);
	    							 }
	    						 }



	    						 if(homology.getEcNumber()!=null) {

	    							 for (EcNumber ecNumber : homology.getEcNumber()){
	    								 try {

	    									 loadECNumbers(ecNumber.getEcNumber(), homologyDataTableContainer.getLocusID());
	    								 }
	    								 catch (Exception e) {

	    									 System.out.println("LocusID: " + homologyDataTableContainer.getLocusID());
	    									 System.out.println("EC number: " +ecNumber.getEcNumber());
	    									 String s = homologyDataTableContainer.getLocusID();
	    									 System.out.println(this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(s)));
	    								 }
	    							 }		
	    						 }





	    						 //						endTime = System.currentTimeMillis();
	    						 //						System.out.println("\t\tTotal elapsed time in execution of loadHomologuesNEW is :"+ String.format("%d min, %d sec", 
	    						 //								TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
	    						 //								-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

	    					 }

	    					 //					endTime = System.currentTimeMillis();
	    					 //					System.out.println("\t\tTotal elapsed time in execution of geneHomology.getHomology() is :"+ String.format("%d min, %d sec", 
	    					 //							TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
	    					 //							-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));





	    					 if (geneblastT.getEcNumber_rank() != null){

	    						 for(EcNumberRank ecnumberrank : geneblastT.getEcNumber_rank()){
	    							 List <Integer> ecRankOrg = new ArrayList<Integer>();

	    							 for (Organism org : ecnumberrank.getOrg()){
	    								 int orgkey = this.loadedData.getOrgMap().get(org.getOrganism());
	    								 ecRankOrg.add(orgkey);
	    							 }
	    							 loadECNumberRank(ecnumberrank.getEcNumber(), ecnumberrank.getRank(), ecRankOrg);
	    						 }	
	    					 }



	    					 if (geneblastT.getProduct_rank() != null){

	    						 for(ProductRank productrank : geneblastT.getProduct_rank()){
	    							 List <Integer> prodRankOrg = new ArrayList<Integer>();
	    							 for (Organism org : productrank.getOrgList()){
	    								 int orgkey = this.loadedData.getOrgMap().get(org.getOrganism());
	    								 prodRankOrg.add(orgkey);
	    							 }	
	    							 loadProductRank(productrank.getProductName(), productrank.getRank(), prodRankOrg);
	    						 }			
	    					 }
	    				 }
	    			 }
	    			 else{
	    				 System.out.println("\nGene " + geneblastT.getQuery() + " already in the database \n");
	    			 }
	    		 }
	    	 //}
	    	 catch (Exception e) {

	    		 e.printStackTrace();
	    	 }
	     }

	     /**
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
	    								 String s = homologue.getLocusID();
	    								 System.out.println(this.loadedData.getEcNumberToHomologues().get(this.loadedData.getHomologuesMap().get(s)).contains(141));
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


	    		 this.loadGeneHomology(geneid, geneid, "", null,"", -1, "PROCESSED", null);

	    		 if (this.geneHomology_s_key != 0){
	    			 this.loadFastaSequence(sequence);
	    		 }

	    		 for(String homologueID : homologuesHash.keySet()){

	    			 //########## Organism #######
	    			 String [] value= homologuesHash.get(homologueID);
	    			 String organism = value[7];
	    			 String taxonomy = value[8];

	    			 int taxRank = rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy);

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
	      * @param geneid
	      * @param sequence
	      * @throws SQLException
	      */
	     public void loadNosimilaritiesGenesFromLocalBlast(String geneid, FastaSequence sequence) throws SQLException{
	    	 //############## Gene and sequence ###########

	    	 this.loadGeneHomology(geneid, geneid, "", null,"", -1, "NO_SIMILARITY", null);

	    	 if (this.geneHomology_s_key != 0){
	    		 this.loadFastaSequence(sequence);
	    	 }
	     }

//	     /**
//	      * @throws SQLException
//	      */
//	     public void loadDataFromRemote() {
//
//	    	 try {
//
//	    		 String program = "hmmer";
//	    		 if(this.rqb != null) { 
//
//	    			 program = this.homologyData.getProgram();
//	    		 }
//
//	    		 ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology " +
//	    				 "INNER JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
//	    				 "WHERE query = '"+this.homologyData.getQuery()+"' " +
//	    				 "AND program = '"+program+"' ");
//
//	    		 if(rs.next()) {
//
//	    			 System.out.println(this.homologyData.getLocus_tag()+"\tGENE already processed!");
//	    		 } 
//	    		 else {
//
//	    			 String star;
//	    			 if(this.homologyData.getQuery() != null && this.homologyData.getUniprotStar()!=null && this.homologyData.getUniprotStar().containsKey(this.homologyData.getQuery())) {
//
//	    				 star = this.homologyData.getUniprotStar().get(this.homologyData.getQuery()).toString();
//	    			 }
//	    			 else {
//
//	    				 try {
//
//	    					 star =  this.homologyData.isUniProtStarred()+"";
//	    				 }
//	    				 catch(NullPointerException e) {
//
//	    					 star = null;
//	    				 }
//	    			 }
//
//	    			 if(this.rqb != null) {
//	    				 this.loadHomologySetup(this.databaseID, this.program, this.version, Double.toString(this.rqb.getBlastExpect()), this.rqb.getBlastMatrix(), 
//	    						 Integer.toString(this.rqb.getBlastWordSize()), this.rqb.getBlastGapCosts(), this.maxNumberOfAlignments);
//	    				 //this.loadHomologySetupRemote(this.databaseID, this.program, this.version);
//	    			 }
//	    			 else {
//
//	    				 this.loadHmmerSetup(this.databaseID, this.program, this.version);
//	    			 }	
//
//	    			 if(this.homologyData.isNoSimilarity()) {
//
//	    				 String locusTag = this.query;
//	    				 if(this.homologyData.getLocus_tag() != null) {
//
//	    					 locusTag = this.homologyData.getLocus_tag();
//	    				 }
//
//	    				 this.loadGeneHomologyRemote(locusTag, this.query, null, null, null, star);
//	    				 this.loadFastaSequence(this.homologyData.getFastaSequence());
//	    			 }
//	    			 else {
//
//	    				 String locusTag;
//
//	    				 if(this.homologyData.getLocus_tag()==null) {
//
//	    					 if(this.homologyData.getLocus_gene_note()==null) {
//
//	    						 locusTag=this.homologyData.getLocus_protein_note();	
//	    					 }
//	    					 else {
//
//	    						 if(this.homologyData.getLocusID().getFirst().matches("[A-Za-z]*\\d*\\s+")
//	    								 && !this.homologyData.getLocus_tag().contains(":") )//if the locus tag contains spaces and not: 
//	    						 {
//
//	    							 locusTag=this.homologyData.getLocusID().getFirst();
//	    						 }
//	    						 else {
//
//	    							 String[] locus = this.homologyData.getLocus_gene_note().split(":");
//	    							 locusTag = locus[locus.length-1];
//	    							 locus = locusTag.split(";");
//	    							 locusTag = locus[0];
//	    						 }
//	    					 }
//	    				 }
//	    				 else {
//
//	    					 locusTag=this.homologyData.getLocus_tag();
//	    				 }
//
//
//	    				 this.loadGeneHomologyRemote(locusTag, this.query, this.homologyData.getGene(), this.homologyData.getChromossome(), this.homologyData.getOrganelle(), star);
//	    				 this.loadFastaSequence(this.homologyData.getFastaSequence());
//	    				 Map<String, Integer> pd = new HashMap<String,Integer>();
//	    				 this.prodOrg = new HashMap<String,List<Integer>>();
//	    				 Map<Set<String>, Integer> ecn = new HashMap<Set<String>,Integer>();
//	    				 Map<Set<String>, List<Integer>> ecOrg = new HashMap<Set<String>,List<Integer>>();
//	    				 boolean first=true;
//	    				 String myOrganismTaxonomy="";
//
//	    				 if(!this.homologyData.isNCBIGenome()) {
//
//	    					 myOrganismTaxonomy = this.homologyData.getTaxID()[1].concat("; "+this.homologyData.getTaxID()[0]);
//	    					 int rankTaxo = rankTaxonomy(this.homologyData.getTaxID()[1].concat("; "+this.homologyData.getTaxID()[0]), myOrganismTaxonomy);
//	    					 Organism organismObj = new Organism(this.homologyData.getTaxID()[0], this.homologyData.getTaxID()[1], rankTaxo);
//	    					 this.loadOrganism(organismObj);
//	    					 first=false;
//	    				 }
//
//	    				 for(String locus:this.homologyData.getLocusID()) {
//
//	    					 //System.out.println(this.homologyData.getLocusID().size());
//	    					 //System.out.println(this.homologyData.getOrganism().size());
//
//	    					 if(!this.cancel.get()) {
//
//	    						 String organism = this.homologyData.getOrganism().get(locus),
//	    								 taxonomy =	this.homologyData.getTaxonomy().get(locus);
//
//	    						 if(first) {//if isNCBIgenome
//
//	    							 myOrganismTaxonomy = this.homologyData.getTaxonomy().get(locus).concat("; "+this.homologyData.getOrganism().get(locus));
//	    							 first=false;
//	    							 Organism organismObj = new Organism(organism, taxonomy, rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy));
//	    							 this.loadOrganism(organismObj);
//	    							 //this.loadOrganism(organism,taxonomy,myOrganismTaxonomy,locus);
//	    							 //this.loadHomology(locus, this.homologyData.getGenes().get(locus), 0, 0.0);
//	    						 }
//	    						 else {
//
//	    							 //							System.out.println(locus);
//	    							 //							System.out.println(this.homologyData.getOrganism().get(locus));
//	    							 //							System.out.println(this.homologyData.getTaxonomy().get(locus));
//	    							 //							System.out.println(myOrganismTaxonomy);
//	    							 //							System.out.println();
//	    							 Organism organismObj = new Organism(organism, taxonomy, rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy));
//	    							 this.loadOrganism(organismObj);
//	    							 //this.loadOrganism(organism,taxonomy,myOrganismTaxonomy,locus);
//	    							 //this.loadHomology(locus, this.homologyData.getGenes().get(locus), this.homologyData.getEValue().get(locus), this.homologyData.getScore().get(locus));
//	    						 }
//
//	    						 //				System.out.println(this.homologyData.getGenes().get(locus));
//	    						 //				System.out.println(this.homologyData.getEValue().get(locus));
//	    						 //				System.out.println(this.homologyData.getScore().get(locus));
//
//	    						 double cwt=0;
//	    						 if(this.homologyData.getCalculated_mol_wt().get(locus)!= null)
//	    						 {	
//	    							 cwt = Double.parseDouble(this.homologyData.getCalculated_mol_wt().get(locus));
//	    						 }
//
//	    						 String blastLocusTagID = this.homologyData.getBlastLocusTag().get(locus);			
//	    						 if (blastLocusTagID==null)
//	    						 {
//	    							 blastLocusTagID=locus;				
//	    						 }
//
//	    						 int uniprot_star_int = -1;
//	    						 star = null;
//	    						 if(this.homologyData.getUniprotStar().containsKey(blastLocusTagID)) {
//
//	    							 star = this.homologyData.getUniprotStar().get(blastLocusTagID).toString();
//	    						 }
//	    						 else {
//	    							 try {
//	    								 star =  UniProtAPI.isStarred(blastLocusTagID)+"";
//	    							 }
//	    							 catch(Exception e) {
//	    								 star = null;
//	    							 }
//	    							 catch(Error e) {
//	    								 star = null;
//	    							 }
//	    							 if(star!= null) {
//	    								 uniprot_star_int = MySQL_Utilities.get_boolean_int(star);
//	    							 }
//	    						 }
//
//	    						 Homologues homologueObj = new Homologues(blastLocusTagID, this.homologyData.getDefinition().get(locus), cwt, 
//	    								 this.homologyData.getProduct().get(locus), this.homologyData.getOrganelles().get(locus), 
//	    								 uniprot_star_int, locus,  this.homologyData.getGenes().get(locus), this.homologyData.getEValue().get(locus), 
//	    								 this.homologyData.getScore().get(locus));
//
//	    						 this.loadHomologuesRemote(homologueObj);
//
//	    						 String product =  this.homologyData.getProduct().get(locus);
//	    						 if(pd.containsKey(product)) {
//
//	    							 pd.put(product, pd.get(product)+1);
//	    							 List<Integer> orgKey = this.prodOrg.get(product);
//	    							 //			if(!orgKey.contains(this.organism_s_key))
//	    							 //			{
//	    							 orgKey.add(this.organism_s_key);
//	    							 //			}
//	    							 this.prodOrg.put(product, orgKey);
//	    						 }
//	    						 else {
//
//	    							 pd.put(product, 1);
//	    							 List<Integer> orgKey = new ArrayList<Integer>();
//	    							 orgKey.add(this.organism_s_key);
//	    							 this.prodOrg.put(product, orgKey);
//	    						 }
//
//	    						 //	    						 pd = loadHomologuesRemote(blastLocusTagID, 
//	    						 //	    								 this.homologyData.getDefinition().get(locus) ,
//	    						 //	    								 cwt,
//	    						 //	    								 this.homologyData.getProduct().get(locus),
//	    						 //	    								 pd,
//	    						 //	    								 this.homologyData.getOrganelles().get(locus));
//
//	    						 //							this.loadHomology(locus, this.homologyData.getGenes().get(locus), this.homologyData.getEValue().get(locus), 
//	    						 //									this.homologyData.getScore().get(locus));
//
//
//	    						 if(this.homologyData.getEcnumber().keySet().contains(locus)) {
//
//	    							 Set<String> ecnumbers = new HashSet<String>();
//
//	    							 for(int e =0; e<this.homologyData.getEcnumber().get(locus).length; e++) {
//
//	    								 this.loadECNumbers(this.homologyData.getEcnumber().get(locus)[e], blastLocusTagID);
//
//	    								 if(!ecnumbers.contains(this.homologyData.getEcnumber().get(locus)[e])) {
//
//	    									 ecnumbers.add(this.homologyData.getEcnumber().get(locus)[e]); 
//	    								 }
//	    							 }
//
//	    							 if(ecn.containsKey(ecnumbers)) {
//
//	    								 ecn.put(ecnumbers, ecn.get(ecnumbers)+1);
//
//	    								 List<Integer> orgKey = ecOrg.get(ecnumbers);
//	    								 orgKey.add(this.organism_s_key);
//
//	    								 ecOrg.put(ecnumbers, orgKey);
//	    							 }
//	    							 else {
//
//	    								 List<Integer> orgKey = new ArrayList<Integer>();
//	    								 ecn.put(ecnumbers, 1);
//	    								 orgKey.add(this.organism_s_key);
//	    								 ecOrg.put(ecnumbers, orgKey);
//	    							 }
//	    						 }
//	    					 }
//	    				 }
//
//
//	    				 if(!this.cancel.get()) {
//
//	    					 if(!pd.isEmpty()){
//
//	    						 for(String productname : pd.keySet()){
//
//	    							 List<Integer> org4prod = this.prodOrg.get(productname);
//	    							 this.loadProductRank(productname, pd.get(productname), org4prod);
//	    						 }
//	    					 }
//
//
//	    					 if (!ecn.isEmpty()) {
//
//	    						 for(Set<String> ecnumbers : ecn.keySet()){
//
//	    							 List<Integer> org4ec = ecOrg.get(ecnumbers);
//	    							 String ecnumber_complete = "";
//	    							 for (String ec : ecnumbers){
//	    								 ecnumber_complete += ec;
//	    							 }
//
//	    							 this.loadECNumberRank(ecnumber_complete, ecn.get(ecnumbers), org4ec);		 
//	    						 }
//
//	    					 }
//	    					 this.updataGeneStatus(locusTag);
//	    				 }
//	    			 }
//
//	    			 if(this.cancel.get()) {
//
//	    				 this.setLoaded(false);
//	    			 }
//	    			 //conn.close();
//	    		 }
//	    	 }
//	    	 catch (Exception e) {
//
//	    		 //	e.printStackTrace();
//	    		 this.deleteEntry();
//	    		 this.setLoaded(false);
//	    	 }
//	     }
//
//	     //	/**
//	     //	 * @param rawString
//	     //	 * @return
//	     //	 */
//	     //	private String parseString(String rawString) {
//	     //		if(rawString==null)
//	     //			return null;
//	     //		return rawString.replace("\\'","'").replace("-","\\-").replace("'","\\'").replace("[","\\[").replace("]","\\]");//.replace(".","");		
//	     //	}

	     /**
	      * 
	      */
	     public void deleteEntry() {

	    	 try  {

	    		 if(this.undo_geneHomology_s_key>0) {

	    			 this.statement.execute("DELETE FROM geneHomology WHERE s_key='"+this.undo_geneHomology_s_key+"'");
	    		 }

	    		 //			if(this.undo_organism_s_key!=null && !this.undo_organism_s_key.isEmpty()) {
	    		 //
	    		 //				this.statement.execute("DELETE FROM organism WHERE s_key='"+this.undo_organism_s_key+"'");
	    		 //			}

	    	 }
	    	 catch (SQLException e) {

	    		 e.printStackTrace();
	    	 }
	     }

	     /**
	      * @param loaded the loaded to set
	      */
	     public void setLoaded(boolean loaded) {
	    	 this.loaded = loaded;
	     }

	     /**
	      * @return the loaded
	      */
	     public boolean isLoaded() {
	    	 return loaded;
	     }


}