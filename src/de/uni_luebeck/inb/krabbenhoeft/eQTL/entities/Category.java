package de.uni_luebeck.inb.krabbenhoeft.eQTL.entities;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Category implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String category;

	private Category(String string) {
		this.category = string;
	}

	public String getCategory() {
		return category;
	}

	private static Map<String, Category> map = Collections.synchronizedMap(new HashMap<String, Category>());

	public static Category wrap(String string) {
		Category category = map.get(string);
		if (category == null) {
			category = new Category(string);
			map.put(string, category);
		}
		return category;
	}
}
