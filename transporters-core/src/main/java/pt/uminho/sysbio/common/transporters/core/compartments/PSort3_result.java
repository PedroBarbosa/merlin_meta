package pt.uminho.sysbio.common.transporters.core.compartments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.sysbio.merlin.utilities.Pair;


public class PSort3_result implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7061067245990913130L;
	private String geneID;
	private List<Pair<String, Double>> compartments;
	
	/**
	 * @param geneID
	 */
	public PSort3_result(String geneID) {
		this.setGeneID(geneID);
		this.setCompartments(new ArrayList<Pair<String, Double>>());
	}
	
	/**
	 * @param compartmentID
	 * @param score
	 */
	public void addCompartment(String compartmentID, double score){
		this.compartments.add(new Pair<String, Double>(compartmentID,score));
	}

	/**
	 * @return the geneID
	 */
	public String getGeneID() {
		return geneID;
	}

	/**
	 * @param geneID the geneID to set
	 */
	public void setGeneID(String geneID) {
		this.geneID = geneID;
	}

	/**
	 * @return the compartments
	 */
	public List<Pair<String, Double>> getCompartments() {
		return compartments;
	}

	/**
	 * @param compartments the compartments to set
	 */
	public void setCompartments(List<Pair<String, Double>> compartments) {
		this.compartments = compartments;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PSort3_result ["
				+ (this.geneID != null ? "geneID=" + this.geneID + ", " : "")
				+ (this.compartments != null ? "compartments=" + this.compartments : "")
				+ "]";
	}
	
}
