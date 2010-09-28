package de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.GenomeRange;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.GenomeDisplayTrack.Block;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.client.scroller.RegisterForAutomation.HasAutomationHandlers;

public class GenomeDisplayScroller extends Widget implements HasValueChangeHandlers<GenomeRange>, HasSelectionHandlers<Long>, RequiresResize {
	private final boolean transpose;

	private final long blockCacheDistance = 4;
	// cache blocks up to
	// blockCacheDistance*pbPerPixel*512
	// BasePairs away

	private long contentLength;
	private long scrollingPosition;
	private long bpPerPixel;
	private Map<Long, GenomeDisplayBlock512px> fromBP2displayBlock = new HashMap<Long, GenomeDisplayBlock512px>();
	private List<GenomeDisplayBlock512px> displayBlocksCurrentlyVisible = new ArrayList<GenomeDisplayBlock512px>();
	private GenomeDisplayTrack<?> tracks[];

	private DivElement divCanvas;
	private DivElement divContents;
	private DivElement divScrollbar;
	private DivElement divScrollbarSlider;
	private DivElement divZoom;
	private DivElement divZoomLevel[] = new DivElement[20];

	private final native void setHoverHandlers(DivElement element) /*-{
		element.onmouseover = function(){ element.className='hover'; };
		element.onmouseout = function(){ element.className=''; };
	}-*/;

	public GenomeDisplayScroller(GenomeDisplayTrack<?> tracks[], String chromosome, boolean transpose, String baseId) {
		this.transpose = transpose;
		this.contentLength = 1;
		this.tracks = tracks;

		RegisterForAutomation.clearBaseId(baseId);
		for (int i = 0; i < tracks.length; i++) {
			GenomeDisplayTrack<?> genomeDisplayTrack = tracks[i];

			genomeDisplayTrack.owner = this;
			genomeDisplayTrack.transpose = transpose;
			genomeDisplayTrack.autoId = RegisterForAutomation.register(baseId, new AutomationHandlers(i));
		}

		Document document = Document.get();
		divCanvas = document.createDivElement();
		setElement(divCanvas);
		setStyleName("GenomeDisplayScroller");

		divContents = document.createDivElement();
		divContents.setClassName("GenomeDisplayScroller-contents");

		divScrollbar = document.createDivElement();
		divScrollbar.setClassName("GenomeDisplayScroller-scrollbar");
		divScrollbarSlider = document.createDivElement();
		divScrollbarSlider.setClassName("GenomeDisplayScroller-scrollbarSlider");
		divScrollbar.appendChild(divScrollbarSlider);

		divZoom = document.createDivElement();
		divZoom.setClassName("GenomeDisplayScroller-zoom");
		for (int i = 0; i < divZoomLevel.length; i++) {
			divZoomLevel[i] = document.createDivElement();
			setHoverHandlers(divZoomLevel[i]);
			divZoom.appendChild(divZoomLevel[i]);
		}

		divCanvas.appendChild(divContents);
		divCanvas.appendChild(divScrollbar);
		divCanvas.appendChild(divZoom);

		sinkEvents(Event.MOUSEEVENTS | Event.ONLOSECAPTURE | Event.ONCLICK);

		changeChromosome(chromosome);
		setZoomLevel(-1);
	}

	private String currentChromosome;

	public void changeChromosome(String chromosome) {
		currentChromosome = chromosome;
		for (GenomeDisplayTrack<?> genomeDisplayTrack : tracks) {
			genomeDisplayTrack.chromosome = chromosome;
			genomeDisplayTrack.changedChromosome();
		}
		updateContentLength(1);
	}

