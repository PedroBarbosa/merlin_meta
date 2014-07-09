package operations;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Schemas;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import utilities.AIBenchUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Clean database.", name="Clean Database.")
public class CleanDatabase {

	private Project project;

	/**
	 * @param project
	 */
	@Port(name="Select Project:",description="Select project.",direction=Direction.INPUT,order=1)
	public void setProject(Project project){
		this.project = project;
	}

	/**
	 * @param schema
	 */
	@Port(name="Select Information",description="Be carefull while selecting information to delete!", defaultValue = "IGNORE", direction = Direction.INPUT, order=2)
	public void cleanSchema(SchemaType schema) {
		
		MySQL_Schemas schemas = new MySQL_Schemas( this.project.getDatabase().getMySqlCredentials().get_database_user(),
				this.project.getDatabase().getMySqlCredentials().get_database_password(),
				this.project.getDatabase().getMySqlCredentials().get_database_host(),
				this.project.getDatabase().getMySqlCredentials().get_database_port());


		String[] filePath=null;
		String path = FileUtils.getCurrentLibDirectory()+"/../utilities";
		switch (schema)
		{
		case ALL_INFORMATION:
		{
			filePath=new String[6];
			filePath[0]=path +"/sysbio_KEGG.sql";
			filePath[1]=path +"/sysbio_blast.sql";
			filePath[2]=path +"/sysbio_metabolites_transporters.sql";
			filePath[5]=path +"/sysbio_metabolites_backup.sql";
			filePath[4]=path +"/sysbio_sw_tcdb.sql";
			filePath[3]=path +"/sysbio_compartments.sql";
			this.project.setSW_TransportersSearch(false);
			this.project.setTransporterLoaded(false);
			this.project.setInitialiseHomologyData(true);
//			this.project.setCompartmentalisedModel(false);
//			this.project.setGeneDataAvailable(false);
//			this.project.setMetabolicDataAvailable(false);
//			this.project.setTransportContainer(null);
//			this.project.setCompartmentsLoaded(false);
			this.project.setGeneCompartmens(null);
			break;
		}
		case KEGG_INFORMATION: {

			filePath=new String[1];
			filePath[0]=path +"/sysbio_KEGG.sql";
//			this.project.setGeneDataAvailable(false);
//			this.project.setMetabolicDataAvailable(false);
//			this.project.setCompartmentalisedModel(false);
			this.project.setTransporterLoaded(false);
			break;
		}
		case SW_TCDB_INFORMATION:
		{
			filePath=new String[1];
			filePath[0]=path +"/sysbio_sw_tcdb.sql";
			this.project.setSW_TransportersSearch(false);
			this.project.setTransportContainer(null);
			break;
		}
		case HOMOLOGY_INFORMATION:
		{
			filePath=new String[1];
			filePath[0]=path +"/sysbio_blast.sql";
			this.project.setInitialiseHomologyData(true);
			break;
		}
		case TRANSPORTERS_DATABASE:
		{
			filePath=new String[2];
			filePath[0]=path+"/sysbio_metabolites_transporters.sql";
			filePath[1]=path+"/sysbio_metabolites_backup.sql";
			this.project.setTransporterLoaded(false);
			this.project.setTransportContainer(null);
			break;
		}
		case COMPARTMENTS_DATABASE: {

			filePath=new String[1];
			filePath[0]=path +"/sysbio_compartments.sql";
			
			if(this.project.isCompartmentalisedModel()) {
			
				Workbench.getInstance().warn("Compartments already integrated in model. To remove compartments from model, all KEGG information should be removed and the database re-loaded!");
			}
			else {
				
//				this.project.setCompartmentalisedModel(false);
//				this.project.setCompartmentsLoaded(false);
				this.project.setGeneCompartmens(null);
			}
			break;
		}
		case IGNORE:
		{
			break;
		}
		}

		if(schema.equals(SchemaType.IGNORE)) {

			Workbench.getInstance().info("Database cleaning ignored.");
		}
		else {
			
			if(schemas.cleanSchema(this.project.getDatabase().getMySqlCredentials().get_database_name(), filePath)) {

				AIBenchUtils.updateAllViews(project.getName());
				Workbench.getInstance().info("Database "+this.project.getDatabase().getMySqlCredentials().get_database_name()+" successfuly cleaned.");
			}
			else {
				
				Workbench.getInstance().error("There was an error when trying to format "+this.project.getDatabase().getMySqlCredentials().get_database_name()+"!!");
			}
		}

	}

	/**
	 * @author ODias
	 *
	 */
	public enum SchemaType {

		ALL_INFORMATION,
		KEGG_INFORMATION,
		HOMOLOGY_INFORMATION,
		SW_TCDB_INFORMATION,
		TRANSPORTERS_DATABASE,
		COMPARTMENTS_DATABASE, 
		IGNORE
	}

}
