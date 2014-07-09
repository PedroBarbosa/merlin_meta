package remote.retriever.entry_types;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Reaction_entry implements Entity_entry {
	
	private String entry;
	private String name;
	private List<String> names;
	private List<String> dblinks;
	private String equation;
	private	Map<String, String[]> reactantsStoichiometry;
	private	Map<String, String[]> productsStoichiometry;
	private List<String> enzymes, comments;
	private	Map<String, String> pathways;
	private boolean isGeneric, non_enzymatic, spontaneous;
	
	/**
	 * @param entry
	 */
	public Reaction_entry(String entry) {
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
	 * @param reactantsStoichiometry the reactantsStoichiometry to set
	 */
	public void setReactantsStoichiometry(Map<String, String[]> reactantsStoichiometry) {
		this.reactantsStoichiometry = reactantsStoichiometry;
	}
	/**
	 * @return the reactantsStoichiometry
	 */
	public Map<String, String[]> getReactantsStoichiometry() {
		return reactantsStoichiometry;
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
	 * @param enzymes the enzymes to set
	 */
	public void setEnzymes(List<String> enzymes) {
		this.enzymes = enzymes;
	}
	/**
	 * @return the enzymes
	 */
	public List<String> getEnzymes() {
		return enzymes;
	}
	/**
	 * @param productsStoichiometry the productsStoichiometry to set
	 */
	public void setProductsStoichiometry(Map<String, String[]> productsStoichiometry) {
		this.productsStoichiometry = productsStoichiometry;
	}
	/**
	 * @return the productsStoichiometry
	 */
	public Map<String, String[]> getProductsStoichiometry() {
		return productsStoichiometry;
	}
	/**
	 * @param equation the equation to set
	 */
	public void setEquation(String equation) {
		this.equation = equation;
	}
	/**
	 * @return the equation
	 */
	public String getEquation() {
		return equation;
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
	 * @param isGeneric the isGeneric to set
	 */
	public void setGeneric(boolean isGeneric) {
		this.isGeneric = isGeneric;
	}

	/**
	 * @return the isGeneric
	 */
	public boolean isGeneric() {
		return isGeneric;
	}

	/**
	 * @param spontaneous the spontaneous to set
	 */
	public void setSpontaneous(boolean spontaneous) {
		this.spontaneous = spontaneous;
	}

	/**
	 * @return the spontaneous
	 */
	public boolean isSpontaneous() {
		return spontaneous;
	}

	/**
	 * @param non_enzymatic the non_enzymatic to set
	 */
	public void setNon_enzymatic(boolean non_enzymatic) {
		this.non_enzymatic = non_enzymatic;
	}

	/**
	 * @return the non_enzymatic
	 */
	public boolean isNon_enzymatic() {
		return non_enzymatic;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	/**
	 * @return the comments
	 */
	public List<String> getComments() {
		return comments;
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
	 * @return the dblinks
	 */
	public List<String> getDblinks() {
		return dblinks;
	}

	/**
	 * @param dblinks the dblinks to set
	 */
	public void setDblinks(List<String> dblinks) {
		this.dblinks = dblinks;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return String
				.format(
						"Reaction_entry [comments=%s, entry=%s, enzymes=%s, equation=%s, isGeneric=%s, name=%s, non_enzymatic=%s, pathways=%s, productsStoichiometry=%s, reactantsStoichiometry=%s, spontaneous=%s]",
						comments != null ? toString(comments, maxLen) : null,
						entry, enzymes != null ? toString(enzymes, maxLen)
								: null, equation, isGeneric, name,
						non_enzymatic, pathways != null ? toString(pathways
								.entrySet(), maxLen) : null,
						productsStoichiometry != null ? toString(
								productsStoichiometry.entrySet(), maxLen)
								: null,
						reactantsStoichiometry != null ? toString(
								reactantsStoichiometry.entrySet(), maxLen)
								: null, spontaneous);
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
