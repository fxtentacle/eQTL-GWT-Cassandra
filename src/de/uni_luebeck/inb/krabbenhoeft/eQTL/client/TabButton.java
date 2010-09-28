package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;

public class TabButton extends SimplePanel {

	private Element inner;

	public TabButton() {
		super(Document.get().createDivElement());
		getElement().appendChild(inner = Document.get().createDivElement());

		setStyleName("tabButton");
		inner.setClassName("tabButtonInner");

		getElement().getStyle().setFloat(Style.Float.LEFT);
	}

	public HandlerRegistration addClickHandler(ClickHandler handler) {
		return addDomHandler(handler, ClickEvent.getType());
	}

	public void setSelected(boolean selected) {
		if (selected) {
			addStyleDependentName("selected");
		} else {
			removeStyleDependentName("selected");
		}
	}

	@Override
	protected com.google.gwt.user.client.Element getContainerElement() {
		return inner.cast();
	}
}
