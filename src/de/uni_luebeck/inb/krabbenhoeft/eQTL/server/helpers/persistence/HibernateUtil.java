package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSet;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.DataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ProcessingParameters;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	static {
		Logger.getRootLogger().setLevel(Level.INFO);

		try {
			// Create the SessionFactory from hibernate.cfg.xml
			sessionFactory = new AnnotationConfiguration().configure().addAnnotatedClass(ColumnForDataSetLayer.class).addAnnotatedClass(DataSet.class).addAnnotatedClass(DataSetLayer.class)
					.addAnnotatedClass(ProcessingParameters.class).buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

}
