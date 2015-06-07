package com.jeff.mvc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.jeff.util.ReflectUtil;

public class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String redirPath = "redirect:";
	private static MvcContext mvcContext;
	// private static String errorPath;
	static {
		try {
			mvcContext = MvcContext.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DispatcherServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Map<String, Object> models = new HashMap<String, Object>();
		if (ServletFileUpload.isMultipartContent(request)) {
			request = new UploadWapper(request);
		}
		String url = request.getRequestURL().toString();
		String actionName = url.substring(url.lastIndexOf("/") + 1);
		try {
			// MvcContext mvcContext = MvcContext.getInstance();
			Method targetMethod = mvcContext.getActionMethod(actionName);
			Class<?> targetcClass = targetMethod.getDeclaringClass();
			Object targetInstance = mvcContext.getActionInstance(targetcClass
					.getName());
			Set<UploadFile> uploadFiles = (Set<UploadFile>) request
					.getAttribute("uploadFiles");
			Map<String, String[]> reqParamsMap = request.getParameterMap();
			Class[] paramTypes = targetMethod.getParameterTypes();
			String[] paramNames = ReflectUtil.getMethodParamName(targetcClass,
					targetMethod);
			Object[] args = new Object[paramNames.length];
			for (int i = 0; i < paramNames.length; i++) {
				Class type = paramTypes[i];
				if (type == UploadFile.class || type == UploadFile[].class) {
					UploadFile[] filesArray = new UploadFile[uploadFiles.size()];
					int j = 0;
					for (UploadFile uploadFile : uploadFiles) {
						filesArray[j] = uploadFile;
						j++;
					}
					args[i] = filesArray;
				} else if (type == HttpServletRequest.class) {
					args[i] = request;
				} else if (type == HttpServletResponse.class) {
					args[i] = response;
				} else if (type == HttpSession.class) {
					args[i] = request.getSession();
				} else if (type == Map.class || type == HashMap.class) {
					args[i] = models;
				} else {
					String[] formValues = reqParamsMap.get(paramNames[i]);
					boolean tag1 = (formValues == null);
					boolean tag2 = isCommonType(type);
					// 没有接受到目标参数
					if (tag1) {
						/*
						 * jdk的基本类型全部赋初值（0，false ）
						 * 基本类型的boxing包装类则传null，所以如果不确定前台是否会传参，最好不要用基本类型
						 */
						if (tag2) {
							if (type == int.class || type == float.class
									|| type == long.class)
								args[i] = 0;
							else if (type == boolean.class)
								args[i] = false;
						} else
							args[i] = fieldsInject(type, reqParamsMap);
					} else {
						args[i] = getArgument(type, formValues);
					}
				}
			}
			Class<?> returnType = targetMethod.getReturnType();
			// 如果有ajax标签
			if (targetMethod.isAnnotationPresent(Ajax.class)) {
				if (returnType == Void.class)
					targetMethod.invoke(targetInstance, args);
				else {
					Object ajaxData = targetMethod.invoke(targetInstance, args);
					ajax(JSONObject.fromObject(ajaxData).toString(),
							"text/html", response);
				}
			} else {
				if (returnType == String.class) {
					String page = String.valueOf(targetMethod.invoke(
							targetInstance, args));
					/*
					 * redirect以http://localhost:8080/ 为根路径
					 * forward以http://localhost:8080/project_name 为根路径
					 */
					if (!page.startsWith(redirPath)) {
						this.getServletContext().getRequestDispatcher(page)
								.forward(request, response);
					} else {
						response.sendRedirect(request.getContextPath()
								+ page.substring(redirPath.length()));
					}
				} else if (returnType == Void.class) {
					targetMethod.invoke(targetInstance, args);
				} else {
					Object ajaxData = targetMethod.invoke(targetInstance, args);
					ajax(JSONObject.fromObject(ajaxData).toString(),
							"text/html", response);
				}
			}
		} catch (Exception e) {
			response.sendRedirect(request.getContextPath()
					+ mvcContext.getErrorPath());
			e.printStackTrace();
		}
	}

	private boolean isCommonType(Class<?> type) {
		if (type == int.class || type == float.class || type == long.class
				|| type == boolean.class)
			return true;
		return false;
	}

	private Object fieldsInject(Class<?> clz, Map<String, String[]> reqParams) {
		Object instance = null;
		try {
			instance = clz.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		Field[] fields = clz.getDeclaredFields();
		String fieldName;
		Class<?> fieldType;
		String[] formValues;
		Object arg;
		String setter;
		for (Field field : fields) {
			fieldName = field.getName();
			if (reqParams.containsKey(fieldName)) {
				fieldType = field.getType();
				formValues = reqParams.get(fieldName);
				arg = getArgument(fieldType, formValues);
				setter = "set" + fieldName.substring(0, 1).toUpperCase()
						+ fieldName.substring(1);
				try {
					Method method = clz.getMethod(setter,
							new Class[] { fieldType });
					method.invoke(instance, arg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return instance;
	}

	private Object getArgument(Class<?> declaringClass, String[] formValues) {
		Object arg = null;
		if (declaringClass == int.class || declaringClass == Integer.class)
			arg = Integer.valueOf(formValues[0]);
		else if (declaringClass == int[].class
				|| declaringClass == Integer[].class) {
			int[] temp = new int[formValues.length];
			for (int i = 0; i < temp.length; i++)
				temp[i] = Integer.valueOf(formValues[i]);
			arg = temp;
		} else if (declaringClass == String.class)
			arg = formValues[0];
		else if (declaringClass == String[].class)
			arg = formValues;
		return arg;
	}

	/**
	 * AJAX输出，返回null
	 * 
	 * @param content
	 *            需要传出去的内容
	 * @param type
	 *            Ajax输出类型 ： 1、"text/plain"(输出文本) <br>
	 *            2、text/html(输出HTML) <br>
	 *            3、text/xml(输出XML)
	 * @return null
	 */
	public String ajax(String content, String type, HttpServletResponse response) {
		try {
			response.setContentType(type + ";charset=UTF-8");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.getWriter().write(content);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ------------------------------------------------------------------------------------------------------------------

}