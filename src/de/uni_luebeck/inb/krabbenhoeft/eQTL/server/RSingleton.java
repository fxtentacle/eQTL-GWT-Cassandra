package de.uni_luebeck.inb.krabbenhoeft.eQTL.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.ColumnForDataSetLayer;
import de.uni_luebeck.inb.krabbenhoeft.eQTL.entities.HajoEntity;

public class RSingleton {
	private static RSingleton singleton;
	private Rengine rEngine;

	public synchronized static RSingleton instance() {
		if (singleton == null)
			singleton = new RSingleton();
		return singleton;
	}

	StringBuilder consoleOutput = new StringBuilder();

	private RSingleton() {
		if (!Rengine.versionCheck())
			throw new RuntimeException("Rengine.versionCheck() failed");

		rEngine = new Rengine(new String[] { "--vanilla" }, false, new CB());
		if (!rEngine.waitForR())
			throw new RuntimeException("Rengine.waitForR() failed");
	}

	public void clearConsoleOutput() {
		consoleOutput.setLength(0);
	}

	public String getConsoleOutput() {
		return consoleOutput.toString();
	}

	public void assingData(List<ColumnForDataSetLayer> columns, Iterator<HajoEntity> entities) {
		Map<String, List<String>> col2list = new HashMap<String, List<String>>();
		for (ColumnForDataSetLayer col : columns) {
			col2list.put(col.getName(), new ArrayList<String>());
		}

		List<String> rowNames = new ArrayList<String>();
		int count = 1000;
		int counter = 0;
		while (entities.hasNext() && count-- > 0) {
			final HajoEntity entity = entities.next();
			for (ColumnForDataSetLayer col : columns) {
				final List<String> list = col2list.get(col.getName());
				switch (col.getType()) {
				case Category:
					list.add(entity.getCategory(col.getName()).getCategory());
					break;
				case Name:
					list.add(entity.getName(col.getName()));
					break;
				case Location:
					list.add(Long.toString(entity.getLocation(col.getName())));
					break;
				case Numerical:
					list.add(Double.toString(entity.getNumerical(col.getName())));
					break;
				}
			}
			rowNames.add(Integer.toString(counter++));
		}

		String[] names = new String[columns.size()];
		long[] refs = new long[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			final String colname = columns.get(i).getName();
			final List<String> list = col2list.get(colname);
			names[i] = colname;
			refs[i] = rEngine.rniPutStringArray(list.toArray(new String[0]));
		}

		long vec = rEngine.rniPutVector(refs);
		long nameRef = rEngine.rniPutStringArray(names);
		rEngine.rniSetAttr(vec, "names", nameRef);

		long rowNameRef = rEngine.rniPutStringArray(rowNames.toArray(new String[0]));
		rEngine.rniSetAttr(vec, "row.names", rowNameRef);
		rEngine.rniSetAttr(vec, "class", rEngine.rniPutString("data.frame"));

		rEngine.rniAssign("data", vec, 0);

	}

	public void eval(String evalMe) {
		consoleOutput.append("> ");
		consoleOutput.append(evalMe);
		consoleOutput.append("\n");

		final REXP eval = rEngine.eval(evalMe);
		if (eval != null)
			rEngine.rniPrintValue(eval.xp);

		consoleOutput.append("\n");
	}

	class CB implements RMainLoopCallbacks {
		public void rBusy(Rengine arg0, int arg1) {
		}

		public String rChooseFile(Rengine arg0, int arg1) {
			return null;
		}

		public void rFlushConsole(Rengine arg0) {
		}

		public void rLoadHistory(Rengine arg0, String arg1) {
		}

		public String rReadConsole(Rengine arg0, String arg1, int arg2) {
			return null;
		}

		public void rSaveHistory(Rengine arg0, String arg1) {
		}

		public void rShowMessage(Rengine arg0, String arg1) {
			consoleOutput.append(arg1);
		}

		public void rWriteConsole(Rengine arg0, String arg1, int arg2) {
			consoleOutput.append(arg1);
		}
	}
}
