package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;

public abstract class CompositeWithChangeEvents extends Composite {

	private HandlerManager handlers = new HandlerManager(null);

	public HandlerRegistration addValueChangeHandlerImpl(ValueChangeHandler<?> handler) {
		return handlers.addHandler(ValueChangeEvent.getType(), handler);
	}

	public HandlerRegistration addSelectionHandlerImpl(SelectionHandler<?> handler) {
		return handlers.addHandler(SelectionEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

}