/**
 * 
 */
package remote;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import remote.loader.DatabaseInitialData;
import remote.loader.KEGG_loader;
import remote.loader.Load_KEGG_Data;
import remote.retriever.Retrieve_kegg_data;
import datatypes.Project;

/**
 * @author ODias
 *
 */
public class GetAndLoadKeggData {

	private MySQLMultiThread msqlmt;
	private String organismID;
	private List<Runnable> runnables;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private Retrieve_kegg_data retrieve_kegg_data;

	/**
	 * @param project
	 * @param organismID
	 */
	public GetAndLoadKeggData(Project project, String organismID) {

		try  {
			
			this.cancel = new AtomicBoolean(false);
			this.organismID = organismID;
			this.msqlmt = new MySQLMultiThread(
					project.getDatabase().getMySqlCredentials().get_database_user(), 
					project.getDatabase().getMySqlCredentials().get_database_password(),
					project.getDatabase().getMySqlCredentials().get_database_host(), 
					project.getDatabase().getMySqlCredentials().get_database_port(),
					project.getDatabase().getMySqlCredentials().get_database_name()
					);
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	/**
	 * @param project
	 * @param organismID
	 * @return
	 */
	public boolean get_data() {

		try  {

			this.retrieve_kegg_data = new Retrieve_kegg_data(this.organismID);
			return true;
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * @param retrieve_kegg_data
	 * @return
	 */
	public boolean load_data() {

		try  {

			boolean error = false;

			Connection conn = new Connection(this.msqlmt);

			long startTime = System.currentTimeMillis();
			int numberOfProcesses =  Runtime.getRuntime().availableProcessors();
			List<Thread> threads = new ArrayList<Thread>();

			this.runnables = new ArrayList<Runnable>();

			DatabaseInitialData databaseInitialData = new DatabaseInitialData(conn);

			for(int i=0; i<numberOfProcesses; i++) {

				Runnable load_KEGG_Data = new Load_KEGG_Data(this.msqlmt,this.organismID, this.retrieve_kegg_data, databaseInitialData, this.progress);
				this.runnables.add(load_KEGG_Data);
				Thread thread = new Thread(load_KEGG_Data);
				threads.add(thread);
				thread.start();
				
				if(((Load_KEGG_Data) load_KEGG_Data).isError()) {
					
					error = true;
				}
			}

			for(Thread thread :threads) {
				
				thread.join();
			}

			long endTime2 = System.currentTimeMillis();

			long startTime1 = System.currentTimeMillis();

			KEGG_loader.build_Views(conn);

			long endTime1 = System.currentTimeMillis();

			long endTime = System.currentTimeMillis();

			System.out.println("Total elapsed time in execution of method Load_kegg is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime2-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime))));

			System.out.println("Total elapsed time in execution of method build view is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1),TimeUnit.MILLISECONDS.toSeconds(endTime1-startTime1) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1))));

			System.out.println("Total elapsed time in execution of method TOTAL is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

			if(error) 
				return false;
			else
				return true;
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		
		return cancel;
	}

	/**
	 * 
	 */
	public void setCancel() {
		
		this.cancel = new AtomicBoolean(true);

		if(this.retrieve_kegg_data != null) {
			
			this.retrieve_kegg_data.setCancel(this.cancel);
		}

		if (this.runnables != null) {
			
			for(Runnable lc :this.runnables) {

				((Load_KEGG_Data) lc).setCancel(this.cancel);
			}
		}
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {
		
		this.progress = progress;		
	}



	//	/**
	//	 * @param user
	//	 * @param password
	//	 * @param host
	//	 * @param port
	//	 * @param database
	//	 * @throws Exception 
	//	 */
	//	public Get_and_load_Kegg_data(String user, String password, String host, int port, String database, String organismID) throws Exception {
	//
	//		long startTime = System.currentTimeMillis();
	//
	//		Retrieve_kegg_data retrieve_kegg_data = new Retrieve_kegg_data(organismID);
	//		MySQLMultiThread msqlmt = new MySQLMultiThread(user, password, host, port, database);
	//
	//		int numberOfProcesses =  Runtime.getRuntime().availableProcessors()*2;
	//		List<Thread> threads = new ArrayList<Thread>();
	//
	//		Connection conn = msqlmt.openConnection();
	//		DatabaseInitialData databaseInitialData = new DatabaseInitialData(conn);
	//		for(int i=0; i<numberOfProcesses; i++) {
	//			
	//			Runnable load_KEGG_Data = new Load_KEGG_Data(msqlmt,organismID,retrieve_kegg_data, databaseInitialData);
	//			Thread thread = new Thread(load_KEGG_Data);
	//			threads.add(thread);
	//			thread.start();
	//		}
	//
	//		for(Thread thread :threads)
	//		{
	//			thread.join();
	//		}
	//
	//		long endTime2 = System.currentTimeMillis();
	//
	//		long startTime1 = System.currentTimeMillis();
	//		KEGG_loader.build_Views(conn);
	//		long endTime1 = System.currentTimeMillis();
	//
	//		long endTime = System.currentTimeMillis();
	//
	//		System.out.println("Total elapsed time in execution of method Load_kegg is :"+ String.format("%d min, %d sec", 
	//				TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime2-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime))));
	//
	//		System.out.println("Total elapsed time in execution of method build view is :"+ String.format("%d min, %d sec", 
	//				TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1),TimeUnit.MILLISECONDS.toSeconds(endTime1-startTime1) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1))));
	//
	//		System.out.println("Total elapsed time in execution of method TOTAL is :"+ String.format("%d min, %d sec", 
	//				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
	//		conn.close();
	//	}
	//
	//	public static void main(String[] args) throws Exception{
	//
	//		new Get_and_load_Kegg_data("root","password","localhost",3306,"klla_new_kegg","KLA");
	//
	//	}


}


