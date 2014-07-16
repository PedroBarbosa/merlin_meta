/**
 * 
 */
package alignment.localblast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import pt.uminho.sysbio.merlin.utilities.OpenBrowser;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import datatypes.Project;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author pedro
 *
 */
public class LocalBlast {
	
	private static Logger LOGGER = Logger.getLogger(LocalBlast.class);
	
	private AtomicBoolean cancel;
	private Process process;
	private long startTime;
	private Project project;
	private String program, args;
	private String commandline = "";

	private String dBPATH;
	private File queryPath, outputFile,uniprotTXTpath;


	//	private String queryPath  = "/home/pedro/Desktop/testeblastx";
	//	private String outputFile = "/home/pedro/Desktop/blastUniprot";


	public ConcurrentLinkedQueue<String> noSimilaritiesGenes = new ConcurrentLinkedQueue<String>();
	public String [] setupInfo = new String[3];


	public LocalBlast(String program, File query, File txtPATH, String dbPath,  String args, Project project){
		this.program = program;
		this.queryPath = query;
		this.outputFile = new File(FileUtils.getCurrentTempDirectory().concat(project.getGenomeCodeName()).concat("_blastOutput.txt"));
		//this.outputFile = new File(FileUtils.getCurrentTempDirectory().concat("genome_uniprot_all_1_blastOutput"));

		this.dBPATH = dbPath;
		this.uniprotTXTpath = txtPATH;
		this.args = args;
		this.project=project;
		this.commandline += program;
		this.cancel = new AtomicBoolean(false);
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

	}

