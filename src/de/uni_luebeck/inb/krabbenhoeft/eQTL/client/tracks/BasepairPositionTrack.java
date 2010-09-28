/**
 * 
 */
package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.tracks;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack;

public class BasepairPositionTrack extends GenomeDisplayTrack<Boolean> {
	public BasepairPositionTrack() {
		super("basepairPosition", Integer.MAX_VALUE);
	}

	@Override
	public void fetchData(String chromosome, long fromBP, long toBP, AsyncCallback<Boolean[]> callback) {
		callback.onSuccess(new Boolean[0]);
	}

	@Override
	protected void renderBlockInternal(int topY, long fromBP, long toBP, long bpPerPixel, de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack.Block<Boolean> b) {
		b.bottomY = topY + 20;
		b.html = genBox("basepairPos", 0, topY, 512, 20, "position: absolute; overflow: visile; font-size: 11px; ", genAutomation(fromBP, 0), Long.toString(fromBP));
	}
}