package datatypes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CVTerm.Type;

import pt.uminho.sysbio.common.biocomponents.container.io.readers.MerlinDBReader;
import pt.uminho.sysbio.common.biocomponents.container.io.readers.merlinAux.CompartmentContainer;
import pt.uminho.sysbio.common.biocomponents.container.io.readers.merlinAux.MetaboliteContainer;
import pt.uminho.sysbio.common.biocomponents.container.io.readers.merlinAux.ReactionContainer;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;


/**
 * @author ODias
 *
 */
/**
 * @author Oscar
 *
 */
public class SBMLFile {

	private SBMLBuilder sbml;
	private boolean isCompartmentalisedModel;
	private Map<String, List<MetaboliteContainer>> reactionMetabolites;
	private Map<String, ReactionContainer> reactions;
	private Map<String, CompartmentContainer> compartments;
	private int compartmentCounter;
	private Map<String, String> compartmentID;
	private String  outsideID;
	private boolean generateFormulae;
	private String biomassEquationID;

	/**
	 * @param msqlmt
	 * @param filePath
	 * @param sbmlFileID
	 * @param isCompartmentalisedModel
	 * @param validateSBML
	 * @param generateFormulae
	 * @param biomassEquationID 
	 */
	public SBMLFile(MySQLMultiThread msqlmt, String filePath, String sbmlFileID, boolean isCompartmentalisedModel, boolean validateSBML, boolean generateFormulae, String biomassEquationID) {

		this.sbml= new SBMLBuilder(sbmlFileID,filePath,2,1);
		this.compartmentCounter = 1;
		this.compartmentID = new TreeMap<String, String>();
		this.isCompartmentalisedModel = isCompartmentalisedModel;
		this.compartments = new HashMap<String, CompartmentContainer>();
		this.reactionMetabolites = new HashMap<String, List<MetaboliteContainer>>();
		this.reactions = new HashMap<String, ReactionContainer>();
		this.outsideID=null;
		this.generateFormulae = generateFormulae;
		this.setBiomassEquationName(biomassEquationID);

		String aux="";

		if(this.isCompartmentalisedModel) {

			aux = aux.concat(" NOT originalReaction");
		}
		else {

			aux = aux.concat(" originalReaction");
		}
		this.getReactions(msqlmt, aux);
		this.getStoichiometry(msqlmt, aux);
		this.getCompartments(msqlmt);
		this.buildModel();
		this.getSbml().createSBMLDocument(validateSBML);
	}

