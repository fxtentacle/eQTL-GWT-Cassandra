package de.uni_luebeck.inb.krabbenhoeft.eQTL.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabButtonRow extends FlowPanel {
	boolean firstAdd = true;

	@Override
	public void add(Widget w) {
		if (w instanceof TabButton) {
			final TabButton tabButton = (TabButton) w;
			tabButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					selectTab(tabButton);
				}
			});

			tabButton.setSelected(firstAdd);
			firstAdd = false;
		}
		super.add(w);
	}

	protected void selectTab(TabButton tabButtonToSet) {
		for (Widget widget : getChildren()) {
			if (widget instanceof TabButton) {
				final TabButton tabButton = (TabButton) widget;
				tabButton.setSelected(tabButtonToSet == tabButton);
			}
		}
	}
}
