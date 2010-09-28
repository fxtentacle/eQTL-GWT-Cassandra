package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayScroller;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayScroller2D;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.BasepairPositionTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ChromosomeBandTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ContigTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.ExpressionQtlTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks.TranscriptTrack;

public class LayerMapView extends Composite implements RequiresResize {

	interface LayerMapViewUiBinder extends UiBinder<Widget, LayerMapView> {
	}

	private static LayerMapViewUiBinder uiBinder = GWT.create(LayerMapViewUiBinder.class);

	private final ExpressionQtlTrack expressionQtlTrackX;
	private final ExpressionQtlTrack expressionQtlTrackY;

	@UiField
	LayoutPanel layout;

	@UiField
	ListBox chromosome;

	@UiField
	ListBox positionColumnX;
	private String selectedPositionColumnX;

	@UiField
	ListBox positionColumnY;
	private String selectedPositionColumnY;

	@UiField(provided = true)
	final GenomeDisplayScroller scrollerX;

	@UiField(provided = true)
	final GenomeDisplayScroller scrollerY;

	@UiField(provided = true)
	final GenomeDisplayScroller2D contents;

	public LayerMapView(DataSetLayerOverview dataSetLayerOverview) {
		String[] chromosomes = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "X" };
		List<String> validNames = Arrays.asList(new String[] { "positionPeakBP", "geneStartBP" });
		selectedPositionColumnX = validNames.get(0);
		selectedPositionColumnY = validNames.get(1);

		expressionQtlTrackX = new ExpressionQtlTrack("eQTL", dataSetLayerOverview.layerKey, selectedPositionColumnX);
		expressionQtlTrackY = new ExpressionQtlTrack("eQTL", dataSetLayerOverview.layerKey, selectedPositionColumnY);

		final GenomeDisplayTrack<?>[] tracksX = new GenomeDisplayTrack[] { new BasepairPositionTrack(), new ChromosomeBandTrack(), new ContigTrack(), new TranscriptTrack(), expressionQtlTrackX };
		this.scrollerX = new GenomeDisplayScroller(tracksX, chromosomes[0], false, "scrollX");

		final GenomeDisplayTrack<?>[] tracksY = new GenomeDisplayTrack[] { new BasepairPositionTrack(), new ChromosomeBandTrack(), new ContigTrack(), new TranscriptTrack(), expressionQtlTrackY };
		this.scrollerY = new GenomeDisplayScroller(tracksY, chromosomes[0], true, "scrollY");

		contents = new GenomeDisplayScroller2D(dataSetLayerOverview, "scroll2d");
		contents.selectedPositionColumnX = selectedPositionColumnX;
		contents.selectedPositionColumnY = selectedPositionColumnY;

		initWidget(uiBinder.createAndBindUi(this));

		for (String string : chromosomes) {
			if (string.equals("X"))
				chromosome.addItem("X Chromosome", string);
			else
				chromosome.addItem("Chromosome " + string, string);
		}

		for (String columName : dataSetLayerOverview.columns) {
			if (validNames.contains(columName)) {
				positionColumnX.addItem(columName);
				positionColumnY.addItem(columName);
			}
		}
		positionColumnX.setSelectedIndex(0);
		positionColumnY.setSelectedIndex(1);
	}

	@UiHandler("chromosome")
	public void onChangeChromosome(ChangeEvent event) {
		String chr = chromosome.getValue(chromosome.getSelectedIndex());
		scrollerX.changeChromosome(chr);
		scrollerY.changeChromosome(chr);
	}

	@UiHandler("positionColumnX")
	public void onChangePositionColumnX(ChangeEvent event) {
		selectedPositionColumnX = positionColumnX.getValue(positionColumnX.getSelectedIndex());
		contents.selectedPositionColumnX = selectedPositionColumnX;
		expressionQtlTrackX.setPositionColumn(selectedPositionColumnX);
		// force refresh
		onChangeChromosome(null);
	}

	@UiHandler("positionColumnY")
	public void onChangePositionColumnY(ChangeEvent event) {
		selectedPositionColumnY = positionColumnX.getValue(positionColumnY.getSelectedIndex());
		contents.selectedPositionColumnY = selectedPositionColumnY;
		expressionQtlTrackY.setPositionColumn(selectedPositionColumnY);
		// force refresh
		onChangeChromosome(null);
	}

	@UiHandler("scrollerX")
	public void onScrollX(ValueChangeEvent<GenomeRange> event) {
		contents.rangeX.pixelLength = contents.getElement().getClientWidth();
		contents.rangeX.update(event.getValue());
	}

	@UiHandler("scrollerY")
	public void onScrollY(ValueChangeEvent<GenomeRange> event) {
		contents.rangeY.pixelLength = contents.getElement().getClientHeight();
		contents.rangeY.update(event.getValue());
	}

	@UiHandler("contents")
	public void onScroll2D(ValueChangeEvent<Integer[]> event) {
		final Integer[] off = event.getValue();
		if (off[0] != 0)
			scrollerX.scrollRelative(off[0]);
		if (off[1] != 0)
			scrollerY.scrollRelative(off[1]);
	}

	@UiHandler("scrollerX")
	public void onZoomX(SelectionEvent<Long> event) {
		scrollerY.setZoomLevel(event.getSelectedItem());
	}

	@UiHandler("scrollerY")
	public void onZoomY(SelectionEvent<Long> event) {
		scrollerX.setZoomLevel(event.getSelectedItem());
	}

	public void onResize() {
		layout.onResize();
	}
}
