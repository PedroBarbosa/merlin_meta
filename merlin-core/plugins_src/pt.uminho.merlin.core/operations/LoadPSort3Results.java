package operations;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.transporters.core.compartments.PSort3;
import pt.uminho.sysbio.common.transporters.core.compartments.PSort3_result;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Load PSORTb v3.0 Results", description="Load PSORTb v3.0 Results to merlin project.")
public class LoadPSort3Results {


	private Project project;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private long startTime;
	private PSort3 pSort3;

	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order=1)
	public void setProject(Project project) {

		this.project = project;
	}

	@Port(direction=Direction.INPUT, name="PSORTb v3.0 files directory",description="Path to PSORTb v3.0 files directory",validateMethod="checkFiles",order=2)
	public void setFile_dir(File file_dir) {

		this.pSort3 = new PSort3();
		this.pSort3.setNCBIGenome(project.isNCBIGenome());

		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

		Map<String, PSort3_result> results = new HashMap<String, PSort3_result>();

		if(!file_dir.isDirectory()) {

			file_dir = new File(file_dir.getParent().toString());
		}

		boolean error = false;
		int counter = 0;
		int max = file_dir.listFiles().length;

		for(File outFile : file_dir.listFiles()) {

			if(outFile.getName().endsWith(".out") || outFile.getName().endsWith(".psort")) {

				Map<String, PSort3_result> tempResults = null;

				try {

					tempResults = this.pSort3.addGeneInformation(outFile);
				}
				catch (Exception e) {

					error=true;
					e.printStackTrace();
				}

				if(tempResults!=null) {

					results.putAll(tempResults);
				}
			}
			this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),counter,max);
			counter++;
		}

		if(error) {

			Workbench.getInstance().error("An error occurred when performing the operation!");
		}
		else {

			if(results.isEmpty()) {

				Workbench.getInstance().warn("merlin could not find any compartments information, skipping PSort3 results loading!");
			}
			else {
				
				this.project.setPsort3Results(results);

				AIBenchUtils.updateProjectView(project.getName());
				Workbench.getInstance().info("PSORTb v3.0 compartments prediction loaded.");
			}
		}

	}

	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel(){
		
		this.progress.setTime(0,1,1);
		this.pSort3.setCancel(new AtomicBoolean(true));
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
		}
	}


	/**
	 * @param project
	 * @throws Exception 
	 */
	public void checkFiles(File file_dir) {

		if(file_dir == null || file_dir.toString().isEmpty()) {

			throw new IllegalArgumentException("PSort3 result files directory not set!");
		}
	}

}
