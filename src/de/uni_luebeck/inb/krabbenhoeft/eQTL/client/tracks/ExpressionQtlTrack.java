/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ExpressionQtlTrackEntry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;

public class ExpressionQtlTrack extends GenomeDisplayTrack<ExpressionQtlTrackEntry> {
	final DataRetrievalAsync dataRetrievalAsync = (DataRetrievalAsync) GWT.create(DataRetrieval.class);
	private final int dataSetLayerKey;

	public ExpressionQtlTrack(String label, int dataSetLayerKey, String positionColumn) {
		super(label, Integer.MAX_VALUE);
		this.dataSetLayerKey = dataSetLayerKey;
		this.positionColumn = positionColumn;
	}

	private String positionColumn;

	public void setPositionColumn(String value) {
		positionColumn = value;
	}

	@Override
	public void fetchData(String chromosome, long fromBP, long toBP, AsyncCallback<ExpressionQtlTrackEntry[]> callback) {
		final GenomeRange genomeRange = new GenomeRange(chromosome, fromBP, toBP);
		dataRetrievalAsync.getTopEntriesForRange(dataSetLayerKey, positionColumn, genomeRange, callback);
	}

	@Override
	protected void renderBlockInternal(int topY, long fromBP, long toBP, long bpPerPixel, de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack.Block<ExpressionQtlTrackEntry> b) {
		b.bottomY = topY + 20;
		b.html = "";
		for (int i = 0; i < b.data.length; i++) {
			ExpressionQtlTrackEntry eqtl = b.data[i];

			// draw marker
			long posS = (eqtl.positionStart - fromBP) / bpPerPixel;
			if (posS < 0)
				posS = 0;
			else
				posS -= 2;

			long posE = (eqtl.positionEnd - fromBP) / bpPerPixel;
			if (posE > 512)
				posE = 512;
			else
				posE += 2;

			int red = (int) ((1 - eqtl.lodScoreInMinMaxRange) * 255.0f);
			int green = (int) (eqtl.lodScoreInMinMaxRange * 255.0f);
			final String style = "background-color: rgb(" + red + "," + green + ",0);";

			b.html += genBox("chr-eqtl-marker", (int) posS, topY, (int) (posE - posS), 10, style, genAutomation(fromBP, i), "");
		}
	}

	@Override
	public void onMouseClick(Object object) {
		ExpressionQtlTrackEntry e = (ExpressionQtlTrackEntry) object;
		Window.open("http://www.ensembl.org/Mus_musculus/Location/View?r=" + chromosome + ":" + (e.positionStart - 1000) + "-" + (e.positionEnd + 1000), "_blank", "");
	}
}
