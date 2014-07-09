/**
 * 
 */
package remote.retriever.entry_types;

import java.util.List;

/**
 * @author ODias
 *
 */
public class Gene_entry implements Entity_entry {
	
	private String entry;
	private String name;
	private	List<String> dblinks;
	private List<String> orthologues;
	private	List<String> genes;
	private	List<String> modules;
	private String chromosome_name;
	private String  left_end_position, right_end_position, aasequence, aalength, ntsequence, ntlength;

	/**
	 * 
	 */
	public Gene_entry(String entry) {
		this.setEntry(entry);
	}

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(String entry) {
		this.entry = entry;
	}

	/**
	 * @return the entry
	 */
	public String getEntry() {
		return entry;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param dblinks the dblinks to set
	 */
	public void setDblinks(List<String> dblinks) {
		this.dblinks = dblinks;
	}

	/**
	 * @return the dblinks
	 */
	public List<String> getDblinks() {
		return dblinks;
	}

	/**
	 * @param genes the genes to set
	 */
	public void setGenes(List<String> genes) {
		this.genes = genes;
	}

	/**
	 * @return the genes
	 */
	public List<String> getGenes() {
		return genes;
	}

	@Override
	public List<String> getNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNames(List<String> names) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param chromosome_name the chromosome_name to set
	 */
	public void setChromosome_name(String chromosome_name) {
		this.chromosome_name = chromosome_name;
	}

	/**
	 * @return the chromosome_name
	 */
	public String getChromosome_name() {
		return chromosome_name;
	}

	/**
	 * @param left_end_position the left_end_position to set
	 */
	public void setLeft_end_position(String left_end_position) {
		this.left_end_position = left_end_position;
	}

	/**
	 * @return the left_end_position
	 */
	public String getLeft_end_position() {
		return left_end_position;
	}

	/**
	 * @param right_end_position the right_end_position to set
	 */
	public void setRight_end_position(String right_end_position) {
		this.right_end_position = right_end_position;
	}

	/**
	 * @return the right_end_position
	 */
	public String getRight_end_position() {
		return right_end_position;
	}

	/**
	 * @param aasequence the aasequence to set
	 */
	public void setAasequence(String aasequence) {
		this.aasequence = aasequence;
	}

	/**
	 * @return the aasequence
	 */
	public String getAasequence() {
		return aasequence;
	}

	/**
	 * @param aalength the aalength to set
	 */
	public void setAalength(String aalength) {
		this.aalength = aalength;
	}

	/**
	 * @return the aalength
	 */
	public String getAalength() {
		return aalength;
	}

	/**
	 * @param ntsequence the ntsequence to set
	 */
	public void setNtsequence(String ntsequence) {
		this.ntsequence = ntsequence;
	}

	/**
	 * @return the ntsequence
	 */
	public String getNtsequence() {
		return ntsequence;
	}

	/**
	 * @param ntlength the ntlength to set
	 */
	public void setNtlength(String ntlength) {
		this.ntlength = ntlength;
	}

	/**
	 * @return the ntlength
	 */
	public String getNtlength() {
		return ntlength;
	}

	/**
	 * @param orthologues the orthologues to set
	 */
	public void setOrthologues(List<String> orthologues) {
		this.orthologues = orthologues;
	}

	/**
	 * @return the orthologues
	 */
	public List<String> getOrthologues() {
		return orthologues;
	}

	/**
	 * @return the modules
	 */
	public List<String> getModules() {
		return modules;
	}

	/**
	 * @param modules the modules to set
	 */
	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Gene_entry ["
				+ (entry != null ? "entry=" + entry + ", " : "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (dblinks != null ? "dblinks=" + dblinks + ", " : "")
				+ (orthologues != null ? "orthologues=" + orthologues + ", "
						: "")
				+ (genes != null ? "genes=" + genes + ", " : "")
				+ (modules != null ? "modules=" + modules + ", " : "")
				+ (chromosome_name != null ? "chromosome_name="
						+ chromosome_name + ", " : "")
				+ (left_end_position != null ? "left_end_position="
						+ left_end_position + ", " : "")
				+ (right_end_position != null ? "right_end_position="
						+ right_end_position + ", " : "")
				+ (aasequence != null ? "aasequence=" + aasequence + ", " : "")
				+ (aalength != null ? "aalength=" + aalength + ", " : "")
				+ (ntsequence != null ? "ntsequence=" + ntsequence + ", " : "")
				+ (ntlength != null ? "ntlength=" + ntlength : "") + "]";
	}

	
}
