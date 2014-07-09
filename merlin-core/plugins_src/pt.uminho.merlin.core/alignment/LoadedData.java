/**;
 * 
 */
package alignment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pedro
 *
 */
public class LoadedData  {

	private ConcurrentHashMap<String, Integer> orgMap;
	private ConcurrentHashMap<String, Integer> ecNumbersMap;
	private ConcurrentHashMap<Integer, Set<Integer>> ecNumberToHomologues;
	private ConcurrentHashMap<String, Integer> homologuesMap;
	private ConcurrentHashMap<Integer, Set<Integer>> geneToHomologuesMap;
	private ConcurrentHashMap<String, Integer> genesMap;
	private ConcurrentHashMap<String, Integer> fastaSequencesMap;
	private ConcurrentHashMap<Integer, Set<String>> productRankMap;
	private ConcurrentHashMap<Integer, Set<Integer>> productRankToOrganism;
	private ConcurrentHashMap<Integer, Set<String>> ecNumberRankMap;
	private ConcurrentHashMap<Integer, Set<Integer>> ecNumberRankToOrganism;
	private Statement statement;

	/**
	 * @throws SQLException 
	 * 
	 */
	public LoadedData(Statement statement) throws SQLException {

		this.statement = statement;
		this.populateFastaSequencesMap();
		this.populategenesMap();
		this.populateOrgMap();
		this.populatecNumbersMap();
		this.populatecNumbersToHomologues();
		this.populateHomologuesMap();
		this.populategeneToHomologuesMap();
		this.populatecNUmberRankMap();
		this.populatecNumberRankToOrganism();
		this.populateproductRankMap();
		this.populateproductRankToOrganism();
	}

	/**
	 * @throws SQLException
	 */
	private void populateFastaSequencesMap() throws SQLException {

		this.fastaSequencesMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM fastaSequence;");

			while(rs.next()) {

				this.fastaSequencesMap.put(rs.getString(3),rs.getInt(1));
				//System.out.println(rs.getString(3));
			}
			rs.close();
		
	}

