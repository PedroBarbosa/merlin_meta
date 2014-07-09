package operations;

import java.io.File;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import datatypes.Project;
import datatypes.SBMLFile;
import datatypes.metabolic.ReactionsContainer;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Export the local database to SBML",name="SBML exporter")
public class ExportToSBML {

	private String filename;
	private File directory;
	private String sbmlFileID;
	private MySQLMultiThread msqlmt;
	private boolean isCompartmentalisedModel;
	private boolean generateFormulae;
	private boolean validateSBML;
	private String biomassEquationID;
	private ReactionsContainer reaction;

	/**
	 * @param project
	 */
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

		this.msqlmt = project.getDatabase().getMySqlCredentials();
		this.sbmlFileID = project.getName();
		this.isCompartmentalisedModel = project.isCompartmentalisedModel();
	}
	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			for(Entity ent : project.getDatabase().getEntities().getEntities())
				if(ent.getName().equalsIgnoreCase("Reactions"))
					reaction = (ReactionsContainer) ent;
			
			if(this.reaction.getActiveReactions() == null)
				throw new IllegalArgumentException("Reactions view unavailable!");
		}
	}
	
	@Port(name="Generate Formulae:",description="Generate formulae file for OptFlux.",direction=Direction.INPUT,defaultValue="false",order=2)
	public void generateFormulae(boolean generateFormulae) {
		
		this.generateFormulae = generateFormulae;
	}
	
	@Port(name="Validate SBML:",description="Validate SBML files online.",direction=Direction.INPUT,defaultValue="false",order=3)
	public void validateSBML(boolean validateSBML) {
		
		this.validateSBML = validateSBML;
	}
	

	@Port(name="Biomass reaction name",description="Set Biomass reaction name",direction=Direction.INPUT,order=4, validateMethod="checkBiomassEquation")
	public void biomassReaction(String biomassEquation) {

	}

	/**
	 * @param biomassEquation
	 */
	public void checkBiomassEquation(String biomassEquation) {

		this.biomassEquationID = null;

		if(biomassEquation == null) {

			throw new IllegalArgumentException("No biomass equation!");
		}
		else {

			for(String rid : this.reaction.getActiveReactions()) {

				if(rid.equalsIgnoreCase(biomassEquation))
					this.biomassEquationID = rid;		
			}

			if(!biomassEquation.isEmpty() && this.biomassEquationID == null)
				throw new IllegalArgumentException("The selected project does not contain the required biomass equation!");
		}
	}

	/**
	 * @param directory
	 */
	@Port(name="Directory:",description="Directory to place the SBML file",direction=Direction.INPUT,validateMethod="checkDirectory",order=5)
	public void selectDirectory(File directory) {

	}

	/**
	 * @param directory
	 */
	public void checkDirectory(File directory) {

		if(directory == null || directory.toString().isEmpty()) {

			throw new IllegalArgumentException("Please select a directory!");
		}
		else {

			if(directory.isDirectory()) {

				this.directory = directory;
			}
			else {

				this.directory = directory.getParentFile();	
			}
		}
	}

	/**
	 * @param filename
	 */
	@Port(name="File name:",defaultValue="",description="Name of the SBML file",direction=Direction.INPUT,validateMethod="checkFilename",order=6)
	public void setFileName(String filename) {

		new SBMLFile(this.msqlmt, this.directory.toString().concat("/").concat(this.filename),
				this.sbmlFileID, this.isCompartmentalisedModel, validateSBML, generateFormulae,
				this.biomassEquationID);
		Workbench.getInstance().info("SBML file generated!");
	}

	/**
	 * @param filename
	 */
	public void checkFilename(String filename) {

		if(filename.isEmpty()) {

			throw new IllegalArgumentException("Please enter name for the SBML file!");
		}
		else {
			
			if(filename.isEmpty()) {

				filename=sbmlFileID;
			}

			if(filename.toLowerCase().endsWith(".xml")) {

				this.filename= filename;
			}
			else {

				this.filename= filename.concat(".xml");			
			}
		}
	}
	
}
