/**
	 * 
	 */
	package operations;

	import java.io.File;
	import java.util.ArrayList;
	import java.util.List;

	import datatypes.Project;
	import es.uvigo.ei.aibench.core.operation.annotation.Direction;
	import es.uvigo.ei.aibench.core.operation.annotation.Operation;
	import es.uvigo.ei.aibench.core.operation.annotation.Port;
	import es.uvigo.ei.aibench.workbench.Workbench;
	import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;

	/**
	 * @author ODias
	 *
	 */
	@Operation(description="Set the genome fasta files to perform similarity searches.")
	public class SetGenomeFastaFiles {

		private Project project=null;
		private boolean isNCBIGenome;
		private boolean isMetagenomicProject;
		private List<File> faaFastaFiles, fnaFastaFiles;
		private long taxonomyID = -1;

		@Port(direction=Direction.INPUT, name="Select Project", validateMethod = "checkProject", order=5)
		public void setProject(Project project){
			this.project = project;
		}

		@Port(direction=Direction.INPUT, validateMethod="isMetagenomic", name = "Is Metagenomics project?", defaultValue = "false", description="Whether this is a multi-organism project",order=2)
		public void isMetagenomicProject(boolean isMetagenomicProject){
			this.isMetagenomicProject = isMetagenomicProject;
		}

		@Port(direction=Direction.INPUT, validateMethod="checkGenome",name = "Is NCBI genome?", defaultValue="false", description="Whether Genome was downloaded from NCBI FTP webSite",order=3)
		public void isNCBIGenome(boolean isNCBIGenome){
			this.isNCBIGenome = isNCBIGenome;
		}

		@Port(direction=Direction.INPUT, validateMethod="checkGenomeID",name = "NCBI Taxonomy ID?", defaultValue="0", description="Required if genome was not downloaded from NCBI FTP webSite",order=4)
		public void setTaxonomyID(long taxonomyID){

			this.taxonomyID = taxonomyID;
		}

		@Port(direction=Direction.INPUT,validateMethod="checkFiles",name="Genome files (fasta format)", description="Path to genome.",order=1)
		public void setFiles(File file){
		}

		
		/**
		 * @param isMeta
		 */
		public void isMetagenomic(boolean isMeta){
			this.isMetagenomicProject = isMeta;
		}
		
		/**
		 * @param isNCBIGenome
		 */
		public void checkGenome(boolean isNCBIGenome){

			if(this.isMetagenomicProject){
				//if(isNCBIGenome) Workbench.getInstance().error("Since you selected a metagenomic project\n please don't choose the 'isNCBIgenome option");
				if(isNCBIGenome) throw new IllegalArgumentException("Since you selected a metagenomic project\nplease don't choose the 'is NCBI genome' option.");
				else this.isNCBIGenome = isNCBIGenome;
			}
			else{
				this.isNCBIGenome = isNCBIGenome;
			}

		}

		/**
		 * @param isNCBIGenome
		 */
		public void checkGenomeID(long taxonomyID) {

			if(this.isMetagenomicProject){

				if(taxonomyID != 131567)throw new IllegalArgumentException("Please enter the '131567' taxonomic ID \nfrom NCBI taxonomy for metagenomics projects.");
				else this.taxonomyID = taxonomyID;
			}
			else{
				
				if(!this.isNCBIGenome) {

					if(taxonomyID == 0 ) {

						throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");
					}
					else {

						this.taxonomyID = taxonomyID;
					}
				}

			}

		}

		/**
		 * @param project
		 */
		public void checkProject(Project project) {
			this.project = project;

			if(this.project==null) {

				throw new IllegalArgumentException("Please select a project.");
			}
			else {

					if(this.project.isMetagenomicProject()){
						
						if(!this.isMetagenomicProject) throw new IllegalArgumentException("Since this is a metagenomics project, please select the 'is Metagenomic project' option \nand don't choose the 'is NCBI genome' one. Additionally, set the taxonomic ID to '131567'.");		
					}	
					else {
						if(this.isMetagenomicProject) throw new IllegalArgumentException("Since this is a single organism project,\nplease don't select the 'is Metagenomic Project' option.");	
					}
					
					try{
					
					if(!faaFastaFiles.isEmpty()) { 

						CreateGenomeFile createGenomeFile = new CreateGenomeFile("genome_"+project.getName()+"_"+faaFastaFiles.size(), faaFastaFiles, ".faa");
						createGenomeFile.setNCBIGenome(this.isNCBIGenome);
						String genomeID=createGenomeFile.getGenomeID();
						this.project.setFaaFiles(true);
						this.project.setGenomeCodeName(genomeID);
						this.project.setNCBIGenome(this.isNCBIGenome);

						if(!this.isNCBIGenome) {

							this.project.setTaxonomyID(this.taxonomyID);
						}
						Workbench.getInstance().info("Project 'faa' files successfully added!");
					}

					if(!fnaFastaFiles.isEmpty()) { 

						CreateGenomeFile createGenomeFile = new CreateGenomeFile("genome_"+project.getName()+"_"+fnaFastaFiles.size(), fnaFastaFiles, ".fna");
						createGenomeFile.setNCBIGenome(this.isNCBIGenome);
						String genomeID=createGenomeFile.getGenomeID();
						this.project.setFnaFiles(true);
						this.project.setGenomeCodeName(genomeID);
						this.project.setNCBIGenome(this.isNCBIGenome);

						if(!this.isNCBIGenome) {

							this.project.setTaxonomyID(this.taxonomyID);
						}
						Workbench.getInstance().info("Project 'fna' files successfully added!");
					}
				} 
				catch (Exception e) {

					throw new IllegalArgumentException("Error uploading fasta files!");
				} 
			}

		}


		/**
		 * @param file
		 */
		public void checkFiles(File file){

			if(file == null || file.toString().isEmpty() || (!file.isDirectory() && !file.isFile())) {

				throw new IllegalArgumentException("Fasta files directory not set!");
			}
			else {

				if(!file.isDirectory()) {

					file = new File(file.getParent().toString());
				}

				this.faaFastaFiles = new ArrayList<File>();
				this.fnaFastaFiles = new ArrayList<File>();

				for(File f: file.listFiles()) {

					if(f.getAbsolutePath().endsWith(".faa")) {

						faaFastaFiles.add(f);
					}
					if(f.getAbsolutePath().endsWith(".fna")) {

						fnaFastaFiles.add(f);
					}
				}

				if(faaFastaFiles.isEmpty() && fnaFastaFiles.isEmpty()) {

					throw new IllegalArgumentException("Please Select a directory with '.faa' or '.fna' files!");
				}
			}
		}

	}