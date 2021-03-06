package com.jeff.mvc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.jeff.util.ReflectUtil;

public class DispatcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String redirPath = "redirect:";
	private static MvcContext mvcContext;
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
			if (targetcClass.isAnnotationPresent(ReqDateFormat.class)) {
				String pattern = targetcClass
						.getAnnotation(ReqDateFormat.class).value();
				mvcContext.setUsingReqDateFormat(new SimpleDateFormat(pattern));
			} else if (targetMethod.isAnnotationPresent(ReqDateFormat.class)) {
				String pattern = targetcClass
						.getAnnotation(ReqDateFormat.class).value();
				mvcContext.setUsingReqDateFormat(new SimpleDateFormat(pattern));
			} else
				mvcContext.setUsingReqDateFormat(null);
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
					ajaxResp(returnType, ajaxData, response);
				}
			}
			// 没有ajax标签
			else {
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
					ajaxResp(returnType, ajaxData, response);
				}
			}
		} catch (Exception e) {
			response.sendRedirect(request.getContextPath()
					+ mvcContext.getErrorPath());
			e.printStackTrace();
		}
	}

	private void ajaxResp(Class<?> returnType, Object ajaxData,
			HttpServletResponse response) {
		if (returnType == List.class || returnType == ArrayList.class
				|| returnType == Set.class || returnType == HashSet.class)
			ajax(JSONArray.fromObject(ajaxData, mvcContext.getDateJsonConfig())
					.toString(), "text/html", response);
		else if (returnType == Date.class)
			ajax(mvcContext.getUsingJsonDateFormat().format(ajaxData),
					"text/html", response);
		else {
			ajax(JSONSerializer
					.toJSON(ajaxData, mvcContext.getDateJsonConfig())
					.toString(), "text/html", response);
		}
	}

	/**
	 * 判断是否是基础类型
	 * 
	 * @param type
	 * @return
	 */
	private boolean isCommonType(Class<?> type) {
		if (type == int.class || type == float.class || type == long.class
				|| type == boolean.class)
			return true;
		return false;
	}

	/**
	 * 为自定义的类的实例对象注入属性
	 * 
	 * @param clz
	 * @param reqParams
	 * @return
	 * @throws jException
	 */
	private Object fieldsInject(Class<?> clz, Map<String, String[]> reqParams)
			throws jException {
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

	/**
	 * 获得合适的参数类型和值
	 * 
	 * @param type
	 * @param formValues
	 * @return
	 * @throws jException
	 */
	private Object getArgument(Class<?> type, String[] formValues)
			throws jException {
		Object arg = null;
		if (type == int.class || type == Integer.class)
			arg = Integer.valueOf(formValues[0]);
		else if (type == int[].class || type == Integer[].class) {
			int[] temp = new int[formValues.length];
			for (int i = 0; i < temp.length; i++)
				temp[i] = Integer.valueOf(formValues[i]);
			arg = temp;
		} else if (type == String.class)
			arg = formValues[0];
		else if (type == String[].class)
			arg = formValues;
		else if (type == Date.class) {
			try {
				arg = mvcContext.getUsingReqDateFormat().parse(formValues[0]);
			} catch (ParseException e) {
				e.printStackTrace();
				throw new jException("日期参数转化失败");
			}
		}
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


}
