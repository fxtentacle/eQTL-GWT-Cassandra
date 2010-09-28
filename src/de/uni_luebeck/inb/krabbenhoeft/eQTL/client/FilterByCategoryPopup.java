package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class FilterByCategoryPopup extends Composite implements HasSelectionHandlers<String> {

	private static FilterByCategoryPopupUiBinder uiBinder = GWT.create(FilterByCategoryPopupUiBinder.class);

	interface FilterByCategoryPopupUiBinder extends UiBinder<Widget, FilterByCategoryPopup> {
	}

	private String[] configurationOptions;

	@UiField
	ListBox category, value;

	public FilterByCategoryPopup(String configurationOptions) {
		this.configurationOptions = configurationOptions.split(",");
		initWidget(uiBinder.createAndBindUi(this));

		Set<String> cats = new HashSet<String>();
		for (String part : this.configurationOptions) {
			final String[] split = part.split("=");
			cats.add(split[0]);
		}

		for (String cat : cats) {
			category.addItem(cat);
		}
		onChange(null);
	}

	@UiHandler("category")
	void onChange(ChangeEvent e) {
		final String cat = category.getValue(category.getSelectedIndex());

		value.clear();
		for (String part : configurationOptions) {
			final String[] split = part.split("=");
			if (split[0].equals(cat))
				value.addItem(split[1]);
		}
	}

	@UiHandler("OK")
	void onClick(ClickEvent e) {
		final String cat = category.getValue(category.getSelectedIndex());
		final String val = value.getValue(value.getSelectedIndex());

		SelectionEvent.fire(this, cat + "=" + val);
	}

	private HandlerManager handlers = new HandlerManager(null);

	public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler) {
		return handlers.addHandler(SelectionEvent.getType(), handler);
	}

	public void fireEvent(GwtEvent<?> event) {
		handlers.fireEvent(event);
	}

}
