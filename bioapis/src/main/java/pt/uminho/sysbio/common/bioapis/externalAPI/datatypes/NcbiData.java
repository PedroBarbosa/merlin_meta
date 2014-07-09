package pt.uminho.sysbio.common.bioapis.externalAPI.datatypes;

import java.util.concurrent.ConcurrentHashMap;

import org.biojava3.core.sequence.ProteinSequence;

public class NcbiData {

	private ConcurrentHashMap<String, String> locus_Tag;
	private ConcurrentHashMap<String, ProteinSequence> sequences;

	/**
	 * @param locus_Tag
	 * @param sequences
	 */
	public NcbiData(ConcurrentHashMap<String, String> locus_Tag, ConcurrentHashMap<String, ProteinSequence> sequences) {

		this.locus_Tag = locus_Tag;
		this.sequences = sequences;
	}
	
	/**
	 * @param name
	 * @param locus
	 */
	public void addLocusTag(String name, String locus){

		this.locus_Tag.put(name, locus);
	}

	/**
	 * @param name
	 * @param sequence
	 */
	public void addSequence(String name, ProteinSequence sequence){

		this.sequences.put(name, sequence);
	}
}
