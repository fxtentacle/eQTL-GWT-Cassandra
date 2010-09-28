package de.uni_luebeck.inb.krabbenhoeft.eQTL.server.helpers.persistence;

import java.util.Random;

public class GeoBoxHelper {
	public final static int minShift = 12;
	public final static int maxShift = 30;

	public static long getBoxForValue(int shift, long value) {
		return value >> shift;
	}

	public static long getSizeForBox(int shift) {
		return 1 << shift;
	}

	public static long getStartForBox(int shift, long box) {
		return box << shift;
	}

	public static void main(String[] args) {
		Random r = new Random();

		for (int i = 0; i < 10000000; i++) {
			long val = r.nextLong();
			for (int s = minShift; s < maxShift; s++) {
				long box = getBoxForValue(s, val);
				long start = getStartForBox(s, box);
				long size = getSizeForBox(s);

				if (start > val)
					throw new RuntimeException();
				if (val >= start + size)
					throw new RuntimeException();
			}
		}
	}

	public static String geobox1D(String layerKey, String columnName, int shift, String chromosome, long box) {
		return layerKey + "#" + columnName + "#" + chromosome + "#" + Integer.toString(shift) + "#" + Long.toString(box);
	}

	public static String geobox2D(String layerKey, String columnName1, String columnName2, int shift, String chromosome, long box1, long box2) {
		return layerKey + "#" + columnName1 + ":" + columnName2 + "#" + chromosome + ":" + chromosome + "#" + Integer.toString(shift) + "#" + Long.toString(box1) + ":" + Long.toString(box2);
	}

}
