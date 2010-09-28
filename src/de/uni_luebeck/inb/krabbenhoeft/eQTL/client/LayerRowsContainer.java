package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;

public class LayerRowsContainer extends CompositeWithChangeEvents implements HasSelectionHandlers<DataSetLayerOverview>, RequiresResize {

	interface LayerRowsContainerUiBinder extends UiBinder<Widget, LayerRowsContainer> {
	}

	private static LayerRowsContainerUiBinder uiBinder = GWT.create(LayerRowsContainerUiBinder.class);
	private final DataRetrievalAsync dataRetrievalService = GWT.create(DataRetrieval.class);
	private final MyMessages myMessages = (MyMessages) GWT.create(MyMessages.class);

	private DataSetLayerOverview dataSetLayerOverview;

	public LayerRowsContainer(DataSetLayerOverview dataSetLayerOverview) {
		this.dataSetLayerOverview = dataSetLayerOverview;
		initWidget(uiBinder.createAndBindUi(this));

		if (this.dataSetLayerOverview.numberOfDataRows == -1) {
			tabContents.add(new Label(myMessages.waitingForCalculationToComplete()));
			new AutoRetry<DataSetLayerOverview>() {
				public void success(DataSetLayerOverview result) {
					LayerRowsContainer.this.dataSetLayerOverview = result;
					enable();
				}

				public void invoke(AsyncCallback<DataSetLayerOverview> callback) {
					final DataSetLayerOverview lo = LayerRowsContainer.this.dataSetLayerOverview;
					dataRetrievalService.getLayerAfterCalculationCompletes(lo.dataSetKey, lo.layerKey, callback);
				}
			}.run(0); // refresh as fast as possible, this also hides msg
		} else
			enable();
	}

	private boolean enabled = false;

	private void enable() {
		if (enabled)
			return;

		enabled = true;
		tabRow.selectTab(tableTab);
		onClickTable(null);
	}

	@UiField
	DockLayoutPanel layout;

	@UiField
	LayoutPanel tabContents;

	@UiField
	TabButtonRow tabRow;

	@UiField
	TabButton tableTab;

	@UiHandler("tableTab")
	public void onClickTable(ClickEvent event) {
		if (!enabled)
			return;

		tabContents.clear();
		tabContents.add(new LayerTableView(dataSetLayerOverview));
	}

	@UiHandler("gaugeTab")
	public void onClickGauge(ClickEvent event) {
		if (!enabled)
			return;

		tabContents.clear();
		tabContents.add(new LayerGaugeView(dataSetLayerOverview));
	}

	@UiHandler("mapTab")
	public void onClickMap(ClickEvent event) {
		if (!enabled)
			return;

		tabContents.clear();
		tabContents.add(new LayerMapView(dataSetLayerOverview));
	}

	@UiHandler("filteringTab")
	public void onClickFiltering(ClickEvent event) {
		if (!enabled)
			return;

		tabContents.clear();
		final LayerFilteringView layerFilteringView = new LayerFilteringView(dataSetLayerOverview.dataSetKey, dataSetLayerOverview.layerKey);
		layerFilteringView.addSelectionHandler(new SelectionHandler<DataSetLayerOverview>() {
			public void onSelection(SelectionEvent<DataSetLayerOverview> event) {
				SelectionEvent.fire(LayerRowsContainer.this, event.getSelectedItem());
			}
		});
		tabContents.add(layerFilteringView);
	}

	@UiHandler("rTab")
	public void onClickR(ClickEvent event) {
		if (!enabled)
			return;

		tabContents.clear();
		tabContents.add(new LayerRView(dataSetLayerOverview));
	}

	public HandlerRegistration addSelectionHandler(SelectionHandler<DataSetLayerOverview> handler) {
		return addSelectionHandlerImpl(handler);
	}

	public void onResize() {
		layout.onResize();
	}
}
