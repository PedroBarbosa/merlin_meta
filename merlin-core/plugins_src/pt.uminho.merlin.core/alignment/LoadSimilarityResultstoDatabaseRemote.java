/**
 * 
 */
package alignment;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.merlin.utilities.DatabaseProgressStatus;
import alignment.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import datatypes.Project;

/**
 * @author oDias
 *
 */
public class LoadSimilarityResultstoDatabaseRemote {

	private NCBIQBlastAlignmentProperties rqb;
	private HomologyDataClient homologyDataClient;
	private Statement statement;
	private String organism_s_key, homologues_s_key, geneHomology_s_key, homologySetupID, databaseID, version, program, query; 
	private HashMap<String, List<String>> prodOrg;
	private String //undo_organism_s_key, 
	undo_geneHomology_s_key;
	private boolean loaded;
	private int maxNumberOfAlignments;
	private Project project;
	private double eVal;
	private AtomicBoolean cancel;


	/**
	 * @param hd
	 * @param rqb
	 * @param maxNumberOfAlignments
	 * @param project
	 * @param uniprotStatus 
	 */
	public LoadSimilarityResultstoDatabaseRemote(HomologyDataClient hd, NCBIQBlastAlignmentProperties rqb, 
			int maxNumberOfAlignments, Project project, AtomicBoolean cancel) {

		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.rqb = rqb;
		this.homologyDataClient = hd;
		this.program = hd.getProgram();
		this.version = hd.getVersion();
		this.databaseID = hd.getDatabaseID();
		this.query = hd.getQuery();
		this.setLoaded(true);
		this.project = project;
		this.cancel = cancel;
	}

	/**
	 * @param hd
	 * @param maxNumberOfAlignments
	 * @param eValue
	 * @param project
	 * @param cancel
	 */
	public LoadSimilarityResultstoDatabaseRemote(HomologyDataClient hd, int maxNumberOfAlignments, 
			double eValue, Project project, AtomicBoolean cancel) {

		this.eVal = eValue;
		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.homologyDataClient = hd;
		this.program = hd.getProgram();
		this.version = hd.getVersion();
		this.databaseID = hd.getDatabaseID();
		this.query = hd.getQuery();
		this.setLoaded(true);
		this.project = project;
		this.cancel = cancel;
	}

