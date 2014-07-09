/**
 * 
 */
package alignment.hmmer;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import operations.HmmerSimilaritySearch.HmmerDatabase;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.merlin.utilities.MySleep;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import alignment.HomologyDataClient;
import alignment.HomologyDataClient.HomologySearchServer;
import alignment.LoadSimilarityResultstoDatabaseRemote;
import datatypes.Project;

/**
 * @author ODias
 *
 */
public class SubmitHMMER implements Runnable {

	private boolean isNCBIGenome;
	private String[] orgArray;
	private Project project;
	private int numberOfAlignments;
	private TimeLeftProgress progress;
	private int sequences_size;
	private long startTime;
	private ConcurrentLinkedQueue<String> sequences;
	private Map<String, ProteinSequence> query;
	private AtomicBoolean cancel;
	private HmmerDatabase database;
	private double eValue;
	private ConcurrentLinkedQueue<String> currentlyBeingProcessed;
	private AtomicInteger sequencesCounter, errorCounter;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private boolean uniprotStatus;
	private AtomicLong time;

	/**
	 * @param sequences
	 * @param numberOfAlignments
	 * @param project
	 * @param isNCBIGenome
	 * @param orgArray
	 * @param database
	 * @param cancel
	 * @param sequences_size
	 * @param startTime
	 * @param progress
	 * @param taxonomyMap 
	 * @param uniprotStatus 
	 */
	public SubmitHMMER(ConcurrentLinkedQueue<String> sequences, Map<String, ProteinSequence> query, double expectedVal, int numberOfAlignments, Project project, 
			boolean isNCBIGenome, String[] orgArray, HmmerDatabase database, AtomicBoolean cancel, 
			int sequences_size, long startTime, TimeLeftProgress progress, ConcurrentLinkedQueue<String> currentlyBeingProcessed, AtomicInteger sequencesCounter, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicInteger errorCounter, boolean uniprotStatus, AtomicLong time) {

		this.sequencesCounter = sequencesCounter;
		this.sequences = sequences;
		this.query = query;
		this.eValue = expectedVal;
		this.isNCBIGenome=isNCBIGenome;
		this.orgArray=orgArray;
		this.project = project;
		this.numberOfAlignments = numberOfAlignments;
		this.database = database;
		this.cancel = cancel;
		this.sequences_size = sequences_size;
		this.startTime = startTime;
		this.progress = progress;
		this.currentlyBeingProcessed = currentlyBeingProcessed;
		this.taxonomyMap = taxonomyMap;
		this.uniprotStar = uniprotStar;
		this.errorCounter = errorCounter;
		this.uniprotStatus = uniprotStatus;
		this.time = time;
	}

	@Override
	public void run() {

		int counter = 0;
		Map<String, String> hmmerResultKeys = new HashMap<>();

		while(this.sequences.size()>0 && !this.cancel.get()) {

			if(this.cancel.get()) {

				this.sequences.clear();
				this.sequencesCounter.set(sequences_size);
			}
			else {

				String sequence = null;

				try {

					sequence = this.sequences.poll();

					while (this.currentlyBeingProcessed.contains(sequence)) {
						if(this.sequences.size()>0) {

							sequence = this.sequences.poll();
						}
						else {

							sequence = null;
						}
					}

					if(sequence != null) { 
						
						if(this.currentlyBeingProcessed.add(sequence));

						ReadHmmertoList hmmerToList = new ReadHmmertoList(this.query.get(sequence).getSequenceAsString(),this.query.get(sequence).getAccession().getID(),
								this.database,this.numberOfAlignments,this.eValue, this.cancel);

						String resLink;
						
						if(hmmerResultKeys.containsKey(sequence)) {
							
							resLink = hmmerResultKeys.get(sequence);
						}
						else {
							
							resLink = hmmerToList.getJobID(time, this.cancel);
							hmmerResultKeys.put(sequence, resLink);
						}
						
						boolean processed = hmmerToList.sCan(resLink);

						HomologyDataClient homologyDataClient;
						if(processed) {

							if(hmmerToList.getResults().size()>0 && !this.cancel.get()) {

								if(isNCBIGenome) {

									homologyDataClient = new HomologyDataClient(hmmerToList, this.taxonomyMap, this.uniprotStar,this.cancel, this.uniprotStatus, HomologySearchServer.HMMER);
								}
								else {

									homologyDataClient = new HomologyDataClient(hmmerToList, this.orgArray, this.taxonomyMap, this.uniprotStar,this.cancel, this.uniprotStatus, HomologySearchServer.HMMER);
								}

								if(homologyDataClient.isDataRetrieved()) {

									LoadSimilarityResultstoDatabaseRemote lbr = new LoadSimilarityResultstoDatabaseRemote(homologyDataClient,this.numberOfAlignments,this.eValue,this.project,this.cancel);
									lbr.loadData();

									if(lbr.isLoaded()) {

										counter=0;
										System.gc();
										this.sequencesCounter.incrementAndGet();
										System.out.println("Gene\t"+homologyDataClient.getLocus_tag()+"\tprocessed.");
									}
									else {

										counter=0;
										this.sequences.offer(sequence);
										this.currentlyBeingProcessed.remove(sequence);
									}
								}
								else {

									this.sequences.offer(sequence);
									this.currentlyBeingProcessed.remove(sequence);
								}
							}
							else {

								if(processed && !this.cancel.get()) {

									homologyDataClient = new HomologyDataClient(hmmerToList.getQuery(),"hmmer",this.cancel, this.uniprotStatus, HomologySearchServer.HMMER);
									homologyDataClient.setFastaSequence(this.query.get(sequence).getSequenceAsString());
									homologyDataClient.setDatabaseID(this.database.toString());
									homologyDataClient.setProgram("hmmer");
									homologyDataClient.setVersion("");
									homologyDataClient.setNoSimilarity(true);
									LoadSimilarityResultstoDatabaseRemote lbr = new LoadSimilarityResultstoDatabaseRemote(homologyDataClient,this.numberOfAlignments,this.eValue,this.project,this.cancel);
									lbr.loadData();
									counter=0;
									this.sequencesCounter.incrementAndGet();
									System.out.println("Gene\t"+homologyDataClient.getLocus_tag()+"\tprocessed. No similarities.");
								}
							}
						}
						else {

							//System.out.println("Sequence "+sequence+" offered to stack");
							this.currentlyBeingProcessed.remove(sequence);
							this.sequences.offer(sequence);
							MySleep.myWait(5000);
						}
					}
				}
				catch (Exception e) {

					e.printStackTrace();

					counter = counter + 1 ;
					if(!this.cancel.get()) {

						if(counter<25) {

							System.err.println(SubmitHMMER.class+" exception.\tReprocessing\t"+sequence+".\t counter:"+counter);
							if(sequence!=null) {
								
								this.sequences.offer(sequence);
								this.currentlyBeingProcessed.remove(sequence);
								
//								try {
//								
//									HmmerFetch.delete(hmmerResultKeys.get(sequence));
//								}
//								catch (Exception e1) {
//									
//									e1.printStackTrace();
//								}
								hmmerResultKeys.remove(sequence);
							}
						} 
						else {

							errorCounter.incrementAndGet();
							counter = 0;
						}
					}
				}
			}

			if(!this.cancel.get()) {

				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),this.sequencesCounter.get(),this.sequences_size);
			}
		}
	}

	/**
	 * @return the progress
	 */
	public TimeLeftProgress getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(TimeLeftProgress progress) {
		this.progress = progress;
	}

	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		return cancel;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(AtomicBoolean cancel) {
		this.cancel = cancel;
	}

}
