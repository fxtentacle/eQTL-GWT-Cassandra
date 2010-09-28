package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GenomeRange implements IsSerializable {
	public String chromosome;
	public long fromBP, toBP;

	public GenomeRange() {
	}

	public GenomeRange(String chromosome, long fromBP, long toBP) {
		super();
		this.chromosome = chromosome;
		this.fromBP = fromBP;
		this.toBP = toBP;
	}

	@Override
	public String toString() {
		return "GenomeRange[" + chromosome + ":" + fromBP + "-" + toBP + "=> len=" + (toBP - fromBP) + " ]";
	}
}
