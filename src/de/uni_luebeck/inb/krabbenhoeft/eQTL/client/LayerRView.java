package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ProcessWithR;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ProcessWithRAsync;

public class LayerRView extends Composite {

	private static LayerRViewUiBinder uiBinder = GWT.create(LayerRViewUiBinder.class);

	interface LayerRViewUiBinder extends UiBinder<Widget, LayerRView> {
	}

	private final ProcessWithRAsync processWithR = GWT.create(ProcessWithR.class);

	@UiField
	TextArea consoleIn;

	@UiField
	TextArea consoleOut;

	private DataSetLayerOverview dataSetLayerOverview;

	public LayerRView(DataSetLayerOverview dataSetLayerOverview) {
		this.dataSetLayerOverview = dataSetLayerOverview;
		initWidget(uiBinder.createAndBindUi(this));
		consoleIn.setText("head(data)");
		onConsoleIn(null);
	}

	@UiHandler("consoleIn")
	void onConsoleIn(final ValueChangeEvent<String> e) {
		new AutoRetry<String>() {
			public void success(String result) {
				consoleOut.setText(result);
			}

			public void invoke(AsyncCallback<String> callback) {
				processWithR.callR(dataSetLayerOverview.layerKey, consoleIn.getText(), callback);
			}
		}.run();
	}

}
