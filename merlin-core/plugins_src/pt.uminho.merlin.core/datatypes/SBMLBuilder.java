package datatypes;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 *
 */
public class SBMLBuilder {

	protected Model sbmlModel;
	protected String filePath;
	private int level;
	private int version;
	private Map<String, Set<String>> reactionReactants, reactionProducts;
	private SBMLDocument sbmlDoc;
	private String biomassEquationID;

	/**
	 * @param id
	 * @param filePath
	 * @param level
	 * @param version
	 */
	public SBMLBuilder(String id, String filePath, int level, int version) {

		this.level=level;
		this.version=version;
		this.filePath = filePath;

		this.sbmlDoc = new SBMLDocument(this.level, this.version);
		this.sbmlModel = sbmlDoc.createModel(id.replaceAll("[^a-zA-Z0-9]", ""));
		this.reactionReactants=new TreeMap<String, Set<String>>();
		this.reactionProducts=new TreeMap<String, Set<String>>();
	}

	/**
	 * @param id
	 * @param name
	 */
	public void addCompartment(String id, String name, String outsideID) {

		Compartment compartment = this.sbmlModel.createCompartment();
		compartment.setId(id.replace(" ", "_"));
		compartment.setName(name);
		compartment.setUnits("volume");
		compartment.setSize(1.00);
		if(outsideID!=null) {

			compartment.setOutside(outsideID);
		}

		if (!this.sbmlModel.containsCompartment(id.replace(" ", "_"))) {

			this.sbmlModel.addCompartment(compartment);
		}
	}

	/**
	 * @param id
	 * @param name
	 * @param compartment
	 * @param string 

	public void addSpecies(String id, String name, String compartment){
		Species specie = new Species(this.level, this.version);
		specie.setId(id);
		specie.setName(name);
		specie.setCompartment(compartment);
		specie.setInitialAmount(1);
		this.sbmlModel.addSpecies(specie);
	}
	 */

	/**
	 * @param id
	 * @param name
	 * @param urn_id
	 * @param compartment
	 */
	public void addSpecies(String id, String name, String compartment, Annotation annotation) {

		Species species = this.sbmlModel.createSpecies();
		species.setId(id);
		species.setName(name);
		species.setCompartment(compartment);
		species.setInitialAmount(1);

		species.setMetaId(id);
		species.setAnnotation(annotation);
	}

	/**
	 * @param id
	 * @param name
	 * @param urn_id
	 * @param reversibility
	 * @param notes
	 * @param lower_bound
	 * @param upper_bound
	 * @param biomassEquation
	 */
	public void addReaction(String id, String name, String urn_id, boolean reversibility, Set<String> notes, double lower_bound , double upper_bound, boolean biomassEquation) {

		Reaction reaction = this.sbmlModel.createReaction();
		reaction.setName(name);
		reaction.setId(id);
		reaction.setMetaId(id);
		reaction.setReversible(reversibility);
		if(notes!=null)
			for(String note : notes)
				reaction.appendNotes(note);

		Annotation annotation = new Annotation();
		CVTerm cvTerm = new CVTerm(Qualifier.BQB_IS);
		annotation.addCVTerm(cvTerm);

		if(urn_id!=null && urn_id.startsWith("R")) {

			String link_id = urn_id;

			if(link_id.contains("_")) {

				link_id = urn_id.split("_")[0];
			}
			cvTerm.addResource("urn:miriam:kegg.reaction:" + link_id);
			cvTerm.addResource("http://www.genome.jp/dbget-bin/www_bget?rn:"+link_id);
			reaction.setAnnotation(annotation);
		}

		ListOf<LocalParameter> listOfLocalParameters = new ListOf<LocalParameter>();
		LocalParameter parameter = new LocalParameter();
		parameter.setId("LOWER_BOUND");
		parameter.setValue(lower_bound);
		listOfLocalParameters.add(parameter);

		parameter = new LocalParameter();
		parameter.setId("OBJECTIVE_COEFFICIENT");
		parameter.setValue(0.000000);

		if(biomassEquation) { 

			parameter.setValue(1.000000);
		}

		listOfLocalParameters.add(parameter);

		parameter = new LocalParameter();
		parameter.setId("UPPER_BOUND");
		parameter.setValue(upper_bound);
		listOfLocalParameters.add(parameter);

		KineticLaw kineticLaw = new KineticLaw();
		String math = "<math xmlns=\"http://www.w3.org/1998/Math/MathML\"><ci> FLUX_VALUE </ci></math>";
		ASTNode mathnode = JSBML.readMathMLFromString(math);
		kineticLaw.setMath(mathnode);
		kineticLaw.setListOfLocalParameters(listOfLocalParameters);
		reaction.setKineticLaw(kineticLaw);
	}

