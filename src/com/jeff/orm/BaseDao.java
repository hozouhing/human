package com.jeff.orm;

import java.util.List;
import java.util.Map;

public interface BaseDao<T> {

	public T getById(Object sql);

	public T getObject(String sql);

	public List<T> getList(String sql);

	public boolean update(T t);

	public boolean deleteById(Object id);

	public boolean add(T t);

	public Object exeSql4obj(String sqlName, Object param, Class<?> tClz);

	public List<?> exeSql4list(String sqlName, Object param, Class<?> tClz);

	public Object exeSql4obj(String sqlName, Map<String, Object> params,
			Class<?> tClz);

	public List<?> exeSql4list(String sqlName, Map<String, Object> params,
			Class<?> tClz);

	public T exeSql4obj(String sqlName, Object param);

	public List<T> exeSql4list(String sqlName, Object param);

	public boolean exeSql4bool(String sqlName, Object param);

}