	public void runBlast() throws IOException {

		try{
			String s = null ;

			this.commandline += " -query " + this.queryPath + " -db " + this.dBPATH  + " -out " + this.outputFile + this.args;
			LOGGER.debug("Blast command: " + this.commandline);
			
			long startTime = System.currentTimeMillis();
			this.process = Runtime.getRuntime().exec(this.commandline);
			  

//			BufferedReader stdInput = new BufferedReader(new 
//					InputStreamReader(this.process.getInputStream()));
//
//			BufferedReader stdError = new BufferedReader(new 
//					InputStreamReader(this.process.getErrorStream()));


			// read the output from the command
			System.out.println("Performing the BLASTP operation..\n");
//			while ((s = stdInput.readLine()) != null) {
//
//				System.out.println(s);
//
//			}

			// read any errors from the attempted command
//			if(stdError != null){
//				System.out.println("Here is the standard error of the command:\n");
//				boolean main_error_displayed = false;
//				while ((s = stdError.readLine()) != null && main_error_displayed == false) {
//					Workbench.getInstance().error("Blast error: "+s);
//					main_error_displayed = true;
//					//System.exit(0);
//				}
//			}

			String dbname = dBPATH.substring(dBPATH.lastIndexOf('/') + 1);
			System.out.println("Blast against " + dbname + " database is finished.");

			long endTime = System.currentTimeMillis();
			System.out.println("Total elapsed time in execution of blast was: \t:"+ String.format("%d min, %d sec.", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
					-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
			//System.exit(0);

		}
		catch (IOException e) {
			Workbench.getInstance().error("Problems ocurred during local blast !!");
			e.printStackTrace();
			//System.exit(-1);
		}

	}

	public LinkedHashMap<String, LinkedHashMap<String, String[]>> parseBlastOutput() throws Exception{

		String delimiter ="";
		String query = "";
		LinkedHashMap< String, LinkedHashMap< String, String []>> blastParse = new LinkedHashMap<>();
		LinkedHashMap <String, String[]>homologues = new LinkedHashMap<>();

		ParserBlast parser = new ParserBlast(this.outputFile, delimiter);
		boolean parseStarted = false;
		boolean firstline = true;

		try{
			if(parser.openFile()){			

				String linhaQuery = "Query=";
				while (parser.nextLine()){
					String linha = parser.getLine();


					if(firstline){

						String [] aux = linha.split(" ");
						String version = aux[1];
						firstline = false;
						this.setupInfo[0] = "ncbi-"+this.program;
						this.setupInfo[1] = version;
					}


					String homologueID = "";
					String score, evalue ;

					if(linha.startsWith("Database")){
						String databaseID  = linha.substring(9).replaceAll(" ", "");
						parser.nextLine();
						databaseID += parser.getLine();
						this.setupInfo[2] = databaseID;
					}

					if(linha.startsWith(linhaQuery)){

						parseStarted = true;
						java.util.regex.Matcher matcher = Pattern.compile("(?<==).*").matcher(linha);
						matcher.find();
						query = matcher.group().replaceAll("\\s", "");	
						homologues = new LinkedHashMap<String, String[]>();

					}


					if (linha.contains("No hits found")) {
						this.noSimilaritiesGenes.add(query);
					}

					java.util.regex.Matcher matcher = Pattern.compile("^\\s+\\w+\\|(\\w+)\\|.+\\s(\\S+)\\s+(\\S+).*").matcher(linha);
					matcher.find();


					if (matcher.matches()) {


						homologueID = matcher.group(1);
						score = matcher.group(2);
						evalue = matcher.group(3);
						String[] par = new String[9];
						par[0] = score;
						par[1] = evalue;
						homologues.put(homologueID, par);
					}


					if(linha.isEmpty() && parseStarted && !homologues.isEmpty()){
						
						blastParse.put(query, homologues);
					}			
				}
			}
		}
		
		catch (IOException e) {
				Workbench.getInstance().error("Problems ocurred during local blast !!");
				e.printStackTrace();
				//System.exit(-1);
			}
		


		if(blastParse.isEmpty()){
			Workbench.getInstance().warn("No hits found for any gene. System will exit now");
			throw new IllegalArgumentException();
			//System.out.println("No hits found for any gene. System will exit now");
			//System.exit(0);
		}
		//		System.out.println(noSimilaritiesGenes.size());
		//		System.out.println(blastParse.size());
		//		System.out.println(count);
		//		System.out.println(noSimilaritiesGenes.size() + blastParse.size());

		LinkedHashMap<String, LinkedHashMap<String, String[]>> blastParseFinal= orderHashMap(blastParse);
		return blastParseFinal;		
	}

	public LinkedHashMap<String, LinkedHashMap<String, String[]>> retrieveUniprotData(LinkedHashMap<String, LinkedHashMap<String, String[]>> sortedHash, File uniprotTxt) throws Exception{



		//HashMap<String, String[]> uniprotRetrieveInformation = new HashMap<String, String[]>();
		//int count = 0;
		FileReader f= new FileReader(uniprotTxt);
		BufferedReader reader = new BufferedReader(f);

		String uniprot_status, uniprotID, fullName, definition, gene_name,ecNumber, organism, taxonomy;
		uniprot_status = uniprotID = fullName = definition = gene_name = ecNumber = organism = taxonomy = "";


		boolean gotUniprotID, gotFullname, goToNextLine, gotOrganism;
		gotUniprotID = gotFullname = goToNextLine =  gotOrganism = false;

		String line = reader.readLine();	

		while(line != null){

			if(!line.startsWith("//")){

				if(goToNextLine == false && line.startsWith("ID") || line.startsWith("AC") || line.startsWith("DE") || line.startsWith("GN") ||line.startsWith("OS") || line.startsWith("OC")){
					//############# ID ###########
					java.util.regex.Matcher matcher = Pattern.compile("^ID\\s+(\\w+)\\s+(\\S+).*").matcher(line);
					if(matcher.find()){
						//name = matcher.group(1);
						uniprot_status = matcher.group(2).replaceAll("\\W","");

						//					for (int i = 1 ; i < matcher.groupCount() +1; i++){
						//						System.out.println(matcher.group(i));
						//					}

						goToNextLine =true;	
					}

					//########### AC - UNIPROT ID ###########
					matcher = Pattern.compile("^AC\\s+(\\w+).*").matcher(line);
					if(matcher.find() && goToNextLine == false && gotUniprotID ==false){//&& gotUniprotID == false){

						uniprotID = matcher.group(1);
						goToNextLine =true;
						gotUniprotID =true;

						//System.out.println(uniprotID);

					}

					//########### DE - FULL NAME / PRODUCT / DEFINITION ###########

					matcher = Pattern.compile("^DE.+Full=([^;]+).*").matcher(line);
					if(matcher.find() && goToNextLine == false && gotFullname== false){
						fullName = matcher.group(1);
						goToNextLine =true;
						gotFullname = true;
						definition = line;


					}

					//########### DE - DEFINITION / EC NUMBERS ###########

					java.util.regex.Matcher matcher2 = Pattern.compile("^DE\\s+(.+).*").matcher(definition);
					if(matcher2.find()){
						definition = matcher2.group(1);

					}

					matcher = Pattern.compile("^DE\\s+(.+).*").matcher(line);
					if(matcher.find() && goToNextLine == false){
						String t = matcher.group(1);

						if(!t.contains("EC=")){

							definition += t;
							goToNextLine =true;
						}
						else{
							ecNumber += t; 
							goToNextLine =true;
						}

					}

					// ###########  GN / NAME ###################
					matcher = Pattern.compile("^GN\\s+Name=(\\w+).*").matcher(line);
					if (matcher.find() && goToNextLine == false){
						gene_name = matcher.group(1);
						goToNextLine =true;	
						if (gene_name.length() < 2){
							gene_name = "";
						}
					}


					// ########### OS / OC - ORGANISM / TAXONOMY ###########
					matcher = Pattern.compile("^OS\\s+(\\w+\\s\\w+).*").matcher(line);//\\s+\\(strain\\s+(.+)\\/.*").matcher(line);
					if (matcher.find() && goToNextLine == false && gotOrganism == false){
						organism = matcher.group(1);
						goToNextLine =true;	
						gotOrganism = true;
					}

					//					matcher = Pattern.compile("^OS\\s+(.+)\\.").matcher(line);//\\s+\\(strain\\s+(\\S+)\\s+(\\S+).*").matcher(s);
					//					if (matcher.find()&& goToNextLine == false && gotOrganism == false){
					//						organism = matcher.group(1);
					//						if(organism.contains("strain")){
					//							organism =organism.replace("strain", "");
					//						}
					//						if(organism.contains("(") && organism.contains(")")){
					//							organism =organism.replaceAll("[()]","");
					//						}
					//
					//						goToNextLine =true;	
					//						gotOrganism = true;
					//					}

					matcher = Pattern.compile("^OC\\s+(.+)$").matcher(line);
					if (matcher.find() && goToNextLine == false){
						taxonomy += matcher.group(1);
						goToNextLine =true;		
					}
				}
				// ############# NEXT LINE ##############
				if(goToNextLine == true){
					goToNextLine = false;
					line = reader.readLine();
				}
				else{
					line = reader.readLine();
				}

			}

			else{
				do{	
					//										System.out.println(name);
					//										System.out.println(uniprot_status);
					//										System.out.println(uniprotID);
					//										System.out.println(fullName);
					//										System.out.println(definition);
					//															if (!ecNumber.isEmpty()){
					//																System.out.println(ecNumber);
					//															}
					//										System.out.println(organism);
					//										System.out.println(taxonomy);
					//					
					//										System.out.println(line.charAt(0));
					//
					//										String [] info = new String [6];
					//										info[0] = uniprot_status;
					//										info[1] = fullName;
					//										info[2] = definition;
					//										info[3] = ecNumber;
					//										info[4] = organism;
					//										info[5] = taxonomy;
					//										uniprotRetrieveInformation.put(uniprotID, info);

					for (String geneid : sortedHash.keySet()){

						LinkedHashMap<String, String[]> homologueshash = sortedHash.get(geneid);
						
						if (homologueshash.containsKey(uniprotID)){
							//							count +=1;
							//							System.out.println(count + "\t" +uniprotID);
							//System.out.println(geneid);
							String [] homoInformation = homologueshash.get(uniprotID);
							homoInformation[2] = gene_name;
							homoInformation[3] = uniprot_status;
							homoInformation[4] = fullName;
							homoInformation[5] = definition;
							homoInformation[6] = ecNumber;
							homoInformation[7] = organism;
							homoInformation[8] = taxonomy;
						}
					}

					line = reader.readLine();
					//name = "";
					uniprot_status = uniprotID = fullName = definition = ecNumber = organism = taxonomy = "";

					gotUniprotID = gotFullname = gotOrganism =false;

				}
				while(line != null && !(line.startsWith("ID")));
			}

		}
		//printHashmap(sortedHash);
		return sortedHash;
	}

	public HashMap< String, FastaSequence> saveSequences() throws Exception{
		HashMap<String, FastaSequence> sequencesHash = new HashMap<>();
		String delimiter = ">";
		ParserBlast parser = new ParserBlast(this.queryPath, delimiter);

		String geneid, sequence;
		geneid = sequence = "";
		FastaSequence fastaseq = new FastaSequence(sequence);
		try{
			if(parser.openFile()){


				while (parser.nextLine()){

					String line = parser.getLine();

					if(! line.startsWith(delimiter)){
						sequence += line;

					}
					else {
						if (!geneid.isEmpty()){
							fastaseq = new FastaSequence(sequence);
							sequencesHash.put(geneid, fastaseq);
							geneid = line.substring(1);
							sequence = "";
						}
						else{
							geneid = line.substring(1);
						}
						//System.out.println(sequencesHash.size());

					}
				}
				fastaseq = new FastaSequence(sequence);
				sequencesHash.put(geneid, fastaseq);
				
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		return sequencesHash;
	}

	private LinkedHashMap< String, LinkedHashMap< String, String []>> orderHashMap (LinkedHashMap < String, LinkedHashMap< String, String []>> linkedhashmap){

		double score =  0;



		LinkedHashMap < String, LinkedHashMap< String, String []>> hashfinal = new LinkedHashMap<>();

		for (String key : linkedhashmap.keySet()){

			LinkedHashMap< String,  String []> hashIntermedia = new LinkedHashMap<>();
			List<Double> orderedScores = new ArrayList<>();
			LinkedHashMap <String, String []> homologueshash = linkedhashmap.get(key);


			for (String key2 : homologueshash.keySet()){
				String[] value2 = homologueshash.get(key2);
				score = Double.parseDouble(value2[0]);
				orderedScores.add(score);
			}
			Collections.sort(orderedScores);
			Collections.reverse(orderedScores);

			//System.out.println(orderedScores);
			double next_score = -1;
			int countRepeatedScores= 1;
			for(int iter = 0; iter < orderedScores.size(); iter += countRepeatedScores){

				double i = orderedScores.get(iter);
				int index = iter;

				if (orderedScores.size() != index+1 ){
					next_score = orderedScores.get(index+1);
				}


				for (String key3 : homologueshash.keySet()){
					String [] interm = homologueshash.get(key3);
					double score_interm = Double.parseDouble(interm[0]);
					if(score_interm == i && next_score != i){
						String[] p = homologueshash.get(key3);
						countRepeatedScores = 1;
						hashIntermedia.put(key3, p);
						break;
					}
					else if (score_interm == i && next_score == i){
						int count = 0;
						for (double j : orderedScores){
							if(j == i){
								count ++;
							}	
						}

						countRepeatedScores = count;

						List<Double> evalues = new ArrayList<>();
						for (int k = index; k < index + count ; k++ ){
							//							System.out.println(k);
							//							System.out.println(count);
							for (String key4 : homologueshash.keySet()){
								String [] p = homologueshash.get(key4);
								if(Double.parseDouble(p[0]) == i ){
									//									System.out.println(i);
									evalues.add(Double.parseDouble(p[1]));

								}
							}
						}
						Collections.sort(evalues);
						for (double eValue : evalues){
							for (String key5 : homologueshash.keySet()){
								String [] p = homologueshash.get(key5);
								if (Double.parseDouble(p[1]) == eValue && Double.parseDouble(p[0]) == i){
									hashIntermedia.put(key5, p);

								}
							}

						}
						break;

					}
				}
				//System.out.println(i);



			}
			String geneID = key;
			hashfinal.put(geneID, hashIntermedia);
			//	hashIntermedia.clear();


		}
		//printHashmap(linkedhashmap);
		//printHashmap(hashfinal);
		return hashfinal;	

	}

	private String makeprintable(LinkedHashMap<String, String []> map) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (String k : map.keySet()) {

			String [] set = map.get(k);
			sb.append("\t\t"+k + " [");
			for (String array : set) {
				sb.append( array + "\t");
			}
			sb.append("]\n");
		}
		return sb.toString();
	}

	private void printHashmap(LinkedHashMap<String, LinkedHashMap<String, String[]>> hash){
		for (String key : hash.keySet()){
			LinkedHashMap<String, String []> value = hash.get(key);

			System.out.println("|" + key + "|" + "\t\t" + makeprintable(value));
		}
	}

	
	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		return cancel;
	}

	/**
	 * 
	 */
	public void setCancel() {

		this.cancel.set(true);
		this.process.destroy();
		setCancel(this.cancel);		
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(AtomicBoolean cancel) {
		this.cancel = cancel;
	}
}