	/**
	 * @param locusTag
	 * @param query
	 * @param gene
	 * @param chromosome
	 * @param organelle
	 * @throws SQLException
	 */
	private void loadGene(String locusTag, String query, String gene, String chromosome, String organelle, String uniprot_star) throws SQLException {

		this.undo_geneHomology_s_key ="";
		int uniprot_star_int = -1;

		if(uniprot_star!= null) {

			uniprot_star_int = MySQL_Utilities.get_boolean_int(uniprot_star);
		}

		if(this.homologyDataClient.isNoSimilarity()) {

			ResultSet rs = this.statement.executeQuery("SELECT * FROM geneHomology WHERE query = '"+MySQL_Utilities.mysqlStrConverter(query)+"' AND homologySetup_s_key = " +this.homologySetupID);
			if(!rs.next()) {

				this.statement.execute("INSERT INTO geneHomology (locusTag, query, homologySetup_s_key, status, uniprot_star, uniprot_ecnumber) VALUES ('"
						+ locusTag +"','"+ MySQL_Utilities.mysqlStrConverter(query) + "','" +this.homologySetupID+ "','"+DatabaseProgressStatus.NO_SIMILARITY+"','"+uniprot_star_int+"','"+this.homologyDataClient.getUniprot_ecnumber()+"');");

				rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}
			this.geneHomology_s_key = rs.getString(1);
			rs.close();
		}
		else {

			ResultSet rs = this.statement.executeQuery("SELECT * FROM geneHomology WHERE query = '"+MySQL_Utilities.mysqlStrConverter(query)+"' AND homologySetup_s_key = " +this.homologySetupID);
			if(rs.next()) {

				this.geneHomology_s_key = rs.getString(1);
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
						"'," +uniprot_star_int + ",'" +this.homologySetupID+ "','"+DatabaseProgressStatus.PROCESSING+"','"+this.homologyDataClient.getUniprot_ecnumber()+"');");
				rs =this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.geneHomology_s_key = rs.getString(1);
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
	private void loadOrganism(String organism, String taxonomy, String myOrganismTaxonomy, String locus) throws Exception {

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

				this.organism_s_key = rs.getString(1);
				//this.undo_organism_s_key="";
			}
			else {

				this.statement.execute("INSERT INTO organism (organism, taxonomy, taxrank) VALUES ('"
						+ MySQL_Utilities.mysqlStrConverter(organism) +"','"+ MySQL_Utilities.mysqlStrConverter(taxonomy) +"','"+this.rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy)+"');");
				rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.organism_s_key = rs.getString(1);
				//this.undo_organism_s_key=this.organism_s_key;
			}
		}
		catch (NullPointerException e) {

			System.out.println(this.homologyDataClient.getOrganism());
			System.out.println();
			System.out.println("LoadBlastResultstoDatabase null pointer locus:"+locus+"\t\torganism:\t"+organism+"\t\ttax:\t"+taxonomy+"\t\tmyOrgTax:\t"+myOrganismTaxonomy);
			//e.printStackTrace();
			throw e;
		}
		catch (SQLException e) {

			System.out.println(this.homologyDataClient.getOrganism());
			System.out.println("LoadBlastResultstoDatabase sql organism "+organism+" tax "+taxonomy+" myOrgTax "+myOrganismTaxonomy);

			throw e;
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
	 * @param locusID
	 * @param definition
	 * @param calculated_mw
	 * @param product
	 * @param pd
	 * @param organelle
	 * @return
	 * @throws SQLException 
	 */
	private Map<String, Integer> loadHomologues(String locusID, String definition, double calculated_mw, String product, Map<String, Integer> pd, String organelle) throws SQLException {

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

			rs = this.statement.executeQuery("SELECT * FROM homologues WHERE " +
					"organism_s_key='"+ this.organism_s_key +"' "+
					//"homologues='"+this.homology_s_key +"' AND homology_geneHomology_s_key='"+ this.geneHomology_s_key +"' " +
					"AND locusID='"+ MySQL_Utilities.mysqlStrConverter(locusID) +"' " +
					"AND (definition='"+ MySQL_Utilities.mysqlStrConverter(definition) +"') " +
					"AND calculated_mw= "+ calculated_mw +" " +
					"AND (product='"+ MySQL_Utilities.mysqlStrConverter(product) +"') " +
					"AND organelle='"+ MySQL_Utilities.mysqlStrConverter(organelle) +"'");
		} 
		catch (SQLException e) {

			System.out.println("SELECT * FROM homologues WHERE " +
					"organism_s_key='"+ this.organism_s_key +"' "+
					//"homologues='"+this.homology_s_key +"' AND homology_geneHomology_s_key='"+ this.geneHomology_s_key +"' " +
					"AND locusID='"+ MySQL_Utilities.mysqlStrConverter(locusID) +"' " +
					"AND (definition='"+ MySQL_Utilities.mysqlStrConverter(definition) +"') " +
					"AND calculated_mw= "+ calculated_mw +" " +
					"AND (product='"+ MySQL_Utilities.mysqlStrConverter(product) +"') " +
					"AND organelle='"+ MySQL_Utilities.mysqlStrConverter(organelle) +"'");

			e.printStackTrace();
			throw e;
		}

		if(rs.next()) {

			this.homologues_s_key = rs.getString(1);
		}
		else {

			int uniprot_star_int = -1;

			if(this.homologyDataClient.getUniprotStar().containsKey(locusID)) {
				
				String star = this.homologyDataClient.getUniprotStar().get(locusID).toString();
				uniprot_star_int = MySQL_Utilities.get_boolean_int(star);
			}

			this.statement.execute("INSERT INTO homologues (" +
					//"homology_s_key, homology_geneHomology_s_key," +
					"organism_s_key, locusID, definition, calculated_mw, product, organelle, uniprot_star) " +
					"VALUES ("
					//"'"+this.homology_s_key +"','"+ this.geneHomology_s_key +"','"+ 
					+this.organism_s_key +",'"+locusID +"','"+ MySQL_Utilities.mysqlStrConverter(definition) + "','"+ calculated_mw +"','"+ MySQL_Utilities.mysqlStrConverter(product) +"'," +"'"+ organelle +"',"+uniprot_star_int+")");

			rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.homologues_s_key = rs.getString(1);
		}

		if(pd.containsKey(product)) {

			pd.put(product, pd.get(product)+1);
			List<String> orgKey = this.prodOrg.get(product);
			//			if(!orgKey.contains(this.organism_s_key))
			//			{
			orgKey.add(this.organism_s_key);
			//			}
			this.prodOrg.put(product, orgKey);
		}
		else {

			pd.put(product, 1);
			List<String> orgKey = new ArrayList<String>();
			orgKey.add(this.organism_s_key);
			this.prodOrg.put(product, orgKey);
		}

		//this.statement.execute("UNLOCK tables;");
		return pd;
	}


