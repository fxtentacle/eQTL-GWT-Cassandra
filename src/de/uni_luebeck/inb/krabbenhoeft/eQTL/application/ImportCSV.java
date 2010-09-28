package de.uni_luebeck.inb.krabbenhoeft.eQTL.application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.restlet.resource.ClientResource;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.CreateNewDataSetParameter;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.DataImporter;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.DataSetLine;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.rest.InsertIntoDataSetParameter;

public class ImportCSV {

	private static DataImporter connectToServer() {
		ClientResource cr = new ClientResource("http://127.0.0.1:8888/restlet/FAKEFAKE/data_import");
		DataImporter dataImporter = cr.wrap(DataImporter.class);
		return dataImporter;
	}

	public static void main(String[] args) throws IOException {
		final DataImporter dataImporter = connectToServer();
		final String[] dataSets = dataImporter.getDataSetNames();
		System.out.println("getDataSets() = {");
		for (String string : dataSets) {
			System.out.println(string);
		}
		System.out.println("}");

		Integer key = dataImporter.createNewDataSet(new CreateNewDataSetParameter("test", new String[] { "sex", "AUC", "severity" }));
		System.out.println("new key: " + key);

		// http://www.ncbi.nlm.nih.gov/sviewer/viewer.fcgi?tool=portal&db=nuccore&dopt=xml&sendto=on&log$=seqview&extrafeat=976&maxplex=0&val=NM_011660.2

		// SELECT Locus, Trait, LOD, Sex, AUC, Severity FROM `qtl` WHERE 1
		// select Locus, Trait, LOD, Sex, AUC, Severity, chromosome,
		// cMorgan_Min, cMorgan_Max, cMorgan_Peak, Accession from qtl inner join
		// IlluminaMouseChip ON ProbeId = trait
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("input.csv")));
		String line;
		Set<LocusPos> locusPositions = new HashSet<LocusPos>();
		Set<TraitAndAccession> traits = new HashSet<TraitAndAccession>();
		Set<Double> scores = new HashSet<Double>();
		Set<String> covariateValues = new HashSet<String>();

		while ((line = bufferedReader.readLine()) != null) {
			String[] parts = line.split(",");
			LocusPos locusPos = new LocusPos();
			locusPos.name = parts[0];
			locusPos.chromosome = parts[6];
			locusPos.min = Double.parseDouble(parts[7]);
			locusPos.max = Double.parseDouble(parts[8]);
			locusPos.peak = Double.parseDouble(parts[9]);
			locusPositions.add(locusPos);

			TraitAndAccession traitAndAccession = new TraitAndAccession();
			traitAndAccession.trait = parts[1];
			traitAndAccession.accession = parts[10];
			traits.add(traitAndAccession);

			scores.add(Double.parseDouble(parts[2]));
			covariateValues.add(parts[3]);
			covariateValues.add(parts[4]);
			covariateValues.add(parts[5]);
		}

		Double[] scoreArr = scores.toArray(new Double[0]);
		String[] covArr = covariateValues.toArray(new String[0]);
		Random random = new Random();

		int numberEmitted = 0;

		final int expectItems = locusPositions.size() * traits.size();
		System.out.println("Expecting: " + expectItems);

		int lastFlushId = 1;

		List<DataSetLine> dataSetLines = new ArrayList<DataSetLine>();
		for (LocusPos locusPos : locusPositions) {
			for (TraitAndAccession traitAndAccession : traits) {
				DataSetLine dataSetLine = new DataSetLine();
				dataSetLine.chromosome = locusPos.chromosome;
				final String auc = covArr[random.nextInt(covArr.length)];
				final String severity = covArr[random.nextInt(covArr.length)];
				final String sex = covArr[random.nextInt(covArr.length)];
				dataSetLine.covariates = new String[] { "AUC=" + auc, "severity=" + severity, "sex=" + sex };
				dataSetLine.geneBankDnaId = traitAndAccession.accession;
				dataSetLine.locusId = locusPos.name;
				dataSetLine.lodScore = scoreArr[random.nextInt(scoreArr.length)] + random.nextDouble();
				dataSetLine.positionMax = locusPos.max;
				dataSetLine.positionMin = locusPos.min;
				dataSetLine.positionPeak = locusPos.peak;
				dataSetLine.traitId = traitAndAccession.trait;
				dataSetLines.add(dataSetLine);
				numberEmitted++;

				if (dataSetLines.size() >= 5000) {
					final InsertIntoDataSetParameter parameters = new InsertIntoDataSetParameter(key, dataSetLines.toArray(new DataSetLine[0]), lastFlushId, false);
					lastFlushId += dataSetLines.size();
					storeWithRetry(dataImporter, parameters);
					dataSetLines.clear();
					System.out.println("Emitted so far: " + numberEmitted + " = " + ((double) numberEmitted * 100.0 / (double) expectItems) + "%");
				}
			}
		}

		// try {
		// executorService.shutdown();
		// executorService.awaitTermination(1, TimeUnit.DAYS);
		// Thread.sleep(5000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		storeWithRetry(dataImporter, new InsertIntoDataSetParameter(key, dataSetLines.toArray(new DataSetLine[0]), lastFlushId, true));
		bufferedReader.close();
	}

	private static void storeWithRetry(final DataImporter dataImporter, final InsertIntoDataSetParameter parameters) {
		dataImporter.insertIntoDataSet(parameters);
	}

	private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	private static ThreadLocal<DataImporter> threadLocalDataImporter = new ThreadLocal<DataImporter>();

	@SuppressWarnings("unused")
	private static void uploadAsynchronous(final InsertIntoDataSetParameter parameters) {
		executorService.execute(new Runnable() {
			public void run() {
				DataImporter dataImporter = threadLocalDataImporter.get();
				if (dataImporter == null) {
					dataImporter = connectToServer();
					threadLocalDataImporter.set(dataImporter);
				}
				storeWithRetry(dataImporter, parameters);
				System.out.println("Done: " + parameters.toString());
			}
		});
	}
}
