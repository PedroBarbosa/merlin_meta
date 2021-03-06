/**
 * 
 */
package pt.uminho.sysbio.merlin.gpr.rules.core;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiTaxonStub_API;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.common.local.alignments.core.PairwiseSequenceAlignement;
import pt.uminho.sysbio.common.local.alignments.core.Run_Similarity_Search;
import pt.uminho.sysbio.common.local.alignments.core.Run_Similarity_Search.Method;
import pt.uminho.sysbio.common.utilities.datastructures.map.MapUtils;
import pt.uminho.sysbio.common.utilities.datastructures.pair.Pair;
import pt.uminho.sysbio.merlin.gpr.rules.core.input.GeneAssociation;
import pt.uminho.sysbio.merlin.gpr.rules.core.input.ModuleCI;
import pt.uminho.sysbio.merlin.gpr.rules.core.input.ReactionProteinGeneAssociation;
import pt.uminho.sysbio.merlin.gpr.rules.core.output.ProteinsGPR_CI;
import pt.uminho.sysbio.merlin.gpr.rules.core.output.ReactionsGPR_CI;
import pt.uminho.sysbio.merlin.utilities.DatabaseProgressStatus;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;

import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;

/**
 * @author ODias
 *
 */
public class IdentifyGenomeSubunits {

	private Map<String, List<String>> ec_numbers;
	private Map<String, ProteinSequence> genome;
	private long reference_organism_id;
	private ConcurrentHashMap<String, ProteinSequence> sequences;
	private ConcurrentHashMap<String, Set<String>> closestOrtholog;
	private MySQLMultiThread msqlmt;
	private double similarity_threshold;
	private Method method;
	private AtomicBoolean cancel;
	private double referenceTaxonomyThreshold;
	private boolean compareToFullGenome;
	private static Logger logger;
	private TimeLeftProgress progress;


	/**
	 * @param msqlmt
	 */
	public IdentifyGenomeSubunits(MySQLMultiThread msqlmt) {

		this.msqlmt = msqlmt;
	}

	/**
	 * @param ec_numbers
	 * @param genome
	 * @param reference_organism_id
	 * @param msqlmt
	 * @param similarity_threshold
	 * @param referenceTaxonomyThreshold
	 * @param method
	 * @param cancel
	 * @param compareToFullGenome
	 */
	public IdentifyGenomeSubunits(Map<String, List<String>> ec_numbers, Map<String, ProteinSequence> genome, long reference_organism_id, 
			MySQLMultiThread msqlmt, double similarity_threshold, double referenceTaxonomyThreshold, Method method, 
			AtomicBoolean cancel, boolean compareToFullGenome) {

		this.ec_numbers = ec_numbers;
		this.genome = genome;
		this.reference_organism_id = reference_organism_id;
		this.msqlmt = msqlmt;
		this.similarity_threshold = similarity_threshold;
		this.method = method;
		this.cancel = cancel;
		this.referenceTaxonomyThreshold = referenceTaxonomyThreshold;
		this.compareToFullGenome = compareToFullGenome;
	}

