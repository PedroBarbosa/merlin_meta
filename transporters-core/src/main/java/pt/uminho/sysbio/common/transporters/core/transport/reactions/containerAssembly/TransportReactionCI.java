/**
 * 
 */
package pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly;

import java.util.Map;

import pt.uminho.sysbio.common.biocomponents.container.components.ReactionCI;
import pt.uminho.sysbio.common.biocomponents.container.components.StoichiometryValueCI;

/**
 * @author ODias
 *
 */
public class TransportReactionCI extends ReactionCI {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String transportType;
	protected Map<String,Map<String, MetabolitesOntology>> chebi_ontology_byGene;
	protected Map<String, Boolean> reversibilityConfirmed_byGene;
	protected Map<String, Map<String, String>> general_equation;
	protected Map<String, Boolean> isOriginalReaction_byGene;
	private Map<String,String> originalReactionID_byGene;

	
	
	/**
	 * @param shortName
	 * @param name
	 * @param reversible
	 * @param reactants
	 * @param products
	 */
	public TransportReactionCI(String shortName, String name, Boolean reversible, Map<String, StoichiometryValueCI> reactants,Map<String, StoichiometryValueCI> products) {

		super(shortName, name, reversible, reactants, products);

	}

	/**
	 * @return the transportType
	 */
	public String getTransportType() {
		return transportType;
	}

	/**
	 * @param transportType the transportType to set
	 */
	public void setTransportType(String transportType) {
		this.transportType = transportType;
	}


	/**
	 * @return the general_equation
	 */
	public Map<String, Map<String, String>> getGeneral_equation() {
		return general_equation;
	}

	/**
	 * @param general_equation the general_equation to set
	 */
	public void setGeneral_equation(Map<String, Map<String, String>> general_equation) {
		this.general_equation = general_equation;
	}

	/**
	 * @return the originalReactionID_byGene
	 */
	public Map<String,String> getOriginalReactionID_byGene() {
		return originalReactionID_byGene;
	}

	/**
	 * @param originalReactionID_byGene the originalReactionID_byGene to set
	 */
	public void setOriginalReactionID_byGene(
			Map<String,String> originalReactionID_byGene) {
		this.originalReactionID_byGene = originalReactionID_byGene;
	}

	/**
	 * @return the chebi_ontology_byGene
	 */
	public Map<String, Map<String, MetabolitesOntology>> getChebi_ontology_byGene() {
		return chebi_ontology_byGene;
	}

	/**
	 * @param chebi_ontology_byGene the chebi_ontology_byGene to set
	 */
	public void setChebi_ontology_byGene(
			Map<String, Map<String, MetabolitesOntology>> chebi_ontology_byGene) {
		this.chebi_ontology_byGene = chebi_ontology_byGene;
	}

	/**
	 * @return the isOriginalReaction_byGene
	 */
	public Map<String, Boolean> getIsOriginalReaction_byGene() {
		return isOriginalReaction_byGene;
	}

	/**
	 * @param isOriginalReaction_byGene the isOriginalReaction_byGene to set
	 */
	public void setIsOriginalReaction_byGene(
			Map<String, Boolean> isOriginalReaction_byGene) {
		this.isOriginalReaction_byGene = isOriginalReaction_byGene;
	}

	/**
	 * @return the reversibilityConfirmed_byGene
	 */
	public Map<String, Boolean> getReversibilityConfirmed_byGene() {
		return reversibilityConfirmed_byGene;
	}

	/**
	 * @param reversibilityConfirmed_byGene the reversibilityConfirmed_byGene to set
	 */
	public void setReversibilityConfirmed_byGene(
			Map<String, Boolean> reversibilityConfirmed_byGene) {
		this.reversibilityConfirmed_byGene = reversibilityConfirmed_byGene;
	}

}
