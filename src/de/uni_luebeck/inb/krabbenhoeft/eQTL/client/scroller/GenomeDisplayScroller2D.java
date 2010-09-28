package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrieval;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataRetrievalAsync;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DataSetLayerOverview;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ExpressionQtlTrackEntry2D;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.AutoRetry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.ClientMainWindow;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasAutomationHandlers;

public class GenomeDisplayScroller2D extends Widget implements HasValueChangeHandlers<Integer[]> {
	private final DataRetrievalAsync dataRetrievalService = GWT.create(DataRetrieval.class);
	private final DataSetLayerOverview dataSetLayerOverview;
	private final DivElement div;
	private final String autoId;

	public GenomeDisplayScroller2D(DataSetLayerOverview dataSetLayerOverview, String baseId) {
		super();
		this.dataSetLayerOverview = dataSetLayerOverview;
		this.div = Document.get().createDivElement();
		setElement(div);
		setStyleName("GenomeDisplayScroller2D");

		sinkEvents(Event.MOUSEEVENTS | Event.ONLOSECAPTURE);

		RegisterForAutomation.clearBaseId(baseId);
		autoId = RegisterForAutomation.register(baseId, new AutomationHandlers());
	}

	public class ScrollAxis {
		public int pixelLength;
		GenomeRange range;
		long rangeSize;
		long bpPerPixel;
		long bpPerPixelShift;

		public void update(GenomeRange newRange) {
			range = newRange;

			if (pixelLength == 0)
				return;

			final boolean scaleChanged;
			long len = range.toBP - range.fromBP;
			if (len != rangeSize) {
				rangeSize = len;
				scaleChanged = true;
			} else
				scaleChanged = false;

			if (scaleChanged) {
				double estimatedBpPerPixel = (double) len / (double) pixelLength;
				// to ensure < works also for =
				estimatedBpPerPixel *= 0.9;
				bpPerPixel = 1;
				bpPerPixelShift = 0;
				while (bpPerPixel < estimatedBpPerPixel) {
					bpPerPixel <<= 1;
					bpPerPixelShift++;
				}

				blockId2block.clear();
				div.setInnerHTML("");
			}

			updatePosition();
		}
	}

	public ScrollAxis rangeX = new ScrollAxis();
	public ScrollAxis rangeY = new ScrollAxis();
	public String selectedPositionColumnX;
	public String selectedPositionColumnY;

	String genAutomation(long id, int i) {
		final String click = "onclick=\"" + autoId + ".click('" + id + "'," + i + ");\" ";
		final String over = "onmouseover=\"" + autoId + ".over('" + id + "'," + i + ");\" ";
		final String out = "onmouseout=\"" + autoId + ".out('" + id + "'," + i + ");\"";
		return click + over + out;
	}

	String fillBlock(ExpressionQtlTrackEntry2D[] data, long fromBpX, long fromBpY, long toBpX, long toBpY, long bpPerPixelX, long bpPerPixelY) {
		long id = xy2id(fromBpX, fromBpY);

		String html = "";

		for (int i = 0; i < data.length; i++) {
			ExpressionQtlTrackEntry2D eqtl = data[i];

			long posSX = (eqtl.positionXStart - fromBpX) / bpPerPixelX;
			if (posSX < 0)
				posSX = 0;
			else
				posSX -= 2;
			long posXE = (eqtl.positionXEnd - fromBpX) / bpPerPixelX;
			if (posXE > 512)
				posXE = 512;
			else
				posXE += 2;

			long posSY = (eqtl.positionYStart - fromBpY) / bpPerPixelY;
			if (posSY < 0)
				posSY = 0;
			else
				posSY -= 2;
			long posYE = (eqtl.positionYEnd - fromBpY) / bpPerPixelY;
			if (posYE > 512)
				posYE = 512;
			else
				posYE += 2;

			int red = (int) ((1 - eqtl.lodScoreInMinMaxRange) * 255.0f);
			int green = (int) (eqtl.lodScoreInMinMaxRange * 255.0f);
			final String style = "background-color: rgb(" + red + "," + green + ",0); left:" + posSX + "px;top:" + posSY + "px;width:" + (posXE - posSX) + "px;height:" + (posYE - posSY) + "px; ";
			html += "<div class=\"map-eqtl-marker\" style=\"" + style + "\" " + genAutomation(id, i) + " ></div>";
		}

		return html;
	}

