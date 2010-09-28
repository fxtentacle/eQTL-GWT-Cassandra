package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DasTrackEntry implements IsSerializable {
	public String label;
	public int type;
	public long from, to;

	@Override
	public String toString() {
		return "DasTrackEntry [label=" + label + ", type=" + type + ", position=" + from + "-" + to + "]";
	}

}