package pt.uminho.sysbio.common.transporters.core.compartments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiEFetchSequenceStub_API;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import jp.cbrc.togo.WoLFPsort;

public class WoLFPSORT implements PSortInterface{

	private String genomeCode;
	private String tempPath;
	private LoadCompartments loadCompartments;
	private int normalization=27;
	private boolean isNCBIGenome;
	private AtomicBoolean cancel;
	private boolean isUseProxy, isUseAuthentication;
	private String host, port, user, pass;
	private int project_id;

	/**
	 * @param conn
	 * @param genomeCode
	 * @param isNCBIGenome
	 * @param project_id
	 */
	public WoLFPSORT(Connection conn, String genomeCode, boolean isNCBIGenome, int project_id) {

		this.isNCBIGenome=isNCBIGenome;
		this.loadCompartments = new LoadCompartments(conn);
		this.genomeCode = genomeCode;
		this.tempPath = FileUtils.getCurrentTempDirectory();
		this.cancel = new AtomicBoolean(false);
		this.project_id = project_id;
	}


	/**
	 * @param conn
	 * @param genomeID
	 * @param tempPath
	 * @param isNCBIGenome
	 */
	public WoLFPSORT(Connection conn, String genomeCode, String tempPath, boolean isNCBIGenome, int project_id) {

		this.isNCBIGenome=isNCBIGenome;
		this.loadCompartments = new LoadCompartments(conn);
		this.genomeCode = genomeCode;
		this.tempPath = FileUtils.getCurrentTempDirectory();
		this.cancel = new AtomicBoolean(false);
		this.project_id = project_id;
	}

	//	/**
	//	 * @param msqlmt
	//	 * @throws SQLException
	//	 */
	//	public WoLFPSORT(Connection conn) throws SQLException  {
	//
	//		this.loadCompartments = new LoadCompartments(conn);
	//		this.cancel = new AtomicBoolean(false);
	//	}

	/* (non-Javadoc)
	 * @see compartments.PSortInterface#getCompartments(java.lang.String)
	 */
	public boolean getCompartments(String type) {

		try {

			String[] args = new String[3];
			args[0]=this.tempPath+this.genomeCode+".faa";
			args[1]=this.tempPath+this.genomeCode+".out";
			args[2]=type;
			//System.out.println(args[0]);
			//System.out.println(args[1]);
			//System.out.println(args[2]);
			WoLFPsort.main(args);
			return true;
		}
		catch(Exception e){e.printStackTrace();return false;}
	}

	/**
	 * @param type
	 * @param genome_file_path
	 * @return
	 */
	public boolean getCompartments(String type, String genome_file_path) {

		try {

			String[] args = new String[3];
			args[0]=genome_file_path;
			args[1]=this.tempPath+this.genomeCode+".out";
			args[2]=type;
			WoLFPsort.main(args);
			return true;
		}
		catch(Exception e){e.printStackTrace();return false;}
	}

	/**
	 * @throws Exception 
	 * @throws ParseException 
	 * @throws IOException 
	 * 
	 */
	public void loadCompartmentsInformation() throws Exception {

		List<WoLFPSORT_Result> results= this.addGeneInformation();

		for(WoLFPSORT_Result woLFPSORT_Result : results) {

			this.loadCompartments.loadData(woLFPSORT_Result.getGeneID(), woLFPSORT_Result.getCompartments(), this.project_id);
		}
	}