	class ScrollBlock {
		long fromBpX, fromBpY;
		DivElement contentsDiv;
		ExpressionQtlTrackEntry2D[] data;

		public ScrollBlock(long fromBpX2, long fromBpY2) {
			contentsDiv = Document.get().createDivElement();
			contentsDiv.setClassName("GenomeMapScrollBlock");
			contentsDiv.setInnerHTML("loading...");
			this.fromBpX = fromBpX2;
			this.fromBpY = fromBpY2;
		}
	}

	Map<Long, ScrollBlock> blockId2block = new HashMap<Long, ScrollBlock>();

	DivElement createBlock(final long fromBpX, final long fromBpY) {
		final long bpPerPixelX = rangeX.bpPerPixel;
		final long bpPerPixelY = rangeY.bpPerPixel;

		final ScrollBlock block = new ScrollBlock(fromBpX, fromBpY);
		new AutoRetry<ExpressionQtlTrackEntry2D[]>() {
			public void success(ExpressionQtlTrackEntry2D[] result) {
				block.data = result;
				block.contentsDiv.setInnerHTML(fillBlock(result, fromBpX, fromBpY, fromBpX + 512 * bpPerPixelX, fromBpY + 512 * bpPerPixelY, bpPerPixelX, bpPerPixelY));
				updatePosition();
			}

			public void invoke(AsyncCallback<ExpressionQtlTrackEntry2D[]> callback) {
				final GenomeRange blockRangeX = new GenomeRange(rangeX.range.chromosome, fromBpX, fromBpX + bpPerPixelX * 512);
				final GenomeRange blockRangeY = new GenomeRange(rangeY.range.chromosome, fromBpY, fromBpY + bpPerPixelY * 512);
				dataRetrievalService.getTopEntriesForArea(dataSetLayerOverview.layerKey, selectedPositionColumnX, blockRangeX, selectedPositionColumnY, blockRangeY, callback);
			}
		}.run();

		long id = xy2id(fromBpX, fromBpY);
		blockId2block.put(id, block);
		return block.contentsDiv;
	}

	DivElement getBlock(final long fromBpX, final long fromBpY) {
		long id = xy2id(fromBpX, fromBpY);
		if (!blockId2block.containsKey(id))
			return null;
		return blockId2block.get(id).contentsDiv;
	}

	private long xy2id(final long fromBpX, final long fromBpY) {
		return fromBpX << 32 | fromBpY;
	}

	void updatePosition() {
		if (rangeX.range == null || rangeY.range == null)
			return;

		long fromBlockX = rangeX.range.fromBP >> rangeX.bpPerPixelShift;
		fromBlockX -= fromBlockX % 512;
		long toX = rangeX.range.toBP;
		long stepX = 512 << rangeX.bpPerPixelShift;

		long fromBlockY = rangeY.range.fromBP >> rangeY.bpPerPixelShift;
		fromBlockY -= fromBlockY % 512;
		long toY = rangeY.range.toBP;
		long stepY = 512 << rangeY.bpPerPixelShift;

		final long cacheRangeX = 4 * stepX;
		final long cacheRangeY = 4 * stepY;

		final Iterator<Entry<Long, ScrollBlock>> cur = blockId2block.entrySet().iterator();
		while (cur.hasNext()) {
			final ScrollBlock block = cur.next().getValue();

			if ((block.fromBpX + stepX < rangeX.range.fromBP - cacheRangeX) || (block.fromBpX > rangeX.range.toBP + cacheRangeX) || (block.fromBpY + stepY < rangeY.range.fromBP - cacheRangeY)
					|| (block.fromBpY > rangeY.range.toBP + cacheRangeY)) {
				// out of cache range
				div.removeChild(block.contentsDiv);
				cur.remove();
			} else if ((block.fromBpX + stepX < rangeX.range.fromBP) || (block.fromBpX > rangeX.range.toBP) || (block.fromBpY + stepY < rangeY.range.fromBP) || (block.fromBpY > rangeY.range.toBP)) {
				// out of sight
				block.contentsDiv.getStyle().setProperty("display", "none");
			}
		}

		for (long x = fromBlockX << rangeX.bpPerPixelShift; x < toX; x += stepX) {
			final int offX = (int) ((x - rangeX.range.fromBP) >> rangeX.bpPerPixelShift);
			for (long y = fromBlockY << rangeY.bpPerPixelShift; y < toY; y += stepY) {
				final int offY = (int) ((y - rangeY.range.fromBP) >> rangeY.bpPerPixelShift);

				DivElement block = getBlock(x, y);
				if (block == null) {
					block = createBlock(x, y);
					div.appendChild(block);
				}
				block.getStyle().setProperty("left", offX + "px");
				block.getStyle().setProperty("top", offY + "px");
				block.getStyle().setProperty("display", "");
			}
		}
	}

