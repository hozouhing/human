package com.jeff.orm;

import java.util.List;

public interface BaseDao<T> {

	public T getById(Object sql);

	public T getObject(String sql);

	public List<T> getList(String sql);

	public boolean update(T t);

	public boolean deleteById(Object id);

	public boolean add(T t);

	public Object exeSql4obj(String sqlName, Object params, Class<?> clz);

	public List<? extends Object> exeSql4list(String sqlName, Object params,
			Class<?> clz);

	public T exeSql4obj(String sqlName, Object params);

	public List<T> exeSql4list(String sqlName, Object params);

	public boolean exeSql4bool(String sqlName, Object params);

}
