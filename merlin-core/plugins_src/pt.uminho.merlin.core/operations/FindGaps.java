package operations;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pt.uminho.sysbio.common.biocomponents.container.Container;
import pt.uminho.sysbio.common.biocomponents.container.ContainerUtils;
import pt.uminho.sysbio.common.biocomponents.container.io.readers.MerlinDBReader;
import utilities.AIBenchUtils;
import datatypes.Project;
import datatypes.metabolic.ReactionsContainer;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

/**
 * @author ODias
 *
 */
@Operation(description="Find gaps in the metabolic network.")
public class FindGaps {

	private Project project;
	private ReactionsContainer reaction;

	/**
	 * 
	 * @param project
	 */
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

		Container container;
		try {
			container = new Container(new MerlinDBReader(project.getDatabase().getMySqlCredentials(),
					project.getName(),project.isCompartmentalisedModel(),
					project.getOrganismName(),
					null));
			
			if(container.getExternalCompartmentId() != null)
				container.constructDrains(container.getExternalCompartment().getMetabolitesInCompartmentID(), container.getExternalCompartmentId(), 0.0,10000.0);

			Set<String> gaps = new HashSet<>(), 
					gapIds = ContainerUtils.identyfyReactionWithDeadEnds(container),
					drains = container.getDrains();
			
			gapIds.removeAll(drains);
			
			for(String id : gapIds)
				gaps.add(container.getReactionsExtraInfo().get(id).get("MERLIN_ID"));

			this.reaction.setGapReactions(gaps);

			AIBenchUtils.updateReactionsView(project.getName());
		} 
		catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			this.project = project;

			for(Entity ent : this.project.getDatabase().getEntities().getEntities())
				if(ent.getName().equalsIgnoreCase("Reactions"))
					reaction = (ReactionsContainer) ent;

			if(this.reaction.getActiveReactions() == null)
				throw new IllegalArgumentException("Reactions view unavailable!");
		}
	}
}
