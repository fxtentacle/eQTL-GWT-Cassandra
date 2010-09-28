package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;

@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
@DefaultLocale("en_US")
public interface MyMessages extends Messages {
	@DefaultMessage("An error occurred while attempting to contact the server. Will try again in five seconds.")
	String autoRetryFailure(@Optional String localizedMessage);

	@DefaultMessage("Calculation in progress. Please wait...")
	String calculationInProgress();

	@DefaultMessage("Loading data set {0}")
	String selectedDataSet(String name);

	@DefaultMessage("Loading data set {1} with key {0}")
	String tableViewDummy(String dataSetKey, String dataSetName);

	@DefaultMessage("Please select a data set first")
	String selectDataSetFirst();

	@DefaultMessage("Calculation in progress. This panel will refresh to a table view as soon as calculations are complete.")
	String waitingForCalculationToComplete();
}
