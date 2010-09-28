package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import org.hibernate.Session;
import org.hibernate.Transaction;

public abstract class RunWithHibernate<T> {
	public T run() {
		final Session sess = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = null;
		final T ret;
		try {
			tx = sess.beginTransaction();
			ret = work(tx, sess);
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null)
				tx.rollback();
			throw new RuntimeException(e);
		} finally {
			sess.close();
		}
		return ret;
	}

	public abstract T work(org.hibernate.Transaction transaction, Session session) throws Exception;

	public String getCurrentUser() {
		return "h@h-yo.de";
	}
}
