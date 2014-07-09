package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlastKeggCompatator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		boolean go=false;

		if(!go)
		{

			String input = "C:/Users/ODias/Desktop/uniprot-KEGG_to_ENTREZ.txt";


			FileInputStream inputStream = null;
			//Create input stream
			try
			{
				inputStream = new FileInputStream(input);
			}
			catch (FileNotFoundException e1){e1.printStackTrace();}

			// Get the object of DataInputStream
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			//Buffer the InputStream
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dataInputStream));
			String stLine = "";

			Map<List<String>,List<String>> keggtoEntrezList = new HashMap<List<String>, List<String>>();
			Map<List<String>,List<String>> entreztoKeggList = new HashMap<List<String>, List<String>>();

			while((stLine=buffer.readLine())!=null)
			{
				List<String> name = new ArrayList<String>();
				List<String> kegg = new ArrayList<String>();
				while(!(stLine=buffer.readLine()).startsWith("//") && stLine!=null)
				{
					if(stLine.startsWith("GN"))
					{
						String temp = "";
						if(stLine.contains("OrderedLocusNames"))
							temp = stLine.split("OrderedLocusNames=")[stLine.split("OrderedLocusNames=").length-1].replace(";", "");
						else
							temp = stLine.replace("GN   ", "").replace(";", "");

						for(String s:temp.split(","))
						{
							if(s.length()>0)
								name.add(s.trim().toLowerCase());
							else System.out.println("sim");
						}
					}

					if(stLine.startsWith("DR   KEGG"))
					{
						kegg.add(stLine.split("kla:")[stLine.split("kla:").length-1].replace("; -.", "").toLowerCase());
					}
				}
				if (!name.equals(kegg))
				{
					entreztoKeggList.put(name, kegg);
					keggtoEntrezList.put(kegg, name);
				}
			}

			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------

			input = "C:/Users/ODias/Desktop/kla_enzyme.list";

			//Create input stream
			inputStream = null;
			try {
				inputStream = new FileInputStream(input);
			}
			catch (FileNotFoundException e1){e1.printStackTrace();}

			// Get the object of DataInputStream
			dataInputStream = new DataInputStream(inputStream);

			//Buffer the InputStream
			buffer = new BufferedReader(new InputStreamReader(dataInputStream));
			stLine = "";

			Map<String,Set<String>> keggOnlyList = new HashMap<String, Set<String>>();

			while((stLine=buffer.readLine())!=null)
			{
				String[] data = stLine.split("\t");
				String name = data[0].replace("kla:", "").toLowerCase();
				String number = data[1].replace("ec:", "").toLowerCase();
				Set<String> list = new HashSet<String>();

				for(List<String> convert: keggtoEntrezList.keySet())
				{
					if(convert.contains(name))
					{
						if(convert.size()==1)						
						{
							name = keggtoEntrezList.get(convert).get(0);
						}
						else
							System.out.println("maior que 1 " + name);
					}
				}

				if(keggOnlyList.containsKey(name))
				{
					list = keggOnlyList.get(name);
					list.add(number);
					keggOnlyList.put(name, list);
				}
				else
				{
					list = new HashSet<String>();
					list.add(number);				
					keggOnlyList.put(name, list);
				}			
			}

			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------

			//			input = "C:/Users/ODias/Desktop/ecNumbersDataNEW.xls";
			input = "C:/Users/ODias/Desktop/proteinDataNEW.xls";

			//Create input stream
			try
			{
				inputStream = new FileInputStream(input);
			}
			catch (FileNotFoundException e1){e1.printStackTrace();}

			// Get the object of DataInputStream
			dataInputStream = new DataInputStream(inputStream);

			//Buffer the InputStream
			buffer = new BufferedReader(new InputStreamReader(dataInputStream));
			stLine = "";

			Map<String,Set<String>> blastOnlyList = new HashMap<String, Set<String>>();

			while((stLine=buffer.readLine())!=null)
			{
				String[] data = stLine.split("\t");
				String name = data[0].toLowerCase();

				if(data.length==1)
				{
					data = new String[2];
					data[1]=new String();
				}

				String number = data[1].toLowerCase();
				Set<String> list = new HashSet<String>();

				if(blastOnlyList.containsKey(name))
				{
					list = blastOnlyList.get(name);
					list.add(number);
					blastOnlyList.put(name, list);
				}
				else
				{
					list = new HashSet<String>();
					if(!number.isEmpty())
						list.add(number);				
					blastOnlyList.put(name, list);
				}			
			}

			System.out.println("KEGG "+keggOnlyList.size());
			System.out.println("BLAST "+blastOnlyList.size());

			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------

			Map<String,Set<String>> matchList = new HashMap<String, Set<String>>();
			Map<String,Set<String>> partialMatchList = new HashMap<String, Set<String>>();
			Map<String,Set<String>> keggMatchExtras = new HashMap<String, Set<String>>();
			Map<String,Set<String>> blastMatchExtras = new HashMap<String, Set<String>>();
			Map<String,Set<String>> keggOnlyWithBlastMatched = new HashMap<String, Set<String>>();
			Map<String,Set<String>> blastOnlyNotEmptyNoMatch = new HashMap<String, Set<String>>();
			Map<String,Set<String>> keggMismatched = new HashMap<String, Set<String>>();
			Map<String,Set<String>> blastMismatched = new HashMap<String, Set<String>>();
			Map<String,Set<String>> blastOnlyEmptyNoMatch = new HashMap<String, Set<String>>();
			Map<String,Set<String>> blastMatchEmpty = new HashMap<String, Set<String>>();

			Set<String> iterate = new HashSet<String>();
			iterate.addAll(blastOnlyList.keySet());

			for(String s: iterate)
			{
				if(keggOnlyList.containsKey(s))
				{
					if(blastOnlyList.get(s).isEmpty())
					{	
						blastMatchEmpty.put(s, keggOnlyList.get(s));
						keggOnlyWithBlastMatched.put(s, keggOnlyList.get(s));
					}
					else
					{
						Set<String> k = keggOnlyList.get(s);
						Set<String> b = blastOnlyList.get(s);
						Set<String> match = new HashSet<String>();

						Set<String> temp = new HashSet<String>();
						temp.addAll(b);

						for(String e: temp)
						{
							if(k.contains(e))
							{
								match.add(e);
								k.remove(e);
								b.remove(e);
							}
						}

						if(match.isEmpty())
						{
							keggMismatched.put(s, k);
							blastMismatched.put(s, b);
						}
						else
						{
							if(b.isEmpty() && k.isEmpty())
							{
								matchList.put(s, match);
							}
							else
							{
								partialMatchList.put(s, match);

								if(!k.isEmpty())
								{
									keggMatchExtras.put(s, k);
								}
								if(!b.isEmpty())
								{
									blastMatchExtras.put(s, b);
								}
							}
						}
					}
					blastOnlyList.remove(s);
					keggOnlyList.remove(s);
				}
				else
				{
					if(blastOnlyList.get(s).isEmpty())
					{	
						blastOnlyEmptyNoMatch.put(s, blastOnlyList.get(s));

					}
					else
					{
						blastOnlyNotEmptyNoMatch.put(s, blastOnlyList.get(s));
					}
					blastOnlyList.remove(s);
				}

			}

			System.out.println("KEGG only "+keggOnlyList.size());
			System.out.println("BLAST Only Empty No Match "+blastOnlyEmptyNoMatch.size());
			System.out.println("BLAST match with no ECNumber "+blastMatchEmpty.size());
			System.out.println("KEGG Only With Blast Match "+keggOnlyWithBlastMatched.size());
			System.out.println("BLAST Only Not Empty No Match "+blastOnlyNotEmptyNoMatch.size());
			System.out.println("KEGG mismatched "+keggMismatched.size());
			System.out.println("BLAST mismatched "+blastMismatched.size());
			System.out.println("Full MATCH "+matchList.size());
			System.out.println("partial MATCH "+partialMatchList.size());
			System.out.println("KEGG extras matched "+keggMatchExtras.size());
			System.out.println("BLAST extras matched "+blastMatchExtras.size());

			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------

			StringBuffer strBuffer = new StringBuffer();

			BufferedWriter bout;

			strBuffer = new StringBuffer("\ngene\tMATCH\n");

			for(String s: matchList.keySet())
			{
				strBuffer.append(s+ "\t" + matchList.get(s));

				strBuffer.append("\n");
			}

			strBuffer.append("\ngene\tpartial match\tBLAST\'ed extras\tKEGG extras\n");

			for(String s: partialMatchList.keySet())
			{
				strBuffer.append(s+ "\t" + partialMatchList.get(s));
				if(blastMatchExtras.containsKey(s))
				{
					strBuffer.append("\t" + blastMatchExtras.get(s));
				}
				if(keggMatchExtras.containsKey(s))
				{
					if(blastMatchExtras.containsKey(s))
					{
						strBuffer.append("\t" + keggMatchExtras.get(s));
					}
					else
					{
						strBuffer.append("\t\t" + keggMatchExtras.get(s));
					}
				}
				strBuffer.append("\n");
			}

			strBuffer.append("\nMismatches\ngene\tBLAST\'ed\tKEGG\n");

			for(String s: blastMismatched.keySet())
			{
				strBuffer.append(s+ "\t" + blastMismatched.get(s) + "\t" + keggMismatched.get(s) + "\n");
			}

			strBuffer.append("\n\t\tKEGG Only With Blast Match\n");

			for(String s: keggOnlyWithBlastMatched.keySet())
			{
				strBuffer.append(s+ "\t\t" + keggOnlyWithBlastMatched.get(s) + "\n");
			}

			strBuffer.append("\n\t\tKEGG Only (gene unavailable at Blast list)\n");

			for(String s: keggOnlyList.keySet())
			{
				strBuffer.append(s+ "\t\t" + keggOnlyList.get(s) + "\n");
			}

			strBuffer.append("\n\tBLAST Only Not Empty No Match\n");

			for(String s: blastOnlyNotEmptyNoMatch.keySet())
			{
				strBuffer.append(s+ "\t" + blastOnlyNotEmptyNoMatch.get(s) + "\n");
			}			

			strBuffer.append("\n\tBLAST Only Empty No Match \n");

			for(String s: blastOnlyEmptyNoMatch.keySet())
			{
				strBuffer.append(s+ "\t" + blastOnlyEmptyNoMatch.get(s) + "\n");
			}

			try 
			{
				bout = new BufferedWriter(new FileWriter("C:/Users/ODias/Desktop/noMatches.xls"));
				bout.write(strBuffer.toString());
				bout.close();
			} 
			catch (IOException e) {e.printStackTrace();}

		}

















		if(go)
		{
			Set<String> numbers = new HashSet<String>(); 

			//			String inputFile = "C:/Users/ODias/Desktop/ecNumbersData.xls";
			String inputFile = "C:/Users/ODias/Desktop/proteinData.xls";

			String strLine = "";
			StringBuffer strBuff = new StringBuffer();

			//Create input stream
			FileInputStream inputFileStream = null;
			try {
				inputFileStream = new FileInputStream(inputFile);
			}
			catch (FileNotFoundException e1){e1.printStackTrace();}


			// Get the object of DataInputStream
			DataInputStream dataStream = new DataInputStream(inputFileStream);

			//Buffer the InputStream
			BufferedReader bfr = new BufferedReader(new InputStreamReader(dataStream));


			try {
				strLine=bfr.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//Read File Line By Line
			String name="";
			try {
				while(strLine!=null)
				{

					numbers = new HashSet<String>();
					if(strLine.trim().toLowerCase().startsWith("klla"))
					{
						String[] data = strLine.split("\t");

						name = data[0];

						if(data.length==1)
						{
							data = new String[2];
							data[1]="";
						}

						String[] num = data[1].split(",");

						if(num[0].isEmpty())
							strBuff.append(name + "\t" + "" +"\n");
						else
							strBuff.append(name + "\t" + num[0] +"\n");

						for(String s: num)
						{
							s=s.replace("\"", "");
							if(!s.isEmpty())
								numbers.add(s.trim());
						}
					}

					strLine=bfr.readLine();

					while(strLine!=null && !strLine.trim().toLowerCase().startsWith("klla"))
					{	
						strLine=strLine.replace("\"", "");
						if(!strLine.contains(",") && !numbers.contains(strLine.trim()))
						{
							if(!strLine.trim().isEmpty() && !numbers.contains(strLine.trim()))
							{
								numbers.add(strLine.trim());
							}
						}
						else
						{
							for(String s: strLine.split(","))
							{
								s=s.replace("\"", "");
								//								System.out.println(s);
								if(!numbers.contains(s.trim()))
								{
									//									System.err.println(s);								
									numbers.add(s.trim());
								}
							}
						}
						strLine=bfr.readLine();
						if(strLine!=null)
							strLine=strLine.replace("\"", "");
					}

					if(!numbers.isEmpty())
						for(String number: numbers)
						{
							strBuff.append(name+"\t"+number+"\n");
						}
					numbers = new HashSet<String>();

				}
			}catch (IOException e){e.printStackTrace();}

			//		StringReader strInputStream = new StringReader	(strBuff.toString());

			System.out.println("DONE!");
			BufferedWriter out;
			try 
			{
				//				out = new BufferedWriter(new FileWriter("C:/Users/ODias/Desktop/ecNumbersDataNEW.xls"));
				out = new BufferedWriter(new FileWriter("C:/Users/ODias/Desktop/proteinDataNEW.xls"));
				out.write(strBuff.toString());
				out.close();
			} catch (IOException e) {e.printStackTrace();}

		}
	}



}
