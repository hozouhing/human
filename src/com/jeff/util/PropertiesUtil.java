package com.jeff.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties jdbcProp;
	private static Properties diProp;
	private static Properties mvcProp;

	public static Properties getJdbcProp() {
		try {
			if (jdbcProp == null) {
				jdbcProp = new Properties();
				jdbcProp.load(PropertiesUtil.class.getClassLoader()
						.getResourceAsStream("jdbc.properties"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jdbcProp;
	}

	public static Properties getDIProp() {
		try {
			if (diProp == null) {
				diProp = new Properties();
				diProp.load(PropertiesUtil.class.getClassLoader()
						.getResourceAsStream("DI.properties"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return diProp;
	}

	public static Properties getMvcProp() {
		try {
			if (mvcProp == null) {
				mvcProp = new Properties();
				mvcProp.load(PropertiesUtil.class.getClassLoader()
						.getResourceAsStream("mvc.properties"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mvcProp;
	}

}
