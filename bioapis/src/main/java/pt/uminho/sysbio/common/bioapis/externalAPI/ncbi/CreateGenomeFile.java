package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.axis2.AxisFault;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;

import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import pt.uminho.sysbio.common.utilities.datastructures.pair.Pair;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;

public class CreateGenomeFile {

	private String tempPath;
	private String genomeID;
	private String today;
	private boolean isNCBIGenome;

	/**
	 * @param genomeID
	 * @param numberOfDaysOld
	 * @param sourceDB
	 * @throws IOException
	 * @throws ParseException
	 */
	public CreateGenomeFile(String genomeID, int numberOfDaysOld, String sourceDB, String extension) throws Exception {

		this.today = CreateGenomeFile.setToday();
		String path = FileUtils.getCurrentTempDirectory();
		this.genomeID = genomeID;
		this.tempPath = path;
		this.createTempFile(numberOfDaysOld, sourceDB, extension);
	}
	
	/**
	 * @param genomeID
	 * @throws Exception 
	 */
	public static Map<String, ProteinSequence> getGenomeFromID(String genomeID, String extension) throws Exception {
		
		String tempPath = FileUtils.getCurrentTempDirectory();
		if(!CreateGenomeFile.currentTemporaryDataIsNOTRecent(0,tempPath, genomeID, CreateGenomeFile.setToday(), extension)) {
			
			return FastaReaderHelper.readFastaProteinSequence(new File(FileUtils.getCurrentTempDirectory()+genomeID+extension));
		}
		return null;
	}

	/**
	 * @param genomeID
	 * @param fastaFiles
	 * @throws Exception
	 */
	public CreateGenomeFile(String genomeID, List<File> fastaFiles, String extension) throws Exception {
		
		this.today = CreateGenomeFile.setToday();
		String path = FileUtils.getCurrentTempDirectory();
		this.genomeID = genomeID;
		this.tempPath = path;
		this.createGenomeFileFromFasta(fastaFiles, extension);
	}


	/**
	 * @param fastaFiles
	 * @throws Exception 
	 */
	private void createGenomeFileFromFasta(List<File> fastaFiles, String extension) throws Exception {
		
		if(CreateGenomeFile.currentTemporaryDataIsNOTRecent(-1,this.tempPath,this.genomeID,this.today,extension)) {
			
			Map<String, ProteinSequence> sequences= new HashMap<String, ProteinSequence>();
			for(File fastFile : fastaFiles) {
				
				sequences.putAll(FastaReaderHelper.readFastaProteinSequence(fastFile));
			}

			this.buildFastFile(null, sequences, extension);
			this.createLogFile(extension);
		}
	}

	/**
	 * @return
	 */
	private static String setToday() {
		
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd");
		return formatter.format(currentDate.getTime());
		//formatter = new SimpleDateFormat("HH:mm:ss");
		//String now = formatter.format(currentDate.getTime());
	}

	/**
	 * @param numberOfDaysOld
	 * @return
	 * @throws Exception
	 */
	public Map<String, ProteinSequence> getGenome(String extension) throws Exception {
		
		return FastaReaderHelper.readFastaProteinSequence(new File(this.tempPath+this.genomeID+extension));
	}


