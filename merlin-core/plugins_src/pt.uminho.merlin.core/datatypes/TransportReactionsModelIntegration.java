/**
 * 
 */
package datatypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.sysbio.common.bioapis.externalAPI.ExternalRefSource;
import pt.uminho.sysbio.common.transporters.core.transport.CompartmentaliseTransportContainer;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;

/**
 * @author ODias
 *
 */
@Operation(name="Proxy",description="Integrate transport Reactions into Model.")
public class TransportReactionsModelIntegration {

	private List<String> modelMetabolitesNamesAndSynonyms;
	private List<String> modelMetabolitesKEGG_ID;
	private Map<String, String> keggIDsToModelNames;
	private CompartmentaliseTransportContainer compartmentaliseTransportContainer;
	private Set<String> acceptedReactions;

	/**
	 * 
	 */
	public TransportReactionsModelIntegration(){
		this.setModelMetabolitesNamesAndSynonyms(new ArrayList<String>());
		this.setModelMetabolites(new ArrayList<String>());
		this.setAcceptedReactions(new HashSet<String>());
		this.setKeggIDsToModelNames(new HashMap<String, String>());
	}

	/**
	 * @param modelMetabolites
	 * @param compartmentaliseTransportContainer
	 */
	public TransportReactionsModelIntegration(List<String> modelMetabolitesKEGGID, CompartmentaliseTransportContainer compartmentaliseTransportContainer) {
		this.setModelMetabolites(modelMetabolitesKEGGID);
		this.setCompartmentaliseTransportContainer(compartmentaliseTransportContainer);
		this.setAcceptedReactions(new HashSet<String>());
	}