	/**
	 * @param referenceID
	 * @param gene
	 * @param eValue
	 * @param bits
	 * @throws SQLException
	 */
	private void load_geneHomology_has_homologues(String referenceID, String gene, double eValue, double bits) {

		try {

			if(gene!=null) {

				gene=MySQL_Utilities.mysqlStrConverter(gene);

				ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' AND homologues_s_key = "+this.homologues_s_key+" " +
						"AND referenceID='"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"' AND gene='"+ gene +"' AND eValue='"+ eValue +"' AND bits='"+ bits +"'");

				if(!rs.next()) {

					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, referenceID, gene ,eValue, bits, homologues_s_key) " +
							"VALUES("+this.geneHomology_s_key +",'"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"','"+ gene +"','"+ eValue +"','"+ bits +"',"+this.homologues_s_key+" );");
				}
			}
			else {

				ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' AND homologues_s_key = "+this.homologues_s_key+" " +
						"AND referenceID='"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"' AND eValue='"+ eValue +"' AND bits='"+ bits +"'");

				if(!rs.next()) {

					this.statement.execute("INSERT INTO geneHomology_has_homologues (geneHomology_s_key, referenceID, eValue, bits, homologues_s_key) " +
							"VALUES("+this.geneHomology_s_key +",'"+ MySQL_Utilities.mysqlStrConverter(referenceID) +"','"+ eValue +"','"+ bits +"',"+this.homologues_s_key+" );");
				}
			}
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param sequence
	 * @throws SQLException
	 */
	private void loadFastaSequence(String sequence) throws SQLException {

		ResultSet rs = this.statement.executeQuery("SELECT * FROM fastaSequence WHERE geneHomology_s_key = '"+this.geneHomology_s_key+"'");

		if(!rs.next()) {

			this.statement.execute("INSERT INTO fastaSequence (geneHomology_s_key, sequence) VALUES('"+ this.geneHomology_s_key + "','"+ sequence +"');");
		}
	}

	/**
	 * @param ecNumber
	 * @throws SQLException
	 */
	private void loadECNumbers(String ecNumber) throws SQLException {

		//this.statement.execute("LOCK tables ecNumber read,homology_has_ecNumber read;");

		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumber WHERE ecNumber = '"+ecNumber+"'");

		if(!rs.next()) {

			this.statement.execute("INSERT INTO ecNumber SET ecNumber='"+ ecNumber +"'");
			rs = this.statement.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}

		String ecnumber_s_key = rs.getString(1);

		rs = this.statement.executeQuery("SELECT * FROM homologues_has_ecNumber " +
				"WHERE homologues_s_key = '"+this.homologues_s_key+"' AND " +
				"ecNumber_s_key = '"+ecnumber_s_key+"'");

		if(!rs.next())
		{
			this.statement.execute("INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) " +
					"VALUES('"+this.homologues_s_key +"','"+ ecnumber_s_key +"');");
		}
		rs.close();

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
	 * @throws SQLException
	 */
	private void loadhomologySetup(String databaseID, String program, String version) throws SQLException {

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
	private void loadHmmerSetup(String databaseID, String program, String version) throws SQLException {

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

	/**
	 * @param pd
	 * @throws SQLException
	 */
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
				String prodkey = rs.getString(1);

				for(String orgKey : this.prodOrg.get(product)) {

					this.statement.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES('"+ MySQL_Utilities.mysqlStrConverter(prodkey) + "','" + orgKey+ "');");
				}
			}
			rs.close();
		}
		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 * @param ecn
	 * @param ecOrg
	 * @throws SQLException
	 */
	private void loadECNumberRank(Map<Set<String>, Integer> ecn, Map<Set<String>, List<String>> ecOrg) throws SQLException {

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

					for(String orgKey : ecOrg.get(ecnumber)) {

						this.statement.execute("INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES('"+ eckey + "','" + orgKey+ "');");
					}
				}
			}
			rs.close();
		}

		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 */
	public void loadData() {

		try {

			Connection conn = this.project.getDatabase().getMySqlCredentials().openConnection();
			this.statement = conn.createStatement();

			String program = "hmmer";
			if(this.rqb != null) { 

				program = this.homologyDataClient.getProgram();
			}

			ResultSet rs  = this.statement.executeQuery("SELECT * FROM geneHomology " +
					"INNER JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
					"WHERE query = '"+this.homologyDataClient.getQuery()+"' " +
					"AND program = '"+program+"' ");

			if(rs.next()) {

				System.out.println(this.homologyDataClient.getLocus_tag()+"\tGENE already processed!");
			} 
			else {

				String star;
				if(this.homologyDataClient.getQuery() != null && this.homologyDataClient.getUniprotStar()!=null && this.homologyDataClient.getUniprotStar().containsKey(this.homologyDataClient.getQuery())) {

					star = this.homologyDataClient.getUniprotStar().get(this.homologyDataClient.getQuery()).toString();
				}
				else {

					try {

						star =  this.homologyDataClient.isUniProtStarred()+"";
					}
					catch(NullPointerException e) {

						star = null;
					}
				}

				if(this.rqb != null) {

					this.loadhomologySetup(this.databaseID, this.program, this.version);
				}
				else {

					this.loadHmmerSetup(this.databaseID, this.program, this.version);
				}	

				if(this.homologyDataClient.isNoSimilarity()) {

					String locusTag = this.query;
					if(this.homologyDataClient.getLocus_tag() != null) {

						locusTag = this.homologyDataClient.getLocus_tag();
					}

					this.loadGene(locusTag, this.query, null, null, null, star);
					this.loadFastaSequence(this.homologyDataClient.getFastaSequence());
				}
				else {

					String locusTag;

					if(this.homologyDataClient.getLocus_tag()==null) {

						if(this.homologyDataClient.getLocus_gene_note()==null) {

							locusTag=this.homologyDataClient.getLocus_protein_note();	
						}
						else {

							if(this.homologyDataClient.getLocusID().getFirst().matches("[A-Za-z]*\\d*\\s+")
									&& !this.homologyDataClient.getLocus_tag().contains(":") )//if the locus tag contains spaces and not: 
							{

								locusTag=this.homologyDataClient.getLocusID().getFirst();
							}
							else {

								String[] locus = this.homologyDataClient.getLocus_gene_note().split(":");
								locusTag = locus[locus.length-1];
								locus = locusTag.split(";");
								locusTag = locus[0];
							}
						}
					}
					else {

						locusTag=this.homologyDataClient.getLocus_tag();
					}
					
					this.loadGene(locusTag, this.query, this.homologyDataClient.getGene(), this.homologyDataClient.getChromossome(), this.homologyDataClient.getOrganelle(), star);
					this.loadFastaSequence(this.homologyDataClient.getFastaSequence());
					Map<String, Integer> pd = new HashMap<String,Integer>();
					this.prodOrg = new HashMap<String,List<String>>();
					Map<Set<String>, Integer> ecn = new HashMap<Set<String>,Integer>();
					Map<Set<String>, List<String>> ecOrg = new HashMap<Set<String>,List<String>>();
					boolean first=true;
					String myOrganismTaxonomy="";

					if(!this.homologyDataClient.isNCBIGenome()) {

						myOrganismTaxonomy = this.homologyDataClient.getTaxonomyID()[1].concat("; "+this.homologyDataClient.getTaxonomyID()[0]);
						
						this.loadOrganism(this.homologyDataClient.getTaxonomyID()[0], this.homologyDataClient.getTaxonomyID()[1],myOrganismTaxonomy,"origin organism");
						first=false;
					}

					for(String locus:this.homologyDataClient.getLocusID()) {

						if(!this.cancel.get()) {

							//				System.out.println(this.homologyData.getGenes().get(locus));
							//				System.out.println(this.homologyData.getEValue().get(locus));
							//				System.out.println(this.homologyData.getScore().get(locus));

							double cwt=0;
							if(this.homologyDataClient.getCalculated_mol_wt().get(locus)!= null) {
								
								cwt = Double.parseDouble(this.homologyDataClient.getCalculated_mol_wt().get(locus));
							}

							String blastLocusTagID = this.homologyDataClient.getBlastLocusTag().get(locus);			
							if (blastLocusTagID==null) {
								
								blastLocusTagID=locus;				
							}

							String organism = this.homologyDataClient.getOrganism().get(locus),
									taxonomy =	this.homologyDataClient.getTaxonomy().get(locus);

							if(first) {
								
								myOrganismTaxonomy = this.homologyDataClient.getTaxonomy().get(locus).concat("; "+this.homologyDataClient.getOrganism().get(locus));
								first=false;
								this.loadOrganism(organism,taxonomy,myOrganismTaxonomy,locus);

								pd = loadHomologues(blastLocusTagID, 
										this.homologyDataClient.getDefinition().get(locus) ,
										cwt,
										this.homologyDataClient.getProduct().get(locus),
										pd,
										this.homologyDataClient.getOrganelles().get(locus));
								this.load_geneHomology_has_homologues(locus, this.homologyDataClient.getGenes().get(locus), 0, 0.0);
							}
							else {

								this.loadOrganism(organism,taxonomy,myOrganismTaxonomy,locus);

								pd = loadHomologues(blastLocusTagID, 
										this.homologyDataClient.getDefinition().get(locus) ,
										cwt,
										this.homologyDataClient.getProduct().get(locus),
										pd,
										this.homologyDataClient.getOrganelles().get(locus));
								this.load_geneHomology_has_homologues(locus, this.homologyDataClient.getGenes().get(locus), this.homologyDataClient.getEValue().get(locus), this.homologyDataClient.getScore().get(locus));
							}

							if(this.homologyDataClient.getEcnumber().keySet().contains(locus)) {

								Set<String> ecnumbers = new HashSet<String>();

								for(int e =0; e<this.homologyDataClient.getEcnumber().get(locus).length; e++) {

									this.loadECNumbers(this.homologyDataClient.getEcnumber().get(locus)[e]);

									if(!ecnumbers.contains(this.homologyDataClient.getEcnumber().get(locus)[e])) {

										ecnumbers.add(this.homologyDataClient.getEcnumber().get(locus)[e]); 
									}
								}

								if(ecn.containsKey(ecnumbers)) {

									List<String> orgKey = ecOrg.get(ecnumbers); 
									ecn.put(ecnumbers, ecn.get(ecnumbers)+1);
									//						if(!orgKey.contains(this.organism_s_key))
									//						{
									orgKey.add(this.organism_s_key);
									//						}
									ecOrg.put(ecnumbers, orgKey);
								}
								else {

									List<String> orgKey = new ArrayList<String>();
									ecn.put(ecnumbers, 1);
									orgKey.add(this.organism_s_key);
									ecOrg.put(ecnumbers, orgKey);
								}
							}
						}
					}

					if(!this.cancel.get()) {

						this.loadProductRank(pd);

						if (!ecn.isEmpty()) {

							this.loadECNumberRank(ecn,ecOrg);		
						}
						this.updataGeneStatus(locusTag);
					}
				}

				if(this.cancel.get()) {

					this.setLoaded(false);
				}
				this.statement.close();
				conn.close();
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			this.deleteEntry();
			this.setLoaded(false);
		}
	}

	//	/**
	//	 * @param rawString
	//	 * @return
	//	 */
	//	private String parseString(String rawString) {
	//		if(rawString==null)
	//			return null;
	//		return rawString.replace("\\'","'").replace("-","\\-").replace("'","\\'").replace("[","\\[").replace("]","\\]");//.replace(".","");		
	//	}

	/**
	 * 
	 */
	public void deleteEntry() {

		try  {

			if(this.undo_geneHomology_s_key!=null && !this.undo_geneHomology_s_key.isEmpty()) {

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