	public void resize(int sx, int sy) {
		resizeToOnMoveEnd = 0; // make sure there is no resize queued

		final int zoomWidth = 20;
		final int scrollbarHeight = 15;

		if (sx == 0 && sy < 0) {
			sx = transpose ? divCanvas.getClientHeight() : divCanvas.getClientWidth();
			sy = -sy + scrollbarHeight;
		}
		if (sx < 300)
			sx = 300;
		if (sy < 50)
			sy = 50;

		if (!transpose) {
			divContents.getStyle().setProperty("top", "0px");
			divContents.getStyle().setProperty("left", "0px");
			divContents.getStyle().setProperty("width", (sx - zoomWidth) + "px");
			divContents.getStyle().setProperty("height", (sy - scrollbarHeight) + "px");

			divScrollbar.getStyle().setProperty("top", (sy - scrollbarHeight) + "px");
			divScrollbar.getStyle().setProperty("left", "0px");
			divScrollbar.getStyle().setProperty("width", (sx - zoomWidth) + "px");
			divScrollbar.getStyle().setProperty("height", scrollbarHeight + "px");

			divZoom.getStyle().setProperty("top", "0px");
			divZoom.getStyle().setProperty("left", (sx - zoomWidth + 2) + "px");
			divZoom.getStyle().setProperty("width", zoomWidth + "px");
			divZoom.getStyle().setProperty("height", sy + "px");
		} else {
			divContents.getStyle().setProperty("left", "0px");
			divContents.getStyle().setProperty("top", "0px");
			divContents.getStyle().setProperty("height", (sx - zoomWidth) + "px");
			divContents.getStyle().setProperty("width", (sy - scrollbarHeight) + "px");

			divScrollbar.getStyle().setProperty("left", (sy - scrollbarHeight) + "px");
			divScrollbar.getStyle().setProperty("top", "0px");
			divScrollbar.getStyle().setProperty("height", (sx - zoomWidth) + "px");
			divScrollbar.getStyle().setProperty("width", scrollbarHeight + "px");

			divZoom.getStyle().setProperty("left", "0px");
			divZoom.getStyle().setProperty("top", (sx - zoomWidth + 2) + "px");
			divZoom.getStyle().setProperty("height", zoomWidth + "px");
			divZoom.getStyle().setProperty("width", sy + "px");
		}

		double scale = (double) sy / (double) (divZoomLevel.length);
		int lastPos = 0;
		for (int i = 0; i < divZoomLevel.length; i++) {
			int curPos = (int) Math.round(((double) i + 1) * scale);
			divZoomLevel[i].getStyle().setProperty("position", "absolute");
			divZoomLevel[i].getStyle().setProperty(transpose ? "left" : "top", lastPos + "px");
			divZoomLevel[i].getStyle().setProperty(transpose ? "width" : "height", (curPos - lastPos) + "px");
			divZoomLevel[i].getStyle().setProperty(transpose ? "height" : "width", zoomWidth + "px");
			lastPos = curPos;
		}
	}

	@Override
	protected void onLoad() {
		DeferredCommand.addCommand(new Command() {
			public void execute() {
				if (transpose)
					resize(divCanvas.getClientHeight(), divCanvas.getClientWidth());
				else
					resize(divCanvas.getClientWidth(), divCanvas.getClientHeight());
				updateBlocks();
			}
		});
	}

	private void updateBlocks() {
		int width = transpose ? divContents.getClientHeight() : divContents.getClientWidth();
		if (width == 0)
			return;

		long blockWidthInBP = 512 * bpPerPixel;
		long elementWidthInBP = width * bpPerPixel;

		if (scrollingPosition > contentLength - elementWidthInBP - 1)
			scrollingPosition = contentLength - elementWidthInBP - 1;
		if (scrollingPosition < 0)
			scrollingPosition = 0;

		int scrollbarWidth = transpose ? divScrollbar.getClientHeight() : divScrollbar.getClientWidth();
		divScrollbarSlider.getStyle().setProperty(transpose ? "top" : "left", scrollingPosition * scrollbarWidth / contentLength + "px");
		divScrollbarSlider.getStyle().setProperty(transpose ? "height" : "width", Math.max(5, elementWidthInBP * scrollbarWidth / contentLength) + "px");

		long lowBlock = (scrollingPosition / blockWidthInBP) * blockWidthInBP;
		long highBlock = ((scrollingPosition + elementWidthInBP) / blockWidthInBP) * blockWidthInBP;

		long cacheDist = blockCacheDistance * bpPerPixel * 512;
		Iterator<Map.Entry<Long, GenomeDisplayBlock512px>> cb = fromBP2displayBlock.entrySet().iterator();
		while (cb.hasNext()) {
			Map.Entry<Long, GenomeDisplayBlock512px> e = cb.next();
			if (e.getKey() + cacheDist < scrollingPosition || e.getKey() - cacheDist > scrollingPosition + elementWidthInBP) {
				// out of cache region => remove
				divContents.removeChild(e.getValue().contentsDiv);
				cb.remove();
			} else if (e.getKey() < lowBlock || e.getKey() > highBlock) {
				// out of visible region => hide
				e.getValue().contentsDiv.getStyle().setProperty("display", "none");
			}
		}

		displayBlocksCurrentlyVisible.clear();
		boolean needRecreate = false;
		// NOTE: it is important to walk right to left and set zIndex so content
		// can overflow correctly
		int zindex = 0;
		for (long i = highBlock; i >= lowBlock; i -= blockWidthInBP) {
			GenomeDisplayBlock512px cur;
			if (fromBP2displayBlock.containsKey(i)) {
				cur = fromBP2displayBlock.get(i);
			} else {
				cur = new GenomeDisplayBlock512px(i, bpPerPixel, tracks.length);
				needRecreate = true;
				fromBP2displayBlock.put(i, cur);
				divContents.appendChild(cur.contentsDiv);
			}
			displayBlocksCurrentlyVisible.add(cur);
			cur.contentsDiv.getStyle().setProperty(transpose ? "top" : "left", (cur.fromBP - scrollingPosition) / bpPerPixel + "px");
			cur.contentsDiv.getStyle().setProperty("display", "");
			cur.contentsDiv.getStyle().setProperty("zIndex", "" + zindex++);
		}

		if (needRecreate)
			updateTracks(false);

		ValueChangeEvent.fire(this, new GenomeRange(currentChromosome, scrollingPosition, scrollingPosition + elementWidthInBP));
	}

