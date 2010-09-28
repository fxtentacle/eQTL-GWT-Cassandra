package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ExpressionQtlTrackEntry implements IsSerializable {
	public String locusId, traitId;
	public double lodScore;
	public double lodScoreInMinMaxRange;
	public long positionStart;
	public long positionEnd;

	@Override
	public String toString() {
		final String posStr;
		if (positionStart == positionEnd)
			posStr = "" + positionStart;
		else
			posStr = positionStart + "-" + positionEnd;
		return "ExpressionQtl[lod=" + lodScore + ", locusId=" + locusId + ", traitId=" + traitId + ", position=" + posStr + "]";
	}

}