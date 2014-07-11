package alignment.localblast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import es.uvigo.ei.aibench.workbench.Workbench;


public class ParserBlast {

	protected String delimiter;
	protected File fileName;
	protected String line;
	protected BufferedReader reader;
	protected FileReader f;
	
	public ParserBlast(File fileName, String delimiter){
		this.fileName = fileName;
		this.delimiter= delimiter;
		this.line = null;
	}
	
	
	/**
	 * This method opens the connection to the file from which data will be extracted.
	 * @return
	 */
	
	public boolean openFile()
	{
		try {
			System.out.println(this.fileName);
			this.f = new FileReader(this.fileName);
			this.reader = new BufferedReader(f);
		} 
		catch(Exception e){
			e.printStackTrace();
			Workbench.getInstance().error("Problems opening the blast output file!");
			return false;
			}
		
		return true;
	}
	
	/**
	 * This method closes the connection to the file from which data was extracted.
	 * @return
	 */
	public boolean closeFile()
	{
		try {
			this.reader.close();
			this.f.close();
		} catch(Exception e)
		{e.printStackTrace();
		return false;}
		
		return true;
	}
	
	/**
	 * Returns the last line parsed.
	 * @return
	 */
	public String getLine()
	{
		return this.line;
	}
	
	/**
	 * Reads and parses the next line in the file.
	 * @return
	 * @throws Exception
	 */
	
	public boolean nextLine() throws Exception
	{
//		boolean res = this.reader.ready();
//		
//		if(res) this.line = this.reader.readLine();
//		else this.line = null;
		

		boolean res = true;
		
		this.line = this.reader.readLine();
		
		if(this.line==null) res=false;
		
		return res;
	}
	
	/**
	 * Returns true if the file has more lines to be read or false if the last line was already read.
	 * @return
	 * @throws Exception
	 */
	public boolean ready() throws Exception
	{
		return this.reader.ready();
	}
	
	/**
	 * This method is used to place the loader in the line in which actual data starts.
	 * No all loader use this method just the ones that are use to parse files that contain long comments before the actual data.
	 * @return
	 * @throws Exception
	 */
	public boolean findDataStart() throws Exception
	{
		boolean stop = false;
		
		try {
			while(this.reader.ready() && !stop)
			{
				this.line = this.reader.readLine();
				if(this.line.startsWith("ECK"))
				{
					stop = true;
				}
			}
		}
		catch(Exception e)
		{e.printStackTrace();
		return false;}
		
		return true;
	}
	
	/**
	 * This method is used to parse the last line read.
	 * By default the line is broken by spaces and the different pieces are returned in a array, 
	 * but this method is usually modified in different loaders.
	 * 
	 * @return
	 */
	public String[] parseLine()
	{		
		String[] res =this.line.split(delimiter);
		
		return res;
	}
		

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
}