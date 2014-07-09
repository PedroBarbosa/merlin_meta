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

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.common.transporters.core.compartments.GeneCompartments;
import pt.uminho.sysbio.common.transporters.core.compartments.ProcessCompartments;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
public class IntegrateCompartmentsData implements IntegrateData{


	private Connection connection;
	private Map<String,GeneCompartments> psortMap;
	private long startTime;
	private TimeLeftProgress progress;
	private AtomicBoolean cancel;
	private ProcessCompartments processCompartments;


	/**
	 * @param project
	 * @param threshold
	 */
	public IntegrateCompartmentsData(Project project) {

		try {

			this.connection = new Connection(project.getDatabase().getMySqlCredentials());
			this.psortMap = project.getGeneCompartments();
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
			this.cancel = new AtomicBoolean(false);
			this.processCompartments = new ProcessCompartments();
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @param bool
	 * @throws SQLException 
	 */
	public void initProcessCompartments() throws SQLException {

		Set<String> compartments = new HashSet<>();
		Statement stmt = this.connection.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT name FROM compartment;");

		while (rs.next()) {

			compartments.add(rs.getString(1));

		}		
		rs.close();
		stmt.close();

		this.processCompartments.initProcessCompartments(compartments);
	}

	/**
	 * @return
	 */
	public boolean performIntegration() {

		try {

			int counter = 0;
			Statement stmt = this.connection.createStatement();

			for(String locusTag :this.psortMap.keySet()) {

				if(this.cancel.get()) {

					counter = this.psortMap.keySet().size();
					break;
				}
				else {

					GeneCompartments geneComp = this.psortMap.get(locusTag);

					List<String> compartments = new ArrayList<String>();
					compartments.add(geneComp.getPrimary_location());

					for(String loc : geneComp.getSecondary_location().keySet()) {

						compartments.add(loc);
					}
					Map<String,String> compartment_id_map = new HashMap<String,String>();

					//compartments.removeAll(compartment_id_map.keySet());
					ResultSet rs;

					for(String compartment:compartments) {

						String abb = geneComp.getPrimary_location_abb();

						if(geneComp.getSecondary_location_abb().containsKey(compartment)) {

							abb = geneComp.getSecondary_location_abb().get(compartment);
						}

						if(!compartment_id_map.containsKey(compartment)) {

							rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '"+compartment+"';");

							if(!rs.next()) {

								stmt.execute("INSERT INTO compartment (name, abbreviation) VALUES('"+compartment+"','"+abb+"')");
								rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
								rs.next();
							}
							compartment_id_map.put(compartment, rs.getString(1));
						}
					}

					rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+locusTag+"';");
					if(rs.next()) {

						String idgene = rs.getString(1);

						rs = stmt.executeQuery("SELECT gene_idgene FROM gene_has_compartment " +
								"WHERE gene_idgene = '"+idgene+"' AND primaryLocation = true;");

						if(!rs.next()) {

							stmt.execute("INSERT INTO gene_has_compartment (gene_idgene, compartment_idcompartment, primaryLocation, score) " +
									"VALUES('"+idgene+"','"+compartment_id_map.get(geneComp.getPrimary_location())+"',"+true+",'"+geneComp.getPrimary_score()+"')");
						}

						compartments = new ArrayList<String>();

						for(String loc : geneComp.getSecondary_location().keySet()) {

							compartments.add(loc);
						}

						for(String compartment:compartments) {

							rs = stmt.executeQuery("SELECT gene_idgene " +
									"FROM gene_has_compartment " +
									"WHERE gene_idgene = "+idgene+" " +
									"AND compartment_idcompartment = "+compartment_id_map.get(compartment)+"  ;");

							if(!rs.next()) {
								
								stmt.execute("INSERT INTO gene_has_compartment (gene_idgene, compartment_idcompartment, primaryLocation, score) " +
										"VALUES("+idgene+","+compartment_id_map.get(compartment)+",false,'"+geneComp.getSecondary_location().get(compartment)+"')");
							}
						}
					}
					this.processCompartments.initProcessCompartments(compartment_id_map.keySet());
				}
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),counter,this.psortMap.keySet().size());
				counter++;
			}
			stmt.close();
			return true;
		}
		catch (SQLException e) {e.printStackTrace();}
		return false;

	}


	/**
	 * @param ignoreList
	 * @return
	 */
	public boolean assignCompartmentsToMetabolicReactions(List<String> ignoreList) {

		Statement stmt;
		ResultSet rs;
		try {

			Map<String,String> compartmentsAbb_ids = this.processCompartments.getIdCompartmentAbbMap(connection);
			Map<String,String> idCompartmentAbbIdMap = this.processCompartments.getCompartmentAbbIdMap(connection);

			Map<String, List<String>> enzymes_reactions = new HashMap<String, List<String>>();
			List<String> reactions_ids;

			stmt = this.connection.createStatement();

			rs = stmt.executeQuery("SELECT idreaction, enzyme_ecnumber, enzyme_protein_idprotein " +
					"FROM reaction " +
					"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
					"WHERE (source = 'KEGG' OR source = 'HOMOLOGY' OR source = 'MANUAL') AND originalReaction");

			while(rs.next()) {

				reactions_ids = new ArrayList<String>();

				if(enzymes_reactions.containsKey(rs.getString(2))) {

					reactions_ids = enzymes_reactions.get(rs.getString(2));	
				}

				reactions_ids.add(rs.getString(1));
				enzymes_reactions.put(rs.getString(2),reactions_ids);
			}

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<String, List<String>> enzymes_compartments = new HashMap<String, List<String>>();
			List<String> compartments;
			rs = stmt.executeQuery("SELECT DISTINCT compartment_idcompartment, enzyme_ecnumber, enzyme_protein_idprotein " +
					"FROM subunit " +
					"INNER JOIN gene_has_compartment ON subunit.gene_idgene = gene_has_compartment.gene_idgene " +
					"ORDER BY enzyme_ecnumber;");

			while(rs.next()) {

				compartments = new ArrayList<String>();

				if(enzymes_compartments.containsKey(rs.getString(2))) {

					compartments = enzymes_compartments.get(rs.getString(2));	
				}

				compartments.add(rs.getString(1));
				enzymes_compartments.put(rs.getString(2),compartments);
			}

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String ecnumber : enzymes_reactions.keySet()) {

				for(String idreaction: enzymes_reactions.get(ecnumber)) {

					String name="", equation="", source="";
					boolean reversible = false, inModel = false, isGeneric = false, isSpontaneous = false, isNonEnzymatic = false;

					int i = 0 ;
					rs = stmt.executeQuery("SELECT name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source, originalReaction " +
							"FROM reaction " +
							"WHERE idreaction = "+idreaction+" AND originalReaction;");

					while (rs.next()) {

						name = rs.getString(1);
						equation = rs.getString(2);
						reversible = rs.getBoolean(3);
						inModel = rs.getBoolean(4);
						isGeneric = rs.getBoolean(5);
						isSpontaneous = rs.getBoolean(6);
						isNonEnzymatic = rs.getBoolean(7);
						source = rs.getString(8);
						i++;
					}

					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					i=0;
					rs = stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber " +
							"FROM reaction_has_enzyme WHERE reaction_idreaction = "+idreaction+" AND enzyme_ecnumber = '"+ecnumber+"';");
					Map<Integer,String> protein_id = new HashMap<Integer, String>();

					while (rs.next()) {

						protein_id.put(i, rs.getString(1));
						i++;
					}
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					rs = stmt.executeQuery("SELECT pathway_idpathway " +
							"FROM pathway_has_reaction WHERE reaction_idreaction = "+idreaction+" ;");
					List<String> pathway_id = new ArrayList<String>();

					while (rs.next()) {

						pathway_id.add(rs.getString(1));
					}

					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

					List<String> compound_idcompound = new ArrayList<String>(), 
							stoichiometric_coefficient = new ArrayList<String>(), 
							numberofchains = new ArrayList<String>();

					i = 0 ;
					rs = stmt.executeQuery("SELECT * " +
							"FROM stoichiometry " +
							"WHERE reaction_idreaction = "+idreaction+";");

					while (rs.next()) {

						compound_idcompound.add(i,rs.getString(3));
						stoichiometric_coefficient.add(i,rs.getString(5));
						numberofchains.add(i,rs.getString(6));
						i++;
					}

					if(enzymes_compartments.containsKey(ecnumber)) {

						Set<String> parsedCompartments = this.processCompartments.parseCompartments(enzymes_compartments.get(ecnumber), compartmentsAbb_ids,idCompartmentAbbIdMap, ignoreList);

						boolean inModelFromCompartment = inModel;

						//all compartments are assigned to the enzyme
						for(String idcompartment: parsedCompartments) {

							if(this.processCompartments.getIgnoreCompartmentsID().contains(idcompartment)) {

								inModelFromCompartment = false;
							}

							rs = stmt.executeQuery("SELECT * FROM reaction  " +
									" WHERE name = '"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"'" +
									" AND equation = '"+MySQL_Utilities.mysqlStrConverter(equation)+"'" +
									" AND reversible = "+reversible+
									" AND inModel = "+inModelFromCompartment+
									" AND isGeneric = "+isGeneric+
									" AND isSpontaneous = "+isSpontaneous+
									" AND isNonEnzymatic = "+isNonEnzymatic+
									" AND source = '"+source+"'" +
									" AND NOT originalReaction ;");

							if(!rs.next()) {

								stmt.execute("INSERT INTO reaction (name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source, originalReaction,compartment_idcompartment) " +
										"VALUES('"+MySQL_Utilities.mysqlStrConverter(name+"_C"+idcompartment)+"','"+MySQL_Utilities.mysqlStrConverter(equation)+"',"
										+reversible+","+inModelFromCompartment+","+isGeneric+","+isSpontaneous+","+isNonEnzymatic+",'"+source+"',false,"+idcompartment+");");

								rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
								rs.next();
							}

							String newReactionID = rs.getString(1);

							for(int j:protein_id.keySet()) {

								this.addReaction_has_Enzyme(protein_id.get(j), ecnumber, newReactionID);
							}

							for(String idPathway : pathway_id) {

								this.addPathway_has_Reaction(idPathway, newReactionID);
							}

							for(int j = 0 ; j < compound_idcompound.size(); j++ ) {

								rs = stmt.executeQuery("SELECT * FROM stoichiometry" +
										" WHERE reaction_idreaction = "+newReactionID+
										" AND compartment_idcompartment = "+idcompartment+
										" AND compound_idcompound = "+compound_idcompound.get(j)+
										" AND stoichiometric_coefficient = '"+stoichiometric_coefficient.get(j)+ "' "+
										" AND numberofchains = '"+numberofchains.get(j)+ "' ;");

								if(!rs.next()) {

									stmt.execute("INSERT INTO stoichiometry (reaction_idreaction, compound_idcompound, compartment_idcompartment, stoichiometric_coefficient, numberofchains) " +
											"VALUES("+newReactionID+","+compound_idcompound.get(j)+","+idcompartment+",'"+stoichiometric_coefficient.get(j)+ "','"+numberofchains.get(j)+ "');");
								}
							}
						}
					}
					else {

						String idcompartment = idCompartmentAbbIdMap.get(this.processCompartments.getInteriorCompartment().toLowerCase())+"";

						// if no compartment is assigned to enzyme it goes to the interior of the cell
						rs = stmt.executeQuery("SELECT * FROM reaction  " +
								" WHERE name = '"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"'" +
								" AND equation = '"+MySQL_Utilities.mysqlStrConverter(equation)+"'" +
								" AND reversible = "+reversible+
								" AND inModel = "+inModel+
								" AND isGeneric = "+isGeneric+
								" AND isSpontaneous = "+isSpontaneous+
								" AND isNonEnzymatic = "+isNonEnzymatic+
								" AND source = '"+source+"'" +
								" AND NOT originalReaction ;");

						if(!rs.next()) {

							stmt.execute("INSERT INTO reaction (name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source, originalReaction, compartment_idcompartment) " +
									"VALUES('"+MySQL_Utilities.mysqlStrConverter(name+"_C"+idcompartment)+"','"+MySQL_Utilities.mysqlStrConverter(equation)+"',"
									+reversible+","+inModel+","+isGeneric+","+isSpontaneous+","+isNonEnzymatic+",'"+source+"',false,"+idcompartment+");");

							rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
							rs.next();
						}

						String newReactionID = rs.getString(1);

						for(int j:protein_id.keySet()) {

							this.addReaction_has_Enzyme(protein_id.get(j), ecnumber, newReactionID);
						}

						for(String idPathway : pathway_id) {

							this.addPathway_has_Reaction(idPathway, newReactionID);
						}
						
						rs.close();

						for(int j = 0 ; j < compound_idcompound.size(); j++ ) {

							rs = stmt.executeQuery("SELECT * FROM stoichiometry" +
									" WHERE reaction_idreaction = "+newReactionID+
									" AND compartment_idcompartment = "+idcompartment+
									" AND compound_idcompound = "+compound_idcompound.get(j)+
									" AND stoichiometric_coefficient = '"+stoichiometric_coefficient.get(j)+ "' "+
									" AND numberofchains = '"+numberofchains.get(j)+ "' ;");

							if(!rs.next()) {

								stmt.execute("INSERT INTO stoichiometry (reaction_idreaction, compound_idcompound, compartment_idcompartment, stoichiometric_coefficient, numberofchains) " +
										"VALUES("+newReactionID+","+compound_idcompound.get(j)+","+idcompartment+",'"+stoichiometric_coefficient.get(j)+ "','"+numberofchains.get(j)+ "');");
							}
						}
					}
					rs.close();
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//if no enzyme is assigned to the reaction

			reactions_ids = new ArrayList<String>();
			rs = stmt.executeQuery("SELECT distinct idreaction " +
					"FROM reaction "+
					"LEFT JOIN reaction_has_enzyme ON reaction.idreaction = reaction_has_enzyme.reaction_idreaction " +
					"WHERE (source = 'KEGG' OR source = 'HOMOLOGY' OR source = 'MANUAL') AND reaction_has_enzyme.enzyme_ecnumber IS NULL AND originalReaction;");

			while(rs.next()) {

				reactions_ids.add(rs.getString(1));
			}

			String idcompartment = idCompartmentAbbIdMap.get(this.processCompartments.getInteriorCompartment().toLowerCase())+"";

			for(String idreaction: reactions_ids) {

				String name="", equation="", source="";
				boolean reversible = false, inModel = false, isGeneric = false, isSpontaneous = false, isNonEnzymatic = false;

				int i = 0 ;
				rs = stmt.executeQuery("SELECT name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source " +
						"FROM reaction WHERE idreaction = "+idreaction+" AND originalReaction;");

				while (rs.next()) {

					name = rs.getString(1);
					equation = rs.getString(2);
					reversible = rs.getBoolean(3);
					inModel = rs.getBoolean(4);
					isGeneric = rs.getBoolean(5);
					isSpontaneous = rs.getBoolean(6);
					isNonEnzymatic = rs.getBoolean(7);
					source = rs.getString(8);
					i++;
				}

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				i=0;
				rs = stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme WHERE reaction_idreaction = "+idreaction+" ;");
				Map<Integer,String> protein_id = new HashMap<Integer, String>();
				Map<Integer,String> ec_number = new HashMap<Integer, String>();

				while (rs.next()) {

					protein_id.put(i, rs.getString(1));
					ec_number.put(i, rs.getString(2));
					i++;
				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				rs = stmt.executeQuery("SELECT pathway_idpathway FROM pathway_has_reaction WHERE reaction_idreaction = "+idreaction+" ;");
				List<String> pathway_id = new ArrayList<String>();

				while (rs.next()) {

					pathway_id.add(rs.getString(1));
					i++;
				}

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				List<String> compound_idcompound = new ArrayList<String>(), 
						stoichiometric_coefficient = new ArrayList<String>(), 
						numberofchains = new ArrayList<String>();

				i = 0 ;
				rs = stmt.executeQuery("SELECT * FROM stoichiometry WHERE reaction_idreaction = "+idreaction+";");


				while (rs.next()) {

					compound_idcompound.add(i,rs.getString(3));
					stoichiometric_coefficient.add(i,rs.getString(5));
					numberofchains.add(i,rs.getString(6));
					i++;
				}

				rs = stmt.executeQuery("SELECT * FROM reaction  " +
						" WHERE name = '"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"'" +
						" AND equation = '"+MySQL_Utilities.mysqlStrConverter(equation)+"'" +
						" AND reversible = "+reversible+
						" AND inModel = "+inModel+
						" AND isGeneric = "+isGeneric+
						" AND isSpontaneous = "+isSpontaneous+
						" AND isNonEnzymatic = "+isNonEnzymatic+
						" AND source = '"+source+"'" +
						" AND NOT originalReaction ;");

				if(!rs.next()) {

					stmt.execute("INSERT INTO reaction (name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source, originalReaction, compartment_idcompartment) " +
							"VALUES('"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"','"+MySQL_Utilities.mysqlStrConverter(equation)+"'," +
							""+reversible+","+inModel+","+isGeneric+","+isSpontaneous+","+isNonEnzymatic+",'"+source+"',false,"+idcompartment+");");
					rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
				}

				String newReactionID = rs.getString(1);

				for(int j:protein_id.keySet()) {

					this.addReaction_has_Enzyme(protein_id.get(j), ec_number.get(j), newReactionID);
				}

				for(String idPathway : pathway_id) {

					this.addPathway_has_Reaction(idPathway, newReactionID);
				}

				for(int j = 0 ; j < compound_idcompound.size(); j++ ) {

					rs = stmt.executeQuery("SELECT * FROM stoichiometry " +
							"WHERE reaction_idreaction = "+newReactionID+
							" AND compartment_idcompartment = "+idcompartment+
							" AND compound_idcompound = "+compound_idcompound.get(j)+
							" AND stoichiometric_coefficient = '"+stoichiometric_coefficient.get(j)+ "' "+
							" AND numberofchains = '"+numberofchains.get(j)+ "' ;");

					if(!rs.next()) {

						stmt.execute("INSERT INTO stoichiometry (reaction_idreaction, compound_idcompound, compartment_idcompartment, stoichiometric_coefficient, numberofchains) " +
								"VALUES("+newReactionID+","+compound_idcompound.get(j)+","+idcompartment+",'"+stoichiometric_coefficient.get(j)+ "','"+numberofchains.get(j)+ "');");
					}
				}
			}
			rs.close();
			stmt.close();
			return true;
		}
		catch (Exception e) { 

			e.printStackTrace();
		}

		return false;
	}


	/**
	 * @param ignoreList
	 * @return
	 * @throws Exception 
	 */
	public boolean assignCompartmentsToTransportReactions(List<String> ignoreList) throws Exception {

		Map<String,String> idCompartmentMap = this.processCompartments.getIdCompartmentAbbMap(connection);

		List<String> reactionReversibility = this.getReversibleReactions();

		Map<String,String> idCompartmentAbbIdMap = this.processCompartments.getCompartmentAbbIdMap(connection);

		Map<String, List<String>> transportProteins_reactions = new HashMap<String, List<String>>();
		List<String> reactions_ids;

		Statement stmt = this.connection.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT DISTINCT idreaction, enzyme_ecnumber, enzyme_protein_idprotein FROM reaction " +
				"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
				"WHERE source = 'TRANSPORTERS' AND originalReaction");

		while(rs.next()) {

			String key = rs.getString(2).concat("_").concat(rs.getString(3));
			reactions_ids = new ArrayList<String>();

			if(transportProteins_reactions.containsKey(key)) {

				reactions_ids = transportProteins_reactions.get(key);	
			}

			reactions_ids.add(rs.getString(1));
			transportProteins_reactions.put(key,reactions_ids);
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


		Map<String, List<String>> transportProteins_compartments = new HashMap<String, List<String>>();
		List<String> compartments;
		rs = stmt.executeQuery("SELECT DISTINCT compartment_idcompartment, enzyme_ecnumber, enzyme_protein_idprotein FROM subunit " +
				"INNER JOIN gene_has_compartment ON subunit.gene_idgene = gene_has_compartment.gene_idgene " +
				"ORDER BY enzyme_ecnumber;");

		while(rs.next()) {

			String key = rs.getString(2).concat("_").concat(rs.getString(3));
			compartments = new ArrayList<String>();

			if(transportProteins_compartments.containsKey(key)) {

				compartments = transportProteins_compartments.get(key);	
			}
			compartments.add(rs.getString(1));
			transportProteins_compartments.put(key,compartments);
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		for(String transporter : transportProteins_reactions.keySet()) {

			for(String idreaction: transportProteins_reactions.get(transporter)) {

				String name="", equation="", source="";
				boolean reversible = false, inModel = false, isGeneric = false, isSpontaneous = false, isNonEnzymatic = false;
				int i = 0 ;
				rs = stmt.executeQuery("SELECT name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source " +
						"FROM reaction WHERE originalReaction AND idreaction = "+idreaction+";");

				while (rs.next()) {

					name = rs.getString(1);
					equation = rs.getString(2);
					reversible = rs.getBoolean(3);
					inModel = rs.getBoolean(4);
					isGeneric = rs.getBoolean(5);
					isSpontaneous = rs.getBoolean(6);
					isNonEnzymatic = rs.getBoolean(7);
					source = rs.getString(8);
					i++;
				}

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				i=0;

				String tcnumber = transporter.split("_")[0];
				rs = stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme WHERE reaction_idreaction = "+idreaction+" AND enzyme_ecnumber = '"+tcnumber+"';");
				Map<Integer,String> protein_id = new HashMap<Integer, String>();

				while (rs.next()) {

					protein_id.put(i, rs.getString(1));
					i++;
				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				rs = stmt.executeQuery("SELECT pathway_idpathway FROM pathway_has_reaction WHERE reaction_idreaction = "+idreaction+" ;");
				List<String> pathway_id = new ArrayList<String>();

				while (rs.next()) {

					pathway_id.add(rs.getString(1));
					i++;
				}

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				List<String> compound_idcompound = new ArrayList<String>(), 
						compartment_idcompartment = new ArrayList<String>(),
						stoichiometric_coefficient = new ArrayList<String>(), 
						numberofchains = new ArrayList<String>();

				i = 0 ;
				rs = stmt.executeQuery("SELECT * FROM stoichiometry WHERE reaction_idreaction = "+idreaction+" ;");
				while (rs.next()) {

					compound_idcompound.add(i,rs.getString(3));
					compartment_idcompartment.add(i,rs.getString(4));
					stoichiometric_coefficient.add(i,rs.getString(5));
					numberofchains.add(i,rs.getString(6));
					i++;
				}

				if(transportProteins_compartments.containsKey(transporter)) {

					for(String idcompartment: transportProteins_compartments.get(transporter)) {

						String abb = idCompartmentMap.get(idcompartment);
						if(reactionReversibility.contains(idreaction) && abb.equalsIgnoreCase("extr")) {

							if(this.processCompartments.getInteriorCompartment().equalsIgnoreCase("cyto")) {

								abb = "PLAS";
							}
							else {

								abb = "outme";
							}
						}

						if(!abb.equalsIgnoreCase("unkn") && !abb.equalsIgnoreCase("cyt") && !abb.equalsIgnoreCase("cyto")
								&& !abb.equalsIgnoreCase("cytop") && !abb.equalsIgnoreCase("perip") && !abb.equalsIgnoreCase("CYSK")) {

							boolean inModelFromCompartment = inModel;

							if(ignoreList.contains(abb.toLowerCase())) {

								inModelFromCompartment = false;
							}

							rs = stmt.executeQuery("SELECT * FROM reaction" +
									" WHERE " +
									//" name = '"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"' AND " +
									" equation = '"+MySQL_Utilities.mysqlStrConverter(equation.replace("(out)","("+this.processCompartments.processCompartment("out", abb )+")").replace("(in)",
											"("+this.processCompartments.processCompartment("in", abb )+")"))+"'" +
											" AND reversible = "+reversible+
											" AND inModel = "+inModelFromCompartment+
											" AND isGeneric = "+isGeneric+
											" AND isSpontaneous = "+isSpontaneous+
											" AND isNonEnzymatic = "+isNonEnzymatic+
											" AND source = '"+source+"'" +
									" AND NOT originalReaction ;");

							if(!rs.next()) {

								stmt.execute("INSERT INTO reaction (name, equation, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic, source, originalReaction, compartment_idcompartment) " +
										"VALUES('"+MySQL_Utilities.mysqlStrConverter(name)+"_C"+idcompartment+"'," +
										"'"+MySQL_Utilities.mysqlStrConverter(equation.replace("(out)","("+this.processCompartments.processCompartment("out", abb )+")").replace("(in)",
												"("+this.processCompartments.processCompartment("in", abb )+")"))+"'," +
												""+reversible+","+inModelFromCompartment+","+isGeneric+","+isSpontaneous+","+isNonEnzymatic+",'"+source+"',false,"+idcompartment+");");
								rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
								rs.next();
							}

							String newReactionID = rs.getString(1);

							for(int j:protein_id.keySet()) {

								this.addReaction_has_Enzyme(protein_id.get(j), tcnumber, newReactionID);
							}

							for(String idPathway : pathway_id) {

								this.addPathway_has_Reaction(idPathway, newReactionID);
							}

							for(int j = 0 ; j < compartment_idcompartment.size(); j++ ) {

								String compartment = this.processCompartments.processCompartment(idCompartmentMap.get(compartment_idcompartment.get(j)), abb );

								if(!idCompartmentAbbIdMap.containsKey(compartment.toLowerCase())) {

									stmt.execute("INSERT INTO compartment (name, abbreviation) VALUES('"+compartment+"','"+compartment+"')");
									rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
									rs.next();
									idCompartmentAbbIdMap.put(compartment.toLowerCase(), rs.getString(1));
									rs.close();
								}

								rs = stmt.executeQuery("SELECT * FROM stoichiometry " +
										"WHERE reaction_idreaction = "+newReactionID+
										" AND compartment_idcompartment = "+idCompartmentAbbIdMap.get(compartment.toLowerCase())+
										" AND compound_idcompound = "+compound_idcompound.get(j)+
										" AND stoichiometric_coefficient = '"+stoichiometric_coefficient.get(j)+ "' "+
										" AND numberofchains = '"+numberofchains.get(j)+ "' ;");

								if(!rs.next()) {

									stmt.execute("INSERT INTO stoichiometry (reaction_idreaction, compound_idcompound, compartment_idcompartment, stoichiometric_coefficient, numberofchains) " +
											"VALUES("+newReactionID+","+compound_idcompound.get(j)+","+ idCompartmentAbbIdMap.get(compartment.toLowerCase()) +",'"+stoichiometric_coefficient.get(j)+ "','"+numberofchains.get(j)+ "');");
								}
							}
						}
					}
				}
			}
		}
		rs.close();
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
	private boolean addReaction_has_Enzyme(String idprotein, String ecNumber, String idReaction) throws SQLException {

		Connection conn = this.connection;
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme " +
				"WHERE reaction_idreaction = "+idReaction+" " +
				"AND enzyme_protein_idprotein = "+idprotein+" " +
				"AND enzyme_ecnumber = '"+ecNumber+"';");

		if(rs.next()) {

			return false;
		}
		else {

			stmt.execute("INSERT INTO reaction_has_enzyme (reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber) " +
					"VALUES("+idReaction+","+idprotein+",'"+ecNumber+"');");
		}
		stmt.close();

		return true;
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
					"VALUES("+idReaction+","+idPathway+");");
		}
		rs.close();
		stmt.close();

		return true;
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private List<String> getReversibleReactions() throws SQLException {

		List<String> reactionReversibility = new ArrayList<String>();

		Statement stmt = this.connection.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT idreaction FROM reaction WHERE source = 'TRANSPORTERS' AND reversible;");

		while(rs.next()) {

			reactionReversibility.add(rs.getString(1));
		}
		return reactionReversibility;
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {

		this.progress = progress;
	}

	/**
	 * 
	 */
	public void setCancel() {

		this.cancel = new AtomicBoolean(true);
	}
}
