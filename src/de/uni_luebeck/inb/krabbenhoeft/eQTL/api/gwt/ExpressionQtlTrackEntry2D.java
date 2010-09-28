package de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ExpressionQtlTrackEntry2D implements IsSerializable {
	public String locusId, traitId;
	public double lodScore;
	public double lodScoreInMinMaxRange;
	public long positionXStart;
	public long positionXEnd;
	public long positionYStart;
	public long positionYEnd;

	@Override
	public String toString() {
		final String posStrX;
		if (positionXStart == positionXEnd)
			posStrX = "" + positionXStart;
		else
			posStrX = positionXStart + "-" + positionXEnd;
		final String posStrY;
		if (positionYStart == positionYEnd)
			posStrY = "" + positionYStart;
		else
			posStrY = positionYStart + "-" + positionYEnd;
		return "ExpressionQtl[lod=" + lodScore + ", locusId=" + locusId + ", traitId=" + traitId + ", x=" + posStrX + ", y=" + posStrY + "]";
	}

}