	/**
	 * @param link
	 * @param conditions
	 */
	private void getReactions(MySQLMultiThread link, String conditions) {

		Statement stmt;
		try {

			Connection connection = new Connection(link);

			stmt = connection.createStatement();


			ResultSet rs = stmt.executeQuery("SELECT DISTINCT idreaction, name, equation, reversible, compartment_idcompartment, notes " +
					"FROM reaction WHERE inModel AND " +conditions );

			while(rs.next()) {

				ReactionContainer reactionContainer = null;

				if(this.reactions.containsKey(rs.getString(1))) {

					System.out.println("same reaction "+rs.getString(1));
				}
				else {

					reactionContainer = new ReactionContainer(rs.getString(1), rs.getString(2), rs.getString(3), rs.getBoolean(4), rs.getString(5), rs.getString(2));

					if(rs.getString(6)!=null) {

						reactionContainer.setNotes(rs.getString(6));
					}
				}
				reactionContainer = this.reactions.put(rs.getString(1), reactionContainer);
			}


			rs = stmt.executeQuery("SELECT reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme ORDER BY reaction_idreaction" );

			while(rs.next()) {

				if(this.reactions.containsKey(rs.getString(1)) && !rs.getString("enzyme_ecnumber").contains(".-")) {

					ReactionContainer reactionContainer = this.reactions.get(rs.getString(1));

					Set<String> enzymeSet = new TreeSet<String>();
					if(reactionContainer.getEnzymes()!=null) {

						enzymeSet = reactionContainer.getEnzymes();
					}

					if(rs.getString(3)!=null) {

						enzymeSet.add(rs.getString(3));
						reactionContainer.setEnzymes(enzymeSet);
					}
					reactionContainer = this.reactions.put(rs.getString(1), reactionContainer);
				}
			}

			rs = stmt.executeQuery("SELECT reaction_idreaction, pathway_idpathway, pathway.name " +
					"FROM pathway_has_reaction " +
					"INNER JOIN pathway ON (pathway_idpathway = pathway.idpathway)" +
					"ORDER BY reaction_idreaction" );

			while(rs.next()) {

				if(this.reactions.containsKey(rs.getString(1))) {

					ReactionContainer reactionContainer = this.reactions.get(rs.getString(1));

					Set<String> pathways = new TreeSet<String>();
					if(reactionContainer.getPathways()!=null) {

						pathways = reactionContainer.getPathways();
					}
					if(rs.getString(2)!=null) {

						pathways.add(rs.getString(3));
						reactionContainer.setPathways(pathways);
					}
					reactionContainer = this.reactions.put(rs.getString(1), reactionContainer);
				}
			}

			rs = stmt.executeQuery("SELECT DISTINCT reaction_idreaction, name, locusTag, subunit.enzyme_ecnumber " +
					"FROM reaction_has_enzyme " +
					"INNER JOIN subunit ON (subunit.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein AND subunit.enzyme_ecnumber = reaction_has_enzyme.enzyme_ecnumber) " +
					"INNER JOIN gene ON (gene_idgene = gene.idgene) " +
					"WHERE (note is null OR note NOT LIKE 'unannotated') " +
					"ORDER BY reaction_idreaction;");

			while(rs.next()) {

				if(this.reactions.containsKey(rs.getString(1)) && !rs.getString("subunit.enzyme_ecnumber").contains(".-")) {

					ReactionContainer reactionContainer = this.reactions.get(rs.getString(1));

					Set<String> genes = new TreeSet<String>();
					if(reactionContainer.getGenes()!=null) {

						genes = reactionContainer.getGenes();
					}
					if(rs.getString(2)==null || rs.getString(2).isEmpty()) {

						genes.add(rs.getString(3).trim());
						reactionContainer.setGenes(genes);
					}
					else {

						genes.add(rs.getString(2).replace(" ","").replace(",","_").replace("/","_").replace("\\","_").replace("-","_").trim()+"_"+rs.getString(3).trim());
						reactionContainer.setGenes(genes);
					}
					reactionContainer = this.reactions.put(rs.getString(1), reactionContainer);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param link
	 * @param conditions
	 */
	private void getStoichiometry(MySQLMultiThread link, String conditions) {

		Statement stmt;
		try {

			Connection connection = new Connection(link);

			stmt = connection.createStatement();


			ResultSet rs = stmt.executeQuery("SELECT idstoichiometry, reaction_idreaction, compound_idcompound, stoichiometry.compartment_idcompartment, " +
					"stoichiometric_coefficient, numberofchains, compound.name, compound.formula, compound.kegg_id " +
					"FROM reaction " +
					"LEFT JOIN stoichiometry ON (stoichiometry.reaction_idreaction = idreaction) " +
					"LEFT JOIN compound ON (stoichiometry.compound_idcompound = compound.idcompound) " +
					"WHERE inModel AND " +conditions );

			while(rs.next()) {

				if(this.reactions.containsKey(rs.getString(2))) {

					if(!rs.getString(5).contains("m") && !rs.getString(5).contains("n")) {

						List<MetaboliteContainer> metabolitesContainer = new ArrayList<MetaboliteContainer>();

						if(this.reactionMetabolites.containsKey(rs.getString(2))) {

							metabolitesContainer = this.reactionMetabolites.get(rs.getString(2));
						}

						MetaboliteContainer metabolite = new MetaboliteContainer(rs.getString(3), rs.getString(7), rs.getString(8), rs.getDouble(5), rs.getString(6), rs.getString(4));
						metabolite.setKegg_id(rs.getString(9));
						metabolitesContainer.add(metabolite);

						this.reactionMetabolites.put(rs.getString(2), metabolitesContainer);
					}
					else {

						this.reactionMetabolites.remove(rs.getString(2));
						this.reactions.remove(rs.getString(2));
					}
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param link
	 * @param conditions
	 */
	private void getCompartments(MySQLMultiThread link) {

		Statement stmt;
		try {

			Connection connection = new Connection(link);

			stmt = connection.createStatement();


			ResultSet rs = stmt.executeQuery("SELECT idcompartment, name, abbreviation FROM compartment");

			while(rs.next()) {

				CompartmentContainer compartmentContainer = new CompartmentContainer(rs.getString(1), rs.getString(2), rs.getString(3));
				this.compartments.put(rs.getString(1), compartmentContainer);

				if((rs.getString(2).equalsIgnoreCase("extracellular") && this.isCompartmentalisedModel) || (rs.getString(2).equalsIgnoreCase("outside") && !this.isCompartmentalisedModel)) {

					this.outsideID=this.getCompartmentID(rs.getString(1));
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param compartment
	 * @return
	 */
	private String getCompartmentID(String compartment) {

		if(!this.compartmentID.containsKey(compartment)) {

			String id = SBMLFile.buildID("C_", this.compartmentCounter);
			this.getSbml().addCompartment(id, this.compartments.get(compartment).getName(), this.outsideID);
			this.compartmentID.put(compartment, id);
			this.compartmentCounter++ ;

		}
		return this.compartmentID.get(compartment);
	}

	/**
	 * 
	 */
	private void buildModel() {

		int reactionsCounter = 1 ;
		int metabolitesCounter = 1;
		int enzymesCounter = 1;

		Map<String,String> enzymesID = new TreeMap<String, String>();
		Map<String,String> compoundCompartmentID = new TreeMap<String, String>();
		Map<String,String> metabolites_formula = new TreeMap<String, String>();		

		for(String reaction_id : this.reactions.keySet()) {

			ReactionContainer reaction = this.reactions.get(reaction_id);
			String name = reaction.getName()+"__("+reaction.getEquation().replace(" ", "")+")";

			String rid = SBMLFile.buildID("R_", reactionsCounter)/*+"_"+reaction.getName().replace(" ", "_").replace("\t", "_").replace("-", "_")*/;

			double upper_bound = 999999;
			double lower_bound = 0;
			boolean biomassEquation = reaction.getName().equalsIgnoreCase(this.getBiomassEquationID());

			if(reaction.isReversibility())
				lower_bound = -999999;

			this.getSbml().addReaction(rid, name, reaction.getName(), reaction.isReversibility(), SBMLFile.getReactionNote(reaction.getGenes(), null, reaction.getPathways(), reaction.getEnzymes(), reaction.getNotes()), lower_bound, upper_bound, biomassEquation);
			reactionsCounter++ ;

			if(this.reactions.get(reaction_id).getEnzymes() != null) {

				for(String enzyme : this.reactions.get(reaction_id).getEnzymes()) {

					String enzyme_surrogate = enzyme.concat("_").concat(this.getCompartmentID(reaction.getLocalisation()));
					String eid;
					if(enzymesID.containsKey(enzyme_surrogate)) {

						eid = enzymesID.get(enzyme_surrogate);
					}
					else {

						eid = SBMLFile.buildID("E_", enzymesCounter);

						this.getSbml().addSpecies(eid, enzyme, this.getCompartmentID(reaction.getLocalisation()), SBMLFile.getAnnotation(enzyme, ""));
						enzymesID.put(enzyme_surrogate, eid);
						enzymesCounter++;
					}
					this.getSbml().setReactionEnzyme(eid, rid);
				}
			}

			if(this.reactionMetabolites.get(reaction_id) != null) {

				for(MetaboliteContainer metabolite : this.reactionMetabolites.get(reaction_id)) {

					String metabolite_surrogate = metabolite.getMetaboliteID().concat("_").concat(metabolite.getCompartment_name());
					String mid;
					if(compoundCompartmentID.containsKey(metabolite_surrogate)) {

						mid = compoundCompartmentID.get(metabolite_surrogate);
					}
					else {

						mid = SBMLFile.buildID("M_", metabolitesCounter);
						compoundCompartmentID.put(metabolite_surrogate,mid);

						String sbmlName="";

						if(metabolite.getKegg_id() != null) {

							sbmlName=sbmlName.concat(metabolite.getKegg_id());
						}

						if(metabolite.getName() != null) {

							if(sbmlName!=null && !sbmlName.isEmpty()) {

								sbmlName=sbmlName.concat("_");
							}
							sbmlName=sbmlName.concat(metabolite.getName());
						}

						if(metabolite.getFormula() != null) {

							if(sbmlName!=null && !sbmlName.isEmpty()) {

								sbmlName=sbmlName.concat("_");
							}
							sbmlName=sbmlName.concat(metabolite.getFormula());
						}
						this.getSbml().addSpecies( mid, sbmlName, this.getCompartmentID(metabolite.getCompartment_name()), SBMLFile.getAnnotation(metabolite.getKegg_id(), metabolite.getFormula()));

						if(!this.getSbml().getSbmlModel().getCompartment(this.getCompartmentID(metabolite.getCompartment_name())).isSetOutside()) {

							String drain_id = "R_EX_"+mid.substring(2)+"_";
							String drain_name = "R_EX_"+sbmlName;

							upper_bound = 999999;
							//lower_bound = -99999;
							lower_bound = 0;

							this.getSbml().addReaction(drain_id, drain_name, null, false, null, lower_bound, upper_bound, false);
							this.getSbml().setReactionCompound(mid, drain_id, new Double(-1));
							//}
						}

						metabolitesCounter++ ;

						metabolites_formula.put(mid, metabolite.getFormula());
					}

					this.getSbml().setReactionCompound(mid, rid, new Double(metabolite.getStoichiometric_coefficient()));

				}
			}
			else {

				System.err.println(reaction_id +"\t" +reaction.getKegg_id());
			}

		}

		if(this.generateFormulae && metabolites_formula.size()>0) {

			try {

				FileWriter fstream = new FileWriter(this.sbml.filePath.replace(".xml","")+"_formulae.txt");
				BufferedWriter out = new BufferedWriter(fstream);

				for(String mid:metabolites_formula.keySet()) {

					out.write(mid+"\t"+metabolites_formula.get(mid)+"\n");
				}
				out.close();
			}
			catch (Exception e){

				System.err.println("Error: " + e.getMessage());
			}
		}
	}

	/**
	 * @param type
	 * @param counter
	 * @return
	 */
	private static String buildID(String type, int counter) {

		if(counter<10 ){return type.concat("0000"+counter);}
		if(counter<100) {return type.concat("000"+counter);}
		if(counter<1000) {return type.concat("00"+counter);}
		if(counter<10000) {return type.concat("0"+counter);}

		return type.concat("_"+counter);
	}

	/**
	 * @param urn_id
	 * @param formula
	 * @return
	 */
	public static Annotation getAnnotation(String urn_id, String formula) {

		Annotation annotation = new Annotation();
		CVTerm cvTerm = new CVTerm(Qualifier.BQB_IS);
		cvTerm.setQualifierType(Type.BIOLOGICAL_QUALIFIER);
		annotation.addCVTerm(cvTerm);


		if(urn_id.startsWith("C")) {

			if(formula!=null) {

				cvTerm.addResource("FORMULA:" + formula.toUpperCase());
			}
			cvTerm.addResource("urn:miriam:kegg.compound:" + urn_id);
			//cvTerm.addResource("<url:element>http://www.genome.jp/dbget-bin/www_bget?gl:"+urn_id+ "</url:element>");
			//annotation.appendNoRDFAnnotation("http://www.genome.jp/dbget-bin/www_bget?gl:"+urn_id);
			cvTerm.addResource("http://www.genome.jp/dbget-bin/www_bget?cpd:"+urn_id);
		}

		else if(urn_id.startsWith("G")) {

			if(formula!=null) {

				cvTerm.addResource("FORMULA: " + formula.toUpperCase());
			}
			cvTerm.addResource("urn:miriam:kegg.glycan:" + urn_id);
			cvTerm.addResource("http://www.genome.jp/dbget-bin/www_bget?gl:"+urn_id);
		}

		else if(urn_id.startsWith("D")) {

			if(formula!=null) {

				cvTerm.addResource("FORMULA: " + formula.toUpperCase());
			}
			cvTerm.addResource("urn:miriam:kegg.drugs:" + urn_id);
			cvTerm.addResource("http://www.genome.jp/dbget-bin/www_bget?dr:"+urn_id);
		}

		else if (urn_id.contains("#")) {

			cvTerm.addResource("urn:miriam:tcdb:" + urn_id);
			cvTerm.addResource("http://www.tcdb.org/search/result.php?tc="+urn_id);
		}

		else if (urn_id.contains(".")) {

			cvTerm.addResource("urn:miriam:ec-code:" + urn_id);
			cvTerm.addResource("http://www.genome.jp/dbget-bin/www_bget?ec:"+urn_id);
		}
		return annotation;

	}

	/**
	 * @param genes
	 * @param proteins
	 * @param pathways
	 * @param enzymes
	 * @param notes
	 * @return
	 */
	public static Set<String> getReactionNote(Set<String> genes, Set<String> proteins, Set<String> pathways, Set<String> enzymes, String notes) {

		String genesNotes = MerlinDBReader.processReactionGenes(genes);
		String proteinsNotes = MerlinDBReader.processReactionProteins(proteins);
		String pathwaysNotes = MerlinDBReader.processReactionPathways(pathways);
		String enzymesNotes = MerlinDBReader.processReactionEnzymes(enzymes);
		Set<String> notesList = MerlinDBReader.processReactionNotes(notes);

		notesList = MerlinDBReader.getGeneRules(genesNotes, notesList).getB();
		notesList = MerlinDBReader.getProteinRules(proteinsNotes, notesList).getB();
		notesList = MerlinDBReader.getPathwaysRules(pathwaysNotes, notesList).getB();
		notesList = MerlinDBReader.getEnzymesRules(enzymesNotes, notesList).getB();

		return notesList;		
	}


	/**
	 * @param notes
	 * @return
	 */
	public static Set<String> processReactionNotes(String notes) {

		Set<String> reactionNotes = new HashSet<>();
		//<html:p>GENE_ASSOCIATION: </html:p>
		//<html:p>PROTEIN_ASSOCIATION: "</html:p>
		//<html:p>SUBSYSTEM: S_Tyrosine__Tryptophan__and_Phenylalanine_Metabolism</html:p>
		//<html:p>PROTEIN_CLASS: 1.13.11.27</html:p>
		//<html:p>GENE_ASSOCIATION: ( YOL096C  and  YDR204W  and  YML110C  and  YGR255C  and  YOR125C  and  YGL119W  and  YLR201C )</html:p>
		//<html:p>PROTEIN_ASSOCIATION: ( Coq3-m and Coq4-m and Coq5-m and Coq6-m and Coq7-m and Coq8-m and Coq9-m )"</html:p>
		//<html:p>SUBSYSTEM: S_Quinone_Biosynthesis</html:p>
		//<html:p>PROTEIN_CLASS: </html:p>


		if(notes!=null && !notes.trim().isEmpty()) {

			if(notes.contains("|")) {

				for(String note : notes.split(" \\| ")) {

					reactionNotes.add(note.replace(",","_").replace(" ","_").replace(":_",": ").replace("_AND_"," and ").replace("_OR_"," or ").trim());	 //.replace(")","").replace("(","_")
				}
			}
		}
		return reactionNotes; 
	}

	/**
	 * @return the sbml
	 */
	public SBMLBuilder getSbml() {
		return sbml;
	}

	/**
	 * @param sbml the sbml to set
	 */
	public void setSbml(SBMLBuilder sbml) {
		this.sbml = sbml;
	}
	
	/**
	 * @return the biomassEquationID
	 */
	public String getBiomassEquationID() {
		return biomassEquationID;
	}

	/**
	 * @param biomassEquationID the biomassEquationID to set
	 */
	public void setBiomassEquationName(String biomassEquationID) {
		this.biomassEquationID = biomassEquationID;
	}

}
