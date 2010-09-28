package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.restlet;

import org.restlet.Context;
import org.restlet.routing.Router;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.DataImportResource;

public class UrlToRestlet extends Router {
	public UrlToRestlet(Context context) {
		super(context);
		attachRoutes();
	}

	protected void attachRoutes() {
		attach("/{token}/data_import", DataImportResource.class);
	}
}