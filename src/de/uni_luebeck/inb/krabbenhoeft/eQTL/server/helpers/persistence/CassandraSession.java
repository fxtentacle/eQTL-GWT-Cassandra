package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.service.Cassandra;
import org.apache.cassandra.service.ColumnOrSuperColumn;
import org.apache.cassandra.service.ColumnParent;
import org.apache.cassandra.service.ColumnPath;
import org.apache.cassandra.service.ConsistencyLevel;
import org.apache.cassandra.service.InvalidRequestException;
import org.apache.cassandra.service.NotFoundException;
import org.apache.cassandra.service.SlicePredicate;
import org.apache.cassandra.service.SliceRange;
import org.apache.cassandra.service.TimedOutException;
import org.apache.cassandra.service.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class CassandraSession {
	private final TTransport transport = new TSocket("127.0.0.1", 9160);
	private final TProtocol proto = new TBinaryProtocol(transport);
	private final Cassandra.Client client = new Cassandra.Client(proto);
	public final static Charset charset = Charset.forName("UTF-8");

	public CassandraSession() {
		try {
			transport.open();
		} catch (TTransportException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Map<String, List<ColumnOrSuperColumn>>> rowKey2columnFamily2objectsToStore = new HashMap<String, Map<String, List<ColumnOrSuperColumn>>>();

	private Map<String, List<ColumnOrSuperColumn>> columnFamily2objectsToStore(String rowKey) {
		Map<String, List<ColumnOrSuperColumn>> list = rowKey2columnFamily2objectsToStore.get(rowKey);
		if (list == null) {
			list = new HashMap<String, List<ColumnOrSuperColumn>>();
			rowKey2columnFamily2objectsToStore.put(rowKey, list);
		}
		return list;
	}

	public List<ColumnOrSuperColumn> getStoreQueue(String rowKey, String columnFamily) {
		final Map<String, List<ColumnOrSuperColumn>> columnFamily2objectsToStore = columnFamily2objectsToStore(rowKey);
		List<ColumnOrSuperColumn> list = columnFamily2objectsToStore.get(columnFamily);
		if (list == null) {
			list = new ArrayList<ColumnOrSuperColumn>();
			columnFamily2objectsToStore.put(columnFamily, list);
		}
		return list;
	}

	public void addToStoreQueue(String rowKey, String columnFamily, ColumnOrSuperColumn addMe) {
		List<ColumnOrSuperColumn> list = getStoreQueue(rowKey, columnFamily);
		list.add(addMe);
	}

	public static long ts() {
		return new Date().getTime();
	}

	public void flush() {
		for (Map.Entry<String, Map<String, List<ColumnOrSuperColumn>>> rowToStore : rowKey2columnFamily2objectsToStore.entrySet()) {
			while (true) {
				// we retry ad infinitum
				try {
					client.batch_insert("expressionqtl", rowToStore.getKey(), rowToStore.getValue(), ConsistencyLevel.ONE);
					break;
				} catch (InvalidRequestException e) {
					e.printStackTrace();
				} catch (UnavailableException e) {
					e.printStackTrace();
				} catch (TimedOutException e) {
					e.printStackTrace();
				} catch (TException e) {
					e.printStackTrace();
				}
				sleep1s();
			}
			rowToStore.getValue().clear();
		}
	}

	public void close() {
		flush();
		transport.close();
	}

	private void sleep1s() {
		// sleep 1s before retry
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<ColumnOrSuperColumn> getIndex(String indexName, boolean reverse, int limit) {
		final SliceRange slice_range = new SliceRange(new byte[0], new byte[0], reverse, limit);
		final SlicePredicate slice_predicate = new SlicePredicate(null, slice_range);
		while (true) {
			try {
				return client.get_slice("expressionqtl", indexName, new ColumnParent("indices", null), slice_predicate, ConsistencyLevel.ONE);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (UnavailableException e) {
				e.printStackTrace();
			} catch (TimedOutException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			sleep1s();
		}
	}

	public ColumnOrSuperColumn getSuperColumn(String entityGroup, String rowKey, byte[] name) {
		final ColumnPath columnPath = new ColumnPath(entityGroup, name, null);
		while (true) {
			try {
				return client.get("expressionqtl", rowKey, columnPath, ConsistencyLevel.ONE);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (UnavailableException e) {
				e.printStackTrace();
			} catch (TimedOutException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				return null;
			}
			sleep1s();
		}
	}

	public ColumnOrSuperColumn getColumn(String entityGroup, String rowKey, byte[] name) {
		final ColumnPath columnPath = new ColumnPath(entityGroup, null, name);
		while (true) {
			try {
				return client.get("expressionqtl", rowKey, columnPath, ConsistencyLevel.ONE);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (UnavailableException e) {
				e.printStackTrace();
			} catch (TimedOutException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				return null;
			}
			sleep1s();
		}
	}

	public List<ColumnOrSuperColumn> getCompleteRow(String entityGroup, String rowKey) {
		final SliceRange slice_range = new SliceRange(new byte[0], new byte[0], false, 1000 * 1000);
		final SlicePredicate slice_predicate = new SlicePredicate(null, slice_range);
		final ColumnParent columnParent = new ColumnParent(entityGroup, null);
		while (true) {
			try {
				return client.get_slice("expressionqtl", rowKey, columnParent, slice_predicate, ConsistencyLevel.ONE);
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (UnavailableException e) {
				e.printStackTrace();
			} catch (TimedOutException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			sleep1s();
		}
	}
}
