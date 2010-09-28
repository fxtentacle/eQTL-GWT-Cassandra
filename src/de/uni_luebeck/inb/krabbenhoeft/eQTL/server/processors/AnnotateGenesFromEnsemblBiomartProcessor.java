/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mortbay.jetty.HttpException;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.Category;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer.ColumType;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CreateAndModifyEntities;

public class AnnotateGenesFromEnsemblBiomartProcessor extends BaseProcessorImplementation {
	@Override
	public int getPreferredNumberOfParallelRunningProcessors() {
		return 2;
	}

	public void addNewColumns(List<ColumnForDataSetLayer> columns) {
		columns.add(new ColumnForDataSetLayer("ensemblGeneId", ColumType.Name));
		columns.add(new ColumnForDataSetLayer("ensemblGeneName", ColumType.Name));
		columns.add(new ColumnForDataSetLayer("ensemblTranscriptId", ColumType.Name));
		columns.add(new ColumnForDataSetLayer("geneChromosome", ColumType.Category));
		final ColumnForDataSetLayer col = new ColumnForDataSetLayer("geneStartBP", ColumType.Location);
		col.setIndexme(true);
		col.setIndexChromosomeField("geneChromosome");
		col.setIndexRangeEndField("geneEndBP");
		columns.add(col);
		columns.add(new ColumnForDataSetLayer("geneEndBP", ColumType.Location));
	}

	private static Map<String, String[]> accession2parts = Collections.synchronizedMap(new HashMap<String, String[]>());

	public int doWork(CreateAndModifyEntities modifier, Iterator<HajoEntity> iter) {
		final List<HajoEntity> entities = new ArrayList<HajoEntity>();

		final Set<String> geneBankDnaIdStrings = new HashSet<String>();
		while (iter.hasNext()) {
			final HajoEntity source = iter.next();
			geneBankDnaIdStrings.add(source.getName("geneBankDnaId"));
			entities.add(source);
		}

		StringBuilder request = new StringBuilder();
		request.append("<Query virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"0\" count = \"\" datasetConfigVersion = \"0.6\" >"
				+ "<Dataset name = \"mmusculus_gene_ensembl\" interface = \"default\" >" + "<Filter name = \"refseq_dna\" value = \"");

		boolean needToAsk = false;
		for (String string : geneBankDnaIdStrings) {
			string = accessionDropDot(string);
			if (accession2parts.containsKey(string))
				continue;
			request.append(string);
			request.append(",");
			needToAsk = true;
		}

		request.append("\"/>" + "<Attribute name = \"refseq_dna\" />" + "<Attribute name = \"ensembl_gene_id\" />" + "<Attribute name = \"ensembl_transcript_id\" />"
				+ "<Attribute name = \"external_gene_id\" />" + "<Attribute name = \"chromosome_name\" />" + "<Attribute name = \"start_position\" />" + "<Attribute name = \"end_position\" />"
				+ "</Dataset></Query>");

		if (needToAsk) {
			String response = null;
			Exception exception = null;

			for (int i = 0; i < 5; i++) {
				try {
					final String biomartUrl = "http://www.ensembl.org/biomart/martservice";
					final String payload = "query=" + URLEncoder.encode(request.toString(), "UTF-8");
					final HttpURLConnection connection = (HttpURLConnection) new URL(biomartUrl).openConnection();
					connection.setDoOutput(true);
					final OutputStream outputStream = connection.getOutputStream();
					outputStream.write(payload.getBytes());
					outputStream.close();

					if (connection.getResponseCode() != 200)
						throw new HttpException(connection.getResponseCode(), connection.getResponseMessage());

					byte[] data = new byte[1024];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					final InputStream inputStream = connection.getInputStream();
					while (true) {
						int nr = inputStream.read(data);
						if (nr == -1)
							break;
						baos.write(data, 0, nr);
					}
					inputStream.close();

					response = baos.toString();
					break;
				} catch (MalformedURLException e) {
					exception = e;
				} catch (IOException e) {
					exception = e;
				}
			}

			if (response == null)
				throw new RuntimeException("Fetching results from ensembl failed!", exception);

			String[] lines = response.split("\n");
			for (String line : lines) {
				String[] parts = line.split("\t");
				accession2parts.put(parts[0], parts);
			}
		}

		int count = 0;
		for (HajoEntity target : entities) {
			String[] parts = accession2parts.get(accessionDropDot(target.getName("geneBankDnaId")));
			if (parts != null) {
				target.setName("ensemblGeneId", parts[1]);
				target.setName("ensemblTranscriptId", parts[2]);
				target.setName("ensemblGeneName", parts[3]);
				target.setCategory("geneChromosome", Category.wrap(parts[4]));
				target.setLocation("geneStartBP", Integer.parseInt(parts[5]));
				target.setLocation("geneEndBP", Integer.parseInt(parts[6]));
			} else {
				target.setName("ensemblGeneId", "MISSING");
				target.setName("ensemblTranscriptId", "MISSING");
				target.setName("ensemblGeneName", "MISSING");
				target.setCategory("geneChromosome", Category.wrap("MISSING"));
				target.setLocation("geneStartBP", -1);
				target.setLocation("geneEndBP", -1);
			}
			modifier.put(target);
			count++;
		}
		return count;
	}

	private String accessionDropDot(String string) {
		final int dotIndex = string.indexOf('.');
		if (dotIndex != -1)
			string = string.substring(0, dotIndex);
		return string;
	}
}