	private int resizeToOnMoveEnd = 0;

	@SuppressWarnings("unchecked")
	public void updateTracks(boolean bForceUpdate) {
		int topY = 0;
		boolean moveFollowing = bForceUpdate;
		for (int tid = 0; tid < tracks.length; tid++) {
			int maxBottomY = topY;
			for (GenomeDisplayBlock512px cur : displayBlocksCurrentlyVisible) {
				GenomeDisplayTrack.Block b = cur.blockCache[tid];
				if (b != null && b.html != null && !moveFollowing) {
					// if we have a cache and we are NOT moving down, use
					// cache
					maxBottomY = Math.max(maxBottomY, b.bottomY);
					continue;
				}

				b = tracks[tid].renderBlock512px(cur.fromBP, cur.bpPerPixel, topY, b);
				cur.blockCache[tid] = b;
				maxBottomY = Math.max(maxBottomY, b.bottomY);
			}

			// if the size of our blocks does not equal the cached blocks,
			// we need to recalculate all blocks below the current one
			if (maxBottomY != tracks[tid].bottomYCacheForScroller) {
				moveFollowing = true;
				tracks[tid].bottomYCacheForScroller = maxBottomY;
			}

			topY = maxBottomY;
		}

		if (moveFollowing) {
			if (!moving)
				resize(0, -topY);
			else
				resizeToOnMoveEnd = -topY;
		}

		for (GenomeDisplayBlock512px cur : displayBlocksCurrentlyVisible) {
			boolean blockLoading = false;
			String html = "";
			for (int tid = 0; tid < tracks.length; tid++) {
				if (cur.blockCache[tid].html != null)
					html += cur.blockCache[tid].html;
				else
					blockLoading = true;
			}
			if (blockLoading) {
				if (transpose)
					html += "<div style=\"position: absolute; z-index: 101; left: 0px; top: 0px; width: 20px; height: 512px; background-color: white; \"> loading ... </div>";
				else
					html += "<div style=\"position: absolute; z-index: 101; left: 0px; top: 0px; width: 512px; height: 20px; background-color: white; \"> loading ... </div>";
			}

			cur.setContent(topY, html);
		}
	}

	private boolean updateUseful = false;

	public void scheduleUpdate() {
		updateUseful = true;

		new Timer() {
			@Override
			public void run() {
				if (updateUseful) {
					updateUseful = false;
					updateTracks(true);
				}
			}
		}.schedule(100);
	}

	public void updateContentLength(long newLength) {
		contentLength = newLength;

		for (GenomeDisplayBlock512px cur : fromBP2displayBlock.values()) {
			divContents.removeChild(cur.contentsDiv);
		}
		fromBP2displayBlock.clear();
		updateBlocks();
	}

	public void setZoomLevel(long zoom) {
		if (bpPerPixel == zoom)
			return;

		bpPerPixel = zoom;
		if (bpPerPixel < 1 || bpPerPixel > 1000 * 1000)
			bpPerPixel = 8192 << 6;

		for (int i = 0; i < divZoomLevel.length; i++) {
			divZoomLevel[i].getStyle().setProperty("backgroundColor", (((long) 1) << i) == bpPerPixel ? "black" : "");
		}

		updateContentLength(contentLength);

		SelectionEvent.fire(this, zoom);
	}

