package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.ProcessWithR;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CassandraSession;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.RunWithHibernate;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.StreamingEntityRead;

public class ProcessWithRService extends RemoteServiceServlet implements ProcessWithR {
	private static final long serialVersionUID = 1L;

	public String callR(final int dataSetLayerKey, String input) {
		final RSingleton r = RSingleton.instance();
		r.clearConsoleOutput();

		new RunWithHibernate<Void>() {
			public Void work(Transaction transaction, Session session) throws Exception {
				DataSetLayer dsl = (DataSetLayer) session.load(DataSetLayer.class, dataSetLayerKey);

				CassandraSession cassandra = new CassandraSession();
				StreamingEntityRead read = new StreamingEntityRead(cassandra, dsl);

				r.assingData(dsl.getColumns(), read.getEntitiesFromSearchIndex("lodScore", false, 0, 1000));

				cassandra.close();
				return null;
			}
		}.run();

		String[] lines = input.split("[\\r\\n]");
		for (String line : lines) {
			r.eval(line);
		}
		return r.getConsoleOutput();
	}
}
