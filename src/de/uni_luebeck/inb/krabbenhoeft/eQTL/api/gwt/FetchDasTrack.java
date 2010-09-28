package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("fetchDasTrack")
public interface FetchDasTrack extends RemoteService {
	public String[] listAvailableTracks();
	public DasTrackEntry[] getTrackForSegment(String trackName, String chromosome, long fromBP, long toBP);
}