	private void populategenesMap() throws SQLException{

		this.genesMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery(	"SELECT * FROM geneHomology ");
		
			while(rs.next()){ 
				this.genesMap.put(rs.getString(4), rs.getInt(1));
				//System.out.println(rs.getString(4));
			}
			rs.close();
		

	}
	private void populateOrgMap() throws SQLException{
		this.orgMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM organism ");

	
			while (rs.next()){
				this.orgMap.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();

	}

	private void populatecNumbersMap() throws SQLException{
		this.ecNumbersMap = new ConcurrentHashMap<>();
		
		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumber ");

			while (rs.next()){
				this.ecNumbersMap.put(rs.getString(2), rs.getInt(1));
			}
			rs.close();
		
	}


	private void populateHomologuesMap() throws SQLException{
		this.homologuesMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM homologues ");
	
			while (rs.next()){
				this.homologuesMap.put(rs.getString(3), rs.getInt(1));
		
			}
			rs.close();
		

	}

	private void populatecNumbersToHomologues() throws SQLException{
		this.ecNumberToHomologues = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery(" SELECT * FROM homologues_has_ecNumber ");

		
			while (rs.next()){
				if(this.ecNumberToHomologues.containsKey(rs.getInt(1))){
					this.ecNumberToHomologues.get(rs.getInt(1)).add(rs.getInt(2));
				}
				else{
					Set <Integer> setEcsNameKey = new HashSet<>();
					setEcsNameKey.add(rs.getInt(2));
					this.ecNumberToHomologues.put(rs.getInt(1), setEcsNameKey);
				}
			}
			rs.close();
		
	}

	private void populategeneToHomologuesMap() throws SQLException{
		this.geneToHomologuesMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM geneHomology_has_homologues ");

	
			while (rs.next()){
				if(this.geneToHomologuesMap.containsKey(rs.getInt(1))){
					this.geneToHomologuesMap.get(rs.getInt(1)).add(rs.getInt(2));
				}
				else{
					Set <Integer> setHomologues = new HashSet<>();
					setHomologues.add(rs.getInt(2));
					this.geneToHomologuesMap.put(rs.getInt(1), setHomologues);
				}	
			}
			rs.close();
		
	}



	private void populatecNUmberRankMap() throws SQLException{
		this.ecNumberRankMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumberRank ");

		
			while (rs.next()){
				if(this.ecNumberRankMap.containsKey(rs.getInt(2))){
					this.ecNumberRankMap.get(rs.getInt(2)).add(rs.getString(3));
				}
				else{
					Set <String> setEcs = new HashSet<>();
					setEcs.add(rs.getString(3));
					this.ecNumberRankMap.put(rs.getInt(2), setEcs);
				}	
			}
			rs.close();
		

	}

	private void populatecNumberRankToOrganism() throws SQLException{
		this.ecNumberRankToOrganism = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM ecNumberRank_has_organism ");

		
			while (rs.next()){
				if (this.ecNumberRankToOrganism.containsKey(rs.getInt(1))){
					this.ecNumberRankToOrganism.get(rs.getInt(1)).add(rs.getInt(2));
				}

				else{

					Set <Integer> setOrgsEc = new HashSet<>();
					setOrgsEc.add(rs.getInt(2));
					this.ecNumberRankToOrganism.put(rs.getInt(1), setOrgsEc);
				}		
			}
			rs.close();	
		
	}

	private void populateproductRankMap() throws SQLException{
		this.productRankMap = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM productRank ");

	
			while (rs.next()){
				if(this.productRankMap.containsKey(rs.getInt(2))){
					this.productRankMap.get(rs.getInt(2)).add(rs.getString(3));
				}
				else{
					Set<String> setProd = new HashSet<>();
					setProd.add(rs.getString(3));
					this.productRankMap.put(rs.getInt(2), setProd);
				}	
			}
			rs.close();
		
	}

	private void populateproductRankToOrganism() throws SQLException{
		this.productRankToOrganism = new ConcurrentHashMap<>();

		ResultSet rs = this.statement.executeQuery("SELECT * FROM productRank_has_organism ");

		
			while (rs.next()){
				if (this.productRankToOrganism.containsKey(rs.getInt(1))){
					this.productRankToOrganism.get(rs.getInt(1)).add(rs.getInt(2));
				}

				else{

					Set <Integer> setOrgsProd = new HashSet<>();
					setOrgsProd.add(rs.getInt(2));
					this.productRankToOrganism.put(rs.getInt(1), setOrgsProd);
				}		
			}
			rs.close();	
		
	}


	/**
	 * @return the orgMap
	 */
	public ConcurrentHashMap<String, Integer> getOrgMap() {
		return orgMap;
	}


	/**
	 * @param orgMap the orgMap to set
	 */
	public void setOrgMap(ConcurrentHashMap<String, Integer> orgMap) {
		this.orgMap = orgMap;
	}


	/**
	 * @return the ecNumbersMap
	 */
	public ConcurrentHashMap<String, Integer> getEcNumbersMap() {
		return ecNumbersMap;
	}


	/**
	 * @param ecNumbersMap the ecNumbersMap to set
	 */
	public void setEcNumbersMap(ConcurrentHashMap<String, Integer> ecNumbersMap) {
		this.ecNumbersMap = ecNumbersMap;
	}


	/**
	 * @return the homologuesMap
	 */
	public ConcurrentHashMap<String, Integer> getHomologuesMap() {
		return homologuesMap;
	}


	/**
	 * @param homologuesMap the homologuesMap to set
	 */
	public void setHomologuesMap(ConcurrentHashMap<String, Integer> homologuesMap) {
		this.homologuesMap = homologuesMap;
	}


	/**
	 * @return the geneToHomologuesMap
	 */
	public ConcurrentHashMap<Integer, Set<Integer>> getGeneToHomologuesMap() {
		return geneToHomologuesMap;
	}


	/**
	 * @param geneToHomologuesMap the geneToHomologuesMap to set
	 */
	public void setGeneToHomologuesMap(
			ConcurrentHashMap<Integer, Set<Integer>> geneToHomologuesMap) {
		this.geneToHomologuesMap = geneToHomologuesMap;
	}


	/**
	 * @return the genesMap
	 */
	public ConcurrentHashMap<String, Integer> getGenesMap() {
		return genesMap;
	}


	/**
	 * @param genesMap the genesMap to set
	 */
	public void setGenesMap(ConcurrentHashMap<String, Integer> genesMap) {
		this.genesMap = genesMap;
	}


	/**
	 * @return the fastaSequencesMap
	 */
	public ConcurrentHashMap<String, Integer> getFastaSequencesMap() {
		return fastaSequencesMap;
	}


	/**
	 * @param fastaSequencesMap the fastaSequencesMap to set
	 */
	public void setFastaSequencesMap(ConcurrentHashMap<String, Integer> fastaSequencesMap) {
		this.fastaSequencesMap = fastaSequencesMap;
	}


	/**
	 * @return the productRankMap
	 */
	public ConcurrentHashMap<Integer, Set<String>> getProductRankMap() {
		return productRankMap;
	}


	/**
	 * @param productRankMap the productRankMap to set
	 */
	public void setProductRankMap(ConcurrentHashMap<Integer, Set<String>> productRankMap) {
		this.productRankMap = productRankMap;
	}


	/**
	 * @return the productRankToOrganism
	 */
	public ConcurrentHashMap<Integer, Set<Integer>> getProductRankToOrganism() {
		return productRankToOrganism;
	}


	/**
	 * @param productRankToOrganism the productRankToOrganism to set
	 */
	public void setProductRankToOrganism(
			ConcurrentHashMap<Integer, Set<Integer>> productRankToOrganism) {
		this.productRankToOrganism = productRankToOrganism;
	}


	/**
	 * @return the ecNumberRankMap
	 */
	public ConcurrentHashMap<Integer, Set<String>> getEcNumberRankMap() {
		return ecNumberRankMap;
	}


	/**
	 * @param ecNumberRankMap the ecNumberRankMap to set
	 */
	public void setEcNumberRankMap(ConcurrentHashMap<Integer, Set<String>> ecNumberRankMap) {
		this.ecNumberRankMap = ecNumberRankMap;
	}


	/**
	 * @return the ecNumberRankToOrganism
	 */
	public ConcurrentHashMap<Integer, Set<Integer>> getEcNumberRankToOrganism() {
		return ecNumberRankToOrganism;
	}


	/**
	 * @param ecNumberRankToOrganism the ecNumberRankToOrganism to set
	 */
	public void setEcNumberRankToOrganism(
			ConcurrentHashMap<Integer, Set<Integer>> ecNumberRankToOrganism) {
		this.ecNumberRankToOrganism = ecNumberRankToOrganism;
	}


	/**
	 * @return the statement
	 */
	public Statement getStatement() {
		return statement;
	}


	/**
	 * @param statement the statement to set
	 */
	public void setStatement(Statement statement) {
		this.statement = statement;
	}




	/**
	 * @return the ecNumberToHomologues
	 */
	public ConcurrentHashMap<Integer, Set<Integer>> getEcNumberToHomologues() {
		return ecNumberToHomologues;
	}


	/**
	 * @param ecNumberToHomologues the ecNumberToHomologues to set
	 */
	public void setEcNumberToHomologues(ConcurrentHashMap<Integer, Set<Integer>> ecNumberToHomologues) {
		this.ecNumberToHomologues = ecNumberToHomologues;
	}


}