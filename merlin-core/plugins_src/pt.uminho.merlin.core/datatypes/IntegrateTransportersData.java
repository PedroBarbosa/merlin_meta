/**
 * 
 */
package datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.bioapis.externalAPI.ExternalRefSource;
import pt.uminho.sysbio.common.biocomponents.container.components.ReactionCI;
import pt.uminho.sysbio.common.biocomponents.container.components.StoichiometryValueCI;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly.ProteinFamiliesSet;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
public class IntegrateTransportersData implements IntegrateData{

	private Connection connection;
	private TransportContainer transportContainer;
	private Map<String,String> metabolites_ids;
	private Map<String,String> compartments_ids;
	private Map<String,String> gene_Database_ids;
	//private Map<String,String> gene_Transporters_ids;
	private Map<String,ProteinFamiliesSet> genes_protein_ids;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private long startTime;
	private List<String> metabolitesInModel;
	private boolean reactionInModel;
	private int compartmentID;
	
	/**
	 * @param project
	 */
	public IntegrateTransportersData (Project project) {

		try {

			this.compartmentID = -1;
			this.connection = new Connection(project.getDatabase().getMySqlCredentials());
			this.transportContainer = project.getTransportContainer();
			this.metabolites_ids = new HashMap<String, String>();
			this.compartments_ids = new HashMap<String, String>();
			this.getMetabolitesInModel();
			this.cancel = new AtomicBoolean(false);
			this.genes_protein_ids = project.getTransportContainer().getGenesProteins();
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	private void getMetabolitesInModel() {

		this.metabolitesInModel = new ArrayList<String>();
		Statement stmt;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT(compound_idcompound) FROM stoichiometry " +
					"LEFT JOIN reaction ON reaction_idreaction = reaction.idreaction " +
					"WHERE inModel");

			while(rs.next()) {

				this.metabolitesInModel.add(rs.getString(1));	
			}

			if (this.compartmentID<0) {
				
				stmt.execute("LOCK TABLES compartment WRITE");
				rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name ='inside'");

				if(!rs.next()) {

					stmt.execute("INSERT INTO compartment (name, abbreviation) VALUES('inside','in')");
					rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
				}
				this.compartmentID = rs.getInt(1);
				stmt.execute("UNLOCK TABLES");
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	public boolean performIntegration() {

		int counter = 0;

		for(String reactionID :this.transportContainer.getReactions().keySet()) {

			if(this.cancel.get()) {

				counter = this.transportContainer.getReactions().keySet().size();
				break;
			}
			else {

				ReactionCI reaction = this.transportContainer.getReactions().get(reactionID);
				boolean isReversible = reaction.isReversible();

				try  {

					String idPathway = this.addPathway("Transporters pathway");

					if(reaction.isAllMetabolitesHaveKEGGId()) {

						Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
						Map<String, StoichiometryValueCI> products = reaction.getProducts();
						this.reactionInModel = true;

						if(this.getCompounsdID(reactants.keySet()) && this.getCompounsdID(products.keySet())) {
							
							String equation="";
							for(String key :reactants.keySet()) {

								equation=equation.concat(reactants.get(key).getStoichiometryValue()+" "+transportContainer.getMetabolites().get(reactants.get(key).getMetaboliteId()).getName()+" ("+reactants.get(key).getCompartmentId())+") + ";
							}
							equation=equation.substring(0, equation.lastIndexOf("+")-1);
							if(transportContainer.getReactions().get(reaction.getId()).isReversible()) {

								equation=equation.concat(" <=> ");
							}
							else {

								equation=equation.concat(" => ");
							}

							for(String key :products.keySet()) {

								equation=equation.concat(products.get(key).getStoichiometryValue()+" "+transportContainer.getMetabolites().get(products.get(key).getMetaboliteId()).getName()+" ("+products.get(key).getCompartmentId())+") + ";
							}
							equation=equation.substring(0, equation.lastIndexOf("+")-1);
							
							String idReaction = this.addReactionID(reactionID, isReversible, equation);
							
							this.addPathway_has_Reaction(idPathway, idReaction);

							for(String metabolite : reactants.keySet()) {

								String compartment;
								if (reactants.get(metabolite).getCompartmentId().equalsIgnoreCase("in")) {

									compartment = "inside";
								}
								else {

									compartment = "outside";
								}
								String idCompartment = this.getCompartmentsID(compartment);

								double reactants_stoichiometry = -1*reactants.get(metabolite).getStoichiometryValue();
								this.addStoichiometry(idReaction, this.metabolites_ids.get(metabolite), idCompartment, reactants_stoichiometry);
							}

							for(String metabolite : products.keySet()) {

								String compartment;
								if (products.get(metabolite).getCompartmentId().equalsIgnoreCase("in")) {

									compartment = "inside";
								}
								else {

									compartment = "outside";
								}
								String idCompartment = this.getCompartmentsID(compartment);

								this.addStoichiometry(idReaction, this.metabolites_ids.get(metabolite), idCompartment, products.get(metabolite).getStoichiometryValue());
							}
							
							for(String locusTag : reaction.getGenesIDs()) {
								
								String idDatabaseGene = this.getGenesDatabaseID(locusTag);

								if(idDatabaseGene != null) {

									//String idTransportersGene = this.getGenesTransportersID(locusTag);
									
									Set<String> tcNumbers = new HashSet<String>();
									
									if(this.genes_protein_ids.get(locusTag).getTc_families_above_half() == null) {

										tcNumbers.add(this.genes_protein_ids.get(locusTag).getMax_score_family());
									}
									else {

										tcNumbers = this.genes_protein_ids.get(locusTag).getTc_families_above_half().keySet();
									}

									for(String tcNumber : tcNumbers)  {

										if(reaction.getProteinIds().contains(tcNumber)) {

											String idProtein = this.addProteinIDs(tcNumber, reactionID);

											this.addSubunit(idProtein, tcNumber, idDatabaseGene);

											this.addReaction_has_Enzyme(idProtein, tcNumber, idReaction);

											this.addPathway_has_Enzyme(idProtein, tcNumber, idPathway);
										}
									}
								}
							}
						}
						else {

							System.out.println("Could not integrate reaction "+reactionID);
						}
					}
				} 
				catch (Exception e) { 

					e.printStackTrace();
				}
			}
			this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),counter,this.transportContainer.getReactions().size());
			counter++;
		}
		return true;
	}

	/**
	 * @param name
	 * @return
	 * @throws SQLException 
	 */
	private String addPathway(String name) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT idpathway FROM pathway WHERE name = '"+name+"';");

		if(!rs.next()) {

			stmt.execute("INSERT INTO pathway (name, code) VALUES('"+name+"','T0001')");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}
		String idpathway = rs.getString(1);
		stmt.close();

		return idpathway;
	}

