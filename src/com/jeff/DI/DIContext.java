package com.jeff.DI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jeff.mvc.TransactionHandler;
import com.jeff.mvc.jException;
import com.jeff.util.PropertiesUtil;
import com.jeff.util.ReflectUtil;

public class DIContext {

	private static final Logger logger = Logger.getLogger(DIContext.class);

	private static final String pNamesStr = PropertiesUtil.getProp()
			.getProperty("di_package");

	private static final String txAdviseStr = PropertiesUtil.getProp()
			.getProperty("transaction_advise");

	private static DIContext diContext;

	private DIContext() {
	}

	private Map<String, Object> diInstances = new HashMap<String, Object>();

	private Set<String> txMethods = new HashSet<String>();

	public Set<String> getTxMethods() {
		return txMethods;
	}

	public Object getDiInstance(String diName) {
		return diInstances.get(diName);
	}

	public static DIContext getInstance() {
		if (diContext == null) {
			diContext = new DIContext();
			try {
				diContext.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return diContext;
	}

	private boolean txCheckMth(String pName, String cName, String mName) {
		String adx = txCheckClz(pName, cName);
		if (adx == null)
			return false;
		int index1 = adx.indexOf("[");
		int index2 = adx.indexOf("]");
		String clzAndMth = adx.substring(index1 + 1, index2);
		String mthName = clzAndMth.split("[.]")[1];
		int mPos = 0;
		String mMatch = mthName;
		if (mthName.startsWith("*")) {
			mPos = 1;// 前
			mMatch = mthName.substring(1);
		} else if (mthName.endsWith("*")) {
			mPos = 2;// 后
			mMatch = mthName.substring(0, mthName.indexOf("*"));
		} else if (mthName.equals("*")) {
			mPos = 3;// 全部
		}
		/**
		 * 过滤一些不必要的方法
		 */
		// System.out.println(mName);
		if (mName.equals("getClass") || mName.equals("equals")
				|| mName.equals("toString") || mName.equals("hashCode")
				|| mName.equals("wait") || mName.equals("notify")
				|| mName.equals("notifyAll"))
			return false;
		boolean m1, m2, m3, m4;
		m1 = (mPos == 0 && mName.equals(mMatch));
		m2 = (mPos == 1 && mName.endsWith(mMatch));
		m3 = (mPos == 2 && mName.startsWith(mMatch));
		m4 = (mPos == 3);
		if (m1 || m2 || m3 || m4)
			return true;
		return false;
	}

	private String txCheckClz(String pName, String cName) {
		String[] tx = txAdviseStr.split("[,]");
		boolean c1, c2, c3, c4;
		int index1, index2, cPos;
		String tpName, clzName, clzAndMth, cMatch;
		for (String adx : tx) {
			index1 = adx.indexOf("[");
			index2 = adx.indexOf("]");
			tpName = adx.substring(0, index1);
			if (!tpName.equals(pName))
				return null;
			clzAndMth = adx.substring(index1 + 1, index2);
			clzName = clzAndMth.split("[.]")[0];
			cPos = 0;
			cMatch = clzName;
			if (clzName.startsWith("*")) {
				cPos = 1;// 前
				cMatch = clzName.substring(1);
			} else if (clzName.endsWith("*")) {
				cPos = 2;// 后
				cMatch = clzName.substring(0, clzName.indexOf("*"));
			} else if (clzName.equals("*")) {
				cPos = 3;// 全部
			}
			c1 = (cPos == 0 && cName.equals(cMatch));
			c2 = (cPos == 1 && cName.endsWith(cMatch));
			c3 = (cPos == 2 && cName.startsWith(cMatch));
			c4 = (cPos == 3);
			if (c1 || c2 || c3 || c4)
				return adx;
		}
		return null;
	}

	private void init() throws Exception {
		String[] pNames = pNamesStr.split("[,]");
		for (String pName : pNames) {
			String[] ps = ReflectUtil.getClassByPackage(pName);
			for (String p : ps) {
				String pc = pName + "."
						+ p.substring(0, p.lastIndexOf(".class"));
				Class<?> clz = Class.forName(pc);
				if (!clz.isAnnotationPresent(Resource.class))
					continue;
				// 创建实例
				Object instance = clz.newInstance();
				if (txCheckClz(pName, pc) != null) {
					/**
					 * 转为代理类实例
					 */
					instance = Proxy.newProxyInstance(this.getClass()
							.getClassLoader(), clz.getInterfaces(),
							new TransactionHandler(instance));
					/**
					 * 加入事务方法集
					 */
					Method[] methods = clz.getMethods();
					String mName;
					for (Method m : methods) {
						mName = m.getName();
						if (txCheckMth(pName, pc, mName)) {
							/**
							 * m.getDeclaringClass()知道这个方法是从那个class继承而来的
							 */
							txMethods
									.add(m.getDeclaringClass().getInterfaces()[0]
											.getName() + "." + mName);
							logger.info("a method called "
									+ m.getDeclaringClass().getInterfaces()[0]
											.getName() + "." + mName
									+ " had been add into txMethod Set...");
						} else
							continue;
					}
				}
				Resource v1 = clz.getAnnotation(Resource.class);
				diInstances.put(v1.value(), instance);
				logger.info("An instance for the class called " + clz.getName()
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
							logger.error("The alias for every DI resource is required...");
							throw new jException(
									"The alias for every DI resource is required...");
						}
						Object object = diContext.getDiInstance(diName);
						if (o == null) {
							logger.error("Failed to find the instance for the DI resource called "
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
							logger.error("Faied to find the setter method for the DI resource "
									+ diName + "...");
							throw new jException(
									"Faied to find the setter method for the DI resource "
											+ diName + "...");
						}
						method.invoke(o, object);
						logger.info("succeed to inject the required DI resource correctly...");
					}
				}
			}
		}
	}

}
