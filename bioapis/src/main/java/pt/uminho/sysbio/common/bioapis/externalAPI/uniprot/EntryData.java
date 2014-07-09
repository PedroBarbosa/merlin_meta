package pt.uminho.sysbio.common.bioapis.externalAPI.uniprot;

public class EntryData {

	private String ecnumber;
	private String uniprotReviewStatus;
	private String locusTag;
	private String entryID;
	
	/**
	 * @param entryID
	 */
	public EntryData(String entryID) {
		super();
		this.entryID = entryID;
	}
	
	public String getEcnumber() {
		return ecnumber;
	}
	public void setEcnumber(String ecnumber) {
		this.ecnumber = ecnumber;
	}
	public String getUniprotReviewStatus() {
		return uniprotReviewStatus;
	}
	public void setUniprotReviewStatus(String uniprotReviewStatus) {
		this.uniprotReviewStatus = uniprotReviewStatus;
	}
	public String getLocusTag() {
		return locusTag;
	}
	public void setLocusTag(String locusTag) {
		this.locusTag = locusTag;
	}
	public String getEntryID() {
		return entryID;
	}
	public void setEntryID(String entryID) {
		this.entryID = entryID;
	}

	@Override
	public String toString() {
		return "EntryData [ecnumber=" + ecnumber + ", uniprotReviewStatus="
				+ uniprotReviewStatus + ", locusTag=" + locusTag + ", entryID="
				+ entryID + "]";
	}
	
}
