package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.NcbiData;

/**
 * @author ODias
 *
 */
public class Runnable_Sequences_retriever implements Runnable {


	private ConcurrentLinkedQueue<List<String>> queryArray;
	private String sourceDB;
	private ConcurrentHashMap<String,String> locus_Tag;
	private ConcurrentHashMap<String,ProteinSequence> sequences;

	/**
	 * @param queryArray
	 * @param locus_Tag
	 * @param sequences
	 * @param sourceDB
	 */
	public Runnable_Sequences_retriever(ConcurrentLinkedQueue<List<String>> queryArray, ConcurrentHashMap<String,String> locus_Tag, ConcurrentHashMap<String,ProteinSequence> sequences, String sourceDB) {
		
		this.queryArray = queryArray;
		this.sourceDB = sourceDB;
		this.locus_Tag = locus_Tag;
		this.sequences = sequences;
	}


	@Override
	public void run() {

		EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
		req.setDb("protein");

		NcbiData ncbiData = new NcbiData(this.locus_Tag, this.sequences);

		while(this.queryArray.size()>0) {

			System.out.println("countdown " + this.queryArray.size());
			List<String> query = this.queryArray.poll();

			try {


				NcbiEFetchSequenceStub_API stub = new NcbiEFetchSequenceStub_API(1);

				ncbiData = stub.getSequences(query, req, this.sourceDB, ncbiData);
			}
			catch (RemoteException e){

				if(query.size()>50) {

					System.out.println("Reducing concatenation size to:\t"+50);
					List<String> list = new ArrayList<String>();
					for(int i = 0; i<query.size();i++) {

						list.add(query.get(i));
						if(list.size()>49) {

							queryArray.offer(list);
							list = new ArrayList<String>();
						}
					}
					if(list.size()>0) {

						queryArray.offer(list);
					}
				}
				else {

					System.out.println("Reducing concatenation size to:\t"+query.size()/2);
					int half = query.size()/2;
					int half_1 =(query.size()/2)+1;
					queryArray.offer(query.subList(0, half));
					queryArray.offer(query.subList(half_1,query.size()));
				}
			} catch (Exception e) {

				e.printStackTrace();
			}
			System.gc();
		}
		
	}


}
