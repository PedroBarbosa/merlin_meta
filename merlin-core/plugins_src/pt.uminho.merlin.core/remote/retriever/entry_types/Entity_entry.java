/**
 * 
 */
package remote.retriever.entry_types;

import java.util.List;

/**
 * @author ODias
 *
 */
public interface Entity_entry {

	/**
	 * @param entry the entry to set
	 */
	public void setEntry(String entry);
	/**
	 * @return the entry
	 */
	public String getEntry();
	/**
	 * @param dblinks the dblinks to set
	 */
	public void setDblinks(List<String> dblinks);
	/**
	 * @return the dblinks
	 */
	public List<String> getDblinks();
	/**
	 * @param names the names to set
	 */
	public void setNames(List<String> names);
	/**
	 * @return the names
	 */
	public List<String> getNames();
	
}
