package pt.uminho.sysbio.merge.databases.containers;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * @author pedro
 *
 */
public class BlastSetup {
	

	private String program,version, databaseID, eValue, matrix, wordSize, gapCosts;
	private int maxNumberOfAlignments;
	private ConcurrentLinkedQueue<GeneBlast> geneblast;

	
		


	public BlastSetup(String program, String version, String databaseID,
			String eValue, String matrix, String wordSize, String gapCosts,
			int maxNumberOfAlignments) {
		super();
		this.program = program;
		this.version = version;
		this.databaseID = databaseID;
		this.eValue = eValue;
		this.matrix = matrix;
		this.wordSize = wordSize;
		this.gapCosts = gapCosts;
		this.maxNumberOfAlignments = maxNumberOfAlignments;
	}

	/**
	 * @return the geneblast
	 */
	public ConcurrentLinkedQueue<GeneBlast> getGeneblast() {
		return geneblast;
	}

	/**
	 * @param geneblast the geneblast to set
	 */
	public void setGeneblast(ConcurrentLinkedQueue<GeneBlast> geneblast) {
		this.geneblast = geneblast;
	}

	/**
	 * @return the program
	 */
	public String getProgram() {
		return program;
	}

	/**
	 * @param program the program to set
	 */
	public void setProgram(String program) {
		this.program = program;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the databaseID
	 */
	public String getDatabaseID() {
		return databaseID;
	}

	/**
	 * @param databaseID the databaseID to set
	 */
	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

	/**
	 * @return the eValue
	 */
	public String geteValue() {
		return eValue;
	}

	/**
	 * @param eValue the eValue to set
	 */
	public void seteValue(String eValue) {
		this.eValue = eValue;
	}

	/**
	 * @return the matrix
	 */
	public String getMatrix() {
		return matrix;
	}

	/**
	 * @param matrix the matrix to set
	 */
	public void setMatrix(String matrix) {
		this.matrix = matrix;
	}

	/**
	 * @return the wordSize
	 */
	public String getWordSize() {
		return wordSize;
	}

	/**
	 * @param wordSize the wordSize to set
	 */
	public void setWordSize(String wordSize) {
		this.wordSize = wordSize;
	}

	/**
	 * @return the gapCosts
	 */
	public String getGapCosts() {
		return gapCosts;
	}

	/**
	 * @param gapCosts the gapCosts to set
	 */
	public void setGapCosts(String gapCosts) {
		this.gapCosts = gapCosts;
	}

	/**
	 * @return the maxNumberOfAlignments
	 */
	public int getMaxNumberOfAlignments() {
		return maxNumberOfAlignments;
	}

	/**
	 * @param maxNumberOfAlignments the maxNumberOfAlignments to set
	 */
	public void setMaxNumberOfAlignments(int maxNumberOfAlignments) {
		this.maxNumberOfAlignments = maxNumberOfAlignments;
	}


	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BlastSetup [ geneblast size=" + geneblast.size() + "]";
	}


}
