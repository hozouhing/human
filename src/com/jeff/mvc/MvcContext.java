package com.jeff.mvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jeff.DI.DI;
import com.jeff.DI.DIContext;
import com.jeff.util.PropertiesUtil;
import com.jeff.util.ReflectUtil;

public class MvcContext {

	private static Logger logger = Logger.getLogger(MvcContext.class);

	private static MvcContext mvcContext;

	private MvcContext() {
		diContext = DIContext.getInstance();
	}

	private Map<String, Object> instances = new HashMap<String, Object>();
	private Map<String, Method> actions = new HashMap<String, Method>();
	private DIContext diContext;
	private String errorPath;
	private String defaultUploadPath;

	public static MvcContext getInstance() throws Exception {
		if (mvcContext == null) {
			mvcContext = new MvcContext();
			mvcContext.initContext();
		}
		return mvcContext;
	}

	private void initContext() throws Exception {
		initInstancesAndActions();
		initErrorPath();
		initDefaultUploadPath();
	}

	public void initWebContext(HttpServletRequest req, HttpServletResponse resp) {
	}

	/**
	 * 初始化instance和action资源
	 * 
	 * @throws Exception
	 */
	private void initInstancesAndActions() throws Exception {
		logger.info("Begin to initial the mvc context...");
		String pName = PropertiesUtil.getProp().getProperty("mvc_package");
		if (pName.equals(""))
			logger.info("Can not get the property of the key,package...");
		String[] ps = ReflectUtil.getClassByPackage(pName);
		for (String p : ps) {
			String pc = pName + "." + p.substring(0, p.lastIndexOf(".class"));
			// 得到了类的class对象
			Class<?> clz = Class.forName(pc);
			if (!clz.isAnnotationPresent(MVC.class))
				continue;
			Object instance = clz.newInstance();
			Field[] fields = clz.getDeclaredFields();
			DI di;
			String setterName;
			String diName;
			for (Field field : fields) {
				if (field.isAnnotationPresent(DI.class)) {
					di = field.getAnnotation(DI.class);
					diName = di.value();
					Object o = diContext.getDiInstance(diName);
					if (o == null) {
						logger.error("Can not get the instance of the DI resource from the DI conext");
						throw new jException("找不到要注入资源实例");
					}
					setterName = "set"
							+ field.getName().substring(0, 1).toUpperCase()
							+ field.getName().substring(1);
					Method method = clz.getMethod(setterName,
							new Class[] { field.getType() });
					if (method == null) {
						logger.error("Can not find \"getter\" method of required DI resource called "
								+ diName);
						throw new jException("找不到要注入资源实例");
					} else {
						method.invoke(instance, o);
					}
				}
			}
			instances.put(clz.getName(), instance);
			// System.out.println("加入单例资源");
			logger.info("An instance called " + clz.getName()
					+ " has been put into the instance context...");
			Method[] ms = clz.getDeclaredMethods();
			for (Method m : ms) {
				if (!m.isAnnotationPresent(MVC.class))
					continue;
				MVC m2 = m.getAnnotation(MVC.class);
				String url = "";
				if (m2.value() != null) {
					url = m2.value();
				} else {
					logger.error("The url of action can not be null...");
					throw new jException("方法的url不能为空");
				}
				actions.put(url, m);
				logger.info("An action called " + url
						+ " has been put into the action context...");
			}
		}
		logger.info("Initial the mvc context successfully...");
	}

	/**
	 * 从properties获取errorPath并初始化
	 */
	private void initErrorPath() {
		logger.info("Searching for the defined error page path");
		String errorPath = PropertiesUtil.getProp().getProperty("error_page_path");
		if (errorPath == null) {
			logger.info("Found to be null and configured to be the default path of \"/404.jsp\"");
			errorPath = "/404.jsp";
		} else
			logger.info("Found to be \"" + errorPath + "\"");
		this.errorPath = errorPath;
	}

	/**
	 * 从properties获取defaultUploadPath并初始化
	 */
	private void initDefaultUploadPath() {
		logger.info("Searching for the defined default upload path");
		String defaultUploadPath = PropertiesUtil.getProp().getProperty(
				"default_upload_path");
		if (defaultUploadPath == null) {
			logger.info("Found to be null and configured to be the default path of \"/d:\\\"");
			defaultUploadPath = "D:\\";
		} else
			logger.info("Found to be \"" + defaultUploadPath + "\"");
		this.defaultUploadPath = defaultUploadPath;
	}

	/**
	 * 暴露获取404页面路径的public方法
	 * 
	 * @return
	 */
	public String getErrorPath() {
		return errorPath;
	}

	/**
	 * 暴露的从context的instances中获取特定的class的实例的public方法
	 * 
	 * @param name
	 * @return
	 */
	public Object getActionInstance(String name) {
		return instances.get(name);
	}

	/**
	 * 暴露的从context的action中获取特定的method的public方法
	 * 
	 * @param name
	 * @return
	 */
	public Method getActionMethod(String name) {
		return actions.get(name);
	}

	/**
	 * 暴露的从context中获取默认上传文件存放路径的public方法
	 * 
	 * @return
	 */
	public String getDefaultUploadPath() {
		return this.defaultUploadPath;
	}

	public Map<String, Method> getActions() {
		return actions;
	}

	public Map<String, Object> getInstances() {
		return instances;
	}
}
