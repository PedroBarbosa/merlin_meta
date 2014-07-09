/**
 * 
 */
package remote.retriever.entry_types;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author ODias
 *
 */
public class Pathways_hierarchy {

	private Map<String, List<String[]>> pathways_hierarchy;
	private String super_pathway;
	
	/**
	 * 
	 */
	public Pathways_hierarchy(String super_pathway) {
		this.setSuper_pathway(super_pathway);
		this.setPathways_hierarchy(new HashMap<String, List<String[]>>());
	}
	/**
	 * @param pathways_hierarchy the pathways_hierarchy to set
	 */
	public void setPathways_hierarchy(Map<String, List<String[]>> pathways_hierarchy) {
		this.pathways_hierarchy = pathways_hierarchy;
	}
	/**
	 * @return the pathways_hierarchy
	 */
	public Map<String, List<String[]>> getPathways_hierarchy() {
		return pathways_hierarchy;
	}
	/**
	 * @param super_pathway the super_pathway to set
	 */
	public void setSuper_pathway(String super_pathway) {
		this.super_pathway = super_pathway;
	}
	/**
	 * @return the super_pathway
	 */
	public String getSuper_pathway() {
		return super_pathway;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		return "Pathways_hierarchy ["
				+ (pathways_hierarchy != null ? "pathways_hierarchy="
						+ toString(pathways_hierarchy.entrySet(), maxLen)
						+ ", " : "")
				+ (super_pathway != null ? "super_pathway=" + super_pathway
						: "") + "]";
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
