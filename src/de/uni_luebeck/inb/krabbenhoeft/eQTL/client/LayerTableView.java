package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;

public class LayerTableView extends Composite {

	interface LayerTableViewUiBinder extends UiBinder<Widget, LayerTableView> {
	}

	private static LayerTableViewUiBinder uiBinder = GWT.create(LayerTableViewUiBinder.class);

	// private final MyMessages myMessages = (MyMessages)
	// GWT.create(MyMessages.class);
	private final DataRetrievalAsync dataRetrievalService = GWT.create(DataRetrieval.class);
	private final DataSetLayerOverview dataSetLayerOverview;

	@UiField
	Button prevA, prevB, nextA, nextB;

	@UiField
	TextBox pageA, pageB;

	@UiField
	Grid table;

	int currentPage;
	int lastPageIndex;

	static int itemsPerPage = 25;

	public LayerTableView(DataSetLayerOverview dataSetLayerOverview) {
		this.dataSetLayerOverview = dataSetLayerOverview;
		initWidget(uiBinder.createAndBindUi(this));

		lastPageIndex = (int) Math.floor((double) dataSetLayerOverview.numberOfDataRows / (double) itemsPerPage);
		if (lastPageIndex > 49)
			lastPageIndex = 49;

		table.resize(1, dataSetLayerOverview.columns.length);
		for (int i = 0; i < dataSetLayerOverview.columns.length; i++) {
			table.setText(0, i, dataSetLayerOverview.columns[i]);
		}
		table.getRowFormatter().addStyleName(0, "tableHeader");
		// refresh pagination
		currentPage = -1;
		setCurrentPage(0);
	}

	void setCurrentPage(int newPage) {
		if (newPage < 0)
			newPage = 0;
		if (newPage > lastPageIndex)
			newPage = lastPageIndex;

		if (newPage == currentPage)
			return;

		currentPage = newPage;
		final String text = Integer.toString(currentPage + 1);
		pageA.setText(text);
		pageB.setText(text);

		boolean enablePrev = currentPage > 0;
		prevA.setEnabled(enablePrev);
		prevB.setEnabled(enablePrev);

		boolean enableNext = currentPage < lastPageIndex;
		nextA.setEnabled(enableNext);
		nextB.setEnabled(enableNext);

		table.resizeRows(1);

		new AutoRetry<String[][]>() {
			public void success(String[][] result) {
				table.resizeRows(1 + result.length);
				for (int i = 0; i < result.length; i++) {
					for (int j = 0; j < result[i].length; j++) {
						table.setText(1 + i, j, result[i][j]);
					}
				}
			}

			public void invoke(AsyncCallback<String[][]> callback) {
				dataRetrievalService.getLayerRows(dataSetLayerOverview.layerKey, currentPage * itemsPerPage, itemsPerPage, callback);
			}
		}.run();
	}

	@UiHandler( { "prevA", "prevB" })
	void onClickPrev(ClickEvent e) {
		setCurrentPage(currentPage - 1);
	}

	@UiHandler( { "nextA", "nextB" })
	void onClickNext(ClickEvent e) {
		setCurrentPage(currentPage + 1);
	}

	@UiHandler("pageA")
	void onChangePageA(BlurEvent e) {
		setCurrentPage(Integer.parseInt(pageA.getText()) - 1);
	}

	@UiHandler("pageB")
	void onChangePageB(BlurEvent e) {
		setCurrentPage(Integer.parseInt(pageB.getText()) - 1);
	}

}
