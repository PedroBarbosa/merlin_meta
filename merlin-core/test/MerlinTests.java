import java.util.GregorianCalendar;

import org.junit.Test;

import alignment.blast.ebi.rest.EbiBlastClientRest;


public class MerlinTests {

	@Test
	public void test() throws Exception {
//		String fasta = 
//				">sp|P35922|FMR1_MOUSE Fragile X mental retardation protein 1 homolog OS=Mus musculus GN=Fmr1 PE=1 SV=1\n" +
//						"MEELVVEVRGSNGAFYKAFVKDVHEDSITVAFENNWQPERQIPFHDVRFPPPVGYNKDIN" +
//						"ESDEVEVYSRANEKEPCCWWLAKVRMIKGEFYVIEYAACDATYNEIVTIERLRSVNPNKP" +
//						"ATKDTFHKIKLEVPEDLRQMCAKESAHKDFKKAVGAFSVTYDPENYQLVILSINEVTSKR" +
//						"AHMLIDMHFRSLRTKLSLILRNEEASKQLESSRQLASRFHEQFIVREDLMGLAIGTHGAN" +
//						"IQQARKVPGVTAIDLDEDTCTFHIYGEDQDAVKKARSFLEFAEDVIQVPRNLVGKVIGKN" + 
//						"GKLIQEIVDKSGVVRVRIEAENEKSVPQEEEIMPPSSLPSNNSRVGPNSSEEKKHLDTKE" +
//						"NTHFSQPNSTKVQRVLVVSSIVAGGPQKPEPKAWQGMVPFVFVGTKDSIANATVLLDYHL" +
//						"NYLKEVDQLRLERLQIDEQLRQIGASSRPPPNRTDKEKGYVTDDGQGMGRGSRPYRNRGH" +
//						"GRRGPGYTSGTNSEASNASETESDHRDELSDWSLAPTEEERESFLRRGDGRRRRGGGRGQ" +
//						"GGRGRGGGFKGNDDHSRTDNRPRNPREAKGRTADGSLQSASSEGSRLRTGKDRNQKKEKP" +
//						"DSVDGLQPLVNGVP";
//		String program = "blastp";
//		String stype = "protein";
//		String database = "uniprotkb";
//		double exp = 1e-100;
//
//		NCBIQBlastAlignmentProperties params = new NCBIQBlastAlignmentProperties ();
//		params.setBlastDatabase(database);
//		params.setSequenceType(stype);
//		params.setBlastProgram(program);
//		params.setBlastExpect(exp);

		EbiBlastClientRest a = new EbiBlastClientRest();

		//String jobid = a.sendAlignmentRequest(fasta, params);
		String jobid = "ncbiblast-R20140520-115941-0298-34624634-oy";

		System.out.println(EbiBlastClientRest.getJobStatus(jobid));
		System.out.println(a.isReady(jobid,GregorianCalendar.getInstance().getTimeInMillis()));
		//System.out.println(getJobResultType(jobid));
		//System.out.println(getJobResultType(jobid, "out"));
		//		System.out.println(getJobResultType(jobid, "xml"));
		//System.out.println(getJobResultType(jobid, "ids"));
	}

}
