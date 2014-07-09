package pt.uminho.sysbio.common.bioapis.externalAPI;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.EntryData;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;

public class TestBioapis{
	
	@Test
	public void uni(){
		
		EntryData a = UniProtAPI.getEntryData("NP_418195");
		
		System.out.println(a);
		
		
	}

	//@Test
	public void tests() {
		   // Create UniProt query service
	    UniProtQueryService uniProtQueryService = UniProtJAPI.factory.getUniProtQueryService();
	    
	    //Create a list of accession numbers (both primary and seconday are acceptable)
	    List<String> accList = new ArrayList<String>();
	    accList.add("O60243");
	    accList.add("Q8IZP7");
	    accList.add("P02070");
	    //Isoform IDs are acceptable as well 
	    accList.add("Q4R572-1");
	    //as well as entry IDs 
	    accList.add("14310_ARATH");
	    
	    Query query = UniProtQueryBuilder.buildIDListQuery(accList);
	    
	    EntryIterator<UniProtEntry> entries = uniProtQueryService.getEntryIterator(query);
	    for (UniProtEntry entry : entries) {
	        System.out.println("entry.getUniProtId() = " + entry.getUniProtId());
	    }
	}

	////	@Test
	////	public void testBioapis() throws Exception{
	////
	////		NcbiEFetchSequenceStub_API stub = new NcbiEFetchSequenceStub_API(1000);
	////
	////		System.out.println(stub.getTaxonomy("158668216"));
	////	}
	////
	////	@Test()
	////	public void testLocus() throws Exception {
	////		
	////		System.out.println(NcbiAPI.newOrgLocusID("NP_116711.1"));
	////	}
	////	
	////	@Test
	////	public void testTaxon() throws Exception {
	////
	////		NcbiTaxonStub_API ncbi = new NcbiTaxonStub_API(1);
	////
	////		System.out.println(ncbi.getTaxonList("131567", 0));
	////		System.out.println(ncbi.getTaxonList("158668216", 0));
	////	}
	////	
	////	@Test
	////	public void testHomologyData() throws Exception {
	////
	////		Set
	////		<String> query = new 
	////		HashSet
	////		<>();
	////		query.add("50307355");
	////		query.add("6321888"); 
	////		query.add("323308728"); 
	////		query.add("323354654"); 
	////		query.add("207344643"); 
	////		query.add("365984491"); 
	////		query.add("367014367");
	////		query.add("302309608");
	////		query.add("366996168"); 
	////		query.add("156837580");
	////		query.add("367014365");
	////		query.add("365760357"); 
	////		query.add("323305437");
	////		query.add("401625444");
	////		query.add("323309661"); 
	////		query.add("363748302"); 
	////		query.add("255714647");
	////		query.add("6320552");
	////		query.add("50284865");
	////
	////		query.add("349577396"); 
	////		query.add("158668216"); 
	////		query.add("574143223"); 
	////		query.add("574143224"); 
	////		query.add("574143226"); 
	////		query.add("574143227"); 
	////		query.add("1730047"); 
	////		query.add("158668220"); 
	////		query.add("15645451");
	////
	////
	////		NcbiEFetchSequenceStub_API s = new NcbiEFetchSequenceStub_API(100);
	////		System.out.println(s.getLocusFromID(query, query.size()));
	////
	////	}
	////
	////	@Test
	////	public void testHomologyData2() throws Exception {
	////
	////		List <String> query = new ArrayList<>();
	////		query.add("50307355");
	////		query.add("6321888"); 
	////		query.add("323308728"); 
	////		query.add("323354654"); 
	////		query.add("207344643"); 
	////		query.add("365984491"); 
	////		query.add("367014367");
	////		query.add("302309608");
	////		query.add("366996168"); 
	////		query.add("156837580");
	////		query.add("367014365");
	////		query.add("365760357"); 
	////		query.add("323305437");
	////		query.add("401625444");
	////		query.add("323309661"); 
	////		query.add("363748302"); 
	////		query.add("255714647");
	////		query.add("6320552");
	////		query.add("50284865");
	////
	////		query.add("349577396"); 
	////		query.add("158668216"); 
	////		query.add("574143223"); 
	////		query.add("574143224"); 
	////		query.add("574143226"); 
	////		query.add("574143227"); 
	////		query.add("1730047"); 
	////		query.add("158668220"); 
	////		query.add("15645451");
	////
	////
	////		HomologuesData ncbiData = new HomologuesData();
	////		ncbiData.setRefSeqGI("302309608");
	////		ncbiData.getBits().put("XP_453656", 1.0);
	////		ncbiData.getBits().put("XP_444845", 1.0);
	////		ncbiData.getBits().put("XP_002553605.1", 1.0);
	////		ncbiData.getBits().put("BAO41018.1", 1.0);
	////		ncbiData.getBits().put("P53387.1", 1.0);
	////		ncbiData.getBits().put("CAGL0A01826g",1.0);
	////
	////		NcbiAPI.getNcbiData(ncbiData, query, 50, 1, true, new AtomicBoolean(false),true);
	////	}
	////
	////	@Test
	////	public void testGetDatabaseIDs() throws RemoteException {
	////
	////		List <String> query = new ArrayList <>();
	////		query.add("NP_207625");
	////		query.add("XP_453656.1");
	////		//		query.add("NC_012759.1");
	////		//		query.add("NC_010473.1");
	////		//		query.add("NC_000913.3");
	////		//		query.add("NC_017633.1");
	////		//		query.add("NC_007613.1");
	////		//		query.add("NC_020163.1");
	////		//		query.add("NC_018661.1");
	////		//		query.add("NC_018658.1");
	////		//		query.add("NC_018650.1");
	////		//		query.add("NC_013353.1");
	////		//		query.add("NC_012967.1");
	////		//		query.add("NC_012947.1");
	////		//		query.add("NC_011741.1");
	////		//		query.add("NC_011748.1");
	////		//		query.add("NC_009801.1");
	////		NcbiESearchStub_API es_stub = new NcbiESearchStub_API(50);
	////
	////		System.out.println(es_stub.getDatabaseIDs("protein", query, 100,0));
	////	}
	////
	////	@Test
	////	public void testUniprot() {
	////		List <String> query = new ArrayList <>();
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("EEU04176.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("AEP68293.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("EGA75150.1");
	////		query.add("CAY79505.1");
	////		query.add("CAY79505.1");
	////		query.add("CAY79505.1");
	////		query.add("CAY79505.1");
	////		query.add("CAY79505.1");
	////		query.add("CAY79505.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68300.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("AEP68270.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EDV09895.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EGA58845.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EDZ72370.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("EGA83051.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68277.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("AEP68276.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EDN59204.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("EHN07391.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68304.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AEP68282.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AAA34698.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68306.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68312.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("AEP68314.1");
	////		query.add("CAA27202.1");
	////		query.add("CAA27202.1");
	////		query.add("CAA27202.1");
	////		query.add("CAA27202.1");
	////		query.add("CAA27202.1");
	////		query.add("CAA27202.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("AEP68353.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EGA79053.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EHN02574.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("EJS43883.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("AAA34697.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("CDH11800.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("AAA34699.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("CDH17483.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("EHN02544.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CDF89726.1");
	////		query.add("CAY79521.1");
	////		query.add("CAY79521.1");
	////		query.add("CAY79521.1");
	////		query.add("CAY79521.1");
	////		query.add("CAY79521.1");
	////		query.add("CAY79521.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EJS43847.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("EGA78968.1");
	////		query.add("CAA27203.1");
	////		query.add("CAA27203.1");
	////		query.add("CAA27203.1");
	////		query.add("CAA27203.1");
	////		query.add("CAA27203.1");
	////		query.add("CAA27203.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("EGA86979.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003677245.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_003681444.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_002499357.1");
	////		query.add("XP_003676449.1");
	////		query.add("XP_003676449.1");
	////		query.add("XP_003676449.1");
	////		query.add("XP_003676449.1");
	////		query.add("XP_003676449.1");
	////		query.add("XP_003676449.1");
	////		query.add("CCK70052.1");
	////		query.add("CCK70052.1");
	////		query.add("CCK70052.1");
	////		query.add("CCK70052.1");
	////		query.add("CCK70052.1");
	////		query.add("CCK70052.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("AGO13413.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_004182526.1");
	////		query.add("XP_003668909.1");
	////		query.add("XP_003668909.1");
	////		query.add("XP_003668909.1");
	////		query.add("XP_003668909.1");
	////		query.add("XP_003668909.1");
	////		query.add("XP_003668909.1");
	////		query.add("CCK71806.1");
	////		query.add("CCK71806.1");
	////		query.add("CCK71806.1");
	////		query.add("CCK71806.1");
	////		query.add("CCK71806.1");
	////		query.add("CCK71806.1");
	////		query.add("XP_003959214.1");
	////		query.add("XP_003959214.1");
	////		query.add("XP_003959214.1");
	////		query.add("XP_003959214.1");
	////		query.add("XP_003959214.1");
	////		query.add("XP_003959214.1");
	////		query.add("CAA43855.1");
	////		query.add("CAA43855.1");
	////		query.add("CAA43855.1");
	////		query.add("CAA43855.1");
	////		query.add("CAA43855.1");
	////		query.add("CAA43855.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("BAO41703.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_003959496.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_004180593.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_003647226.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001643699.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_001525198.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_003672143.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_002614796.1");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_001386689.2");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("XP_006684677.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("EEQ43548.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_003686647.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_002422011.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("XP_007373806.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("EJT42730.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002555058.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_002545633.1");
	////		query.add("XP_003869587.1");
	////		query.add("XP_003869587.1");
	////		query.add("XP_003869587.1");
	////		query.add("XP_003869587.1");
	////		query.add("XP_003869587.1");
	////		query.add("XP_003869587.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("CCE42960.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EMG48741.1");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("EDK36867.2");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_001487588.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199854.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("XP_004199002.1");
	////		query.add("CCA39705.1");
	////		query.add("CCA39705.1");
	////		query.add("CCA39705.1");
	////		query.add("CCA39705.1");
	////		query.add("CCA39705.1");
	////		query.add("CCA39705.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("CCH42795.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("XP_002492685.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("CDK24636.1");
	////		query.add("XP_007739515.1");
	////		query.add("XP_007739515.1");
	////		query.add("XP_007739515.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("EHY59872.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("ETI19252.1");
	////		query.add("XP_007723625.1");
	////		query.add("XP_007723625.1");
	////		query.add("XP_007723625.1");
	////		query.add("XP_007583986.1");
	////		query.add("XP_007583986.1");
	////		query.add("XP_007583986.1");
	////		query.add("XP_007761315.1");
	////		query.add("XP_007761315.1");
	////		query.add("XP_007761315.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("EKG17827.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("XP_002144584.1");
	////		query.add("EDZ72350.1");
	////		query.add("EDZ72350.1");
	////		query.add("EDZ72350.1");
	////		query.add("EDZ72350.1");
	////		query.add("EDZ72350.1");
	////		query.add("EDZ72350.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("EYE93877.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("GAD98651.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("CDK24838.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("EJP63171.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////		query.add("XP_001597079.1");
	////
	////		Query q = UniProtQueryBuilder.buildIDListQuery(query);
	////		UniProtQueryService uniProtQueryService = UniProtJAPI.factory.getUniProtQueryService();
	////		EntryIterator<UniProtEntry> entries = uniProtQueryService.getEntryIterator(q);
	////		for (UniProtEntry entry : entries) {
	////			System.out.println(entry.getUniProtId().getValue());
	////
	////		}
	////		//		System.out.println(UniProtAPI.getUniprotEntriesFromRefSeq(queryL));
	////
	////		//		UniProtEntry entry = UniProtAPI.getEntryFromUniProtID("P13181",0);
	////		//		//System.out.println(entry.getProteinDescription().getSection().getNames().get(0).getFieldsByType(FieldType.FULL).get(0).getValue());
	////		//		System.out.println(entry.getGenes().get(0).getOrderedLocusNames().get(0).getValue());
	////		//		System.out.println(entry.getProteinDescription().getSubNames());
	////		//		System.out.println(entry.getProteinDescription().getEcNumbers());
	////		//		System.out.println(entry.getTaxonomy());
	////		//		System.out.println(entry.getOrganism().getScientificName().getValue());
	////
	////		System.out.println(UniProtAPI.getEntryData("NP_116711.*"));
	////
	////		//UniProtEntry en = getUniProtEntryID("Q9P903");
	////		//System.out.println(en.getOrganism());
	//		//System.out.println(en.getOrganism().getScientificName());
	//		//System.out.println(en.getOrganism().getCommonName());
	//		//System.out.println(en.getOrganism().getOrganismNames());
	//		//UniProtEntry entry = UniProtAPI.getEntry("SP670_1270", 0);
	//		//System.out.println(entry.getGenes().get(0).getOrderedLocusNames());
	//		//System.out.println("is starred "+UniProtAPI.isStarred("NP_414542"));
	//		//System.out.println("is starred "+UniProtAPI.isStarred("RSAG_02623"));
	//
	//		//			String f = FileUtils.read("C:\\Users\\ODias\\Desktop\\test.txt");
	//		//			String[] l = f.split("\n");
	//		//			
	//		//			for(String s : l)
	//		//				System.out.println(s+"\tis starred "+UniProtAPI.isStarred(s));
	//
	//		//System.out.println(UniProtAPI.getUniProtEntryID("158668216", 0));
	//	}

}
