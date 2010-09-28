package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;

public class GenomeDisplayBlock512px {
	public DivElement contentsDiv;

	public long fromBP, bpPerPixel, toBP;

	public GenomeDisplayTrack.Block<?> blockCache[];

	public GenomeDisplayBlock512px(long fromBP, long bpPerPixel, int numberOfTracks) {
		contentsDiv = Document.get().createDivElement();
		contentsDiv.setClassName("GenomeDisplayBlock");
		contentsDiv.getStyle().setProperty("width", "512px");
		contentsDiv.getStyle().setProperty("height", "512px");

		this.fromBP = fromBP;
		this.bpPerPixel = bpPerPixel;
		this.toBP = fromBP + 512 * bpPerPixel;

		this.blockCache = new GenomeDisplayTrack.Block[numberOfTracks];
	}

	public void setContent(int height, String html) {
		contentsDiv.setInnerHTML(html);
	}
}
