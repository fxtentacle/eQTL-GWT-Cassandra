package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.cassandra.service.Column;
import org.apache.cassandra.service.ColumnOrSuperColumn;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.DasTrackEntry;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.api.gwt.FetchDasTrack;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.CassandraSession;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence.GeoBoxHelper;

public class FetchDasTrackService extends RemoteServiceServlet implements FetchDasTrack {
	private static final long serialVersionUID = 1L;

	public String[] listAvailableTracks() {
		return new String[] { "karyotype", "reference", "transcript" };
	}

	private static NodeList fetchNodes(String type, String segment) {
		Document doc;
		try {
			URL url = new URL("http://www.ensembl.org/das/Mus_musculus.NCBIM37." + type + "/features?segment=" + segment);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(url.toString());
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return doc.getElementsByTagName("FEATURE");
	}

	public List<DasTrackEntry> getCompleteTrack(String type, String chromosome) {
		ArrayList<DasTrackEntry> list = new ArrayList<DasTrackEntry>();
		NodeList nodeLst = fetchNodes(type, chromosome);
		for (int s = 0; s < nodeLst.getLength(); s++) {
			Node featureNode = nodeLst.item(s);
			Element featureElement = (Element) featureNode;

			DasTrackEntry band = new DasTrackEntry();
			band.label = featureElement.getAttribute("id");
			band.from = Long.parseLong(featureElement.getElementsByTagName("START").item(0).getTextContent());
			band.to = Long.parseLong(featureElement.getElementsByTagName("END").item(0).getTextContent());

			if (type.equals("karyotype")) {
				band.type = 1;
				if (featureElement.getElementsByTagName("TYPE").item(0).getTextContent().contains("gvar"))
					band.type = 2;
				if (featureElement.getElementsByTagName("TYPE").item(0).getTextContent().contains("gpos"))
					band.type = 3;
			} else if (type.equals("reference")) {
				if (featureElement.getElementsByTagName("TYPE").item(0).getTextContent().equals("chromosome"))
					continue;

				band.type = s % 2;
			} else if (type.equals("transcript")) {
				band.label = ((Element) featureElement.getElementsByTagName("GROUP").item(0)).getAttribute("label");
				band.type = featureElement.getElementsByTagName("ORIENTATION").item(0).getTextContent().contains("+") ? 0 : 1;
			}

			list.add(band);
		}
		return list;
	}

	// NOTE: ensure ordering based on from field
	public DasTrackEntry[] getTrackForSegment(String trackName, String chromosome, long fromBP, long toBP) {
		int shiftToUse;
		for (shiftToUse = GeoBoxHelper.minShift; shiftToUse <= GeoBoxHelper.maxShift; shiftToUse++) {
			final long boxSize = GeoBoxHelper.getSizeForBox(shiftToUse);
			if (boxSize >= toBP - fromBP)
				break;
		}
		long boxToUse = GeoBoxHelper.getBoxForValue(shiftToUse, fromBP);
		if (shiftToUse > GeoBoxHelper.maxShift)
			boxToUse = 0;

		CassandraSession cassandra = new CassandraSession();
		final String rowKey = trackName + "#" + chromosome + "#" + shiftToUse + "#" + boxToUse;

		List<ColumnOrSuperColumn> completeRow = cassandra.getCompleteRow("dastracks", rowKey);
		if (completeRow.size() == 0) {
			boolean modified = cacheDasTrackForChromosome(cassandra, trackName, chromosome);
			if (modified)
				completeRow = cassandra.getCompleteRow("dastracks", rowKey);
		}

		final List<DasTrackEntry> completeTrack = new ArrayList<DasTrackEntry>();
		for (ColumnOrSuperColumn columnOrSuperColumn : completeRow) {
			final ByteBuffer bb = ByteBuffer.wrap(columnOrSuperColumn.column.value);
			int strlen = bb.limit() - 4 - 8 - 8;

			DasTrackEntry entry = new DasTrackEntry();
			byte[] label = new byte[strlen];
			bb.get(label);
			entry.label = new String(label, CassandraSession.charset);
			entry.type = bb.getInt();
			entry.from = bb.getLong();
			entry.to = bb.getLong();
			completeTrack.add(entry);
		}
		cassandra.close();

		return completeTrack.toArray(new DasTrackEntry[0]);
	}

	private boolean cacheDasTrackForChromosome(CassandraSession cassandra, String trackName, String chromosome) {
		final String trackNameStateRowKey = trackName + "#" + chromosome + "#state";
		final Column statusColumn = new Column("state".getBytes(CassandraSession.charset), "preparing".getBytes(CassandraSession.charset), CassandraSession.ts());

		// read status column, to check if track is done
		ColumnOrSuperColumn statusColRead = cassandra.getColumn("dastracks", trackNameStateRowKey, statusColumn.name);
		if (statusColRead != null) {
			final String stateString = new String(statusColRead.column.value, CassandraSession.charset);
			if (stateString.equals("done"))
				return false;
		} else {
			// since there is no column status
			// store that we would like to prepare it
			cassandra.addToStoreQueue(trackNameStateRowKey, "dastracks", new ColumnOrSuperColumn(statusColumn, null));
			cassandra.flush();
		}

		// wait a second to allow for multiple threads to offer preparing
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// re-read the column status
		statusColRead = cassandra.getColumn("dastracks", trackNameStateRowKey, statusColumn.name);
		String stateString = new String(statusColRead.column.value, CassandraSession.charset);

		// column is done, we're done
		if (stateString.equals("done"))
			return true;

		// column is in preparing, but not by us
		if (stateString.equals("preparing") && statusColRead.column.timestamp != statusColumn.timestamp) {
			while (stateString.equals("preparing")) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				statusColRead = cassandra.getColumn("dastracks", trackNameStateRowKey, statusColumn.name);
				stateString = new String(statusColRead.column.value, CassandraSession.charset);
			}
			return true;
		}

		try {
			// prepare
			final List<DasTrackEntry> completeTrack = getCompleteTrack(trackName, chromosome);
			int counter = 0;
			for (DasTrackEntry entry : completeTrack) {
				final byte[] name = ByteBuffer.allocate(4).putInt(counter++).array();
				final byte[] label = entry.label.getBytes(CassandraSession.charset);
				final byte[] value = ByteBuffer.allocate(label.length + 4 + 8 + 8).put(label).putInt(entry.type).putLong(entry.from).putLong(entry.to).array();

				for (int shift = GeoBoxHelper.minShift; shift <= GeoBoxHelper.maxShift; shift++) {
					final long boxFrom = GeoBoxHelper.getBoxForValue(shift, entry.from);
					final long boxTo = GeoBoxHelper.getBoxForValue(shift, entry.to);

					for (long box = boxFrom; box <= boxTo; box++) {
						final String geobox = trackName + "#" + chromosome + "#" + shift + "#" + box;
						cassandra.addToStoreQueue(geobox, "dastracks", new ColumnOrSuperColumn(new Column(name, value, CassandraSession.ts()), null));
					}
				}
				final String overflowGeobox = trackName + "#" + chromosome + "#" + (GeoBoxHelper.maxShift + 1) + "#0";
				cassandra.addToStoreQueue(overflowGeobox, "dastracks", new ColumnOrSuperColumn(new Column(name, value, CassandraSession.ts()), null));
			}
		} catch (Throwable e) {
		}

		// mark as done, even if we encountered an error
		statusColumn.setValue("done".getBytes(CassandraSession.charset));
		statusColumn.setTimestamp(CassandraSession.ts());
		cassandra.addToStoreQueue(trackNameStateRowKey, "dastracks", new ColumnOrSuperColumn(statusColumn, null));
		cassandra.flush();
		return true;
	}
}
