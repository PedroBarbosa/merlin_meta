/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment.hmmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import operations.HmmerSimilaritySearch.HmmerDatabase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import pt.uminho.sysbio.merlin.utilities.MySleep;

/**
 *
 * @author fxe
 */

public class HmmerScan {

	//private static Logger LOGGER = Logger.getLogger(HmmerScan.class);

	private String seq;
	private String header = "seq";
	private static final String HMMER_JANELIA_URL = "http://hmmer.janelia.org/search/phmmer";
	private HmmerDatabase db = HmmerDatabase.uniprotkb; //SET AS DEFAULT
	//nr, uniprotkb, swissprot, pdb, env_nr, unimes, rp

	/**
	 * @param seq
	 */
	public HmmerScan(String seq) {
		this.seq = seq;
	}

	/**
	 * @param seq
	 * @param header
	 */
	public HmmerScan(String seq, String header) {

		this.seq = seq;
		this.header = header;
	}

	/**
	 * @param seq
	 * @param header
	 * @param db
	 */
	public HmmerScan(String seq, String header, HmmerDatabase db) {

		this.seq = seq;
		this.header = header;
		this.db = db;
	}

	/**
	 * @param db
	 */
	public void setDB(HmmerDatabase db) {
		this.db = db;
	}

	/**
	 * @return
	 */
	public HmmerDatabase getDB() {
		return db;
	}

	/**
	 * @return
	 * @throws IOException 
	 */
//	public String HmmerSubmitJobOld() throws IOException {
//		URL respUrl;
//		HttpURLConnection httpcon = null;
//		try {
//			URL url = new URL(HMMER_JANELIA_URL);
//			httpcon = (HttpURLConnection) url.openConnection();
//			httpcon.setDoInput(true);
//			httpcon.setDoOutput(true);
//			httpcon.setInstanceFollowRedirects(false);
//			httpcon.setRequestMethod("POST");
//			httpcon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//			httpcon.setRequestProperty("Accept", "application/json");
//			String fasta = ">" + header + "\n" + seq;
//			String urlParameters = "seqdb=" + URLEncoder.encode(db, "UTF-8") + "&seq=" + URLEncoder.encode(fasta, "UTF-8");
//
//			System.out.println(urlParameters);
//
//			httpcon.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
//
//			DataOutputStream wr = new DataOutputStream (httpcon.getOutputStream ());
//			wr.writeBytes (urlParameters);
//			wr.flush ();
//			wr.close ();
//			respUrl = new URL( httpcon.getHeaderField( "Location" ));
//			return respUrl.toString();
//
//		} catch (MalformedURLException e) {
//			System.out.println(httpcon.getURL());
//			throw e;
//		}
//	}

	/**
	 * @param date
	 * @param cancel
	 * @return
	 * @throws IOException
	 */
	public String HmmerSubmitJob(AtomicLong date, AtomicBoolean cancel) throws IOException {

		long delay = -1;
		synchronized (date) {

			while(delay<1500 && !cancel.get()) {
				
				delay = System.currentTimeMillis() - date.get();
				date.set(System.currentTimeMillis());
				
				if (delay < 1500)
					MySleep.myWait(1500);
			}
		}

		String res = null;
		String fasta = ">" + header + "\n" + seq;

		//System.out.println("header "+header+" "+delay);

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(HMMER_JANELIA_URL);
		List<NameValuePair> nameValuePairs = new ArrayList<> ();
		Map<String, String> params = new HashMap<> ();
		params.put("seqdb", db.toString());
		params.put("seq", fasta);
		for (String key : params.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, params.get(key)));
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		//LOGGER.debug(httpPost.getRequestLine());

		HttpResponse httpResponse = httpClient.execute(httpPost);
		HttpEntity httpEntity = httpResponse.getEntity();
		BufferedReader br = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
		StringBuilder responseString = new StringBuilder();
		Pattern pattern = Pattern.compile("http*?.+score");

		String readline;

		while ((readline = br.readLine()) != null) {

			Matcher matcher = pattern.matcher(readline);

			while(matcher.find()) {

				String s = matcher.group();
				res = new String(s);

				s = s.substring(4);
				matcher = pattern.matcher(s);
			}
			responseString.append(readline+"\n");
		}
		br.close();

		//LOGGER.debug(res);
		
		if(res==null) {
			
			System.out.println(responseString.toString());
			System.out.println();
		}
		//else 
			//System.out.println("res "+header+"\t"+res);

		return res;
	}

	/**
	 * @return the seq
	 */
	public String getSeq() {
		return seq;
	}

	/**
	 * @param seq the seq to set
	 */
	public void setSeq(String seq) {
		this.seq = seq;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	
	public static void main(String args[]) throws Exception {
		//		HmmerScan hmmerScan = new HmmerScan("MSVSLVSRNVARKLLLVQLLVVIASGLLFSLKDPFWGVSAISGGLAVFLPNVLFMIFAWRHQAHTPAKGRVAWTFAFGEAFKVLAMLVLLVVALAVLKAVFLPLIVTWVLVLVVQILAPAVINNKG", "gene8", "nr");
		//		String  jobid = hmmerScan.HmmerSubmitJob();

		String jobid = "http://hmmer.janelia.org/results/A9015010-E278-11E3-B032-014FF29B2471/score";
		System.out.println(jobid);

		String res = HmmerFetch.fetch(jobid, 1);
		System.out.println(res);
	}
}
