package com.jeff.DI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jeff.mvc.jException;
import com.jeff.util.PropertiesUtil;
import com.jeff.util.ReflectUtil;

public class DIContext {

	private static final Logger loggger = Logger.getLogger(DIContext.class);

	private static DIContext diContext;

	private DIContext() {
	}

	private Map<String, Object> diInstances = new HashMap<String, Object>();

	public Object getDiInstance(String diName) {
		return diInstances.get(diName);
	}

	public static DIContext getInstance() {
		if (diContext == null) {
			diContext = new DIContext();
			try {
				diContext.initDIContext();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return diContext;
	}

	private void initDIContext() throws Exception {
		loggger.info("Begin to initailize the DI context...");
		String pName = PropertiesUtil.getProp().getProperty("di_package");
		if (pName.equals(""))
			loggger.info("Could not find the property of the key called package...");
		String[] ps = ReflectUtil.getClassByPackage(pName);
		for (String p : ps) {
			String pc = pName + "." + p.substring(0, p.lastIndexOf(".class"));
			Class<?> clz = Class.forName(pc);
			if (!clz.isAnnotationPresent(Resource.class))
				continue;
			Object instance = clz.newInstance();
			Resource v1 = clz.getAnnotation(Resource.class);
			diInstances.put(v1.value(), instance);
			loggger.info("An instance for the class called " + clz.getName()
					+ " had been put into DI context...");
		}
		for (Object o : diInstances.values()) {
			Class<?> clz = o.getClass();
			Field[] fields = clz.getFields();
			DI di;
			String setterName;
			String diName;
			for (Field field : fields) {
				if (field.isAnnotationPresent(DI.class)) {
					di = field.getAnnotation(DI.class);
					diName = di.value();
					if (diName.equals("")) {
						loggger.error("The alias for every DI resource is required...");
						throw new jException(
								"The alias for every DI resource is required...");
					}
					Object object = diContext.getDiInstance(diName);
					if (o == null) {
						loggger.error("Failed to find the instance for the DI resource called "
								+ diName + "...");
						throw new jException(
								"Failed to find the instance for the DI resource called "
										+ diName + "...");
					}
					setterName = "set"
							+ field.getName().substring(0, 1).toUpperCase()
							+ field.getName().substring(1);
					Method method = clz.getMethod(setterName,
							new Class[] { field.getClass() });
					if (method == null) {
						loggger.error("Faied to find the setter method for the DI resource "
								+ diName + "...");
						throw new jException(
								"Faied to find the setter method for the DI resource "
										+ diName + "...");
					}
					method.invoke(o, object);
					loggger.info("succeed to inject the required DI resource correctly...");
				}
			}
		}
		loggger.info("Initailized the DI context successfully...");
	}

}
