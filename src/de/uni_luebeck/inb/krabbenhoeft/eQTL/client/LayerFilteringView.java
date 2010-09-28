package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataProcessing;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataProcessingAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetProcessorOverview;

public class LayerFilteringView extends CompositeWithChangeEvents implements HasSelectionHandlers<DataSetLayerOverview> {

	interface LayerFilteringViewUiBinder extends UiBinder<Widget, LayerFilteringView> {
	}

	private static LayerFilteringViewUiBinder uiBinder = GWT.create(LayerFilteringViewUiBinder.class);
	private final DataProcessingAsync dataProcessingService = GWT.create(DataProcessing.class);

	@UiField
	FlowPanel filterButtonList;

	private Integer dataSetKey;
	private Integer dataSetLayerKey;

	public LayerFilteringView(Integer indataSetKey, Integer indataSetLayerKey) {
		this.dataSetKey = indataSetKey;
		this.dataSetLayerKey = indataSetLayerKey;
		initWidget(uiBinder.createAndBindUi(this));
		new AutoRetry<DataSetProcessorOverview[]>() {
			public void success(DataSetProcessorOverview[] result) {
				fillProcessorPanel(result);
			}

			public void invoke(AsyncCallback<DataSetProcessorOverview[]> callback) {
				dataProcessingService.enumerateProcessors(dataSetLayerKey, callback);
			}
		}.run();
	}

	protected void fillProcessorPanel(DataSetProcessorOverview[] result) {
		for (final DataSetProcessorOverview processor : result) {
			filterButtonList.add(new Button(processor.name, new ClickHandler() {
				public void onClick(ClickEvent event) {
					clickProcessor(processor);
				}
			}));
		}
	}

	public HandlerRegistration addSelectionHandler(SelectionHandler<DataSetLayerOverview> handler) {
		return addSelectionHandlerImpl(handler);
	}

	private void clickProcessor(final DataSetProcessorOverview processor) {
		if (processor.key.equals("de.uni_luebeck.inb.krabbenhoeft.eQTL.server.processors.FilterByCategory")) {
			final PopupPanel popup = new PopupPanel();
			popup.setGlassEnabled(true);
			final FilterByCategoryPopup fbc = new FilterByCategoryPopup(processor.parameterDescription);
			fbc.addSelectionHandler(new SelectionHandler<String>() {
				public void onSelection(SelectionEvent<String> event) {
					popup.hide();
					applyProcessor(processor.key, event.getSelectedItem());
				}
			});
			popup.add(fbc);
			popup.show();
		} else
			applyProcessor(processor.key, "");
	}

	private void applyProcessor(final String key, final String param) {
		new AutoRetry<DataSetLayerOverview>() {
			public void success(DataSetLayerOverview result) {
				SelectionEvent.fire(LayerFilteringView.this, result);
			}

			public void invoke(AsyncCallback<DataSetLayerOverview> callback) {
				dataProcessingService.applyProcessor(dataSetKey, dataSetLayerKey, key, param, callback);
			}
		}.run();
	}
}
