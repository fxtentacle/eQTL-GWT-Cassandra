package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.restlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.servlet.ServletAdapter;


public class HajosRestletServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected ServletAdapter adapter;

	@Override
	public void init() throws ServletException {
		Context context = new Context();
		Application application = new Application();
		application.setContext(context);
		application.setInboundRoot(new UrlToRestlet(context));
		adapter = new ServletAdapter(getServletContext());
		adapter.setNext(application);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		adapter.service(request, response);
	}

}