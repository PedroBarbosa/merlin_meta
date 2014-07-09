/**
 * 
 */
package alignment.localblast;

/**
 * @author pedro
 *
 */
public class BlastArguments {


	String evalue,word_size,matrix,args;
	int num_descriptions; 
	int num_alignments = 0;
	int num_threads = Runtime.getRuntime().availableProcessors();


	String gap_costs = "defaults";
	boolean default_values = false;

	/**
	 * 
	 */

	public BlastArguments(String evalue, int num_descript, String matrix, String word_size) {
		this.evalue= evalue;
		this.num_descriptions = num_descript;
		this.matrix = matrix;
		this.word_size = word_size;
		
		if(evalue.equals(10) && num_descript == 500 && matrix.equals("BLOSUM62") && word_size.equals(3) && num_threads == 1){
			default_values = true;
		}
	}

	public String getArguments (){

		if(default_values){
			args ="";
		}
		else{
			args = " -evalue " + this.evalue + " -num_descriptions " + this.num_descriptions+ " -num_alignments " + this.num_alignments +" -matrix " + this.matrix + " -word_size " + this.word_size + " -num_threads " + this.num_threads;				
		}
		return args;
	}

	/**
	 * @return the num_descriptions
	 */
	public int getNum_descriptions() {
		return num_descriptions;
	}

	/**
	 * @param num_descriptions the num_descriptions to set
	 */
	public void setNum_descriptions(int num_descriptions) {
		this.num_descriptions = num_descriptions;
	}

	/**
	 * @return the num_alignments
	 */
	public int getNum_alignments() {
		return num_alignments;
	}

	/**
	 * @param num_alignments the num_alignments to set
	 */
	public void setNum_alignments(int num_alignments) {
		this.num_alignments = num_alignments;
	}

	/**
	 * @return the evalue
	 */
	public String getEvalue() {
		return evalue;
	}

	/**
	 * @param evalue the evalue to set
	 */
	public void setEvalue(String evalue) {
		this.evalue = evalue;
	}



	/**
	 * @return the word_size
	 */
	public String getWord_size() {
		return word_size;
	}

	/**
	 * @param word_size the word_size to set
	 */
	public void setWord_size(String word_size) {
		this.word_size = word_size;
	}

	/**
	 * @return the num_threads
	 */
	public int getNum_threads() {
		return num_threads;
	}

	/**
	 * @param num_threads the num_threads to set
	 */
	public void setNum_threads(int num_threads) {
		this.num_threads = num_threads;
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
	 * @return the args
	 */
	public String getArgs() {
		return args;
	}

	/**
	 * @param args the args to set
	 */
	public void setArgs(String args) {
		this.args = args;
	}
	/**
	 * @return the gap_costs
	 */
	public String getGap_costs() {
		return gap_costs;
	}

	/**
	 * @param gap_costs the gap_costs to set
	 */
	public void setGap_costs(String gap_costs) {
		this.gap_costs = gap_costs;
	}
	/**
	 * @return the default_values
	 */
	public boolean isDefault_values() {
		return default_values;
	}

	/**
	 * @param default_values the default_values to set
	 */
	public void setDefault_values(boolean default_values) {
		this.default_values = default_values;
	}
	
	
}