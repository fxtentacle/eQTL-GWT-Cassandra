package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetOverview;

public class DataSetLayerSelector extends Composite implements HasValueChangeHandlers<DataSetLayerOverview> {

	interface DataSetLayerViewUiBinder extends UiBinder<Widget, DataSetLayerSelector> {
	}

	private static DataSetLayerViewUiBinder uiBinder = GWT.create(DataSetLayerViewUiBinder.class);

	private final DataSetOverview dataSetOverview;
	private final MenuItem menuItemForLayers;

	@UiField
	Label key, name, dateCreated;

	@UiField
	Grid layerTable;

	public DataSetLayerSelector(DataSetOverview overview, MenuItem menuItem) {
		this.dataSetOverview = overview;
		this.menuItemForLayers = menuItem;
		initWidget(uiBinder.createAndBindUi(this));
		key.setText(Integer.toString(dataSetOverview.key));
		name.setText(dataSetOverview.name);
		dateCreated.setText(DateTimeFormat.getShortDateTimeFormat().format(dataSetOverview.dateCreated));

		initializeLayerTableWithHeader();
		updateLayerTable();
		updateLayerMenu();
	}

	private void updateLayerMenu() {
		menuItemForLayers.setVisible(true);
		MenuBar subMenu = new MenuBar(true);
		for (int i = 0; i < dataSetOverview.layers.length; i++) {
			DataSetLayerOverview layerOverview = dataSetOverview.layers[i];
			final int layerIndex = i;
			subMenu.addItem(layerOverview.layerKey + ": " + layerOverview.operationFromLastLayer, new Command() {
				public void execute() {
					selectLayer(layerIndex);
				}
			});
		}
		menuItemForLayers.setSubMenu(subMenu);
	}

	private void initializeLayerTableWithHeader() {
		layerTable.resize(1, 5);
		layerTable.getRowFormatter().addStyleName(0, "tableHeader");
		layerTable.setText(0, 0, "Key");
		layerTable.setText(0, 1, "Operation");
		layerTable.setText(0, 2, "Date created");
		layerTable.setText(0, 3, "Number of Rows");
		layerTable.setText(0, 4, "Columns");
	}

	private void updateLayerTable() {
		final int numberOfLayers = dataSetOverview.layers.length;
		layerTable.resize(1 + numberOfLayers, 5);
		for (int i = 0; i < numberOfLayers; i++) {
			DataSetLayerOverview layerOverview = dataSetOverview.layers[i];
			final Button button = new Button(Integer.toString(layerOverview.layerKey));
			final int layerIndex = i;
			button.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					selectLayer(layerIndex);
				}
			});
			layerTable.setWidget(1 + i, 0, button);
			layerTable.setText(1 + i, 1, layerOverview.operationFromLastLayer);
			layerTable.setText(1 + i, 2, DateTimeFormat.getShortDateTimeFormat().format(layerOverview.dateCreated));
			layerTable.setText(1 + i, 3, Integer.toString(layerOverview.numberOfDataRows));
			String columnString = "";
			for (int j = 0; j < layerOverview.columns.length; j++) {
				if (j > 0)
					columnString += ", ";
				columnString += layerOverview.columns[j];
			}
			layerTable.setText(1 + i, 4, columnString);
		}
	}

	public void addAndSelectNewLayer(DataSetLayerOverview dataSetLayerOverview) {
		final DataSetLayerOverview[] oldLayers = dataSetOverview.layers;
		dataSetOverview.layers = new DataSetLayerOverview[oldLayers.length + 1];
		System.arraycopy(oldLayers, 0, dataSetOverview.layers, 0, oldLayers.length);
		dataSetOverview.layers[oldLayers.length] = dataSetLayerOverview;
		updateLayerTable();
		updateLayerMenu();
		selectLayer(oldLayers.length);
	}

	private Integer currentlySelected = null;

	private void selectLayer(int layerIndex) {
		if (currentlySelected != null)
			layerTable.getRowFormatter().removeStyleName(currentlySelected, "selectedLayer");
		currentlySelected = layerIndex + 1;
		layerTable.getRowFormatter().addStyleName(currentlySelected, "selectedLayer");

		ValueChangeEvent.fire(this, dataSetOverview.layers[layerIndex]);
	}

	// value change event handling

	private HandlerManager handlers = new HandlerManager(null);

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DataSetLayerOverview> handler) {
		return handlers.addHandler(ValueChangeEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

}
