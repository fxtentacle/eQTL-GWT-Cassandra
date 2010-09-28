/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DasTrackEntry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.FetchDasTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.FetchDasTrackAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.AutoRetry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;

public class ChromosomeBandTrack extends GenomeDisplayTrack<Boolean> {
	DasTrackEntry[] cachedBands;

	public ChromosomeBandTrack() {
		super("bands", Integer.MAX_VALUE);
	}

	@Override
	public void changedChromosome() {
		cachedBands = null;

		final FetchDasTrackAsync ensemblAsync = (FetchDasTrackAsync) GWT.create(FetchDasTrack.class);
		new AutoRetry<DasTrackEntry[]>() {
			public void success(DasTrackEntry[] result) {
				cachedBands = result;

				long clen = 0;
				for (DasTrackEntry DasTrackEntry : result)
					clen = Math.max(clen, DasTrackEntry.to);
				updateContentLength(clen);
			}

			public void invoke(AsyncCallback<DasTrackEntry[]> callback) {
				ensemblAsync.getTrackForSegment("karyotype", chromosome, 0, 1000000000000L, callback);
			}
		}.run();
	}

	@Override
	public void fetchData(String chromosome, long fromBP, long toBP, AsyncCallback<Boolean[]> callback) {
		callback.onSuccess(new Boolean[0]);
	}

	@Override
	protected void renderBlockInternal(int topY, long fromBP, long toBP, long bpPerPixel, de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack.Block<Boolean> b) {
		final String col[] = new String[] { "chr-band-white", "chr-band-grey", "chr-band-black" };

		if (cachedBands == null) {
			b.html = null;
			return;
		}

		b.bottomY = topY + 30;
		b.html = "";
		for (int i = 0; i < cachedBands.length; i++) {
			DasTrackEntry band = cachedBands[i];

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

			boolean showlabel = band.from > fromBP && (band.to - band.from) / bpPerPixel > 15;
			String displabel = showlabel ? band.label : "";
			b.html += genBox(col[band.type - 1], (int) from, topY, (int) width, 21, 30, "", genAutomation(fromBP, i), displabel);
		}
	}
}