	/* (non-Javadoc)
	 * @see compartments.PSortInterface#loadCompartmentsInformation()
	 */
	public void loadCompartmentsInformation(boolean silico) throws Exception {

		List<WoLFPSORT_Result> results= this.addGeneInformation();

		for(WoLFPSORT_Result woLFPSORT_Result : results) {

			this.loadCompartments.loadData(woLFPSORT_Result.getGeneID(), woLFPSORT_Result.getCompartments(), this.project_id);
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public List<WoLFPSORT_Result> addGeneInformation() throws Exception {

		List<WoLFPSORT_Result> compartmentLists = new ArrayList<WoLFPSORT_Result>();
		Map<String,Integer> locus_tags = new HashMap<String, Integer>();

		try {

			BufferedReader in = new BufferedReader(new FileReader(this.tempPath+this.genomeCode+".out"));
			String str;
			int index=0;

			while ((str = in.readLine()) != null && !this.cancel.get()) {

				if(str.startsWith("#")) {

					this.normalization = Integer.parseInt(str.split(":")[1].trim());
				}
				else {

					StringTokenizer st = new StringTokenizer(str,",");
					int i = 0;
					String[] results = new String[st.countTokens()];

					while(st.hasMoreTokens()) {

						results[i] = st.nextToken();
						i++;
					}

					StringTokenizer id_result_tokenizer = new StringTokenizer(results[0]," ");
					String id = id_result_tokenizer.nextToken();
					String result = id_result_tokenizer.nextToken();
					double score = Double.valueOf(id_result_tokenizer.nextToken());
					WoLFPSORT_Result woLFPSORT_Result = new WoLFPSORT_Result(id);

					//					if(this.isNCBIGenome) {
					//						
					//						locus_tags.put(id.split("\\|")[3], index);
					//					}
					//					else {
					//						
					//						locus_tags.put(id.split("\\|")[0], index);
					//					}

					locus_tags.put(id, index);

					woLFPSORT_Result.addCompartment(result, score);

					for(int j = 1; j < results.length; j++) {

						id_result_tokenizer = new StringTokenizer(results[j]," ");
						id = id_result_tokenizer.nextToken();
						result = id_result_tokenizer.nextToken();
						woLFPSORT_Result.addCompartment(id,  Double.valueOf(result));
					}
					compartmentLists.add(index,woLFPSORT_Result);
					index++;
				}
			}
			in.close();

			if(!this.cancel.get()) {

				if(this.isNCBIGenome) {

					NcbiEFetchSequenceStub_API fetchStub = new NcbiEFetchSequenceStub_API(2);

					fetchStub = new NcbiEFetchSequenceStub_API(2);

					Map<String, String> idLocus = fetchStub.getLocusFromID(locus_tags.keySet(),1000);
					Map<String, Integer> temp_locus_tags = new HashMap<String, Integer>();

					for (String id : idLocus.keySet()) {

						for(String acc:locus_tags.keySet()) {

							if(acc.contains(id)) {

								temp_locus_tags.put(idLocus.get(id), locus_tags.get(acc));
							}
						}
					}
					locus_tags = temp_locus_tags;
				}

				List<WoLFPSORT_Result> compartmentResults = new ArrayList<WoLFPSORT_Result>();
				for(String locus_tag:locus_tags.keySet()) {

					WoLFPSORT_Result woLFPSORT_result = compartmentLists.get(locus_tags.get(locus_tag));
					woLFPSORT_result.setGeneID(locus_tag);
					compartmentResults.add(woLFPSORT_result);
				}

				return compartmentResults;
			}
		}
		catch (IOException e) {

			System.out.println("WoLFPSORT output file not Found!\nPlease Run WoLFPSORT and try again!");
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		catch (Exception e) {

			System.err.println("NCBI error!");
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public List<WoLFPSORT_Result> addGeneInformation(boolean silico) throws Exception {

		List<WoLFPSORT_Result> compartmentLists = new ArrayList<WoLFPSORT_Result>();
		Map<String,Integer> locus_tags = new HashMap<String, Integer>();

		try {

			BufferedReader in = new BufferedReader(new FileReader(this.tempPath+this.genomeCode+".out"));
			String str;
			int index=0;

			while ((str = in.readLine()) != null && !this.cancel.get()) {

				if(str.startsWith("#")) {

					this.normalization = Integer.parseInt(str.split(":")[1].trim());
				}
				else {

					StringTokenizer st = new StringTokenizer(str,",");
					int i = 0;
					String[] results = new String[st.countTokens()];

					while(st.hasMoreTokens()) {

						results[i] = st.nextToken();
						i++;
					}

					StringTokenizer id_result_tokenizer = new StringTokenizer(results[0]," ");
					String id = id_result_tokenizer.nextToken();
					String result = id_result_tokenizer.nextToken();
					double score = Double.valueOf(id_result_tokenizer.nextToken());
					WoLFPSORT_Result woLFPSORT_Result = new WoLFPSORT_Result(id);

					locus_tags.put(id, index);

					woLFPSORT_Result.addCompartment(result, score);

					for(int j = 1; j < results.length; j++) {

						id_result_tokenizer = new StringTokenizer(results[j]," ");
						id = id_result_tokenizer.nextToken();
						result = id_result_tokenizer.nextToken();
						woLFPSORT_Result.addCompartment(id,  Double.valueOf(result));
					}
					compartmentLists.add(index,woLFPSORT_Result);
					index++;
				}
			}
			in.close();

			if(!this.cancel.get()) {

				if(this.isNCBIGenome) {

					NcbiEFetchSequenceStub_API fetchStub = new NcbiEFetchSequenceStub_API(2);

					fetchStub = new NcbiEFetchSequenceStub_API(2);

					Map<String, String> idLocus = fetchStub.getLocusFromID(locus_tags.keySet(),1000);
					Map<String, Integer> temp_locus_tags = new HashMap<String, Integer>();

					for (String id : idLocus.keySet()) {

						temp_locus_tags.put(idLocus.get(id), locus_tags.get(id));
					}
					locus_tags = temp_locus_tags;
				}

				List<WoLFPSORT_Result> compartmentResults = new ArrayList<WoLFPSORT_Result>();
				for(String locus_tag:locus_tags.keySet()) {

					WoLFPSORT_Result woLFPSORT_result = compartmentLists.get(locus_tags.get(locus_tag));
					woLFPSORT_result.setGeneID(locus_tag);
					compartmentResults.add(woLFPSORT_result);
				}

				return compartmentResults;
			}
		}
		catch (IOException e) {

			System.out.println("WoLFPSORT output file not Found!\nPlease Run WoLFPSORT and try again!");
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		catch (Exception e) {

			System.err.println("NCBI error!");
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		return null;
	}

	/**
	 * @return the isUseProxy
	 */
	public boolean isUseProxy() {
		return isUseProxy;
	}

	/**
	 * @param isUseProxy the isUseProxy to set
	 */
	public void setUseProxy(boolean isUseProxy) {
		this.isUseProxy = isUseProxy;
	}

	/**
	 * @return the isUseAuthentication
	 */
	public boolean isUseAuthentication() {
		return isUseAuthentication;
	}

	/**
	 * @param isUseAuthentication the isUseAuthentication to set
	 */
	public void setUseAuthentication(boolean isUseAuthentication) {
		this.isUseAuthentication = isUseAuthentication;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the pass
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @param pass the pass to set
	 */
	public void setPass(String pass) {
		this.pass = pass;
	}

	/* (non-Javadoc)
	 * @see compartments.PSortInterface#getBestCompartmentsForGene(double)
	 */
	public Map<String,GeneCompartments> getBestCompartmentsByGene(double threshold)  {

		return this.loadCompartments.getBestCompartmenForGene(threshold,this.normalization, this.project_id);
	}

	@Override
	public AtomicBoolean isCancel() {
		return this.cancel;
	}

	@Override
	public void setCancel(AtomicBoolean cancel) {
		this.cancel = cancel;
	}


	@Override
	public boolean isEukaryote() {

		return true;
	}

	//		/**
	//		 * @param args
	//		 */
	//		public static void main(String[] args) {
	//			MySQLMultiThread msqlmt = new MySQLMultiThread ("localhost","3306","psort_test","root","password");
	//			
	//			WoLFPSORT obj = new WoLFPSORT(Connection conn, String genomeID, boolean isNCBIGenome);
	//			
	//			//obj.getCompartments("C:/Users/ODias/Desktop/CR382121.faa", "fungi");
	//			obj.getCompartments("ftp://ftp.ncbi.nih.gov/genomes/Fungi/Kluyveromyces_lactis_NRRL_Y-1140_uid12377/NC_006042.faa", "fungi");
	//		}

}
