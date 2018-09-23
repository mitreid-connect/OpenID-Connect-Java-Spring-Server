package org.mitre.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadUtils {

	private static ThreadLocal<Map<String, Object>> threadAttrs = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	public static Object get(String key) {
		return threadAttrs.get().get(key);
	}

	public static void set(String key, Object value) {
		threadAttrs.get().put(key, value);
	}
}
