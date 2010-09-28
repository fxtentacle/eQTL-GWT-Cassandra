package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayScroller;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.BasepairPositionTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ChromosomeBandTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ContigTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ExpressionQtlTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.TranscriptTrack;

public class LayerGaugeView extends Composite {

	interface LayerGaugeViewUiBinder extends UiBinder<Widget, LayerGaugeView> {
	}

	private static LayerGaugeViewUiBinder uiBinder = GWT.create(LayerGaugeViewUiBinder.class);

	private final DataRetrievalAsync dataRetrievalService = GWT.create(DataRetrieval.class);
	private final DataSetLayerOverview dataSetLayerOverview;
	private final ExpressionQtlTrack expressionQtlTrack;

	@UiField
	ListBox chromosome;

	@UiField
	ListBox positionColumn;
	private String selectedPositionColumn;

	@UiField(provided = true)
	final GenomeDisplayScroller scroller;

	@UiField
	Grid table;

	static int itemsPerPage = 25;

	public LayerGaugeView(DataSetLayerOverview dataSetLayerOverview) {
		this.dataSetLayerOverview = dataSetLayerOverview;

		String[] chromosomes = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X" };
		List<String> validNames = Arrays.asList(new String[] { "positionPeakBP", "geneStartBP" });
		selectedPositionColumn = validNames.get(0);

		expressionQtlTrack = new ExpressionQtlTrack("eQTL", dataSetLayerOverview.layerKey, selectedPositionColumn);
		final GenomeDisplayTrack<?>[] tracks = new GenomeDisplayTrack[] { new BasepairPositionTrack(), new ChromosomeBandTrack(), new ContigTrack(), new TranscriptTrack(), expressionQtlTrack };
		this.scroller = new GenomeDisplayScroller(tracks, chromosomes[0], false, "gauge");

		initWidget(uiBinder.createAndBindUi(this));

		for (String string : chromosomes) {
			if (string.equals("X"))
				chromosome.addItem("X Chromosome", string);
			else
				chromosome.addItem("Chromosome " + string, string);
		}

		for (String columName : dataSetLayerOverview.columns) {
			if (validNames.contains(columName))
				positionColumn.addItem(columName);
		}

		table.resize(1, dataSetLayerOverview.columns.length);
		for (int i = 0; i < dataSetLayerOverview.columns.length; i++) {
			table.setText(0, i, dataSetLayerOverview.columns[i]);
		}
		table.getRowFormatter().addStyleName(0, "tableHeader");
	}

	@UiHandler("chromosome")
	public void onChangeChromosome(ChangeEvent event) {
		String chr = chromosome.getValue(chromosome.getSelectedIndex());
		scroller.changeChromosome(chr);
		table.setVisible(false);
	}

	@UiHandler("positionColumn")
	public void onChangePositionColumn(ChangeEvent event) {
		selectedPositionColumn = positionColumn.getValue(positionColumn.getSelectedIndex());
		expressionQtlTrack.setPositionColumn(selectedPositionColumn);
		// force refresh
		onChangeChromosome(null);
	}

	private GenomeRange tableRange = null;
	private Timer updateOnTimeout = new Timer() {
		public void run() {
			reloadTable();
		}
	};

	@UiHandler("scroller")
	public void onScroll(ValueChangeEvent<GenomeRange> event) {
		table.setVisible(false);
		updateOnTimeout.cancel();
		tableRange = event.getValue();
		updateOnTimeout.schedule(100);
	}

	public void reloadTable() {
		table.setVisible(false);

		new AutoRetry<String[][]>() {
			public void success(String[][] result) {
				table.resizeRows(1 + result.length);
				for (int i = 0; i < result.length; i++) {
					for (int j = 0; j < result[i].length; j++) {
						table.setText(1 + i, j, result[i][j]);
					}
				}
				table.setVisible(true);
			}

			public void invoke(AsyncCallback<String[][]> callback) {
				dataRetrievalService.getTopRowsForRange(dataSetLayerOverview.layerKey, selectedPositionColumn, tableRange, callback);
			}
		}.run();
	}
}
