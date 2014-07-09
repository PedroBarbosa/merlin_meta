/**
 * 
 */
package alignment.hmmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import operations.HmmerSimilaritySearch.HmmerDatabase;

import org.json.JSONException;

/**
 * @author ODias
 *
 */
public class ReadHmmertoList {
	private HmmerSet hmmJobResults;
	private HmmerScan hmmerStub;
	private String program;
	private int maxNumberofAlignments;
	private List<String> results; 
	private Map<String, Double> eValues, scores;
	private double maxEValue;
	private AtomicBoolean cancel;
	private HmmerDatabase database;

	/**
	 * 
	 */
	public ReadHmmertoList() {
	}

	/**
	 * @param seq
	 * @param header
	 * @param database
	 * @param maxNumberofAlignments
	 * @param maxEvalue
	 */
	public ReadHmmertoList(String seq, String header, HmmerDatabase database, int maxNumberofAlignments, double maxEvalue, AtomicBoolean cancel) {

		this.hmmerStub = new HmmerScan(seq, header, database);
		this.setDatabase(database);
		this.program = "hmmer";
		this.maxEValue = maxEvalue;
		this.maxNumberofAlignments = maxNumberofAlignments;
		this.scores = new HashMap<String, Double>();
		this.eValues = new HashMap<String, Double>();
		this.cancel = cancel;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public boolean sCan(String resLink) throws Exception {

		try {

			this.hmmJobResults = new HmmerSet(HmmerFetch.fetch(resLink, this.maxNumberofAlignments), resLink, this.hmmerStub.getHeader());

			this.results = new ArrayList<String>();

			for (int i = 0; i < this.hmmJobResults.getResults().size(); i++) {

				HmmerResult hmmRes = this.hmmJobResults.getResults().get(i);

				if(this.maxEValue<0 || hmmRes.getEval()<=this.maxEValue) {

					String id = hmmRes.getAcc();
					id = id.split(",")[0].trim();
					//this.results.add(id);
					this.scores.put(id, hmmRes.getScore());
					this.eValues.put(id, hmmRes.getEval());

					if(this.getDatabase().equals(HmmerDatabase.uniprotkb)
							|| this.getDatabase().equals(HmmerDatabase.unimes)
							|| this.getDatabase().equals(HmmerDatabase.uniprotrefprot)
							)
						this.results.add(hmmRes.getAcc());
					else
						this.results.add(hmmRes.getGI());
				}
				else {

					i = hmmJobResults.getResults().size();
				}
			}
			//System.out.println(this.results);
			return !this.cancel.get();
		}
		catch(JSONException e) {

			System.out.println("Wating for results on query "+resLink);
		}
		return false;
	}
	
	
	
	/**
	 * @param date
	 * @param cancel
	 * @return
	 * @throws IOException
	 */
	public String getJobID(AtomicLong date, AtomicBoolean cancel) throws IOException {
		
		return hmmerStub.HmmerSubmitJob(date, this.cancel);
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void deleteJob(String jobID) throws Exception{
		
		HmmerFetch.delete(jobID);
	}

	/**
	 * @return
	 */
	public String getQuery() {
		return this.hmmJobResults.getAccession();
	}

	/**
	 * @return
	 */
	public HmmerDatabase getDatabaseId() {
		return this.hmmerStub.getDB();
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return "";
	}

	/**
	 * @return
	 */
	public String getProgram() {
		return this.program;
	}

	/**
	 * @return
	 */
	public List<String> getResults() {
		return this.results;	
	}

	/**
	 * @return
	 */
	public Map<String, Double> getScores() {
		return this.scores;
	}

	/**
	 * @return
	 */
	public Map<String, Double> getEValues() {
		return this.eValues; 
	}

	/**
	 * @return the database
	 */
	public HmmerDatabase getDatabase() {
		return database;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(HmmerDatabase database) {
		this.database = database;
	}

	//	public static void main(String[] args) throws Exception {
	//		String header = ">tr|O25559|O25559_HELPY Hydrogenase expression/formation protein (HypC) OS=Helicobacter pylori (strain ATCC 700392 / 26695) GN=HP_0899 PE=4 SV=1"; 
	//		String seq =
	////				" msetakkviv gmsggvdssv sawllqqqgy qveglfmknw eeddgeeyct aaadladaqa"
	////				+"vcdklgielh tvnfaaeywd nvfelflaey kagrtpnpdi lcnkeikfka flefaaedlg"
	////				+"adyiatghyv rradvdgksr llrgldsnkd qsyflytlsh eqiaqslfpv gelekpqvrk"
	////				+"iaedlglvta kkkdstgicf igerkfrefl grylpaqpgk iitvdgdeig ehqglmyhtl"
	////				+"gqrkglgigg tkegteepwy vvdkdvenni lvvaqghehp rlmsvgliaq qlhwvdrepf"
	////				+"tgtmrctvkt ryrqtdipct vkaldddrie vifdepvaav tpgqsavfyn gevclgggii"
	////				+"eqrlplpv";
	//				
	//				"MCLAIPSKVIAIKDNVVLLETLGVQREASLDLMGESVKVGDYVLLHIGYVMSKIDEKEAL"
	//				+"ESIELYQEMIARMNETQ";
	//		AtomicBoolean b = new AtomicBoolean(false);
	//		ReadHmmertoList hmmToList = new ReadHmmertoList(seq, header, "nr", 100, 10E-3,b);
	//
	//		hmmToList.sCan();
	//
	//		for (String k : hmmToList.getEValues().keySet()) {
	//			System.out.println(k + "\t" + hmmToList.getEValues().get(k));
	//		}
	//
	//		for (String k : hmmToList.getScores().keySet()) {
	//			System.out.println(k + "\t" + hmmToList.getScores().get(k));
	//		}
	//
	//
	//		System.out.println("TOTAL RESULTS: " + hmmToList.getResults().size());
	//	}
}
