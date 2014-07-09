/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment.hmmer;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 *
 * @author fxe
 */


/*
 * "archScore":"2"
 * "arch":" PF01118.19 PF02774.13"
 * "kg":"Bacteria"
 * "ndom":1
 * "extlink":"http://www.ncbi.nlm.nih.gov/protein/6225260?report=genbank"
 * "taxid":"210"
 * "acc2":"O25801.1, DHAS_HELPY"
 * "acc":"6225260"
 * "taxlink":"http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id="
 * "desc":"RecName: Full=Aspartate-semialdehyde dehydrogenase; Short=ASA dehydrogenase; Short=ASADH; AltName: Full=Aspartate-beta-semialdehyde dehydrogenase"
 * "dcl":143760
 * "pvalue":-538.55133590766
 * "flags":3
 * "nregions":1
 * "niseqs":1
 * "name":"6225260"
 * "species":"Helicobacter pylori"
 * "score":"762.8"
 * "bias":"3.7"
 * "sindex":"1959138"
 * "domains":[]
 * "nincluded":1
 * "evalue":"1.6e-227"
 * "nreported":1
 * "archindex":18452477937173
 */
public class HmmerSet {
	private ArrayList<HmmerResult> results = new ArrayList<HmmerResult>();
	private String urlLink;
	private String accession;
	//private int jobID;

	/**
	 * @param jobID
	 * @param set
	 */
	public HmmerSet(int jobID, ArrayList<HmmerResult> set) {
		this.results.addAll(set);
		//this.jobID = jobID;
	}

	/**
	 * @param HmmerJSONOutput
	 * @param urlLink
	 * @param accession
	 * @throws Exception
	 */
	public HmmerSet(String HmmerJSONOutput, String urlLink, String accession) throws Exception {
		//System.out.print(HmmerJSONOutput);
		fillResultsFromJSON(HmmerJSONOutput);
		this.urlLink = urlLink;
		this.accession = accession;
	}

	/**
	 * @param HmmerJSONOutput
	 * @param urlLink
	 * @throws Exception
	 */
	public HmmerSet(String HmmerJSONOutput, String urlLink) throws Exception {
		//System.out.print(HmmerJSONOutput);
		fillResultsFromJSON(HmmerJSONOutput);
		this.urlLink = urlLink;
		this.accession = "";
	}

	/**
	 * @param HmmerJSONOutput
	 * @throws Exception
	 */
	public HmmerSet(String HmmerJSONOutput) throws Exception {
		try{
			//System.out.print(HmmerJSONOutput);
			fillResultsFromJSON(HmmerJSONOutput);
			this.urlLink = "NoLink";
			this.accession = "";
		}
		catch (OutOfMemoryError e) {
			throw new Exception("Out of memory error");
		}
	}

	/**
	 * @return
	 */
	public String getAccession() {
		return this.accession;
	}

	/**
	 * @return
	 */
	public String getUrlLink() {
		return this.urlLink;
	}

	/**
	 * @return
	 */
	public ArrayList<HmmerResult> getResults() {
		return this.results;
	}

	/**
	 * @param HmmerJSONOutput
	 * @throws Exception
	 */
	private void fillResultsFromJSON(String HmmerJSONOutput) throws Exception {

		JSONObject jso = new JSONObject(HmmerJSONOutput);
		JSONObject hits = (JSONObject) jso.get("results");


		JSONArray jsA = hits.getJSONArray("hits");
		for(int i = 0; i < jsA.length(); i++) {
			JSONObject val = (JSONObject) jsA.get(i);
			
			String species = this.parseResult(val, "species");
			String desc = this.parseResult(val, "desc");
			String gi = this.parseResult(val, "acc");
			String acc = this.parseResult(val, "acc2");
			String taxid = this.parseResult(val, "taxid");
			String kg = this.parseResult(val, "kg");
			double eval = Double.parseDouble(this.parseResult(val, "evalue"));
			double score = Double.parseDouble(this.parseResult(val, "score"));
			double pval = val.getDouble("pvalue");

			//System.out.println(species);
			//System.out.println(desc);
			results.add(new HmmerResult(species, desc, gi, acc, taxid, kg, eval, pval, score));
		}
	}
	
	/**
	 * @param val
	 * @param key
	 * @return
	 * @throws JSONException
	 */
	private String parseResult(JSONObject val, String key) throws JSONException {
		String result = "";
		try {
			result = val.getString(key);
		} catch (JSONException e) {
			if(e.getMessage().endsWith("not a string.")) {
				return result;
			}
			throw e;
		}
		return result;
	}


	public void printResults() {
		for (HmmerResult hres: results) {
			System.out.println(hres.toString());
		}
	}
}
