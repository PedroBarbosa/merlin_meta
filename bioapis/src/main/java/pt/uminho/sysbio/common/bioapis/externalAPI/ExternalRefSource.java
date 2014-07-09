package pt.uminho.sysbio.common.bioapis.externalAPI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.sysbio.common.bioapis.externalAPI.chebi.ChebiAPIInterface;
import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.datastructures.KeggReactionInformation;

public enum ExternalRefSource {

	KEGG_PATHWAY{
		@Override
		public String getURL(){
			return "http://www.genome.jp/dbget-bin/show_pathway?";
		}
		
		public String getName(){
			return "kegg pathway";
		}
		
		public String getMiriamId(){
			return "kegg.pathway";
		}
		
		public ExternalRef getReference(String id){
			return null;
		}
	},
	
	KEGG_REACTION{
		@Override
		public String getURL(){
			return "http://www.genome.jp/dbget-bin/www_bget?";
		}
		
		public String getName(){
			return "kegg reaction";
		}
		
		public String getMiriamId(){
			return "kegg.reaction";
		}
		
		public KeggReactionInformation getReference(String id) throws Exception{
			return KeggAPI.getReactionByKeggId(id);
		}
		
	},
	
	EC_Code{
		public String getURL(){
			return "";
		}
		
		public String getName(){
			return "ec-code";
		}
		
		public String getMiriamId(){
			return "ec-code";
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	},
	
	KEGG_CPD{
		@Override
		public String getURL(){
			return "http://www.genome.jp/dbget-bin/www_bget?";
		}
		
		public String getName(){
			return "kegg compound";
		}
		
		public String getMiriamId(){
			return "kegg.compound";
		}

		public MetaboliteExternalRef getReference(String id) throws Exception{
			return KeggAPI.getCompoundByKeggId(id);
		}
		
	},
	
	CHEBI{
		@Override
		public String getURL(){
			return "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=";
		}
		
		public String getName(){
			return "chebi";
		}
		
		public String getMiriamId(){
			return "obo.chebi";
		}
		
		public MetaboliteExternalRef getReference(String id) throws Exception{
			return ChebiAPIInterface.getExternalReference(id);
			
		}

	},
	
	PUBMED{
		@Override
		public String getURL(){
			return "";
		}
		
		public String getName(){
			return "pubmed";
		}
		
		public String getMiriamId(){
			return "pubmed";
		}
		
		public ExternalRef getReference(String id){
			return null;
			
		}
		
	},
	
	SGD{
		@Override
		public String getURL(){
			return "";
		}
		
		public String getName(){
			return "sgd";
		}
		
		public String getMiriamId(){
			return "sgd";
		}
		
		public ExternalRef getReference(String id){
			return null;
		}
		
	},
	
	ECO{
		@Override
		public String getURL(){
			return "";
		}
		
		public String getName(){
			return "evidence code";
		}
		
		public String getMiriamId(){
			return "ECO";
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	},
	
	UNIPROT{
		@Override
		public String getURL(){
			return "";
		}
		
		public String getName(){
			return "uniprot";
		}
		
		public String getMiriamId(){
			return "uniprot";
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		

	}, KEGG_ORTHOLOGY {
		
		@Override
		public String getURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}, BRENDA {
		@Override
		public String getURL() {
			return "http://www.brenda-enzymes.org/";
		}

		@Override
		public String getName() {
			return "BRENDA";
		}

		@Override
		String getMiriamId() {
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			return null;
		}
	}, CAS {
		@Override
		public String getURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return "CAS";
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}, PUBCHEM {
		@Override
		public String getURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getName() {
			return "pubchem";
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}, METACYC_CPD {
		@Override
		public String getURL() {
			return "http://metacyc.org/META/NEW-IMAGE?object=";
		}

		@Override
		public String getName() {
			return "metacyc compound";
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			return new ExternalRef(id, null, this);
		}
	}, SEED_CPD {
		@Override
		public String getURL() {
			return "http://seed-viewer.theseed.org/seedviewer.cgi?page=CompoundViewer&compound=";
		}

		@Override
		public String getName() {
			return "seed compound";
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			return new ExternalRef(id, null, this);
		}
	}, MODEL {
		@Override
		public String getURL() {
			return null;
		}

		@Override
		public String getName() {
			return "model";
		}

		@Override
		String getMiriamId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		IExternalRef getReference(String id) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	} ;

		
	abstract public String getURL();
	abstract public String getName();
	abstract String getMiriamId();
	abstract IExternalRef getReference(String id) throws Exception;
		
	
	public String getSourceId(String miriamCode){
		
		miriamCode = miriamCode.replaceAll("%3A", ":");
		String regExpString = "urn:miriam:"+getMiriamId()+":(.+)";
		
		Pattern pattern = Pattern.compile(regExpString);
		Matcher matcher = pattern.matcher(miriamCode);

		String id = null;
		if(matcher.find())
			id = matcher.group(1);
		return id;
	}
	
	public String getMiriamCode(String sourceId){
		if(this.getMiriamId() == null)
			return null;
		
		String miriamCode = "urn:miriam:" + getMiriamId() + ":" + sourceId;
		return miriamCode;
	}
	
	public String getUrlLink(String sourceId){
		return this.getURL() + sourceId;
	}

}