	/**
	 * @param enzymeID
	 * @param reactionID
	 */
	public void setReactionEnzyme(String enzymeID, String reactionID) {

		ModifierSpeciesReference msrEnzyme = new ModifierSpeciesReference(this.level, this.version);
		msrEnzyme.setSpecies(enzymeID);
		this.sbmlModel.getReaction(reactionID).addModifier(msrEnzyme);
	}

	/**
	 * @param id
	 * @param reactionID
	 * @param stoichiometry
	 */
	public void setReactionCompound(String compoundID, String reactionID, double stoichiometry) {

		if(stoichiometry>0) {

			if(this.reactionProducts.containsKey(reactionID)) {

				if(this.reactionProducts.get(reactionID).contains(compoundID)) {

					// do nothing
					System.err.println(compoundID+"\t"+ reactionID);
				}
				else {

					Set<String> compoundsID = this.reactionProducts.get(reactionID);
					compoundsID.add(compoundID);
					this.reactionProducts.put(reactionID, compoundsID);

					SpeciesReference srCompound = this.sbmlModel.getReaction(reactionID).createProduct(this.sbmlModel.getSpecies(compoundID));
					srCompound.setStoichiometry(stoichiometry);
				}
			}
			else {

				Set<String> compoundsID = new HashSet<String>();
				compoundsID.add(compoundID);
				this.reactionProducts.put(reactionID, compoundsID);

				SpeciesReference srCompound = this.sbmlModel.getReaction(reactionID).createProduct(this.sbmlModel.getSpecies(compoundID));
				srCompound.setStoichiometry(stoichiometry);
			}
		}
		else {

			if(this.reactionReactants.containsKey(reactionID)) {

				if(this.reactionReactants.get(reactionID).contains(compoundID.toString())) {

					// do nothing
					System.err.println(compoundID+"\t"+ reactionID);
				}
				else {

					Set<String> compoundsID = this.reactionReactants.get(reactionID);
					compoundsID.add(compoundID);
					this.reactionReactants.put(reactionID, compoundsID);

					SpeciesReference srCompound = this.sbmlModel.getReaction(reactionID).createReactant(this.sbmlModel.getSpecies(compoundID));
					stoichiometry=stoichiometry*-1;
					srCompound.setStoichiometry(stoichiometry);
				}
			}
			else {

				Set<String> compoundsID = new HashSet<String>();
				compoundsID.add(compoundID);
				this.reactionReactants.put(reactionID, compoundsID);

				SpeciesReference srCompound = this.sbmlModel.getReaction(reactionID).createReactant(this.sbmlModel.getSpecies(compoundID));
				stoichiometry=stoichiometry*-1;
				srCompound.setStoichiometry(stoichiometry);				
			}
		}
	}

	/**
	 * 
	 */
	public void createSBMLDocument(boolean validateSBML) {

		SBMLWriter sbmlwriter = new SBMLWriter();

		try {

			sbmlwriter.setProgramName("merlin - www.merlin-sysbio.org");
			sbmlwriter.setProgramVersion("2");
			sbmlwriter.write(this.sbmlDoc, this.filePath);

			if(validateSBML)
				if(this.sbmlDoc.checkConsistency()>0) {

					Workbench.getInstance().error(this.sbmlDoc.getListOfErrors().getErrorCount() + " errors have occured while writing the sbml.");
					System.out.println(this.sbmlDoc.getListOfErrors());
				}
		}
		catch (FileNotFoundException e) {Workbench.getInstance().error("File not found."); e.printStackTrace();}
		catch (XMLStreamException e) {e.printStackTrace();}
		catch (SBMLException e) {Workbench.getInstance().error(e.getShortMessage()); e.printStackTrace();}
	}

	/**
	 * get SBML document level
	 * 
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * set SBML document level
	 * 
	 * @param level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * get SBML document version
	 * 
	 * @return
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * set SBML document version
	 * 
	 * @param version
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * @return the sbmlModel
	 */
	public Model getSbmlModel() {
		return sbmlModel;
	}

	/**
	 * @param sbmlModel the sbmlModel to set
	 */
	public void setSbmlModel(Model sbmlModel) {
		this.sbmlModel = sbmlModel;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//SBMLBuilder sbml = new SBMLBuilder("id", "C:/Users/ODias/Desktop/xml.xml",2,4);
		//sbml.addSpecies("id2", "name", "1.1.1.1", "HERE");
		//sbml.addSpecies("id3", "name", "C00001", "HERE");
		//sbml.createSBMLDocument();
	}
}