	private boolean moving = false;
	private boolean dragging = false;
	private int movingStartX = 0;
	private long movingStartScroll;

	private void doScrollOrDrag(int curx) {
		if (dragging) {
			scrollingPosition = movingStartScroll + (movingStartX - curx) * bpPerPixel;
		} else {
			int w = transpose ? divScrollbar.getClientHeight() : divScrollbar.getClientWidth();
			scrollingPosition = movingStartScroll + (curx - movingStartX) * contentLength / w;
		}
	}

	public void scrollRelative(int offset) {
		scrollingPosition += offset * bpPerPixel;
		updateBlocks();
	}

	@Override
	public void onBrowserEvent(Event event) {
		Element target = DOM.eventGetTarget(event);
		switch (DOM.eventGetType(event)) {

		case Event.ONMOUSEDOWN: {
			movingStartX = transpose ? (DOM.eventGetClientY(event) - getAbsoluteTop()) : (DOM.eventGetClientX(event) - getAbsoluteLeft());
			movingStartScroll = scrollingPosition;
			moving = true;
			if (DOM.isOrHasChild((Element) divContents.cast(), target))
				dragging = true;
			else
				dragging = false;
			DOM.setCapture((Element) (dragging ? divContents : divScrollbar).cast());
			DOM.eventPreventDefault(event);
			break;
		}

		case Event.ONMOUSEUP: {
			if (moving) {
				// The order of these two lines is important. If we release
				// capture
				// first, then we might trigger an onLoseCapture event before we
				// set
				// isResizing to false.
				moving = false;
				if (resizeToOnMoveEnd != 0)
					resize(0, resizeToOnMoveEnd);
				DOM.releaseCapture((Element) (dragging ? divContents : divScrollbar).cast());
			}
			break;
		}

		case Event.ONMOUSEMOVE: {
			if (moving) {
				assert DOM.getCaptureElement() != null;
				doScrollOrDrag(transpose ? (DOM.eventGetClientY(event) - getAbsoluteTop()) : (DOM.eventGetClientX(event) - getAbsoluteLeft()));
				updateBlocks();
				DOM.eventPreventDefault(event);
			}
			break;
		}

			// IE automatically releases capture if the user switches windows,
			// so we
			// need to catch the event and stop resizing.
		case Event.ONLOSECAPTURE: {
			moving = false;
			if (resizeToOnMoveEnd != 0)
				resize(0, resizeToOnMoveEnd);
			break;
		}

		case Event.ONCLICK: {
			for (int i = 0; i < divZoomLevel.length; i++) {
				if (DOM.isOrHasChild((Element) divZoomLevel[i].cast(), target))
					setZoomLevel(((long) 1) << i);
			}
			break;
		}

		}
		super.onBrowserEvent(event);
	}

	public List<GenomeDisplayTrack.Block<?>> collectVisibleBlocksForTrack(int trackId) {
		List<GenomeDisplayTrack.Block<?>> ret = new ArrayList<GenomeDisplayTrack.Block<?>>();
		for (GenomeDisplayBlock512px cur : displayBlocksCurrentlyVisible) {
			Block<?> o = cur.blockCache[trackId];
			if (o == null)
				return null;
			ret.add(o);
		}
		return ret;
	}

	private HandlerManager handlers = new HandlerManager(null);

	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<GenomeRange> handler) {
		return handlers.addHandler(ValueChangeEvent.getType(), handler);
	}

	public HandlerRegistration addSelectionHandler(SelectionHandler<Long> handler) {
		return handlers.addHandler(SelectionEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

	public class AutomationHandlers implements HasAutomationHandlers {
		final int trackId;

		public AutomationHandlers(int i) {
			trackId = i;
		}

		private Object getObject(String fromBP, int itemIndex) {
			return fromBP2displayBlock.get(Long.parseLong(fromBP)).blockCache[trackId].data[itemIndex];
		}

		public void onMouseClick(String fromBP, int itemIndex) {
			tracks[trackId].onMouseClick(getObject(fromBP, itemIndex));
		}

		public void onMouseOut(String fromBP, int itemIndex) {
			tracks[trackId].onMouseOut(getObject(fromBP, itemIndex));
		}

		public void onMouseOver(String fromBP, int itemIndex) {
			tracks[trackId].onMouseOver(getObject(fromBP, itemIndex));
		}
	}

	public void onResize() {
		updateTracks(true);
	}
}
