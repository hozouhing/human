package com.jeff.mvc;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

public class JsonDateValueProcessor implements JsonValueProcessor {

	private SimpleDateFormat sdf;

	public JsonDateValueProcessor() {
		super();
	}

	public JsonDateValueProcessor(SimpleDateFormat dateFormat) {
		super();
		this.sdf = dateFormat;
	}

	@Override
	public Object processArrayValue(Object paramObject,
			JsonConfig paramJsonConfig) {
		return process(paramObject);
	}

	@Override
	public Object processObjectValue(String paramString, Object paramObject,
			JsonConfig paramJsonConfig) {
		return process(paramObject);
	}

	private String process(Object value) {
		String v = "";
		if (value instanceof Date) {
			v =  sdf.format(value);
		}
		return v;
	}

}
