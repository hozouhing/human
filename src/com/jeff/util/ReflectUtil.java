package com.jeff.util;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class ReflectUtil {
	/**
	 * 通过包名获取类的名字的组合
	 */
	public static String[] getClassByPackage(String pName) {
		String pr = pName.replace(".", "/");
		String pp = ReflectUtil.class.getClassLoader().getResource(pr)
				.getPath();
		File file = new File(pp);
		String[] fs = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".class"))
					return true;
				return false;
			}
		});
		return fs;
	}

	/**
	 * 通过javassist反射增强工具获取方法参数的名字，因为默认的class文件不会保存参数名（JDK1.8之后会保存）
	 * 
	 * @param targetcClass
	 * @param method
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NotFoundException
	 */
	public static String[] getMethodParamName(Class<?> targetcClass,
			Method method) throws ClassNotFoundException, NotFoundException {
		/**
		 * C/S ClassPool pool =
		 * ClassPool.getDefault();因为default的classPath就是JVM的classPath（JDK路径下）
		 * 但B/S ClassPool对于的路径不是JVM的classPath而是tomcat的classPath
		 * 所以要这样获取：pool.insertClassPath(new ClassClassPath(ReflectUtil.class));
		 */
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(ReflectUtil.class));
		CtClass cc = pool.get(targetcClass.getName());
		CtMethod cm = cc.getDeclaredMethod(method.getName());
		// 使用javassist反射增强工具
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		String[] paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++)
			paramNames[i] = attr.variableName(i + pos);
		return paramNames;
	}

}
