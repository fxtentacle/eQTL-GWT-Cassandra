package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetOverview;

public class ClientMainWindow implements EntryPoint {

	public static void notifyUser(String message, int timeout) {
		final Label label = notifyUserAdd(message);
		new Timer() {
			public void run() {
				notifyUserRem(label);
			}
		}.schedule(timeout);
	}

	public static Label notifyUserAdd(String message) {
		final RootPanel messagePanel = RootPanel.get("messages");
		final Label label = new Label(message, true);
		messagePanel.add(label);
		return label;
	}

	public static void notifyUserRem(Label label) {
		final RootPanel messagePanel = RootPanel.get("messages");
		messagePanel.remove(label);
	}

	interface ClientMainWindowUiBinder extends UiBinder<Widget, ClientMainWindow> {
	}

	private static ClientMainWindowUiBinder uiBinder = GWT.create(ClientMainWindowUiBinder.class);
	private final DataRetrievalAsync dataRetrievalService = GWT.create(DataRetrieval.class);
	private final MyMessages myMessages = (MyMessages) GWT.create(MyMessages.class);

	private DataSetOverview dataSetOverview;

	@UiField
	MenuBar loadMenu;

	@UiField
	MenuItem menuItemForLayers;

	public void onModuleLoad() {
		if (RootPanel.get("content").getElement().getElementsByTagName("a").getLength() > 0)
			return; // user is seeing login error

		RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));

		new AutoRetry<Map<Integer, String>>() {
			public void success(Map<Integer, String> result) {
				for (final Map.Entry<Integer, String> dataSet : result.entrySet()) {
					MenuItem menuItem = new MenuItem(dataSet.getValue(), new Command() {
						public void execute() {
							selectDataSet(dataSet.getKey(), dataSet.getValue());
						}
					});
					loadMenu.addItem(menuItem);
				}
			}

			public void invoke(AsyncCallback<Map<Integer, String>> callback) {
				dataRetrievalService.enumerateDataSets(callback);
			}
		}.run();
	}

	public void selectDataSet(final Integer key, String name) {
		dataSetOverview = null;
		menuItemForLayers.setVisible(false);

		ClientMainWindow.notifyUser(myMessages.selectedDataSet(name), 1000);

		new AutoRetry<DataSetOverview>() {
			public void success(DataSetOverview result) {
				dataSetOverview = result;
				updateDataSetViews();
			}

			public void invoke(AsyncCallback<DataSetOverview> callback) {
				dataRetrievalService.getOverview(key, callback);
			}
		}.run();
	}

	@UiField
	LayoutPanel layerViewContainer, layerRowsContainer;

	public void updateDataSetViews() {
		layerViewContainer.clear();
		layerRowsContainer.clear();

		final DataSetLayerSelector dataSetLayerSelector = new DataSetLayerSelector(dataSetOverview, menuItemForLayers);
		dataSetLayerSelector.addValueChangeHandler(new ValueChangeHandler<DataSetLayerOverview>() {
			public void onValueChange(ValueChangeEvent<DataSetLayerOverview> event) {
				selectDataSetLayer(dataSetLayerSelector, event.getValue());
			}
		});
		layerViewContainer.add(dataSetLayerSelector);
	}

	private void selectDataSetLayer(final DataSetLayerSelector dataSetLayerSelector, DataSetLayerOverview selectedLayer) {
		layerRowsContainer.clear();
		final LayerRowsContainer dataSetLayerContents = new LayerRowsContainer(selectedLayer);
		dataSetLayerContents.addSelectionHandler(new SelectionHandler<DataSetLayerOverview>() {
			public void onSelection(SelectionEvent<DataSetLayerOverview> event) {
				dataSetLayerSelector.addAndSelectNewLayer(event.getSelectedItem());
			}
		});
		layerRowsContainer.add(dataSetLayerContents);
	}

}