	/**
	 * @param reactionID
	 * @param isReversible
	 * @param equation
	 * @return
	 * @throws SQLException
	 */
	private String addReactionID(String reactionID, boolean isReversible, String equation) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT idreaction FROM reaction WHERE name = '"+reactionID+"';");

		if(!rs.next()){

			stmt.execute("INSERT INTO reaction (name, reversible, inModel, equation, source, isGeneric, isSpontaneous, isNonEnzymatic,originalReaction,compartment_idcompartment) " +
					"VALUES('"+reactionID+"',"+isReversible+","+this.reactionInModel+"," +
					"'"+MySQL_Utilities.mysqlStrConverter(equation)+"', 'TRANSPORTERS',false,false,false,true,"+this.compartmentID+")");

			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}
		String idreaction = rs.getString(1);
		stmt.close();

		return idreaction;
	}

	/**
	 * @param geneID
	 * @return
	 * @throws SQLException
	 */
	private String getGenesDatabaseID(String geneID) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs;

		if(this.gene_Database_ids==null) {
			
			this.gene_Database_ids = new HashMap<String, String>();

			rs = stmt.executeQuery("SELECT locusTag,idgene FROM gene;");

			while(rs.next()) {

				this.gene_Database_ids.put(rs.getString(1),rs.getString(2));
			}
			rs.close();
		}

		if(!this.gene_Database_ids.containsKey(geneID)) {

			rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+geneID+"'");

			if(!rs.next()) {

				rs = stmt.executeQuery("SELECT idchromosome FROM chromosome WHERE name = 'DEFAULT'");
				String idchromosome;

				if(rs.next()) {

					idchromosome = rs.getString(1);
				}
				else {

					stmt.execute("INSERT INTO chromosome (name) VALUES('DEFAULT')");
					rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
					idchromosome = rs.getString(1);
				}
				stmt.execute("INSERT INTO gene (locusTag,chromosome_idchromosome,origin) VALUES('"+geneID+"','"+idchromosome+"','TRANSPORTERS')");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}
			this.gene_Database_ids.put(geneID, rs.getString(1));
			rs.close();
		}
		stmt.close();
		return this.gene_Database_ids.get(geneID);
	}

