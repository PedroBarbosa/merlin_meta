package alignment.blast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;

import alignment.blast.ebi.rest.EbiBlastClientRest;
import alignment.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import alignment.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;

public class NCBIQBlastServiceTest {
	/**
	 * The program take only a string with a path toward a sequence file
	 * 
	 * For this example, I keep it simple with a single FASTA formatted file
	 * 
	 */
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		EbiBlastClientRest rbw;
		NCBIQBlastAlignmentProperties rqb;
		NCBIQBlastOutputProperties rof;
		InputStream is;
		ConcurrentLinkedQueue<String> rids = new ConcurrentLinkedQueue<String>();
		String request = "";

		try
		{

			// Let's capture the sequences in a file...
			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(args[0]));

			/*
			 * You would imagine that one would blast a bunch of sequences of
			 * identical nature with identical parameters...
			 */
			//rbw = new NCBIQBlastService();
			rbw = new EbiBlastClientRest();
			rqb = new NCBIQBlastAlignmentProperties();

			rqb.setBlastProgram("blastp");
			rqb.setBlastDatabase("uniprotkb");
			rqb.setBlastExpect(new Double(1E-10));
			//	rqb.setBlastExpect(0.00000000000000000001);
			rqb.setBlastMatrix("PAM30");
			rqb.setBlastGapCreation(10);
			rqb.setBlastGapExtension(1);
			rqb.setBlastWordSize(3);
			
			//rqb.setAdvancedOptions("-e:1e-30, -E:1, -G:10, -W:3");
			//System.out.println(rqb);

			//enviar multithreading de 3 em 3 segundos ou tentar parsing dos resultados assim

			/*
			 * First, let's send all the sequences to the QBlast service and
			 * keep the RID for fetching the results at some later moments
			 * (actually, in a few seconds :-))
			 *
			 * Using a data structure to keep track of all request IDs is a good
			 * practice.
			 *
			 */
			
			String s="";
			int counter=0;
			for (Entry<String, ProteinSequence> entry : a.entrySet()) {

					counter++;
					//				System.out.println( entry.getValue().getOriginalHeader() + "\n");
					//				String s = entry.getValue().toString();
					//				request = rbw.sendAlignmentRequest(s,rqb);
					//				System.out.println(s);
					//				rid.add(request);
					//s += ">"+entry.getValue().getOriginalHeader()+"\n";
					s += entry.getValue().getSequenceAsString()+"\n";

					request = NCBIQBlastServiceTest.sendRequest(rbw,rqb,s,0);
					rids.add(request);
					//s="";
					System.out.println(request);
			}

			//request = NCBIQBlastServiceTest.sendRequest(rbw,rqb,s,0);
			//rids.add(request);

			System.out.println(counter);
			System.out.println("number of rids: "+rids.size());
			System.out.println();

			/*
			 * Let's check that our requests have been processed. If completed,
			 * let's look at the alignments with my own selection of output and
			 * alignment formats.
			 */
			
			//rids.offer("ncbiblast-R20140502-150238-0783-77012752-oy");
			//rbw.getHolder().put("ncbiblast-R20140502-150238-0783-77012752-oy", System.currentTimeMillis());

			while(rids.size()>0) {
				
				String aRid = rids.poll();
				//System.out.println("trying to get BLAST results for RID "+ aRid);
				try {
					
					if(rbw.isReady(aRid, System.currentTimeMillis())) {
						
//						rof = new NCBIQBlastOutputProperties();
//						rof.setOutputFormat(NCBIQBlastOutputFormat.TEXT);
//						rof.setAlignmentOutputFormat(NCBIQBlastOutputFormat.PAIRWISE);
//						rof.setDescriptionNumber(100);
//						rof.setAlignmentNumber(100);
//						is = rbw.getAlignmentResults(aRid, rof);
						
						is = rbw.getAlignmentResults(aRid, null);

						BufferedReader br = new BufferedReader(new InputStreamReader(is));

						String line = null;

						while ((line = br.readLine()) != null) {
							
							System.out.println(line);
						}
					}
					else {
						
						rids.offer(aRid);
					}
				}
				catch (Exception e) {
					
					rids.offer(aRid);
					e.printStackTrace();
				}

				//System.out.println("queue size: "+rids.size());
			}

//			for (String aRid : rids) {
//				
//				System.out.println("trying to get BLAST results for RID "+ aRid);
//				boolean wasBlasted = false, noErrors=true;
//				int i = 0;
//				while (!wasBlasted)
//				{
//					try
//					{
//						wasBlasted = rbw.isReady(aRid, System.currentTimeMillis());
//					}
//					catch (Exception e)
//					{
//						i++;
//						System.err.println("Retrying to determine blast state. Try number: "+i);
//						if(i>100)
//						{
//							wasBlasted=true;
//							noErrors=false;
//						}
//						//e.printStackTrace();
//					}
//				}
//
//				if(noErrors)
//				{
//					rof = new NCBIQBlastOutputProperties();
//					rof.setOutputFormat(NCBIQBlastOutputFormat.TEXT);
//					rof.setAlignmentOutputFormat(NCBIQBlastOutputFormat.PAIRWISE);
//					rof.setDescriptionNumber(100);
//					rof.setAlignmentNumber(100);
//					is = rbw.getAlignmentResults(request, rof);
//
//					BufferedReader br = new BufferedReader(
//							new InputStreamReader(is));
//
//					String line = null;
//
//					while ((line = br.readLine()) != null) {
//						System.out.println(line);
//					}
//				}
//				else
//				{
//					Workbench.getInstance().error("A problem as occurred.\nPlease check you internet connetion and try again later");
//					System.out.println("A problem as occurred.\nPlease check you internet connetion and try again later");
//				}
//			}
			long endTime = System.currentTimeMillis();
			System.out.println("Total elapsed time in execution of query "+request+" is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
					-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		}
		/*
		 * What happens if the file can't be read
		 */
		catch (IOException ioe) {ioe.printStackTrace();}
		/*
		 * What happens if FastaReaderHelper hits a snag
		 */
		catch (Exception bio) {
			bio.printStackTrace();
		}
	}

	/**
	 * @param rbw
	 * @param rqb
	 * @param query
	 * @param i
	 * @return
	 */
	private static String sendRequest(EbiBlastClientRest rbw, NCBIQBlastAlignmentProperties rqb, String query, int i) {

		try {
			
			return rbw.sendAlignmentRequest(query,rqb);
		}
		catch (Exception e) {

			i++;
			if(i<100) {

				System.out.println("send request try \t"+i+"\n"+query+"\n");
				//NCBIQBlastServiceTest.sendRequest(rbw, rqb, query, i);
			}
			e.printStackTrace();
		}
		return null;
	}

}
