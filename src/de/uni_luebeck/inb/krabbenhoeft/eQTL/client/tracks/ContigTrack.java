/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DasTrackEntry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.FetchDasTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.FetchDasTrackAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;

public class ContigTrack extends GenomeDisplayTrack<DasTrackEntry> {

	public ContigTrack() {
		super("contigs", 16384);
	}

	final FetchDasTrackAsync ensemblAsync = (FetchDasTrackAsync) GWT.create(FetchDasTrack.class);

	@Override
	public void fetchData(String chromosome, long fromBP, long toBP, AsyncCallback<DasTrackEntry[]> callback) {
		ensemblAsync.getTrackForSegment("reference", chromosome, fromBP, toBP, callback);
	}

	@Override
	protected void renderBlockInternal(int topY, long fromBP, long toBP, long bpPerPixel, de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack.Block<DasTrackEntry> b) {
		final String col[] = new String[] { "chr-contig-0", "chr-contig-1" };

		b.bottomY = topY + 10;
		b.html = "";
		for (int i = 0; i < b.data.length; i++) {
			DasTrackEntry band = b.data[i];

			if (band.to < fromBP)
				continue;
			if (band.from > toBP)
				continue;
			long from = Math.max(band.from, fromBP) - fromBP;
			long to = Math.min(band.to, toBP) - fromBP;
			from /= bpPerPixel;
			to /= bpPerPixel;
			long width = to - from;
			if (width <= 0)
				continue;

			b.html += genBox(col[band.type], (int) from, topY, (int) width, 10, "", genAutomation(fromBP, i), "");
		}
	}

	@Override
	public void onMouseClick(Object object) {
		DasTrackEntry e = (DasTrackEntry) object;
		Window.open("http://www.ensembl.org/Mus_musculus/Location/View?r=" + chromosome + ":" + (e.from - 1000) + "-" + (e.to + 1000), "_blank", "");
	}
}