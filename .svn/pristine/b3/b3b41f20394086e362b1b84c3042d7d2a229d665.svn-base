package com.jeff.mvc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.jeff.orm.OrmContext;

/**
 * 使用jdk默认代理类实现AOP插入事务管理
 * 
 * @author jeff he
 *
 */
public class TransationHandler implements InvocationHandler {

	private static final Logger logger = Logger
			.getLogger(TransationHandler.class);

	private Object target;

	public TransationHandler(Object target) {
		super();
		this.target = target;
	}

	private void openTransation() {
		try {
			OrmContext.getCurrentConnection().setAutoCommit(false);
			logger.info("Began a transaction.......");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void closeTransation() {
		try {
			OrmContext.getCurrentConnection().commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				OrmContext.getCurrentConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				OrmContext.getCurrentConnection().setAutoCommit(true);
				logger.info("Committed a transaction.......");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		openTransation();
		result = method.invoke(target, args);
		closeTransation();
		return result;
	}

}