	/**
	 * @param useInternalReactions
	 * @param keggMetabolitesOnly
	 */
	public void selectReactionToIntegrateInModel(boolean useInternalReactions, boolean keggMetabolitesOnly) {
		
		if(this.getCompartmentaliseTransportContainer().isLoaded()) {
			
			for(String reaction:this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().keySet()) {
				
				boolean insertReaction=true;
				
				if(this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).isAllMetabolitesHaveKEGGId()) {
					
					for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().keySet()) {
						
						if(keggMetabolitesOnly) {
							
							if(!this.modelMetabolitesKEGG_ID.contains(ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID)))) {
								
								insertReaction=false;
							}
						}
						else {
							
							if(!this.getModelMetabolitesNamesAndSynonyms().contains(this.getCompartmentaliseTransportContainer().getTransportContainer().getMetabolites().get(metaboliteID).getName()) && 
									!this.modelMetabolitesKEGG_ID.contains(ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID))))
							{
								insertReaction=false;
							}
						}

						if(!useInternalReactions) {
							
							String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getCompartmentId(); 
							if(compartment.equalsIgnoreCase("E.R.") || compartment.equalsIgnoreCase("CYSK") || compartment.equalsIgnoreCase("GOLG") ||compartment.equalsIgnoreCase("MITO")
									|| compartment.equalsIgnoreCase("NUCL") || compartment.equalsIgnoreCase("PERO") || compartment.equalsIgnoreCase("PLAS") ||compartment.equalsIgnoreCase("VACO")
									|| compartment.equalsIgnoreCase("VES"))
							{
								insertReaction=false;
							}
						}
					}

					for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().keySet()) {

						if(keggMetabolitesOnly) {
							
							if(!this.modelMetabolitesKEGG_ID.contains(ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID)))) {
								
								insertReaction=false;
							}
						}
						else {
							
							if(!this.getModelMetabolitesNamesAndSynonyms().contains(this.getCompartmentaliseTransportContainer().getTransportContainer().getMetabolites().get(metaboliteID).getName()) && 
									!this.modelMetabolitesKEGG_ID.contains(ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID))))
							{
								insertReaction=false;
							}
						}
						
						if(!useInternalReactions) {
							
							String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getCompartmentId(); 
							if(compartment.equalsIgnoreCase("E.R.") || compartment.equalsIgnoreCase("CYSK") || compartment.equalsIgnoreCase("GOLG") ||compartment.equalsIgnoreCase("MITO")
									|| compartment.equalsIgnoreCase("NUCL") || compartment.equalsIgnoreCase("PERO") || compartment.equalsIgnoreCase("PLAS") ||compartment.equalsIgnoreCase("VACO")
									|| compartment.equalsIgnoreCase("VES"))
							{
								insertReaction=false;
							}
						}
					}
				}
				else {
					
					insertReaction=false;
				}

				if(insertReaction) {
					
					this.acceptedReactions.add(reaction);
				}
			}
		}
	}

	/**
	 * @return the modelMetabolites
	 */
	public List<String> getModelMetabolites() {
		
		return modelMetabolitesKEGG_ID;
	}

	/**
	 * @param modelMetabolites the modelMetabolites to set
	 */
	public void setModelMetabolites(List<String> modelMetabolitesKEGGID) {
		
		this.modelMetabolitesKEGG_ID = modelMetabolitesKEGGID;
	}

	/**
	 * @return the compartmentaliseTransportContainer
	 */
	public CompartmentaliseTransportContainer getCompartmentaliseTransportContainer() {
		
		return compartmentaliseTransportContainer;
	}

	/**
	 * @param compartmentaliseTransportContainer the compartmentaliseTransportContainer to set
	 */
	public void setCompartmentaliseTransportContainer(
			CompartmentaliseTransportContainer compartmentaliseTransportContainer) {
		
		this.compartmentaliseTransportContainer = compartmentaliseTransportContainer;
	}

	/**
	 * @return the acceptedReactions
	 */
	public Set<String> getAcceptedReactions() {
		
		return acceptedReactions;
	}

	/**
	 * @param acceptedReactions the acceptedReactions to set
	 */
	public void setAcceptedReactions(Set<String> acceptedReactions) {
		
		this.acceptedReactions = acceptedReactions;
	}

	/**
	 * @param path
	 * @throws IOException
	 */
	public void setListFromFile(String path) throws IOException {
		
		FileInputStream fstream = new FileInputStream(path);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		
		while ((strLine = br.readLine()) != null) {
			
			String[] read = strLine.split("\t");
			this.modelMetabolitesNamesAndSynonyms.add(read[0]);
			
			if(read[1]!=null && !read[1].equalsIgnoreCase("null")) {
				
				this.modelMetabolitesKEGG_ID.add(ExternalRefSource.KEGG_CPD.getSourceId(read[1]));
				this.modelMetabolitesNamesAndSynonyms.add(read[3]);
				this.keggIDsToModelNames.put(read[1],read[0]);
			}
			
			if(read[4]!=null && !read[1].equalsIgnoreCase("null")) {
				
				this.modelMetabolitesNamesAndSynonyms.add(read[4]);
			}
		}
		br.close();
		in.close();
		fstream.close();
	}

	/**
	 * @param path
	 * @throws IOException
	 */
	public void getListToFile(String outPath) throws IOException {
		
		FileWriter fWriterStream = new FileWriter(outPath);  
		BufferedWriter out = new BufferedWriter(fWriterStream);

		out.write("number of reactions\t"+this.acceptedReactions.size()+"\n");
		out.write("\n");

		for(String reaction:this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().keySet()) {
			
			if(this.acceptedReactions.contains(reaction)) {
				
				StringBuffer buff = new StringBuffer();
				buff.append(reaction+"\n");
				buff.append(this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getGenesIDs()+"\n");
				boolean addplus=false;
				for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().keySet()) {
					
					String metabolite = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getMetaboliteId();
					String metabolite_name = this.getCompartmentaliseTransportContainer().getTransportContainer().getMetabolites().get(metabolite).getName();
					if(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID)!=null) {
						
						metabolite_name = this.keggIDsToModelNames.get(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID));
					}
					String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getCompartmentId();
					double stoi = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getStoichiometryValue();
					
					if(addplus) 
					{
						buff.append("\t+\t");
					}
					else {
						
						addplus=true;
					}
					buff.append(stoi+" "+metabolite_name+" ("+compartment+") ");
				}

				String rev = "=>";
				if(this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReversible())
				{
					rev="<=>";
				}

				buff.append(" "+rev+" ");
				addplus=false;
				for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().keySet())
				{
					String metabolite = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getMetaboliteId();
					String metabolite_name = this.getCompartmentaliseTransportContainer().getTransportContainer().getMetabolites().get(metabolite).getName();
					if(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID)!=null)
					{
						metabolite_name = this.keggIDsToModelNames.get(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID));
					}
					String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getCompartmentId();
					double stoi = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getStoichiometryValue();
					if(addplus)
					{
						buff.append("\t+\t");
					}
					else
					{
						addplus=true;
					}
					buff.append(stoi+" "+metabolite_name+" ("+compartment+") ");
				}
				buff.append("\n");
				//				addplus=false;
				//				for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().keySet())
				//				{
				//					//String metabolite = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getMetaboliteId();
				//					String metabolite_name = ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID));
				//					String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getCompartmentId();
				//					double stoi = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReactants().get(metaboliteID).getStoichiometryValue();
				//					if(addplus)
				//					{
				//						buff.append("\t+\t");
				//					}
				//					else
				//					{
				//						addplus=true;
				//					}
				//					buff.append(stoi+" "+metabolite_name+" ("+compartment+") ");
				//				}
				//				
				//				rev = "=>";
				//				if(this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getReversible())
				//				{
				//					rev="<=>";
				//				}
				//				
				//				buff.append(" "+rev+" ");
				//				addplus=false;
				//				for(String metaboliteID : this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().keySet())
				//				{
				//					//String metabolite = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getMetaboliteId();
				//					String metabolite_name = ExternalRefSource.KEGG_CPD.getSourceId(this.getCompartmentaliseTransportContainer().getTransportContainer().getKeggMiriam().get(metaboliteID));
				//					String compartment = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getCompartmentId();
				//					double stoi = this.getCompartmentaliseTransportContainer().getTransportContainer().getReactions().get(reaction).getProducts().get(metaboliteID).getStoichiometryValue();
				//					if(addplus)
				//					{
				//						buff.append("\t+\t");
				//					}
				//					else
				//					{
				//						addplus=true;
				//					}
				//					buff.append(stoi+" "+metabolite_name+" ("+compartment+") ");
				//				}
				buff.append("\n\n");
				out.append(buff);
			}
		}
		out.close();
	}

	/**
	 * @return the modelMetabolitesNamesAndSynonyms
	 */
	public List<String> getModelMetabolitesNamesAndSynonyms() {
		return modelMetabolitesNamesAndSynonyms;
	}

	/**
	 * @param modelMetabolitesNamesAndSynonyms the modelMetabolitesNamesAndSynonyms to set
	 */
	public void setModelMetabolitesNamesAndSynonyms(
			List<String> modelMetabolitesNamesAndSynonyms) {
		this.modelMetabolitesNamesAndSynonyms = modelMetabolitesNamesAndSynonyms;
	}

	/**
	 * @return the keggIDsToModelNames
	 */
	public Map<String, String> getKeggIDsToModelNames() {
		return keggIDsToModelNames;
	}

	/**
	 * @param keggIDsToModelNames the keggIDsToModelNames to set
	 */
	public void setKeggIDsToModelNames(Map<String, String> keggIDsToModelNames) {
		this.keggIDsToModelNames = keggIDsToModelNames;
	}

}
