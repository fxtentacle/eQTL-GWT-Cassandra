package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.AutoRetry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.ClientMainWindow;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasObjectAutomationHandlers;

public abstract class GenomeDisplayTrack<T> implements HasObjectAutomationHandlers{
	private final String label;
	private final int maxBpPerPixel;

	public String chromosome = "1";
	int bottomYCacheForScroller; // DO NOT MODIFY !!
	GenomeDisplayScroller owner; // DO NOT MODIFY !!
	boolean transpose;
	String autoId;

	public static class Block<T> {
		public int bottomY;
		public String html;
		public T[] data;
	};

	public GenomeDisplayTrack(String label, int maxBpPerPixel) {
		super();
		this.label = label;
		this.maxBpPerPixel = maxBpPerPixel;
	}

	public abstract void fetchData(String chromosome, long fromBP, long toBP, AsyncCallback<T[]> callback);

	public Block<T> renderBlock512px(final long fromBP, long bpPerPixel, int topY, Block<T> b) {
		if (bpPerPixel >= maxBpPerPixel) {
			Block<T> block = new Block<T>();
			block.html = genBox("zoomInToView", 0, topY, 512, 15, "position: absolute; overflow: visile; font-size: 9px; ", "", "zoom in to view " + label);
			block.bottomY = topY + 15;
			block.data = null;
			return block;
		}

		final long toBP = fromBP + 512 * bpPerPixel;

		if (b == null) {
			// no cache yet, so initiate download
			b = new Block<T>();
			final Block<T> setme = b;

			new AutoRetry<T[]>() {
				public void success(T[] result) {
					setme.data = result;
					owner.scheduleUpdate();
				}

				public void invoke(AsyncCallback<T[]> callback) {
					fetchData(chromosome, fromBP, toBP, callback);
				}
			}.run();
		}

		if (b.data == null) {
			// no data? return b with html = null to notify scroller
			if (b.html != null) {
				b.html = genBox("networkFailure", 0, topY, 512, 20, "position: absolute;", "", "network failure");
				b.bottomY = topY + 20;
			}
			return b;
		}

		renderBlockInternal(topY, fromBP, toBP, bpPerPixel, b);
		return b;
	}

	protected abstract void renderBlockInternal(int topY, long fromBP, long toBP, long bpPerPixel, Block<T> b);

	// this will also be called on startup
	protected void changedChromosome() {
	};

	protected String genAutomation(long fromBP, int i) {
		final String click = "onclick=\"" + autoId + ".click('" + fromBP + "'," + i + ");\" ";
		final String over = "onmouseover=\"" + autoId + ".over('" + fromBP + "'," + i + ");\" ";
		final String out = "onmouseout=\"" + autoId + ".out('" + fromBP + "'," + i + ");\"";
		return click + over + out;
	}

	protected String genBox(String styleClas, int x, int y, int w, int h, String styleAdd, String auto, String displayLabel) {
		return genBox(styleClas, x, y, w, h, h, styleAdd, auto, displayLabel);
	}

	protected String genBox(String styleClas, int x, int y, int w, int h, int realH, String styleAdd, String auto, String displayLabel) {
		final String style;
		if (transpose)
			style = "-webkit-transform: rotate(90deg); -moz-transform: rotate(90deg); -moz-transform-origin: left bottom; -webkit-transform-origin: left bottom;" + styleAdd + " left: " + y
					+ "px; top: " + (x - realH) + "px; width: " + w + "px; height: " + h + "px;";
		else
			style = styleAdd + " left: " + x + "px; top: " + y + "px; width: " + w + "px; height: " + h + "px;";

		return "<div class=\"" + styleClas + "\" style=\"" + style + "\" " + auto + " >" + displayLabel + "</div>";

	}

	protected void updateContentLength(long newLength) {
		owner.updateContentLength(newLength);
	}
	
	
	Label currentMouseOver = null;
	public void onMouseClick(Object object) {
	}

	public void onMouseOut(Object object) {
		if (currentMouseOver == null)
			return;

		ClientMainWindow.notifyUserRem(currentMouseOver);
		currentMouseOver = null;
	}

	public void onMouseOver(Object object) {
		if (currentMouseOver != null)
			ClientMainWindow.notifyUserRem(currentMouseOver);
		currentMouseOver = ClientMainWindow.notifyUserAdd("Mouse over: " + object.toString());
	}
}