	boolean moving = false;
	int scrollStartX, scrollStartY;

	private void doScrollOrDrag(int x, int y) {
		int offx = x - scrollStartX;
		scrollStartX = x;
		int offy = y - scrollStartY;
		scrollStartY = y;
		ValueChangeEvent.fire(this, new Integer[] { -offx, -offy });
	}

	@Override
	public void onBrowserEvent(Event event) {
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEDOWN: {
			scrollStartX = DOM.eventGetClientX(event) - getAbsoluteLeft();
			scrollStartY = DOM.eventGetClientY(event) - getAbsoluteTop();
			moving = true;
			DOM.setCapture((Element) div.cast());
			DOM.eventPreventDefault(event);
			break;
		}
		case Event.ONMOUSEUP: {
			if (moving) {
				moving = false;
				DOM.releaseCapture((Element) div.cast());
			}
			break;
		}
		case Event.ONMOUSEMOVE: {
			if (moving) {
				assert DOM.getCaptureElement() != null;
				doScrollOrDrag(DOM.eventGetClientX(event) - getAbsoluteLeft(), DOM.eventGetClientY(event) - getAbsoluteTop());
				DOM.eventPreventDefault(event);
			}
			break;
		}
		case Event.ONLOSECAPTURE: {
			moving = false;
			break;
		}
		}
		super.onBrowserEvent(event);
	}

	private HandlerManager handlers = new HandlerManager(null);

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer[]> handler) {
		return handlers.addHandler(ValueChangeEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

	public class AutomationHandlers implements HasAutomationHandlers {
		private ExpressionQtlTrackEntry2D getObject(String id, int itemIndex) {
			return blockId2block.get(Long.parseLong(id)).data[itemIndex];
		}

		public void onMouseClick(String fromBP, int itemIndex) {
			final ExpressionQtlTrackEntry2D e = getObject(fromBP, itemIndex);
			Window.open("http://www.ensembl.org/Mus_musculus/Location/View?r=" + rangeX.range.chromosome + ":" + (e.positionXStart - 1000) + "-" + (e.positionXEnd + 1000), "_blank", "");
			Window.open("http://www.ensembl.org/Mus_musculus/Location/View?r=" + rangeY.range.chromosome + ":" + (e.positionYStart - 1000) + "-" + (e.positionYEnd + 1000), "_blank", "");
		}

		Label currentMouseOver = null;

		public void onMouseOut(String fromBP, int itemIndex) {
			if (currentMouseOver == null)
				return;

			ClientMainWindow.notifyUserRem(currentMouseOver);
			currentMouseOver = null;
		}

		public void onMouseOver(String fromBP, int itemIndex) {
			if (currentMouseOver != null)
				ClientMainWindow.notifyUserRem(currentMouseOver);
			currentMouseOver = ClientMainWindow.notifyUserAdd("Mouse over: " + getObject(fromBP, itemIndex).toString());
		}
	}
}
