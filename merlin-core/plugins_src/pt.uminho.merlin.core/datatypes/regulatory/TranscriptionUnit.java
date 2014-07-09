package datatypes.regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pt.uminho.sysbio.merlin.utilities.SortableData;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST, namingMethod = "getName")
public class TranscriptionUnit extends Entity implements Serializable {

	private static final long serialVersionUID = 9008699561446234481L;

	private HashMap<String, String> names;
	private int sortP;

	public TranscriptionUnit(Table dbt, String name) {
		super(dbt, name);
		this.sortP = 1;
	}

	public String[][] getStats() {
		int num = 0;
		int noname = 0;

		String[][] res = new String[6][];
		try {
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ this.table.getName());

			while (rs.next()) {
				num++;
				if (rs.getString(2) == null)
					noname++;
			}

			res[0] = new String[] { "Number of TUs", "" + num };
			res[1] = new String[] { "Number of TUs with no name associated",
					"" + noname };

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT count(distinct(gene_idgene)) FROM transcription_unit JOIN "
							+ "transcription_unit_gene ON "
							+ "transcription_unit.idtranscription_unit = "
							+ "transcription_unit_gene.transcription_unit_idtranscription_unit");

			rs.next();
			String snumgenes = rs.getString(1);

			res[2] = new String[] { "Number of genes associated with TUs",
					snumgenes };

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT count(distinct(transcription_unit.idtranscription_unit)) "
							+ "FROM transcription_unit JOIN transcription_unit_gene "
							+ "ON transcription_unit.idtranscription_unit = "
							+ "transcription_unit_gene.transcription_unit_idtranscription_unit");

			rs.next();
			String snumtus = rs.getString(1);

			res[3] = new String[] { "Number of TUs with genes associated",
					snumtus };

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT count(distinct(promoter_idpromoter)) FROM transcription_unit_promoter");

			rs.next();
			double promoters_by_tus = (new Double(rs.getString(1))
					.doubleValue())
					/ (new Double(num).doubleValue());

			res[4] = new String[] { "Average number of promoters by TU",
					"" + promoters_by_tus };

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT count(distinct(gene_idgene)) FROM transcription_unit_gene");

			rs.next();

			int gens_tu = new Integer(rs.getString(1)).intValue();

			res[5] = new String[] {
					"Average number of genes by TU",
					"" + new Double(gens_tu).doubleValue()
							/ new Double(num).doubleValue() };

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public void setSort(int sortP) {
		this.sortP = sortP;
	}

	public GenericDataTable getData() {
		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String, String[]> qls = new HashMap<String, String[]>();

		columnsNames.add("Names");
		columnsNames.add("Number of genes");
		columnsNames.add("Number of promoters");

		GenericDataTable res = new GenericDataTable(columnsNames, "TUs", "TU");

		try {
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM "
					+ this.table.getName());

			while (rs.next()) {
				String[] ql = new String[4];
				ql[0] = rs.getString(2);
				ql[1] = "0";
				ql[2] = "0";
				ql[3] = rs.getString(1);
				index.add(rs.getString(1));
				qls.put(rs.getString(1), ql);
			}

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT idtranscription_unit, "
							+ "COUNT(gene_idgene) FROM transcription_unit JOIN transcription_unit_gene "
							+ "ON transcription_unit.idtranscription_unit = "
							+ "transcription_unit_gene.transcription_unit_idtranscription_unit "
							+ "GROUP BY idtranscription_unit");

			while (rs.next()) {
				qls.get(rs.getString(1))[1] = rs.getString(2);
			}

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT idtranscription_unit, COUNT(promoter_idpromoter) "
							+ "FROM transcription_unit JOIN transcription_unit_promoter ON "
							+ "transcription_unit.idtranscription_unit = "
							+ "transcription_unit_promoter.transcription_unit_idtranscription_unit "
							+ "GROUP BY idtranscription_unit");

			while (rs.next()) {
				qls.get(rs.getString(1))[2] = rs.getString(2);
			}

			SortableData[] sos = new SortableData[index.size()];

			for (int i = 0; i < index.size(); i++) {
				String[] gark = qls.get(index.get(i));
				sos[i] = new SortableData(new Integer(gark[sortP]).intValue(),
						gark);
			}

			Arrays.sort(sos);

			for (int i = 0; i < sos.length; i++) {
				ArrayList<Object> ql = new ArrayList<Object>();
				String[] gark = (String[]) sos[i].getData();
				ql.add(gark[0]);
				ql.add(gark[1]);
				ql.add(gark[2]);
				res.addLine(ql, gark[3]);
				this.names.put(gark[3], gark[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public HashMap<Integer, Integer[]> getSearchData() {
		HashMap<Integer, Integer[]> res = new HashMap<Integer, Integer[]>();

		res.put(new Integer(0), new Integer[] { new Integer(0) });

		return res;
	}

	public String[] getSearchDataIds() {
		String[] res = new String[] { "Name" };

		return res;
	}

	public boolean hasWindow() {
		return true;
	}

	public DataTable[] getRowInfo(String id) {
		DataTable[] res = new DataTable[3];

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Synonym");

		res[0] = new DataTable(columnsNames, "Synonyms");

		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Genes");

		res[1] = new DataTable(columnsNames2, "Genes");

		ArrayList<String> columnsNames3 = new ArrayList<String>();

		columnsNames3.add("Promoters");

		res[2] = new DataTable(columnsNames3, "Promoters");

		try {
			Statement stmt = super.table.getConnection().createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT alias FROM aliases WHERE class = 'tu' AND entity = "
							+ id);

			while (rs.next()) {
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[0].addLine(ql);
			}

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT gene.name FROM transcription_unit JOIN transcription_unit_gene "
							+ "ON transcription_unit.idtranscription_unit = transcription_unit_gene.transcription_unit_idtranscription_unit "
							+ "JOIN gene ON idgene = gene_idgene WHERE idtranscription_unit = "
							+ id);

			while (rs.next()) {
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[1].addLine(ql);
			}

			//stmt = super.table.getMySqlCredentials().createStatement();
			rs = stmt
					.executeQuery("SELECT promoter.name FROM transcription_unit JOIN "
							+ "transcription_unit_promoter ON transcription_unit.idtranscription_unit = "
							+ "transcription_unit_promoter.transcription_unit_idtranscription_unit JOIN promoter "
							+ "ON idpromoter = promoter_idpromoter WHERE idtranscription_unit = "
							+ id);

			while (rs.next()) {
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[2].addLine(ql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public String getName(String id) {
		return this.names.get(id);
	}

	public String getSingular() {
		return "TU: ";
	}
}