	/**
	 * @throws Exception
	 */
	public boolean runIdentification() throws Exception {

		boolean ret = true;

		try {

			this.sequences = new ConcurrentHashMap<>();
			this.closestOrtholog = new ConcurrentHashMap<>();

			List<String> referenceTaxonomy = this.getReferenceTaxonomy(reference_organism_id);
			System.out.println("Reference taxonomy set to "+ referenceTaxonomy);

			ConcurrentHashMap<String, Integer> ncbi_taxonomy_ids = new ConcurrentHashMap<>();
			ConcurrentHashMap<String, Integer> kegg_taxonomy_scores = new ConcurrentHashMap<>();
			ConcurrentHashMap<String, Map<String, List<String>>> orthologsSequences = new ConcurrentHashMap<>();;

			kegg_taxonomy_scores.put("noOrg", 0);
			Map<String, String> kegg_taxonomy_ids = this.getKeggTaxonomyIDs();

			Connection conn = this.msqlmt.openConnection();

			Set<String> bypass = IdentifyGenomeSubunits.getECNumbersWithModules(conn);

			long startTime = GregorianCalendar.getInstance().getTimeInMillis();
			List<String> iterator = new ArrayList<>(this.ec_numbers.keySet());

			for(int i = 0; i<iterator.size(); i++) {

				String ec_number = iterator.get(i);

				//double size = counter/this.ec_numbers.keySet().size();
				//System.out.println("\t"+ec_number+"\t"+size*100+"%\t"+counter);

				if(!hasLetters(ec_number) && !bypass.contains(ec_number)  && !this.cancel.get()) {

					try {

						System.out.print("Retrieving GPR for "+ec_number+"...\t");
						AssembleGPR gpr = new AssembleGPR(ec_number);

						AssembleGPR.setLogger(logger);
						Map<String,List<ReactionProteinGeneAssociation>> result = gpr.run();
						System.out.println("Retrieved!");

						Map<String, Set<String>> genes_ko_modules = this.loadModule(conn, result);
						System.out.println("Genes, KO, modules\t"+genes_ko_modules);

						ConcurrentHashMap<String, ProteinSequence> orthologs = new ConcurrentHashMap<>();
						boolean noOrthologs = true;

						for(String ko : genes_ko_modules.keySet()) {

							List<String> locusTags = PairwiseSequenceAlignement.checkDatabase(conn, ko);

							if(locusTags.isEmpty()) {

								GetClosestOrhologSequence seq = new GetClosestOrhologSequence(ko, referenceTaxonomy, this.sequences, kegg_taxonomy_ids,
										ncbi_taxonomy_ids, kegg_taxonomy_scores, this.closestOrtholog, orthologsSequences );
								seq.run();

								for(String gene : this.closestOrtholog.get(ko))
									orthologs.put(gene, this.sequences.get(gene));

								//if(orthologs.isEmpty()) {
								//
								//	String dummyGene = "noOrg:noOrtholog"+"";  
								//	Set<String> set = new HashSet<>();
								//	set.add(dummyGene);
								//	this.closestOrtholog.put(ko, set);
								//	orthologs.put(dummyGene, new ProteinSequence(""));
								//
								//}
							}
							else {

								Map<String, Set<String>> temp = this.getOrthologs(ko);

								for(String key : temp.keySet()) {

									noOrthologs = false;

									for(String locus :locusTags) {

										String[] similarityData = new String[4];

										similarityData[0]= key;
										similarityData[1]= locus;
										similarityData[2]= null;
										similarityData[3]= null;

										PairwiseSequenceAlignement.loadOrthologsData(similarityData, conn, ec_number, temp, genes_ko_modules);
									}
								}
							}
						}
						System.out.println("Orthologs to be searched in genome:\t"+orthologs.keySet());

						if(orthologs.size()>0 && !this.cancel.get()) {

							Run_Similarity_Search search = new Run_Similarity_Search(this.msqlmt, this.genome, this.similarity_threshold, 
									this.method, orthologs, this.cancel, new AtomicInteger(0), new AtomicInteger(this.ec_numbers.size()), -1);
							search.setEc_number(ec_number);
							search.setModules(genes_ko_modules);
							search.setClosestOrthologs(MapUtils.revertMapFromSet(this.closestOrtholog));
							search.setReferenceTaxonomyScore(referenceTaxonomy.size());
							search.setKegg_taxonomy_scores(kegg_taxonomy_scores);
							search.setAnnotatedGenes(this.ec_numbers.get(ec_number));
							search.setReferenceTaxonomyThreshold(this.referenceTaxonomyThreshold);
							search.setCompareToFullGenome(this.compareToFullGenome);
							search.run_OrthologsSearch();

							Map<String, Set<String>> modules = MapUtils.revertMapFromSet(genes_ko_modules);

							for(String module : modules.keySet()) {

								int module_id = Integer.parseInt(module);

								if(search.getSequencesWithoutSimilarities().containsAll(modules.get(module)))
									IdentifyGenomeSubunits.updateECNumberNote(conn, ec_number, module_id, "no_similarities");
							}
						}
						else if(noOrthologs) {

							IdentifyGenomeSubunits.updateECNumberNote(conn, ec_number, -1, null);
						}
					}
					catch (Exception e) {

						IdentifyGenomeSubunits.updateECNumberStatus(conn, ec_number, DatabaseProgressStatus.PROCESSING);
						e.printStackTrace();
						ret = false;
					}
				}
				IdentifyGenomeSubunits.setSubunitProcessed(conn, ec_number);

				if(cancel.get())
					i = iterator.size();

				if(this.progress!=null)
					progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - startTime, i, iterator.size());
			}
			this.msqlmt.closeConnection(conn);
		} 
		catch (Exception e) {

			ret = false;
			e.printStackTrace();
			IdentifyGenomeSubunits.logger.log(Level.SEVERE, e.getMessage(), e);
			throw e;
		}
		return ret;
	}

	/**
	 * @param ko
	 * @return
	 * @throws SQLException
	 */
	private Map<String, Set<String>> getOrthologs(String ko) throws SQLException {

		Map<String, Set<String>> ret = new HashMap<>();
		Set<String> ret_set = new HashSet<>();

		ret_set.add(ko);

		Connection conn = this.msqlmt.openConnection();
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT locus_id FROM orthology where entry_id = '"+ko+"'");

		while(rs.next()) {

			if(rs.getString(1)!=null)
				ret.put(" :"+rs.getString(1), ret_set);
		}

		this.msqlmt.closeConnection(conn);
		return ret;
	}


	/**
	 * @param threshold 
	 * @throws SQLException
	 */
	public Map<String, ReactionsGPR_CI> runAssignment(double threshold) throws SQLException {

		Connection conn = this.msqlmt.openConnection();
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT DISTINCT reaction, enzyme_ecnumber, definition, idgene, " +
				" orthology.entry_id, locusTag, gene.name, note, similarity " +
				" FROM module" +
				" INNER JOIN subunit ON (subunit.module_id = module.id)" +
				" INNER JOIN module_has_orthology ON (module_has_orthology.module_id = module.id)"+
				" INNER JOIN orthology ON (module_has_orthology.orthology_id = orthology.id)"+
				" INNER JOIN gene_has_orthology ON (gene_has_orthology.orthology_id = module_has_orthology.orthology_id AND gene_has_orthology.gene_idgene = subunit.gene_idgene)" +
				" INNER JOIN gene ON (gene_has_orthology.gene_idgene = gene.idgene)" 
				//+" WHERE similarity >= "+threshold				
				);

		Map<String, ReactionsGPR_CI> rpgs = new HashMap<>();

		while (rs.next()) {

			if(rs.getString("note")==null || !rs.getString("note").equalsIgnoreCase("unannotated") || (rs.getString("note").equalsIgnoreCase("unannotated") && rs.getDouble("similarity")>=threshold)) {

				ReactionsGPR_CI rpg = new ReactionsGPR_CI(rs.getString(1));

				if(rpgs.containsKey(rs.getString(1)))
					rpg  = rpgs.get(rs.getString(1));

				{
					ProteinsGPR_CI pga = new ProteinsGPR_CI(rs.getString(2), rs.getString(3));
					pga.addSubunit(rs.getString(3).split(" OR "));

					if(rpg.getProteins()!= null && rpg.getProteins().containsKey(rs.getString(2)))
						pga = rpg.getProteins().get(rs.getString(2));

					String gene_name = rs.getString(6);

					if(rs.getString(7)!=null && !rs.getString(7).isEmpty() && !rs.getString(7).equalsIgnoreCase("null"))
						gene_name = rs.getString(7)+"_"+gene_name;

					pga.addLocusTag(rs.getString(5), gene_name);

					rpg.addProteinGPR_CI(pga);
				}

				rpgs.put(rpg.getReaction(), rpg);
			}
		}

		this.msqlmt.closeConnection(conn);
		return rpgs;
	}

	/**
	 * @param s
	 * @return
	 */
	public static boolean hasLetters(String s) {

		if (s == null) return false;
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetter(c)) return true;
		}
		return false;
	}

	/**
	 * @param conn
	 * @param result
	 * @throws SQLException
	 */
	private Map<String, Set<String>> loadModule(Connection conn, Map<String, List<ReactionProteinGeneAssociation>> result) throws SQLException {

		Map<String, Set<String>> genes_ko_modules = new HashMap<>();

		Statement stmt = conn.createStatement();

		for(String reaction: result.keySet()) {

			for(int i=0; i<result.get(reaction).size(); i++) {

				for(String p : result.get(reaction).get(i).getProteinGeneAssociation().keySet()) {

					List<GeneAssociation> genes_list = result.get(reaction).get(i).getProteinGeneAssociation().get(p).getGenes();

					String definition = "";

					for(int index_list = 0; index_list< genes_list.size(); index_list++) {

						GeneAssociation g = genes_list.get(index_list);

						if(index_list!=0)
							definition += " OR ";

						for(int index = 0; index< g.getGenes().size(); index++) {

							String gene  = g.getGenes().get(index);

							if(index!=0)
								definition += " AND ";  

							definition += gene;
						}
					}

					for(GeneAssociation g : genes_list) {

						for(ModuleCI mic : g.getModules().values()) {

							ResultSet rs = stmt.executeQuery("SELECT id, definition FROM module WHERE entry_id='"+mic.getModule()+"' AND reaction='"+reaction+"' AND definition ='"+definition+"'");

							if(!rs.next()) {

								stmt.execute("INSERT INTO module (reaction, entry_id, name, definition, type) " +
										"VALUES ('"+reaction+"', '"+mic.getModule()+"', '"+mic.getName()+"', '"+definition+"', '"+mic.getModuleType().toString()+"')");
								rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
								rs.next();
							}

							String idModule = rs.getString(1);

							for(String gene : g.getGenes()) {

								rs = stmt.executeQuery("SELECT * FROM orthology WHERE entry_id='"+gene+"'");

								boolean noEntry = true;
								Set<Integer> ids = new HashSet<>();

								while(rs.next()) {

									noEntry = false;
									ids.add(rs.getInt(1));
								}

								if(noEntry) { 

									stmt.execute("INSERT INTO orthology (entry_id) VALUES('"+gene+"')");
									rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
									rs.next();
									ids.add(rs.getInt(1));
								}

								for (int idGene : ids) {

									rs = stmt.executeQuery("SELECT * FROM module_has_orthology WHERE module_id="+idModule+" AND orthology_id = "+idGene+"");

									if(!rs.next()) {

										stmt.execute("INSERT INTO module_has_orthology (module_id, orthology_id) VALUES('"+idModule+"', '"+idGene+"')");
										rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
										rs.next();
									}

									Set<String> modules = new HashSet<>();

									if(genes_ko_modules.containsKey(gene))
										modules = genes_ko_modules.get(gene);

									modules.add(idModule);
									genes_ko_modules.put(gene, modules);
								}
							}
							rs.close();
						}
					}
				}
			}
		}

		stmt.close();
		stmt=null;
		return genes_ko_modules;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private Map<String, String> getKeggTaxonomyIDs() throws Exception {

		Map<String, String> kegg_taxonomy_ids = new HashMap<>();
		List<String[]> organisms = KeggAPI.getGenomes();
		for(String[] org : organisms) {

			kegg_taxonomy_ids.put(org[0], org[1]);
		}

		return kegg_taxonomy_ids;
	}

	/**
	 * @param tax_id
	 * @return
	 * @throws Exception 
	 */
	private List<String> getReferenceTaxonomy(long tax_id_long) throws Exception {

		String tax_id = tax_id_long+"";

		List<String> referenceTaxonomy = new ArrayList<>();

		NcbiTaxonStub_API ncsa = new NcbiTaxonStub_API(10);
		Map<String,String[]> ncbi_ids= ncsa.getTaxonList(tax_id, 0);

		String[] taxonomy = ncbi_ids.get(tax_id)[1].split(";");

		for(String t : taxonomy) {

			referenceTaxonomy.add(t.trim());
		}

		referenceTaxonomy.add(ncbi_ids.get(tax_id)[0].trim());

		return referenceTaxonomy;
	}

	/**
	 * @param msqlmt
	 * @return
	 * @throws SQLException 
	 */
	private static Set<String> getECNumbersWithModules(Connection conn) throws SQLException {

		Set<String> ec_numbers = new HashSet<>();

		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT DISTINCT(enzyme_ecnumber) FROM subunit WHERE gpr_status = '"+DatabaseProgressStatus.PROCESSED+"'");

		while(rs.next()) {

			ec_numbers.add(rs.getString(1));
		}
		rs.close();
		stmt.close();

		return ec_numbers;
	}

	/**
	 * @param conn
	 * @param ec_number
	 * @throws SQLException 
	 */
	private static void setSubunitProcessed(Connection conn, String ec_number) throws SQLException {

		IdentifyGenomeSubunits.updateECNumberStatus(conn, ec_number,DatabaseProgressStatus.PROCESSED);

	}


	/**
	 * @param conn
	 * @param ec_number
	 * @param module_id
	 * @param note
	 * @throws SQLException
	 */
	private static void updateECNumberNote(Connection conn, String ec_number, int module_id, String note) throws SQLException {

		Statement stmt = conn.createStatement();

		String string = "";
		boolean update = true, notExists = true;

		if(module_id > 0 ) {

			string = ",module_id ="+module_id;

			ResultSet rs = stmt.executeQuery("SELECT module_id FROM subunit WHERE enzyme_ecnumber = '"+ec_number+"'");

			while (rs.next()) {

				if(rs.getInt(1)>0) {

					update=false;

					if(rs.getInt(1)==module_id)
						notExists = false;

				}
			}
		}

		if(update)
			stmt.execute("UPDATE subunit SET note = '"+note+"'" +string +" WHERE enzyme_ecnumber='"+ec_number+"'");
		else
			if(notExists) {

				ResultSet rs = stmt.executeQuery("SELECT DISTINCT gene_idgene, enzyme_protein_idprotein FROM subunit WHERE enzyme_ecnumber = '"+ec_number+"'");

				Set<Pair<String,String>> genes_proteins = new HashSet<Pair<String,String>>();

				while (rs.next()) {

					Pair<String,String> pair = new Pair<>(rs.getString(1), rs.getString(2));

					genes_proteins.add(pair);
				}

				for(Pair<String,String> pair : genes_proteins) {

					stmt.execute("INSERT INTO subunit (gene_idgene, enzyme_protein_idprotein, enzyme_ecnumber, note, module_id) VALUES(" + pair.getA() + ", "+pair.getB() + ", '"+ec_number+"', '"+note+"'," +module_id+")");

				}
			}

		stmt.close();
	}
	
	/**
	 * @param conn
	 * @param ec_number
	 * @throws SQLException
	 */
	private static void updateECNumberStatus(Connection conn, String ec_number, DatabaseProgressStatus status) throws SQLException {

		Statement stmt = conn.createStatement();

			stmt.execute("UPDATE subunit SET gpr_status = '"+status+"' WHERE enzyme_ecnumber='"+ec_number+"'");

		stmt.close();
	}

	/**
	 * @param msqlmt
	 * @param originalReactions 
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, List<String>> getECNumbers(MySQLMultiThread msqlmt) throws SQLException {

		Map<String, List<String>> ec_numbers = new HashMap<>();

		Connection conn = msqlmt.openConnection();
		Statement stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT locusTag, enzyme_ecnumber FROM subunit " +
				"INNER JOIN gene ON (gene.idgene = gene_idgene)"
				);

		while(rs.next()) {

			List<String> genes = new ArrayList<>();

			String gene = rs.getString(1);
			String enzyme = rs.getString(2);

			if(ec_numbers.containsKey(enzyme))
				genes = ec_numbers.get(enzyme);

			genes.add(gene);

			ec_numbers.put(enzyme, genes);

		}
		rs.close();
		stmt.close();
		msqlmt.closeConnection(conn);

		return ec_numbers;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		long reference_organism_id = Long.parseLong(args[0]);
		double similarity_threshold = Double.parseDouble(args[1]);
		double referenceTaxonomyThreshold = Double.parseDouble(args[2]); 
		String user = args[3];
		String password = args[4];
		String server = args[5]; 
		int port = Integer.parseInt(args[6]);
		String database = args[7];
		String file = args[8];
		boolean isNCBI = new Boolean(args[9]);
		boolean compareToFullGenome = Boolean.parseBoolean(args[10]);
		boolean identifyOrAssign = Boolean.parseBoolean(args[11]);
		boolean integrateToDatabase = Boolean.parseBoolean(args[12]);
		boolean keepReactionsWithNotes = Boolean.parseBoolean(args[13]);
		double threshold = Double.parseDouble(args[14]);
		boolean keepManualReactions = true;

		boolean originalReaction = false;

		MySQLMultiThread msqlmt = new MySQLMultiThread(user, password, server, port, database);

		if(identifyOrAssign) {

			AtomicBoolean cancel = new AtomicBoolean(false);
			Method method = Method.SmithWaterman;

			Map<String, List<String>> ec_numbers = IdentifyGenomeSubunits.getECNumbers(msqlmt);
			System.out.println("Enzymes size:\t"+ec_numbers.keySet().size());

			Map<String, ProteinSequence> genome =  FastaReaderHelper.readFastaProteinSequence(new File(file));

			Map<String, ProteinSequence> newGenome = genome;

			if(isNCBI)
				newGenome = NcbiAPI.getNCBILocusTags(genome);

			System.out.println("Genome size:\t"+newGenome.keySet().size());

			IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(ec_numbers, newGenome, reference_organism_id, msqlmt, similarity_threshold, 
					referenceTaxonomyThreshold, method, cancel, compareToFullGenome);
			i.runIdentification();

		}
		else {

			IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(msqlmt);

			FilterModelReactions f = new FilterModelReactions(msqlmt, originalReaction);
			f.filterReactions(i.runAssignment(threshold));

			if(integrateToDatabase) {

				f.removeReactionsFromModel(keepReactionsWithNotes, keepManualReactions);
				f.setModelGPRsFromTool();
			}
		}

	}

	/**
	 * @param progress
	 */
	public void setProgress(TimeLeftProgress progress) {

		this.progress = progress;
	}

	public static void setLogger(Logger logger) {

		IdentifyGenomeSubunits.logger = logger;
	}

}
