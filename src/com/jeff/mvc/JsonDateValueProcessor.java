package com.jeff.mvc;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JsonDateValueProcessor implements JsonValueProcessor {
	private SimpleDateFormat format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public SimpleDateFormat getFormat() {
		return format;
	}

	//配合链式编程
	public JsonDateValueProcessor setFormat(SimpleDateFormat format) {
		this.format = format;
		return this;
	}

	private static JsonDateValueProcessor jdvp = new JsonDateValueProcessor();

	private JsonDateValueProcessor() {

	}

	public static JsonDateValueProcessor getInstance() {
		return jdvp;
	}

	public Object processArrayValue(Object value, JsonConfig jsonConfig) {
		String[] obj = {};
		if (value instanceof Date[]) {
			Date[] dates = (Date[]) value;
			obj = new String[dates.length];
			for (int i = 0; i < dates.length; i++) {
				obj[i] = format.format(dates[i]);
			}
		}
		return obj;
	}

	public Object processObjectValue(String key, Object value,
			JsonConfig jsonConfig) {
		if (value instanceof Date) {
			String str = format.format((Date) value);
			return str;
		}
		return value.toString();
	}

}