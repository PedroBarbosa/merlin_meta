package remote.retriever.entry_types;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Enzyme_entry implements Entity_entry {

	private String entry;
	private String name;
	private List<String> names;
	private	List<String> dblinks;
	private String enzyme_class;
	private List<String> orthologues;
	private	List<String> cofactors;
	private List<String> reactions;
	private	Map<String, String> pathways;
	private	List<String> genes;
	
	/**
	 * @param entry
	 */
	public Enzyme_entry(String entry) {
		this.setEntry(entry);
	}
	
	/**
	 * @param names the names to set
	 */
	public void setNames(List<String> names) {
		this.names = names;
	}
	/**
	 * @return the names
	 */
	public List<String> getNames() {
		return names;
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
	 * @param enzyme_class the enzyme_class to set
	 */
	public void setEnzyme_class(String enzyme_class) {
		this.enzyme_class = enzyme_class;
	}

	/**
	 * @return the enzyme_class
	 */
	public String getEnzyme_class() {
		return enzyme_class;
	}

	/**
	 * @param cofactors the cofactors to set
	 */
	public void setCofactors(List<String> cofactors) {
		this.cofactors = cofactors;
	}

	/**
	 * @return the cofactors
	 */
	public List<String> getCofactors() {
		return cofactors;
	}

	/**
	 * @param pathways the pathways to set
	 */
	public void setPathways(Map<String, String> pathways) {
		this.pathways = pathways;
	}

	/**
	 * @return the pathways
	 */
	public Map<String, String> getPathways() {
		return pathways;
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

	/**
	 * @param reactions the reactions to set
	 */
	public void setReactions(List<String> reactions) {
		this.reactions = reactions;
	}

	/**
	 * @return the reactions
	 */
	public List<String> getReactions() {
		return reactions;
	}

	/**
	 * @return the orthologues
	 */
	public List<String> getOrthologues() {
		return orthologues;
	}

	/**
	 * @param orthologues the orthologues to set
	 */
	public void setOrthologues(List<String> orthologues) {
		this.orthologues = orthologues;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return "Enzyme_entry ["
				+ (cofactors != null ? "cofactors="
						+ toString(cofactors, maxLen) + ", " : "")
				+ (dblinks != null ? "dblinks=" + toString(dblinks, maxLen)
						+ ", " : "")
				+ (entry != null ? "entry=" + entry + ", " : "")
				+ (enzyme_class != null ? "enzyme_class=" + enzyme_class + ", "
						: "")
				+ (genes != null ? "genes=" + toString(genes, maxLen) + ", "
						: "")
				+ (name != null ? "name=" + name + ", " : "")
				+ (names != null ? "names=" + toString(names, maxLen) + ", "
						: "")
				+ (orthologues != null ? "orthologues="
						+ toString(orthologues, maxLen) + ", " : "")
				+ (pathways != null ? "pathways="
						+ toString(pathways.entrySet(), maxLen) + ", " : "")
				+ (reactions != null ? "reactions="
						+ toString(reactions, maxLen) : "") + "]";
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
				&& i < maxLen; i++) {
			if (i > 0)
				builder.append(", ");
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
	
}
