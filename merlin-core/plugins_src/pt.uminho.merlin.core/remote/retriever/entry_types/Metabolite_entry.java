/**
 * 
 */
package remote.retriever.entry_types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ODias
 *
 */
public class Metabolite_entry implements Entity_entry  {

	private String entry;
	private String formula;
	private String molecular_weight;
	private String name;
	private List<String> names;
	private List<String> enzymes;
	private List<String> reactions;
	private Map<String, String> pathways;
	private List<String> dblinks;
	private List<String> same_as;
	
	
	/**
	 * @param entry
	 */
	public Metabolite_entry(String entry) {
		this.setEntry(entry);
		this.setSame_as(new ArrayList<String>());
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
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}


	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}


	/**
	 * @param molecular_weight the molecular_weight to set
	 */
	public void setMolecular_weight(String molecular_weight) {
		this.molecular_weight = molecular_weight;
	}


	/**
	 * @return the molecular_weight
	 */
	public String getMolecular_weight() {
		return molecular_weight;
	}


	/**
	 * @param names the name to set
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
	 * @param reactions the reaction to set
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
	 * @param same_as the same_as to set
	 */
	public void setSame_as(List<String> same_as) {
		this.same_as = same_as;
	}
	
	/**
	 * @param same_as
	 */
	public void setSame_as(String same_as) {
		this.same_as.add(same_as);		
	}

	/**
	 * @return the same_as
	 */
	public List<String> getSame_as() {
		return same_as;
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


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return String
				.format(
						"Metabolite_entry [dblinks=%s, entry=%s, enzymes=%s, formula=%s, molecular_weight=%s, name=%s, names=%s, pathways=%s, reactions=%s, same_as=%s]",
						dblinks != null ? toString(dblinks, maxLen) : null,
						entry,
						enzymes != null ? toString(enzymes, maxLen) : null,
						formula,
						molecular_weight,
						name,
						names != null ? toString(names, maxLen) : null,
						pathways != null ? toString(pathways.entrySet(), maxLen)
								: null, reactions != null ? toString(reactions,
								maxLen) : null, same_as != null ? toString(
								same_as, maxLen) : null);
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
