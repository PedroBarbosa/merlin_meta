/**
 * 
 */
package datatypes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;

/**
 * @author ODias
 *
 */
public class ValidateData {


	/**
	 * @throws ServiceException 
	 * @throws RemoteException 
	 * 
	 */
	public ValidateData(){

	}

	/**
	 * @param ecnumbers
	 * @throws Exception 
	 */
	public void kegg_ecnumber_validation(List<String> ecnumbers) throws Exception {
		while(!ecnumbers.isEmpty())
		{
			int i=0;
			String ecnumber = "ec:"+ecnumbers.remove(i);
			i++;
			while(i<99 && !ecnumbers.isEmpty())
			{
				ecnumber+=" ec:"+ecnumbers.remove(i);
				i++;
			}
			this.verifyECNumbers_kegg(ecnumber);
			List<String> temp = new ArrayList<String>(ecnumbers);
			ecnumbers=temp;
		}
	}

	/**
	 * @param uniprot_ids
	 */
	public void validate_organisms_by_uniprot_id(List<String> uniprot_ids){
		for(String uniprot_id : uniprot_ids)
		{
			this.getOrganism_id(uniprot_id);
		}
	}
	
	/**
	 * @param uniprot_ids
	 */
	public void validate_organisms_taxonomy_by_uniprot_id(List<String> uniprot_ids){
		for(String uniprot_id : uniprot_ids)
		{
			this.get_organism_taxonomy_id(uniprot_id);
		}
	}
	
	/**
	 * @param uniprot_ids
	 */
	public void validate_organisms_by_locus_tag(List<String> loci){
		for(String locus : loci)
		{
			this.getOrganism_id_locus(locus);
		}
	}
	

	/**
	 * @param genes
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public void uniprot_genes_validation(List<String> genes) throws RemoteException {
		for(String gene:genes)
		{
			verify_genes_uniprot(gene);
		}
	}

	/**
	 * @param ecnumbers
	 * @throws Exception 
	 */
	private void verifyECNumbers_kegg(String ecnumbers) throws Exception{

		Map<String, List<String>> printer = KeggAPI.getEnzymeInfo(ecnumbers);

		for(String key: printer.keySet())
		{
			System.out.println(key+"\t"+printer.get(key)+"\r");
		}
	}

	/**
	 * @param gene
	 */
	private void verify_genes_uniprot(String gene){

		UniProtEntry entry = UniProtAPI.getEntry(gene,0);

		if(entry!=null)
		{
			System.out.println( gene+"\t"+UniProtAPI.getProteinExistence(entry)+"\t"+UniProtAPI.getECnumbers(entry)+"\t"+UniProtAPI.getOrganism(entry));
		}
		else
		{
			entry = UniProtAPI.getEntryFromUniProtID(gene,0);
			if(entry!=null)
			{
				System.out.println( gene+"\t"+UniProtAPI.getProteinExistence(entry)+"\t"+UniProtAPI.getECnumbers(entry)+"\t"+UniProtAPI.getOrganism(entry));
			}
		}

		//		    // Example2: get accession numbers of SwissProt entries with protein EC number "EC 3.1.6.-"
		//		    Query query2 = UniProtQueryBuilder.buildECNumberQuery("EC 3.1.6.-");
		//		    Query queryReviewed = UniProtQueryBuilder.setReviewedEntries(query2);
		//
		//		    AccessionIterator accs = uniProtQueryService.getAccessions(queryReviewed);
		//
		//		    System.out.println("uniProtAccessions count = " + accs.getResultSize());
	}


	/**
	 * @param uniprot_id
	 */
	private void getOrganism_id(String uniprot_id){
		UniProtEntry entry = UniProtAPI.getEntryFromUniProtID(uniprot_id,0);
		System.out.println(uniprot_id+"\t"+UniProtAPI.getLocusTag(entry)+"\t"+UniProtAPI.getOrganism(entry));
	}
	
	/**
	 * @param uniprot_id
	 */
	private void get_organism_taxonomy_id(String uniprot_id){
		UniProtEntry entry = UniProtAPI.getEntryFromUniProtID(uniprot_id,0);
		String[] taxon = UniProtAPI.getOrganismTaxa(entry);
		System.out.println(uniprot_id+"\t"+taxon[0]+"\t"+taxon[1]);
	}
	
	/**
	 * @param uniprot_id
	 */
	private void getOrganism_id_locus(String locus){
		UniProtEntry entry = UniProtAPI.getEntry(locus,0);
		System.out.println(locus+"\t"+UniProtAPI.getOrganism(entry));
	}
	
	/**
	 * @param uniprot_id
	 */
	public void get_organism_taxonomy_id_locus(String locus){
		UniProtEntry entry = UniProtAPI.getEntry(locus,0);
		String[] taxon = UniProtAPI.getOrganismTaxa(entry);
		System.out.println(locus+"\t"+taxon[0]+"\t"+taxon[1]);
	}

	/**
	 * @param args
	 * @throws ServiceException 
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws RemoteException {

		ValidateData vd = new ValidateData();
		//vd.uniprot_genes_validation(UtilsMethods.get_data_file("D:/locus.txt"));
		//vd.validate_organisms_taxonomy_by_uniprot_id(UtilsMethods.get_data_file("D:/locus.txt",true));
		//vd.validate_organisms_by_locus_tag(get_data_file("D:/locus.txt"));
		vd.uniprot_genes_validation(NcbiAPI.get_data_file("D:/locus.txt",true));

		//vd.validate_organisms_by_uniprot_id(get_data_file("D:/locus.txt"));
		
	}

}