//	/**
//	 * @param geneID
//	 * @return
//	 * @throws SQLException
//	 */
//	private String getGenesTransportersID(String geneID) throws SQLException {
//
//		if(this.gene_Transporters_ids== null) {
//
//			this.gene_Transporters_ids = new HashMap<String, String>();
//			
//			Connection conn = this.connection;
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery("SELECT locus_tag, id FROM genes");
//
//			while(rs.next()) {
//
//				this.gene_Transporters_ids.put(rs.getString(1),rs.getString(2));
//			}
//			stmt.close();
//		}
//
//		return this.gene_Transporters_ids.get(geneID);
//	}

	/**
	 * @param tcnumber
	 * @param reactionID
	 * @return
	 * @throws SQLException
	 */
	private String addProteinIDs(String tcnumber, String reactionID) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs;

		String class_name = "";
		if (tcnumber.startsWith("1.")) {

			class_name = "Channel/Pore";
		}
		else if (tcnumber.startsWith("2.")) {

			class_name = "Electrochemical Potential-driven Transporter";
		}
		else if (tcnumber.startsWith("3.")) {

			class_name = "Primary Active Transporter";
		}
		else if (tcnumber.startsWith("4.")) {

			class_name = "Group Translocator";
		}
		else if (tcnumber.startsWith("5.")) {

			class_name = "Transmembrane Electron Carrier";
		}
		else if (tcnumber.startsWith("8.")) {

			class_name = "Accessory Factor Involved in Transport";
		}
		else if (tcnumber.startsWith("9.")) {

			class_name = "Incompletely Characterized Transport System";
		}

		String reaction_code = reactionID;

		if(reactionID.contains("_")) {

			reaction_code = reactionID.split("_")[0];
		}

		String name = "Transport protein for reaction "+reaction_code;

		rs = stmt.executeQuery("SELECT idprotein FROM protein " +
				"WHERE name = '"+name+"' " +
				"AND class = '"+class_name+"';");

		if (!rs.next()) {

			stmt.execute("INSERT INTO protein (name, class) " +
					"VALUES('"+name+"','"+class_name+"');");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}

		String idprotein =  rs.getString(1);

		rs = stmt.executeQuery("SELECT inModel FROM enzyme " +
				"WHERE protein_idprotein = '"+idprotein+"' " +
				"AND ecnumber = '"+tcnumber+"';");

		if(rs.next()) {
			
			if(!rs.getBoolean(1) && this.reactionInModel) {
				
			stmt.execute("UPDATE enzyme SET inModel = "+this.reactionInModel+" WHERE protein_idprotein='"+idprotein+"' AND ecnumber='"+tcnumber+"' AND source='TRANSPORTERS'");
			}
		}
		else {

			stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel, source) " +
					"VALUES('"+idprotein+"','"+tcnumber+"',"+this.reactionInModel+",'TRANSPORTERS')");
		}
		stmt.close();

		return idprotein;
	}

	/**
	 * @param idpathway
	 * @param idreaction
	 * @return
	 * @throws SQLException
	 */
	private boolean addPathway_has_Reaction(String idPathway, String idReaction) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM pathway_has_reaction " +
				"WHERE reaction_idreaction = '"+idReaction+"' " +
				"AND pathway_idpathway = '"+idPathway+"';");

		if(rs.next()) {

			return false;
		}
		else {

			stmt.execute("INSERT INTO pathway_has_reaction (reaction_idreaction, pathway_idpathway) " +
					"VALUES('"+idReaction+"','"+idPathway+"');");
		}
		stmt.close();

		return true;
	}

	/**
	 * @param idprotein
	 * @param tcNumber
	 * @param idReaction
	 * @return
	 * @throws SQLException
	 */
	private boolean addReaction_has_Enzyme(String idprotein, String tcNumber, String idReaction) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme " +
				"WHERE reaction_idreaction = '"+idReaction+"' " +
				"AND enzyme_protein_idprotein = '"+idprotein+"' " +
				"AND enzyme_ecnumber = '"+tcNumber+"';");

		if(rs.next()) {

			return false;
		}
		else {

			stmt.execute("INSERT INTO reaction_has_enzyme (reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber) " +
					"VALUES('"+idReaction+"','"+idprotein+"','"+tcNumber+"');");
		}
		stmt.close();

		return true;
	}


	/**
	 * @param idprotein
	 * @param tcNumber
	 * @param idReaction
	 * @return
	 * @throws SQLException
	 */
	private boolean addPathway_has_Enzyme(String idprotein, String tcNumber, String idPathway) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM pathway_has_enzyme " +
				"WHERE pathway_idpathway = '"+idPathway+"' " +
				"AND enzyme_protein_idprotein = '"+idprotein+"' " +
				"AND enzyme_ecnumber = '"+tcNumber+"';");

		if(rs.next()) {

			return false;
		}
		else {

			stmt.execute("INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_protein_idprotein, enzyme_ecnumber) " +
					"VALUES('"+idPathway+"','"+idprotein+"','"+tcNumber+"');");
		}
		stmt.close();

		return true;
	}


	/**
	 * @param idProtein
	 * @param tcNumber
	 * @param idgene
	 * @return
	 * @throws SQLException
	 */
	private boolean addSubunit(String idProtein, String tcNumber, String idgene) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM subunit " +
				"WHERE gene_idgene = '"+idgene+"' " +
				"AND enzyme_protein_idprotein = '"+idProtein+"' " +
				"AND enzyme_ecnumber = '"+tcNumber+"';");

		if(rs.next()) {

			return false;
		}
		else {

			stmt.execute("INSERT INTO subunit (gene_idgene, enzyme_protein_idprotein, enzyme_ecnumber) " +
					"VALUES('"+idgene+"','"+idProtein+"','"+tcNumber+"');");
		}
		stmt.close();

		return true;
	}

	/**
	 * @param idReaction
	 * @param idCompound
	 * @param idCompartment
	 * @param stoichiometry
	 * @return
	 * @throws SQLException
	 */
	private boolean addStoichiometry(String idReaction, String idCompound, String idCompartment, double stoichiometry) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT idstoichiometry FROM stoichiometry " +
				"WHERE reaction_idreaction = '"+idReaction+"' " +
				"AND compound_idcompound = '"+idCompound+"' " +
				"AND compartment_idcompartment = '"+idCompartment+"' " +
				"AND stoichiometric_coefficient = '"+stoichiometry+"' ;");

		if(!rs.next()) {

			stmt.execute("INSERT INTO stoichiometry (reaction_idreaction, compound_idcompound, compartment_idcompartment, stoichiometric_coefficient, numberofchains) " +
					"VALUES('"+idReaction+"','"+idCompound+"','"+idCompartment+"','"+stoichiometry+"','1')");
		}
		stmt.close();

		return true;
	}


	/**
	 * @param metaboliteID
	 * @return
	 * @throws SQLException
	 */
	private boolean getCompounsdID(Set<String> metabolites) throws SQLException {

		Connection conn = this.connection;

		for(String metabolite : metabolites) {

			if(this.metabolites_ids.containsKey(metabolite)) {

				
			}
			else {

				Statement stmt = conn.createStatement();

				ResultSet rs = stmt.executeQuery("SELECT idcompound FROM compound " +
						"WHERE kegg_id = '"+ExternalRefSource.KEGG_CPD.getSourceId(this.transportContainer.getKeggMiriam().get(metabolite))+"' ;");

				String idcompound = null;
				
				if(rs.next()) {

					idcompound = rs.getString(1);
					this.metabolites_ids.put(metabolite,idcompound);
				}
				else {
					
					stmt.close();
					return false;
				}
				stmt.close();
			}

			if(this.metabolitesInModel.contains(this.metabolites_ids.get(metabolite))) {

			}
			else {
			
				this.reactionInModel = false;
			}
		}
		
		return true;
	}

	/**
	 * @param compartment
	 * @return
	 * @throws SQLException
	 */
	private String getCompartmentsID(String compartment) throws SQLException {
		
		String idcompartment = null;

		if(this.compartments_ids.containsKey(compartment)) {

			idcompartment = this.compartments_ids.get(compartment);
		}
		else {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT idCompartment FROM compartment " +
					"WHERE name = '"+compartment+"' ;");

			if(!rs.next()) {

				String abb = compartment;

				if(compartment.length()>3) {

					abb = compartment.substring(0, 3);
				}

				stmt.execute("INSERT INTO compartment (name, abbreviation) " +
						"VALUES('"+compartment+"','"+abb+"');");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}
			idcompartment = rs.getString(1);
			stmt.close();

		}
		return idcompartment;
	}


	/**
	 * 
	 */
	public void setCancel() {

		this.cancel = new AtomicBoolean(true);
	}


	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {

		this.progress = progress;
	}


	/**
	 * @return
	 */
	public AtomicBoolean isCancel() {

		return this.cancel;
	}

}
