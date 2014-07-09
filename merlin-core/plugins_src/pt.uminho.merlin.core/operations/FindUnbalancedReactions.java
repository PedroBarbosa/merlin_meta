package operations;

import pt.uminho.sysbio.common.biocomponents.container.Container;
import pt.uminho.sysbio.common.biocomponents.container.io.readers.MerlinDBReader;
import pt.uminho.sysbio.common.biocomponents.validation.chemestry.BalanceValidator;
import utilities.AIBenchUtils;
import datatypes.Project;
import datatypes.metabolic.ReactionsContainer;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

@Operation(description="Find gaps in the metabolic network.")
public class FindUnbalancedReactions {

	private Project project;
	private ReactionsContainer reaction;
	private String protonId;
	private Container container;

	/**
	 * 
	 * @param project
	 */
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {


		try {
			this.container = new Container(new MerlinDBReader(project.getDatabase().getMySqlCredentials(),
					project.getName(),project.isCompartmentalisedModel(),
					project.getOrganismName(),
					null));
			
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new IllegalArgumentException(e);
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

	@Port(name="Proton name",description="Set the proton (H+) name",direction=Direction.INPUT,order=2, validateMethod="checkProtonId")
	public void setProtonId(String protonName) {

		try {
			String proton = null;

			for(String mid : container.getMetabolites().keySet()) {

				if(this.protonId.equals(container.getMetabolites().get(mid).getName()))
					proton = mid;
			}

			BalanceValidator bv = new BalanceValidator(container);
			System.out.println("Set Formulas");
			bv.setFormulasFromContainer();
			System.out.println("validateAllReactions");
			bv.validateAllReactions();
			bv.balanceH(proton);
			Container cont = bv.getBalancedContainer();
			
			for(String id : cont.getReactions().keySet())
				this.reaction.getExternalModelIds().put(container.getReactionsExtraInfo().get(id).get("MERLIN_ID"),id);


//			for(String reactionId : cont.getReactions().keySet()){
//
//				if(!cont.getDrains().contains(reactionId)) {
//
//					ReactionCI r = cont.getReaction(reactionId);
//
//					String reactionTag = bv.getReactionTags().get(reactionId);
//					Set<String> metTags = new TreeSet<String>();
//					System.out.println(reactionId + "\t"+
//							ContainerUtils.getReactionToString(r)+"\t"+
//							bv.getSumOfReactantsToString(reactionId) + "\t"+
//							bv.getSumOfProductsToString(reactionId)+"\t"+ 
//							bv.getDifResultToString(reactionId)+"\t"+
//							bv.getChargeR(reactionId) + "\t" +
//							bv.getChargeP(reactionId) +"\t"+
//							bv.getChargeDiff(reactionId)+"\t" +
//							metTags + "\t"+reactionTag+"\n");
//				}
//			}

			this.reaction.setBalanceValidator(bv);

			AIBenchUtils.updateReactionsView(project.getName());
		} 
		catch (Exception e) {

			e.printStackTrace();
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param proton
	 */
	public void checkProtonId(String protonName) {

		this.protonId = null;

		if(protonName == null || protonName.isEmpty()) {

			throw new IllegalArgumentException("Define proton identifier!");
		}
		else {

			String [][] matrix = this.reaction.getAllMetabolites();

			for(int i = 0; i<matrix[0].length; i++ )
				if(protonName.equalsIgnoreCase(matrix[2][i]))
					this.protonId = matrix[3][i];		


			if(this.protonId == null)
				throw new IllegalArgumentException("The selected project does not contain the required proton id!");
		}
	}

}
