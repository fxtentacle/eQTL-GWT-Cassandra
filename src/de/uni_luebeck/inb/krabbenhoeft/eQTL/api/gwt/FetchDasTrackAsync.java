package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FetchDasTrackAsync {
	void listAvailableTracks(AsyncCallback<String[]> callback);

	void getTrackForSegment(String trackName, String chromosome, long fromBP, long toBP, AsyncCallback<DasTrackEntry[]> callback);
}