	/**
	 * @param numberOfDaysOld
	 * @param sourceDB
	 * @throws IOException
	 * @throws ParseException
	 */
	private void createTempFile(int numberOfDaysOld,String sourceDB, String extension) throws Exception{

		new File(this.tempPath).mkdir();
		if(CreateGenomeFile.currentTemporaryDataIsNOTRecent(numberOfDaysOld,this.tempPath,this.genomeID,this.today, extension))
		{
			long startTime = System.currentTimeMillis();
			Pair<Map<String,String>, Map<String,ProteinSequence>> pair = this.getFastaAAGenomeFromEntrezProtein(sourceDB);
			Map<String, ProteinSequence> sequences = pair.getPairValue();
			Map<String, String> locusTag = pair.getValue();
			long endTime = System.currentTimeMillis();
			System.out.println("Total elapsed time in execution of method is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

			this.buildFastFile(locusTag, sequences, extension);
			this.createLogFile(extension);
			//return pair;
		}
	}

	/**
	 * @param locusTag
	 * @param sequences
	 * @throws IOException
	 */
	private void buildFastFile(Map<String, String> locusTag, Map<String, ProteinSequence> sequences, String extension) throws IOException{
		
		FileWriter fstream = new FileWriter(this.tempPath+this.genomeID+extension);  
		BufferedWriter out = new BufferedWriter(fstream); 

		for(String key:sequences.keySet())
		{
			if(locusTag!=null && locusTag.containsKey(key) && locusTag.get(key)!=null)
			{
				out.write(">"+key+"|"+locusTag.get(key)+"\n");
			}
			else
			{
				out.write(">"+key+"\n");
			}
			out.write(sequences.get(key).getSequenceAsString()+"\n");
		}
		out.close();
	}

	/**
	 * @throws IOException
	 */
	private void createLogFile(String extension) throws IOException{

		StringBuffer buffer = new StringBuffer();
		if(new File(this.tempPath+"genomes.log").exists()) {
			
			FileInputStream finstream = new FileInputStream(this.tempPath+"genomes.log");
			DataInputStream in = new DataInputStream(finstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				
				buffer.append(strLine+"\n");
			}
			br.close();
			in.close();
			finstream.close();
		}
		else
		{
			buffer.append("organismID\tdate\n");
		}

		new File(this.tempPath+"genomes.log");
		FileWriter fstream = new FileWriter(this.tempPath+"genomes.log");  
		BufferedWriter out = new BufferedWriter(fstream);
		out.append(buffer);
		out.write(this.genomeID+extension+"\t"+today);
		out.close();
	}

	/**
	 * @param numberOfDaysOld
	 * @param tempPath
	 * @param genomeID
	 * @param today
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private static boolean currentTemporaryDataIsNOTRecent(int numberOfDaysOld, String tempPath, String genomeID, String today, String extension) throws ParseException, IOException{
		
		if(numberOfDaysOld<0) {
			
			return true;
		}
		
		if(new File(tempPath+"genomes.log").exists()) {
			
			FileInputStream fstream = new FileInputStream(tempPath+"genomes.log");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String logFileDate = null;
			StringBuffer buffer = new StringBuffer();

			String strLine;
			while ((strLine = br.readLine()) != null) {
				
				String[] data = strLine.split("\t"); 
			
				if(data[0].equalsIgnoreCase(genomeID+extension)) {
					
					logFileDate = data[1];
				}
				else {
					
					buffer.append(strLine+"\n");
				}
			}
			br.close();
			in.close();
			fstream.close();

			if(logFileDate!=null) {
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMM/dd");
				Date date1 = sdf.parse(today);
				Date date2 = sdf.parse(logFileDate.replace("\"", ""));
				
				if(numberOfDaysOld==0) {
					
					date2 = sdf.parse(today);
				}
				
				if(date1.after(date2) || date1.equals(date2)) {
					
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.setTime(date1);
					cal2.setTime(date2);

					if(cal1.get(Calendar.YEAR)==cal2.get(Calendar.YEAR)) {
						
						int old = cal1.get(Calendar.DAY_OF_YEAR)-cal2.get(Calendar.DAY_OF_YEAR);
						
						if(old<=numberOfDaysOld || numberOfDaysOld<=0) {
							
							return false;
						}
						else  {
							
							System.out.println("File is\t"+old+"\tdays old.");
						}
					}
				}
				new File(tempPath+"genomes.log");
				FileWriter fWriterStream = new FileWriter(tempPath+"genomes.log");  
				BufferedWriter out = new BufferedWriter(fWriterStream);
				out.append(buffer);
				out.write(genomeID+extension+"\t"+logFileDate);
				out.close();
			}
		}
		else {
			
			FileWriter fstream = new FileWriter(tempPath+"genomes.log");  
			BufferedWriter out = new BufferedWriter(fstream);  
			new File(tempPath+"genomes.log");
			out.write("organismID\tdate\n");
			out.close();
		}
		return true;
	}

	/**
	 * @param genomeID
	 * @return 
	 * @throws AxisFault 
	 */
	private Pair<Map<String, String>,Map<String, ProteinSequence>> getFastaAAGenomeFromEntrezProtein(String sourceDB) throws Exception{

		List<String> ids_list = new ArrayList<String>();
		ids_list.add(this.genomeID);

		NcbiServiceStub_API stub = new NcbiServiceStub_API(2);
		List<List<String>> links_list = stub.getLinksList(ids_list, "taxonomy", "protein",0);

		NcbiEFetchSequenceStub_API fetch_stubApi;
		fetch_stubApi = new NcbiEFetchSequenceStub_API(2);
		
		Pair<Map<String, String>,Map<String, ProteinSequence>> pair = fetch_stubApi.getLocusAndSequencePairFromID(links_list,500,sourceDB);

		pair = this.checkForDuplicates(pair);

		return pair;
	}

	/**
	 * @param pair
	 * @return
	 */
	private Pair<Map<String, String>,Map<String, ProteinSequence>> checkForDuplicates(Pair<Map<String, String>,Map<String, ProteinSequence>> pair) {

		Map<String, String> locustags = pair.getValue();
		Map<String, ProteinSequence> sequences =  pair.getPairValue();
		Set<String> locus_to_be_checked = new HashSet<String>();

		List<String> duplicate_keys = new ArrayList<String>();
		
		for(String key : locustags.keySet()) {
			
			if(!duplicate_keys.contains(key)) {
				
				String locus = locustags.get(key);
				for(String test_key : locustags.keySet()) {
					
					if(locustags.get(test_key).equals(locus)) {
						
						duplicate_keys.add(test_key);
						if(!test_key.equals(key)) {
							
							locus_to_be_checked.add(key);
						}
					}
				}
				duplicate_keys.remove(key);
			}
		}

		for(String key:duplicate_keys) {
			
			locustags.remove(key);
			sequences.remove(key);
		}

		duplicate_keys = new ArrayList<String>();
		for(String key : sequences.keySet()) {
			
			if(!duplicate_keys.contains(key)) {
				
				ProteinSequence sequence = sequences.get(key);
				for(String test_key : sequences.keySet()) {
					
					if(sequences.get(test_key).equals(sequence)) {
						
						duplicate_keys.add(test_key);
						if(!test_key.equals(key)) {
							
							locus_to_be_checked.add(key);
						}
					}
				}
				duplicate_keys.remove(key);

			}
		}

		for(String key:duplicate_keys) {
			
			locustags.remove(key);
			sequences.remove(key);
		}

		for(String acc : locus_to_be_checked) {
			
			String locus = locustags.get(acc);
			UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(acc,0);
			if(uniProtEntry!=null && UniProtAPI.getLocusTag(uniProtEntry)!=null && UniProtAPI.getLocusTag(uniProtEntry).size()>0) {
				
				locus = UniProtAPI.getLocusTag(uniProtEntry).get(0).getValue();
			}
			else {
				
				uniProtEntry = UniProtAPI.getUniProtEntryID(locustags.get(acc),0);
				if(uniProtEntry!=null && UniProtAPI.getLocusTag(uniProtEntry)!=null && UniProtAPI.getLocusTag(uniProtEntry).size()>0) {
					
					locus = UniProtAPI.getLocusTag(uniProtEntry).get(0).getValue();
				}
			}

			if(!locus.equalsIgnoreCase(locustags.get(acc))) {
				
				locustags.put(acc, locus);
			}
		}
		pair.setValue(locustags);
		pair.setPairValue(sequences);
		return pair;
	}

	/**
	 * @return the tempPath
	 */
	public String getTempPath() {
		return tempPath;
	}

	/**
	 * @param tempPath the tempPath to set
	 */
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	/**
	 * @return the genomeID
	 */
	public String getGenomeID() {
		return genomeID;
	}

	/**
	 * @param genomeID the genomeID to set
	 */
	public void setGenomeID(String genomeID) {
		this.genomeID = genomeID;
	}

	public static void main(String [] args) throws Exception{
		//new CreateGenomeFile("83333",5,"",".faa"); // ecoli k12
		
		NcbiEFetchSequenceStub_API fetchStub = new NcbiEFetchSequenceStub_API(50);
		Set<String> querySet = new HashSet<String>();
		querySet.add("NP_207514.1");
		System.out.println(fetchStub.getLocusFromID(querySet,10).get("NP_207514.1"));
	}

	/**
	 * @return the isNCBIGenome
	 */
	public boolean isNCBIGenome() {
		return isNCBIGenome;
	}

	/**
	 * @param isNCBIGenome the isNCBIGenome to set
	 */
	public void setNCBIGenome(boolean isNCBIGenome) {
		this.isNCBIGenome = isNCBIGenome;
	}
}
